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
import vazkii.botania.common.component.BotaniaDataComponents;
import vazkii.botania.common.item.brew.BaseBrewItem;
import io.github.lounode.extrabotany.common.helper.ItemNBTHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 酿造（brew）的工具方法。
 * Utility methods for brews.
 *
 * <p>植物魔法的酿造物品把酿造的 ID 存在数据组件里。本模组的透镜和手雷，以及旧存档里的物品，把它存在自定义数据的 {@code brewKey} 里。这里的读取方法两处都读。
 * <p>The brew items of Botania keep the ID of the brew in a data component. The lens and the grenade of this mod, and the items of old saves, keep it in {@code brewKey} of the custom data. The read method here reads the two places.
 */
public class BrewUtil {
	public static final Brew EMPTY = BotaniaBrews.FALLBACK;
	public static final String TAG_BREW_KEY = "brewKey";

	public static Brew getBrew(ItemStack stack) {
		Registry<Brew> registry = BotaniaAPI.instance().getBrewRegistry();
		if (registry == null) {
			return BotaniaBrews.FALLBACK;
		}
		ResourceLocation location = stack.get(BotaniaDataComponents.BREW);
		if (location == null) {
			// Existing ExtraBotany lenses, grenades and older stacks still use this key.
			location = ResourceLocation.tryParse(ItemNBTHelper.getString(stack, TAG_BREW_KEY, ""));
		}
		if (location == null) {
			return BotaniaBrews.FALLBACK;
		}
		Brew brew = registry.get(location);
		return brew != null ? brew : BotaniaBrews.FALLBACK;
	}

	public static void setBrew(ItemStack stack, Brew brew) {
		if (stack.getItem() instanceof BaseBrewItem) {
			// Drinking and Botania's recipe UI read the component, not custom_data.brewKey.
			BaseBrewItem.setBrew(stack, brew);
			ItemNBTHelper.removeEntry(stack, TAG_BREW_KEY);
			return;
		}
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
