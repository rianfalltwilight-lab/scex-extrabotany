package io.github.lounode.extrabotany.common.block.flower.generating;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import io.github.lounode.extrabotany.api.block.PassiveFlower;
import io.github.lounode.extrabotany.common.block.flower.ExtraGeneratingFlowerBlockEntity;
import io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks;
import io.github.lounode.extrabotany.xplat.ExtraBotanyConfig;
import vazkii.botania.api.block_entity.RadiusDescriptor;

/**
 * 风铃草。被动产能花。它必须能看到天空，并且高于 Y = {@value #BASE_Y}。它越高，产生的魔力越多；下雨时更多。和别的被动花一样，它经过一段时间后枯萎。
 * Bellflower. A passive generating flower. It must see the sky and be higher than Y = {@value #BASE_Y}. The higher it is, the more mana it makes; rain gives more. As the other passive flowers, it decays after some time.
 *
 * <p>来源：ExtraMeteorP 1.16 的 {@code SubTileBellFlower}。每 {@value #INTERVAL} tick 产生 (10 + 下雨时 3) × Y / 90 魔力。重燃的风力模型、周围方块减产和扇水仙吹死都是重燃加的，已经去掉。
 * <p>Source: {@code SubTileBellFlower} of ExtraMeteorP 1.16. Each {@value #INTERVAL} ticks it makes (10 + 3 when it rains) × Y / 90 mana. The wind model of Reburn, the loss from nearby blocks and the death by a Daffomill were Reburn additions, and they are removed.
 */
public class BellflowerBlockEntity extends ExtraGeneratingFlowerBlockEntity implements PassiveFlower {

	public static final int MAX_MANA = 300;
	public static final int BASE_Y = 90;
	public static final int BASE_GENERATE = 10;
	public static final int RAIN_GENERATE = 3;
	public static final int INTERVAL = 10;
	private static final int RANGE = 2;

	private int passiveDecayTicks;

	public BellflowerBlockEntity(BlockPos pos, BlockState state) {
		super(ExtrabotanyFlowerBlocks.BELLFLOWER, pos, state);
	}

	@Override
	public void tickFlower() {
		super.tickFlower();
		if (getLevel().isClientSide()) {
			return;
		}

		checkToDecay(this);
		if (isRemoved()) {
			return;
		}

		if (ticksExisted % INTERVAL == 0) {
			int mana = getGenerateMana();
			if (mana > 0) {
				addMana(mana);
				syncThrottled();
			}
		}
	}

	/**
	 * 这一次产生的魔力。看不到天空或者不高于 Y = {@value #BASE_Y} 时是 0。
	 * The mana of this time. It is 0 when the flower cannot see the sky or is not higher than Y = {@value #BASE_Y}.
	 */
	public int getGenerateMana() {
		BlockPos pos = getEffectivePos();
		int y = pos.getY();
		if (y <= BASE_Y || !getLevel().canSeeSky(pos)) {
			return 0;
		}
		int rain = getLevel().isRaining() ? RAIN_GENERATE : 0;
		return (BASE_GENERATE + rain) * y / BASE_Y;
	}

	@Override
	public int getMaxMana() {
		return ExtraBotanyConfig.common().bellflowerMaxMana();
	}

	@Override
	public int getColor() {
		return 0xFFFF99;
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
