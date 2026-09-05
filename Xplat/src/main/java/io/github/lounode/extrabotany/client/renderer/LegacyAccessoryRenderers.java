package io.github.lounode.extrabotany.client.renderer;

import io.github.lounode.extrabotany.common.item.legacy.LegacyCosmetics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import vazkii.botania.client.render.AccessoryRenderRegistry;

public final class LegacyAccessoryRenderers {
    private LegacyAccessoryRenderers() {}
    public static void register() {
        for (var item : LegacyCosmetics.ITEMS.values()) {
            AccessoryRenderRegistry.register(item, (model, stack, living, pose, buffers, light,
                    limbSwing, limbAmount, partialTicks, age, yaw, pitch) -> {
                var transform = item.transform;
                pose.pushPose();
                (transform.head() ? model.head : model.body).translateAndRotate(pose);
                pose.translate(transform.x(), transform.y(), transform.z());
                pose.scale(transform.sx(), transform.sy(), transform.sz());
                Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.NONE,
                        light, OverlayTexture.NO_OVERLAY, pose, buffers, living.level(), 0);
                pose.popPose();
            });
        }
    }
}
