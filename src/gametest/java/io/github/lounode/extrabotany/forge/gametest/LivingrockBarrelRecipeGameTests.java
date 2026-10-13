package io.github.lounode.extrabotany.forge.gametest;

import io.github.lounode.extrabotany.common.block.ExtraBotanyBlocks;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.botania.common.block.BotaniaBlocks;

@GameTestHolder("extrabotany")
@PrefixGameTestTemplate(false)
public final class LivingrockBarrelRecipeGameTests {
    @GameTest(template = "empty", timeoutTicks = 20)
    public static void sevenLivingrockCraftOneBarrel(GameTestHelper helper) {
        var stacks = new java.util.ArrayList<ItemStack>();
        for (int i = 0; i < 9; i++) stacks.add(i == 1 || i == 4 ? ItemStack.EMPTY : new ItemStack(BotaniaBlocks.LIVINGROCK));
        var input = CraftingInput.of(3, 3, stacks);
        var level = helper.getLevel();
        var recipe = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level).orElseThrow();
        var output = recipe.value().assemble(input, level.registryAccess());
        helper.assertTrue(recipe.id().equals(ResourceLocation.parse("extrabotany:elfjar"))
                && output.is(ExtraBotanyBlocks.livingrockBarrel.asItem()) && output.getCount() == 1, "Wrong barrel recipe/result");
        var invalid = new java.util.ArrayList<>(stacks); invalid.set(0, new ItemStack(Items.COBBLESTONE));
        helper.assertTrue(!recipe.value().matches(CraftingInput.of(3, 3, invalid), level), "Recipe accepted ordinary cobblestone");
        helper.assertTrue(level.getServer().getAdvancements().get(ResourceLocation.parse("extrabotany:recipes/decorations/elfjar")) != null,
                "Recipe book unlock advancement missing");
        helper.succeed();
    }
}
