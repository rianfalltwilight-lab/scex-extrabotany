package io.github.lounode.extrabotany.data.recipes;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;

import vazkii.botania.data.recipes.BotaniaRecipeProvider;

import java.util.concurrent.CompletableFuture;

/**
 * Shared recipe-provider base using Minecraft 1.21's registry-aware recipe output.
 */
public abstract class ExtraBotanyRecipeProvider extends BotaniaRecipeProvider {
	protected ExtraBotanyRecipeProvider(PackOutput packOutput,
			CompletableFuture<HolderLookup.Provider> lookupProvider) {
		super(packOutput, lookupProvider);
	}
}
