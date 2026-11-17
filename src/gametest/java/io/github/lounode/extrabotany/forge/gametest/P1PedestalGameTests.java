package io.github.lounode.extrabotany.forge.gametest;

import io.github.lounode.extrabotany.common.block.ExtraBotanyBlocks;
import io.github.lounode.extrabotany.common.block.block_entity.PedestalBlockEntity;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

@GameTestHolder("extrabotany")
@PrefixGameTestTemplate(false)
public final class P1PedestalGameTests {
    @GameTest(template = "empty", timeoutTicks = 20)
    public static void removedHammerCannotSmashOrDeleteItsReplacement(GameTestHelper h) {
        var pedestal = create(h);
        var hammer = new ItemStack(ExtraBotanyItems.manasteelHammer);
        hammer.setDamageValue(hammer.getMaxDamage() - 1);
        var frame = attachFrame(h, pedestal, hammer);
        List<ItemEntity> removedDrops = List.of();
        try {
            refreshCache(pedestal);
            ItemStack oldFramedHammer = frame.getItem();
            int oldDamage = oldFramedHammer.getDamageValue();
            h.assertTrue(frame.hurt(h.getLevel().damageSources().generic(), 1), "Taking the hammer out failed");
            h.assertTrue(frame.isAlive() && frame.getItem().isEmpty(), "Taking the item also removed its frame");
            removedDrops = h.getLevel().getEntitiesOfClass(ItemEntity.class, frame.getBoundingBox().inflate(2),
                    entity -> entity.getItem().is(ExtraBotanyItems.manasteelHammer));
            h.assertTrue(removedDrops.size() == 1, "The removed hammer was not returned as one dropped item");

            var replacement = new ItemStack(Items.NETHERITE_INGOT);
            replacement.set(DataComponents.CUSTOM_NAME, Component.literal("Keep this replacement"));
            frame.setItem(replacement);
            pedestal.setItem(new ItemStack(ExtraBotanyItems.spiritFuel));
            // The cache was refreshed at tick 20. Reach tick 30 before its next refresh at tick 40.
            tick(pedestal, 10);

            h.assertTrue(ItemStack.isSameItemSameComponents(frame.getItem(), replacement)
                            && frame.getItem().getCount() == 1,
                    "A removed hammer cleared the item later placed in its frame");
            h.assertTrue(pedestal.getStrikes() == 0 && pedestal.getItem().is(ExtraBotanyItems.spiritFuel),
                    "A removed hammer still processed the pedestal input");
            h.assertTrue(oldFramedHammer.getCount() == 1 && oldFramedHammer.getDamageValue() == oldDamage,
                    "The pedestal damaged its cached, removed hammer");
            h.assertTrue(removedDrops.getFirst().getItem().getDamageValue() == oldDamage,
                    "The returned hammer changed while it was outside the frame");
        } finally {
            frame.discard();
            removedDrops.forEach(ItemEntity::discard);
        }
        h.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void currentFramedHammerConsumesDurabilityAndCompletesRecipe(GameTestHelper h) {
        var pedestal = create(h);
        var frame = attachFrame(h, pedestal, new ItemStack(ExtraBotanyItems.manasteelHammer));
        try {
            refreshCache(pedestal);
            pedestal.setItem(new ItemStack(ExtraBotanyItems.spiritFuel));
            tick(pedestal, 100);
            assertCompleted(h, pedestal);
            h.assertTrue(frame.getItem().is(ExtraBotanyItems.manasteelHammer)
                            && frame.getItem().getDamageValue() == 10,
                    "The current framed hammer did not pay one durability for each of the ten strikes");
            tick(pedestal, 10);
            assertCompleted(h, pedestal);
            h.assertTrue(frame.getItem().getDamageValue() == 10, "A finished recipe kept damaging its hammer");
        } finally {
            frame.discard();
        }
        h.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void currentHammerBreaksOnFinalStrikeAndClearsOnlyItsOwnFrame(GameTestHelper h) {
        var pedestal = create(h);
        var hammer = new ItemStack(ExtraBotanyItems.manasteelHammer);
        hammer.setDamageValue(hammer.getMaxDamage() - 10);
        var frame = attachFrame(h, pedestal, hammer);
        try {
            refreshCache(pedestal);
            pedestal.setItem(new ItemStack(ExtraBotanyItems.spiritFuel));
            tick(pedestal, 100);
            assertCompleted(h, pedestal);
            h.assertTrue(frame.isAlive() && frame.getItem().isEmpty(),
                    "The broken current hammer was not removed from its still-living frame");
            tick(pedestal, 10);
            assertCompleted(h, pedestal);
        } finally {
            frame.discard();
        }
        h.succeed();
    }

    private static PedestalBlockEntity create(GameTestHelper h) {
        var relative = new BlockPos(2, 2, 2);
        h.setBlock(relative, ExtraBotanyBlocks.livingrockPedestal);
        return h.getBlockEntity(relative);
    }

    private static ItemFrame attachFrame(GameTestHelper h, PedestalBlockEntity pedestal, ItemStack item) {
        var frame = new ItemFrame(h.getLevel(), pedestal.getBlockPos().relative(Direction.NORTH), Direction.NORTH);
        frame.setItem(item);
        h.assertTrue(h.getLevel().addFreshEntity(frame), "The item frame was not added to the test world");
        h.assertTrue(pedestal.getPerhapsHammers().containsValue(frame), "The actual frame was outside the pedestal scan");
        return frame;
    }

    private static void refreshCache(PedestalBlockEntity pedestal) {
        // Keep the pedestal empty during the real tick-20 scan, so the cached hammer is not used yet.
        pedestal.tickCount = 20;
        tick(pedestal, 1);
    }

    private static void tick(PedestalBlockEntity pedestal, int ticks) {
        for (int i = 0; i < ticks; i++) {
            PedestalBlockEntity.serverTick(pedestal.getLevel(), pedestal.getBlockPos(), pedestal.getBlockState(), pedestal);
        }
    }

    private static void assertCompleted(GameTestHelper h, PedestalBlockEntity pedestal) {
        h.assertTrue(pedestal.getItem().is(ExtraBotanyItems.spiritFragment) && pedestal.getItem().getCount() == 1
                        && pedestal.getStrikes() == pedestal.FINISH_CRAFT_STRIKE_FLAG,
                "Ten strikes did not produce exactly one completed spirit fragment");
    }
}
