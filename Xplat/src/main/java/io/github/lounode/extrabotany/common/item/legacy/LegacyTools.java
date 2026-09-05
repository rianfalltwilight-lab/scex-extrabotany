package io.github.lounode.extrabotany.common.item.legacy;

import io.github.lounode.extrabotany.common.util.AttributeUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.entity.PixieEntity;
import java.util.Map;

public final class LegacyTools {
    public static final Item ROD = new DiscordRod();
    public static final Item PIXIE = new PixieBottle();
    public static final Item KATANA = new Katana();
    public static final Map<String, Item> ITEMS = Map.of("rod_of_discord", ROD, "bottled_pixie", PIXIE, "shadow_katana", KATANA,
            "uuz_fan", new ProjectileTool(io.github.lounode.extrabotany.common.entity.LegacyProjectile.Kind.BUTTERFLY),
            "bottled_star", new ProjectileTool(io.github.lounode.extrabotany.common.entity.LegacyProjectile.Kind.BOTTLED_STAR),
            "photon_shotgun", new ProjectileTool(io.github.lounode.extrabotany.common.entity.LegacyProjectile.Kind.PHOTON));
    private LegacyTools() {}

    private static final class ProjectileTool extends Item {
        private final io.github.lounode.extrabotany.common.entity.LegacyProjectile.Kind kind;
        ProjectileTool(io.github.lounode.extrabotany.common.entity.LegacyProjectile.Kind kind) { super(new Properties().stacksTo(1)); this.kind = kind; }
        @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            var stack = player.getItemInHand(hand);
            boolean star = kind == io.github.lounode.extrabotany.common.entity.LegacyProjectile.Kind.BOTTLED_STAR;
            boolean fan = kind == io.github.lounode.extrabotany.common.entity.LegacyProjectile.Kind.BUTTERFLY;
            if (!level.isClientSide()) {
                if (star && !ManaItemHandler.instance().requestManaExactForTool(stack, player, 300, true)) return InteractionResultHolder.fail(stack);
                for (int i = 0; i < (star ? 1 : fan ? 3 : 12); i++) {
                    var projectile = io.github.lounode.extrabotany.common.entity.LegacyProjectile.create(kind, player);
                    if (star) projectile.setPos(player.getX(), player.getY() + 1.2, player.getZ());
                    else if (fan) { projectile.setPos(player.position()); projectile.shootFromRotation(player, player.getXRot(), player.getYRot() + (i - 1) * 25, 0, .5F, 1); }
                    else {
                        float pitch = player.getXRot() - 8 + level.random.nextFloat() * 16, yaw = player.getYRot() - 8 + level.random.nextFloat() * 16;
                        projectile.setPos(player.getX(), player.getEyeY() - .1, player.getZ());
                        projectile.setDeltaMovement(net.minecraft.world.phys.Vec3.directionFromRotation(pitch, yaw).scale(3.2)); projectile.setXRot(pitch); projectile.setYRot(yaw);
                    }
                    level.addFreshEntity(projectile);
                }
                if (star) player.getCooldowns().addCooldown(this, 100);
            }
            if (fan) player.getCooldowns().addCooldown(this, 10);
            if (!star) player.awardStat(net.minecraft.stats.Stats.ITEM_USED.get(this));
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
    }

    @SuppressWarnings("deprecation") // Retain the scex.1 MOB_SUMMONED initialization contract.
    public static void spawnPixie(ServerPlayer owner, LivingEntity target) {
        var pixie = new PixieEntity(owner.level(), false);
        pixie.setPos(owner.getX(), owner.getY() + 1.5, owner.getZ());
        pixie.setProps(target, owner, 3.5F);
        pixie.finalizeSpawn(owner.serverLevel(), owner.level().getCurrentDifficultyAt(pixie.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
        owner.level().addFreshEntity(pixie);
    }
    private static final class PixieBottle extends Item {
        PixieBottle() { super(new Properties().stacksTo(1)); }
        @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            var stack = player.getItemInHand(hand);
            if (player instanceof ServerPlayer serverPlayer) {
                if (!ManaItemHandler.instance().requestManaExactForTool(stack, player, 300, true)) return InteractionResultHolder.fail(stack);
                spawnPixie(serverPlayer, player);
                player.getCooldowns().addCooldown(this, 240);
            }
            return InteractionResultHolder.success(stack);
        }
    }
    private static final class DiscordRod extends Item {
        DiscordRod() { super(new Properties().durability(81)); }
        @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            var stack = player.getItemInHand(hand);
            var hit = player.pick(64, 1, false);
            if (!(hit instanceof BlockHitResult block) || hit.getType() == HitResult.Type.MISS) return InteractionResultHolder.pass(stack);
            if (!level.isClientSide()) {
                if (!ManaItemHandler.instance().requestManaExactForTool(stack, player, 2000, true)) return InteractionResultHolder.fail(stack);
                var position = block.getLocation();
                player.teleportTo(position.x, position.y + 1, position.z);
                level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1, 3);
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100));
                if (stack.getDamageValue() > 0) player.setHealth(Math.max(1, player.getHealth() - player.getMaxHealth() / 6));
                stack.setDamageValue(80);
            }
            return InteractionResultHolder.success(stack);
        }
        @Override public void inventoryTick(ItemStack stack, Level level, Entity owner, int slot, boolean selected) {
            super.inventoryTick(stack, level, owner, slot, selected);
            if (!level.isClientSide() && stack.getDamageValue() > 0) stack.setDamageValue(stack.getDamageValue() - 1);
        }
    }
    private static final class Katana extends SwordItem {
        private static final ResourceLocation DAMAGE = ResourceLocation.parse("extrabotany:shadow_katana_night_damage");
        private static final ResourceLocation SPEED = ResourceLocation.parse("extrabotany:shadow_katana_night_attack_speed");
        Katana() { super(Tiers.IRON, new Properties().attributes(SwordItem.createAttributes(Tiers.IRON, 3, -2.4F))); }
        @Override public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
            if (!attacker.level().isClientSide() && !attacker.level().isDay()) attacker.heal(2);
            return super.hurtEnemy(stack, target, attacker);
        }
        @Override public void inventoryTick(ItemStack stack, Level level, Entity owner, int slot, boolean selected) {
            super.inventoryTick(stack, level, owner, slot, selected);
            if (owner instanceof ServerPlayer player && stack.getDamageValue() > 0
                    && ManaItemHandler.instance().requestManaExactForTool(stack, player, 60, true)) stack.setDamageValue(stack.getDamageValue() - 1);
            AttributeUtil.removeAttributeModifier(stack, DAMAGE);
            AttributeUtil.removeAttributeModifier(stack, SPEED);
            if (!level.isDay()) {
                AttributeUtil.addAttributeModifier(stack, Attributes.ATTACK_DAMAGE, new AttributeModifier(DAMAGE, 10, AttributeModifier.Operation.ADD_VALUE), EquipmentSlot.MAINHAND);
                AttributeUtil.addAttributeModifier(stack, Attributes.ATTACK_SPEED, new AttributeModifier(SPEED, .15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), EquipmentSlot.MAINHAND);
            }
        }
    }
}
