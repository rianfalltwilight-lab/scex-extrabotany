package io.github.lounode.extrabotany.common.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import vazkii.botania.api.brew.Brew;

import io.github.lounode.extrabotany.common.brew.BrewUtil;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

import javax.annotation.Nullable;

import java.util.List;

public class HolyWaterGrenadeEntity extends ThrownPotion {

	public static final double SPLASH_RANGE = 4.0D;
	private static final double SPLASH_RANGE_SQ = 16.0D;

	public HolyWaterGrenadeEntity(EntityType<? extends HolyWaterGrenadeEntity> entityType, Level level) {
		super(entityType, level);
	}

	public HolyWaterGrenadeEntity(Level level, LivingEntity shooter) {
		this(level, shooter.getX(), shooter.getEyeY() - (double) 0.1F, shooter.getZ());
		this.setOwner(shooter);
	}

	public HolyWaterGrenadeEntity(Level level, double x, double y, double z) {
		this(ExtraBotanyEntityType.HOLY_WATER_GRENADE, level);
		this.setPos(x, y, z);
	}

	@Override
	protected Item getDefaultItem() {
		return ExtraBotanyItems.holyWaterGrenade;
	}

	@Override
	protected void onHitBlock(BlockHitResult result) {
		BlockState blockstate = this.level().getBlockState(result.getBlockPos());
		blockstate.onProjectileHit(this.level(), blockstate, result, this);
	}

	@Override
	protected void onHit(HitResult result) {
		HitResult.Type hitresult$type = result.getType();
		if (hitresult$type == HitResult.Type.ENTITY) {
			this.onHitEntity((EntityHitResult) result);
			this.level().gameEvent(GameEvent.PROJECTILE_LAND, result.getLocation(), GameEvent.Context.of(this, (BlockState) null));
		} else if (hitresult$type == HitResult.Type.BLOCK) {
			BlockHitResult blockhitresult = (BlockHitResult) result;
			this.onHitBlock(blockhitresult);
			BlockPos blockpos = blockhitresult.getBlockPos();
			this.level().gameEvent(GameEvent.PROJECTILE_LAND, blockpos, GameEvent.Context.of(this, this.level().getBlockState(blockpos)));
		}

		if (!this.level().isClientSide) {
			Brew brew = getBrew();

			List<MobEffectInstance> list = BrewUtil.getPotionEffects(brew);
			this.applySplash(list, result.getType() == HitResult.Type.ENTITY ? ((EntityHitResult) result).getEntity() : null);

			int i = BrewUtil.hasInstantEffects(brew) ? 2007 : 2002;
			this.level().levelEvent(i, this.blockPosition(), BrewUtil.getColor(brew));
			this.discard();
		}
	}

	public void applySplash(List<MobEffectInstance> effectInstances, @Nullable Entity target) {
		AABB effectAura = this.getBoundingBox().inflate(SPLASH_RANGE, 2.0D, SPLASH_RANGE);

		List<LivingEntity> nearby = this.level().getEntitiesOfClass(LivingEntity.class, effectAura);
		if (nearby.isEmpty()) {
			return;
		}

		Entity source = this.getEffectSource();

		for (LivingEntity victim : nearby) {
			if (!victim.isAffectedByPotions()) {
				continue;
			}
			double distanceToSqr = this.distanceToSqr(victim);
			if (distanceToSqr > SPLASH_RANGE_SQ) {
				continue;
			}

			double d1 = victim == target ? 1.0D : 1.0D - Math.sqrt(distanceToSqr) / SPLASH_RANGE;
			boolean isSource = victim == source;

			for (MobEffectInstance tmpInstance : effectInstances) {
				var mobeffect = tmpInstance.getEffect();
				boolean isBeneficial = mobeffect.value().isBeneficial();

				//Only give beneficial to owner. harmful to others
				if ((isBeneficial && !isSource) || (!isBeneficial && isSource)) {
					continue;
				}

				if (mobeffect.value().isInstantenous()) {
					mobeffect.value().applyInstantenousEffect(this, this.getOwner(), victim, tmpInstance.getAmplifier(), d1);
					continue;
				}

				int durationAdjustByDistance = tmpInstance.mapDuration(
						(originalDuration) -> (int) (d1 * (double) originalDuration + 0.5D));

				MobEffectInstance apply = new MobEffectInstance(mobeffect, durationAdjustByDistance, tmpInstance.getAmplifier(), tmpInstance.isAmbient(), tmpInstance.isVisible());
				if (!apply.endsWithin(20)) {
					victim.addEffect(apply, source);
				}
			}
		}
	}

	public Brew getBrew() {
		return BrewUtil.getBrew(getItem());
	}
}
