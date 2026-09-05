package io.github.lounode.extrabotany.common.block.flower.generating;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import io.github.lounode.extrabotany.common.block.flower.ExtraGeneratingFlowerBlockEntity;
import vazkii.botania.api.block_entity.RadiusDescriptor;

import io.github.lounode.extrabotany.api.block.PassiveFlower;
import io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks;
import io.github.lounode.extrabotany.xplat.ExtraBotanyConfig;

public class SunshineLilyBlockEntity extends ExtraGeneratingFlowerBlockEntity implements PassiveFlower {

	public static final int MAX_MANA = 200;
	public static final int COOLDOWN = 4;
	public static final int MANA_PER_GENERATE = 2;

	private int passiveDecayTicks;

	public SunshineLilyBlockEntity(BlockPos pos, BlockState state) {
		super(ExtrabotanyFlowerBlocks.SUNSHINE_LILY, pos, state);
	}

	@Override
	public void tickFlower() {
		super.tickFlower();
		if (getLevel().isClientSide()) {
			return;
		}
		checkToDecay(this);
		if (ticksExisted % COOLDOWN != 0) {
			return;
		}
		if (!getLevel().isDay()) {
			return;
		}
		if (getMana() >= getMaxMana()) {
			return;
		}
		addMana(getGenerateMana());
		sync();
	}

	public int getGenerateMana() {
		return ExtraBotanyConfig.common().sunshineLilyProduceMana();
	}

	@Override
	public int getMaxMana() {
		return ExtraBotanyConfig.common().sunshineLilyMaxMana();
	}

	@Override
	public int getColor() {
		return 0xFFA500;
	}

	@Override
	public @Nullable RadiusDescriptor getRadius() {
		return null;
	}

	@Override
	public void readFromPacketNBT(CompoundTag cmp) {
		super.readFromPacketNBT(cmp);
		setPassiveDecayTicks(cmp.getInt(TAG_PASSIVE_DECAY_TICKS));
	}

	@Override
	public void writeToPacketNBT(CompoundTag cmp) {
		super.writeToPacketNBT(cmp);
		cmp.putInt(TAG_PASSIVE_DECAY_TICKS, getPassiveDecayTicks());
	}

	@Override
	public int getPassiveDecayTicks() {
		return passiveDecayTicks;
	}

	@Override
	public void setPassiveDecayTicks(int ticks) {
		this.passiveDecayTicks = ticks;
	}
}
