package io.github.lounode.extrabotany.common.entity;

import io.github.lounode.extrabotany.common.brew.ExtraBotanyMobEffects;
import io.github.lounode.extrabotany.common.handler.DamageHandler;
import io.github.lounode.extrabotany.common.sounds.ExtraBotanySounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import java.util.EnumMap;
import java.util.Map;

public final class LegacyFlameArea extends LegacyOwnedEntity {
    public enum Kind { SLASH("flamescion_slash"), VOID("flamescion_void"), ULT("flamescion_ult"); public final String id; Kind(String id) { this.id = id; } }
    public static final Map<Kind, EntityType<LegacyFlameArea>> TYPES = new EnumMap<>(Kind.class);
    static { for (var kind : Kind.values()) TYPES.put(kind, EntityType.Builder.<LegacyFlameArea>of((type, level) -> new LegacyFlameArea(type, level, kind), MobCategory.MISC)
            .sized(.25F, .25F).clientTrackingRange(8).updateInterval(2).build("extrabotany:" + kind.id)); }
    private static final EntityDataAccessor<Float> ROTATION = SynchedEntityData.defineId(LegacyFlameArea.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> PITCH = SynchedEntityData.defineId(LegacyFlameArea.class, EntityDataSerializers.FLOAT);
    private final Kind kind;
    private LegacyFlameArea(EntityType<? extends LegacyFlameArea> type, Level level, Kind kind) { super(type, level); this.kind = kind; }
    public static LegacyFlameArea create(Kind kind, LivingEntity owner, Vec3 position) {
        var area = new LegacyFlameArea(TYPES.get(kind), owner.level(), kind); area.setOwner(owner); area.setPos(position);
        if (kind == Kind.SLASH) { area.entityData.set(ROTATION, owner.getRandom().nextFloat() * 120 - 60); area.entityData.set(PITCH, owner.getRandom().nextFloat() * 360); }
        return area;
    }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) { builder.define(ROTATION, 0F); builder.define(PITCH, 0F); }
    @Override public void tick() {
        setDeltaMovement(Vec3.ZERO); super.tick();
        if (kind == Kind.SLASH) {
            level().addParticle(ParticleTypes.FLAME, getX(), getY() + .5, getZ(), 0, .04, 0);
            for (var target : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(3.5)))
                if (target instanceof Mob && target instanceof Enemy && DamageHandler.INSTANCE.checkPassable(target, getOwner())) target.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.TIMELOCK, 2, 3));
            if (!level().isClientSide()) { if (tickCount == 2 || tickCount == 5) damageAround(this, getOwner(), 3.5, 1); if (tickCount >= 6) discard(); }
        } else if (kind == Kind.VOID) {
            level().addParticle(ParticleTypes.SOUL_FIRE_FLAME, getX(), getY() + .3, getZ(), 0, .03, 0);
            if (!level().isClientSide()) {
                for (var target : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(6))) {
                    if (!DamageHandler.INSTANCE.checkPassable(target, getOwner())) continue;
                    var pull = position().subtract(target.position());
                    if (pull.lengthSqr() > 0) { target.setDeltaMovement(pull.normalize().scale(1.5)); target.hurtMarked = true; }
                    target.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.TIMELOCK, 2, 3));
                }
                if (tickCount % 15 == 0) damageAround(this, getOwner(), 6, 1.5F);
                if (tickCount >= 40) discard();
            }
        } else {
            if (!level().isClientSide()) {
                if (tickCount == 1) level().playSound(null, blockPosition(), ExtraBotanySounds.FLAMESCION_ULT, SoundSource.PLAYERS, 1, 1);
                if (tickCount == 10 || tickCount == 35 || tickCount == 60) damageAround(this, getOwner(), 8, 12);
                if (tickCount >= 85) discard();
            } else if (tickCount >= 40) level().addParticle(ParticleTypes.EXPLOSION, getX() - 2 + random.nextDouble() * 4,
                    getY() - 2 + random.nextDouble() * 4, getZ() - 2 + random.nextDouble() * 4, 0, 0, 0);
        }
    }
    public static void damageAround(Entity source, Entity owner, double range, float damage) {
        for (var target : source.level().getEntitiesOfClass(LivingEntity.class, source.getBoundingBox().inflate(range))) {
            if (target == owner || !DamageHandler.INSTANCE.checkPassable(target, owner)) continue;
            target.invulnerableTime = 0;
            target.hurt(owner == null ? source.damageSources().magic() : source.damageSources().indirectMagic(source, owner), damage);
        }
    }
    @Override public boolean canBeCollidedWith() { return false; }
    @Override public boolean isPushable() { return false; }
    @Override public boolean isPushedByFluid() { return false; }
}
