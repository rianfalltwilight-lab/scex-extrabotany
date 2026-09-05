package io.github.lounode.extrabotany.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.lounode.extrabotany.common.entity.LegacySwordProjectile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;

public final class LegacySwordRenderer extends EntityRenderer<LegacySwordProjectile> {
    public LegacySwordRenderer(EntityRendererProvider.Context context) { super(context); }
    @SuppressWarnings("deprecation") // The archived item mesh is static and carries no model-data-dependent quads.
    @Override public void render(LegacySwordProjectile entity, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
        var item = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("extrabotany", entity.kind.item));
        var model = Minecraft.getInstance().getItemRenderer().getItemModelShaper().getItemModel(item);
        pose.pushPose(); pose.scale(1.2F, 1.2F, 1.2F);
        pose.mulPose(Axis.YP.rotationDegrees(entity.getYRot() - 90));
        pose.mulPose(Axis.ZP.rotationDegrees(entity.getXRot())); pose.mulPose(Axis.ZP.rotationDegrees(-45));
        pose.translate(-.5F, -.5F, -.5F);
        var random = RandomSource.create(42);
        var vertex = buffers.getBuffer(Sheets.translucentItemSheet());
        for (int i = 0; i <= Direction.values().length; i++) {
            random.setSeed(42);
            var side = i == Direction.values().length ? null : Direction.values()[i];
            for (var quad : model.getQuads(null, side, random))
                vertex.putBulkData(pose.last(), quad, 1, 1, 1, .8980392F, 0xF000F0, OverlayTexture.NO_OVERLAY);
        }
        pose.popPose(); super.render(entity, yaw, partialTick, pose, buffers, light);
    }
    @Override public ResourceLocation getTextureLocation(LegacySwordProjectile entity) { return InventoryMenu.BLOCK_ATLAS; }
}
