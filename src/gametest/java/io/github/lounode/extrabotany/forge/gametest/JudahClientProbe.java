package io.github.lounode.extrabotany.forge.gametest;

import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.lounode.extrabotany.client.renderer.IconModelRenderer;
import io.github.lounode.extrabotany.client.renderer.entity.JudahOathRenderer;
import io.github.lounode.extrabotany.common.entity.judah.JudahOathEntity;
import io.github.lounode.extrabotany.common.item.relic.JudahOathItem.Variant;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Opt-in client regression using the registered entity renderer and its actual baked-model vertices. */
@EventBusSubscriber(modid = "extrabotany", value = Dist.CLIENT)
public final class JudahClientProbe {
	private static final int FULL_BRIGHT = 0xF000F0;
	private static final Map<String, Object> checks = new LinkedHashMap<>();
	private static int stage, ticks, total;

	@SubscribeEvent
	public static void tick(ClientTickEvent.Post event) {
		if (!Boolean.getBoolean("extrabotany.judahClientProbe") || stage == 99) return;
		Minecraft mc = Minecraft.getInstance();
		try {
			if (++total > 4800) throw new AssertionError("Judah probe timeout at stage " + stage);
			mc.options.pauseOnLostFocus = false;
			mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
			if (stage == 0 && mc.screen instanceof net.minecraft.client.gui.screens.AccessibilityOnboardingScreen) {
				mc.options.onboardingAccessibilityFinished();
				mc.setScreen(new TitleScreen());
			}
			if (stage == 0 && mc.screen instanceof TitleScreen && mc.getOverlay() == null) {
				stage = 1;
				mc.createWorldOpenFlows().createFreshLevel("extrabotany-judah-probe",
						new LevelSettings("Judah renderer regression", GameType.CREATIVE, false, Difficulty.NORMAL, true, new GameRules(), WorldDataConfiguration.DEFAULT),
						new WorldOptions(20261025L, false, false),
						r -> r.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(), new TitleScreen());
				return;
			}
			if (mc.level == null || mc.player == null) return;
			if (stage == 1) {
				if (mc.screen != null || mc.getOverlay() != null || ++ticks < 30) return;
				checks.put("renderer", org.lwjgl.opengl.GL11.glGetString(org.lwjgl.opengl.GL11.GL_RENDERER));
				mc.setScreen(new JudahScreen(0));
				stage = 2;
				ticks = 0;
				return;
			}
			if (stage == 2 && ++ticks >= 30) {
				capture(mc, "judah-front.png");
				mc.setScreen(new JudahScreen(45));
				stage = 3;
				ticks = 0;
				return;
			}
			if (stage == 3 && ++ticks >= 30) {
				capture(mc, "judah-angle.png");
				verifyGeometry(mc);
				finish(mc, null);
			}
		} catch (Throwable error) {
			finish(mc, error);
		}
	}

	private static JudahOathEntity oath(Minecraft mc, Variant variant, float angle) {
		return new JudahOathEntity(mc.level, mc.player, variant, 7, angle);
	}

	private static void verifyGeometry(Minecraft mc) {
		for (Variant variant : Variant.values()) {
			var model = mc.getModelManager().getModel(ModelResourceLocation.standalone(JudahOathRenderer.model(variant)));
			check(model != mc.getModelManager().getMissingModel(), "registered_model_" + variant);
			for (float angle : new float[] {0, 45, 90, 137}) {
				JudahOathEntity oath = oath(mc, variant, angle);
				VertexCapture fixed = new VertexCapture();
				mc.getEntityRenderDispatcher().getRenderer(oath).render(oath, 0, 0, new PoseStack(), type -> fixed, FULL_BRIGHT);
				VertexCapture legacy = new VertexCapture();
				renderLegacy(oath, new PoseStack(), type -> legacy);
				String key = variant + "_" + (int) angle;
				double seamDistance = seamDistance(fixed.vertices, angle, "fixed_" + key);
				double oldDistance = seamDistance(legacy.vertices, angle, "legacy_" + key);
				checks.put("emitted_vertices_" + key, fixed.vertices.size());
				checks.put("seam_distance_" + key, seamDistance);
				checks.put("legacy_seam_distance_" + key, oldDistance);
				check(oldDistance > 2.5, "legacy_transform_reproduces_split_" + key);
				check(seamDistance < 0.0001, "rendered_halves_meet_" + key);
			}
		}
	}

	private static double seamDistance(List<Vertex> vertices, float angle, String key) {
		double radians = Math.toRadians(angle);
		double sin = Math.sin(radians), cos = Math.cos(radians);
		double distance = 0;
		// ItemModelGenerator creates NORTH/SOUTH image faces and separate pixel-edge walls.
		// NeoForge adjusts those walls' positions and UVs independently. Only the image
		// faces' maximum U identifies the seam; the maximum across all quads does not.
		for (int side : new int[] {-1, 1}) {
			List<Vertex> face = vertices.stream().filter(vertex -> {
				double length = Math.sqrt(vertex.nx() * vertex.nx() + vertex.ny() * vertex.ny() + vertex.nz() * vertex.nz());
				return side * (vertex.nx() * sin + vertex.nz() * cos) > 0.9999 * length;
			}).toList();
			String faceKey = key + (side == 1 ? "_front" : "_back");
			checks.put("image_face_vertices_" + faceKey, face.size());
			check(face.size() == 8, "both_halves_have_image_quad_" + faceKey);
			float maxU = face.stream().map(Vertex::u).max(Float::compare).orElseThrow();
			List<Vertex> seam = face.stream().filter(vertex -> Math.abs(vertex.u() - maxU) < 0.0000001F).toList();
			check(seam.size() == 4, "both_halves_have_texture_edge_" + faceKey);
			double faceDistance = seam.stream().mapToDouble(vertex -> Math.abs(vertex.x() * cos - vertex.z() * sin)).max().orElseThrow();
			checks.put("image_face_seam_distance_" + faceKey, faceDistance);
			distance = Math.max(distance, faceDistance);
		}
		return distance;
	}

