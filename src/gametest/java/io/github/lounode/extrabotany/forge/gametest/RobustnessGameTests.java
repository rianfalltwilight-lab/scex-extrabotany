package io.github.lounode.extrabotany.forge.gametest;

import io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks;
import io.github.lounode.extrabotany.common.block.flower.generating.ResoncundBlockEntity;
import io.github.lounode.extrabotany.common.crafting.recipe.DasRheingoldChangeSoulBoundRecipe;
import io.github.lounode.extrabotany.common.helper.ItemNBTHelper;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

@GameTestHolder("extrabotany")
@PrefixGameTestTemplate(false)
public final class RobustnessGameTests {
    private RobustnessGameTests() {}

    private static FixedResoncund flower(GameTestHelper h) {
        var flower = new FixedResoncund(h.absolutePos(new BlockPos(2, 1, 2)));
        flower.setLevel(h.getLevel());
        return flower;
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void resoncundKeepsNormalDecayAndDoesNotOverflow(GameTestHelper h) {
        var flower = flower(h);
        var sound = SoundEvents.NOTE_BLOCK_HARP.value();
        for (int expected : new int[] {500, 450, 400, 350, 300, 250, 200, 150, 100, 50, 0, 0}) {
            flower.addMana(-flower.getMana());
            flower.onSoundHeard(sound);
            h.assertTrue(flower.getMana() == expected, "Normal repeated-sound decay changed: expected " + expected);
        }

        flower.loss = Integer.MAX_VALUE; // This is a valid config value.
        flower.getSoundHeard().put(sound, 2);
        flower.addMana(-flower.getMana());
        flower.onSoundHeard(sound);
        h.assertTrue(flower.getMana() == 0, "Large configured decay wrapped into a positive mana reward");

        flower.loss = 50;
        flower.getSoundHeard().put(sound, Integer.MAX_VALUE);
        var saved = flower.saveWithoutMetadata(h.getLevel().registryAccess());
        var restored = flower(h);
        restored.loadWithComponents(saved, h.getLevel().registryAccess());
        for (int i = 0; i < 2; i++) {
            restored.onSoundHeard(sound);
            h.assertTrue(restored.getMana() == 0 && restored.getSoundHeard().get(sound) == Integer.MAX_VALUE,
                    "Loaded counter limit wrapped or restarted the exhausted sound reward");
        }
        h.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void resoncundIgnoresInvalidSavedSoundsWithoutLosingValidCounts(GameTestHelper h) {
        var sounds = new CompoundTag();
        var valid = SoundEvents.NOTE_BLOCK_HARP.value();
        sounds.putInt(valid.getLocation().toString(), 7);
        sounds.putInt(SoundEvents.NOTE_BLOCK_BASS.value().getLocation().toString(), -1);
        sounds.putInt("not a valid sound ID", 4);
        sounds.putInt("extrabotany:nonexistent_robustness_sound", 5);
        var saved = new CompoundTag();
        saved.put(ResoncundBlockEntity.TAG_SOUND_HEARD, sounds);
        var flower = flower(h);
        flower.readFromPacketNBT(saved);
        h.assertTrue(flower.getSoundHeard().size() == 1 && flower.getSoundHeard().get(valid) == 7,
                "Malformed, unknown or negative-count sound corrupted the valid sound cache");
        flower.onSoundHeard(valid);
        h.assertTrue(flower.getMana() == 150, "Valid persisted repeat count did not retain its original decay");
        h.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void rheingoldChangesOnlyOneRelicAndRejectsForeignSlots(GameTestHelper h) {
        var recipe = new DasRheingoldChangeSoulBoundRecipe(CraftingBookCategory.EQUIPMENT);
        var oldOwner = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
        var newOwner = UUID.fromString("11111111-2222-3333-4444-555555555555");
        for (boolean bound : new boolean[] {true, false}) {
            var gold = new ItemStack(ExtraBotanyItems.dasRheingold, 4);
            var relic = new ItemStack(ExtraBotanyItems.excalibur, 3);
            DasRheingoldChangeSoulBoundRecipe.bindToUUID(relic, oldOwner);
            ItemNBTHelper.setInt(relic, "robustness_marker", 73);
            if (bound) DasRheingoldChangeSoulBoundRecipe.bindToUUID(gold, newOwner);
            var beforeGold = gold.copy();
            var beforeRelic = relic.copy();
            var input = CraftingInput.of(2, 2, List.of(relic, ItemStack.EMPTY, gold, ItemStack.EMPTY));
            h.assertTrue(recipe.matches(input, h.getLevel()), "Valid relic and Rheingold were rejected");
            var output = recipe.assemble(input, h.getLevel().registryAccess());
            h.assertTrue(output.is(ExtraBotanyItems.excalibur) && output.getCount() == 1
                    && ItemNBTHelper.getInt(output, "robustness_marker", 0) == 73,
                    "Binding changed the relic data or duplicated an overstacked input");
            var owner = DasRheingoldChangeSoulBoundRecipe.getSoulbindUUID(output);
            h.assertTrue(bound ? newOwner.equals(owner) : owner == null, "Original bind/unbind behavior changed");
            h.assertTrue(ItemStack.matches(gold, beforeGold) && ItemStack.matches(relic, beforeRelic),
                    "Recipe assembly mutated its inputs");

            for (var stacks : List.of(
                    List.of(gold, new ItemStack(Items.DIAMOND), relic),
                    List.of(gold, relic, new ItemStack(Items.DIAMOND)),
                    List.of(gold, relic, gold.copy()),
                    List.of(gold, relic, relic.copy()),
                    List.of(gold, ItemStack.EMPTY, new ItemStack(Items.DIAMOND)),
                    List.of(relic, ItemStack.EMPTY, relic.copy()))) {
                var invalid = CraftingInput.of(3, 1, stacks);
                h.assertTrue(!recipe.matches(invalid, h.getLevel())
                        && recipe.assemble(invalid, h.getLevel().registryAccess()).isEmpty(),
                        "Foreign, duplicate or missing input was accepted by the binding recipe");
            }
        }
        h.succeed();
    }

    private static final class FixedResoncund extends ResoncundBlockEntity {
        private int loss = 50;
        private FixedResoncund(BlockPos pos) { super(pos, ExtrabotanyFlowerBlocks.resoncund.defaultBlockState()); }
        @Override public int getLossPerHeard() { return loss; }
        @Override public int getMaxMana() { return 1200; }
    }
}
