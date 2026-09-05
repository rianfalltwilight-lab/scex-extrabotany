package io.github.lounode.extrabotany.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.lounode.extrabotany.common.entity.LegacyLance;
import io.github.lounode.extrabotany.common.entity.LegacySwordDomain;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

/** Original scex.1 texture planes and animation parameters, shared without duplicating vertex code. */
public final class LegacyBossSupportRenderer<T extends Entity> extends EntityRenderer<T> {
    public LegacyBossSupportRenderer(EntityRendererProvider.Context context) { super(context); }

    @Override public ResourceLocation getTextureLocation(T entity) {
        String texture = entity instanceof LegacyLance ? "entity/spearsubspace"
                : entity instanceof LegacySwordDomain domain ? "item/sworddomain_" + domain.variety() : "entity/wing";
        return ResourceLocation.fromNamespaceAndPath("extrabotany", "textures/" + texture + ".png");
    }

    @Override public void render(T entity, float yaw, float partial, PoseStack pose, MultiBufferSource buffers, int light) {
        float age = entity.tickCount + partial;
        var vertices = buffers.getBuffer(RenderType.entityTranslucent(getTextureLocation(entity)));
        pose.pushPose();
        if (entity instanceof LegacyLance) {
            pose.translate(0, 1.6, 0);
            for (int plane = 0; plane < 4; plane++) {
                pose.pushPose();
                pose.mulPose(Axis.YP.rotationDegrees(age * 4 + plane * 45));
                quad(pose, vertices, .35F, 2.2F, 0xDDFFFFFF, true);
                pose.popPose();
            }
        } else if (entity instanceof LegacySwordDomain) {
            var camera = entityRenderDispatcher.camera.getPosition();
            float facing = (float) Math.toDegrees(Math.atan2(camera.z - entity.getZ(), camera.x - entity.getX())) - 90;
            pose.translate(0, .9, 0);
            pose.mulPose(Axis.YP.rotationDegrees(facing));
            pose.mulPose(Axis.ZP.rotationDegrees(age * 6));
            float scale = 1.2F + Math.min(age / 30, 1) * .6F;
            pose.scale(scale, scale, scale);
            quad(pose, vertices, .5F, .5F, 0xEEFFFFFF, false);
        } else {
            pose.translate(0, 1, 0);
            float pulse = 1.2F + (float) Math.sin(age * .25F) * .15F;
            pose.scale(pulse, pulse, pulse);
            int color = ((int) (80 * Math.max(0, 1 - age / 60)) << 24) | 0x8A5CFF;
            for (int plane = 0; plane < 3; plane++) {
                pose.pushPose();
                pose.mulPose(Axis.YP.rotationDegrees(age * 3 + plane * 60));
                pose.mulPose(Axis.XP.rotationDegrees(90 - plane * 35));
                quad(pose, vertices, 1.5F, 1.5F, color, false);
                pose.popPose();
            }
        }
        pose.popPose();
        super.render(entity, yaw, partial, pose, buffers, light);
    }

    private static void quad(PoseStack pose, VertexConsumer vertices, float x, float y, int color, boolean up) {
        for (int corner = 0; corner < 4; corner++) {
            boolean right = corner == 1 || corner == 2, top = corner >= 2;
            vertices.addVertex(pose.last().pose(), right ? x : -x, top ? y : -y, 0)
                    .setColor(color).setUv(right ? 1 : 0, top ? 0 : 1)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0, up ? 1 : 0, up ? 0 : 1);
        }
    }
}
