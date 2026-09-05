package io.github.lounode.extrabotany.data.recipes;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.recipe.StateIngredient;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.crafting.ManaInfusionRecipe;
import vazkii.botania.common.crafting.StateIngredients;

import io.github.lounode.extrabotany.common.block.ExtraBotanyBlocks;
import io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class ManaInfusionProvider extends ExtraBotanyRecipeProvider {
	public ManaInfusionProvider(PackOutput packOutput,
			CompletableFuture<HolderLookup.Provider> lookupProvider) {
		super(packOutput, lookupProvider);
	}

	@Override
	public String getName() {
		return "ExtraBotany mana pool recipes";
	}

	@Override
	public void buildRecipes(RecipeOutput recipeOutput) {
		Consumer<FinishedRecipe> consumer = recipe -> recipeOutput.accept(recipe.id,
				new ManaInfusionRecipe(recipe.output, recipe.input, recipe.mana,
						recipe.group.isEmpty() ? null : recipe.group, recipe.catalyst), null);
		consumer.accept(new FinishedRecipe(id("nightmare_fuel"), new ItemStack(ExtraBotanyItems.nightmareFuel), Ingredient.of(Items.COAL), 2000));
		consumer.accept(new FinishedRecipe(id("fried_chicken"), new ItemStack(ExtraBotanyItems.friedChicken), Ingredient.of(Items.COOKED_CHICKEN), 600));
		//Dimension
		consumer.accept(FinishedRecipe.dimension(id("snowball_to_ender_pearl"), new ItemStack(Items.ENDER_PEARL), ingr(Items.SNOWBALL), 2000));
		consumer.accept(FinishedRecipe.dimension(id("diamond_horse_armor_to_shulker_shell"), new ItemStack(Items.SHULKER_SHELL), ingr(Items.DIAMOND_HORSE_ARMOR), 20000));
		consumer.accept(FinishedRecipe.dimension(id("apple_to_chorus_fruit"), new ItemStack(Items.CHORUS_FRUIT), ingr(Items.APPLE), 500));
		consumer.accept(FinishedRecipe.dimension(id("stone_to_end_stone"), new ItemStack(Items.END_STONE), ingr(Items.STONE), 500));
		consumer.accept(FinishedRecipe.dimension(id("cobblestone_to_nether_rack"), new ItemStack(Items.NETHERRACK), ingr(Items.COBBLESTONE), 500));
		consumer.accept(FinishedRecipe.dimension(id("sand_to_soul_sand"), new ItemStack(Items.SOUL_SAND), ingr(Items.SAND), 500));
		consumer.accept(FinishedRecipe.dimension(id("iron_ore_to_quartz_ore"), new ItemStack(Items.NETHER_QUARTZ_ORE), ingr(Items.IRON_ORE), 2000));
		consumer.accept(FinishedRecipe.dimension(id("blaze_rod_dupe"), new ItemStack(Items.BLAZE_ROD, 2), ingr(Items.BLAZE_ROD), 2000));
		consumer.accept(FinishedRecipe.dimension(id("nether_star_to_totem_of_undying"), new ItemStack(Items.TOTEM_OF_UNDYING), ingr(Items.NETHER_STAR), 50000));
		consumer.accept(FinishedRecipe.dimension(id("the_origin_to_elytra"), new ItemStack(Items.ELYTRA), ingr(ExtraBotanyItems.theOrigin), 50000));
		//Mini
		consumer.accept(mini(ExtrabotanyFlowerBlocks.necrofleurChibi, ExtrabotanyFlowerBlocks.necrofleur));
	}

	protected void cycle(Consumer<FinishedRecipe> consumer, int cost, String group, ItemLike... items) {
		for (int i = 0; i < items.length; i++) {
			Ingredient in = ingr(items[i]);
			ItemStack out = new ItemStack(i == items.length - 1 ? items[0] : items[i + 1]);
			String id = String.format("%s_to_%s", BuiltInRegistries.ITEM.getKey(items[i].asItem()).getPath(), BuiltInRegistries.ITEM.getKey(out.getItem()).getPath());
			consumer.accept(FinishedRecipe.alchemy(id(id), out, in, cost, group));
		}
	}

	protected FinishedRecipe mini(ItemLike mini, ItemLike full) {
		return FinishedRecipe.alchemy(id(BuiltInRegistries.ITEM.getKey(mini.asItem()).getPath()), new ItemStack(mini), ingr(full), 2500, "botania:flower_shrinking");
	}

	protected FinishedRecipe deconstruct(String id, ItemLike items, ItemLike block) {
		return FinishedRecipe.alchemy(id(id), new ItemStack(items, 4), ingr(block), 25, "botania:block_deconstruction");
	}

	protected ResourceLocation id(String s) {
		return prefix("mana_infusion/" + s);
	}

	protected static Ingredient ingr(ItemLike i) {
		return Ingredient.of(i);
	}

	protected static class FinishedRecipe {
		private static final StateIngredient CONJURATION = StateIngredients.of(BotaniaBlocks.CONJURATION_CATALYST);
		private static final StateIngredient ALCHEMY = StateIngredients.of(BotaniaBlocks.ALCHEMY_CATALYST);

		private static final StateIngredient DIMENSION = StateIngredients.of(ExtraBotanyBlocks.dimensionCatalyst);

		private final ResourceLocation id;
		private final Ingredient input;
		private final ItemStack output;
		private final int mana;
		private final String group;
		@Nullable
		private final StateIngredient catalyst;

		public static FinishedRecipe conjuration(ResourceLocation id, ItemStack output, Ingredient input, int mana) {
			return new FinishedRecipe(id, output, input, mana, "", CONJURATION);
		}

		public static FinishedRecipe dimension(ResourceLocation id, ItemStack output, Ingredient input, int mana) {
			return new FinishedRecipe(id, output, input, mana, "", DIMENSION);
		}

		public static FinishedRecipe alchemy(ResourceLocation id, ItemStack output, Ingredient input, int mana) {
			return alchemy(id, output, input, mana, "");
		}

		public static FinishedRecipe alchemy(ResourceLocation id, ItemStack output, Ingredient input, int mana, String group) {
			return new FinishedRecipe(id, output, input, mana, group, ALCHEMY);
		}

		public FinishedRecipe(ResourceLocation id, ItemStack output, Ingredient input, int mana) {
			this(id, output, input, mana, "");
		}

		public FinishedRecipe(ResourceLocation id, ItemStack output, Ingredient input, int mana, String group) {
			this(id, output, input, mana, group, null);
		}

		public FinishedRecipe(ResourceLocation id, ItemStack output, Ingredient input, int mana, String group, @Nullable StateIngredient catalyst) {
			this.id = id;
			this.input = input;
			this.output = output;
			this.mana = mana;
			this.group = group;
			this.catalyst = catalyst;
		}

	}
}
