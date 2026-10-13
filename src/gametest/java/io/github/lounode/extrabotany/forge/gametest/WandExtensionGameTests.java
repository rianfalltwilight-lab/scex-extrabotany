package io.github.lounode.extrabotany.forge.gametest;

import java.util.List;
import io.github.lounode.extrabotany.common.crafting.recipe.WandOfTheForestExtendRecipe;
import io.github.lounode.extrabotany.common.helper.ItemNBTHelper;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.botania.common.item.BotaniaItems;

@GameTestHolder("extrabotany")
@PrefixGameTestTemplate(false)
public final class WandExtensionGameTests {
    @GameTest(template = "empty", timeoutTicks = 20)
    public static void wandUpgradePreservesInputsAndRejectsForeignSlots(GameTestHelper h) {
        var recipe = new WandOfTheForestExtendRecipe(CraftingBookCategory.EQUIPMENT);
        for (var wandItem : List.of(BotaniaItems.WAND_OF_THE_FOREST, BotaniaItems.WAND_OF_THE_ELVEN_FOREST)) {
            var wand = new ItemStack(wandItem);
            ItemNBTHelper.setInt(wand, "unchanged_marker", 17);
            var reader = new ItemStack(ExtraBotanyItems.manaReader);
            var input = CraftingInput.of(2, 2, List.of(wand, ItemStack.EMPTY, reader, ItemStack.EMPTY));
            h.assertTrue(recipe.matches(input, h.getLevel()), "Valid wand + reader rejected");
            var result = recipe.assemble(input, h.getLevel().registryAccess());
            h.assertTrue(result.is(wandItem) && result.getCount() == 1, "Upgrade changed wand type or output count");
            h.assertTrue(ItemNBTHelper.getInt(result, "unchanged_marker", 0) == 17, "Upgrade lost existing wand data");
            h.assertTrue(((CompoundTag) ItemNBTHelper.get(result, "extrabotany")).getCompound("mana_reader").getBoolean("enable"), "Upgrade flag absent");
            h.assertTrue(!ItemNBTHelper.verifyExistance(wand, "extrabotany") && reader.getCount() == 1, "Assemble mutated input stacks");
            for (var extra : List.of(new ItemStack(Items.DIAMOND), wand.copy(), reader.copy())) {
                var invalid = CraftingInput.of(3, 1, List.of(wand, reader, extra));
                h.assertTrue(!recipe.matches(invalid, h.getLevel()), "Foreign/duplicate slot accepted and would be consumed");
                h.assertTrue(recipe.assemble(invalid, h.getLevel().registryAccess()).isEmpty(), "Invalid input produced a wand");
            }
            var wrongReader = CraftingInput.of(2, 1, List.of(wand, new ItemStack(Items.DIAMOND)));
            h.assertTrue(!recipe.matches(wrongReader, h.getLevel()) && recipe.assemble(wrongReader, h.getLevel().registryAccess()).isEmpty(), "Foreign item substituted for reader");
        }
        h.succeed();
    }
}
