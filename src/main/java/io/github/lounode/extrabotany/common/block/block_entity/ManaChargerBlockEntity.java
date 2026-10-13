package io.github.lounode.extrabotany.common.block.block_entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.common.block.block_entity.mana.BellowsBlockEntity;
import vazkii.botania.common.block.block_entity.mana.ManaPoolBlockEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * 魔力充能基座的方块实体。它使用自己下面的魔力池。
 * The block entity of the Mana Charger. It uses the mana pool below it.
 */
public class ManaChargerBlockEntity extends ChargerBlockEntity {
	public ManaChargerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public ManaChargerBlockEntity(BlockPos pos, BlockState state) {
		this(ExtraBotanyBlockEntities.MANA_CHARGER, pos, state);
	}

	@Nullable
	@Override
	public ManaPoolBlockEntity getPool() {
		var tile = level.getBlockEntity(worldPosition.below());
		return tile instanceof ManaPoolBlockEntity ? (ManaPoolBlockEntity) tile : null;
	}

	@Override
	public List<BellowsBlockEntity> getBellows(Level level, BlockPos worldPosition, ManaPoolBlockEntity self) {
		return new ArrayList<>();
	}
}
