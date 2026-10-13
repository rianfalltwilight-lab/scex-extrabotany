package io.github.lounode.extrabotany.api.recipe;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;

import org.jetbrains.annotations.NotNull;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 祭坛的锤击配方：输入物品、可以使用的锤子、敲击次数、产物和经验。
 * A hammer recipe of the pedestal: the input item, the hammers that can be used, the number of hits, the product and the experience.
 */
public interface PedestalRecipe extends Recipe<RecipeInput> {
	ResourceLocation TYPE_ID = prefix("pedestal_smash");

	@NotNull
	@Override
	default RecipeType<?> getType() {
		return BuiltInRegistries.RECIPE_TYPE.get(TYPE_ID);
	}

	Ingredient getSmashTools();
	Ingredient getInput();
	ItemStack getOutput();
	int getStrike();
	int getExp();

}
