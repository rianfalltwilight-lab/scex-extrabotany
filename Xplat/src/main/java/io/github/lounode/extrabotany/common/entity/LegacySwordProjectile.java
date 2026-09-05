package io.github.lounode.extrabotany.common.entity;

import io.github.lounode.extrabotany.common.handler.DamageHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import vazkii.botania.client.fx.WispParticleData;
import java.util.EnumMap;
import java.util.Map;

public final class LegacySwordProjectile extends ThrowableItemProjectile {
    public enum Kind { TERRA("true_terrablade", 60, 11), SHADOW("true_shadow_katana", 40, 5), INFLUX("influx_waver", 60, 12);
        public final String item;
        final int lifetime, damage;
        Kind(String item, int lifetime, int damage) { this.item = item; this.lifetime = lifetime; this.damage = damage; }
    }
    public static final Map<Kind, EntityType<LegacySwordProjectile>> TYPES = new EnumMap<>(Kind.class);
    static { for (var kind : Kind.values()) TYPES.put(kind, EntityType.Builder.<LegacySwordProjectile>of((type, level) -> new LegacySwordProjectile(type, level, kind), MobCategory.MISC)
            .sized(.5F, .5F).clientTrackingRange(4).updateInterval(2).build("extrabotany:" + kind.item + "_projectile")); }
    public final Kind kind;
    private Vec3 targetPos = Vec3.ZERO;
    private float attackBonus;
    private int strikes, removeAt = -1;
    private BlockPos next = BlockPos.ZERO;
    private LegacySwordProjectile(EntityType<? extends LegacySwordProjectile> type, Level level, Kind kind) { super(type, level); this.kind = kind; setNoGravity(true); }
    public static LegacySwordProjectile create(Kind kind, LivingEntity owner, Vec3 start, Vec3 aim, double speed, int strikes) {
        var projectile = new LegacySwordProjectile(TYPES.get(kind), owner.level(), kind);
        projectile.setOwner(owner); projectile.setPos(start); projectile.strikes = strikes;
        projectile.attackBonus = owner instanceof Player ? (float) Math.max(0, owner.getAttributeValue(Attributes.ATTACK_DAMAGE) - 9) : 0;
        projectile.shootAt(aim, speed); return projectile;
    }
    private void shootAt(Vec3 aim, double speed) {
        targetPos = aim; var delta = aim.subtract(position()).normalize().scale(speed); setDeltaMovement(delta);
        setXRot((float) Math.toDegrees(Math.atan2(delta.y, delta.horizontalDistance())));
        setYRot((float) Math.toDegrees(Math.atan2(delta.x, delta.z)));
    }
    @Override protected Item getDefaultItem() {
        // ThrowableItemProjectile asks for this during super construction, before kind is assigned.
        var effective = kind != null ? kind : TYPES.entrySet().stream().filter(entry -> entry.getValue() == getType()).map(Map.Entry::getKey).findFirst().orElse(Kind.TERRA);
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("extrabotany", effective.item));
    }
    @Override protected void onHit(HitResult hit) { if (!level().isClientSide() && hit.getType() == HitResult.Type.BLOCK) discard(); }
    @Override public void tick() {
        if (!level().isClientSide() && kind == Kind.INFLUX && removeAt != -1 && tickCount >= removeAt + 4) {
            if (getOwner() instanceof LivingEntity owner && !next.equals(BlockPos.ZERO)) {
                double angle = -Math.PI + Math.PI * 2 * random.nextDouble();
                double pitch = .37699111843077515 * random.nextDouble() + .8796459430051422;
                var start = new Vec3(next.getX() + 6 * Math.sin(pitch) * Math.cos(angle), next.getY() + 6 * Math.cos(pitch), next.getZ() + 6 * Math.sin(pitch) * Math.sin(angle));
                level().addFreshEntity(create(kind, owner, start, Vec3.atCenterOf(next), .8, strikes - 1));
            }
            discard(); return;
        }
        super.tick(); setNoGravity(true);
        if (level().isClientSide()) {
            if (tickCount % 2 == 0) level().addParticle(switch (kind) {
                case TERRA -> WispParticleData.wisp(.3F, .1F, .95F, .1F, 1);
                case SHADOW -> WispParticleData.wisp(.15F, 0, 0, 0, 1);
                case INFLUX -> WispParticleData.wisp(.3F, .1F, .1F, .85F, 1);
            }, getX(), getY(), getZ(), 0, 0, 0);
            return;
        }
        if (getOwner() instanceof LivingEntity owner && !owner.isAlive() || tickCount >= kind.lifetime) { discard(); return; }
        if (kind == Kind.SHADOW && tickCount < 4 || kind == Kind.TERRA && tickCount % 3 != 0 || kind == Kind.INFLUX && removeAt != -1) return;
        for (var target : level().getEntitiesOfClass(LivingEntity.class, new AABB(getX(), getY(), getZ(), xOld, yOld, zOld).inflate(2))) {
            if (!DamageHandler.INSTANCE.checkPassable(target, getOwner()) || kind == Kind.INFLUX && target.hurtTime > 0) continue;
            if (!damage(target)) continue;
            if (kind == Kind.TERRA) continue;
            if (kind == Kind.SHADOW) discard();
            else if (strikes > 0) {
                if (target.isAlive()) { next = target.blockPosition().above(); removeAt = tickCount; }
                else for (var other : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(5))) {
                    if (DamageHandler.INSTANCE.checkPassable(other, getOwner())) { next = other.blockPosition().above(); removeAt = tickCount; break; }
                }
            }
            break;
        }
    }
    private boolean damage(LivingEntity target) {
        var owner = getOwner();
        if (kind == Kind.SHADOW) target.invulnerableTime = 0;
        if (kind == Kind.INFLUX) target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
        if (!(owner instanceof Player) && kind != Kind.INFLUX) {
            if (kind == Kind.SHADOW || target.invulnerableTime == 0) trueMagicDamage(target, owner, kind == Kind.TERRA ? 2.5F : 2);
            return target.hurt(damageSources().indirectMagic(this, owner), kind == Kind.TERRA ? 7 : 5.5F);
        }
        var source = owner instanceof Player player ? damageSources().playerAttack(player)
                : owner instanceof LivingEntity living ? damageSources().mobAttack(living) : damageSources().generic();
        return target.hurt(source, kind.damage + attackBonus);
    }
    public static void trueMagicDamage(LivingEntity target, net.minecraft.world.entity.Entity owner, float amount) {
        if (!target.isAlive() || !Float.isFinite(amount) || amount <= 0) return;
        var source = target.damageSources().indirectMagic(owner == null ? target : owner, owner);
        if (target.getHealth() <= amount) target.hurt(source, Float.MAX_VALUE);
        else { target.setHealth(Math.max(1, target.getHealth() - amount)); target.hurt(source, .01F); }
    }
    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putDouble("TargetX", targetPos.x); tag.putDouble("TargetY", targetPos.y); tag.putDouble("TargetZ", targetPos.z); tag.putFloat("AttackBonus", attackBonus);
        if (kind == Kind.INFLUX) { tag.putInt("Strikes", strikes); tag.putInt("RemoveAt", removeAt); tag.putLong("Next", next.asLong()); }
    }
    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        targetPos = new Vec3(tag.getDouble("TargetX"), tag.getDouble("TargetY"), tag.getDouble("TargetZ")); attackBonus = tag.getFloat("AttackBonus");
        if (kind == Kind.INFLUX) { strikes = tag.getInt("Strikes"); removeAt = tag.getInt("RemoveAt"); next = BlockPos.of(tag.getLong("Next")); }
    }
}
