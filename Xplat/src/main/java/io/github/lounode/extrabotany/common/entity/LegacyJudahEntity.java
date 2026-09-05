package io.github.lounode.extrabotany.common.entity;

import io.github.lounode.extrabotany.common.handler.DamageHandler;
import io.github.lounode.extrabotany.common.item.legacy.LegacyJudahItem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class LegacyJudahEntity extends ThrowableItemProjectile {
    public static final EntityType<LegacyJudahEntity> OATH = EntityType.Builder.<LegacyJudahEntity>of((type, level) -> new LegacyJudahEntity(type, level, false), MobCategory.MISC)
            .sized(.35F, .35F).clientTrackingRange(8).updateInterval(2).build("extrabotany:judah_oath");
    public static final EntityType<LegacyJudahEntity> SPEAR = EntityType.Builder.<LegacyJudahEntity>of((type, level) -> new LegacyJudahEntity(type, level, true), MobCategory.MISC)
            .sized(.35F, .35F).clientTrackingRange(8).updateInterval(2).build("extrabotany:judah_spear");
    private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(LegacyJudahEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> FAKE = SynchedEntityData.defineId(LegacyJudahEntity.class, EntityDataSerializers.BOOLEAN);
    private static final float[][] COLORS = {{.85F,.6F,.02F},{.01F,.6F,.75F},{1,.8F,.8F}};
    private final boolean spear;
    private boolean landed, hit;
    private float range = 5, damage = 7;
    private int fakeCount, attackCount, standbyTicks;
    private LegacyJudahEntity(EntityType<? extends LegacyJudahEntity> type, Level level, boolean spear) { super(type, level); this.spear = spear; setNoGravity(spear); }
    public static LegacyJudahEntity create(boolean spear, Entity owner, int variant, boolean fake) {
        var entity = new LegacyJudahEntity(spear ? SPEAR : OATH, owner.level(), spear); entity.setOwner(owner);
        entity.variant(variant); entity.entityData.set(FAKE, fake); return entity;
    }
    private void variant(int value) { int id = value >= 0 && value < 3 ? value : 0; entityData.set(VARIANT, id); setItem(new ItemStack(LegacyJudahItem.itemFor(id))); }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) { super.defineSynchedData(builder); builder.define(VARIANT, 0); builder.define(FAKE, false); }
    @Override protected Item getDefaultItem() { return LegacyJudahItem.itemFor(0); }
    @Override protected void onHit(HitResult result) { /* Legacy field objects do not collide through the projectile hit callback. */ }
    @Override public void tick() {
        super.tick();
        int variant = entityData.get(VARIANT); var color = COLORS[variant];
        if (spear) {
            setNoGravity(true);
            level().addParticle(ParticleTypes.ENCHANT, getX(), getY(), getZ(), color[0] * .02, color[1] * .02, color[2] * .02);
            setPos(getX(), getY() + (entityData.get(FAKE) ? .75 : -.95), getZ());
            if (!level().isClientSide() && !entityData.get(FAKE) && !hit) {
                var box = new AABB(getX(), getY() - 6, getZ(), xOld, yOld + 5, zOld).inflate(1.3, 0, 1.3);
                for (var target : level().getEntitiesOfClass(LivingEntity.class, box)) {
                    if (!DamageHandler.INSTANCE.checkPassable(target, getOwner())) continue;
                    var owner = getOwner();
                    var source = owner instanceof Player player ? damageSources().playerAttack(player)
                            : owner instanceof LivingEntity living ? damageSources().mobAttack(living) : damageSources().lightningBolt();
                    target.hurt(source, damage * 1.6F);
                    if (target.isAlive() && damage > 0) {
                        var magic = damageSources().indirectMagic(this, owner);
                        if (target.getHealth() <= damage * .15F) target.hurt(magic, Float.MAX_VALUE);
                        else { target.setHealth(Math.max(1, target.getHealth() - damage * .15F)); target.hurt(magic, .01F); }
                    }
                    hit = true; break;
                }
            }
            if (!level().isClientSide() && tickCount > 100) discard();
            return;
        }
        if (!landed) {
            level().addParticle(ParticleTypes.END_ROD, getX(), getY(), getZ(), 0, 0, 0);
            setDeltaMovement(getDeltaMovement().multiply(1, .6, 1));
            if (!level().getBlockState(blockPosition().below()).is(Blocks.AIR) || onGround()) {
                landed = true; setDeltaMovement(Vec3.ZERO); level().addParticle(ParticleTypes.EXPLOSION, getX(), getY(), getZ(), 0, 0, 0);
            }
        } else {
            for (int i = 0; i < 360; i += 16) level().addParticle(ParticleTypes.ENCHANT,
                    getX() + .5 - Math.cos(Math.toRadians(i)) * range, getY() + .2, getZ() + .5 - Math.sin(Math.toRadians(i)) * range,
                    color[0] * .02, color[1] * .02, color[2] * .02);
            setDeltaMovement(Vec3.ZERO); standbyTicks++; if (range <= 13) range += .5F;
            if (!level().isClientSide()) standby(variant);
        }
        if (!level().isClientSide() && (standbyTicks > 140 || tickCount > 300 || !(getOwner() instanceof Player player) || !player.isAlive())) discard();
    }
    private void standby(int variant) {
        if (variant == 2) {
            if (standbyTicks >= 20 && standbyTicks % 30 == 0 && attackCount <= 2) {
                var points = new Vec3[5];
                for (int i = 0; i < 5; i++) { double angle = Math.PI * 2 / 5 * i + Math.toRadians(getYRot() + attackCount * 36); points[i] = position().add(Math.cos(angle) * 11, 1, Math.sin(angle) * 11); }
                for (int i = 0; i < 5; i++) { var sword = new LegacyJudahSword(LegacyJudahSword.TYPE, level()); sword.configure(getOwner(), points[i], points[(i + 2) % 5], 6); level().addFreshEntity(sword); }
                attackCount++;
            }
            return;
        }
        if (getOwner() == null) return;
        if (tickCount % 4 == 0 && fakeCount < 13) { spawnSpear(position(), true); fakeCount++; }
        var aim = position().add(0, 10, 0);
        for (var target : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(range))) {
            if (DamageHandler.INSTANCE.checkPassable(target, getOwner())) { target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 9)); aim = target.position().add(0, 10, 0); }
        }
        if (standbyTicks > 20 && tickCount % 10 == 0 && attackCount < 13) { spawnSpear(aim, false); attackCount++; }
    }
    private void spawnSpear(Vec3 pos, boolean fake) { var entity = create(true, getOwner(), entityData.get(VARIANT), fake); entity.setPos(pos); level().addFreshEntity(entity); }
    @Override public void addAdditionalSaveData(CompoundTag tag) { super.addAdditionalSaveData(tag); tag.putInt("Variant", entityData.get(VARIANT)); if (spear) { tag.putFloat("Damage", damage); tag.putBoolean("Fake", entityData.get(FAKE)); } }
    @Override public void readAdditionalSaveData(CompoundTag tag) { super.readAdditionalSaveData(tag); variant(tag.getInt("Variant")); if (spear) { damage = tag.getFloat("Damage"); entityData.set(FAKE, tag.getBoolean("Fake")); } }
}
