package io.github.lounode.extrabotany.common.integration.kubejs.schema;

import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.IngredientComponent;
import dev.latvian.mods.kubejs.recipe.component.ItemStackComponent;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import io.github.lounode.extrabotany.common.lib.ExtraBotanyTags;

/**
 * 祭坛锤击配方在 KubeJS 里的字段。脚本的写法：{@code pedestal_smash(output, input, [smash_tools], [exp], [strike])}。
 * The fields of the pedestal hammer recipe in KubeJS. The form in a script: {@code pedestal_smash(output, input, [smash_tools], [exp], [strike])}.
 *
 * <p>后三个参数可以省略：工具默认是 {@code #extrabotany:hammers} 标签，经验和敲击次数默认都是 5。
 * <p>The last three parameters are optional: the default tools are the tag {@code #extrabotany:hammers}, and the default experience and the default number of hits are 5.
 */
public interface NaturePedestalSchema {

	RecipeKey<ItemStack> RESULT = ItemStackComponent.ITEM_STACK.outputKey("output");
	RecipeKey<Ingredient> INPUT = IngredientComponent.INGREDIENT.inputKey("input");
	RecipeKey<Ingredient> SMASH_TOOLS = IngredientComponent.INGREDIENT.inputKey("smash_tools")
			.optional(Ingredient.of(ExtraBotanyTags.Items.HAMMERS)).alwaysWrite();
	RecipeKey<Integer> EXP = NumberComponent.INT.otherKey("exp").optional(5).alwaysWrite();
	RecipeKey<Integer> STRIKE = NumberComponent.INT.otherKey("strike").optional(5).alwaysWrite();

	RecipeSchema SCHEMA = new RecipeSchema(RESULT, INPUT, SMASH_TOOLS, EXP, STRIKE);
}
