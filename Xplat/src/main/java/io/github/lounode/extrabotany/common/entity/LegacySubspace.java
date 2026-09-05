package io.github.lounode.extrabotany.common.entity;

import io.github.lounode.extrabotany.common.handler.DamageHandler;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import java.util.Comparator;

public final class LegacySubspace extends LegacyOwnedEntity {
    public static final EntityType<LegacySubspace> TYPE = EntityType.Builder.<LegacySubspace>of(LegacySubspace::new, MobCategory.MISC)
            .sized(.1F, .1F).clientTrackingRange(8).updateInterval(2).build("extrabotany:subspace");
    private int liveTicks, delay, interval = 10, count, mode;
    private float size, rotation;
    public LegacySubspace(EntityType<? extends LegacySubspace> type, Level level) { super(type, level); }
    public void configure(int mode, int life, int delay, int interval, float size, float rotation) {
        this.mode = mode; liveTicks = life; this.delay = delay; this.interval = Math.max(1, interval); this.size = size; this.rotation = rotation;
    }
    @Override public void tick() {
        setDeltaMovement(Vec3.ZERO); super.tick();
        float range = Math.max(.25F, size);
        for (int i = 0; i < 4; i++) level().addParticle(ParticleTypes.PORTAL, getX() + (random.nextDouble() - .5) * range,
                getY() + random.nextDouble() * range, getZ() + (random.nextDouble() - .5) * range, 0, .02, 0);
        if (level().isClientSide()) return;
        if (!(getOwner() instanceof LivingEntity owner) || !owner.isAlive()) { discard(); return; }
        if (tickCount < delay) return;
        if (tickCount > liveTicks + delay) { discard(); return; }
        if (mode == 1 && tickCount > delay + 8 && count < 1) { spawn(owner, owner.getLookAngle(), 2.45F, 100); count++; }
        else if (mode == 0 && tickCount % interval == 0 && count < 5 && tickCount > delay + 5 && tickCount < liveTicks - delay - 10) {
            var direction = level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(12)).stream()
                    .filter(target -> DamageHandler.INSTANCE.checkPassable(target, owner)).min(Comparator.comparingDouble(this::distanceToSqr))
                    .map(target -> target.position().add(0, target.getBbHeight() * .5, 0).subtract(position()).normalize()).orElse(owner.getLookAngle());
            spawn(owner, direction, 1.35F, 80); count++;
        }
    }
    private void spawn(LivingEntity owner, Vec3 direction, float speed, int life) {
        if (direction.lengthSqr() == 0) direction = owner.getLookAngle();
        var spear = new LegacySubspaceSpear(LegacySubspaceSpear.TYPE, level()); spear.setOwner(owner); spear.configure(12, life);
        spear.setPos(getX(), getY() - .75, getZ()); spear.shoot(direction.x, direction.y, direction.z, speed, 1);
        spear.setYRot(owner.getYRot()); spear.setXRot(-owner.getXRot()); level().addFreshEntity(spear);
    }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {}
    @Override protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag); tag.putInt("LiveTicks", liveTicks); tag.putInt("Delay", delay); tag.putInt("Interval", interval);
        tag.putInt("Count", count); tag.putInt("Type", mode); tag.putFloat("Size", size); tag.putFloat("Rotation", rotation);
    }
    @Override protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag); liveTicks = tag.getInt("LiveTicks"); delay = tag.getInt("Delay"); interval = Math.max(1, tag.getInt("Interval"));
        count = tag.getInt("Count"); mode = tag.getInt("Type"); size = tag.getFloat("Size"); rotation = tag.getFloat("Rotation");
    }
    @Override public boolean canBeCollidedWith() { return false; }
    @Override public boolean isPushable() { return false; }
    @Override public boolean isPushedByFluid() { return false; }
}
