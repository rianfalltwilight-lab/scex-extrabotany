package io.github.lounode.extrabotany.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

import io.github.lounode.extrabotany.common.entity.VoidFieldEntity;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 虚空力场的渲染器。它绘制一个半透明的橙色立方体，立方体的一个角朝下。
 * The renderer of the void field. It draws a translucent orange cube that stands on one corner.
 *
 * <p>来源：ExtraMeteorP 1.12 {@code RenderVoid} 和 {@code ModelVoid}。
 * <p>Source: ExtraMeteorP 1.12 {@code RenderVoid} and {@code ModelVoid}.
 */
public class VoidFieldRenderer extends EntityRenderer<VoidFieldEntity> {
	private static final ResourceLocation TEXTURE = prefix("textures/model/void.png");
	/** 原版的颜色：红 1、绿 0.55、蓝 0.02、不透明度 0.3。 / The color of the original: red 1, green 0.55, blue 0.02, alpha 0.3. */
	private static final int COLOR = 0x4CFF8C05;
	/** 原版用 2.8/16 的单位绘制这个模型。 / The original draws this model with a unit of 2.8/16. */
	private static final float SCALE = 2.8F;

	private final ModelPart cube;

	public VoidFieldRenderer(EntityRendererProvider.Context context) {
		super(context);
		MeshDefinition mesh = new MeshDefinition();
		mesh.getRoot().addOrReplaceChild("Shape1", CubeListBuilder.create().texOffs(0, 0).addBox(0F, 0F, 0F, 16, 16, 16),
				PartPose.offsetAndRotation(-12F, 3F, -6F, 0.7853982F, 0.7853982F, 0.7853982F));
		cube = LayerDefinition.create(mesh, 64, 64).bakeRoot();
	}

	@Override
	public ResourceLocation getTextureLocation(VoidFieldEntity entity) {
		return TEXTURE;
	}

	@Override
	public void render(VoidFieldEntity entity, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int packedLight) {
		poseStack.pushPose();
		poseStack.translate(0.5F, 2F, -0.5F);
		poseStack.scale(SCALE, -SCALE, -SCALE);
		cube.render(poseStack, buffers.getBuffer(RenderType.entityTranslucent(TEXTURE)), packedLight, OverlayTexture.NO_OVERLAY, COLOR);
		poseStack.popPose();
		super.render(entity, yaw, partialTick, poseStack, buffers, packedLight);
	}
}
