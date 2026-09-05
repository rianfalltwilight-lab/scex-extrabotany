package io.github.lounode.extrabotany.data.recipes;

import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.world.item.crafting.Ingredient;

import io.github.lounode.extrabotany.common.block.ExtraBotanyBlocks;
import vazkii.botania.mixin.RecipeProviderAccessor;

import java.util.concurrent.CompletableFuture;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;
public class SmeltingProvider extends ExtraBotanyRecipeProvider {
	public SmeltingProvider(PackOutput packOutput,
			CompletableFuture<HolderLookup.Provider> lookupProvider) {
		super(packOutput, lookupProvider);
	}

	@Override
	public void buildRecipes(RecipeOutput consumer) {
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(ExtraBotanyBlocks.gaiaQuartzBlock), RecipeCategory.BUILDING_BLOCKS,
				ExtraBotanyBlocks.smoothGaiaQuartz, 0.1f, 200)
				.unlockedBy("has_item", conditionsFromItem(ExtraBotanyBlocks.gaiaQuartzBlock))
				.save(consumer, id("smooth_gaia_quartz"));
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(ExtraBotanyBlocks.elementiumQuartzBlock), RecipeCategory.BUILDING_BLOCKS,
				ExtraBotanyBlocks.smoothElementiumQuartz, 0.1f, 200)
				.unlockedBy("has_item", conditionsFromItem(ExtraBotanyBlocks.elementiumQuartzBlock))
				.save(consumer, id("smooth_elementium_quartz"));
	}

	private static Criterion<InventoryChangeTrigger.TriggerInstance> conditionsFromItem(net.minecraft.world.level.ItemLike item) {
		return RecipeProviderAccessor.botania_inventoryTrigger(ItemPredicate.Builder.item().of(item));
	}

	protected static String id(String id) {
		return prefix("smelting/" + id).toString();
	}

	@Override
	public String getName() {
		return "Extrabotany smelting recipes";
	}
}
