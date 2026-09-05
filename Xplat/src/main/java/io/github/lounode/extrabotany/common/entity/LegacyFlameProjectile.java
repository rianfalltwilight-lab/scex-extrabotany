package io.github.lounode.extrabotany.common.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class LegacyFlameProjectile extends ThrowableProjectile {
    public static final EntityType<LegacyFlameProjectile> STRENGTHEN = EntityType.Builder.<LegacyFlameProjectile>of((type, level) -> new LegacyFlameProjectile(type, level, true), MobCategory.MISC)
            .sized(.35F, .35F).clientTrackingRange(8).updateInterval(2).build("extrabotany:strengthen_slash");
    public static final EntityType<LegacyFlameProjectile> SWORD = EntityType.Builder.<LegacyFlameProjectile>of((type, level) -> new LegacyFlameProjectile(type, level, false), MobCategory.MISC)
            .sized(.35F, .35F).clientTrackingRange(8).updateInterval(2).build("extrabotany:flamescion_sword");
    private final boolean strengthen;
    private LegacyFlameProjectile(EntityType<? extends LegacyFlameProjectile> type, Level level, boolean strengthen) { super(type, level); this.strengthen = strengthen; setNoGravity(true); }
    public static LegacyFlameProjectile create(boolean strengthen, LivingEntity owner, Vec3 velocity) {
        var projectile = new LegacyFlameProjectile(strengthen ? STRENGTHEN : SWORD, owner.level(), strengthen);
        projectile.setOwner(owner); projectile.setPos(owner.position().add(0, .5, 0)); projectile.setDeltaMovement(velocity); return projectile;
    }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {}
    @Override protected void onHit(HitResult hit) {}
    @Override public void tick() {
        super.tick(); setNoGravity(true);
        level().addParticle(ParticleTypes.FLAME, getX(), getY(), getZ(), 0, strengthen ? .02 : .03, 0);
        if (!level().isClientSide()) {
            if (tickCount % (strengthen ? 2 : 4) == 0) LegacyFlameArea.damageAround(this, getOwner(), strengthen ? 2.5 : 3.5, strengthen ? 5 : 4);
            if (tickCount >= (strengthen ? 15 : 30)) discard();
        }
    }
}
