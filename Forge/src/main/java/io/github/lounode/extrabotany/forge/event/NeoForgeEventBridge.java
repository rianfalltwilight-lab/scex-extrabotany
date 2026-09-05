package io.github.lounode.extrabotany.forge.event;

import net.minecraft.world.InteractionResult;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.PlayLevelSoundEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import io.github.lounode.extrabotany.common.block.flower.generating.ResoncundBlockEntity;
import io.github.lounode.extrabotany.common.brew.effect.HealReverseMobEffect;
import io.github.lounode.extrabotany.common.brew.effect.LinkMobEffect;
import io.github.lounode.extrabotany.common.brew.effect.ThirrorMobEffect;
import io.github.lounode.extrabotany.common.brew.effect.WarmMobEffect;
import io.github.lounode.extrabotany.common.impl.WindImpl;
import io.github.lounode.extrabotany.common.item.NightmareFuelItem;
import io.github.lounode.extrabotany.common.item.SpiritFuelItem;
import io.github.lounode.extrabotany.common.item.equipment.armor.goblin_slayer.GoblinSlayerHelmetItem;
import io.github.lounode.extrabotany.common.item.equipment.armor.pleiades_combat_maid.PleiadesCombatMaidSuitItem;
import io.github.lounode.extrabotany.common.item.equipment.armor.pleiades_combat_maid.SanguinePleiadesCombatMaidSuitItem;
import io.github.lounode.extrabotany.common.item.equipment.armor.shadow_warrior.ShadowWarriorHelmetItem;
import io.github.lounode.extrabotany.common.item.equipment.bauble.FeatherOfJingweiItem;
import io.github.lounode.extrabotany.common.item.equipment.bauble.NatureOrbItem;
import io.github.lounode.extrabotany.common.item.equipment.bauble.PureDaisyPendantItem;
import io.github.lounode.extrabotany.common.item.equipment.shield.ManasteelShieldItem;
import io.github.lounode.extrabotany.common.item.equipment.shield.ElementiumShieldItem;
import io.github.lounode.extrabotany.common.item.equipment.tool.hammer.ElementiumHammerItem;
import io.github.lounode.extrabotany.common.item.equipment.tool.hammer.RheinHammerItem;
import io.github.lounode.extrabotany.common.item.relic.AchillesShieldItem;
import io.github.lounode.extrabotany.common.item.relic.ExcaliburItem;
import io.github.lounode.extrabotany.common.item.relic.void_archives.variants.Excalibur;
import io.github.lounode.extrabotany.common.item.relic.voidcore.CoreOfTheVoidItem;
import io.github.lounode.extrabotany.common.telemetry.ExtraBotanyTelemetry;

/**
 * Routes NeoForge events into the behavior handlers inherited from the MIT
 * cross-platform baseline. Keeping the routing explicit prevents accidental
 * double registration and makes the logical-side contract testable.
 */
public final class NeoForgeEventBridge {
    private NeoForgeEventBridge() {}

    public static void register(IEventBus bus) {
        bus.addListener(io.github.lounode.extrabotany.forge.ForgeExtrabotanyCommands::register);
        bus.addListener(LegacyAccessoryEvents::discount);
        bus.addListener(NeoForgeEventBridge::onLevelLoad);
        bus.addListener(NeoForgeEventBridge::onLevelUnload);
        bus.addListener(NeoForgeEventBridge::onServerStarted);
        bus.addListener(NeoForgeEventBridge::onServerStopping);
        bus.addListener(NeoForgeEventBridge::onFurnaceFuel);
        bus.addListener(NeoForgeEventBridge::onIncomingDamage);
        bus.addListener(NeoForgeEventBridge::onDamagePost);
        bus.addListener(NeoForgeEventBridge::onLivingHeal);
        bus.addListener(NeoForgeEventBridge::onLivingDeath);
        bus.addListener(NeoForgeEventBridge::onEffectApplicable);
        bus.addListener(NeoForgeEventBridge::onEffectAdded);
        bus.addListener(NeoForgeEventBridge::onEffectRemoved);
        bus.addListener(NeoForgeEventBridge::onEffectExpired);
        bus.addListener(NeoForgeEventBridge::onShieldBlock);
        bus.addListener(NeoForgeEventBridge::onAttackEntity);
        bus.addListener(NeoForgeEventBridge::onLeftClickEmpty);
        bus.addListener((PlayerInteractEvent.LeftClickBlock event) -> {
            if (event.getHand() == net.minecraft.world.InteractionHand.MAIN_HAND
                    && !(event.getItemStack().getItem() instanceof io.github.lounode.extrabotany.common.item.legacy.LegacySubspaceSpearItem)
                    && event.getItemStack().getItem() instanceof io.github.lounode.extrabotany.common.item.legacy.LegacyRelicSword sword)
                sword.tryUse(event.getEntity(), null);
        });
        bus.addListener(NeoForgeEventBridge::onRightClickBlock);
        bus.addListener(NeoForgeEventBridge::onBreakSpeed);
        bus.addListener(NeoForgeEventBridge::onItemAttributeModifier);
        bus.addListener(NeoForgeEventBridge::onPlayerLoggedOut);
        bus.addListener(NeoForgeEventBridge::onPlayerTickPost);
        bus.addListener(NeoForgeEventBridge::onSoundAtPosition);
        bus.addListener(NeoForgeEventBridge::onSoundAtEntity);
    }

