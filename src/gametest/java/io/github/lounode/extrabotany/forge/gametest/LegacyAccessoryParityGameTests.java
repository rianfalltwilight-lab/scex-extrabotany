package io.github.lounode.extrabotany.forge.gametest;

import io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.item.equipment.bauble.DispersiveRingItem;
import io.github.lounode.extrabotany.common.item.relic.voidcore.CoreOfTheVoidItem;
import io.github.lounode.extrabotany.xplat.EXplatAbstractions;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.botania.api.block_entity.FunctionalFlowerBlockEntity;
import vazkii.botania.api.mana.ManaItem;
import vazkii.botania.common.item.BotaniaItems;

import java.util.List;
import java.util.UUID;

@GameTestHolder("extrabotany")
@PrefixGameTestTemplate(false)
public final class LegacyAccessoryParityGameTests {
    private LegacyAccessoryParityGameTests() {}

    private static ManaItem supplyMana(Player player, int amount) {
        ItemStack tablet = new ItemStack(BotaniaItems.MANA_TABLET);
        ManaItem mana = ManaItem.LOOKUP.find(tablet);
        mana.addMana(amount);
        player.getInventory().setItem(2, tablet);
        return mana;
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void foreignVoidCoreCannotCureOrChargeTheWearer(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var mana = supplyMana(player, 1000);
        var core = (CoreOfTheVoidItem) ExtraBotanyItems.coreOfTheVoid;
        ItemStack stack = new ItemStack(core);
        EXplatAbstractions.INSTANCE.findRelic(stack).bindToUUID(UUID.randomUUID());
        player.addEffect(new MobEffectInstance(MobEffects.POISON, 600));
        core.onWornTick(stack, player);
        helper.assertTrue(player.hasEffect(MobEffects.POISON), "A foreign relic bypassed the potion ownership gate");
        helper.assertTrue(mana.getMana() == 1000, "Foreign relic spent the wearer's mana on a forbidden cure");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void ownedVoidCoreCuresOnceAfterSaveAndRequiresEnoughMana(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var mana = supplyMana(player, 1000);
        var core = (CoreOfTheVoidItem) ExtraBotanyItems.coreOfTheVoid;
        ItemStack stack = new ItemStack(core);
        EXplatAbstractions.INSTANCE.findRelic(stack).bindToUUID(player.getUUID());
        stack = ItemStack.parseOptional(helper.getLevel().registryAccess(),
                (CompoundTag) stack.save(helper.getLevel().registryAccess()));
        helper.assertTrue(EXplatAbstractions.INSTANCE.findRelic(stack).isRightPlayer(player),
                "Relic ownership did not survive item reload");
        player.addEffect(new MobEffectInstance(MobEffects.POISON, 600));
        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 600));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600));
        core.onWornTick(stack, player);
        helper.assertTrue(!player.hasEffect(MobEffects.POISON) && !player.hasEffect(MobEffects.WEAKNESS)
                && player.hasEffect(MobEffects.MOVEMENT_SPEED), "Cure failed or removed a beneficial effect");
        helper.assertTrue(mana.getMana() == 800, "The cure must charge once for all active harmful effects");
        core.onWornTick(stack, player);
        helper.assertTrue(mana.getMana() == 800, "An idle core charged for an absent harmful effect");
        mana.addMana(199 - mana.getMana());
        player.addEffect(new MobEffectInstance(MobEffects.POISON, 600));
        core.onWornTick(stack, player);
        helper.assertTrue(player.hasEffect(MobEffects.POISON) && mana.getMana() == 199,
                "An underfunded cure removed an effect or consumed partial payment");
        helper.succeed();
    }

    private static FunctionalFlowerBlockEntity flower(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos.below(), Blocks.GRASS_BLOCK);
        helper.setBlock(pos, ExtrabotanyFlowerBlocks.necrofleur);
        return helper.getBlockEntity(pos);
    }

    @GameTest(template = "projectile_arena", timeoutTicks = 20)
    public static void dispersiveRingUsesLegacyBoundsAndConservesMana(GameTestHelper helper) {
        BlockPos center = new BlockPos(16, 16, 16);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setPos(helper.absolutePos(center).getCenter());
        var mana = supplyMana(player, 500000);
        var ring = (DispersiveRingItem) ExtraBotanyItems.dispersiveRing;
        var stack = new ItemStack(ring);
        var inside = List.of(flower(helper, center.offset(-7, -7, -7)),
                flower(helper, center.offset(6, 6, 6)));
        var outside = List.of(flower(helper, center.offset(-8, 0, 0)), flower(helper, center.offset(7, 0, 0)),
                flower(helper, center.offset(0, -8, 0)), flower(helper, center.offset(0, 7, 0)),
                flower(helper, center.offset(0, 0, -8)), flower(helper, center.offset(0, 0, 7)));
        player.tickCount = 19;
        ring.onWornTick(stack, player);
        helper.assertTrue(inside.stream().allMatch(f -> f.getMana() == 0) && mana.getMana() == 500000,
                "The retained twenty-tick transfer interval changed");
        player.tickCount = 20;
        ring.onWornTick(stack, player);
        int paid = inside.stream().mapToInt(FunctionalFlowerBlockEntity::getMaxMana).sum();
        helper.assertTrue(inside.stream().allMatch(f -> f.getMana() == f.getMaxMana())
                && outside.stream().allMatch(f -> f.getMana() == 0), "Transfer crossed the legacy [-7, +6] bounds");
        helper.assertTrue(mana.getMana() == 500000 - paid, "Ring did not conserve the transferred mana");
        ring.onWornTick(stack, player);
        helper.assertTrue(mana.getMana() == 500000 - paid, "Full flowers drained additional mana");
        helper.succeed();
    }

    @GameTest(template = "projectile_arena", timeoutTicks = 20)
    public static void dispersiveRingKeepsLegacyPriorityWhenManaIsScarce(GameTestHelper helper) {
        BlockPos center = new BlockPos(16, 16, 16);
        var first = flower(helper, center.offset(-6, 0, 6));
        var second = flower(helper, center.offset(0, 0, -6));
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setPos(helper.absolutePos(center).getCenter());
        var mana = supplyMana(player, first.getMaxMana());
        player.tickCount = 20;
        var ring = (DispersiveRingItem) ExtraBotanyItems.dispersiveRing;
        ring.onWornTick(new ItemStack(ring), player);
        helper.assertTrue(first.getMana() == first.getMaxMana() && second.getMana() == 0 && mana.getMana() == 0,
                "Scarce mana must fill flowers in legacy X/Y/Z order, not stream Z/Y/X order");
        helper.succeed();
    }
}
