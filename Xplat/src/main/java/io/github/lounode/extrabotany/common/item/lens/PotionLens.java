package io.github.lounode.extrabotany.common.item.lens;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;

import vazkii.botania.api.brew.Brew;
import vazkii.botania.api.internal.ManaBurst;
import vazkii.botania.api.mana.BurstProperties;
import vazkii.botania.common.brew.BotaniaBrews;
import vazkii.botania.common.item.lens.Lens;

import io.github.lounode.extrabotany.common.brew.BrewUtil;

public class PotionLens extends Lens {

	@Override
	public void apply(ItemStack stack, BurstProperties props) {
		props.motionModifier *= 0.9F;
		props.maxMana *= 4;
		props.manaLossPerTick *= 4;
		Brew brew = BrewUtil.getBrew(stack);
		if (brew != BotaniaBrews.FALLBACK) {
			props.color = brew.getColor(stack);
		}
	}

	@Override
	public boolean collideBurst(ManaBurst burst, HitResult pos, boolean isManaBlock, boolean shouldKill, ItemStack stack) {
		Projectile burstEntity = burst.entity();
		Level level = burstEntity.level();
		if (level.isClientSide()) {
			return false;
		}
		if (burst.isFake()) {
			return shouldKill;
		}
		Brew brew = BrewUtil.getBrew(stack);
		if (brew == BotaniaBrews.FALLBACK) {
			return shouldKill;
		}

		var entities = level.getEntitiesOfClass(LivingEntity.class, getAABB(burst));
		for (var entity : entities) {
			applyBrewForLivingEntity(entity, brew);
		}

		return true;
	}

	public void applyBrewForLivingEntity(LivingEntity entity, Brew brew) {
		var effectInstances = BrewUtil.getPotionEffects(brew);

		for (var effect : effectInstances) {
			MobEffectInstance newInstance = new MobEffectInstance(effect.getEffect(), effect.getDuration() / 3,
					effect.getAmplifier(), true, true);

			if (effect.getEffect().value().isInstantenous()) {
				effect.getEffect().value().applyInstantenousEffect(null, null, entity, effect.getAmplifier(), 1F);
			} else {
				entity.addEffect(newInstance);
			}
		}
	}

	public AABB getAABB(ManaBurst burst) {
		Projectile burstEntity = burst.entity();
		return new AABB(burstEntity.getX(), burstEntity.getY(), burstEntity.getZ(),
				burstEntity.xOld, burstEntity.yOld, burstEntity.zOld).inflate(1);
	}
}
