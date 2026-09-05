package io.github.lounode.extrabotany.common.integration.kubejs.schema;

import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.IngredientComponent;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;

import net.minecraft.world.item.crafting.Ingredient;

public interface OmnivioletSchema {
	RecipeKey<Ingredient> INPUT = IngredientComponent.INGREDIENT.inputKey("input");
	RecipeKey<Integer> BURN_TIME = NumberComponent.INT.otherKey("burnTime");

	RecipeSchema SCHEMA = new RecipeSchema(INPUT, BURN_TIME);
}
