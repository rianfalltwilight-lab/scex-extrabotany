package io.github.lounode.extrabotany.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import io.github.lounode.extrabotany.common.entity.mount.UfoEntity;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 飞碟的渲染器。发光部件的亮度随时间变化。
 * The renderer for the Cosmic Car. The brightness of the glow part changes with time.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code RenderUfo} 和 {@code ModelUfo}。
 * <p>Source: ExtraMeteorP 1.16 {@code RenderUfo} and {@code ModelUfo}.
 */
public class UfoRenderer extends EntityRenderer<UfoEntity> {
	private static final ResourceLocation TEXTURE = prefix("textures/entity/ufo.png");
	private static final float SCALE = 1.35F;
	// 天空光固定为最大值。方块光在 0x30 和 0xF0 之间变化。
	// The sky light is always the maximum. The block light changes between 0x30 and 0xF0.
	private static final int GLOW_LIGHT_BASE = 0xF00090;
	private static final int GLOW_LIGHT_RANGE = 0x60;
	private static final float GLOW_SPEED = 0.15F;

	private final ModelPart body;
	private final ModelPart glow;

	public UfoRenderer(EntityRendererProvider.Context context) {
		super(context);
		ModelPart root = MountModels.ufo().bakeRoot();
		body = root.getChild("body");
		glow = root.getChild("glow");
	}

	@Override
	public void render(UfoEntity ufo, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
		pose.pushPose();
		pose.translate(0.0, 2.5, 0.0);
		pose.mulPose(Axis.YP.rotationDegrees(180F - yaw));
		pose.mulPose(Axis.ZP.rotationDegrees(ufo.getRoll()));
		pose.mulPose(Axis.XP.rotationDegrees(ufo.getPitch()));
		pose.scale(-SCALE, -SCALE, SCALE);

		VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
		body.render(pose, buffer, light, OverlayTexture.NO_OVERLAY);
		int glowLight = GLOW_LIGHT_BASE + (int) (GLOW_LIGHT_RANGE * Mth.sin((ufo.tickCount + partialTick) * GLOW_SPEED));
		glow.render(pose, buffer, glowLight, OverlayTexture.NO_OVERLAY);
		pose.popPose();
		super.render(ufo, yaw, partialTick, pose, buffers, light);
	}

	@Override
	public ResourceLocation getTextureLocation(UfoEntity ufo) {
		return TEXTURE;
	}
}
