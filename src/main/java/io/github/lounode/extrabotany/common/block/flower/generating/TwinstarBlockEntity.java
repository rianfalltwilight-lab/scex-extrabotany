package io.github.lounode.extrabotany.common.block.flower.generating;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import org.jetbrains.annotations.Nullable;

import io.github.lounode.extrabotany.common.block.flower.ExtraGeneratingFlowerBlockEntity;
import vazkii.botania.api.block_entity.RadiusDescriptor;

import io.github.lounode.extrabotany.api.block.PassiveFlower;
import io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks;
import io.github.lounode.extrabotany.xplat.EXplatAbstractions;
import io.github.lounode.extrabotany.xplat.ExtraBotanyConfig;

/**
 * 双子兰。被动产能花。它利用周围流体的温差产生魔力：最热和最冷的流体相差越大，产生的魔力越多。它经过一段时间后枯萎。
 * Twinstar. A passive generating flower. It makes mana from the temperature difference of the fluids around it: a larger difference between the hottest fluid and the coldest fluid gives more mana. It decays after some time.
 *
 * <p>来源：重制版（Lounode）。它对应 ExtraMeteorP 1.12 的 {@code SubTileGeminiOrchid}。
 * <p>Source: Reburn (Lounode). It corresponds to {@code SubTileGeminiOrchid} of ExtraMeteorP 1.12.
 */
public class TwinstarBlockEntity extends ExtraGeneratingFlowerBlockEntity implements PassiveFlower {

	public static final int RANGE = 1;
	public static final int MAX_MANA = 1000;

	public static final int TEMPERATURE_MIN = 0;
	public static final int TEMPERATURE_MAX = Integer.MAX_VALUE;
	public static final int COOLDOWN = 8;

	private int passiveDecayTicks;

	public TwinstarBlockEntity(BlockPos pos, BlockState state) {
		super(ExtrabotanyFlowerBlocks.TWINSTAR, pos, state);
	}

	@Override
	public void tickFlower() {
		super.tickFlower();
		if (getLevel().isClientSide()) {
			return;
		}
		checkToDecay(this);
		if (getMana() >= getMaxMana()) {
			return;
		}
		if (ticksExisted % COOLDOWN != 0) {
			return;
		}

		int mana = getGenerateMana();
		if (mana <= 0) {
			return;
		}

		addMana(mana);
		syncThrottled();
	}

	public int getGenerateMana() {
		int thermoelectric = getThermoelectric();
		return (int) (thermoelectric / 100F);
	}

	public int getThermoelectric() {
		BlockPos _pos = getEffectivePos();

		int tempMax = TEMPERATURE_MIN;
		int tempMin = TEMPERATURE_MAX;

		for (var pos : BlockPos.betweenClosed(_pos.offset(-RANGE, 0, -RANGE), _pos.offset(RANGE, 0, RANGE))) {
			Fluid fluid = getLevel().getFluidState(pos).getType();
			if (fluid.isSame(Fluids.EMPTY)) {
				continue;
			}
			int temp = EXplatAbstractions.INSTANCE.getFluidTemperature(fluid);

			tempMax = Math.max(tempMax, temp);
			tempMin = Math.min(tempMin, temp);
		}

		if (tempMax == TEMPERATURE_MIN && tempMin == TEMPERATURE_MAX) {
			return 0;
		}

		return Math.min(getTemperatureMax(), tempMax) - Math.min(getTemperatureMin(), tempMin);
	}

	public int getTemperatureMax() {
		return ExtraBotanyConfig.common().twinstarMaxTemperature();
	}

	public int getTemperatureMin() {
		return ExtraBotanyConfig.common().twinstarMinTemperature();
	}

	@Override
	public int getMaxMana() {
		return ExtraBotanyConfig.common().twinstarMaxMana();
	}

	@Override
	public int getColor() {
		return 0xfcec5c;
	}

	@Override
	public @Nullable RadiusDescriptor getRadius() {
		return RadiusDescriptor.Rectangle.square(getEffectivePos(), RANGE);
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
