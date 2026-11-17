package io.github.lounode.extrabotany.forge.gametest;

import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.platform.NativeImage;
import io.github.lounode.extrabotany.api.ExtraBotaniaRegistries;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.packs.resources.ResourceMetadata;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/** Opt-in regression for the real atlas, creative stacks and GUI item rendering. */
@EventBusSubscriber(modid = "extrabotany", value = Dist.CLIENT)
public final class ShadowKatanaClientProbe {

	private static final Map<String, Object> checks = new LinkedHashMap<>();
	private static final List<ItemStack> stacks = new ArrayList<>();
	private static int stage, ticks, total;

	@SubscribeEvent
	public static void tick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event) {
		if (!Boolean.getBoolean("extrabotany.shadowKatanaClientProbe") || stage == 99) return;
		Minecraft mc = Minecraft.getInstance();
		try {
			if (++total > 4800) throw new AssertionError("Shadow Katana probe timeout at stage " + stage);
			mc.options.pauseOnLostFocus = false;
			mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
			if (stage == 0 && mc.screen instanceof net.minecraft.client.gui.screens.AccessibilityOnboardingScreen) {
				mc.options.onboardingAccessibilityFinished();
				mc.setScreen(new TitleScreen());
			}
			if (stage == 0 && mc.screen instanceof TitleScreen && mc.getOverlay() == null) {
				stage = 1;
				mc.createWorldOpenFlows().createFreshLevel("extrabotany-shadow-katana-probe",
						new LevelSettings("Shadow Katana texture regression", GameType.CREATIVE, false, Difficulty.NORMAL, true, new GameRules(), WorldDataConfiguration.DEFAULT),
						new WorldOptions(20261025L, false, false),
						r -> r.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(), new TitleScreen());
				return;
			}
			if (mc.level == null || mc.player == null) return;
			if (stage == 1) {
				if (mc.screen != null || mc.getOverlay() != null || ++ticks < 30) return;
				checks.put("renderer", org.lwjgl.opengl.GL11.glGetString(org.lwjgl.opengl.GL11.GL_RENDERER));
				CreativeModeTabs.tryRebuildTabContents(mc.player.connection.enabledFeatures(), true, mc.level.registryAccess());
				var tab = BuiltInRegistries.CREATIVE_MODE_TAB.get(ExtraBotaniaRegistries.EXTRA_BOTANIA_TAB_KEY);
				for (Item item : List.of(ExtraBotanyItems.shadowKatana, ExtraBotanyItems.trueShadowKatana)) {
					ItemStack stack = tab.getDisplayItems().stream().filter(candidate -> candidate.is(item)).findFirst().orElseThrow();
					stacks.add(stack);
				}
				mc.setScreen(new KatanaScreen());
				stage = 2;
				ticks = 0;
				return;
			}
			if (stage == 2 && ++ticks >= 30) {
				capture(mc);
				verifyItems(mc);
				finish(mc, null);
			}
		} catch (Throwable error) {
			finish(mc, error);
		}
	}

	@SuppressWarnings("deprecation")
	private static void verifyItems(Minecraft mc) throws java.io.IOException {
		check(readable(Component.literal("\u00A76Relic\u00A77, Unbound")), "legacy_colors_render_as_readable_text");
		check(!readable(Component.literal("\u00A7kShadow")), "legacy_obfuscation_is_rejected");
		check(!readable(Component.literal("Shadow").withStyle(ChatFormatting.OBFUSCATED)), "component_obfuscation_is_rejected");
		check(!readable(Component.literal("\uFFFD")), "replacement_glyph_is_rejected");
		for (ItemStack stack : stacks) {
			String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
			check(!stack.isEmpty(), "creative_stack_" + id);
			Component name = stack.getHoverName();
			checks.put("display_name_" + id, name.getString());
			check(!name.getString().equals(stack.getDescriptionId()), "translated_name_" + id);
			check(readable(name), "readable_name_" + id);
			List<TextEvidence> tooltip = Screen.getTooltipFromItem(mc, stack).stream().map(ShadowKatanaClientProbe::textEvidence).toList();
			checks.put("tooltip_lines_" + id, tooltip);
			check(tooltip.stream().allMatch(ShadowKatanaClientProbe::readable), "readable_tooltip_" + id);
			if (stack.is(ExtraBotanyItems.trueShadowKatana)) {
				check(tooltip.stream().anyMatch(line -> line.translationKey().equals("botaniamisc.relicUnbound")), "unbound_relic_tooltip_present");
			}
			var sprite = mc.getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS).getSprite(prefix("item/" + id));
			var contents = sprite.contents();
			checks.put("sprite_size_" + id, contents.width() + "x" + contents.height());
			check(contents.width() == 16 && contents.height() == 16, "square_item_frame_" + id);
			var model = mc.getItemRenderer().getModel(stack, mc.level, mc.player, 0);
			check(model != mc.getModelManager().getMissingModel(), "registered_model_" + id);
			int quads = 0;
			for (int side = 0; side <= Direction.values().length; side++) {
				for (var quad : model.getQuads(null, side == Direction.values().length ? null : Direction.values()[side], RandomSource.create(42))) {
					check(quad.getSprite() == sprite, "model_uses_item_sprite_" + id);
					quads++;
				}
			}
			check(quads > 0, "model_has_quads_" + id);
			checks.put("model_quad_count_" + id, quads);
			if (stack.is(ExtraBotanyItems.shadowKatana)) {
				var metadata = contents.metadata().getSection(AnimationMetadataSection.SERIALIZER).orElseThrow();
				check(metadata.getDefaultFrameTime() == 4, "classic_animation_frame_time");
				check(java.util.Arrays.equals(contents.getUniqueFrames().toArray(), new int[] {0, 1, 2, 3, 4, 5, 6, 7}), "eight_animation_frames");
				// Load the same resource without its metadata to reproduce the pre-fix sprite.
				try (var input = mc.getResourceManager().getResourceOrThrow(prefix("textures/item/shadow_katana.png")).open()) {
					NativeImage image = NativeImage.read(input);
					var oldSize = AnimationMetadataSection.EMPTY.calculateFrameSize(image.getWidth(), image.getHeight());
					try (var oldSprite = new SpriteContents(prefix("item/shadow_katana"), oldSize, image, ResourceMetadata.EMPTY)) {
						checks.put("missing_metadata_sprite_size", oldSprite.width() + "x" + oldSprite.height());
						check(oldSprite.width() == 16 && oldSprite.height() == 128 && oldSprite.createTicker() == null,
								"missing_metadata_reproduces_static_strip");
					}
				}
			} else {
				check(contents.metadata().getSection(AnimationMetadataSection.SERIALIZER).isEmpty(), "true_shadow_katana_stays_static");
			}
		}
	}

	private static boolean readable(Component text) {
		return readable(textEvidence(text));
	}

	private static boolean readable(TextEvidence text) {
		return !text.renderedText().contains("\uFFFD") && !text.renderedText().contains("\u00A7") && !text.obfuscated();
	}

	private record TextEvidence(String translationKey, String rawText, String renderedText, boolean obfuscated, List<String> glyphStyles) {}

	private static TextEvidence textEvidence(Component text) {
		StringBuilder rendered = new StringBuilder();
		boolean[] obfuscated = {false};
		List<String> styles = new ArrayList<>();
		// Botania's relic binding line uses legacy §6/§7 colors in its translation.
		// Inspect the actual glyph stream after Minecraft has consumed formatting codes.
		text.getVisualOrderText().accept((index, style, codePoint) -> {
			rendered.appendCodePoint(codePoint);
			obfuscated[0] |= style.isObfuscated();
			String glyphStyle = style.toString();
			if (styles.isEmpty() || !styles.getLast().equals(glyphStyle)) styles.add(glyphStyle);
			return true;
		});
		String key = text.getContents() instanceof TranslatableContents translated ? translated.getKey() : "";
		return new TextEvidence(key, text.getString(), rendered.toString(), obfuscated[0], styles);
	}

	private static final class KatanaScreen extends Screen {
		private KatanaScreen() { super(Component.literal("Shadow Katana: creative item models and names")); }
		@Override public boolean isPauseScreen() { return false; }
		@Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
			graphics.fill(0, 0, width, height, 0xFF17212D);
			graphics.drawCenteredString(font, title, width / 2, 12, 0xFFFFFFFF);
			for (int i = 0; i < stacks.size(); i++) {
				ItemStack stack = stacks.get(i);
				int center = width * (i * 2 + 1) / 4;
				graphics.drawCenteredString(font, stack.getHoverName(), center, 38, 0xFFFFFFFF);
				graphics.pose().pushPose();
				graphics.pose().translate(center - 32, 58, 0);
				graphics.pose().scale(4, 4, 1);
				graphics.renderItem(stack, 0, 0);
				graphics.pose().popPose();
				graphics.renderItem(stack, center - 8, 132);
			}
			graphics.renderTooltip(font, stacks.getFirst(), 18, 170);
		}
	}

	private static void capture(Minecraft mc) throws java.io.IOException {
		check(mc.screen instanceof KatanaScreen, "visible_item_preview");
		var output = mc.gameDirectory.toPath().resolve("verification");
		Files.createDirectories(output);
		var file = output.resolve("shadow-katana.png");
		try (var pixels = Screenshot.takeScreenshot(mc.getMainRenderTarget())) { pixels.writeToFile(file); }
		check(Files.size(file) > 0, "captured_shadow_katana");
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
		} catch (Exception saveError) { saveError.printStackTrace(); }
		if (error != null) error.printStackTrace();
		if (mc.getSingleplayerServer() != null) mc.getSingleplayerServer().halt(true);
		mc.disconnect();
		mc.stop();
	}
}
