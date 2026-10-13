package io.github.lounode.extrabotany.forge.gametest;

import com.mojang.authlib.GameProfile;
import io.github.lounode.extrabotany.common.block.ExtraBotanyBlocks;
import io.github.lounode.extrabotany.common.block.block_entity.PedestalBlockEntity;
import io.github.lounode.extrabotany.common.crafting.PedestalsRecipe;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.lib.LibMisc;
import io.github.lounode.extrabotany.xplat.EXplatAbstractions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@GameTestHolder(LibMisc.MOD_ID)
@PrefixGameTestTemplate(false)
public final class PedestalStateGameTests {
    @GameTest(template = "empty", timeoutTicks = 20)
    public static void idleAndUnchangedStrikesDoNotDirtyPedestal(GameTestHelper h) {
        var pedestal = create(h);
        for (int i = 0; i <= 20; i++) {
            pedestal.setStrikes(0);
            tick(pedestal, i);
        }
        h.assertTrue(pedestal.dirtyCalls == 0, "Idle pedestal still dirties or broadcasts every ten ticks");
        h.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void lastChargePersistsAndFlushesAfterOrbBecomesFull(GameTestHelper h) {
        var pedestal = create(h);
        pedestal.setItem(new ItemStack(ExtraBotanyItems.natureOrb));
        var energy = EXplatAbstractions.INSTANCE.findNatureEnergyItem(pedestal.getItem());
        energy.addEnergy(energy.getMaxEnergy() - 1);
        pedestal.chargeAmount = 4;
        pedestal.dirtyCalls = 0;
        tick(pedestal, 1);
        h.assertTrue(energy.getEnergy() == energy.getMaxEnergy() && pedestal.dirtyCalls == 1,
                "Final partial charge must dirty the save immediately");
        var restored = restore(h, pedestal);
        h.assertTrue(EXplatAbstractions.INSTANCE.findNatureEnergyItem(restored.getItem()).getEnergy() == energy.getMaxEnergy(),
                "Final charge was not preserved by save/load");
        for (int i = 2; i <= 10; i++) tick(pedestal, i);
        h.assertTrue(pedestal.dirtyCalls == 2, "Full orb lost its pending ten-tick update");
        for (int i = 11; i <= 20; i++) tick(pedestal, i);
        h.assertTrue(pedestal.dirtyCalls == 2, "Full orb schedules repeated unchanged updates");
        h.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void manualPartialStrikePersistsAndFlushesWithoutEnergy(GameTestHelper h) {
        var manager = h.getLevel().getRecipeManager();
        var original = new ArrayList<>(manager.getRecipes());
        var player = FakePlayerFactory.get(h.getLevel(), new GameProfile(UUID.randomUUID(), "[PedestalTest]"));
        try {
            manager.replaceRecipes(List.of(recipe(3)));
            var pedestal = create(h);
            pedestal.setItem(new ItemStack(Items.COBBLESTONE));
            pedestal.dirtyCalls = 0;
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK));
            pedestal.handleSmashNew(pedestal.getBlockState(), h.getLevel(), pedestal.getBlockPos(),
                    player, InteractionHand.MAIN_HAND, null);
            h.assertTrue(pedestal.getStrikes() == 1 && pedestal.dirtyCalls == 1,
                    "Manual partial strike did not immediately dirty its save");
            var restored = restore(h, pedestal);
            h.assertTrue(restored.getStrikes() == 1 && restored.getItem().is(Items.COBBLESTONE),
                    "Partial manual strike did not survive save/load");
            // The inherited inventory loader marks its container changed; loading must not queue another update.
            restored.dirtyCalls = 0;
            tick(restored, 0);
            h.assertTrue(restored.dirtyCalls == 0, "Loaded strike state scheduled an unnecessary client update");
            tick(pedestal, 9);
            h.assertTrue(pedestal.dirtyCalls == 1, "Manual strike changed the ten-tick update cadence");
            tick(pedestal, 10);
            h.assertTrue(pedestal.dirtyCalls == 2, "Manual strike never flushed without a nature orb");
            tick(pedestal, 20);
            h.assertTrue(pedestal.dirtyCalls == 2, "Completed strike update kept broadcasting while idle");
        } finally {
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            manager.replaceRecipes(original);
        }
        h.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void automaticStrikeAndCompletionSurviveSaveReload(GameTestHelper h) {
        var manager = h.getLevel().getRecipeManager();
        var original = new ArrayList<>(manager.getRecipes());
        try {
            manager.replaceRecipes(List.of(recipe(2)));
            var pedestal = create(h);
            pedestal.setItem(new ItemStack(Items.COBBLESTONE));
            pedestal.dirtyCalls = 0;
            var hammer = new ItemStack(Items.STICK);
            h.assertTrue(pedestal.tryAutoSmash(hammer) && pedestal.getStrikes() == 1 && pedestal.dirtyCalls == 1,
                    "Automatic partial strike did not dirty its save");
            var restored = restore(h, pedestal);
            h.assertTrue(restored.getStrikes() == 1 && restored.tryAutoSmash(hammer),
                    "Automatic strike could not resume after save/load");
            h.assertTrue(restored.getItem().is(Items.GRANITE) && restored.getStrikes() == -1,
                    "Automatic strike did not finish with its original output and completion flag");
            h.assertTrue(restored.getAnalogOutputSignal() == 15 && restored.canTakeItemThroughFace(0, restored.getItem(), Direction.DOWN),
                    "Completed recipe lost comparator or hopper eligibility");
            int beforeFlush = restored.dirtyCalls;
            tick(restored, 10);
            h.assertTrue(restored.dirtyCalls == beforeFlush + 1,
                    "Completion flag written after setItem was not queued for the client");
            var completed = restore(h, restored);
            h.assertTrue(completed.getStrikes() == -1 && completed.getItem().is(Items.GRANITE),
                    "Finished automatic recipe lost output or completion flag on reload");
        } finally {
            manager.replaceRecipes(original);
        }
        h.succeed();
    }

    private static RecipeHolder<PedestalsRecipe> recipe(int strikes) {
        return new RecipeHolder<>(ResourceLocation.fromNamespaceAndPath(LibMisc.MOD_ID, "test/pedestal_state"),
                new PedestalsRecipe(new ItemStack(Items.GRANITE), Ingredient.of(Items.STICK),
                        Ingredient.of(Items.COBBLESTONE), strikes, 0));
    }

    private static CountingPedestal create(GameTestHelper h) {
        var relative = new BlockPos(1, 2, 1);
        h.setBlock(relative, ExtraBotanyBlocks.livingrockPedestal);
        var pedestal = new CountingPedestal(h.absolutePos(relative));
        pedestal.setLevel(h.getLevel());
        pedestal.dirtyCalls = 0;
        return pedestal;
    }

    private static CountingPedestal restore(GameTestHelper h, CountingPedestal original) {
        var restored = new CountingPedestal(original.getBlockPos());
        restored.setLevel(h.getLevel());
        restored.loadWithComponents(original.saveWithoutMetadata(h.getLevel().registryAccess()), h.getLevel().registryAccess());
        return restored;
    }

    private static void tick(CountingPedestal pedestal, int tick) {
        pedestal.tickCount = tick;
        PedestalBlockEntity.serverTick(pedestal.getLevel(), pedestal.getBlockPos(), pedestal.getBlockState(), pedestal);
    }

    private static final class CountingPedestal extends PedestalBlockEntity {
        private int dirtyCalls;
        private int chargeAmount;
        private CountingPedestal(BlockPos pos) { super(pos, ExtraBotanyBlocks.livingrockPedestal.defaultBlockState()); }
        @Override public void setChanged() { dirtyCalls++; super.setChanged(); }
        @Override public int getChargeAmount() { return chargeAmount; }
        @Override public void updateTier() { }
        @Override public void updateAutomaticHammers() { }
    }
}
