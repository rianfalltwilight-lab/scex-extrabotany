package io.github.lounode.extrabotany.common.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import java.util.UUID;

public final class LegacySwordDomain extends Entity {
    public static final EntityType<LegacySwordDomain> TYPE = EntityType.Builder.<LegacySwordDomain>of(LegacySwordDomain::new, MobCategory.MISC)
            .sized(.5F, 2).clientTrackingRange(8).updateInterval(2).build("extrabotany:sword_domain");
    private static final EntityDataAccessor<Integer> VARIETY = SynchedEntityData.defineId(LegacySwordDomain.class, EntityDataSerializers.INT);
    private UUID target;
    private BlockPos source = BlockPos.ZERO;
    private int floorY;
    public LegacySwordDomain(EntityType<? extends LegacySwordDomain> type, Level level) { super(type, level); }
    public void configure(UUID target, BlockPos source, int variety) { this.target = target; this.source = source.immutable(); floorY = source.getY() + 5; entityData.set(VARIETY, Math.floorMod(variety, 10)); }
    public int variety() { return entityData.get(VARIETY); }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) { builder.define(VARIETY, 0); }
    @Override public void tick() {
        super.tick();
        if (level().isClientSide()) { level().addParticle(ParticleTypes.END_ROD, getX(), getY(), getZ(), 0, 0, 0); return; }
        boolean host = level().getEntitiesOfClass(io.github.lounode.extrabotany.common.entity.gaia.Gaia.class, new AABB(blockPosition()).inflate(15), Entity::isAlive)
                .stream().anyMatch(entity -> entity instanceof io.github.lounode.extrabotany.common.entity.gaia.GaiaIII
                        || net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getPath().equals("void_herrscher"));
        if (tickCount > 201 || !host) { discard(); return; }
        setPos(getX(), Math.max(getY() - .01, floorY), getZ());
        if (target != null && level() instanceof ServerLevel server && server.getEntity(target) instanceof Player player) {
            if (tickCount > 70) {
                var delta = source.getCenter().subtract(player.position().add(0, player.getBbHeight() * .5, 0));
                if (delta.lengthSqr() >= 16) {
                    var motion = delta.normalize(); player.setDeltaMovement(motion.x, .2, motion.z); player.hurtMarked = true;
                    if (player.getVehicle() != null) { player.getVehicle().setDeltaMovement(motion.x, .2, motion.z); player.getVehicle().hurtMarked = true; }
                    player.addEffect(new MobEffectInstance(MobEffects.UNLUCK, 400, 4));
                }
                double x = player.getX() - source.getX(), z = player.getZ() - source.getZ();
                if (x * x + z * z > 400) player.teleportTo(source.getX(), source.getY(), source.getZ());
            }
            if (tickCount % 50 == 0) player.hurt(damageSources().magic(), 1);
            if (tickCount == 200) LegacySwordProjectile.trueMagicDamage(player, this, 1);
        }
    }
    @Override protected void addAdditionalSaveData(CompoundTag tag) {
        if (target != null) tag.putUUID("Target", target); tag.putLong("Source", source.asLong()); tag.putInt("FloorY", floorY); tag.putInt("Type", variety());
    }
    @Override protected void readAdditionalSaveData(CompoundTag tag) {
        target = tag.hasUUID("Target") ? tag.getUUID("Target") : null; source = BlockPos.of(tag.getLong("Source")); floorY = tag.getInt("FloorY"); entityData.set(VARIETY, Math.floorMod(tag.getInt("Type"), 10));
    }
}
