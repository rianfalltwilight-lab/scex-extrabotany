package io.github.lounode.extrabotany.common.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import io.github.lounode.extrabotany.api.recipe.OmnivioletRecipe;

public class OmniVioletsRecipe implements OmnivioletRecipe {
	private final Ingredient input;
	private final int burnTime;

	public OmniVioletsRecipe(Ingredient input, int burnTime) {
		this.input = input;
		this.burnTime = burnTime;
	}

	@Override
	public Ingredient getInput() {
		return input;
	}

	@Override
	public int getBurnTime() {
		return burnTime;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return ExtraBotanyRecipeTypes.OMNIVIOLET_SERIALIZER;
	}

	@Override
	public RecipeType<? extends OmnivioletRecipe> getType() {
		return ExtraBotanyRecipeTypes.OMNIVIOLET_RECIPE_TYPE;
	}

	public static class Serializer implements RecipeSerializer<OmniVioletsRecipe> {
		private static final MapCodec<OmniVioletsRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Ingredient.CODEC_NONEMPTY.fieldOf("input").forGetter(OmniVioletsRecipe::getInput),
				Codec.INT.fieldOf("burnTime").forGetter(OmniVioletsRecipe::getBurnTime)
		).apply(instance, OmniVioletsRecipe::new));

		private static final StreamCodec<RegistryFriendlyByteBuf, OmniVioletsRecipe> STREAM_CODEC = StreamCodec.composite(
				Ingredient.CONTENTS_STREAM_CODEC, OmniVioletsRecipe::getInput,
				ByteBufCodecs.VAR_INT, OmniVioletsRecipe::getBurnTime,
				OmniVioletsRecipe::new);

		@Override
		public MapCodec<OmniVioletsRecipe> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, OmniVioletsRecipe> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
