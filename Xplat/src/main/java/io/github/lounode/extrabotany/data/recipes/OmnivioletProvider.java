package io.github.lounode.extrabotany.data.recipes;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import io.github.lounode.extrabotany.common.crafting.OmniVioletsRecipe;

import java.util.concurrent.CompletableFuture;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class OmnivioletProvider extends ExtraBotanyRecipeProvider {
	public OmnivioletProvider(PackOutput packOutput,
			CompletableFuture<HolderLookup.Provider> lookupProvider) {
		super(packOutput, lookupProvider);
	}

	@Override
	public void buildRecipes(RecipeOutput consumer) {
		consumer.accept(id("book"), new OmniVioletsRecipe(Ingredient.of(Items.BOOK), 50), null);
		consumer.accept(id("written_book"), new OmniVioletsRecipe(Ingredient.of(Items.WRITTEN_BOOK), 65), null);
	}

	private static ResourceLocation id(String id) {
		return prefix("omniviolet/" + id);
	}

	@Override
	public String getName() {
		return "Extrabotany Omniviolet recipes";
	}
}
