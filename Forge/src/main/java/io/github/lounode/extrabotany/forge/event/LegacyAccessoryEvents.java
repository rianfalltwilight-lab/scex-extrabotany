package io.github.lounode.extrabotany.forge.event;

import io.github.lounode.extrabotany.common.entity.gaia.Gaia;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.item.legacy.LegacyAccessories;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import vazkii.botania.api.neoforge.mana.ManaDiscountEvent;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.handler.EquipmentHandler;

public final class LegacyAccessoryEvents {
    private LegacyAccessoryEvents() {}

    public static void crown(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player) || EquipmentHandler.findOrEmpty(
                io.github.lounode.extrabotany.common.item.legacy.LegacyCosmetics.ITEMS.get("super_crown"), player).isEmpty()) return;
        var choices = java.util.List.of(
                java.util.List.of(ExtraBotanyItems.starryIdolHeadgear, ExtraBotanyItems.pleiadesCombatMaidHeadgear),
                java.util.List.of(ExtraBotanyItems.starryIdolSuit, ExtraBotanyItems.pleiadesCombatMaidSuit, ExtraBotanyItems.sanguinePleiadesCombatMaidSuit),
                java.util.List.of(ExtraBotanyItems.starryIdolSkirt, ExtraBotanyItems.pleiadesCombatMaidSkirt),
                java.util.List.of(ExtraBotanyItems.starryIdolBoots, ExtraBotanyItems.pleiadesCombatMaidBoots));
        var slots = java.util.List.of(net.minecraft.world.entity.EquipmentSlot.HEAD, net.minecraft.world.entity.EquipmentSlot.CHEST,
                net.minecraft.world.entity.EquipmentSlot.LEGS, net.minecraft.world.entity.EquipmentSlot.FEET);
        for (int i = 0; i < slots.size(); i++) if (!choices.get(i).contains(player.getItemBySlot(slots.get(i)).getItem())) return;
        event.setAmount(Math.max(0, event.getAmount() - 2));
    }

    public static void discount(ManaDiscountEvent event) {
        if (LegacyAccessories.worn("aqua_stone", event.getEntityPlayer())
                || LegacyAccessories.worn("the_community", event.getEntityPlayer())) event.setDiscount(event.getDiscount() + .1F);
    }

    public static void protect(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof Player attacker)) return;
        boolean available = event.getSource().is(TagKey.create(Registries.DAMAGE_TYPE,
                ResourceLocation.fromNamespaceAndPath("extrabotany", "peace_amulet_available")))
                || ExtraBotanyItems.ALL.containsValue(attacker.getMainHandItem().getItem())
                || ExtraBotanyItems.ALL.containsValue(attacker.getOffhandItem().getItem());
        if (!available) return;
        var target = event.getEntity();
        boolean protectedTarget = target instanceof Player || target instanceof Mob && !(target instanceof Enemy);
        if (LegacyAccessories.worn("peace_amulet", attacker) && protectedTarget
                || target instanceof Player && LegacyAccessories.worn("peace_amulet", target)) {
            event.setAmount(0);
            event.setCanceled(true);
        }
    }

    public static void rescue(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide()) return;
        var chips = EquipmentHandler.findOrEmpty(LegacyAccessories.ITEMS.get("potato_chips"), player);
        if (chips.isEmpty() || player.getCooldowns().isOnCooldown(chips.getItem())
                || !ManaItemHandler.instance().requestManaExactForTool(chips, player, 3000, true)) return;
        event.setCanceled(true);
        player.level().playSound(null, player.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1, 1);
        player.setHealth(5);
        player.removeAllEffects();
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 900, 1));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1));
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 800, 0));
        var source = event.getSource().getEntity();
        player.getCooldowns().addCooldown(chips.getItem(), source instanceof EnderDragon
                || source instanceof WitherBoss || source instanceof Gaia ? 12000 : 600);
    }
}
