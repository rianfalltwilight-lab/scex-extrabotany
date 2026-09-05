package io.github.lounode.extrabotany.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import io.github.lounode.extrabotany.common.entity.SkullLandMineEntity;

public class SkullLandMineRenderer extends EntityRenderer<SkullLandMineEntity> {

	private final SkullModel model;

	protected SkullLandMineRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.model = new SkullModel(context.bakeLayer(ModelLayers.PLAYER_HEAD));
	}

	@Override
	public void render(SkullLandMineEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
//        poseStack.pushPose();
//        poseStack.scale(-1.0F, -1.0F, 1.0F);
//        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
//        float f = entity.getYRot();
//        float f1 = entity.getXRot();
//
//        this.model.setupAnim(0.0F, f, f1);
//        this.model.renderToBuffer(poseStack, buffer.getBuffer(this.model.renderType(this.getTextureLocation(entity))), packedLight,  OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
//
//        poseStack.popPose();
//        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
//
		poseStack.pushPose();

		int fuse = entity.getExplodeCountDown();
		if ((float) fuse - partialTick + 1.0F < 10.0F) {
			float scaleFactor = 1.0F - ((float) fuse - partialTick + 1.0F) / 10.0F;
			scaleFactor = Mth.clamp(scaleFactor, 0.0F, 1.0F);
			scaleFactor *= scaleFactor;
			scaleFactor *= scaleFactor;
			float scale = 1.0F + scaleFactor * 0.3F;
			poseStack.scale(scale, scale, scale);
		}

		poseStack.scale(-1.0F, -1.0F, 1.0F);
		poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

		int overlay = fuse / 5 % 2 == 0 ? OverlayTexture.NO_OVERLAY : OverlayTexture.pack(10, 10);

		float f = entity.getYRot();
		float f1 = entity.getXRot();
		this.model.setupAnim(0.0F, f, f1);
		this.model.renderToBuffer(poseStack,
				buffer.getBuffer(this.model.renderType(this.getTextureLocation(entity))),
				packedLight,
				overlay,
				0xFFFFFFFF);
		poseStack.popPose();
		super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
	}

	@Override
	public ResourceLocation getTextureLocation(SkullLandMineEntity entity) {
		return entity.getTexture();
	}
}
