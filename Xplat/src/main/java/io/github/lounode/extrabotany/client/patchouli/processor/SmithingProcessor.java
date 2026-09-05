package io.github.lounode.extrabotany.client.patchouli.processor;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import vazkii.botania.client.patchouli.PatchouliUtils;
import vazkii.patchouli.api.IComponentProcessor;
import vazkii.patchouli.api.IVariable;
import vazkii.patchouli.api.IVariableProvider;
import vazkii.patchouli.mixin.AccessorSmithingTransformRecipe;
import vazkii.patchouli.mixin.AccessorSmithingTrimRecipe;

import java.util.List;

public class SmithingProcessor implements IComponentProcessor {
	SmithingRecipe recipe;

	@Override
	public void setup(Level level, IVariableProvider variables) {
		ResourceLocation id = ResourceLocation.tryParse(variables.get("recipe", level.registryAccess()).asString());
		this.recipe = PatchouliUtils.getRecipe(level, RecipeType.SMITHING, id);
	}

	@Override
	public IVariable process(Level level, String key) {
		if (recipe == null) {
			return null;
		}
		return switch (key) {
			case "base" -> PatchouliUtils.interweaveIngredients(List.of(getBase(recipe)), level);
			case "template" -> PatchouliUtils.interweaveIngredients(List.of(getTemplate(recipe)), level);
			case "addition" -> PatchouliUtils.interweaveIngredients(List.of(getAddition(recipe)), level);
			case "output" -> IVariable.from(getRecipeOutput(level, recipe), level.registryAccess());
			case "symbol" -> IVariable.from(recipe.getToastSymbol(), level.registryAccess());
			case "heading" -> IVariable.from(getRecipeOutput(level, recipe).getHoverName(), level.registryAccess());
			default -> null;
		};
	}

	private Ingredient getBase(SmithingRecipe recipe) {
		if (recipe instanceof SmithingTrimRecipe) {
			return ((AccessorSmithingTrimRecipe) recipe).getBase();
		}
		if (recipe instanceof SmithingTransformRecipe) {
			return ((AccessorSmithingTransformRecipe) recipe).getBase();
		}
		return Ingredient.EMPTY;
	}

	private Ingredient getAddition(SmithingRecipe recipe) {
		if (recipe instanceof SmithingTrimRecipe) {
			return ((AccessorSmithingTrimRecipe) recipe).getAddition();
		}
		if (recipe instanceof SmithingTransformRecipe) {
			return ((AccessorSmithingTransformRecipe) recipe).getAddition();
		}
		return Ingredient.EMPTY;
	}

	private Ingredient getTemplate(SmithingRecipe recipe) {
		if (recipe instanceof SmithingTrimRecipe) {
			return ((AccessorSmithingTrimRecipe) recipe).getTemplate();
		}
		if (recipe instanceof SmithingTransformRecipe) {
			return ((AccessorSmithingTransformRecipe) recipe).getTemplate();
		}
		return Ingredient.EMPTY;
	}

	protected ItemStack getRecipeOutput(Level level, SmithingRecipe recipe) {
		if (recipe == null || level == null) {
			return ItemStack.EMPTY;
		}

		return recipe.getResultItem(level.registryAccess());
	}
}
