package io.github.lounode.extrabotany.data.recipes;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import io.github.lounode.extrabotany.common.crafting.PedestalsRecipe;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.lib.ExtraBotanyTags;

import java.util.concurrent.CompletableFuture;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 生成祭坛锤击配方。
 * Makes these recipes. The pedestal hammer recipes.
 */
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
		// 对照原版审计 E5：1.12 ModPedestalRecipe 的五条配方。每条都敲 5 下（ConfigHandler.CRAFTING_STRIKES），给 1 点经验（TilePedestal）。
		// Parity audit E5: the five recipes of ModPedestalRecipe of 1.12. Each needs 5 strikes (ConfigHandler.CRAFTING_STRIKES) and gives 1 experience (TilePedestal).
		smash(consumer, "gilded_potato_mashed", ExtraBotanyItems.gildedPotatoMashed, ExtraBotanyItems.gildedPotato);
		smash(consumer, "spirit_fragment", ExtraBotanyItems.spiritFragment, ExtraBotanyItems.spiritFuel);
		smash(consumer, "gravel", Items.GRAVEL, Items.COBBLESTONE);
		smash(consumer, "flint", Items.FLINT, Items.GRAVEL);
		smash(consumer, "gunpowder", Items.GUNPOWDER, Items.FLINT);
	}

	private static void smash(RecipeOutput consumer, String name, ItemLike output, ItemLike input) {
		consumer.accept(id(name),
				new PedestalsRecipe(new ItemStack(output),
						Ingredient.of(ExtraBotanyTags.Items.HAMMERS),
						Ingredient.of(input), 5, 1), null);
	}

	private static ResourceLocation id(String id) {
		return prefix("pedestal_smash/" + id);
	}
}
