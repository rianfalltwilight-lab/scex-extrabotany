package io.github.lounode.extrabotany.common.item.brew;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.brew.Brew;
import vazkii.botania.common.brew.BotaniaBrews;
import vazkii.botania.common.component.BotaniaDataComponents;
import vazkii.botania.common.item.brew.BaseBrewItem;

import io.github.lounode.extrabotany.common.brew.ExtraBotanyBrews;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

import java.util.function.Supplier;

public class ManaCocktailItem extends BaseBrewItem {

	public ManaCocktailItem(Properties properties, int swigs, int drinkSpeed, Supplier<Item> baseItem) {
		super(drinkSpeed, baseItem, properties.component(BotaniaDataComponents.MAX_USES, swigs));
	}

	@Override
	public void addToCreativeTab(Item me, CreativeModeTab.Output output) {
		for (Brew brew : BotaniaAPI.instance().getBrewRegistry()) {
			if (brew == BotaniaBrews.FALLBACK) {
				continue;
			}
			if (brew == ExtraBotanyBrews.manaCocktail) {
				continue;
			}

			ItemStack stack = new ItemStack(this);
			setBrew(stack, brew);
			output.accept(stack);
		}
	}

	@Override
	public @NotNull Component getName(@NotNull ItemStack stack) {
		Brew brew = getBrew(stack);
		if (brew == ExtraBotanyBrews.manaCocktail && getSwigsLeft(stack) == 1) {
			return Component.translatable("item.extrabotany.mana_cocktail.default");
		}
		return super.getName(stack);
	}

	public static ItemStack getDefaultCocktail() {
		ItemStack manaCocktail = new ItemStack(ExtraBotanyItems.manaCocktail);
		ManaCocktailItem.setBrew(manaCocktail, ExtraBotanyBrews.manaCocktail);
		manaCocktail.set(BotaniaDataComponents.REMAINING_USES, 1);

		return manaCocktail;
	}
}
