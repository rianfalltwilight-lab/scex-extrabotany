package io.github.lounode.extrabotany.common.item.legacy;

import io.github.lounode.extrabotany.common.entity.LegacySwordProjectile;
import io.github.lounode.extrabotany.common.handler.DamageHandler;
import io.github.lounode.extrabotany.xplat.EXplatAbstractions;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.entity.FallingStarEntity;
import vazkii.botania.common.item.relic.RelicImpl;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LegacyRelicSword extends SwordItem {
    public static final Map<String, LegacyRelicSword> ITEMS = new LinkedHashMap<>();
    static {
        ITEMS.put("true_terrablade", new LegacyRelicSword(400, LegacySwordProjectile.Kind.TERRA));
        ITEMS.put("true_shadow_katana", new LegacyRelicSword(800, LegacySwordProjectile.Kind.SHADOW));
        ITEMS.put("influx_waver", new LegacyRelicSword(500, LegacySwordProjectile.Kind.INFLUX));
        ITEMS.put("star_wrath", new LegacyRelicSword(500, null));
        ITEMS.put("first_fractal", new LegacyFirstFractal());
        ITEMS.put("spear_of_subspace", new LegacySubspaceSpearItem());
    }
    private final int manaCost;
    private static final Map<Player, Long> LAST_USE = new java.util.WeakHashMap<>();
    private final LegacySwordProjectile.Kind kind;
    protected LegacyRelicSword(net.minecraft.world.item.Tier tier, Properties properties, int cost) {
        super(tier, properties); manaCost = cost; kind = null;
    }
    private LegacyRelicSword(int cost, LegacySwordProjectile.Kind kind) {
        super(Tiers.DIAMOND, new Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant()
                .attributes(SwordItem.createAttributes(Tiers.DIAMOND, kind == null ? 6 : 5, kind == null ? -1.6F : -2F)));
        manaCost = cost; this.kind = kind;
    }
    public boolean tryUse(Player player, Entity target) {
        var stack = player.getMainHandItem();
        if (player.level().isClientSide() || player.isSpectator() || !stack.is(this) || player.getAttackStrengthScale(0) != 1) return false;
        long now = player.level().getGameTime();
        Long previous = LAST_USE.get(player);
        if (previous != null && now - previous < Math.ceil(player.getCurrentItemAttackStrengthDelay())) return false;
        var relic = EXplatAbstractions.INSTANCE.findRelic(stack);
        if (relic == null || !relic.isRightPlayer(player) || !canUse(player)) return false;
        int cost = manaCost(player);
        if (cost > 0 && !ManaItemHandler.instance().requestManaExactForTool(stack, player, cost, true)) return false;
        LAST_USE.put(player, now);
        perform(player, target);
        return true;
    }
    protected boolean canUse(Player player) { return true; }
    protected int manaCost(Player player) { return manaCost; }
    protected void perform(Player player, Entity target) {
        var aim = resolveTarget(player, target, kind == LegacySwordProjectile.Kind.TERRA ? 80 : 64);
        if (kind == null) {
            for (int i = 0; i < 5; i++) {
                var impact = aim.add((.5 - player.getRandom().nextDouble()) * 6, 0, (.5 - player.getRandom().nextDouble()) * 6);
                var offset = new Vec3((.5 * player.getRandom().nextDouble() - .25) * 18, 24, (.5 * player.getRandom().nextDouble() - .25) * 18);
                var star = new FallingStarEntity(player, player.level());
                star.setPos(impact.add(offset)); star.setDeltaMovement(offset.normalize().scale(-1.5)); player.level().addFreshEntity(star);
            }
        } else if (kind == LegacySwordProjectile.Kind.SHADOW) {
            var found = target instanceof LivingEntity living && DamageHandler.INSTANCE.checkPassable(living, player) ? living
                    : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(8)).stream()
                    .filter(living -> DamageHandler.INSTANCE.checkPassable(living, player)).min(Comparator.comparingDouble(player::distanceToSqr)).orElse(null);
            if (found != null) aim = found.position().add(0, 1, 0);
            var look = player.getLookAngle().multiply(1, 0, 1);
            if (look.lengthSqr() < 1E-4) look = Vec3.directionFromRotation(0, player.getYRot());
            var forward = look.normalize().scale(1.75);
            var side = new Vec3(-forward.z, 0, forward.x).normalize();
            var base = player.position().add(0, player.getBbHeight() * .55, 0).add(forward);
            for (int i = -1; i <= 1; i++) player.level().addFreshEntity(LegacySwordProjectile.create(kind, player,
                    base.add(side.scale(i * 1.4)).add(0, Math.abs(i) * .2, 0), aim, .75, 0));
        } else player.level().addFreshEntity(LegacySwordProjectile.create(kind, player, player.position().add(0, 1.1, 0), aim,
                kind == LegacySwordProjectile.Kind.TERRA ? .8 : .7, kind == LegacySwordProjectile.Kind.INFLUX ? 3 : 0));
    }
    public static Vec3 resolveTarget(LivingEntity user, Entity target, double range) {
        if (target != null) return target.position().add(0, target.getBbHeight() * .5, 0);
        var eye = user.getEyePosition(); var end = eye.add(user.getLookAngle().scale(range));
        var hit = user.level().clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, user));
        return hit.getType() == HitResult.Type.MISS ? end : hit.getLocation().add(0, 1, 0);
    }
    @Override public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!level.isClientSide() && entity instanceof Player player) {
            var relic = EXplatAbstractions.INSTANCE.findRelic(stack); if (relic != null) relic.tickBinding(player);
        }
        super.inventoryTick(stack, level, entity, slot, selected);
    }
    @Override public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flags) {
        tooltip.add(Component.empty()); RelicImpl.addDefaultTooltip(stack, tooltip);
    }
}
