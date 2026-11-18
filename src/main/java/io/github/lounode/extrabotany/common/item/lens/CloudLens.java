package io.github.lounode.extrabotany.common.item.lens;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

import vazkii.botania.api.brew.Brew;
import vazkii.botania.api.internal.ManaBurst;
import vazkii.botania.api.mana.BurstProperties;
import vazkii.botania.common.brew.BotaniaBrews;

import io.github.lounode.extrabotany.common.brew.BrewUtil;

/**
 * 魔力透镜：云朵。脉冲飞过生物时，每个生物消耗 400 魔力，在脉冲的位置放出一片药水云；云里的效果是透镜里酿造的效果，持续时间是原来的四分之一。
 * Cloud Lens. When the burst flies past a creature, each creature uses 400 mana and a potion cloud appears at the burst; the cloud has the effects of the brew in the lens, with one quarter of the usual duration.
 *
 * <p>来源：ExtraMeteorP 1.12 {@code LensCloud}（对照原版审计 E6）。它和药水透镜一样用合成来装入酿造，所以继承药水透镜；碰撞时不再另外给效果。
 * <p>Source: {@code LensCloud} of ExtraMeteorP 1.12 (parity audit E6). Like the potion lens it gets its brew by crafting, so it extends the potion lens; a collision gives no extra effect.
 */
public class CloudLens extends PotionLens {
	public static final int MANA_PER_CLOUD = 400;

	@Override
	public void apply(ItemStack stack, BurstProperties props) {
		props.motionModifier *= 0.75F;
		props.maxMana *= 5;
		props.manaLossPerTick *= 4;
		Brew brew = BrewUtil.getBrew(stack);
		if (brew != BotaniaBrews.FALLBACK) {
			props.color = brew.getColor(stack);
		}
	}

	@Override
	public boolean collideBurst(ManaBurst burst, HitResult pos, boolean isManaBlock, boolean shouldKill, ItemStack stack) {
		return shouldKill;
	}

	@Override
	public void updateBurst(ManaBurst burst, ItemStack stack) {
		Projectile entity = burst.entity();
		Level level = entity.level();
		if (level.isClientSide()) {
			return;
		}
		Brew brew = BrewUtil.getBrew(stack);
		if (brew == BotaniaBrews.FALLBACK) {
			return;
		}
		for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, getAABB(burst))) {
			if (burst.getMana() < MANA_PER_CLOUD) {
				return;
			}
			burst.setMana(burst.getMana() - MANA_PER_CLOUD);
			if (!burst.isFake()) {
				spawnCloud(level, entity.getX(), entity.getY(), entity.getZ(), brew);
			}
		}
	}

	public static AreaEffectCloud spawnCloud(Level level, double x, double y, double z, Brew brew) {
		AreaEffectCloud cloud = new AreaEffectCloud(level, x, y, z);
		cloud.setRadius(3F);
		cloud.setRadiusOnUse(-0.5F);
		cloud.setWaitTime(10);
		cloud.setRadiusPerTick(-cloud.getRadius() / cloud.getDuration());
		for (MobEffectInstance effect : BrewUtil.getPotionEffects(brew)) {
			cloud.addEffect(new MobEffectInstance(effect.getEffect(), effect.getDuration() / 4,
					Math.max(0, effect.getAmplifier()), true, true));
		}
		level.addFreshEntity(cloud);
		return cloud;
	}
}
