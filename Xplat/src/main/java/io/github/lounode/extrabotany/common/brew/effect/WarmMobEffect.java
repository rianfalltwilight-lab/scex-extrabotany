package io.github.lounode.extrabotany.common.brew.effect;

import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

import io.github.lounode.extrabotany.common.brew.ExtraBotanyMobEffects;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

public class WarmMobEffect extends MobEffect {
	public WarmMobEffect(MobEffectCategory category, int color) {
		super(category, color);
	}

	public static class EventHandler {

		public static void onEntityHurt(LivingIncomingDamageEvent event) {
			if (!event.getSource().is(DamageTypes.FREEZE)) {
				return;
			}
			if (event.getEntity().hasEffect(ExtraBotanyMobEffects.WARM)) {
				event.setCanceled(true);
			}
		}
	}
}
