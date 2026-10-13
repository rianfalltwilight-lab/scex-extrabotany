package io.github.lounode.extrabotany.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

import io.github.lounode.extrabotany.common.block.block_entity.ManaBufferBlockEntity;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 量子魔力缓存器的渲染器。这个方块没有普通的方块模型，它的外形完全由这里绘制。
 * The renderer of the Quantum Mana Buffer. This block has no usual block model, and this class draws its full shape.
 *
 * <p>来源：ExtraMeteorP 1.12 {@code RenderQuantumManaBuffer}。
 * <p>Source: ExtraMeteorP 1.12 {@code RenderQuantumManaBuffer}.
 */
public class QuantumManaBufferRenderer implements BlockEntityRenderer<ManaBufferBlockEntity> {
	private static final ResourceLocation TEXTURE = prefix("textures/model/quantummanabuffer.png");

	private final ModelPart model = QuantumManaBufferModel.create();

	public QuantumManaBufferRenderer(BlockEntityRendererProvider.Context context) {}

	@Override
	public void render(ManaBufferBlockEntity buffer, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int packedLight, int packedOverlay) {
		poseStack.pushPose();
		// 旧式模型的原点在方块上方 1.5 格，并且上下颠倒。
		// The origin of the old model format is 1.5 blocks above the block, and the model is upside down.
		poseStack.translate(0.5F, 1.5F, 0.5F);
		poseStack.scale(1F, -1F, -1F);
		model.render(poseStack, buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)), packedLight, packedOverlay);
		poseStack.popPose();
	}
}
