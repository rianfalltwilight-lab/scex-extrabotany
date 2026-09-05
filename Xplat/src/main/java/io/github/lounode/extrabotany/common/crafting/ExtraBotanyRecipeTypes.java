package io.github.lounode.extrabotany.common.crafting;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import io.github.lounode.extrabotany.api.recipe.EdelweissRecipe;
import io.github.lounode.extrabotany.api.recipe.OmnivioletRecipe;
import io.github.lounode.extrabotany.api.recipe.PedestalRecipe;
import io.github.lounode.extrabotany.api.recipe.StonesiaRecipe;

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class ExtraBotanyRecipeTypes {
	public static final RecipeType<PedestalRecipe> PEDESTAL_SMASH_TYPE = new ModRecipeType<>();
	public static final RecipeSerializer<PedestalsRecipe> PEDESTAL_SMASH_SERIALIZER = new PedestalsRecipe.Serializer();
	public static final RecipeType<StonesiaRecipe> STONESIA_RECIPE_TYPE = new ModRecipeType<>();
	public static final RecipeSerializer<StonesiasRecipe> STONESIA_SERIALIZER = new StonesiasRecipe.Serializer();
	public static final RecipeType<EdelweissRecipe> EDELWEISS_RECIPE_TYPE = new ModRecipeType<>();
	public static final RecipeSerializer<EdelweissRecipes> EDELWEISS_SERIALIZER = new EdelweissRecipes.Serializer();
	public static final RecipeType<OmnivioletRecipe> OMNIVIOLET_RECIPE_TYPE = new ModRecipeType<>();
	public static final RecipeSerializer<OmniVioletsRecipe> OMNIVIOLET_SERIALIZER = new OmniVioletsRecipe.Serializer();

	public static void submitRecipeTypes(BiConsumer<RecipeType<?>, ResourceLocation> r) {
		r.accept(PEDESTAL_SMASH_TYPE, PedestalsRecipe.TYPE_ID);
		r.accept(STONESIA_RECIPE_TYPE, StonesiaRecipe.TYPE_ID);
		r.accept(EDELWEISS_RECIPE_TYPE, EdelweissRecipe.TYPE_ID);
		r.accept(OMNIVIOLET_RECIPE_TYPE, OmnivioletRecipe.TYPE_ID);
	}

	public static void submitRecipeSerializers(BiConsumer<RecipeSerializer<?>, ResourceLocation> r) {
		r.accept(PEDESTAL_SMASH_SERIALIZER, PedestalsRecipe.TYPE_ID);
		r.accept(STONESIA_SERIALIZER, StonesiaRecipe.TYPE_ID);
		r.accept(EDELWEISS_SERIALIZER, EdelweissRecipe.TYPE_ID);
		r.accept(OMNIVIOLET_SERIALIZER, OmnivioletRecipe.TYPE_ID);
	}

	private static class ModRecipeType<T extends Recipe<?>> implements RecipeType<T> {
		@Override
		public String toString() {
			return BuiltInRegistries.RECIPE_TYPE.getKey(this).toString();
		}
	}

	public static <I extends RecipeInput, T extends Recipe<I>> Map<ResourceLocation, T> getRecipes(Level world, RecipeType<T> type) {
		return world.getRecipeManager().getAllRecipesFor(type).stream().collect(Collectors.toMap(
				RecipeHolder::id, RecipeHolder::value, (first, ignored) -> first, LinkedHashMap::new));
	}
}
