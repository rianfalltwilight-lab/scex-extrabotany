package io.github.lounode.extrabotany.common.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.critereon.EntityTypePredicate;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import io.github.lounode.extrabotany.api.recipe.EdelweissRecipe;

public class EdelweissRecipes implements EdelweissRecipe {
	private final EntityTypePredicate input;
	private final int outputMana;

	public EdelweissRecipes(EntityTypePredicate input, int outputMana) {
		this.input = input;
		this.outputMana = outputMana;
	}

	@Override
	public EntityTypePredicate getInput() {
		return input;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return ExtraBotanyRecipeTypes.EDELWEISS_SERIALIZER;
	}

	@Override
	public RecipeType<? extends EdelweissRecipe> getType() {
		return ExtraBotanyRecipeTypes.EDELWEISS_RECIPE_TYPE;
	}

	@Override
	public int getManaOutput() {
		return outputMana;
	}

	public static class Serializer implements RecipeSerializer<EdelweissRecipes> {
		private static final MapCodec<EdelweissRecipes> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				EntityTypePredicate.CODEC.fieldOf("input").forGetter(EdelweissRecipes::getInput),
				Codec.INT.fieldOf("outputMana").forGetter(EdelweissRecipes::getManaOutput)
		).apply(instance, EdelweissRecipes::new));

		private static final StreamCodec<RegistryFriendlyByteBuf, EdelweissRecipes> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.fromCodecWithRegistries(EntityTypePredicate.CODEC), EdelweissRecipes::getInput,
				ByteBufCodecs.VAR_INT, EdelweissRecipes::getManaOutput,
				EdelweissRecipes::new);

		@Override
		public MapCodec<EdelweissRecipes> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, EdelweissRecipes> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
