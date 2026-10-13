package io.github.lounode.extrabotany.forge.gametest;

import io.github.lounode.extrabotany.common.brew.ExtraBotanyMobEffects;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.item.lens.SuperconductorLens;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.botania.api.mana.BurstProperties;
import java.util.ArrayList;
import java.util.List;

@GameTestHolder("extrabotany")
@PrefixGameTestTemplate(false)
public final class FullLegacyGameTests {
    @GameTest(template = "empty", timeoutTicks = 20)
    public static void allLegacyEntitiesConstructAndReload(GameTestHelper helper) {
        int count = 0;
        for (var entry : BuiltInRegistries.ENTITY_TYPE.entrySet()) {
            if (!entry.getKey().location().getNamespace().equals("extrabotany")) continue;
            var entity = entry.getValue().create(helper.getLevel());
            helper.assertTrue(entity != null, "Entity factory returned null: " + entry.getKey().location());
            entity.setPos(helper.absolutePos(new BlockPos(2, 5, 2)).getCenter());
            var saved = new CompoundTag(); entity.save(saved);
            var loaded = net.minecraft.world.entity.EntityType.loadEntityRecursive(saved, helper.getLevel(), restored -> restored);
            helper.assertTrue(loaded != null && loaded.getType() == entity.getType() && loaded.getUUID().equals(entity.getUUID()), "Entity reload lost identity: " + entry.getKey().location());
            count++;
        }
        helper.assertTrue(count >= 34, "Old saves use 34 entity types and all of them must exist, found " + count);
        helper.succeed();
    }
    @GameTest(template = "empty", timeoutTicks = 20)
    public static void relicSwordOwnershipManaAndDuplicateAttack(GameTestHelper helper) {
        var garden = new ItemStack(ExtraBotanyItems.kingGarden);
        var flower = new ItemStack(io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks.bellflower);
        for (int i = 0; i < 20; i++) helper.assertTrue(io.github.lounode.extrabotany.common.item.KingGardenItem.addFlower(garden, flower), "Garden refused slot " + i);
        helper.assertTrue(!io.github.lounode.extrabotany.common.item.KingGardenItem.addFlower(garden, flower), "Garden exceeded twenty slots");
        var loaded = ItemStack.parseOptional(helper.getLevel().registryAccess(), (CompoundTag) garden.save(helper.getLevel().registryAccess()));
        helper.assertTrue(io.github.lounode.extrabotany.common.item.KingGardenItem.getTypes(loaded).length == 20
                && java.util.Arrays.stream(io.github.lounode.extrabotany.common.item.KingGardenItem.getTypes(loaded)).allMatch(type -> type == 15), "Garden flower configuration changed on reload");
        helper.succeed();
    }
    @GameTest(template = "empty", timeoutTicks = 20)
    public static void restoredLegacyStacksAndBinder(GameTestHelper helper) {
        var names = new ArrayList<>(List.of("aero_stone", "aqua_stone", "earth_stone", "ignis_stone", "the_community", "power_glove", "peace_amulet", "potato_chips", "death_ring", "frost_star", "gem_of_conquest"));
        names.addAll(List.of("true_terrablade", "true_shadow_katana", "influx_waver", "star_wrath", "first_fractal", "spear_of_subspace")); names.addAll(List.of("motor", "cosmic_car_key", "motor_accessory", "cosmic_car_key_accessory"));
        names.addAll(List.of("king_garden", "silver_bullet", "sun_ring", "moon_pendant", "all_for_one", "elven_king", "judah_oath", "judah_oath_kira", "judah_oath_sakura", "flamescion_weapon", "voidcaller"));
        names.addAll(List.of("fox_ear", "fox_mask", "pylon", "goggle_jungle", "goggle_ocean", "goggle_snowfield", "goggle_standard", "black_glasses", "thug_life", "super_crown", "mask", "red_scarf")); names.addAll(List.of("miku_helm", "miku_chest", "miku_legs", "miku_boots", "shootingguardian_helm", "shootingguardian_chest", "shootingguardian_legs", "shootingguardian_boots", "silentsages_helm", "silentsages_chest", "silentsages_legs", "silentsages_boots")); names.addAll(List.of("rod_of_discord", "bottled_pixie", "shadow_katana", "uuz_fan", "bottled_star", "photon_shotgun"));
        names.addAll(List.of("binder", "universal_petal", "empty_core_of_the_void", "mana_drink", "lens_superconductor", "bottled_flame", "silent_eternity", "limited_edition_supply_bag"));
        for (String name : names) {
            var old = new CompoundTag(); old.putString("id", "extrabotany:" + name); old.putInt("count", 4);
            var components = new CompoundTag(); var payload = new CompoundTag();
            payload.putString("legacy_marker", "opaque data survives"); payload.putIntArray("old_array", new int[] {1, -3, 99});
            components.put("minecraft:custom_data", payload); old.put("components", components);
            var stack = ItemStack.parseOptional(helper.getLevel().registryAccess(), old);
            helper.assertTrue(!stack.isEmpty() && stack.getCount() == 4 && BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().equals("extrabotany:" + name), "Legacy stack lost: " + name);
            var restored = ItemStack.parseOptional(helper.getLevel().registryAccess(), (CompoundTag) stack.save(helper.getLevel().registryAccess()));
            helper.assertTrue(ItemStack.matches(stack, restored) && restored.get(DataComponents.CUSTOM_DATA).copyTag().equals(payload), "Components changed: " + name);
        }
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var binder = new ItemStack(ExtraBotanyItems.binder);
        CustomData.update(DataComponents.CUSTOM_DATA, binder, tag -> tag.putString("unknown", "retain"));
        player.setItemInHand(InteractionHand.MAIN_HAND, binder); player.setShiftKeyDown(true);
        var position = helper.absolutePos(new BlockPos(2, 1, 2));
        binder.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, new BlockHitResult(position.getCenter(), Direction.UP, position, false)));
        var restored = ItemStack.parseOptional(helper.getLevel().registryAccess(), (CompoundTag) binder.save(helper.getLevel().registryAccess()));
        var tag = restored.get(DataComponents.CUSTOM_DATA).copyTag();
        helper.assertTrue(tag.getInt("posx") == position.getX() && tag.getInt("posy") == position.getY() && tag.getInt("posz") == position.getZ()
                && tag.getString("dim").equals(helper.getLevel().dimension().location().toString()) && tag.getString("unknown").equals("retain"), "Binder mutation/reload lost coordinates or unknown field");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void supplyPoolSurvivesPartialDrawAndReload(GameTestHelper helper) {
        var data = new io.github.lounode.extrabotany.common.item.SupplyBagItem.PoolData();
        var uuid = java.util.UUID.fromString("9453a240-76cb-4db8-b44d-e4267a4d2079");
        var random = RandomSource.create(90210);
        helper.assertTrue(data.draw(uuid, random).is(ExtraBotanyItems.emptyCoreOfTheVoid), "First bag is not guaranteed core");
        var counts = new java.util.TreeMap<String, Integer>();
        for (int i = 0; i < 101; i++) {
            if (i == 43) data = io.github.lounode.extrabotany.common.item.SupplyBagItem.PoolData.load(data.save(new CompoundTag(), helper.getLevel().registryAccess()));
            var reward = data.draw(uuid, random);
            helper.assertTrue(!reward.isEmpty(), "Finite pool returned an empty reward");
            counts.merge(BuiltInRegistries.ITEM.getKey(reward.getItem()).toString(), reward.getCount(), Integer::sum);
        }
        helper.assertTrue(counts.equals(java.util.Map.of("extrabotany:silent_eternity", 1, "extrabotany:lens_superconductor", 4,
                "extrabotany:hero_medal", 10, "extrabotany:vier_reward_bag", 40, "extrabotany:drei_reward_bag", 80,
                "extrabotany:zwei_reward_bag", 180, "extrabotany:nine_and_three_quarters_reward_bag", 30,
                "minecraft:diamond", 60, "extrabotany:empty_core_of_the_void", 1)), "Pool reset, duplicated reward, or lost progress: " + counts);
        var extra = data.draw(uuid, random); helper.assertTrue(extra.is(net.minecraft.world.item.Items.EMERALD) && extra.getCount() == 2, "An empty pool did not give 2 emeralds");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void drinkLensAndEffectBehavior(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL); player.setHealth(10);
        var drink = new ItemStack(ExtraBotanyItems.manaDrink);
        var bottle = drink.finishUsingItem(helper.getLevel(), player);
        helper.assertTrue(bottle.is(ExtraBotanyItems.manaGlassBottle) && player.getHealth() == 15, "Drink lost healing or empty bottle");
        for (var effect : List.of(MobEffects.DAMAGE_RESISTANCE, MobEffects.DAMAGE_BOOST, MobEffects.MOVEMENT_SPEED, MobEffects.JUMP))
            helper.assertTrue(player.getEffect(effect) != null && player.getEffect(effect).getDuration() == 1200, "Drink effect missing");
        var burst = new BurstProperties(100, 50, 2, .5F, 2, 0);
        new SuperconductorLens().apply(new ItemStack(ExtraBotanyItems.lensSuperconductor), burst);
        helper.assertTrue(burst.maxMana == 800 && burst.ticksBeforeManaLoss == 40 && burst.manaLossPerTick == 32
                && burst.motionModifier == 3 && burst.gravity == .5F && burst.color == 0x5555FF, "Lens burst contract changed");
        player.setHealth(10); player.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.WITCH_CURSE, 100, 4)); player.heal(8);
        helper.assertTrue(player.getHealth() == 12, "Witch curse is registered but healing hook is missing");
        player.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.ETERNITY, 10)); player.hurt(player.damageSources().generic(), 5);
        helper.assertTrue(player.getHealth() == 12, "Eternity damage protection missing");
        helper.succeed();
    }
}
