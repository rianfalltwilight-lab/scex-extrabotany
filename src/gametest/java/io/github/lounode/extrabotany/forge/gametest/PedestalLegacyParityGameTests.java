package io.github.lounode.extrabotany.forge.gametest;

import io.github.lounode.extrabotany.common.block.ExtraBotanyBlocks;
import io.github.lounode.extrabotany.common.block.block_entity.PedestalBlockEntity;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.xplat.EXplatAbstractions;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.block.block_entity.mana.ManaPoolBlockEntity;

import java.util.ArrayList;
import java.util.List;

@GameTestHolder("extrabotany")
@PrefixGameTestTemplate(false)
public final class PedestalLegacyParityGameTests {
    @GameTest(template = "empty", timeoutTicks = 20)
    public static void nightmareFuelConvertsAfterSixThousandDayTicksWithoutIdleUpdates(GameTestHelper h) {
        long previousTime = h.getLevel().getDayTime();
        try {
            daytime(h, 1000);
            var pedestal = create(h);
            // The old rule checks daytime, not direct sunlight or a multiblock.
            h.setBlock(new BlockPos(4, 4, 4), Blocks.STONE);
            pedestal.setItem(new ItemStack(ExtraBotanyItems.nightmareFuel));
            pedestal.dirtyCalls = 0;
            ticks(pedestal, 5999);
            h.assertTrue(pedestal.getItem().is(ExtraBotanyItems.nightmareFuel) && pedestal.dirtyCalls == 0,
                    "Daylight progress changed the item early or queued unchanged inventory updates");
            ticks(pedestal, 1);
            h.assertTrue(pedestal.getItem().is(ExtraBotanyItems.spiritFuel) && pedestal.getItem().getCount() == 1,
                    "The 6000th daytime tick must turn nightmare fuel into one spirit fuel");
            var tag = pedestal.getUpdateTag(h.getLevel().registryAccess());
            var restored = create(h);
            restored.loadWithComponents(tag, h.getLevel().registryAccess());
            h.assertTrue(restored.getItem().is(ExtraBotanyItems.spiritFuel), "Converted fuel was missing from the client/save snapshot");
            int changed = pedestal.dirtyCalls;
            ticks(pedestal, 40);
            h.assertTrue(pedestal.dirtyCalls == changed, "Finished fuel kept scheduling idle updates");
        } finally {
            daytime(h, previousTime);
        }
        h.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void nightPausesFuelProgressAndRemovalOrReloadResetsIt(GameTestHelper h) {
        long previousTime = h.getLevel().getDayTime();
        try {
            daytime(h, 1000);
            var pedestal = create(h);
            pedestal.setItem(new ItemStack(ExtraBotanyItems.nightmareFuel));
            ticks(pedestal, 5999);
            daytime(h, 18000);
            ticks(pedestal, 40);
            h.assertTrue(pedestal.getItem().is(ExtraBotanyItems.nightmareFuel), "Night advanced the original daytime-only timer");
            daytime(h, 1000);
            ticks(pedestal, 1);
            h.assertTrue(pedestal.getItem().is(ExtraBotanyItems.spiritFuel), "Night reset the accumulated daytime progress");

            pedestal.setItem(new ItemStack(ExtraBotanyItems.nightmareFuel));
            ticks(pedestal, 5999);
            pedestal.setItem(ItemStack.EMPTY);
            ticks(pedestal, 1);
            pedestal.setItem(new ItemStack(ExtraBotanyItems.nightmareFuel));
            ticks(pedestal, 1);
            h.assertTrue(pedestal.getItem().is(ExtraBotanyItems.nightmareFuel), "Removed fuel leaked its progress into a later item");
            ticks(pedestal, 5998);
            var saved = pedestal.saveWithoutMetadata(h.getLevel().registryAccess());
            pedestal.loadWithComponents(saved, h.getLevel().registryAccess());
            ticks(pedestal, 1);
            h.assertTrue(pedestal.getItem().is(ExtraBotanyItems.nightmareFuel), "Transient 1.12.2 daylight timer survived reload");
            ticks(pedestal, 5999);
            h.assertTrue(pedestal.getItem().is(ExtraBotanyItems.spiritFuel), "Reloaded fuel did not restart its full daytime period");
        } finally {
            daytime(h, previousTime);
        }
        h.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void fullNatureOrbDoesNotDrainTheFourPools(GameTestHelper h) {
        var pedestal = create(h);
        pedestal.setTier(2);
        List<ManaPoolBlockEntity> pools = new ArrayList<>();
        for (var offset : PedestalBlockEntity.POOL_LOCATIONS) {
            BlockPos relative = new BlockPos(4, 3, 4).offset(offset);
            h.setBlock(relative, BotaniaBlocks.MANA_POOL);
            ManaPoolBlockEntity pool = h.getBlockEntity(relative);
            pool.receiveMana(1000);
            pools.add(pool);
        }
        pedestal.setItem(new ItemStack(ExtraBotanyItems.natureOrb));
        var orb = EXplatAbstractions.INSTANCE.findNatureEnergyItem(pedestal.getItem());
        orb.addEnergy(orb.getMaxEnergy());
        pedestal.dirtyCalls = 0;
        ticks(pedestal, 40);
        h.assertTrue(pools.stream().allMatch(pool -> pool.getCurrentMana() == 1000) && pedestal.dirtyCalls == 0,
                "A full nature orb kept consuming pool mana or queuing unchanged updates");
        orb.addEnergy(-17);
        ticks(pedestal, 1);
        h.assertTrue(orb.getEnergy() == orb.getMaxEnergy() && pools.stream().allMatch(pool -> pool.getCurrentMana() == 990),
                "Valid tier-two charging changed its original 9 + 4*2 energy / 4*10 mana exchange");
        ticks(pedestal, 20);
        h.assertTrue(pools.stream().allMatch(pool -> pool.getCurrentMana() == 990), "Completed orb resumed draining the pools");
        h.succeed();
    }

    private static void daytime(GameTestHelper h, long time) {
        h.getLevel().setDayTime(time);
        h.getLevel().updateSkyBrightness();
    }

    private static CountingPedestal create(GameTestHelper h) {
        BlockPos relative = new BlockPos(4, 3, 4);
        h.setBlock(relative, ExtraBotanyBlocks.livingrockPedestal);
        var pedestal = new CountingPedestal(h.absolutePos(relative));
        pedestal.setLevel(h.getLevel());
        return pedestal;
    }

    private static void ticks(CountingPedestal pedestal, int count) {
        for (int i = 0; i < count; i++) {
            PedestalBlockEntity.serverTick(pedestal.getLevel(), pedestal.getBlockPos(), pedestal.getBlockState(), pedestal);
        }
    }

    private static final class CountingPedestal extends PedestalBlockEntity {
        private int dirtyCalls;
        private CountingPedestal(BlockPos pos) { super(pos, ExtraBotanyBlocks.livingrockPedestal.defaultBlockState()); }
        @Override public void setChanged() { dirtyCalls++; super.setChanged(); }
        @Override public void updateTier() { }
        @Override public void updateAutomaticHammers() { }
    }
}
