package io.github.lounode.extrabotany.common.entity;

import io.github.lounode.extrabotany.common.handler.DamageHandler;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import vazkii.botania.common.entity.FallingStarEntity;
import java.util.*;

/** Three distinct registered entity types with their scex.1 tick/hit contracts. */
public final class LegacyProjectile extends ThrowableItemProjectile {
    public enum Kind {
        BUTTERFLY("butterfly_projectile", "uuz_fan", .25F, 100),
        BOTTLED_STAR("bottled_star", "bottled_star", .35F, 130),
        PHOTON("photon_shotgun_projectile", "photon_shotgun", .25F, 10);
        public final String id, item;
        final float size;
        final int lifetime;
        Kind(String id, String item, float size, int lifetime) { this.id = id; this.item = item; this.size = size; this.lifetime = lifetime; }
    }
    public static final Map<Kind, EntityType<LegacyProjectile>> TYPES = new EnumMap<>(Kind.class);
    static {
        for (var kind : Kind.values()) TYPES.put(kind, EntityType.Builder.<LegacyProjectile>of((type, level) -> new LegacyProjectile(type, level, kind), MobCategory.MISC)
                .sized(kind.size, kind.size).clientTrackingRange(4).updateInterval(2).build("extrabotany:" + kind.id));
    }
    private final Kind kind;
    private final Set<UUID> damaged = new HashSet<>();
    private LegacyProjectile(EntityType<? extends LegacyProjectile> type, Level level, Kind kind) { super(type, level); this.kind = kind; setNoGravity(true); }
    public static LegacyProjectile create(Kind kind, LivingEntity owner) {
        var projectile = new LegacyProjectile(TYPES.get(kind), owner.level(), kind); projectile.setOwner(owner); return projectile;
    }
    @Override protected Item getDefaultItem() {
        var effective = kind != null ? kind : TYPES.entrySet().stream().filter(entry -> entry.getValue() == getType()).map(Map.Entry::getKey).findFirst().orElse(Kind.BUTTERFLY);
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("extrabotany", effective.item));
    }
    @Override public void tick() {
        super.tick(); setNoGravity(true);
        if (kind == Kind.BOTTLED_STAR) setDeltaMovement(Vec3.ZERO);
        if (level().isClientSide()) {
            if (kind != Kind.BUTTERFLY) level().addParticle(ParticleTypes.END_ROD, getX(), getY() + (kind == Kind.BOTTLED_STAR ? .25 : 0), getZ(), 0, kind == Kind.BOTTLED_STAR ? .01 : 0, 0);
            return;
        }
        if (kind == Kind.PHOTON) hitNearby();
        if (tickCount > kind.lifetime) { discard(); return; }
        if (kind == Kind.BOTTLED_STAR) {
            if (!(getOwner() instanceof LivingEntity owner) || !owner.isAlive()) { discard(); return; }
            if (tickCount % 6 == 0) falling(owner, position().add((random.nextDouble() * 2 - 1) * 1.8, 0, (random.nextDouble() * 2 - 1) * 1.8));
            if (tickCount % 4 == 0) for (var target : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(12))) {
                if (DamageHandler.INSTANCE.checkPassable(target, owner)) { falling(owner, target.position()); break; }
            }
        }
    }
    private void falling(LivingEntity owner, Vec3 impact) {
        var offset = new Vec3((random.nextDouble() * 2 - 1) * 32.4, 24, (random.nextDouble() * 2 - 1) * 32.4);
        var star = new FallingStarEntity(owner, level()); star.setPos(impact.add(offset)); star.setDeltaMovement(offset.normalize().scale(-1.5)); level().addFreshEntity(star);
    }
    private void hitNearby() {
        var owner = getOwner();
        var source = owner instanceof Player player ? damageSources().playerAttack(player)
                : owner instanceof LivingEntity living ? damageSources().mobAttack(living) : damageSources().generic();
        for (var target : level().getEntitiesOfClass(LivingEntity.class, new AABB(getX(), getY(), getZ(), xOld, yOld, zOld).inflate(.7))) {
            if (target instanceof Player || damaged.contains(target.getUUID()) || !DamageHandler.INSTANCE.checkPassable(target, owner)
                    || !target.hurt(source, 5)) continue;
            if (target.isAlive()) {
                if (target.getHealth() <= .5F) target.hurt(damageSources().indirectMagic(this, owner), Float.MAX_VALUE);
                else { target.setHealth(Math.max(1, target.getHealth() - .5F)); target.hurt(damageSources().indirectMagic(this, owner), .01F); }
            }
            damaged.add(target.getUUID());
        }
    }
    @Override protected void onHit(HitResult hit) {
        if (kind == Kind.BOTTLED_STAR) super.onHit(hit);
        else if (kind == Kind.PHOTON && !level().isClientSide() && hit.getType() == HitResult.Type.BLOCK) discard();
        // scex.1 butterflies deliberately pass through hits until their lifetime expires.
    }
}
