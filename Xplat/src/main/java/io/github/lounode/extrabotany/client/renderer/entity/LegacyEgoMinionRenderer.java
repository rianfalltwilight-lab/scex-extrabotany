package io.github.lounode.extrabotany.client.renderer.entity;

import io.github.lounode.extrabotany.common.entity.LegacyEgoMinion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;

public final class LegacyEgoMinionRenderer extends HumanoidMobRenderer<LegacyEgoMinion, HumanoidModel<LegacyEgoMinion>> {
    public LegacyEgoMinionRenderer(EntityRendererProvider.Context context) { super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), .5F); }
    @Override public ResourceLocation getTextureLocation(LegacyEgoMinion entity) {
        return Minecraft.getInstance().getCameraEntity() instanceof AbstractClientPlayer player
                ? player.getSkin().texture() : DefaultPlayerSkin.get(entity.getUUID()).texture();
    }
}