    private static void onLevelLoad(LevelEvent.Load event) {
        WindImpl.EventHandler.onLevelLoad(event);
    }

    private static void onLevelUnload(LevelEvent.Unload event) {
        WindImpl.EventHandler.onLevelUnLoad(event);
    }

    private static void onServerStarted(ServerStartedEvent event) {
        ExtraBotanyTelemetry.onServerStarted(event);
    }

    private static void onServerStopping(ServerStoppingEvent event) {
        ExtraBotanyTelemetry.onServerStopping(event);
    }

    private static void onFurnaceFuel(FurnaceFuelBurnTimeEvent event) {
        SpiritFuelItem.makeFuel(event);
        NightmareFuelItem.makeFuel(event);
    }

    private static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getSource().is(net.minecraft.tags.DamageTypeTags.IS_FIRE)
                && !vazkii.botania.common.handler.EquipmentHandler.findOrEmpty(
                io.github.lounode.extrabotany.common.item.legacy.LegacyCelestialAccessories.MOON, event.getEntity()).isEmpty()) {
            event.setAmount(0); event.setCanceled(true); return;
        }
        if (event.getEntity() instanceof net.minecraft.world.entity.player.Player
                && event.getEntity().hasEffect(io.github.lounode.extrabotany.common.brew.ExtraBotanyMobEffects.ETERNITY)) {
            event.setAmount(0);
            event.setCanceled(true);
            return;
        }
        LegacyAccessoryEvents.protect(event);
        if (event.isCanceled()) return;
        LegacyAccessoryEvents.crown(event);
        if (!event.getEntity().level().isClientSide() && event.getSource().getEntity() instanceof net.minecraft.world.entity.player.Player player
                && event.getSource().getDirectEntity() == player && io.github.lounode.extrabotany.common.item.legacy.LegacyFlamescionItem.mode(player)) {
            player.level().addFreshEntity(io.github.lounode.extrabotany.common.entity.LegacyFlameArea.create(
                    io.github.lounode.extrabotany.common.entity.LegacyFlameArea.Kind.SLASH, player, event.getEntity().position().add(0, 1, 0)));
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(io.github.lounode.extrabotany.common.brew.ExtraBotanyMobEffects.INCANDESCENCE, 30));
            event.getEntity().addEffect(new net.minecraft.world.effect.MobEffectInstance(io.github.lounode.extrabotany.common.brew.ExtraBotanyMobEffects.TIMELOCK, 30));
        }
        WarmMobEffect.EventHandler.onEntityHurt(event);
        ThirrorMobEffect.EventHandler.onLivingAttack(event);
        CoreOfTheVoidItem.onLivingAttack(event);
        if (event.isCanceled()) {
            return;
        }
        LinkMobEffect.onEntityDamaged(event);
        ShadowWarriorHelmetItem.EventHandler.onPlayerAttacked(event);
        GoblinSlayerHelmetItem.EventHandler.onPlayerAttack(event);
        PleiadesCombatMaidSuitItem.EventHandler.onEntityAttacked(event);
        PleiadesCombatMaidSuitItem.EventHandler.onPlayerAttacked(event);
        CoreOfTheVoidItem.onLivingHurt(event);
        if (!event.isCanceled()) {
            CoreOfTheVoidItem.onLivingDamage(event);
        }
    }

    private static void onDamagePost(LivingDamageEvent.Post event) {
        SanguinePleiadesCombatMaidSuitItem.EventHandler.onAttackLiving(event);
    }

    private static void onLivingHeal(LivingHealEvent event) {
        HealReverseMobEffect.onLivingHeal(event);
        var curse = event.getEntity().getEffect(io.github.lounode.extrabotany.common.brew.ExtraBotanyMobEffects.WITCH_CURSE);
        if (!Float.isFinite(event.getAmount()) || event.getAmount() <= 0) event.setAmount(0);
        else if (curse != null) event.setAmount(event.getAmount() / Math.max(1, curse.getAmplifier()));
        if (event.getEntity() instanceof net.minecraft.world.entity.player.Player player) {
            var eternity = vazkii.botania.common.handler.EquipmentHandler.findOrEmpty(
                    io.github.lounode.extrabotany.common.item.legacy.LegacyEternityItem.INSTANCE, player);
            if (!eternity.isEmpty() && io.github.lounode.extrabotany.common.item.legacy.LegacyEternityItem.owned(eternity, player)) event.setAmount(0);
        }
    }

    private static void onLivingDeath(LivingDeathEvent event) {
        SanguinePleiadesCombatMaidSuitItem.EventHandler.onKilled(event);
        LegacyAccessoryEvents.rescue(event);
    }

    private static void onEffectApplicable(MobEffectEvent.Applicable event) {
        CoreOfTheVoidItem.onEffectAdd(event);
    }

    private static void onEffectAdded(MobEffectEvent.Added event) {
        SanguinePleiadesCombatMaidSuitItem.EventHandler.onEffectAdded(event);
    }

    private static void onEffectRemoved(MobEffectEvent.Remove event) {
        if (event.getEffectInstance() != null) {
            SanguinePleiadesCombatMaidSuitItem.EventHandler.onEffectRemove(event);
        }
    }

    private static void onEffectExpired(MobEffectEvent.Expired event) {
        SanguinePleiadesCombatMaidSuitItem.EventHandler.onEffectExpired(event);
    }

    private static void onShieldBlock(LivingShieldBlockEvent event) {
        ManasteelShieldItem.EventHandler.onShieldBlockDamage(event);
    }

    private static void onAttackEntity(AttackEntityEvent event) {
        var player = event.getEntity();
        io.github.lounode.extrabotany.common.item.legacy.LegacyFlamescionItem.attack(player);
        if (player.getMainHandItem().getItem() instanceof io.github.lounode.extrabotany.common.item.legacy.LegacyRelicSword sword)
            sword.tryUse(player, event.getTarget());
        var stack = player.getMainHandItem();
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer && !player.isSpectator()
                && player.getAttackStrengthScale(0) == 1 && stack.is(io.github.lounode.extrabotany.common.item.legacy.LegacyTools.KATANA)
                && event.getTarget() instanceof net.minecraft.world.entity.LivingEntity target && target.isAlive()
                && vazkii.botania.api.mana.ManaItemHandler.instance().requestManaExactForTool(stack, player, 100, true)) {
            io.github.lounode.extrabotany.common.item.legacy.LegacyTools.spawnPixie(serverPlayer, target);
        }
        ExcaliburItem.attackEntity(event);
        FeatherOfJingweiItem.attackEntity(event);
        Excalibur.attackEntity(event);
    }

    private static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        if (event.getItemStack().getItem() instanceof io.github.lounode.extrabotany.common.item.legacy.LegacyRelicSword
                || event.getItemStack().is(io.github.lounode.extrabotany.common.item.legacy.LegacyFlamescionItem.INSTANCE))
            net.neoforged.neoforge.network.PacketDistributor.sendToServer(io.github.lounode.extrabotany.forge.network.LegacySwordPacket.INSTANCE);
        ExcaliburItem.leftClick(event);
        FeatherOfJingweiItem.leftClick(event);
        Excalibur.leftClick(event);
    }

    private static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        NatureOrbItem.onPlayerInteract(event);
        PureDaisyPendantItem.EventHandler.onPlayerInteract(event);
        if (event.isCanceled()) {
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }

    private static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        RheinHammerItem.onDig(event);
    }

	private static void onItemAttributeModifier(ItemAttributeModifierEvent event) {
		ExcaliburItem.onModifyAttributes(event);
		AchillesShieldItem.onModifyAttributes(event);
		ElementiumShieldItem.onModifyAttributes(event);
		ElementiumHammerItem.onModifyAttributes(event);
	}

    private static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        CoreOfTheVoidItem.playerLoggedOut(event);
    }

    private static void onPlayerTickPost(PlayerTickEvent.Post event) {
        io.github.lounode.extrabotany.common.item.legacy.LegacyFlamescionItem.tickPlayer(event.getEntity());
        CoreOfTheVoidItem.updatePlayerFlyStatus(event.getEntity());
        var player = event.getEntity();
        var eternity = player.getEffect(io.github.lounode.extrabotany.common.brew.ExtraBotanyMobEffects.ETERNITY);
        if (eternity != null && eternity.getDuration() < 115) {
            var velocity = player.getDeltaMovement();
            player.setDeltaMovement(velocity.x * .25, 0, velocity.z * .25);
        }
    }

    private static void onSoundAtPosition(PlayLevelSoundEvent.AtPosition event) {
        for (ResoncundBlockEntity listener : ResoncundBlockEntity.listeners()) {
            listener.onPlayLevelSound(event);
        }
    }

    private static void onSoundAtEntity(PlayLevelSoundEvent.AtEntity event) {
        for (ResoncundBlockEntity listener : ResoncundBlockEntity.listeners()) {
            listener.onPlayLevelSound(event);
        }
    }
}
