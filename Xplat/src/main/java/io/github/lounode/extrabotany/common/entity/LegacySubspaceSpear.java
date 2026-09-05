package io.github.lounode.extrabotany.common.entity;

import io.github.lounode.extrabotany.common.handler.DamageHandler;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
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

public final class LegacySubspaceSpear extends ThrowableItemProjectile {
    public static final EntityType<LegacySubspaceSpear> TYPE = EntityType.Builder.<LegacySubspaceSpear>of(LegacySubspaceSpear::new, MobCategory.MISC)
            .sized(.35F, .35F).clientTrackingRange(8).updateInterval(2).build("extrabotany:subspace_spear");
    private float damage = 12;
    private int life = 100;
    public LegacySubspaceSpear(EntityType<? extends LegacySubspaceSpear> type, Level level) { super(type, level); setNoGravity(true); }
    public void configure(float damage, int life) { this.damage = damage; this.life = life; }
    @Override protected Item getDefaultItem() { return BuiltInRegistries.ITEM.get(ResourceLocation.parse("extrabotany:spear_of_subspace")); }
    @Override protected void onHit(HitResult hit) { /* Pierces blocks and targets, as in the archived build. */ }
    @Override public void tick() {
        super.tick(); setNoGravity(true);
        if (level().isClientSide()) { level().addParticle(ParticleTypes.END_ROD, getX(), getY(), getZ(), 0, 0, 0); return; }
        var owner = getOwner();
        for (var target : level().getEntitiesOfClass(LivingEntity.class, new AABB(getX(), getY(), getZ(), xOld, yOld, zOld).inflate(1, .45, 1))) {
            if (target.hurtTime > 0 || !DamageHandler.INSTANCE.checkPassable(target, owner)) continue;
            float amount = damage * .4F;
            if (target.isAlive() && Float.isFinite(amount) && amount > 0) {
                var magic = damageSources().indirectMagic(this, owner);
                if (target.getHealth() <= amount) target.hurt(magic, Float.MAX_VALUE);
                else { target.setHealth(Math.max(1, target.getHealth() - amount)); target.hurt(magic, .01F); }
            }
            var source = owner instanceof Player player ? damageSources().playerAttack(player)
                    : owner instanceof LivingEntity living ? damageSources().mobAttack(living) : damageSources().generic();
            target.hurt(source, damage * 1.5F);
        }
        if (tickCount > life || !(owner instanceof LivingEntity living) || !living.isAlive()) discard();
    }
    @Override public void addAdditionalSaveData(CompoundTag tag) { super.addAdditionalSaveData(tag); tag.putFloat("Damage", damage); tag.putInt("Life", life); }
    @Override public void readAdditionalSaveData(CompoundTag tag) { super.readAdditionalSaveData(tag); damage = tag.getFloat("Damage"); life = tag.getInt("Life"); }
}
