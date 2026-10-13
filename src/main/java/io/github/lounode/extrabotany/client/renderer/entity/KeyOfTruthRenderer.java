package io.github.lounode.extrabotany.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import io.github.lounode.extrabotany.common.entity.KeyOfTruthEntity;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 真理之钥的渲染器。它画出钥匙的模型，并在钥匙射击时画出一道指向目标的光束。
 * The renderer for the Key of Truth. It draws the key model, and it draws a beam to the target while the key shoots.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code RenderKeyOfTruth} 和 {@code ModelKeyOfTruth}。光束的画法和守卫者的光束相同。
 * <p>Source: ExtraMeteorP 1.16 {@code RenderKeyOfTruth} and {@code ModelKeyOfTruth}. The beam is drawn in the same way as the beam of a guardian.
 */
public class KeyOfTruthRenderer extends EntityRenderer<KeyOfTruthEntity> {
	private static final ResourceLocation TEXTURE = prefix("textures/entity/keyoftruth.png");
	private static final ResourceLocation BEAM_TEXTURE = prefix("textures/entity/energybeam.png");
	private static final RenderType BEAM_RENDER_TYPE = RenderType.entityCutoutNoCull(BEAM_TEXTURE);

	private static final int BEAM_RED = 10;
	private static final int BEAM_GREEN = 229;
	private static final int BEAM_BLUE = 238;
	private static final float BEAM_OUTER_RADIUS = 0.282F;
	private static final float BEAM_INNER_RADIUS = 0.2F;
	private static final int FULL_BRIGHT = 0xF000F0;
	// 端面四个角的顺序和守卫者光束相同：135 度、45 度、315 度、225 度。
	// The sequence of the four cap corners is the same as for the guardian beam: 135, 45, 315 and 225 degrees.
	private static final float[] CAP_CORNER_OFFSETS = { 0F, -Mth.HALF_PI, Mth.PI, Mth.HALF_PI };
	private static final float[] CAP_U = { 0.5F, 1F, 1F, 0.5F };
	private static final float[] CAP_V = { 0.5F, 0.5F, 0F, 0F };

	private final ModelPart model;

