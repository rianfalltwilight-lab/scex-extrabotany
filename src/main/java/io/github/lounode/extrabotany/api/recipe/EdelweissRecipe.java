package io.github.lounode.extrabotany.api.recipe;

import net.minecraft.advancements.critereon.EntityTypePredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 雪绒花配方：一种实体，和雪绒花吃掉它产生的魔力。
 * An Edelweiss recipe: an entity type, and the mana that the Edelweiss makes when it eats that entity.
 */
public interface EdelweissRecipe extends Recipe<RecipeInput>, ManaOutputRecipe {
	ResourceLocation TYPE_ID = prefix("edelweiss");

	EntityTypePredicate getInput();

	@Override
	RecipeType<? extends EdelweissRecipe> getType();

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
