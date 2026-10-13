package io.github.lounode.extrabotany.common.brew.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;

import io.github.lounode.extrabotany.common.brew.ExtraBotanyMobEffects;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.Objects;

/**
 * 镜棘。有这个效果的生物受到攻击时，把一部分伤害反弹给攻击者。反弹的比例是 1 / (6 - 等级)，最大是全部伤害。
 * Thirror. When a creature with this effect is attacked, it returns a part of the damage to the attacker. The returned fraction is 1 / (6 - level), with a maximum of the full damage.
 */
public class ThirrorMobEffect extends MobEffect {
	public ThirrorMobEffect(MobEffectCategory category, int color) {
		super(category, color);
	}

	public static class EventHandler {

		public static void onLivingAttack(LivingIncomingDamageEvent event) {
			// Reflected damage must not bounce between two Thirror users before either
			// original hurt call can finish and establish its invulnerability window.
			if (event.getSource().is(DamageTypes.THORNS) || event.getAmount() <= 0) {
				return;
			}
			LivingEntity defender = event.getEntity();
			if (!defender.hasEffect(ExtraBotanyMobEffects.THIRROR)) {
				return;
			}
			if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) {
				return;
			}

			int level = Objects.requireNonNull(defender.getEffect(ExtraBotanyMobEffects.THIRROR)).getAmplifier();
			float returnDamage = event.getAmount() / Math.max(1, 6 - level);

			attacker.hurt(defender.damageSources().thorns(defender), returnDamage);
		}
	}
}
