package io.github.lounode.extrabotany.common.block.flower.functional;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import org.jetbrains.annotations.Nullable;

import io.github.lounode.extrabotany.common.block.flower.ExtraFunctionalFlowerBlockEntity;
import vazkii.botania.api.block_entity.RadiusDescriptor;

import io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks;
import io.github.lounode.extrabotany.common.brew.ExtraBotanyMobEffects;
import io.github.lounode.extrabotany.xplat.ExtraBotanyConfig;

/**
 * 死之花。每秒它在范围里找一个生命值不超过一半的生物，对它造成魔法伤害，并给它治疗反转效果。红石信号让它停止工作。
 * Necrofleur. On each second it finds one creature in the range that has half of its health or less, gives magic damage to it, and gives it the Heal Reverse effect. A redstone signal stops it.
 *
 * <p>伤害来源是这个生物自己。原版也是这样做的，所以怪物不会因为这朵花去攻击别的生物。
 * <p>The source of the damage is the creature itself. The original does the same, thus a monster does not attack other creatures because of this flower.
 *
 * <p>来源：重制版（Lounode）。它对应 ExtraMeteorP 1.12 的 {@code SubTileNecrofleur}。
 * <p>Source: Reburn (Lounode). It corresponds to {@code SubTileNecrofleur} of ExtraMeteorP 1.12.
 */
public class NecrofleurBlockEntity extends ExtraFunctionalFlowerBlockEntity {

	private static final int RANGE = 7;
	private static final float DAMAGE = 10;
	private static final int RANGE_MINI = 3;
	private static final float DAMAGE_MINI = 6;

	public static final int MAX_MANA = 200;
	public static final int COOLDOWN = 20;

	public static final int DAMAGE_COST = 200;

	public NecrofleurBlockEntity(BlockPos pos, BlockState state) {
		super(ExtrabotanyFlowerBlocks.NECROFLEUR, pos, state);
	}

	public NecrofleurBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public void tickFlower() {
		super.tickFlower();
		if (getLevel().isClientSide() || isPowered()) {
			return;
		}
		if (ticksExisted % COOLDOWN != 0) {
			return;
		}
		if (getMana() < getDamageCost()) {
			return;
		}

		var entities = getLevel().getEntitiesOfClass(LivingEntity.class, new AABB(getEffectivePos()).inflate(getRange()));

		for (var entity : entities) {
			if (getMana() < getDamageCost()) {
				break;
			}
			if (entity.getHealth() <= entity.getMaxHealth() * 0.5F) {
				entity.hurt(entity.damageSources().indirectMagic(entity, entity), getDamage());
				entity.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.HEAL_REVERSE, 20 * 15));

				addMana(-getDamageCost());
				syncThrottled();
				break;
			}
		}
	}

	public int getDamageCost() {
		return DAMAGE_COST;
	}

	public int getRange() {
		return RANGE;
	}

	public float getDamage() {
		return DAMAGE;
	}

	@Override
	public int getMaxMana() {
		return ExtraBotanyConfig.common().necrofleurMaxMana();
	}

	@Override
	public int getColor() {
		return 0xa05058;
	}

	@Override
	public @Nullable RadiusDescriptor getRadius() {
		return RadiusDescriptor.Rectangle.square(getEffectivePos(), getRange());
	}

	public static class Mini extends NecrofleurBlockEntity {

		public Mini(BlockEntityType<?> type, BlockPos pos, BlockState state) {
			super(type, pos, state);
		}

		public Mini(BlockPos pos, BlockState state) {
			super(ExtrabotanyFlowerBlocks.NECROFLEUR_CHIBI, pos, state);
		}

		@Override
		public int getRange() {
			return RANGE_MINI;
		}

		@Override
		public float getDamage() {
			return DAMAGE_MINI;
		}
	}
}
