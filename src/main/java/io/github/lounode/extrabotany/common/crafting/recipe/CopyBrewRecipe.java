package io.github.lounode.extrabotany.common.crafting.recipe;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;

import org.jetbrains.annotations.NotNull;

/**
 * 把一个物品里的酿造复制到另一个物品里的无序配方的基类。
 * The base class of the shapeless recipes that copy the brew of one item into a different item.
 */
public abstract class CopyBrewRecipe extends ShapelessRecipe {

	public CopyBrewRecipe(ShapelessRecipe compose) {
		super(compose.getGroup(), compose.category(), compose.getResultItem(RegistryAccess.EMPTY), compose.getIngredients());
	}

	public abstract Item getBrewSource();
	@Override
	public abstract @NotNull RecipeSerializer<?> getSerializer();
}
