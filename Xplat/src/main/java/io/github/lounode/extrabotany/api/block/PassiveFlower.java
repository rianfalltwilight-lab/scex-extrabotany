package io.github.lounode.extrabotany.api.block;

import net.minecraft.world.level.block.Blocks;

import vazkii.botania.api.block_entity.SpecialFlowerBlockEntity;

import io.github.lounode.extrabotany.common.block.ExtraBotanyBlocks;

public interface PassiveFlower {
	String TAG_PASSIVE_DECAY_TICKS = "passiveDecayTicks";
	int DECAY_TIME = 72000;

	default void checkToDecay(SpecialFlowerBlockEntity flower) {
		if (!flower.isFloating()
				&& flower.getLevel().getBlockState(flower.getBlockPos().below()).is(ExtraBotanyBlocks.enchantedSoil)) {
			return;
		}
		if (getPassiveDecayTicks() > getDecayTime()) {
			flower.getLevel().destroyBlock(flower.getBlockPos(), false);
			if (Blocks.DEAD_BUSH.defaultBlockState().canSurvive(flower.getLevel(), flower.getBlockPos())) {
				flower.getLevel().setBlockAndUpdate(flower.getBlockPos(), Blocks.DEAD_BUSH.defaultBlockState());
			}
		}
		setPassiveDecayTicks(getPassiveDecayTicks() + 1);
	}

	int getPassiveDecayTicks();
	void setPassiveDecayTicks(int ticks);

	default int getDecayTime() {
		return DECAY_TIME;
	}
}
