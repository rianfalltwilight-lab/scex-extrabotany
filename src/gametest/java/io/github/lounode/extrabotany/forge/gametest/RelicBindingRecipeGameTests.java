package io.github.lounode.extrabotany.forge.gametest;

import io.github.lounode.extrabotany.common.crafting.recipe.DasRheingoldChangeSoulBoundRecipe;
import io.github.lounode.extrabotany.common.helper.ItemNBTHelper;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.botania.api.item.Relic;
import vazkii.botania.common.item.BotaniaItems;

import java.util.List;
import java.util.UUID;

@GameTestHolder("extrabotany")
@PrefixGameTestTemplate(false)
public final class RelicBindingRecipeGameTests {

	private RelicBindingRecipeGameTests() {}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void rheingoldChangesActualRelicOwnerAndSurvivesSave(GameTestHelper helper) {
		var recipe = new DasRheingoldChangeSoulBoundRecipe(CraftingBookCategory.EQUIPMENT);
		var oldOwner = helper.makeMockPlayer(GameType.SURVIVAL);
		oldOwner.setUUID(UUID.fromString("11111111-1111-1111-1111-111111111111"));
		var newOwner = helper.makeMockPlayer(GameType.SURVIVAL);
		newOwner.setUUID(UUID.fromString("22222222-2222-2222-2222-222222222222"));
		for (var item : List.of(ExtraBotanyItems.excalibur, BotaniaItems.FRUIT_OF_GRISAIA)) {
			for (boolean bind : new boolean[] {true, false}) {
				ItemStack inputRelic = new ItemStack(item);
				Relic.LOOKUP.find(inputRelic).bindToUUID(oldOwner.getUUID());
				ItemNBTHelper.setInt(inputRelic, "recipe_marker", 61);
				// A migrated stack can contain both formats; neither may resurrect an old owner.
				ItemNBTHelper.setString(inputRelic, "soulbindUUID", oldOwner.getUUID().toString());
				ItemStack gold = new ItemStack(ExtraBotanyItems.dasRheingold);
				if (bind) ItemNBTHelper.setString(gold, "soulbindUUID", newOwner.getUUID().toString());
				ItemStack beforeRelic = inputRelic.copy();
				ItemStack beforeGold = gold.copy();
				var input = CraftingInput.of(2, 1, List.of(inputRelic, gold));
				helper.assertTrue(recipe.matches(input, helper.getLevel()), "Rheingold rejected a registered relic");
				ItemStack result = recipe.assemble(input, helper.getLevel().registryAccess());
				result = ItemStack.parseOptional(helper.getLevel().registryAccess(),
						(CompoundTag) result.save(helper.getLevel().registryAccess()));
				Relic actual = Relic.LOOKUP.find(result);
				helper.assertTrue(actual != null && !actual.isRightPlayer(oldOwner),
						"Rheingold changed only its legacy tag and left the actual owner bound");
				if (bind) {
					helper.assertTrue(actual.isRightPlayer(newOwner), "Rheingold did not bind the actual relic to its target player");
				} else {
					helper.assertTrue(actual.getSoulbindUUID() == null, "Empty Rheingold did not remove the binding component");
					actual.tickBinding(newOwner);
					helper.assertTrue(actual.isRightPlayer(newOwner), "Unbound relic could not bind to its next holder");
				}
				helper.assertTrue(ItemNBTHelper.getInt(result, "recipe_marker", 0) == 61
						&& !ItemNBTHelper.verifyExistance(result, "soulbindUUID"),
						"Binding lost unrelated data or retained a stale legacy owner");
				helper.assertTrue(ItemStack.matches(inputRelic, beforeRelic) && ItemStack.matches(gold, beforeGold),
						"Binding recipe mutated its ingredient stacks");
			}
		}
		helper.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void malformedRheingoldBindingDoesNotMutateCraftingInput(GameTestHelper helper) {
		var recipe = new DasRheingoldChangeSoulBoundRecipe(CraftingBookCategory.EQUIPMENT);
		ItemStack relic = new ItemStack(ExtraBotanyItems.excalibur);
		Relic.LOOKUP.find(relic).bindToUUID(UUID.fromString("11111111-1111-1111-1111-111111111111"));
		ItemStack gold = new ItemStack(ExtraBotanyItems.dasRheingold);
		ItemNBTHelper.setString(gold, "soulbindUUID", "invalid-legacy-owner");
		ItemStack beforeGold = gold.copy();
		var input = CraftingInput.of(2, 1, List.of(relic, gold));
		ItemStack result = recipe.assemble(input, helper.getLevel().registryAccess());
		helper.assertTrue(!result.isEmpty() && Relic.LOOKUP.find(result).getSoulbindUUID() == null,
				"Malformed Rheingold binding failed to produce an unbound relic");
		helper.assertTrue(ItemStack.matches(gold, beforeGold), "Reading malformed binding changed the crafting grid");
		helper.succeed();
	}
}
