package io.github.lounode.extrabotany.common.block.flower.functional;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import io.github.lounode.extrabotany.common.block.flower.ExtraFunctionalFlowerBlockEntity;
import vazkii.botania.api.block_entity.RadiusDescriptor;
import vazkii.botania.api.block_entity.SpecialFlowerBlockEntity;

import io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks;
import io.github.lounode.extrabotany.xplat.ExtraBotanyConfig;

import java.util.List;

public class SerenitianBlockEntity extends ExtraFunctionalFlowerBlockEntity {
	private static final int COOLDOWN = 20;

	public static final int RANGE = 3;

	private static final String INHIBITION_TAG = "passiveDecayTicks";

	public SerenitianBlockEntity(BlockPos pos, BlockState state) {
		super(ExtrabotanyFlowerBlocks.SERENITIAN, pos, state);
	}

	@Override
	public void tickFlower() {
		super.tickFlower();
		if (getLevel().isClientSide()) {
			return;
		}
		if (ticksExisted % COOLDOWN != 0) {
			return;
		}

		var flowers = getSpecialFlowerNearby();
		var registries = getLevel().registryAccess();

		for (var flower : flowers) {
			var cmp = flower.saveCustomOnly(registries);
			if (!cmp.contains(INHIBITION_TAG)) {
				continue;
			}
			cmp.putInt(INHIBITION_TAG, 0);
			flower.loadCustomOnly(cmp, registries);
			flower.setChanged();
		}
	}

	public List<SpecialFlowerBlockEntity> getSpecialFlowerNearby() {
		BlockPos base = getEffectivePos();
		return BlockPos.betweenClosedStream(
				base.offset(-getRange(), 0, -getRange()),
				base.offset(getRange(), 0, getRange())
		)
				.map(pos -> getLevel().getBlockEntity(pos))
				.filter(be -> be instanceof SpecialFlowerBlockEntity)
				.map(be -> (SpecialFlowerBlockEntity) be)
				.toList();
	}

	@Override
	public int getMaxMana() {
		return 0;
	}

	@Override
	public int getColor() {
		return 0x8A68BF;
	}

	@Override
	public @Nullable RadiusDescriptor getRadius() {
		return RadiusDescriptor.Rectangle.square(getEffectivePos(), getRange());
	}

	public int getRange() {
		return ExtraBotanyConfig.common().serenitianRange();
	}
}
