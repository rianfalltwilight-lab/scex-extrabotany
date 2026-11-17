package io.github.lounode.extrabotany.forge.gametest;

import io.github.lounode.extrabotany.common.brew.ExtraBotanyBrews;
import io.github.lounode.extrabotany.common.helper.ItemNBTHelper;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.item.brew.InfiniteWineItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.botania.api.item.Relic;
import vazkii.botania.common.item.brew.BaseBrewItem;

import java.util.List;
import java.util.UUID;

@GameTestHolder("extrabotany")
@PrefixGameTestTemplate(false)
public final class P2BrewGameTests {

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void wineRemakePreservesBoundOwnerAndUnrelatedComponents(GameTestHelper h) {
		var owner = h.makeMockPlayer(GameType.SURVIVAL);
		owner.setUUID(UUID.fromString("69f64623-a924-461a-84ee-8c36f45b7551"));
		var thief = h.makeMockPlayer(GameType.SURVIVAL);
		thief.setUUID(UUID.fromString("69f64623-a924-461a-84ee-8c36f45b7552"));
		ItemStack wine = new ItemStack(ExtraBotanyItems.infiniteWine);
		Relic.LOOKUP.find(wine).bindToUUID(owner.getUUID());
		wine.set(DataComponents.CUSTOM_NAME, Component.literal("Owner's wine"));
		wine.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
		ItemNBTHelper.setInt(wine, "recipe_marker", 73);
		ItemStack cocktail = new ItemStack(ExtraBotanyItems.manaCocktail);
		BaseBrewItem.setBrew(cocktail, ExtraBotanyBrews.revolution);
		for (var ingredients : List.of(List.of(wine, cocktail), List.of(cocktail, wine))) {
			ItemStack beforeWine = wine.copy(), beforeCocktail = cocktail.copy();
			var input = CraftingInput.of(2, 1, ingredients);
			var recipe = h.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, h.getLevel()).orElseThrow().value();
			ItemStack result = recipe.assemble(input, h.getLevel().registryAccess());
			result = ItemStack.parseOptional(h.getLevel().registryAccess(), (CompoundTag) result.save(h.getLevel().registryAccess()));
			var item = (InfiniteWineItem) result.getItem();
			h.assertTrue(item.getBrew(result) == ExtraBotanyBrews.revolution && item.getSwigsLeft(result) == item.getSwigs(result),
					"Remake lost its new brew or full uses");
			h.assertTrue(Relic.LOOKUP.find(result).isRightPlayer(owner) && !Relic.LOOKUP.find(result).isRightPlayer(thief),
					"Remaking wine erased its soul binding");
			h.assertTrue(Component.literal("Owner's wine").equals(result.get(DataComponents.CUSTOM_NAME))
					&& Boolean.TRUE.equals(result.get(DataComponents.ENCHANTMENT_GLINT_OVERRIDE))
					&& ItemNBTHelper.getInt(result, "recipe_marker", 0) == 73, "Remake lost unrelated components");
			thief.setItemInHand(InteractionHand.MAIN_HAND, result);
			item.inventoryTick(result, h.getLevel(), thief, 0, true);
			h.assertTrue(Relic.LOOKUP.find(result).isRightPlayer(owner)
					&& item.use(h.getLevel(), thief, InteractionHand.MAIN_HAND).getResult() == InteractionResult.PASS
					&& !thief.isUsingItem(), "New holder rebound or used another player's remade wine");
			owner.setItemInHand(InteractionHand.MAIN_HAND, result);
			h.assertTrue(item.use(h.getLevel(), owner, InteractionHand.MAIN_HAND).getResult().consumesAction(),
					"Original owner can no longer drink remade wine");
			owner.stopUsingItem();
			h.assertTrue(ItemStack.matches(wine, beforeWine) && ItemStack.matches(cocktail, beforeCocktail),
					"Remake mutated crafting inputs");
		}
		h.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void firstWineCreationDoesNotCopyMedalDataAndPartialWineCannotRemake(GameTestHelper h) {
		ItemStack medal = new ItemStack(ExtraBotanyItems.heroMedal);
		medal.set(DataComponents.CUSTOM_NAME, Component.literal("Medal only"));
		ItemNBTHelper.setInt(medal, "medal_marker", 19);
		ItemStack cocktail = new ItemStack(ExtraBotanyItems.manaCocktail);
		BaseBrewItem.setBrew(cocktail, ExtraBotanyBrews.revolution);
		var input = CraftingInput.of(2, 1, List.of(medal, cocktail));
		var recipe = h.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, h.getLevel()).orElseThrow().value();
		ItemStack result = recipe.assemble(input, h.getLevel().registryAccess());
		h.assertTrue(result.is(ExtraBotanyItems.infiniteWine) && !result.has(DataComponents.CUSTOM_NAME)
				&& ItemNBTHelper.getInt(result, "medal_marker", 0) == 0 && Relic.LOOKUP.find(result).getSoulbindUUID() == null,
				"First creation copied medal metadata into the new relic");
		var wine = (InfiniteWineItem) result.getItem();
		wine.setSwigsLeft(result, wine.getSwigs(result) - 1);
		var partial = CraftingInput.of(2, 1, List.of(result, cocktail));
		var remake = h.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, partial, h.getLevel()).orElseThrow().value();
		h.assertTrue(remake.assemble(partial, h.getLevel().registryAccess()).isEmpty(),
				"Partially consumed wine can be refilled by remaking it");
		h.succeed();
	}
}
