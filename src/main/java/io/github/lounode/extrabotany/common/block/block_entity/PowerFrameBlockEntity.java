package io.github.lounode.extrabotany.common.block.block_entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.common.block.block_entity.mana.ManaPoolBlockEntity;

/**
 * 力量框架的方块实体。它使用自己上面的魔力池。它的作用和魔力充能基座相同，但是没有粒子效果。
 * The block entity of the Power Frame. It uses the mana pool above it. It does the same job as the Mana Charger, but it has no particle effect.
 */
public class PowerFrameBlockEntity extends ChargerBlockEntity {
	public PowerFrameBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public PowerFrameBlockEntity(BlockPos pos, BlockState state) {
		this(ExtraBotanyBlockEntities.POWER_FRAME, pos, state);
	}

	@Nullable
	@Override
	public ManaPoolBlockEntity getPool() {
		var tile = level.getBlockEntity(worldPosition.above());
		return tile instanceof ManaPoolBlockEntity ? (ManaPoolBlockEntity) tile : null;
	}

	@Override
	public void chargeParticles() {

	}
}
