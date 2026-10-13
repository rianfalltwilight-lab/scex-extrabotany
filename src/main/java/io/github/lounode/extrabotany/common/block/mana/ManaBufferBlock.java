package io.github.lounode.extrabotany.common.block.mana;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import vazkii.botania.common.block.block_entity.mana.ManaPoolBlockEntity;

import io.github.lounode.extrabotany.common.block.block_entity.ManaBufferBlockEntity;

import java.util.function.Supplier;

/**
 * 魔力缓存器和量子魔力缓存器的方块。
 * The block of the Mana Buffer and of the Quantum Mana Buffer.
 *
 * <p>魔力缓存器有比较器输出，算法和魔力池相同。量子魔力缓存器没有比较器输出，它的模型由方块实体渲染器绘制。这些都和原版相同。
 * <p>The Mana Buffer has a comparator output, with the same formula as the mana pool. The Quantum Mana Buffer has no comparator output, and the block entity renderer draws its model. All of this is the same as in the original.
 *
 * <p>来源：ExtraMeteorP 1.12 {@code BlockManaBuffer} 和 {@code BlockQuantumManaBuffer}。
 * <p>Source: ExtraMeteorP 1.12 {@code BlockManaBuffer} and {@code BlockQuantumManaBuffer}.
 */
public class ManaBufferBlock extends ManaMachineBlock<ManaBufferBlockEntity> {
	private final ManaBufferBlockEntity.Tier tier;

	public ManaBufferBlock(ManaBufferBlockEntity.Tier tier, Properties properties, Supplier<BlockEntityType<ManaBufferBlockEntity>> type) {
		super(properties, type);
		this.tier = tier;
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return tier == ManaBufferBlockEntity.Tier.QUANTUM ? RenderShape.ENTITYBLOCK_ANIMATED : RenderShape.MODEL;
	}

	@Override
	protected boolean hasAnalogOutputSignal(BlockState state) {
		return tier == ManaBufferBlockEntity.Tier.BUFFER;
	}

	@Override
	protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
		return level.getBlockEntity(pos) instanceof ManaBufferBlockEntity buffer
				? ManaPoolBlockEntity.calculateComparatorLevel(buffer.getCurrentMana(), buffer.getMaxMana())
				: 0;
	}
}
