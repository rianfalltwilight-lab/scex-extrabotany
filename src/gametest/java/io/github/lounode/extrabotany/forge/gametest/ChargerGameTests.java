package io.github.lounode.extrabotany.forge.gametest;

import io.github.lounode.extrabotany.common.block.ExtraBotanyBlocks;
import io.github.lounode.extrabotany.common.block.block_entity.ChargerBlockEntity;
import io.github.lounode.extrabotany.common.block.block_entity.ManaChargerBlockEntity;
import io.github.lounode.extrabotany.common.block.block_entity.PowerFrameBlockEntity;
import io.github.lounode.extrabotany.xplat.EXplatAbstractions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.botania.api.state.BotaniaStateProperties;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.block.block_entity.mana.BellowsBlockEntity;
import vazkii.botania.common.block.block_entity.mana.ManaPoolBlockEntity;
import vazkii.botania.common.item.BotaniaItems;

import java.util.List;

@GameTestHolder("extrabotany")
@PrefixGameTestTemplate(false)
public final class ChargerGameTests {
    @GameTest(template = "empty", timeoutTicks = 20)
    public static void emptyAndMissingPoolDoNotBroadcastUnchangedInventory(GameTestHelper h) {
        var charger = create(h, false, 0);
        ticks(charger, 40);
        h.assertTrue(charger.tickCount == 40 && charger.syncCalls == 0 && charger.dirtyCalls == 0,
                "Empty charger stopped its clock or broadcast an unchanged slot");
        charger.setItem(new ItemStack(BotaniaItems.MANA_TABLET));
        ticks(charger, 40);
        h.assertTrue(charger.tickCount == 80 && charger.syncCalls == 1 && charger.dirtyCalls == 1,
                "Missing pool must send the inserted item once, then remain idle");
        h.assertTrue(mana(charger.getItem()) == 0, "Missing pool changed item mana");
        h.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void chargeAndDrainPreserveRateAndSendPostTransferSnapshots(GameTestHelper h) {
        var charger = create(h, true, 50_000);
        var pool = charger.getPool();
        charger.setItem(new ItemStack(BotaniaItems.MANA_TABLET));
        charger.dirtyCalls = 0;
        ticks(charger, 11);
        h.assertTrue(mana(charger.getItem()) == 11_000 && pool.getCurrentMana() == 39_000,
                "Charging changed the 1000 mana per tick transfer or conservation");
        h.assertTrue(charger.syncCalls == 2 && charger.dirtyCalls == 0,
                "Active charging must combine updates without notifying comparators every transfer tick");
        assertSyncedMana(h, charger, 11_000);

        // Changing the pool mode must affect the very next transfer.
        h.getLevel().setBlockAndUpdate(pool.getBlockPos(), pool.getBlockState().setValue(BotaniaStateProperties.OUTPUTTING, false));
        ticks(charger, 3);
        h.assertTrue(mana(charger.getItem()) == 8_000 && pool.getCurrentMana() == 42_000,
                "Draining changed the 1000 mana per tick transfer or conservation");
        h.assertTrue(charger.getItem().getCount() == 1, "Transfer changed the item count");
        h.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void finalPartialChargeFlushesAfterCompletionAndPersists(GameTestHelper h) {
        var charger = create(h, true, 30_000);
        var stack = new ItemStack(BotaniaItems.MANA_TABLET);
        var capability = EXplatAbstractions.INSTANCE.findManaItem(stack);
        capability.addMana(capability.getMaxMana() - 1_250);
        charger.setItem(stack);
        charger.tickCount = 1;
        charger.dirtyCalls = 0;
        var chunk = h.getLevel().getChunkAt(charger.getBlockPos());
        for (int i = 0; i < 2; i++) {
            chunk.setUnsaved(false);
            ticks(charger, 1);
            h.assertTrue(chunk.isUnsaved(), "Off-cadence transfer failed to mark its chunk for saving");
        }
        h.assertTrue(mana(stack) == capability.getMaxMana() && charger.getPool().getCurrentMana() == 28_750,
                "Final partial charge lost or created mana");
        h.assertTrue(charger.syncCalls == 0 && charger.dirtyCalls == 0,
                "Off-cadence work must not send packets or notify comparators every tick");
        var restored = restore(h, charger, charger.saveWithoutMetadata(h.getLevel().registryAccess()));
        h.assertTrue(mana(restored.getItem()) == capability.getMaxMana(), "Last partial charge was lost on save/load");
        ticks(charger, 1);
        h.assertTrue(charger.syncCalls == 1, "Completion before a ten-tick boundary lost the final update");
        assertSyncedMana(h, charger, capability.getMaxMana());
        ticks(charger, 40);
        h.assertTrue(charger.syncCalls == 1 && charger.dirtyCalls == 0, "Full item kept sending unchanged updates");
        restored.dirtyCalls = 0;
        ticks(restored, 20);
        h.assertTrue(restored.syncCalls == 0 && restored.dirtyCalls == 0, "Loading a full item queued an idle update");
        h.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void emptySourceAndFullDestinationFlushTheirLastTransfer(GameTestHelper h) {
        var charger = create(h, true, 1_250);
        charger.setItem(new ItemStack(BotaniaItems.MANA_TABLET));
        charger.tickCount = 1;
        ticks(charger, 3);
        h.assertTrue(mana(charger.getItem()) == 1_250 && charger.getPool().getCurrentMana() == 0 && charger.syncCalls == 1,
                "Empty source lost its final partial transfer or update");
        assertSyncedMana(h, charger, 1_250);
        ticks(charger, 20);
        h.assertTrue(charger.syncCalls == 1, "Empty source kept synchronizing unchanged inventory");

        var pool = charger.getPool();
        pool.receiveMana(pool.getMaxMana() - 250);
        h.getLevel().setBlockAndUpdate(pool.getBlockPos(), pool.getBlockState().setValue(BotaniaStateProperties.OUTPUTTING, false));
        charger.tickCount = 5;
        ticks(charger, 2);
        h.assertTrue(mana(charger.getItem()) == 1_000 && pool.getCurrentMana() == pool.getMaxMana() && charger.syncCalls == 2,
                "Full destination lost its final partial transfer or update");
        assertSyncedMana(h, charger, 1_000);
        ticks(charger, 20);
        h.assertTrue(charger.syncCalls == 2, "Full destination kept synchronizing unchanged inventory");
        h.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void slotRemovalAndInvalidItemReachUpdatePacket(GameTestHelper h) {
        var charger = create(h, true, 20_000);
        charger.setItem(new ItemStack(BotaniaItems.MANA_TABLET));
        charger.tickCount = 1;
        ticks(charger, 1);
        var removed = charger.removeItem(0, 1);
        ticks(charger, 1);
        h.assertTrue(mana(removed) == 1_000 && charger.isEmpty() && charger.syncCalls == 1,
                "Removing an item lost its charge or the empty-slot update");
        var client = restore(h, charger, charger.lastPacket);
        h.assertTrue(client.isEmpty(), "Empty slot was absent from the block entity packet");
        charger.setItem(new ItemStack(Items.STONE));
        ticks(charger, 20);
        h.assertTrue(charger.syncCalls == 2 && charger.getItem().is(Items.STONE), "Invalid slot contents repeatedly synced or changed");
        client = restore(h, charger, charger.lastPacket);
        h.assertTrue(client.getItem().is(Items.STONE), "Idle slot replacement was absent from the update packet");
        h.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void losingPoolFlushesPendingManaAndReloadResumes(GameTestHelper h) {
        var charger = create(h, true, 20_000);
        charger.setItem(new ItemStack(BotaniaItems.MANA_TABLET));
        charger.tickCount = 1;
        ticks(charger, 1);
        // Suppress shape propagation to exercise a missing pool without destroying the charger.
        h.getLevel().setBlock(charger.getBlockPos().below(), Blocks.AIR.defaultBlockState(),
                Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        ticks(charger, 1);
        h.assertTrue(charger.syncCalls == 1, "Losing the pool discarded a pending mana update");
        assertSyncedMana(h, charger, 1_000);
        ticks(charger, 20);
        h.assertTrue(charger.syncCalls == 1, "Missing pool repeated an unchanged pending update");

        var restored = restore(h, charger, charger.saveWithoutMetadata(h.getLevel().registryAccess()));
        ticks(restored, 20);
        h.assertTrue(restored.syncCalls == 0 && mana(restored.getItem()) == 1_000, "Reload without a pool changed or retransmitted inventory");
        h.getLevel().setBlockAndUpdate(charger.getBlockPos().below(),
                BotaniaBlocks.MANA_POOL.defaultBlockState().setValue(BotaniaStateProperties.OUTPUTTING, true));
        restored.getPool().receiveMana(2_000);
        ticks(restored, 1);
        h.assertTrue(mana(restored.getItem()) == 2_000 && restored.getPool().getCurrentMana() == 1_000,
                "Reloaded charger failed to resume its original transfer rate");
        h.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void powerFramePreservesLinkedBellowsRateAndSkipsIdleScans(GameTestHelper h) {
        var framePos = new BlockPos(1, 1, 1);
        var poolPos = framePos.above();
        h.setBlock(framePos, ExtraBotanyBlocks.powerFrame);
        h.setBlock(poolPos, BotaniaBlocks.MANA_POOL.defaultBlockState().setValue(BotaniaStateProperties.OUTPUTTING, true));
        for (var direction : List.of(Direction.EAST, Direction.WEST, Direction.NORTH)) {
            h.setBlock(poolPos.relative(direction), BotaniaBlocks.MANATIDE_BELLOWS.defaultBlockState()
                    .setValue(BlockStateProperties.HORIZONTAL_FACING,
                            direction == Direction.NORTH ? direction : direction.getOpposite()));
        }
        var frame = new CountingPowerFrame(h.absolutePos(framePos));
        frame.setLevel(h.getLevel());
        h.getLevel().setBlockEntity(frame);
        var pool = frame.getPool();
        pool.receiveMana(20_000);
        ticks(frame, 20);
        h.assertTrue(frame.bellowsScans == 0, "Empty Power Frame still scans adjacent block entities");
        frame.setItem(new ItemStack(BotaniaItems.MANA_TABLET));
        ticks(frame, 1);
        h.assertTrue(mana(frame.getItem()) == 3_000 && pool.getCurrentMana() == 17_000,
                "Power Frame did not apply exactly two linked bellows to its original transfer rate");
        for (var direction : List.of(Direction.EAST, Direction.WEST)) {
            var bellows = (BellowsBlockEntity) h.getLevel().getBlockEntity(h.absolutePos(poolPos.relative(direction)));
            h.assertTrue(bellows.getUpdateTag(h.getLevel().registryAccess()).getBoolean("active"), "Linked bellows was not activated");
        }
        var unlinked = (BellowsBlockEntity) h.getLevel().getBlockEntity(h.absolutePos(poolPos.north()));
        h.assertTrue(!unlinked.getUpdateTag(h.getLevel().registryAccess()).getBoolean("active"), "Unlinked bellows was activated");
        h.getLevel().setBlockAndUpdate(pool.getBlockPos(), pool.getBlockState().setValue(BotaniaStateProperties.OUTPUTTING, false));
        ticks(frame, 1);
        h.assertTrue(mana(frame.getItem()) == 0 && pool.getCurrentMana() == 20_000, "Bellows-assisted draining changed rate or conservation");
        ticks(frame, 20);
        h.assertTrue(frame.bellowsScans == 2, "Empty item still scans bellows while its pool is draining");
        h.succeed();
    }

    private static CountingCharger create(GameTestHelper h, boolean poolPresent, int poolMana) {
        var relative = new BlockPos(1, 2, 1);
        if (poolPresent) {
            h.setBlock(relative.below(), BotaniaBlocks.MANA_POOL.defaultBlockState().setValue(BotaniaStateProperties.OUTPUTTING, true));
            h.setBlock(relative, ExtraBotanyBlocks.manaCharger);
        }
        var charger = new CountingCharger(h.absolutePos(relative));
        charger.setLevel(h.getLevel());
        if (poolPresent) {
            h.getLevel().setBlockEntity(charger);
            charger.getPool().receiveMana(poolMana);
        }
        return charger;
    }

    private static int mana(ItemStack stack) {
        return EXplatAbstractions.INSTANCE.findManaItem(stack).getMana();
    }

    private static void ticks(ChargerBlockEntity charger, int count) {
        for (int i = 0; i < count; i++) {
            ChargerBlockEntity.serverTick(charger.getLevel(), charger.getBlockPos(), charger.getBlockState(), charger);
        }
    }

    private static final class CountingPowerFrame extends PowerFrameBlockEntity {
        private int bellowsScans;

        private CountingPowerFrame(BlockPos pos) {
            super(pos, ExtraBotanyBlocks.powerFrame.defaultBlockState());
        }

        @Override
        public List<BellowsBlockEntity> getBellows(Level level, BlockPos poolPos, ManaPoolBlockEntity pool) {
            bellowsScans++;
            return super.getBellows(level, poolPos, pool);
        }
    }

    private static CountingCharger restore(GameTestHelper h, CountingCharger original, CompoundTag tag) {
        var restored = new CountingCharger(original.getBlockPos());
        restored.setLevel(h.getLevel());
        restored.loadWithComponents(tag, h.getLevel().registryAccess());
        return restored;
    }

    private static void assertSyncedMana(GameTestHelper h, CountingCharger charger, int expected) {
        h.assertTrue(charger.lastPacket != null && charger.lastPacket.equals(charger.lastUpdateTag),
                "Block entity packet and initial chunk update expose different inventory data");
        var client = restore(h, charger, charger.lastPacket);
        h.assertTrue(mana(client.getItem()) == expected && client.getItem().getCount() == 1,
                "Client inventory snapshot lost the final mana or stack count");
        h.assertTrue(Float.compare(client.getChargeProcess(), charger.getChargeProcess()) == 0,
                "Client charge display differs from the synchronized server state");
    }

    private static final class CountingCharger extends ManaChargerBlockEntity {
        private int syncCalls;
        private int dirtyCalls;
        private CompoundTag lastPacket;
        private CompoundTag lastUpdateTag;

        private CountingCharger(BlockPos pos) {
            super(pos, ExtraBotanyBlocks.manaCharger.defaultBlockState());
        }

        @Override
        public void setChanged() {
            dirtyCalls++;
            super.setChanged();
        }

        @Override
        protected void syncItem() {
            syncCalls++;
            lastPacket = ((ClientboundBlockEntityDataPacket) getUpdatePacket()).getTag().copy();
            lastUpdateTag = getUpdateTag(getLevel().registryAccess());
            super.syncItem();
        }
    }
}
