package io.github.lounode.extrabotany.forge.gametest;

import io.github.lounode.extrabotany.common.entity.gaia.GaiaIII;
import io.github.lounode.extrabotany.common.entity.gaia.behavior.GaiaDisarm;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import top.theillusivec4.curios.api.CuriosApi;

@GameTestHolder("extrabotany")
@PrefixGameTestTemplate(false)
public final class GaiaDisarmGameTests {
    private static final class Disarm extends GaiaDisarm<GaiaIII> {
        void apply(Player player) { disArm(player); }
        void step(net.minecraft.server.level.ServerLevel level, GaiaIII gaia) { tick(level, gaia, level.getGameTime()); }
    }
    private static ItemStack forbidden(GameTestHelper helper) {
        var id = ResourceLocation.parse("patchouli:guide_book");
        helper.assertTrue(BuiltInRegistries.ITEM.containsKey(id), "Missing real third-party test item");
        var stack = new ItemStack(BuiltInRegistries.ITEM.get(id));
        var tag = new CompoundTag(); tag.putString("scex_disarm", "preserve-me");
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return stack;
    }
    @GameTest(template = "empty", timeoutTicks = 20)
    public static void equippedItemsDropButStoredItemsRemain(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setPos(helper.absolutePos(new BlockPos(2, 3, 2)).getCenter());
        var original = forbidden(helper);
        player.getInventory().setItem(12, original.copy());
        player.getInventory().setItem(1, original.copy());
        player.setItemSlot(EquipmentSlot.MAINHAND, original.copy());
        player.setItemSlot(EquipmentSlot.OFFHAND, original.copy());
        player.setItemSlot(EquipmentSlot.HEAD, original.copy());
        player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.DIAMOND_CHESTPLATE));
        var disarm = new Disarm(); disarm.apply(player);
        helper.assertTrue(player.getMainHandItem().isEmpty() && player.getOffhandItem().isEmpty()
                && player.getItemBySlot(EquipmentSlot.HEAD).isEmpty(), "Forbidden equipment retained");
        helper.assertTrue(player.getInventory().getItem(12).getCount() == 1 && player.getInventory().getItem(1).getCount() == 1,
                "Stored/unselected items were removed");
        helper.assertTrue(player.getItemBySlot(EquipmentSlot.CHEST).is(Items.DIAMOND_CHESTPLATE), "Allowed armor removed");
        var drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(2));
        helper.assertTrue(drops.size() == 3 && drops.stream().allMatch(drop -> drop.hasPickUpDelay()
                && ItemStack.isSameItemSameComponents(drop.getItem(), original)), "Drop count/components/pickup delay mismatch");
        disarm.apply(player);
        helper.assertTrue(helper.getLevel().getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(2)).size() == 3,
                "Repeated disarm duplicated drops");
        player.getInventory().selected = 1; disarm.apply(player);
        helper.assertTrue(player.getMainHandItem().isEmpty() && player.getInventory().getItem(12).getCount() == 1,
                "Selecting a stored forbidden item did not disarm it");
        helper.succeed();
    }
    @GameTest(template = "empty", timeoutTicks = 20)
    public static void curiosEquipmentDropsAndCreativeIsExempt(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setPos(helper.absolutePos(new BlockPos(2, 3, 2)).getCenter());
        var inventory = CuriosApi.getCuriosInventory(player).orElseThrow();
        var handler = inventory.getCurios().values().stream().filter(h -> h.getStacks().getSlots() > 0).findFirst().orElseThrow();
        var original = forbidden(helper);
        handler.getStacks().setStackInSlot(0, original.copy());
        new Disarm().apply(player);
        helper.assertTrue(handler.getStacks().getStackInSlot(0).isEmpty(), "Curios equipment retained");
        var drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(2));
        helper.assertTrue(drops.size() == 1 && ItemStack.isSameItemSameComponents(drops.getFirst().getItem(), original),
                "Curios item lost or duplicated");
        var creative = helper.makeMockPlayer(GameType.CREATIVE);
        creative.setItemSlot(EquipmentSlot.MAINHAND, original.copy()); new Disarm().apply(creative);
        helper.assertTrue(!creative.getMainHandItem().isEmpty(), "Creative exemption broken");
        helper.succeed();
    }
    @GameTest(template = "empty", timeoutTicks = 20)
    public static void cancelledDropKeepsOriginalAndConfigDisablesDisarm(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setPos(helper.absolutePos(new BlockPos(2, 3, 2)).getCenter());
        var original = forbidden(helper); player.setItemSlot(EquipmentSlot.MAINHAND, original.copy());
        java.util.function.Consumer<net.neoforged.neoforge.event.entity.EntityJoinLevelEvent> cancel = event -> {
            if (event.getEntity() instanceof ItemEntity item && item.distanceToSqr(player) < 4
                    && ItemStack.isSameItemSameComponents(item.getItem(), original)) event.setCanceled(true);
        };
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(cancel);
        try {
            new Disarm().apply(player);
            helper.assertTrue(ItemStack.matches(player.getMainHandItem(), original), "Cancelled drop destroyed the original");
        } finally { net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(cancel); }
        boolean enabled = io.github.lounode.extrabotany.xplat.ExtraBotanyConfig.common().guardianItemCheck();
        try {
            io.github.lounode.extrabotany.forge.ForgeExtrabotanyConfig.setGuardianItemCheck(false);
            new Disarm().apply(player);
            helper.assertTrue(ItemStack.matches(player.getMainHandItem(), original), "Disabled itemcheck still disarmed");
        } finally { io.github.lounode.extrabotany.forge.ForgeExtrabotanyConfig.setGuardianItemCheck(enabled); }
        helper.succeed();
    }
    @GameTest(template = "empty", timeoutTicks = 40)
    public static void summonWithForbiddenCurioAndCombatDisarm(GameTestHelper helper) {
        var level = helper.getLevel();
        // Separate arena, clear of other concurrently running test structures.
        var center = helper.absolutePos(new BlockPos(0, 4, 180));
        for (int x = -16; x <= 16; x++) for (int z = -16; z <= 16; z++) {
            for (int y = 0; y <= GaiaIII.ARENA_HEIGHT; y++) level.setBlockAndUpdate(center.offset(x, y, z), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            level.setBlockAndUpdate(center.offset(x, -1, z), net.minecraft.world.level.block.Blocks.IRON_BLOCK.defaultBlockState());
        }
        level.setBlockAndUpdate(center, net.minecraft.world.level.block.Blocks.BEACON.defaultBlockState());
        for (var offset : io.github.lounode.extrabotany.api.gaia.GaiaArena.PYLON_LOCATIONS)
            level.setBlockAndUpdate(center.offset(offset), vazkii.botania.common.block.BotaniaBlocks.GAIA_PYLON.defaultBlockState());
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setPos(center.offset(2, 0, 0).getCenter());
        level.addFreshEntity(player);
        var original = forbidden(helper); player.getInventory().setItem(12, original.copy());
        player.setItemSlot(EquipmentSlot.MAINHAND, original.copy());
        var handler = CuriosApi.getCuriosInventory(player).orElseThrow().getCurios().values().stream()
                .filter(h -> h.getStacks().getSlots() > 0).findFirst().orElseThrow();
        handler.getStacks().setStackInSlot(0, original.copy());
        var arena = io.github.lounode.extrabotany.api.gaia.GaiaArena.of(net.minecraft.core.GlobalPos.of(level.dimension(), center), GaiaIII.ARENA_RANGE, GaiaIII.ARENA_HEIGHT);
        helper.assertTrue(vazkii.botania.common.helper.PlayerHelper.isTruePlayer(player), "Test player rejected as fake");
        helper.assertTrue(arena.checkStructure(level, io.github.lounode.extrabotany.api.gaia.GaiaArena.ARENA_PATTERN), "Invalid test altar");
        helper.assertTrue(arena.checkArea(level).isEmpty(), "Invalid test arena floor/clearance");
        helper.assertTrue(!io.github.lounode.extrabotany.api.gaia.GaiaArena.checkGuardianInventoryPass(player), "Fixture would not trigger old gate");
        var difficulty = level.getDifficulty();
        try {
            level.getServer().getWorldData().setDifficulty(net.minecraft.world.Difficulty.NORMAL);
            helper.assertTrue(GaiaIII.spawn(player, new ItemStack(io.github.lounode.extrabotany.common.item.ExtraBotanyItems.challengeTicket), level, center),
                    "Summon failed: difficulty=" + level.getDifficulty() + " beacon=" + level.getBlockEntity(center)
                            + " otherGaia=" + arena.countGaiaAround(level, io.github.lounode.extrabotany.common.entity.gaia.Gaia.class)
                            + " botaniaGaia=" + arena.countGaiaAround(level, vazkii.botania.common.entity.GaiaGuardianEntity.class));
        } finally { level.getServer().getWorldData().setDifficulty(difficulty); }
        var gaia = level.getEntitiesOfClass(GaiaIII.class, arena.getArenaBB()).getFirst();
        gaia.getBrain().setMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.NEAREST_PLAYERS, java.util.List.of(player));
        new Disarm().step(level, gaia);
        helper.assertTrue(player.getMainHandItem().isEmpty() && handler.getStacks().getStackInSlot(0).isEmpty()
                && player.getInventory().getItem(12).getCount() == 1, "Summoned Gaia did not apply equipment-only disarm");
        gaia.discard(); player.discard(); helper.succeed();
    }
}
