package io.github.lounode.extrabotany.common.brew.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.trading.MerchantOffer;

public class DiscountMobEffect extends MobEffect {

	public static double DISCOUNT_PER_LEVEL = 0.01D;

	public DiscountMobEffect(MobEffectCategory category, int color) {
		super(category, color);
	}

	public int getDiscount(MobEffectInstance effectInstance, Villager villager, MerchantOffer offer, Player player) {
		int level = effectInstance.getAmplifier() + 1;
		int costA = offer.getBaseCostA().getCount();
		double discountRate = Math.min(1.0f, Math.max(0.0f, getDiscountPerLevel() * level));

		return -(int) (costA * discountRate);
	}

	public double getDiscountPerLevel() {
		return DISCOUNT_PER_LEVEL;
	}
}
