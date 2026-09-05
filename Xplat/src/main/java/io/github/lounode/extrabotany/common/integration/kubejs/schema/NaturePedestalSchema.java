package io.github.lounode.extrabotany.common.integration.kubejs.schema;

import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.IngredientComponent;
import dev.latvian.mods.kubejs.recipe.component.ItemStackComponent;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import io.github.lounode.extrabotany.common.lib.ExtraBotanyTags;

public interface NaturePedestalSchema {

	RecipeKey<ItemStack> RESULT = ItemStackComponent.ITEM_STACK.outputKey("output");
	RecipeKey<Ingredient> INPUT = IngredientComponent.INGREDIENT.inputKey("input");
	RecipeKey<Ingredient> SMASH_TOOLS = IngredientComponent.INGREDIENT.inputKey("smash_tools")
			.optional(Ingredient.of(ExtraBotanyTags.Items.HAMMERS)).alwaysWrite();
	RecipeKey<Integer> EXP = NumberComponent.INT.otherKey("exp").optional(5).alwaysWrite();
	RecipeKey<Integer> STRIKE = NumberComponent.INT.otherKey("strike").optional(5).alwaysWrite();

	RecipeSchema SCHEMA = new RecipeSchema(RESULT, INPUT, SMASH_TOOLS, EXP, STRIKE);
}
