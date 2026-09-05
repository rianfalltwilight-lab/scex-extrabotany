package io.github.lounode.extrabotany.common.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public final class LegacyVoidField extends Entity {
    public static final EntityType<LegacyVoidField> TYPE = EntityType.Builder.<LegacyVoidField>of(LegacyVoidField::new, MobCategory.MISC)
            .sized(3, 2).clientTrackingRange(8).updateInterval(2).build("extrabotany:void_field");
    private int life = 60;
    public LegacyVoidField(EntityType<? extends LegacyVoidField> type, Level level) { super(type, level); }
    @Override public void tick() {
        super.tick();
        if (level().isClientSide()) {
            for (int i = 0; i < 6; i++) level().addParticle(ParticleTypes.PORTAL, getX() + (random.nextDouble() - .5) * 3,
                    getY() + random.nextDouble() * 2, getZ() + (random.nextDouble() - .5) * 3, 0, .02, 0);
            return;
        }
        for (var living : level().getEntitiesOfClass(LivingEntity.class, new AABB(blockPosition()).inflate(1.5))) {
            living.setDeltaMovement(living.getDeltaMovement().scale(.12)); living.hurtMarked = true;
        }
        if (--life <= 0) discard();
    }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {}
    @Override protected void addAdditionalSaveData(CompoundTag tag) { tag.putInt("Life", life); }
    @Override protected void readAdditionalSaveData(CompoundTag tag) { life = tag.getInt("Life"); }
}
