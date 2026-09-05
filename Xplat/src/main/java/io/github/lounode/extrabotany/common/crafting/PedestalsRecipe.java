package io.github.lounode.extrabotany.common.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import io.github.lounode.extrabotany.api.recipe.PedestalRecipe;

public class PedestalsRecipe implements PedestalRecipe {
	private final ItemStack output;
	private final Ingredient smashTools;
	private final Ingredient input;
	private final int strike;
	private final int exp;

	public PedestalsRecipe(ItemStack output, Ingredient smashTools, Ingredient input, int strike, int exp) {
		this.output = output;
		this.input = input;
		this.smashTools = smashTools;
		this.strike = strike;
		this.exp = exp;
	}

	@Override
	public boolean matches(RecipeInput input, Level level) {
		return input.size() > 0 && this.input.test(input.getItem(0));
	}

	@Override
	public ItemStack assemble(RecipeInput input, HolderLookup.Provider registries) {
		return output.copy();
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return false;
	}

	@Override
	public ItemStack getResultItem(HolderLookup.Provider registries) {
		return output;
	}

	@Override
	public Ingredient getSmashTools() {
		return smashTools;
	}

	@Override
	public Ingredient getInput() {
		return input;
	}

	@Override
	public ItemStack getOutput() {
		return output;
	}

	@Override
	public int getStrike() {
		return strike;
	}

	@Override
	public int getExp() {
		return exp;
	}

	@Override
	public RecipeType<? extends PedestalRecipe> getType() {
		return ExtraBotanyRecipeTypes.PEDESTAL_SMASH_TYPE;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return ExtraBotanyRecipeTypes.PEDESTAL_SMASH_SERIALIZER;
	}

	public static class Serializer implements RecipeSerializer<PedestalsRecipe> {
		private static final MapCodec<PedestalsRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				ItemStack.STRICT_CODEC.fieldOf("output").forGetter(PedestalsRecipe::getOutput),
				Ingredient.CODEC_NONEMPTY.fieldOf("smash_tools").forGetter(PedestalsRecipe::getSmashTools),
				Ingredient.CODEC_NONEMPTY.fieldOf("input").forGetter(PedestalsRecipe::getInput),
				Codec.INT.fieldOf("strike").forGetter(PedestalsRecipe::getStrike),
				Codec.INT.fieldOf("exp").forGetter(PedestalsRecipe::getExp)
		).apply(instance, PedestalsRecipe::new));

		private static final StreamCodec<RegistryFriendlyByteBuf, PedestalsRecipe> STREAM_CODEC = StreamCodec.composite(
				ItemStack.STREAM_CODEC, PedestalsRecipe::getOutput,
				Ingredient.CONTENTS_STREAM_CODEC, PedestalsRecipe::getSmashTools,
				Ingredient.CONTENTS_STREAM_CODEC, PedestalsRecipe::getInput,
				ByteBufCodecs.VAR_INT, PedestalsRecipe::getStrike,
				ByteBufCodecs.VAR_INT, PedestalsRecipe::getExp,
				PedestalsRecipe::new);

		@Override
		public MapCodec<PedestalsRecipe> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, PedestalsRecipe> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
