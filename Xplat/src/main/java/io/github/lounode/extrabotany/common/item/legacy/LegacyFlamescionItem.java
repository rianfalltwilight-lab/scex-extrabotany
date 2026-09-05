package io.github.lounode.extrabotany.common.item.legacy;

import io.github.lounode.extrabotany.common.brew.ExtraBotanyMobEffects;
import io.github.lounode.extrabotany.common.entity.LegacyFlameArea;
import io.github.lounode.extrabotany.common.entity.LegacyFlameProjectile;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public final class LegacyFlamescionItem extends SwordItem {
    public static final LegacyFlamescionItem INSTANCE = new LegacyFlamescionItem();
    private static final Map<Player, Long> LAST_ATTACK = new WeakHashMap<>();
    private LegacyFlamescionItem() { super(Tiers.NETHERITE, new Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant().attributes(SwordItem.createAttributes(Tiers.NETHERITE, 5, -1.6F))); }
    public static int energy(ItemStack stack) { return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt("flamescion_energy"); }
    public static boolean overloaded(ItemStack stack) { return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getBoolean("flamescion_overloaded"); }
    public static void energy(ItemStack stack, int amount) { CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putInt("flamescion_energy", Math.clamp(amount, 0, 600))); }
    public static void overloaded(ItemStack stack, boolean value) { CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putBoolean("flamescion_overloaded", value)); }
    public static boolean mode(Player player) { return !player.onGround() && player.getMainHandItem().is(INSTANCE)
            && player.hasEffect(ExtraBotanyMobEffects.INCANDESCENCE) && !overloaded(player.getMainHandItem()); }
    public static void tickPlayer(Player player) {
        if (player.level().isClientSide()) return;
        var stack = player.getMainHandItem(); if (!stack.is(INSTANCE)) return;
        if (mode(player)) { if (energy(stack) < 600) energy(stack, energy(stack) + 2); else overloaded(stack, true); }
        if (overloaded(stack)) { if (energy(stack) > 0) energy(stack, energy(stack) - 3); else overloaded(stack, false); }
    }
    public static boolean attack(Player player) {
        var stack = player.getMainHandItem();
        if (player.level().isClientSide() || player.isSpectator() || !stack.is(INSTANCE) || player.getAttackStrengthScale(0) != 1 || overloaded(stack)) return false;
        var previous = LAST_ATTACK.get(player); long now = player.level().getGameTime();
        if (previous != null && now - previous < Math.ceil(player.getCurrentItemAttackStrengthDelay())) return false;
        if (player.hasEffect(ExtraBotanyMobEffects.FLAMESCION)) {
            for (int i = 0; i < 3; i++) player.level().addFreshEntity(LegacyFlameProjectile.create(true, player,
                    player.getLookAngle().yRot((float) Math.toRadians(-15 + 15 * i)).normalize().scale(1.2)));
            player.removeEffect(ExtraBotanyMobEffects.FLAMESCION);
        } else if (mode(player)) player.level().addFreshEntity(LegacyFlameProjectile.create(false, player, player.getLookAngle().normalize()));
        else return false;
        LAST_ATTACK.put(player, now); return true;
    }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (overloaded(stack)) return InteractionResultHolder.pass(stack);
        if (player.isShiftKeyDown() && !mode(player)) {
            if (!level.isClientSide()) {
                if (player.onGround()) for (var target : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(3))) {
                    if (target == player) continue;
                    target.setDeltaMovement(target.getDeltaMovement().add(0, 1, 0)); target.hurtMarked = true;
                    target.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.TIMELOCK, 60));
                }
                player.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.INCANDESCENCE, 60));
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        if (mode(player)) {
            if (!level.isClientSide()) {
                level.addFreshEntity(LegacyFlameArea.create(LegacyFlameArea.Kind.VOID, player, player.position().add(player.getLookAngle().scale(5))));
                player.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.INCANDESCENCE, 80)); player.getCooldowns().addCooldown(this, 40);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        return InteractionResultHolder.pass(stack);
    }
    public static void ultimate(Player player) {
        if (player.level().isClientSide() || !mode(player)) return;
        var start = player.position().add(player.getLookAngle().normalize().scale(5));
        player.level().addFreshEntity(LegacyFlameArea.create(LegacyFlameArea.Kind.ULT, player, new net.minecraft.world.phys.Vec3(start.x, player.getY() + .25, start.z)));
        energy(player.getMainHandItem(), 600); overloaded(player.getMainHandItem(), true);
        player.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.TIMELOCK, 40));
    }
    public static void dash(Player player) {
        if (player.level().isClientSide() || !mode(player) || player.getCooldowns().isOnCooldown(INSTANCE)) return;
        var start = player.position(); var movement = player.getLookAngle().normalize().scale(4); var end = start.add(movement);
        player.teleportTo(end.x, end.y, end.z);
        boolean hit = false;
        var source = io.github.lounode.extrabotany.common.ExtraBotanyDamageTypes.Sources.source(player.level().registryAccess(),
                net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DAMAGE_TYPE, net.minecraft.resources.ResourceLocation.parse("extrabotany:flamescion_flame")));
        for (var target : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(8))) {
            if (target == player || target.getBoundingBox().inflate(4).clip(start.subtract(movement), end.add(movement)).isEmpty()) continue;
            target.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.TIMELOCK, 40)); target.invulnerableTime = 0; target.hurt(source, 6); hit = true;
        }
        if (hit) { player.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.INCANDESCENCE, 80)); player.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.FLAMESCION, 200)); }
        player.getCooldowns().addCooldown(INSTANCE, 20);
    }
    @Override public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flags) {
        tooltip.add(Component.translatable("tooltip.extrabotany.flamescion_weapon.energy", energy(stack), 600).withStyle(ChatFormatting.GRAY));
        if (overloaded(stack)) tooltip.add(Component.translatable("tooltip.extrabotany.flamescion_weapon.overloaded").withStyle(ChatFormatting.RED));
    }
}
