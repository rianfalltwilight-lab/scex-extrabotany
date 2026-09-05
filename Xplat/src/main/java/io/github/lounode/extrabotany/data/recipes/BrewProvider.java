package io.github.lounode.extrabotany.data.recipes;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;

import vazkii.botania.api.brew.Brew;
import vazkii.botania.common.crafting.BotanicalBreweryRecipe;

import io.github.lounode.extrabotany.common.brew.ExtraBotanyBrews;

import java.util.concurrent.CompletableFuture;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class BrewProvider extends ExtraBotanyRecipeProvider {
	public BrewProvider(PackOutput packOutput,
			CompletableFuture<HolderLookup.Provider> lookupProvider) {
		super(packOutput, lookupProvider);
	}

	@Override
	public String getName() {
		return "Extrabotany Brew recipes";
	}

	@Override
	public void buildRecipes(RecipeOutput consumer) {
		make(consumer, "revolution", ExtraBotanyBrews.revolution,
				Ingredient.of(Items.NETHER_WART), Ingredient.of(Items.IRON_PICKAXE), Ingredient.of(Items.SUGAR));
		make(consumer, "deadpool", ExtraBotanyBrews.deadpool,
				Ingredient.of(Items.NETHER_WART), Ingredient.of(Items.ROTTEN_FLESH),
				Ingredient.of(Items.BONE), Ingredient.of(Items.BLAZE_POWDER));
		make(consumer, "shield", ExtraBotanyBrews.shield,
				Ingredient.of(Items.NETHER_WART), Ingredient.of(Items.GOLDEN_APPLE),
				Ingredient.of(Items.BUCKET), Ingredient.of(Blocks.OBSIDIAN));
		make(consumer, "floating", ExtraBotanyBrews.floating,
				Ingredient.of(Items.NETHER_WART), Ingredient.of(Items.CHORUS_FRUIT), Ingredient.of(Items.SUGAR));
		make(consumer, "all_in_one", ExtraBotanyBrews.allInOne,
				Ingredient.of(Items.NETHER_WART), Ingredient.of(Items.GOLDEN_CARROT),
				Ingredient.of(Items.GHAST_TEAR), Ingredient.of(Items.GLOWSTONE_DUST));
	}

	private static void make(RecipeOutput consumer, String id, Brew brew, Ingredient... inputs) {
		consumer.accept(idFor(id), new BotanicalBreweryRecipe(brew, inputs), null);
	}

	private static ResourceLocation idFor(String path) {
		return prefix("brew/" + path);
	}
}
