package io.github.lounode.extrabotany.common.integration.kubejs.schema;

import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;

import vazkii.botania.api.recipe.StateIngredient;

import io.github.lounode.extrabotany.common.integration.kubejs.util.BlockStateComponent;

/**
 * 石中姬配方在 KubeJS 里的字段。脚本的写法：{@code stonesia(outputMana, input)}。{@code input} 是方块、方块状态或者方块标签。
 * The fields of the Stonesia recipe in KubeJS. The form in a script: {@code stonesia(outputMana, input)}. {@code input} is a block, a block state or a block tag.
 */
public interface StonesiaSchema {

	RecipeKey<StateIngredient> INPUT = BlockStateComponent.TYPE.inputKey("input");
	RecipeKey<Integer> OUTPUT_MANA = NumberComponent.INT.outputKey("outputMana");

	RecipeSchema SCHEMA = new RecipeSchema(OUTPUT_MANA, INPUT);
}
