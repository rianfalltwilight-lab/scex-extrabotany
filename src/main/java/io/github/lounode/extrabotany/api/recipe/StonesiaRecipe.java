package io.github.lounode.extrabotany.api.recipe;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.api.recipe.StateIngredient;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 石中姬配方：一种方块，和石中姬吃掉它产生的魔力。
 * A Stonesia recipe: a block, and the mana that the Stonesia makes when it eats that block.
 */
public interface StonesiaRecipe extends Recipe<RecipeInput>, ManaOutputRecipe {
	ResourceLocation TYPE_ID = prefix("stonesia");

	/** Valid inputs for the recipe */
	StateIngredient getInput();

	@Override
	RecipeType<? extends StonesiaRecipe> getType();

	default int getManaOutput(@NotNull Level level, @NotNull BlockPos pos) {
		return getManaOutput();
	}

	@Override
	default boolean matches(RecipeInput c, Level l) {
		return false;
	}

	@Override
	default ItemStack assemble(RecipeInput c, @NotNull HolderLookup.Provider registries) {
		return ItemStack.EMPTY;
	}

	@Override
	default boolean canCraftInDimensions(int width, int height) {
		return false;
	}

	@Override
	default ItemStack getResultItem(@NotNull HolderLookup.Provider registries) {
		return ItemStack.EMPTY;
	}

	@Override
	default boolean isSpecial() {
		return true;
	}
}
