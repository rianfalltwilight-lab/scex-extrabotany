package io.github.lounode.extrabotany.common.integration.kubejs.schema;

import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.IngredientComponent;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;

import net.minecraft.world.item.crafting.Ingredient;

/**
 * 全知瑾配方在 KubeJS 里的字段。脚本的写法：{@code omniviolet(input, burnTime)}。{@code input} 是物品或者物品标签。
 * The fields of the Omniviolet recipe in KubeJS. The form in a script: {@code omniviolet(input, burnTime)}. {@code input} is an item or an item tag.
 */
public interface OmnivioletSchema {
	RecipeKey<Ingredient> INPUT = IngredientComponent.INGREDIENT.inputKey("input");
	RecipeKey<Integer> BURN_TIME = NumberComponent.INT.otherKey("burnTime");

	RecipeSchema SCHEMA = new RecipeSchema(INPUT, BURN_TIME);
}
