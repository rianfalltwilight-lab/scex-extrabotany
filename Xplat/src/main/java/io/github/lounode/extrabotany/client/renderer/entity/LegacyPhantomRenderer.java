package io.github.lounode.extrabotany.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.lounode.extrabotany.common.entity.LegacyPhantomSword;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public final class LegacyPhantomRenderer extends EntityRenderer<LegacyPhantomSword> {
    public LegacyPhantomRenderer(EntityRendererProvider.Context context) { super(context); }
    @Override public void render(LegacyPhantomSword entity, float yaw, float partial, PoseStack pose, MultiBufferSource buffers, int light) {
        if (entity.delay() > 0) return;
        float alpha = entity.fake() ? Math.max(0, .6F - (entity.tickCount + partial) * .015F) : 1;
        if (alpha <= 0) return;
        var velocity = entity.getDeltaMovement();
        float heading = velocity.lengthSqr() > 1E-6 ? (float) Math.toDegrees(Math.atan2(velocity.x, velocity.z)) : entity.getYRot();
        float angle = velocity.lengthSqr() > 1E-6 ? (float) Math.toDegrees(Math.atan2(velocity.horizontalDistance(), velocity.y)) : 90 - entity.getXRot();
        pose.pushPose(); pose.mulPose(Axis.YP.rotationDegrees(heading)); pose.mulPose(Axis.XP.rotationDegrees(angle));
        pose.mulPose(Axis.ZP.rotationDegrees(45)); pose.scale(1.5F, 1.5F, 1.5F);
        var vertex = buffers.getBuffer(RenderType.entityTranslucent(getTextureLocation(entity)));
        float[][] corners = {{-.5F,-.5F,0,1},{.5F,-.5F,1,1},{.5F,.5F,1,0},{-.5F,.5F,0,0}};
        for (var corner : corners) vertex.addVertex(pose.last().pose(), corner[0], corner[1], 0)
                .setColor((int) (alpha * 255) << 24 | 0xFFFFFF).setUv(corner[2], corner[3])
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0, 0, 1);
        pose.popPose(); super.render(entity, yaw, partial, pose, buffers, light);
    }
    @Override public ResourceLocation getTextureLocation(LegacyPhantomSword entity) {
        return ResourceLocation.parse("extrabotany:textures/item/sworddomain_" + Math.floorMod(entity.variety(), 10) + ".png");
    }
}
