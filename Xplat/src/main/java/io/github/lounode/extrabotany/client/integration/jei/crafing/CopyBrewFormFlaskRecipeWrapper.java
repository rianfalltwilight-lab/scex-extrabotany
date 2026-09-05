package io.github.lounode.extrabotany.client.integration.jei.crafing;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.common.item.BotaniaItems;

import io.github.lounode.extrabotany.common.brew.BrewUtil;
import io.github.lounode.extrabotany.common.crafting.recipe.CopyBrewRecipe;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.item.brew.ManaCocktailItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CopyBrewFormFlaskRecipeWrapper implements ICraftingCategoryExtension<CopyBrewRecipe> {

	public CopyBrewFormFlaskRecipeWrapper(CopyBrewRecipe recipe) {
	}

	@Override
	public void setRecipe(RecipeHolder<CopyBrewRecipe> recipe, IRecipeLayoutBuilder builder,
			ICraftingGridHelper helper, IFocusGroup focusGroup) {
		ItemStack cocktail = focusGroup.getFocuses(VanillaTypes.ITEM_STACK, RecipeIngredientRole.INPUT)
				.filter(f -> f.getTypedValue().getIngredient().getItem() == ExtraBotanyItems.manaCocktail)
				.map(f -> f.getTypedValue().getIngredient())
				.findFirst().orElse(null);

		ItemStack flask = focusGroup.getFocuses(VanillaTypes.ITEM_STACK, RecipeIngredientRole.INPUT)
				.filter(f -> f.getTypedValue().getIngredient().getItem() == BotaniaItems.BREW_FLASK)
				.map(f -> f.getTypedValue().getIngredient())
				.findFirst().orElse(null);

		List<List<ItemStack>> inputs = new ArrayList<>();

		inputs.add(Collections.singletonList(
				cocktail != null ? cocktail.copy() : ManaCocktailItem.getDefaultCocktail()
		));

		List<ItemStack> flasks = new ArrayList<>();
		if (flask != null) {
			flasks.add(flask.copy());
		} else {

			BotaniaAPI.instance().getBrewRegistry().forEach(brew -> {
				ItemStack flaskStack = new ItemStack(BotaniaItems.BREW_FLASK);
				BrewUtil.setBrew(flaskStack, brew);
				flasks.add(flaskStack);
			});
		}
		inputs.add(flasks);

		helper.createAndSetInputs(builder, VanillaTypes.ITEM_STACK, inputs, 0, 0);

		ItemStack output = new ItemStack(ExtraBotanyItems.manaCocktail);
		if (flask != null) {
			BrewUtil.setBrew(output, BrewUtil.getBrew(flask));
		}

		helper.createAndSetOutputs(builder, VanillaTypes.ITEM_STACK, List.of(output));
	}
}
