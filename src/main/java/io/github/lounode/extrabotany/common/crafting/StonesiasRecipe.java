package io.github.lounode.extrabotany.common.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import vazkii.botania.api.recipe.StateIngredient;
import vazkii.botania.common.crafting.StateIngredients;

import io.github.lounode.extrabotany.api.recipe.StonesiaRecipe;

/**
 * 石中姬配方的实现：一种方块，和吃掉它产生的魔力。
 * The implementation of the Stonesia recipe: a block, and the mana that the flower makes when it eats that block.
 */
public class StonesiasRecipe implements StonesiaRecipe {
	private final StateIngredient input;
	private final int outputMana;

	public StonesiasRecipe(StateIngredient input, int outputMana) {
		this.input = input;
		this.outputMana = outputMana;
	}

	@Override
	public StateIngredient getInput() {
		return input;
	}

	@Override
	public int getManaOutput() {
		return outputMana;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return ExtraBotanyRecipeTypes.STONESIA_SERIALIZER;
	}

	@Override
	public RecipeType<? extends StonesiaRecipe> getType() {
		return ExtraBotanyRecipeTypes.STONESIA_RECIPE_TYPE;
	}

	public static class Serializer implements RecipeSerializer<StonesiasRecipe> {
		private static final MapCodec<StonesiasRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				StateIngredients.TYPED_CODEC.fieldOf("input").forGetter(StonesiasRecipe::getInput),
				Codec.INT.fieldOf("outputMana").forGetter(StonesiasRecipe::getManaOutput)
		).apply(instance, StonesiasRecipe::new));

		private static final StreamCodec<RegistryFriendlyByteBuf, StonesiasRecipe> STREAM_CODEC = StreamCodec.composite(
				StateIngredients.TYPED_STREAM_CODEC, StonesiasRecipe::getInput,
				ByteBufCodecs.VAR_INT, StonesiasRecipe::getManaOutput,
				StonesiasRecipe::new);

		@Override
		public MapCodec<StonesiasRecipe> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, StonesiasRecipe> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
