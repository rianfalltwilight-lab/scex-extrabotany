package io.github.lounode.extrabotany.common.brew.effect;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

import io.github.lounode.extrabotany.common.ExtraBotanyDamageTypes;
import io.github.lounode.extrabotany.common.brew.ExtraBotanyMobEffects;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * 链接。有这个效果的生物受伤时，{@value #LINK_RADIUS} 格内其他有这个效果的生物也受到一部分伤害：0 级是 20%，每一级再加 20%。
 * Link. When a creature with this effect is hurt, the other creatures with this effect in {@value #LINK_RADIUS} blocks also get a part of the damage: 20% at level 0, and 20% more for each level.
 *
 * <p>链接造成的伤害有自己的伤害类型，它不会再次触发链接。
 * <p>The damage from the link has its own damage type, and it does not start the link again.
 */
public class LinkMobEffect extends MobEffect {
	private static final double LINK_RADIUS = 8.0D;
	private static final float BASE_RATIO = 0.2F;
	private static final float PER_LEVEL_BONUS = 0.2F;

	//private static final int MAX_LINK_ENTITY = 10;
	public LinkMobEffect(MobEffectCategory category, int color) {
		super(category, color);
	}

	public static void onEntityDamaged(LivingIncomingDamageEvent event) {
		LivingEntity victim = event.getEntity();
		DamageSource source = event.getSource();
		float damage = event.getAmount();
		if (!victim.hasEffect(ExtraBotanyMobEffects.LINK)) {
			return;
		}
		if (source.is(ExtraBotanyDamageTypes.LINK_DAMAGE)) {
			return;
		}

		victim.level().getEntitiesOfClass(LivingEntity.class, victim.getBoundingBox().inflate(LINK_RADIUS))
				.stream()
				.filter(e -> e != victim)
				.filter(e -> e.hasEffect(ExtraBotanyMobEffects.LINK))
				.forEach(e -> {
					int level = e.getEffect(ExtraBotanyMobEffects.LINK).getAmplifier();
					float ratio = BASE_RATIO + (PER_LEVEL_BONUS * level);
					e.hurt(damageSource(e.level().registryAccess()), damage * ratio);
				});
	}

	private static DamageSource damageSource(RegistryAccess registryAccess) {
		return ExtraBotanyDamageTypes.Sources.linkDamage(registryAccess);
	}
}
