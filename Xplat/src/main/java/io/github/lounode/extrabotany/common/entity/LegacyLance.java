package io.github.lounode.extrabotany.common.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.Comparator;

public final class LegacyLance extends LegacyOwnedEntity {
    public static final EntityType<LegacyLance> TYPE = EntityType.Builder.<LegacyLance>of(LegacyLance::new, MobCategory.MISC)
            .sized(.5F, 2).clientTrackingRange(8).updateInterval(2).build("extrabotany:subspace_lance");
    private int life = 1200;
    private float damage = 4;
    public LegacyLance(EntityType<? extends LegacyLance> type, Level level) { super(type, level); }
    public void configure(float damage, int life) { this.damage = damage; this.life = life; }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {}
    @Override public void tick() {
        super.tick();
        if (level().isClientSide()) { level().addParticle(ParticleTypes.END_ROD, getX(), getY() + 1.5, getZ(), 0, 0, 0); return; }
        if (hasOwner() && tickCount % 20 == 0 && (!(getOwner() instanceof LivingEntity owner) || !owner.isAlive())) { discard(); return; }
        if (!onGround()) { setDeltaMovement(getDeltaMovement().add(0, -.15, 0)); move(MoverType.SELF, getDeltaMovement()); }
        if (tickCount % 35 == 0) level().getEntitiesOfClass(Player.class, new AABB(blockPosition()).inflate(8), LivingEntity::isAlive).stream()
                .min(Comparator.comparingDouble(this::distanceToSqr)).ifPresent(player -> {
                    if (player.getHealth() > 10) LegacySwordProjectile.trueMagicDamage(player, this, damage + player.getMaxHealth() * .1F);
                    else player.hurt(damageSources().indirectMagic(this, this), damage);
                    player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1));
                });
        if (tickCount % 5 == 0 && level() instanceof ServerLevel server) {
            for (var other : level().getEntitiesOfClass(LegacyLance.class, new AABB(blockPosition()).inflate(15), EntityCandidate -> EntityCandidate.isAlive())) {
                if (other == this || other.getId() < getId()) continue;
                var a = position().add(0, 1.5, 0); var b = other.position().add(0, 1.5, 0); var delta = b.subtract(a);
                for (var player : level().getEntitiesOfClass(Player.class, new AABB(a, b).inflate(.8), LivingEntity::isAlive)) {
                    var center = player.position().add(0, player.getBbHeight() * .5, 0);
                    double fraction = Math.clamp(center.subtract(a).dot(delta) / Math.max(1E-6, delta.lengthSqr()), 0, 1);
                    if (center.distanceTo(a.add(delta.scale(fraction))) > .8 || player.getHealth() <= 8) continue;
                    player.hurt(damageSources().lightningBolt(), 1.2F); LegacySwordProjectile.trueMagicDamage(player, this, .4F);
                    player.addEffect(new MobEffectInstance(MobEffects.UNLUCK, 100, 1));
                }
                server.sendParticles(ParticleTypes.ELECTRIC_SPARK, (a.x + b.x) * .5, (a.y + b.y) * .5, (a.z + b.z) * .5,
                        8, Math.abs(a.x - b.x) * .25, Math.abs(a.y - b.y) * .25, Math.abs(a.z - b.z) * .25, .01);
            }
        }
        if (--life <= 0) discard();
    }
    @Override protected void addAdditionalSaveData(CompoundTag tag) { super.addAdditionalSaveData(tag); tag.putInt("Life", life); tag.putFloat("Damage", damage); }
    @Override protected void readAdditionalSaveData(CompoundTag tag) { super.readAdditionalSaveData(tag); life = tag.getInt("Life"); damage = tag.getFloat("Damage"); }
}
