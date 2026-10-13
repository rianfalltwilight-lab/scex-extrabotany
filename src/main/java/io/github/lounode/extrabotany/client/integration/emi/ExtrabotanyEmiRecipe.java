package io.github.lounode.extrabotany.client.integration.emi;

import dev.emi.emi.api.recipe.EmiRecipeCategory;

import net.minecraft.world.item.crafting.RecipeHolder;

import vazkii.botania.client.integration.emi.BotaniaEmiRecipe;

/**
 * 本模组 EMI 配方的基类。
 * The base class of the EMI recipes of this mod.
 */
public abstract class ExtrabotanyEmiRecipe extends BotaniaEmiRecipe {
	public ExtrabotanyEmiRecipe(EmiRecipeCategory category, RecipeHolder<?> recipe) {
		super(category, recipe);
	}
}
