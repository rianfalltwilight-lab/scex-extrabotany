package io.github.lounode.extrabotany.common.crafting.recipe;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.crafting.recipe.WrappingRecipeSerializer;

import io.github.lounode.extrabotany.common.brew.BrewUtil;
import io.github.lounode.extrabotany.common.brew.ExtraBotanyBrews;
import io.github.lounode.extrabotany.common.item.brew.ManaCocktailItem;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class CopyBrewFormFlaskRecipe extends CopyBrewRecipe {

	public static final WrappingRecipeSerializer<CopyBrewFormFlaskRecipe> SERIALIZER = new CopyBrewFormFlaskRecipe.Serializer();

	public CopyBrewFormFlaskRecipe(ShapelessRecipe compose) {
		super(compose);
	}

	@Override
	public Item getBrewSource() {
		return BotaniaItems.BREW_FLASK;
	}

	@Override
	public @NotNull RecipeSerializer<?> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public @NotNull ItemStack assemble(@NotNull CraftingInput inv, @NotNull HolderLookup.Provider registries) {
		ItemStack result = getResultItem(registries).copy();
		ItemStack brewSource = ItemStack.EMPTY;
		ItemStack input = ItemStack.EMPTY;

		List<ItemStack> _inputs = new ArrayList<>();

		for (int i = 0; i < inv.size(); i++) {
			ItemStack _input = inv.getItem(i);
			if (_input.isEmpty()) {
				continue;
			}
			if (_input.is(getBrewSource())) {
				brewSource = _input;
			} else {
				input = _input;
			}

			_inputs.add(_input);
		}

		if (_inputs.size() != 2 || brewSource.isEmpty() || input.isEmpty()) {
			return ItemStack.EMPTY;
		}

		if (!(input.getItem() instanceof ManaCocktailItem cocktail)) {
			return ItemStack.EMPTY;
		}

		if (cocktail.getBrew(input) != ExtraBotanyBrews.manaCocktail || cocktail.getSwigsLeft(input) != 1) {
			return ItemStack.EMPTY;
		}

		BrewUtil.setBrew(result, BrewUtil.getBrew(brewSource));

		return result;
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingInput container) {
		NonNullList<ItemStack> nonnulllist = NonNullList.withSize(container.size(), ItemStack.EMPTY);

		for (int i = 0; i < nonnulllist.size(); ++i) {
			ItemStack stack = container.getItem(i);
			Item item = stack.getItem();
			if (stack.hasCraftingRemainingItem()) {
				nonnulllist.set(i, stack.getCraftingRemainingItem());
			} else if (item == BotaniaItems.BREW_FLASK) {
				nonnulllist.set(i, BotaniaItems.MANAGLASS_VIAL.getDefaultInstance());
			}
		}

		return nonnulllist;
	}

	private static class Serializer implements WrappingRecipeSerializer<CopyBrewFormFlaskRecipe> {
		private static final MapCodec<CopyBrewFormFlaskRecipe> CODEC = SHAPELESS_RECIPE.codec()
				.xmap(CopyBrewFormFlaskRecipe::new, Function.identity());
		private static final StreamCodec<RegistryFriendlyByteBuf, CopyBrewFormFlaskRecipe> STREAM_CODEC = SHAPELESS_RECIPE.streamCodec()
				.map(CopyBrewFormFlaskRecipe::new, Function.identity());

		private Serializer() {}

		@Override
		public CopyBrewFormFlaskRecipe wrap(Recipe<?> recipe) {
			if (!(recipe instanceof ShapelessRecipe shapelessRecipe)) {
				throw new IllegalArgumentException("Copy brew from flask must wrap a shapeless recipe");
			}
			return new CopyBrewFormFlaskRecipe(shapelessRecipe);
		}

		@Override
		public MapCodec<CopyBrewFormFlaskRecipe> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, CopyBrewFormFlaskRecipe> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
