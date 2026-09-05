package io.github.lounode.extrabotany.data.recipes;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import io.github.lounode.extrabotany.common.crafting.PedestalsRecipe;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.lib.ExtraBotanyTags;

import java.util.concurrent.CompletableFuture;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class PedestalRecipeProvider extends ExtraBotanyRecipeProvider {
	public PedestalRecipeProvider(PackOutput packOutput,
			CompletableFuture<HolderLookup.Provider> lookupProvider) {
		super(packOutput, lookupProvider);
	}

	@Override
	public String getName() {
		return "ExtraBotany pedestal recipes";
	}

	@Override
	public void buildRecipes(RecipeOutput consumer) {
		consumer.accept(id("gilded_potato_mashed"),
				new PedestalsRecipe(new ItemStack(ExtraBotanyItems.gildedPotatoMashed),
						Ingredient.of(ExtraBotanyTags.Items.HAMMERS),
						Ingredient.of(ExtraBotanyItems.gildedPotato), 5, 5), null);
		consumer.accept(id("spirit_fragment"),
				new PedestalsRecipe(new ItemStack(ExtraBotanyItems.spiritFragment),
						Ingredient.of(ExtraBotanyTags.Items.HAMMERS),
						Ingredient.of(ExtraBotanyItems.spiritFuel), 10, 5), null);
	}

	private static ResourceLocation id(String id) {
		return prefix("pedestal_smash/" + id);
	}
}
