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
		if (getLevel().isClientSide()) {
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
				sync();
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
