package io.github.lounode.extrabotany.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import io.github.lounode.extrabotany.common.block.block_entity.PowerFrameBlockEntity;

public class PowerFrameRenderer implements BlockEntityRenderer<PowerFrameBlockEntity> {
	private final ItemRenderer itemRenderer;

	public PowerFrameRenderer(BlockEntityRendererProvider.Context context) {
		this.itemRenderer = context.getItemRenderer();
	}

	@Override
	public void render(PowerFrameBlockEntity charger, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
		ItemStack stack = charger.getItem();
		if (stack.isEmpty()) {
			return;
		}
		poseStack.pushPose();

		poseStack.translate(0.5F, 0.6F, 0.5F);
		float rotation = (charger.tickCount + partialTick) * 2F;
		poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
		poseStack.scale(0.5F, 0.5F, 0.5F);

		itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, 15728640,
				packedOverlay, poseStack, buffer, charger.getLevel(), 0);
		poseStack.popPose();
	}
}
