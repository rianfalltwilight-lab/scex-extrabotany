package io.github.lounode.extrabotany.common.block.flower.generating;


import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import org.jetbrains.annotations.Nullable;

import io.github.lounode.extrabotany.common.block.flower.ExtraGeneratingFlowerBlockEntity;
import vazkii.botania.api.block_entity.RadiusDescriptor;

import io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks;
import io.github.lounode.extrabotany.common.brew.ExtraBotanyMobEffects;
import io.github.lounode.extrabotany.xplat.ExtraBotanyConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * 鲜血妖姬。它伤害紧挨着它的生物，并用它们的生命产生魔力。同一个生物被伤害的次数越多，产生的魔力越少。
 * Blood Enchantress. It hurts the creatures adjacent to it and makes mana from their health. The more times the same creature is hurt, the less mana it gives.
 *
 * <p>被伤害的生物得到"血之诱惑"效果，每次伤害加一级（最高 11 级），持续时间随等级变长。等级越高，这个生物给的魔力越少；10 级以上时花不再伤害它。周围所有生物的等级之和超过 35 时，花停止工作。效果消失后，生物重新可以产出魔力。
 * <p>A creature that is hurt gets the "Blood Temptation" effect, one level more for each hit (11 levels at most), and the duration becomes longer with the level. The higher the level, the less mana the creature gives; from the 10th amplifier the flower does not hurt it. When the sum of the amplifiers of all the creatures near it is more than 35, the flower stops. When the effect ends, the creature gives mana again.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code SubTileBloodyEnchantress} 和 {@code ExtraBotanyAPI.addPotionEffect}。每只生物一次 3 点魔法伤害，给 {@link #getManaFor} 魔力，并让花休息 20 tick；必须绑定魔力发射器；上限 800。
 * <p>Source: {@code SubTileBloodyEnchantress} and {@code ExtraBotanyAPI.addPotionEffect} of ExtraMeteorP 1.16. Each creature takes 3 magic damage one time, gives {@link #getManaFor} mana, and makes the flower rest for 20 ticks; it must be bound to a mana spreader; the maximum is 800.
 */
public class BloodEnchantressBlockEntity extends ExtraGeneratingFlowerBlockEntity {

	private static final String TAG_BURN_TIME = "burnTime";

	private static final int RANGE = 1;

	public static final int MAX_MANA = 800;
	public static final int DENY_PRODUCE_LEVEL = 35;
	public static final float DAMAGE = 3F;
	public static final int BURN_TIME_PER_CREATURE = 20;
	public static final int TEMPTATION_BASE_TICKS = 100;
	public static final int TEMPTATION_TICKS_PER_LEVEL = 200;
	public static final int TEMPTATION_MAX_AMPLIFIER = 10;

	private int burnTime;

	public BloodEnchantressBlockEntity(BlockPos pos, BlockState state) {
		super(ExtrabotanyFlowerBlocks.BLOOD_ENCHANTRESS, pos, state);
	}

	@Override
	public void tickFlower() {
		super.tickFlower();
		if (getLevel().isClientSide()) {
			return;
		}
		if (getBurnTime() > 0) {
			setBurnTime(getBurnTime() - 1);
		}

		// One AABB query per tick (was two: getInhibition() scanned the same box again).
		List<LivingEntity> entities = new ArrayList<>();
		for (LivingEntity entity : getLevel().getEntitiesOfClass(LivingEntity.class, new AABB(getEffectivePos()).inflate(RANGE))) {
			if (entity.isAlive()) {
				entities.add(entity);
			}
		}
		if (entities.isEmpty()) {
			return;
		}
		int inhibitionLevel = getInhibition(entities);
		if (inhibitionLevel > DENY_PRODUCE_LEVEL) {
			return;
		}
		// 1.16 只有绑定了魔力发射器、没有在冷却、魔力没满时才吸血。
		// In 1.16 it only takes blood when it is bound to a mana spreader, does not cool down, and is not full.
		if (!isValidBinding() || getBurnTime() > 0 || getMana() >= getMaxMana()) {
			return;
		}

		for (var entity : entities) {
			int amplifier = getTemptation(entity);
			if (amplifier > 4 && entity.getRandom().nextBoolean()) {
				// Above 4, the flower skips the creature half of the time.
				continue;
			}
			if (amplifier >= TEMPTATION_MAX_AMPLIFIER) {
				// The original stops the whole scan here, not only for this creature. The effect runs out, so this does not last.
				break;
			}
			if (entity.hurt(getLevel().damageSources().magic(), DAMAGE)) {
				addMana(getManaFor(amplifier, inhibitionLevel));
				addTemptation(entity);
				setBurnTime(getBurnTime() + BURN_TIME_PER_CREATURE);
				syncThrottled();
			}
		}
	}

	/**
	 * 一只生物这一次给的魔力：22 × 12 × (1 − 0.04 × 它的等级 − 0.02 × 周围等级之和)。
	 * The mana that one creature gives this time: 22 × 12 × (1 − 0.04 × its amplifier − 0.02 × the sum of the amplifiers near the flower).
	 */
	public static int getManaFor(int amplifier, int inhibitionLevel) {
		return (int) (22F * 12F * (1F - 0.04F * amplifier - 0.02F * inhibitionLevel));
	}

	public int getInhibition() {
		List<LivingEntity> entities = new ArrayList<>();
		for (LivingEntity entity : getLevel().getEntitiesOfClass(LivingEntity.class, new AABB(getEffectivePos()).inflate(RANGE))) {
			if (entity.isAlive()) {
				entities.add(entity);
			}
		}
		return getInhibition(entities);
	}

	private static int getInhibition(List<LivingEntity> entities) {
		int inhibitionLevel = 0;
		for (var entity : entities) {
			inhibitionLevel += getTemptation(entity);
		}
		return inhibitionLevel;
	}

	/**
	 * 生物身上血之诱惑的等级（amplifier），没有这个效果时是 0。和原版一样，没有效果和 0 级效果都算 0。
	 * The amplifier of Blood Temptation on the creature, 0 without the effect. The same as in the original, no effect and an effect of amplifier 0 both count as 0.
	 */
	public static int getTemptation(LivingEntity entity) {
		MobEffectInstance effect = entity.getEffect(ExtraBotanyMobEffects.BLOOD_TEMPTATION);
		return effect == null ? 0 : effect.getAmplifier();
	}

	/**
	 * 给生物加一级血之诱惑。没有效果时得到 0 级、100 tick；已有效果时加一级（最高 10），持续 100 + 200 × 原等级 tick。
	 * Adds one level of Blood Temptation to the creature. Without the effect it gets amplifier 0 for 100 ticks; with the effect, one level more (10 at most) for 100 + 200 × the old amplifier ticks.
	 */
	public static void addTemptation(LivingEntity entity) {
		MobEffectInstance effect = entity.getEffect(ExtraBotanyMobEffects.BLOOD_TEMPTATION);
		if (effect == null) {
			entity.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.BLOOD_TEMPTATION, TEMPTATION_BASE_TICKS, 0));
			return;
		}
		int amplifier = effect.getAmplifier();
		int duration = TEMPTATION_BASE_TICKS + TEMPTATION_TICKS_PER_LEVEL * amplifier;
		entity.removeEffect(ExtraBotanyMobEffects.BLOOD_TEMPTATION);
		entity.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.BLOOD_TEMPTATION, duration,
				Math.min(TEMPTATION_MAX_AMPLIFIER, amplifier + 1)));
	}

	@Override
	public int getMaxMana() {
		return ExtraBotanyConfig.common().bloodEnchantressMaxMana();
	}

	@Override
	public int getColor() {
		return 0x8B0000;
	}

	@Override
	public @Nullable RadiusDescriptor getRadius() {
		return RadiusDescriptor.Rectangle.square(getEffectivePos(), RANGE);
	}

	@Override
	public void writeToPacketNBT(CompoundTag cmp) {
		super.writeToPacketNBT(cmp);
		cmp.putInt(TAG_BURN_TIME, getBurnTime());
	}

	@Override
	public void readFromPacketNBT(CompoundTag cmp) {
		super.readFromPacketNBT(cmp);
		setBurnTime(cmp.getInt(TAG_BURN_TIME));
	}

	public int getBurnTime() {
		return burnTime;
	}

	public void setBurnTime(int burnTime) {
		this.burnTime = burnTime;
	}
}