	/** Frozen pre-fix transform, rendered with the same real models as a negative control. */
	private static void renderLegacy(JudahOathEntity oath, PoseStack pose, MultiBufferSource buffers) {
		pose.translate(0, 3, 0);
		pose.mulPose(Axis.YP.rotationDegrees(oath.getRotation()));
		for (int side = 0; side < 2; side++) {
			pose.pushPose();
			if (side == 1) pose.scale(-1, 1, 1);
			pose.mulPose(Axis.ZP.rotationDegrees(180));
			pose.scale(2.64F, 2.64F, 2.64F);
			IconModelRenderer.renderFlat(JudahOathRenderer.model(oath.getVariant()), pose, buffers, 1, FULL_BRIGHT);
			pose.popPose();
		}
	}

	private record Vertex(float x, float y, float z, float u, float v, float nx, float ny, float nz) {}

	private static final class VertexCapture implements VertexConsumer {
		private final List<Vertex> vertices = new ArrayList<>();
		private float x, y, z, u, v;

		@Override public VertexConsumer addVertex(float x, float y, float z) { this.x = x; this.y = y; this.z = z; return this; }
		@Override public VertexConsumer setColor(int red, int green, int blue, int alpha) { return this; }
		@Override public VertexConsumer setUv(float u, float v) { this.u = u; this.v = v; return this; }
		@Override public VertexConsumer setUv1(int u, int v) { return this; }
		@Override public VertexConsumer setUv2(int u, int v) { return this; }
		@Override public VertexConsumer setNormal(float x, float y, float z) { vertices.add(new Vertex(this.x, this.y, this.z, u, v, x, y, z)); return this; }
	}

	private static final class JudahScreen extends Screen {
		private final float angle;
		private JudahScreen(float angle) {
			super(Component.literal("Judah Oath: " + (int) angle + " degrees"));
			this.angle = angle;
		}

		@Override public boolean isPauseScreen() { return false; }

		@Override
		public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
			graphics.fill(0, 0, width, height, 0xFF17212D);
			graphics.drawCenteredString(font, title, width / 2, 12, 0xFFFFFFFF);
			int cellWidth = (width - 24) / 3;
			float scale = Math.min((cellWidth - 20) / 3F, (height - 80) / 3.2F);
			for (Variant variant : Variant.values()) {
				int center = 12 + cellWidth * variant.ordinal() + cellWidth / 2;
				graphics.fill(center - cellWidth / 2 + 3, 35, center + cellWidth / 2 - 3, height - 18, 0xFF2A3746);
				graphics.drawCenteredString(font, variant.name(), center, height - 36, 0xFFFFFFFF);
				graphics.flush();
				graphics.pose().pushPose();
				graphics.pose().translate(center, 48 + 3 * scale, 100);
				graphics.pose().scale(scale, -scale, scale);
				Lighting.setupForEntityInInventory();
				JudahOathEntity oath = oath(minecraft, variant, angle);
				minecraft.getEntityRenderDispatcher().getRenderer(oath).render(oath, 0, partialTick, graphics.pose(), graphics.bufferSource(), FULL_BRIGHT);
				graphics.flush();
				graphics.pose().popPose();
				Lighting.setupFor3DItems();
			}
		}
	}

	private static void capture(Minecraft mc, String name) throws java.io.IOException {
		check(mc.screen instanceof JudahScreen, "visible_scene_" + name);
		var output = mc.gameDirectory.toPath().resolve("verification");
		Files.createDirectories(output);
		var file = output.resolve(name);
		try (var pixels = Screenshot.takeScreenshot(mc.getMainRenderTarget())) {
			pixels.writeToFile(file);
		}
		check(Files.size(file) > 0, "captured_" + name);
	}

	private static void check(boolean success, String label) {
		if (!success) throw new AssertionError(label);
		checks.put(label, true);
	}

	private static void finish(Minecraft mc, Throwable error) {
		stage = 99;
		try {
			var output = mc.gameDirectory.toPath().resolve("verification");
			Files.createDirectories(output);
			Files.writeString(output.resolve("result.json"), new GsonBuilder().setPrettyPrinting().create().toJson(
					Map.of("status", error == null ? "passed" : "failed", "checks", checks, "error", error == null ? "" : error.toString())));
		} catch (Exception saveError) {
			saveError.printStackTrace();
		}
		if (error != null) error.printStackTrace();
		if (mc.getSingleplayerServer() != null) mc.getSingleplayerServer().halt(true);
		mc.disconnect();
		mc.stop();
	}
}
