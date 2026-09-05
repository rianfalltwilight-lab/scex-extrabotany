package io.github.lounode.extrabotany.forge.gametest;

import io.github.lounode.extrabotany.common.brew.ExtraBotanyMobEffects;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.item.legacy.*;
import io.github.lounode.extrabotany.common.item.lens.SuperconductorLens;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
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
    public static void legacyEnergyConversionConservesResources(GameTestHelper helper) {
        var level = helper.getLevel(); var pos = helper.absolutePos(new BlockPos(2, 3, 2));
        var kind = io.github.lounode.extrabotany.common.block.legacy.LegacyMachineBlock.Kind.LIQUEFACTION;
        level.setBlockAndUpdate(pos, io.github.lounode.extrabotany.common.block.legacy.LegacyMachineBlock.BLOCKS.get(kind).defaultBlockState());
        level.setBlockAndUpdate(pos.east(), io.github.lounode.extrabotany.common.block.ExtraBotanyBlocks.livingrockBarrel.defaultBlockState());
        var machine = (io.github.lounode.extrabotany.common.block.legacy.LegacyMachineEntity) level.getBlockEntity(pos);
        var tank = (io.github.lounode.extrabotany.common.block.legacy.LegacyBarrelEntity) level.getBlockEntity(pos.east());
        tank.fill(new net.neoforged.neoforge.fluids.FluidStack(io.github.lounode.extrabotany.forge.fluid.LegacyManaFluid.SOURCE, 100), net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
        machine.serverTick();
        helper.assertTrue(machine.getCurrentMana() == 1000 && machine.getEnergyStored() == 0 && tank.getFluidInTank(0).getAmount() == 99, "Fluid-to-mana conversion lost resources");
        level.setBlockAndUpdate(pos.below(), net.minecraft.world.level.block.Blocks.REDSTONE_BLOCK.defaultBlockState());
        machine.serverTick(); machine.serverTick();
        helper.assertTrue(machine.getCurrentMana() == 0 && machine.getEnergyStored() == 0 && tank.getFluidInTank(0).getAmount() == 100, "Powered reverse conversion duplicated/lost resources");
        var generatorKind = io.github.lounode.extrabotany.common.block.legacy.LegacyMachineBlock.Kind.GENERATOR;
        var generatorPos = pos.offset(0, 3, 0); var state = io.github.lounode.extrabotany.common.block.legacy.LegacyMachineBlock.BLOCKS.get(generatorKind).defaultBlockState();
        level.setBlockAndUpdate(generatorPos, state);
        var generator = (io.github.lounode.extrabotany.common.block.legacy.LegacyMachineEntity) level.getBlockEntity(generatorPos);
        var tag = new CompoundTag(); tag.putInt("energy", 1000); generator.loadWithComponents(tag, level.registryAccess()); generator.serverTick();
        helper.assertTrue(generator.getCurrentMana() == 99 && generator.getEnergyStored() == 0, "Generator FE conversion differs from legacy 1000:99");
        tag.putInt("mana", generator.getMaxMana()); generator.loadWithComponents(tag, level.registryAccess()); generator.serverTick();
        helper.assertTrue(generator.getEnergyStored() == 1000 && generator.isFull(), "Full generator consumed FE without output capacity");
        helper.succeed();
    }
    @GameTest(template = "empty", timeoutTicks = 20)
    public static void flamescionOverloadAndUltimateAreServerControlled(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL); player.setPos(helper.absolutePos(new BlockPos(2, 7, 2)).getCenter()); player.setOnGround(false);
        var stack = new ItemStack(LegacyFlamescionItem.INSTANCE); player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        player.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.INCANDESCENCE, 1000));
        for (int i = 0; i < 300; i++) LegacyFlamescionItem.tickPlayer(player);
        helper.assertTrue(LegacyFlamescionItem.energy(stack) == 600 && !LegacyFlamescionItem.overloaded(stack), "Flamescion charging rate changed");
        LegacyFlamescionItem.tickPlayer(player);
        helper.assertTrue(LegacyFlamescionItem.overloaded(stack) && LegacyFlamescionItem.energy(stack) == 597 && !LegacyFlamescionItem.mode(player), "Flamescion overload transition changed");
        for (int i = 0; i < 200; i++) LegacyFlamescionItem.tickPlayer(player);
        helper.assertTrue(!LegacyFlamescionItem.overloaded(stack) && LegacyFlamescionItem.energy(stack) == 0, "Flamescion overload failed to recover");
        LegacyFlamescionItem.ultimate(player); var area = player.getBoundingBox().inflate(12);
        var attacks = helper.getLevel().getEntitiesOfClass(io.github.lounode.extrabotany.common.entity.LegacyFlameArea.class, area, entity -> entity.getOwner() == player);
        helper.assertTrue(attacks.size() == 1 && LegacyFlamescionItem.overloaded(stack) && player.hasEffect(ExtraBotanyMobEffects.TIMELOCK), "Ultimate did not spawn or apply its cost");
        LegacyFlamescionItem.ultimate(player);
        helper.assertTrue(helper.getLevel().getEntitiesOfClass(io.github.lounode.extrabotany.common.entity.LegacyFlameArea.class, area, entity -> entity.getOwner() == player).size() == 1, "Repeated ultimate bypassed overload");
        var restored = ItemStack.parseOptional(helper.getLevel().registryAccess(), (CompoundTag) stack.save(helper.getLevel().registryAccess()));
        helper.assertTrue(LegacyFlamescionItem.energy(restored) == 600 && LegacyFlamescionItem.overloaded(restored), "Flamescion energy state lost on reload");
        helper.succeed();
    }
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
        helper.assertTrue(count == 34, "Expected the complete 34-entity legacy registry, got " + count);
        helper.succeed();
    }
    @GameTest(template = "empty", timeoutTicks = 20)
    public static void guardianPhasePersistenceAndAttackFormations(GameTestHelper helper) throws ReflectiveOperationException {
        var level = helper.getLevel();
        var home = helper.absolutePos(new BlockPos(2, 1, 2));
        var gaia = new io.github.lounode.extrabotany.common.entity.gaia.GaiaIII(level, home);
        gaia.setPos(home.getCenter()); gaia.setNoAi(true);
        var old = gaia.saveWithoutId(new CompoundTag()); old.putInt("BigPhase", 2); old.putInt("EgoStage", 1);
        old.putBoolean("InFogBarrage", true); old.putInt("FogWaveIndex", 3); old.putInt("FogWaveTimer", 47); old.putInt("FlickerCooldown", 119);
        gaia.load(old);
        var saved = gaia.saveWithoutId(new CompoundTag());
        for (var key : List.of("BigPhase", "EgoStage", "InFogBarrage", "FogWaveIndex", "FogWaveTimer", "FlickerCooldown"))
            helper.assertTrue(old.get(key).equals(saved.get(key)), "Gaia III phase state lost: " + key);
        var method = gaia.getClass().getDeclaredMethod("spawnFogWave", int.class); method.setAccessible(true);
        var area = new net.minecraft.world.phys.AABB(home).inflate(40);
        int[] expected = {64, 80, 72, 80, 128, 102};
        for (int wave = 0; wave < expected.length; wave++) {
            method.invoke(gaia, wave);
            var mines = level.getEntitiesOfClass(io.github.lounode.extrabotany.common.entity.SkullLandMineEntity.class, area, mine -> mine.getOwner() == gaia);
            helper.assertTrue(mines.size() == expected[wave], "Incorrect fog wave " + wave + ": " + mines.size());
            mines.forEach(net.minecraft.world.entity.Entity::discard);
        }
        var boss = new io.github.lounode.extrabotany.common.entity.gaia.LegacyVoidHerrscher(io.github.lounode.extrabotany.common.entity.gaia.LegacyVoidHerrscher.TYPE, level);
        var bossTag = boss.saveWithoutId(new CompoundTag()); bossTag.putInt("ShieldLayers", 2); bossTag.putInt("RotatingShields", 3);
        bossTag.putBoolean("RankII", true); bossTag.putBoolean("RankIII", true); bossTag.putFloat("HerrscherDamageTaken", 31.5F);
        bossTag.putInt("HerrscherTpDelay", 41); bossTag.putInt("DodgeCd", 117); bossTag.putInt("SkillCd", 89); bossTag.putInt("SkillType", 2);
        bossTag.putInt("SupportCd", 211); bossTag.putBoolean("EmergeLanceDone", true);
        var supporters = new net.minecraft.nbt.ListTag(); supporters.add(net.minecraft.nbt.StringTag.valueOf("Nyx")); bossTag.put("Supporters", supporters);
        boss.load(bossTag); saved = boss.saveWithoutId(new CompoundTag());
        for (var key : List.of("ShieldLayers", "RotatingShields", "RankII", "RankIII", "HerrscherDamageTaken", "HerrscherTpDelay", "DodgeCd", "SkillCd", "SkillType", "SupportCd", "EmergeLanceDone", "Supporters"))
            helper.assertTrue(bossTag.get(key).equals(saved.get(key)), "Herrscher state lost: " + key);
        helper.assertTrue(boss.isRankIIRenderState() && boss.getRotatingShieldsRenderState() == 3 && Math.abs(boss.getDamageCap() - 25 * .85F * .85F * .85F) < .0001F, "Shield render/damage state disagrees after reload");
        var missile = new io.github.lounode.extrabotany.common.entity.SkullMissileEntity(level, gaia);
        missile.setDamage(7); missile.setTrueDamage(3); missile.setEffect(true); missile.setFire(true);
        var reloaded = new io.github.lounode.extrabotany.common.entity.SkullMissileEntity(io.github.lounode.extrabotany.common.entity.ExtraBotanyEntityType.SKULL_MISSILE, level);
        reloaded.load(missile.saveWithoutId(new CompoundTag()));
        helper.assertTrue(reloaded.getDamage() == 7 && reloaded.getTrueDamage() == 3 && reloaded.hasEffect() && reloaded.isFire(), "Herrscher missile lost damage/effects on reload");
        helper.succeed();
    }
    @GameTest(template = "empty", timeoutTicks = 20)
    public static void relicSwordOwnershipManaAndDuplicateAttack(GameTestHelper helper) {
        for (var entry : LegacyRelicSword.ITEMS.entrySet()) {
            var player = helper.makeMockPlayer(GameType.SURVIVAL);
            player.setPos(helper.absolutePos(new BlockPos(2, 4, 2)).getCenter());
            var sword = entry.getValue();
            var stack = new ItemStack(sword); player.setItemInHand(InteractionHand.MAIN_HAND, stack);
            var tablet = new ItemStack(vazkii.botania.common.item.BotaniaItems.MANA_TABLET);
            var mana = vazkii.botania.api.mana.ManaItem.LOOKUP.find(tablet);
            mana.addMana(25000); player.getInventory().setItem(2, tablet);
            for (int i = 0; i < 30; i++) player.tick();
            var relic = io.github.lounode.extrabotany.xplat.EXplatAbstractions.INSTANCE.findRelic(stack);
            helper.assertTrue(relic != null, "Relic capability missing: " + entry.getKey());
            relic.bindToUUID(java.util.UUID.randomUUID());
            int before = mana.getMana();
            helper.assertTrue(!sword.tryUse(player, null) && mana.getMana() == before, "Wrong owner consumed mana or fired");
            relic.bindToUUID(player.getUUID());
            helper.assertTrue(sword.tryUse(player, null), "Bound full-strength sword failed: " + entry.getKey());
            int expected = switch (entry.getKey()) { case "true_terrablade" -> 400; case "true_shadow_katana" -> 800; case "first_fractal" -> 640; case "spear_of_subspace" -> 600; default -> 500; };
            helper.assertTrue(before - mana.getMana() == expected, "Incorrect mana payment: " + entry.getKey());
            helper.assertTrue(!sword.tryUse(player, null) && before - mana.getMana() == expected, "Duplicate same-tick attack fired or charged twice");
        }
        var garden = new ItemStack(LegacyKingGardenItem.INSTANCE);
        var flower = new ItemStack(io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks.bellflower);
        for (int i = 0; i < 20; i++) helper.assertTrue(LegacyKingGardenItem.addFlower(garden, flower), "Garden refused slot " + i);
        helper.assertTrue(!LegacyKingGardenItem.addFlower(garden, flower), "Garden exceeded twenty slots");
        var loaded = ItemStack.parseOptional(helper.getLevel().registryAccess(), (CompoundTag) garden.save(helper.getLevel().registryAccess()));
        helper.assertTrue(LegacyKingGardenItem.types(loaded).length == 20
                && java.util.Arrays.stream(LegacyKingGardenItem.types(loaded)).allMatch(type -> type == 15), "Garden flower configuration changed on reload");
        helper.succeed();
    }
    @GameTest(template = "empty", timeoutTicks = 20)
    public static void barrelTransactionsAndCocoonHatchAfterReload(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(2, 2, 2));
        var state = io.github.lounode.extrabotany.common.block.ExtraBotanyBlocks.livingrockBarrel.defaultBlockState();
        var barrel = new io.github.lounode.extrabotany.common.block.legacy.LegacyBarrelEntity(pos, state);
        var old = new CompoundTag(); old.putString("fluid", "extrabotany:fluidedmana"); old.putInt("amount", 15333);
        barrel.loadWithComponents(old, level.registryAccess());
        var mana = io.github.lounode.extrabotany.forge.fluid.LegacyManaFluid.SOURCE;
        var simulate = net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.SIMULATE;
        var execute = net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE;
        helper.assertTrue(barrel.fill(new net.neoforged.neoforge.fluids.FluidStack(mana, 1000), simulate) == 667
                && barrel.getFluidInTank(0).getAmount() == 15333, "Fluid simulation mutated tank");
        helper.assertTrue(barrel.fill(new net.neoforged.neoforge.fluids.FluidStack(net.minecraft.world.level.material.Fluids.WATER, 1000), execute) == 0,
                "Barrel mixed unlike fluids");
        helper.assertTrue(barrel.drain(333, execute).getAmount() == 333, "Barrel drain lost fluid");
        var reloaded = (io.github.lounode.extrabotany.common.block.legacy.LegacyBarrelEntity)
                net.minecraft.world.level.block.entity.BlockEntity.loadStatic(pos, state, barrel.saveWithFullMetadata(level.registryAccess()), level.registryAccess());
        helper.assertTrue(reloaded != null && reloaded.getFluidInTank(0).getFluid() == mana
                && reloaded.getFluidInTank(0).getAmount() == 15000, "Barrel save changed fluid or amount");
        var cocoonState = io.github.lounode.extrabotany.common.block.ExtraBotanyBlocks.cocoonOfDesire.defaultBlockState();
        level.setBlockAndUpdate(pos, cocoonState);
        var cocoon = (io.github.lounode.extrabotany.common.block.legacy.LegacyCocoonEntity) level.getBlockEntity(pos);
        var oldCocoon = new CompoundTag(); oldCocoon.putInt("timePassed", 1199); oldCocoon.putInt("Rot", 347);
        oldCocoon.put("Item", new ItemStack(net.minecraft.world.item.Items.LEATHER).save(level.registryAccess()));
        cocoon.loadWithComponents(oldCocoon, level.registryAccess());
        var saved = cocoon.saveWithFullMetadata(level.registryAccess());
        cocoon.loadWithComponents(saved, level.registryAccess());
        cocoon.serverTick();
        helper.assertTrue(cocoon.getItem().isEmpty() && !level.getEntitiesOfClass(net.minecraft.world.entity.animal.Cow.class,
                new net.minecraft.world.phys.AABB(pos).inflate(2)).isEmpty(), "Reloaded cocoon failed to hatch or duplicate-consumed material");
        helper.succeed();
    }
    @GameTest(template = "empty", timeoutTicks = 20)
    public static void machineTransfersAndLotusReload(GameTestHelper helper) {
        var level = helper.getLevel();
        var base = helper.absolutePos(new BlockPos(2, 2, 2));
        var blocks = io.github.lounode.extrabotany.common.block.legacy.LegacyMachineBlock.BLOCKS;
        var kind = io.github.lounode.extrabotany.common.block.legacy.LegacyMachineBlock.Kind.BUFFER;
        for (var pos : List.of(base, base.below(), base.above())) level.setBlockAndUpdate(pos, blocks.get(kind).defaultBlockState());
        var source = (io.github.lounode.extrabotany.common.block.legacy.LegacyMachineEntity) level.getBlockEntity(base.below());
        var middle = (io.github.lounode.extrabotany.common.block.legacy.LegacyMachineEntity) level.getBlockEntity(base);
        var target = (io.github.lounode.extrabotany.common.block.legacy.LegacyMachineEntity) level.getBlockEntity(base.above());
        source.receiveMana(12345);
        middle.serverTick();
        helper.assertTrue(source.getCurrentMana() == 11345 && middle.getCurrentMana() == 0 && target.getCurrentMana() == 1000,
                "Buffer transfer direction, speed or conservation changed");
        var saved = target.saveWithFullMetadata(level.registryAccess());
        var loaded = (io.github.lounode.extrabotany.common.block.legacy.LegacyMachineEntity)
                net.minecraft.world.level.block.entity.BlockEntity.loadStatic(base.above(), target.getBlockState(), saved, level.registryAccess());
        helper.assertTrue(loaded != null && loaded.getCurrentMana() == 1000, "Buffer save lost mana");
        loaded.receiveMana(Integer.MAX_VALUE);
        helper.assertTrue(loaded.getCurrentMana() == 64000000, "Buffer capacity overflow");
        var fluid = io.github.lounode.extrabotany.forge.fluid.LegacyManaFluid.SOURCE;
        helper.assertTrue(BuiltInRegistries.FLUID.getKey(fluid).toString().equals("extrabotany:fluidedmana")
                && fluid.getBucket() == io.github.lounode.extrabotany.forge.fluid.LegacyManaFluid.BUCKET,
                "Legacy fluid/bucket linkage lost");
        var lotusState = io.github.lounode.extrabotany.common.block.flower.functional.LegacyLotusBlocks.FLOWER.defaultBlockState();
        var lotus = new io.github.lounode.extrabotany.common.block.flower.functional.StardustLotusBlockEntity(base, lotusState);
        var old = new CompoundTag();
        old.putBoolean("hasTarget", true); old.putInt("targetX", 37); old.putInt("targetY", 83); old.putInt("targetZ", -49);
        old.putBoolean("hasPaper", true); old.putInt("consumedMana", 18763);
        lotus.loadWithComponents(old, level.registryAccess());
        var restored = (io.github.lounode.extrabotany.common.block.flower.functional.StardustLotusBlockEntity)
                net.minecraft.world.level.block.entity.BlockEntity.loadStatic(base, lotusState, lotus.saveWithFullMetadata(level.registryAccess()), level.registryAccess());
        helper.assertTrue(restored != null && restored.hasPaper() && restored.getConsumedMana() == 18763
                && restored.getTarget().equals(new BlockPos(37, 83, -49)), "Lotus lost paid ticket, charge or destination on reload");
        helper.succeed();
    }
    @GameTest(template = "empty", timeoutTicks = 20)
    public static void restoredLegacyStacksAndBinder(GameTestHelper helper) {
        var names = new ArrayList<>(LegacyAccessories.ITEMS.keySet());
        names.addAll(LegacyRelicSword.ITEMS.keySet()); names.addAll(LegacyMountItems.ITEMS.keySet());
        names.addAll(List.of("king_garden", "silver_bullet", "sun_ring", "moon_pendant", "all_for_one", "elven_king", "judah_oath", "judah_oath_kira", "judah_oath_sakura", "flamescion_weapon", "voidcaller"));
        names.addAll(LegacyCosmetics.ITEMS.keySet()); names.addAll(LegacyArmorItem.ITEMS.keySet()); names.addAll(LegacyTools.ITEMS.keySet());
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
        var data = new LegacySupplyBagItem.PoolData();
        var uuid = java.util.UUID.fromString("9453a240-76cb-4db8-b44d-e4267a4d2079");
        var random = RandomSource.create(90210);
        helper.assertTrue(data.draw(uuid, random).is(ExtraBotanyItems.emptyCoreOfTheVoid), "First bag is not guaranteed core");
        var counts = new java.util.TreeMap<String, Integer>();
        for (int i = 0; i < 101; i++) {
            if (i == 43) data = LegacySupplyBagItem.PoolData.load(data.save(new CompoundTag(), helper.getLevel().registryAccess()));
            var reward = data.draw(uuid, random);
            helper.assertTrue(!reward.isEmpty(), "Finite pool returned an empty reward");
            counts.merge(BuiltInRegistries.ITEM.getKey(reward.getItem()).toString(), reward.getCount(), Integer::sum);
        }
        helper.assertTrue(counts.equals(java.util.Map.of("extrabotany:silent_eternity", 1, "extrabotany:lens_superconductor", 4,
                "extrabotany:hero_medal", 10, "extrabotany:vier_reward_bag", 40, "extrabotany:drei_reward_bag", 80,
                "extrabotany:zwei_reward_bag", 180, "extrabotany:nine_and_three_quarters_reward_bag", 30,
                "minecraft:diamond", 60, "extrabotany:empty_core_of_the_void", 1)), "Pool reset, duplicated reward, or lost progress: " + counts);
        helper.assertTrue(!data.draw(uuid, random).isEmpty(), "Exhausted pool did not refill");
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
