package io.github.lounode.extrabotany.common.block.block_entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.common.block.block_entity.mana.ManaPoolBlockEntity;

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
