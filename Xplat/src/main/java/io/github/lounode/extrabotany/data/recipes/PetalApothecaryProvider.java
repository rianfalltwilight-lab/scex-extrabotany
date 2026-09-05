package io.github.lounode.extrabotany.data.recipes;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import vazkii.botania.common.crafting.BotaniaRecipeTypes;
import vazkii.botania.common.crafting.PetalApothecaryRecipe;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.lib.BotaniaTags;
import vazkii.botania.data.util.BotaniaRecipeHelper;

import io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

import java.util.concurrent.CompletableFuture;

public class PetalApothecaryProvider extends ExtraBotanyRecipeProvider {
	private static final Ingredient DEFAULT_REAGENT = Ingredient.of(BotaniaTags.Items.SEED_APOTHECARY_REAGENT);

	public PetalApothecaryProvider(PackOutput packOutput,
			CompletableFuture<HolderLookup.Provider> lookupProvider) {
		super(packOutput, lookupProvider);
	}

	@Override
	public void buildRecipes(RecipeOutput consumer) {
		Ingredient white = Ingredient.of(BotaniaTags.Items.PETALS_WHITE);
		Ingredient orange = Ingredient.of(BotaniaTags.Items.PETALS_ORANGE);
		Ingredient magenta = Ingredient.of(BotaniaTags.Items.PETALS_MAGENTA);
		Ingredient lightBlue = Ingredient.of(BotaniaTags.Items.PETALS_LIGHT_BLUE);
		Ingredient yellow = Ingredient.of(BotaniaTags.Items.PETALS_YELLOW);
		Ingredient lime = Ingredient.of(BotaniaTags.Items.PETALS_LIME);
		Ingredient pink = Ingredient.of(BotaniaTags.Items.PETALS_PINK);
		Ingredient gray = Ingredient.of(BotaniaTags.Items.PETALS_GRAY);
		Ingredient cyan = Ingredient.of(BotaniaTags.Items.PETALS_CYAN);
		Ingredient purple = Ingredient.of(BotaniaTags.Items.PETALS_PURPLE);
		Ingredient blue = Ingredient.of(BotaniaTags.Items.PETALS_BLUE);
		Ingredient brown = Ingredient.of(BotaniaTags.Items.PETALS_BROWN);
		Ingredient green = Ingredient.of(BotaniaTags.Items.PETALS_GREEN);
		Ingredient red = Ingredient.of(BotaniaTags.Items.PETALS_RED);
		Ingredient black = Ingredient.of(BotaniaTags.Items.PETALS_BLACK);
		Ingredient runeWater = Ingredient.of(BotaniaItems.RUNE_OF_WATER);
		Ingredient runeFire = Ingredient.of(BotaniaItems.RUNE_OF_FIRE);
		Ingredient runeEarth = Ingredient.of(BotaniaItems.RUNE_OF_EARTH);
		Ingredient runeAir = Ingredient.of(BotaniaItems.RUNE_OF_AIR);
		Ingredient runeSpring = Ingredient.of(BotaniaItems.RUNE_OF_SPRING);
		Ingredient runeSummer = Ingredient.of(BotaniaItems.RUNE_OF_SUMMER);
		Ingredient runeAutumn = Ingredient.of(BotaniaItems.RUNE_OF_AUTUMN);
		Ingredient runeWinter = Ingredient.of(BotaniaItems.RUNE_OF_WINTER);
		Ingredient runeMana = Ingredient.of(BotaniaItems.RUNE_OF_MANA);
		Ingredient runeLust = Ingredient.of(BotaniaItems.RUNE_OF_LUST);
		Ingredient runeGluttony = Ingredient.of(BotaniaItems.RUNE_OF_GLUTTONY);
		Ingredient runeGreed = Ingredient.of(BotaniaItems.RUNE_OF_GREED);
		Ingredient runeSloth = Ingredient.of(BotaniaItems.RUNE_OF_SLOTH);
		Ingredient runeWrath = Ingredient.of(BotaniaItems.RUNE_OF_WRATH);
		Ingredient runeEnvy = Ingredient.of(BotaniaItems.RUNE_OF_ENVY);
		Ingredient runePride = Ingredient.of(BotaniaItems.RUNE_OF_PRIDE);

		Ingredient redstoneRoot = Ingredient.of(BotaniaItems.REDSTONE_ROOT);
		Ingredient gaiaSpirit = Ingredient.of(BotaniaItems.GAIA_SPIRIT);
		Ingredient spiritFragment = Ingredient.of(ExtraBotanyItems.spiritFragment);
		Ingredient manaDust = Ingredient.of(BotaniaItems.MANA_POWDER);

		make(consumer, ExtrabotanyFlowerBlocks.tradeOrchid, lime, lime, green, brown, runeGreed, runeLust, redstoneRoot);
		make(consumer, ExtrabotanyFlowerBlocks.woodienia, brown, brown, brown, gray, Ingredient.of(ExtraBotanyItems.elementiumQuartz), runeGluttony, redstoneRoot);
		make(consumer, ExtrabotanyFlowerBlocks.reikarlily, lightBlue, lightBlue, cyan, cyan, blue, runePride, runeEnvy, runeSloth, gaiaSpirit);
		make(consumer, ExtrabotanyFlowerBlocks.bellflower, yellow, yellow, lime, lime, spiritFragment);
		make(consumer, ExtrabotanyFlowerBlocks.annoyingflower, white, white, pink, pink, green, runeMana, spiritFragment);
		make(consumer, ExtrabotanyFlowerBlocks.stonesia, gray, gray, black, gaiaSpirit, runeAutumn, runeGluttony);
		make(consumer, ExtrabotanyFlowerBlocks.edelweiss, white, white, white, lightBlue, lightBlue, manaDust, runeMana, runeWinter);
		make(consumer, ExtrabotanyFlowerBlocks.resoncund, magenta, magenta, orange, orange, runeLust, runeGluttony);
		make(consumer, ExtrabotanyFlowerBlocks.sunshineLily, yellow, yellow, yellow, orange);
		make(consumer, ExtrabotanyFlowerBlocks.moonlightLily, black, black, purple, gray);
		make(consumer, ExtrabotanyFlowerBlocks.serenitian, purple, purple, blue, blue, runeMana, runeSloth, runeGreed, gaiaSpirit, Ingredient.of(Items.WITHER_ROSE));
		make(consumer, ExtrabotanyFlowerBlocks.twinstar, yellow, yellow, yellow, orange, orange, orange, manaDust, manaDust);
		make(consumer, ExtrabotanyFlowerBlocks.omniviolet, purple, purple, blue, blue, runeSpring, runeMana, runeLust);
		make(consumer, ExtrabotanyFlowerBlocks.tinkle, yellow, yellow, green, lime, runeWater, runeEarth, manaDust, spiritFragment, spiritFragment);
		make(consumer, ExtrabotanyFlowerBlocks.bloodEnchantress, red, red, red, red, runeFire, runeSummer, runeWrath);
		make(consumer, ExtrabotanyFlowerBlocks.mirrowtunia, cyan, cyan, lightBlue, blue, runeWrath, runePride, runeAir, manaDust);
		make(consumer, ExtrabotanyFlowerBlocks.manalink, cyan, cyan, cyan, lightBlue, lightBlue, runeSloth, runeLust, gaiaSpirit);
		make(consumer, ExtrabotanyFlowerBlocks.necrofleur, gray, gray, pink, pink, red, runeWrath, manaDust);
		make(consumer, ExtrabotanyFlowerBlocks.enchanter, purple, purple, magenta, lime, lime, runePride, runeGreed, runeGluttony, gaiaSpirit);
	}

	private static void make(RecipeOutput consumer, ItemLike output, Ingredient... ingredients) {
		consumer.accept(BotaniaRecipeHelper.deriveRecipeId(BotaniaRecipeTypes.PETAL_APOTHECARY_TYPE, output),
				new PetalApothecaryRecipe(new ItemStack(output), DEFAULT_REAGENT, ingredients), null);
	}

	@Override
	public String getName() {
		return "Extrabotany petal apothecary recipes";
	}
}
