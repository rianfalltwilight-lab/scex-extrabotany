package io.github.lounode.extrabotany.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import io.github.lounode.extrabotany.common.block.block_entity.PedestalBlockEntity;

public class PedestalRenderer implements BlockEntityRenderer<PedestalBlockEntity> {
	private static final float SIZE = 1.0F;
	private final ItemRenderer itemRenderer;

	public PedestalRenderer(BlockEntityRendererProvider.Context context) {
		this.itemRenderer = context.getItemRenderer();
	}

	@Override
	public void render(PedestalBlockEntity pedestal, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
		ItemStack itemStack = pedestal.getItem();
		if (itemStack.isEmpty()) {
			return;
		}
		poseStack.pushPose();

		poseStack.translate(0.5F, 1.35F, 0.5F);
		float rotation = (pedestal.tickCount + partialTick) * 2F;
		poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
		float floatOffset = Mth.sin((pedestal.tickCount + partialTick) * 0.1F) * 0.05F;
		poseStack.translate(0F, floatOffset, 0F);
		poseStack.scale(0.5F, 0.5F, 0.5F);

		itemRenderer.renderStatic(itemStack, ItemDisplayContext.FIXED, packedLight,
				packedOverlay, poseStack, buffer, pedestal.getLevel(), 0);
		poseStack.popPose();

	}
}
