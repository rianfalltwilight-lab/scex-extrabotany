package io.github.lounode.extrabotany.common.integration.kubejs.schema;

import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;

import vazkii.botania.api.recipe.StateIngredient;

import io.github.lounode.extrabotany.common.integration.kubejs.util.BlockStateComponent;

public interface StonesiaSchema {

	RecipeKey<StateIngredient> INPUT = BlockStateComponent.TYPE.inputKey("input");
	RecipeKey<Integer> OUTPUT_MANA = NumberComponent.INT.outputKey("outputMana");

	RecipeSchema SCHEMA = new RecipeSchema(OUTPUT_MANA, INPUT);
}
