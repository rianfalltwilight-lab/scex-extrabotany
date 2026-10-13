package io.github.lounode.extrabotany.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.RenderTypeHelper;
import net.neoforged.neoforge.client.model.data.ModelData;

import io.github.lounode.extrabotany.common.block.block_entity.CocoonOfDesireBlockEntity;

/**
 * 不随想之茧的渲染器。它绘制茧的方块模型。茧在孵化时会摇动，越接近孵化摇得越频繁。
 * The renderer of the Cocoon of Desire. It draws the block model of the cocoon. The cocoon wobbles during the incubation, and it wobbles more frequently when the hatch time is near.
 *
 * <p>来源：ExtraMeteorP 1.12 {@code RenderCocoonDesire}。
 * <p>Source: ExtraMeteorP 1.12 {@code RenderCocoonDesire}.
 *
 * <p>和原版的差异：摇动使用孵化进度。原版使用"方块实体存在的时间"，所以茧只在区块加载后的头两分钟摇动，和孵化无关。
 * <p>Difference from the original: the wobble uses the hatch progress. The original uses "the time that the block entity has existed", thus the cocoon only wobbles in the first two minutes after the chunk loads, with no relation to the incubation.
 */
public class CocoonOfDesireRenderer implements BlockEntityRenderer<CocoonOfDesireBlockEntity> {
	/** 一次摇动持续的 tick 数。 / The number of ticks that one wobble continues. */
	private static final float WOBBLE_TICKS = 10F;

	private final BlockRenderDispatcher blockRenderer;
	private final RandomSource random = RandomSource.create(42);

	public CocoonOfDesireRenderer(BlockEntityRendererProvider.Context context) {
		this.blockRenderer = context.getBlockRenderDispatcher();
	}

	@Override
	public void render(CocoonOfDesireBlockEntity cocoon, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int packedLight, int packedOverlay) {
		BlockState state = cocoon.getBlockState();
		float angle = cocoon.getItem().isEmpty() ? 0F : getWobbleAngle(cocoon.getTimePassed() + partialTick);

		poseStack.pushPose();
		if (angle != 0F) {
			// 绕方块底面的中线转动。
			// Turn around the center line of the bottom face of the block.
			poseStack.translate(0.5F, 0F, 0.5F);
			poseStack.mulPose(Axis.XP.rotationDegrees(angle));
			poseStack.translate(-0.5F, 0F, -0.5F);
		}
		BakedModel model = blockRenderer.getBlockModel(state);
		for (RenderType chunkType : model.getRenderTypes(state, random, ModelData.EMPTY)) {
			blockRenderer.getModelRenderer().renderModel(poseStack.last(), buffers.getBuffer(RenderTypeHelper.getEntityRenderType(chunkType, false)),
					state, model, 1F, 1F, 1F, packedLight, packedOverlay, ModelData.EMPTY, chunkType);
		}
		poseStack.popPose();
	}

	/**
	 * 计算摇动的角度（度）。
	 * Calculates the angle of the wobble (in degrees).
	 *
	 * <p>两次摇动的间隔从 60 tick 缩短到 30 tick。摇动的幅度随时间的对数增大。这些公式和原版相同。
	 * <p>The interval between two wobbles decreases from 60 ticks to 30 ticks. The amplitude of the wobble increases with the logarithm of the time. These formulas are the same as in the original.
	 */
	private static float getWobbleAngle(float time) {
		float interval = 60F - time / CocoonOfDesireBlockEntity.HATCH_TIME * 30F;
		float phase = time % interval;
		if (phase >= WOBBLE_TICKS) {
			return 0F;
		}
		return (float) (Math.sin(phase / 5F * Math.PI * 2) * Math.log(time + 1F));
	}
}
