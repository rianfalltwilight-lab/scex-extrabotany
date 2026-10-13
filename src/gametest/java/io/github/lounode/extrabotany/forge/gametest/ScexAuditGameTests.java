package io.github.lounode.extrabotany.forge.gametest;

import io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks;
import io.github.lounode.extrabotany.common.block.flower.generating.ResoncundBlockEntity;
import io.github.lounode.extrabotany.common.helper.ItemNBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Regression contracts for the 2026-09-30 tick/GC audit (patch A). */
@GameTestHolder("extrabotany")
@PrefixGameTestTemplate(false)
public final class ScexAuditGameTests {
    private ScexAuditGameTests() {}

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void itemNbtReadsDoNotCopyButReturnDefensiveValues(GameTestHelper h) {
        var stack = new ItemStack(Items.PAPER);
        var list = new ListTag();
        list.add(StringTag.valueOf("a"));
        ItemNBTHelper.setList(stack, "list", list);
        ItemNBTHelper.setInt(stack, "n", 7);
        var before = stack.get(DataComponents.CUSTOM_DATA);

        var read = ItemNBTHelper.getList(stack, "list", Tag.TAG_STRING, false);
        read.add(StringTag.valueOf("leak"));
        h.assertTrue(ItemNBTHelper.getList(stack, "list", Tag.TAG_STRING, false).size() == 1,
                "getList leaked the live tag");
        h.assertTrue(ItemNBTHelper.getInt(stack, "n", -1) == 7 && ItemNBTHelper.getInt(stack, "missing", -1) == -1,
                "Primitive reads changed semantics");
        ItemNBTHelper.removeEntry(stack, "absent");
        h.assertTrue(stack.get(DataComponents.CUSTOM_DATA) == before,
                "Removing an absent key must not rewrite CUSTOM_DATA");
        ItemNBTHelper.removeEntry(stack, "n");
        h.assertTrue(!ItemNBTHelper.verifyExistance(stack, "n"), "Present key was not removed");
        h.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void boxSimulatorLookupIsInertWithoutSimulator(GameTestHelper h) {
        var player = h.makeMockPlayer(GameType.SURVIVAL);
        h.assertTrue(io.github.lounode.extrabotany.common.item.equipment.bauble.AllForOneRingItem.find(stack -> true, player).isEmpty(),
                "Players without a worn simulator must not be scanned");
        h.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void resoncundStopsListeningWhenRemoved(GameTestHelper h) {
        var flower = new ResoncundBlockEntity(h.absolutePos(new BlockPos(1, 1, 1)),
                ExtrabotanyFlowerBlocks.resoncund.defaultBlockState());
        flower.setLevel(h.getLevel());
        h.assertTrue(ResoncundBlockEntity.listeners().contains(flower), "New flower is not listening");
        flower.setRemoved();
        h.assertTrue(!ResoncundBlockEntity.listeners().contains(flower), "Removed flower is still listening");
        flower.clearRemoved();
        h.assertTrue(ResoncundBlockEntity.listeners().contains(flower), "Re-added flower is not listening");
        flower.setRemoved();
        h.succeed();
    }
}
