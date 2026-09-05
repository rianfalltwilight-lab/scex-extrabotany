package io.github.lounode.extrabotany.common.brew;

import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.brew.Brew;
import vazkii.botania.common.brew.BotaniaBrews;
import io.github.lounode.extrabotany.common.helper.ItemNBTHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BrewUtil {
	public static final Brew EMPTY = BotaniaBrews.FALLBACK;
	public static final String TAG_BREW_KEY = "brewKey";

	public static Brew getBrew(ItemStack stack) {
		String key = ItemNBTHelper.getString(stack, TAG_BREW_KEY, "");
		Registry<Brew> registry = BotaniaAPI.instance().getBrewRegistry();
		if (registry == null) {
			return BotaniaBrews.FALLBACK;
		}
		ResourceLocation location = ResourceLocation.tryParse(key);
		if (location == null) {
			return BotaniaBrews.FALLBACK;
		}
		Brew brew = registry.get(location);
		return brew != null ? brew : BotaniaBrews.FALLBACK;
	}

	public static void setBrew(ItemStack stack, Brew brew) {
		ResourceLocation id = Objects.requireNonNull(BotaniaAPI.instance().getBrewRegistry()).getKey(brew);
		ItemNBTHelper.setString(stack, TAG_BREW_KEY, id.toString());
	}

	public static boolean hasInstantEffects(Brew brew) {
		if (!getPotionEffects(brew).isEmpty()) {
			for (MobEffectInstance mobeffectinstance : getPotionEffects(brew)) {
				if (mobeffectinstance.getEffect().value().isInstantenous()) {
					return true;
				}
			}
		}

		return false;
	}

	public static List<MobEffectInstance> getPotionEffects(Brew brew) {
		//Why need an itemstack and unused in code???
		if (!brew.getPotionEffects(new ItemStack(Items.AIR)).isEmpty()) {
			return brew.getPotionEffects(new ItemStack(Items.AIR));
		}
		return new ArrayList<>();
	}

	public static int getColor(Brew brew) {
		//Also why???
		return brew.getColor(new ItemStack(Items.AIR));
	}

	public static void addPotionTooltip(Brew brew, List<Component> lores, float durationFactor,
			int amplifierAddition, float tickRate) {
		List<MobEffectInstance> effects = getPotionEffects(brew).stream()
				.map(effect -> new MobEffectInstance(effect.getEffect(), effect.getDuration(),
						effect.getAmplifier() + amplifierAddition, effect.isAmbient(), effect.isVisible(), effect.showIcon()))
				.toList();
		PotionContents.addPotionTooltip(effects, lores::add, durationFactor, tickRate);
	}
}
