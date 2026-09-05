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

import io.github.lounode.extrabotany.common.entity.SkullMissileEntity;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class SkullMissileRenderer extends EntityRenderer<SkullMissileEntity> {
	private static final ResourceLocation DEFAULT_SKULL = prefix("textures/entity/skull_missile/skull_missile.png");
	private final SkullModel model;

	public SkullMissileRenderer(EntityRendererProvider.Context ctx) {
		super(ctx);
		this.model = new SkullModel(ctx.bakeLayer(ModelLayers.PLAYER_HEAD));
	}

	@Override
	public void render(SkullMissileEntity entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int light) {
		poseStack.pushPose();
		poseStack.scale(-1.0F, -1.0F, 1.0F);
		poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
		float f = entity.getYRot();
		float f1 = entity.getXRot();

		this.model.setupAnim(0.0F, f, f1);
		this.model.renderToBuffer(poseStack, buffer.getBuffer(this.model.renderType(this.getTextureLocation(entity))), light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);

		poseStack.popPose();
		super.render(entity, yaw, partialTicks, poseStack, buffer, light);
	}

	@Override
	public ResourceLocation getTextureLocation(SkullMissileEntity entity) {
		/*
		Minecraft mc = Minecraft.getInstance();
		
		if (!(mc.getCameraEntity() instanceof AbstractClientPlayer clientPlayer)) {
			return DefaultPlayerSkin.getDefaultSkin(entity.getUUID());
		}
		
		return clientPlayer.getSkinTextureLocation();
		
		*/
		return DEFAULT_SKULL;
	}
}
