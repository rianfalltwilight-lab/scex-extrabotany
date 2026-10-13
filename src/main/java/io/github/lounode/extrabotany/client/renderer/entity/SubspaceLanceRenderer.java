package io.github.lounode.extrabotany.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

import io.github.lounode.extrabotany.common.entity.SubspaceLanceEntity;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 亚空之矛枪的渲染器。它绘制一把竖直立着的亚空之矛，总是用最大亮度。
 * The renderer of the Subspace Lance. It draws a Spear of Subspace that stands vertically, always with the maximum brightness.
 *
 * <p>来源：ExtraMeteorP 1.12 {@code RenderSubspaceLance}。原版的矛枪朝向固定（俯仰 -90 度），所以这里不同步朝向。
 * <p>Source: ExtraMeteorP 1.12 {@code RenderSubspaceLance}. The lance of the original has a fixed direction (a pitch of -90 degrees), thus this port does not synchronize the direction.
 */
public class SubspaceLanceRenderer extends EntityRenderer<SubspaceLanceEntity> {
	private static final ResourceLocation TEXTURE = prefix("textures/entity/spearsubspace.png");
	/** 原版用 0.07 的单位绘制这个模型，而不是通常的 1/16。 / The original draws this model with a unit of 0.07 and not the usual 1/16. */
	private static final float SCALE = 0.07F * 16F;

	private final ModelPart spear = SubspaceSpearRenderer.bakeModel();

	public SubspaceLanceRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public ResourceLocation getTextureLocation(SubspaceLanceEntity entity) {
		return TEXTURE;
	}

	@Override
	public boolean shouldRender(SubspaceLanceEntity entity, Frustum frustum, double x, double y, double z) {
		// 矛比实体的碰撞箱长得多。
		// The spear is much longer than the bounding box of the entity.
		return entity.shouldRender(x, y, z) && frustum.isVisible(entity.getBoundingBox().inflate(4));
	}

	@Override
	public void render(SubspaceLanceEntity entity, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int packedLight) {
		poseStack.pushPose();
		poseStack.translate(0.5F, 1.5F, 0.5F);
		poseStack.scale(SCALE, -SCALE, -SCALE);
		spear.render(poseStack, buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
		poseStack.popPose();
		super.render(entity, yaw, partialTick, poseStack, buffers, packedLight);
	}
}