	public KeyOfTruthRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.model = createLayer().bakeRoot().getChild("bone");
	}

	private static LayerDefinition createLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition bone = mesh.getRoot().addOrReplaceChild("bone", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-4.0F, -1.0F, 1.0F, 4.0F, 6.0F, 2.0F)
				.texOffs(28, 27).addBox(-5.0F, 3.0F, 1.0F, 6.0F, 1.0F, 3.0F)
				.texOffs(16, 13).addBox(-4.0F, 3.0F, 4.0F, 4.0F, 1.0F, 9.0F)
				.texOffs(28, 23).addBox(-5.0F, -2.0F, 1.0F, 6.0F, 1.0F, 3.0F)
				.texOffs(0, 0).addBox(-4.0F, -2.0F, 4.0F, 4.0F, 1.0F, 12.0F)
				.texOffs(33, 9).addBox(-3.5F, -0.5F, 3.0F, 3.0F, 3.0F, 3.0F)
				.texOffs(20, 0).addBox(-3.53F, -1.14F, 2.96F, 3.0F, 4.0F, 1.0F)
				.texOffs(0, 10).addBox(-4.0F, -1.0F, 8.0F, 4.0F, 1.0F, 1.0F)
				.texOffs(0, 24).addBox(-1.0F, -1.0F, 8.0F, 1.0F, 4.0F, 1.0F)
				.texOffs(0, 8).addBox(-4.0F, 2.0F, 8.0F, 4.0F, 1.0F, 1.0F)
				.texOffs(21, 13).addBox(-4.0F, -1.0F, 8.0F, 1.0F, 4.0F, 1.0F)
				.texOffs(0, 13).addBox(-3.5F, -2.5F, 1.0F, 3.0F, 1.0F, 10.0F)
				.texOffs(10, 24).addBox(-4.0F, 0.0F, -6.0F, 4.0F, 1.0F, 4.0F)
				.texOffs(0, 24).addBox(0.0F, 0.0F, -7.3F, 1.0F, 2.0F, 8.0F)
				.texOffs(18, 23).addBox(-5.0F, 0.0F, -7.3F, 1.0F, 2.0F, 8.0F),
				PartPose.offset(0.0F, 19.0F, -1.0F));
		bone.addOrReplaceChild("bone2", CubeListBuilder.create()
				.texOffs(32, 32).addBox(-4.04F, -2.6285F, -3.0258F, 4.0F, 6.0F, 4.0F)
				.texOffs(15, 33).addBox(-3.5F, -5.7332F, -2.0606F, 3.0F, 4.0F, 3.0F),
				PartPose.offsetAndRotation(0.0F, 1.0F, 1.0F, 0.4363F, 0.0F, 0.0F));
		bone.addOrReplaceChild("bone3", CubeListBuilder.create()
				.texOffs(20, 0).addBox(-6.0F, -2.4752F, -5.1498F, 4.0F, 1.0F, 8.0F),
				PartPose.offsetAndRotation(2.0F, 5.0F, -3.3F, -0.6109F, 0.0F, 0.0F));
		bone.addOrReplaceChild("bone4", CubeListBuilder.create()
				.texOffs(16, 16).addBox(0.1F, -9.1213F, -1.4645F, 1.0F, 3.0F, 3.0F),
				PartPose.offsetAndRotation(0.0F, 6.5F, -4.0F, -0.7854F, 0.0F, 0.0F));
		bone.addOrReplaceChild("bone5", CubeListBuilder.create()
				.texOffs(0, 13).addBox(-0.1F, -9.1213F, -1.4645F, 1.0F, 3.0F, 3.0F),
				PartPose.offsetAndRotation(-5.0F, 6.5F, -4.0F, -0.7854F, 0.0F, 0.0F));
		return LayerDefinition.create(mesh, 64, 64);
	}

	@Override
	public void render(KeyOfTruthEntity key, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
		pose.pushPose();
		pose.translate(0.0, 1.375, 0.0);
		pose.mulPose(Axis.YP.rotationDegrees(key.getRotation()));
		pose.mulPose(Axis.XP.rotationDegrees(key.getPitch()));
		pose.scale(1F, -1F, -1F);
		model.render(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)), light, OverlayTexture.NO_OVERLAY);
		pose.popPose();

		if (key.isShooting() && key.level().getEntity(key.getTargetId()) instanceof LivingEntity target) {
			renderBeam(key, target, partialTick, pose, buffers);
		}
		super.render(key, yaw, partialTick, pose, buffers, light);
	}

	private void renderBeam(KeyOfTruthEntity key, LivingEntity target, float partialTick, PoseStack pose, MultiBufferSource buffers) {
		float time = key.level().getGameTime() + partialTick;
		float scroll = time * 0.5F % 1F;
		float eyeHeight = key.getEyeHeight();

		Vec3 from = lerpedPosition(key, eyeHeight, partialTick);
		Vec3 to = lerpedPosition(target, target.getBbHeight() * 0.5, partialTick);
		Vec3 delta = to.subtract(from);
		float length = (float) (delta.length() + 1.0);
		Vec3 direction = delta.normalize();
		float polar = (float) Math.acos(direction.y);
		float azimuth = (float) Math.atan2(direction.z, direction.x);

		pose.pushPose();
		pose.translate(0.0, eyeHeight, 0.0);
		pose.mulPose(Axis.YP.rotationDegrees((Mth.HALF_PI - azimuth) * Mth.RAD_TO_DEG));
		pose.mulPose(Axis.XP.rotationDegrees(polar * Mth.RAD_TO_DEG));

		float spin = time * 0.05F * -1.5F;
		float vStart = -1F + scroll;
		float vEnd = length * 2.5F + vStart;
		VertexConsumer buffer = buffers.getBuffer(BEAM_RENDER_TYPE);
		PoseStack.Pose last = pose.last();

		// 两个互相垂直的平面组成光束的主体。
		// Two planes at a right angle to each other make the body of the beam.
		for (int plane = 0; plane < 2; plane++) {
			float angle = spin - plane * Mth.HALF_PI;
			float x1 = Mth.cos(angle + Mth.PI) * BEAM_INNER_RADIUS;
			float z1 = Mth.sin(angle + Mth.PI) * BEAM_INNER_RADIUS;
			float x2 = Mth.cos(angle) * BEAM_INNER_RADIUS;
			float z2 = Mth.sin(angle) * BEAM_INNER_RADIUS;
			beamVertex(buffer, last, x1, length, z1, 0.4999F, vEnd);
			beamVertex(buffer, last, x1, 0F, z1, 0.4999F, vStart);
			beamVertex(buffer, last, x2, 0F, z2, 0F, vStart);
			beamVertex(buffer, last, x2, length, z2, 0F, vEnd);
		}

		// 光束末端的方形端面。
		// The square cap at the end of the beam.
		float capAngle = spin + Mth.PI * 0.75F;
		for (int corner = 0; corner < 4; corner++) {
			float angle = capAngle + CAP_CORNER_OFFSETS[corner];
			beamVertex(buffer, last, Mth.cos(angle) * BEAM_OUTER_RADIUS, length, Mth.sin(angle) * BEAM_OUTER_RADIUS,
					CAP_U[corner], CAP_V[corner]);
		}
		pose.popPose();
	}

	private static Vec3 lerpedPosition(Entity entity, double yOffset, float partialTick) {
		return new Vec3(
				Mth.lerp(partialTick, entity.xOld, entity.getX()),
				Mth.lerp(partialTick, entity.yOld, entity.getY()) + yOffset,
				Mth.lerp(partialTick, entity.zOld, entity.getZ()));
	}

	private static void beamVertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float z, float u, float v) {
		buffer.addVertex(pose, x, y, z)
				.setColor(BEAM_RED, BEAM_GREEN, BEAM_BLUE, 255)
				.setUv(u, v)
				.setOverlay(OverlayTexture.NO_OVERLAY)
				.setLight(FULL_BRIGHT)
				.setNormal(pose, 0F, 1F, 0F);
	}

	@Override
	public ResourceLocation getTextureLocation(KeyOfTruthEntity key) {
		return TEXTURE;
	}
}
