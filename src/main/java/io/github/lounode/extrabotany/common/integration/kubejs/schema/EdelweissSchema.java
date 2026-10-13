package io.github.lounode.extrabotany.common.integration.kubejs.schema;

import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.component.StringComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;

/**
 * 雪绒花配方在 KubeJS 里的字段。脚本的写法：{@code edelweiss(outputMana, input)}。{@code input} 是实体类型的 ID，或者以 {@code #} 开头的实体类型标签。
 * The fields of the Edelweiss recipe in KubeJS. The form in a script: {@code edelweiss(outputMana, input)}. {@code input} is the ID of an entity type, or an entity type tag that starts with {@code #}.
 */
public interface EdelweissSchema {

	RecipeKey<String> INPUT = StringComponent.STRING.inputKey("input");
	RecipeKey<Integer> OUTPUT_MANA = NumberComponent.INT.outputKey("outputMana");

	RecipeSchema SCHEMA = new RecipeSchema(OUTPUT_MANA, INPUT);
}
