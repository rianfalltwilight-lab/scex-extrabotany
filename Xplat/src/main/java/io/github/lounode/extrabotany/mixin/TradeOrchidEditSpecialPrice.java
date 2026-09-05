package io.github.lounode.extrabotany.mixin;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.trading.MerchantOffer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.lounode.extrabotany.common.brew.effect.DiscountMobEffect;

@Mixin(Villager.class)
public class TradeOrchidEditSpecialPrice {

	@Inject(
		method = "startTrading",
		at = @At("HEAD")
	)
	private void onStartTrade(Player player, CallbackInfo ci) {
		Villager self = (Villager) (Object) this;
		var effects = self.getActiveEffects();

		MobEffectInstance discountEffect = null;
		for (var effect : effects) {
			if (effect.getEffect().value() instanceof DiscountMobEffect) {
				discountEffect = effect;
			}
		}

		if (discountEffect != null) {
			DiscountMobEffect effect = (DiscountMobEffect) discountEffect.getEffect().value();

			for (MerchantOffer offer : self.getOffers()) {
				int discount = effect.getDiscount(discountEffect, self, offer, player);
				offer.setSpecialPriceDiff(discount);
			}
		}
	}
}
