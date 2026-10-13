package io.github.lounode.extrabotany.forge.gametest;

import io.github.lounode.extrabotany.common.brew.BrewUtil;
import io.github.lounode.extrabotany.common.brew.ExtraBotanyBrews;
import io.github.lounode.extrabotany.common.crafting.recipe.PotionLensChangePotionRecipe;
import io.github.lounode.extrabotany.common.entity.HolyWaterGrenadeEntity;
import io.github.lounode.extrabotany.common.helper.ItemNBTHelper;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.item.brew.HolyWaterGrenadeItem;
import io.github.lounode.extrabotany.common.item.brew.InfiniteWineItem;
import io.github.lounode.extrabotany.common.item.brew.ManaCocktailItem;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.common.brew.BotaniaBrews;
import vazkii.botania.common.component.BotaniaDataComponents;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.item.brew.BaseBrewItem;

import java.util.List;

@GameTestHolder("extrabotany")
@PrefixGameTestTemplate(false)
public final class BrewRecipeGameTests {

	private BrewRecipeGameTests() {}

	private static ItemStack craft(GameTestHelper helper, ItemStack first, ItemStack second) {
		ItemStack beforeFirst = first.copy();
		ItemStack beforeSecond = second.copy();
		var input = CraftingInput.of(2, 1, List.of(first, second));
		var recipe = helper.getLevel().getRecipeManager()
				.getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel()).orElseThrow().value();
		ItemStack result = recipe.assemble(input, helper.getLevel().registryAccess());
		helper.assertTrue(!result.isEmpty() && result.getCount() == 1, "Brew recipe produced no result or duplicated its output");
		helper.assertTrue(ItemStack.matches(first, beforeFirst) && ItemStack.matches(second, beforeSecond),
				"Brew recipe assembly modified its input stacks");
		return result;
	}

	private static ItemStack saveAndLoad(GameTestHelper helper, ItemStack stack) {
		return ItemStack.parseOptional(helper.getLevel().registryAccess(),
				(CompoundTag) stack.save(helper.getLevel().registryAccess()));
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void flaskBrewSurvivesCraftSaveAndActualDrink(GameTestHelper helper) {
		ItemStack flask = new ItemStack(BotaniaItems.BREW_FLASK);
		BaseBrewItem.setBrew(flask, ExtraBotanyBrews.revolution);
		ItemStack cocktail = craft(helper, ManaCocktailItem.getDefaultCocktail(), flask);
		helper.assertTrue(cocktail.is(ExtraBotanyItems.manaCocktail), "Flask recipe produced the wrong item");
		cocktail = saveAndLoad(helper, cocktail);
		var item = (ManaCocktailItem) cocktail.getItem();
		helper.assertTrue(item.getBrew(cocktail) == ExtraBotanyBrews.revolution && item.getSwigsLeft(cocktail) == 8,
				"Flask recipe lost the brew component or changed the full cocktail amount");
		var player = helper.makeMockPlayer(GameType.SURVIVAL);
		item.finishUsingItem(cocktail, helper.getLevel(), player);
		var haste = player.getEffect(MobEffects.DIG_SPEED);
		helper.assertTrue(haste != null && haste.getAmplifier() == 2 && haste.getDuration() == 1800,
				"Crafted cocktail did not apply the flask's actual effect");
		helper.assertTrue(item.getSwigsLeft(cocktail) == 7, "Crafted cocktail did not consume exactly one swig");
		helper.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void cocktailBrewSurvivesWineCreationRemakeAndDrink(GameTestHelper helper) {
		for (var ingredient : List.of(ExtraBotanyItems.heroMedal, ExtraBotanyItems.infiniteWine)) {
			ItemStack cocktail = new ItemStack(ExtraBotanyItems.manaCocktail);
			BaseBrewItem.setBrew(cocktail, ExtraBotanyBrews.revolution);
			ItemStack wineStack = saveAndLoad(helper, craft(helper, cocktail, new ItemStack(ingredient)));
			helper.assertTrue(wineStack.is(ExtraBotanyItems.infiniteWine), "Wine recipe produced the wrong item");
			var wine = (InfiniteWineItem) wineStack.getItem();
			helper.assertTrue(wine.getBrew(wineStack) == ExtraBotanyBrews.revolution && wine.getSwigsLeft(wineStack) == 12,
					"Wine creation/remake lost its brew or changed the full wine amount");
			var player = helper.makeMockPlayer(GameType.SURVIVAL);
			wine.finishUsingItem(wineStack, helper.getLevel(), player);
			var haste = player.getEffect(MobEffects.DIG_SPEED);
			helper.assertTrue(haste != null && haste.getAmplifier() == 3 && haste.getDuration() == 2700,
					"Crafted wine did not apply its copied brew with one amplification");
			helper.assertTrue(wine.getSwigsLeft(wineStack) == 11, "Crafted wine did not consume exactly one swig");
		}
		helper.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void cocktailBrewRemainsReadableByGrenadeItemAndEntity(GameTestHelper helper) {
		ItemStack cocktail = new ItemStack(ExtraBotanyItems.manaCocktail);
		BaseBrewItem.setBrew(cocktail, ExtraBotanyBrews.revolution);
		ItemStack grenadeStack = saveAndLoad(helper, craft(helper, cocktail, new ItemStack(Items.POPPED_CHORUS_FRUIT)));
		helper.assertTrue(grenadeStack.is(ExtraBotanyItems.holyWaterGrenade), "Grenade recipe produced the wrong item");
		var item = (HolyWaterGrenadeItem) grenadeStack.getItem();
		var grenade = new HolyWaterGrenadeEntity(helper.getLevel(), helper.makeMockPlayer(GameType.SURVIVAL));
		grenade.setItem(grenadeStack);
		helper.assertTrue(item.getBrew(grenadeStack) == ExtraBotanyBrews.revolution
				&& grenade.getBrew() == ExtraBotanyBrews.revolution,
				"Recipe component migration made grenade item/entity disagree on the brew");
		helper.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void potionLensAcceptsModernBrewAndKeepsLensData(GameTestHelper helper) {
		var recipe = new PotionLensChangePotionRecipe(CraftingBookCategory.MISC);
		ItemStack cocktail = new ItemStack(ExtraBotanyItems.manaCocktail);
		BaseBrewItem.setBrew(cocktail, ExtraBotanyBrews.revolution);
		ItemStack lens = new ItemStack(ExtraBotanyItems.lensPotion, 3);
		ItemNBTHelper.setInt(lens, "recipe_marker", 29);
		for (var ingredients : List.of(List.of(lens, cocktail), List.of(cocktail, lens))) {
			var input = CraftingInput.of(2, 1, ingredients);
			helper.assertTrue(recipe.matches(input, helper.getLevel()), "Potion lens rejected a full modern cocktail");
			ItemStack result = saveAndLoad(helper, recipe.assemble(input, helper.getLevel().registryAccess()));
			helper.assertTrue(result.is(ExtraBotanyItems.lensPotion) && result.getCount() == 1
					&& BrewUtil.getBrew(result) == ExtraBotanyBrews.revolution,
					"Potion lens lost the modern cocktail's brew or copied the input stack count");
			helper.assertTrue(ItemNBTHelper.getInt(result, "recipe_marker", 0) == 29 && lens.getCount() == 3
					&& BrewUtil.getBrew(lens) == BotaniaBrews.FALLBACK,
					"Potion lens recipe lost unrelated data or mutated its input");
		}
		helper.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void legacyBrewKeyStillCopiesAndComponentTakesPrecedence(GameTestHelper helper) {
		var registry = BotaniaAPI.instance().getBrewRegistry();
		ItemStack flask = new ItemStack(BotaniaItems.BREW_FLASK);
		ItemNBTHelper.setString(flask, BrewUtil.TAG_BREW_KEY, registry.getKey(ExtraBotanyBrews.revolution).toString());
		ItemStack result = craft(helper, ManaCocktailItem.getDefaultCocktail(), flask);
		helper.assertTrue(((ManaCocktailItem) result.getItem()).getBrew(result) == ExtraBotanyBrews.revolution,
				"Legacy-only flask lost its brew when copied into a component-based cocktail");
		helper.assertTrue(!flask.has(BotaniaDataComponents.BREW) && ItemNBTHelper.verifyExistance(flask, BrewUtil.TAG_BREW_KEY),
				"Reading a legacy flask modified the input save representation");

		BaseBrewItem.setBrew(flask, BotaniaBrews.MENDING);
		helper.assertTrue(BrewUtil.getBrew(flask) == BotaniaBrews.MENDING,
				"Stale legacy key overrode the brew component used by Botania");
		result = craft(helper, ManaCocktailItem.getDefaultCocktail(), flask);
		helper.assertTrue(((ManaCocktailItem) result.getItem()).getBrew(result) == BotaniaBrews.MENDING,
				"Copy recipe ignored the current component in favor of the old brew key");
		helper.succeed();
	}
}
