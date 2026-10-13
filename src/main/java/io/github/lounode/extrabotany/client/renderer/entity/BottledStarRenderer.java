package io.github.lounode.extrabotany.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;

import io.github.lounode.extrabotany.common.entity.projectile.BottledStarEntity;

/**
 * 星标的渲染器。它把瓶中星物品画在空中，并让它缓慢旋转。
 * The renderer for the star marker. It draws the Bottled Star item in the air and turns it slowly.
 */
public class BottledStarRenderer extends EntityRenderer<BottledStarEntity> {
	private static final float DEGREES_PER_TICK = 6F;

	public BottledStarRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public void render(BottledStarEntity entity, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
		pose.pushPose();
		pose.translate(0, 0.25, 0);
		pose.mulPose(Axis.YP.rotationDegrees((entity.tickCount + partialTick) * DEGREES_PER_TICK));
		Minecraft.getInstance().getItemRenderer().renderStatic(entity.getItem(), ItemDisplayContext.GROUND,
				light, OverlayTexture.NO_OVERLAY, pose, buffers, entity.level(), entity.getId());
		pose.popPose();

		super.render(entity, yaw, partialTick, pose, buffers, light);
	}

	@Override
	public ResourceLocation getTextureLocation(BottledStarEntity entity) {
		return InventoryMenu.BLOCK_ATLAS;
	}
}
