package io.github.lounode.extrabotany.common.brew.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.trading.MerchantOffer;

/**
 * 打折。有这个效果的村民在交易时降低第一种货币的价格，每一级降低 1%。商友兰给村民这个效果。
 * Discount. A villager with this effect decreases the price of the first cost item in a trade, 1% for each level. The Trade Orchid gives this effect to villagers.
 *
 * <p>价格在 {@code TradeOrchidEditSpecialPrice} 里修改。
 * <p>The price is changed in {@code TradeOrchidEditSpecialPrice}.
 */
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
