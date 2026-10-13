package io.github.lounode.extrabotany.data.recipes;

import net.minecraft.advancements.critereon.EntityTypePredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import io.github.lounode.extrabotany.common.crafting.EdelweissRecipes;

import java.util.concurrent.CompletableFuture;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 生成雪绒花的配方：哪种实体产生多少魔力。
 * Makes these recipes. The recipes of the Edelweiss: which entity type gives how much mana.
 */
public class EdelweissRecipeProvider extends ExtraBotanyRecipeProvider {
	public EdelweissRecipeProvider(PackOutput packOutput,
			CompletableFuture<HolderLookup.Provider> lookupProvider) {
		super(packOutput, lookupProvider);
	}

	@Override
	public void buildRecipes(RecipeOutput consumer) {
		consumer.accept(id("snow_golem"),
				new EdelweissRecipes(EntityTypePredicate.of(EntityType.SNOW_GOLEM), 3200), null);
	}

	private static ResourceLocation id(String id) {
		return prefix("edelweiss/" + id);
	}

	@Override
	public String getName() {
		return "Extrabotany Edelweiss recipes";
	}
}
