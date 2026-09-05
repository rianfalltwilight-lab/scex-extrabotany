package io.github.lounode.extrabotany.data.recipes;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import vazkii.botania.common.crafting.ElvenTradeRecipe;
import vazkii.botania.common.item.BotaniaItems;

import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

import java.util.concurrent.CompletableFuture;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class ElvenTradeProvider extends ExtraBotanyRecipeProvider {
	public ElvenTradeProvider(PackOutput packOutput,
			CompletableFuture<HolderLookup.Provider> lookupProvider) {
		super(packOutput, lookupProvider);
	}

	@Override
	public void buildRecipes(RecipeOutput consumer) {
		consumer.accept(id("elementium_quartz"),
				new ElvenTradeRecipe(new ItemStack[] { new ItemStack(ExtraBotanyItems.elementiumQuartz) },
						Ingredient.of(BotaniaItems.MANA_QUARTZ), Ingredient.of(BotaniaItems.MANA_QUARTZ)),
				null);
	}

	private static ResourceLocation id(String path) {
		return prefix("elven_trade/" + path);
	}

	@Override
	public String getName() {
		return "ExtraBotany elven trade recipes";
	}
}
