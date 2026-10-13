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

/**
 * 月耀百合。被动产能花。它在夜晚产生魔力，经过一段时间后枯萎。
 * Moonlight Lily. A passive generating flower. It makes mana at night, and it decays after some time.
 *
 * <p>来源：重制版（Lounode）。它对应 ExtraMeteorP 1.12 的 {@code SubTileMoonBless}。
 * <p>Source: Reburn (Lounode). It corresponds to {@code SubTileMoonBless} of ExtraMeteorP 1.12.
 */
public class MoonlightLilyBlockEntity extends ExtraGeneratingFlowerBlockEntity implements PassiveFlower {

	public static final int MAX_MANA = 200;
	public static final int MANA_PER_GENERATE = 1;
	public static final int COOLDOWN = 4;

	private int passiveDecayTicks;

	public MoonlightLilyBlockEntity(BlockPos pos, BlockState state) {
		super(ExtrabotanyFlowerBlocks.MOONLIGHT_LILY, pos, state);
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
		if (!getLevel().isNight()) {
			return;
		}
		if (getMana() >= getMaxMana()) {
			return;
		}
		addMana(getGenerateMana());
		syncThrottled();
	}

	public int getGenerateMana() {
		return ExtraBotanyConfig.common().moonlightLilyProduceMana();
	}

	@Override
	public int getMaxMana() {
		return ExtraBotanyConfig.common().moonlightLilyMaxMana();
	}

	@Override
	public int getColor() {
		return 0x8514e0;
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
