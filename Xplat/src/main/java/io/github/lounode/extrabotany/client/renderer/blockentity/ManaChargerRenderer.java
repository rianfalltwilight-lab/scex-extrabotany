package io.github.lounode.extrabotany.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import io.github.lounode.extrabotany.common.block.block_entity.ManaChargerBlockEntity;

public class ManaChargerRenderer implements BlockEntityRenderer<ManaChargerBlockEntity> {

	private static final float SIZE = 1.0F;
	private final ItemRenderer itemRenderer;

	public ManaChargerRenderer(BlockEntityRendererProvider.Context context) {
		this.itemRenderer = context.getItemRenderer();
	}

	@Override
	public void render(ManaChargerBlockEntity charger, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
		ItemStack stack = charger.getItem();
		if (stack.isEmpty()) {
			return;
		}
		poseStack.pushPose();

		poseStack.translate(0.5F, 0.2F, 0.5F);
		poseStack.mulPose(Axis.XP.rotationDegrees(90));
		poseStack.scale(0.5F * SIZE, 0.5F * SIZE, 0.5F * SIZE);

		itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, packedLight,
				packedOverlay, poseStack, buffer, charger.getLevel(), 0);
		poseStack.popPose();
	}
}
