package io.github.lounode.extrabotany.common.entity;

import io.github.lounode.extrabotany.common.handler.DamageHandler;
import io.github.lounode.extrabotany.common.item.legacy.LegacyKingGardenItem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class LegacyFlowerWeapon extends ThrowableItemProjectile {
    public static final EntityType<LegacyFlowerWeapon> TYPE = EntityType.Builder.<LegacyFlowerWeapon>of(LegacyFlowerWeapon::new, MobCategory.MISC)
            .sized(.35F, .35F).clientTrackingRange(8).updateInterval(2).build("extrabotany:flower_weapon");
    private static final EntityDataAccessor<Boolean> CHARGING = SynchedEntityData.defineId(LegacyFlowerWeapon.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> VARIETY = SynchedEntityData.defineId(LegacyFlowerWeapon.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CHARGE = SynchedEntityData.defineId(LegacyFlowerWeapon.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> LIVE = SynchedEntityData.defineId(LegacyFlowerWeapon.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DELAY = SynchedEntityData.defineId(LegacyFlowerWeapon.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> ROTATION = SynchedEntityData.defineId(LegacyFlowerWeapon.class, EntityDataSerializers.FLOAT);
    public LegacyFlowerWeapon(EntityType<? extends LegacyFlowerWeapon> type, Level level) { super(type, level); setNoGravity(true); }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CHARGING, false); builder.define(VARIETY, 0); builder.define(CHARGE, 0);
        builder.define(LIVE, 0); builder.define(DELAY, 0); builder.define(ROTATION, 0F);
    }
    public void configure(int variety, int delay, float rotation) { entityData.set(VARIETY, variety); entityData.set(DELAY, delay); entityData.set(ROTATION, rotation); }
    @Override protected Item getDefaultItem() { return LegacyKingGardenItem.INSTANCE; }
    @Override protected void onHit(HitResult hit) { /* Legacy weapons pass through blocks; swept living hits are handled below. */ }
    @Override public void tick() {
        setNoGravity(true);
        var owner = getOwner();
        if (!level().isClientSide() && (!(owner instanceof Player player) || !player.isAlive())) { discard(); return; }
        if (!level().isClientSide() && owner instanceof Player player) {
            var held = player.getMainHandItem().is(LegacyKingGardenItem.INSTANCE) ? player.getMainHandItem() : player.getOffhandItem();
            entityData.set(CHARGING, held.is(LegacyKingGardenItem.INSTANCE) && LegacyKingGardenItem.charging(held));
        }
        int live = entityData.get(LIVE), delay = entityData.get(DELAY);
        boolean charging = entityData.get(CHARGING) && live == 0;
        var motion = getDeltaMovement();
        if (charging) {
            setDeltaMovement(Vec3.ZERO); entityData.set(CHARGE, entityData.get(CHARGE) + 1);
            if (!level().isClientSide() && random.nextInt(20) == 0)
                level().playSound(null, blockPosition(), SoundEvents.TRIDENT_RETURN, SoundSource.PLAYERS, .1F, 1 + random.nextFloat() * 3);
        } else {
            if (live < delay) setDeltaMovement(Vec3.ZERO);
            else if (live == delay && owner instanceof Player player) {
                var eye = player.getEyePosition(); var end = eye.add(player.getLookAngle().scale(64));
                var hit = level().clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
                motion = (hit.getType() == HitResult.Type.MISS ? end : hit.getLocation()).subtract(position()).normalize().scale(2);
                setDeltaMovement(motion);
                level().playSound(null, blockPosition(), SoundEvents.TRIDENT_THROW.value(), SoundSource.PLAYERS, 1.4F, .1F + random.nextFloat() * 3);
            }
            entityData.set(LIVE, live + 1);
            if (!level().isClientSide()) {
                for (var target : level().getEntitiesOfClass(LivingEntity.class, new AABB(getX(), getY(), getZ(), xOld, yOld, zOld).inflate(2))) {
                    if (target.hurtTime != 0 || !DamageHandler.INSTANCE.checkPassable(target, owner)) continue;
                    apply(target, (Player) owner);
                    if (entityData.get(VARIETY) == 5) level().explode(this, getX(), getY(), getZ(), 2, Level.ExplosionInteraction.NONE);
                    discard(); break;
                }
            }
        }
        super.tick();
        if (!charging) setDeltaMovement(motion);
        if (level().isClientSide() && live > delay) level().addParticle(ParticleTypes.HAPPY_VILLAGER, getX(), getY(), getZ(), 0, .05, 0);
        if (!level().isClientSide() && live > 200 + delay) discard();
    }
    private void apply(LivingEntity target, Player owner) {
        int variety = entityData.get(VARIETY);
        int[] damage = {9, 7, 7, 8, 12, 5, 7, 7, 10, 6, 6, 8, 9, 5, 5, 8, 8};
        var source = switch (variety) { case 0, 3, 9, 13, 14 -> damageSources().indirectMagic(this, owner);
            case 8 -> damageSources().lava(); default -> damageSources().playerAttack(owner); };
        target.hurt(source, variety >= 0 && variety < damage.length ? damage[variety] : 6);
        if (variety == 1) target.igniteForSeconds(5);
        var effect = switch (variety) { case 2 -> MobEffects.BLINDNESS; case 3 -> MobEffects.CONFUSION;
            case 6 -> MobEffects.WITHER; case 7 -> MobEffects.MOVEMENT_SLOWDOWN; case 9 -> MobEffects.UNLUCK;
            case 10 -> MobEffects.POISON; case 11 -> MobEffects.WEAKNESS; case 13 -> MobEffects.DAMAGE_BOOST;
            case 14 -> MobEffects.ABSORPTION; case 15 -> MobEffects.JUMP; case 16 -> MobEffects.MOVEMENT_SPEED; default -> null; };
        if (effect != null) (variety >= 13 ? owner : target).addEffect(new MobEffectInstance(effect, 200, variety == 6 ? 0 : 1));
    }
    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Charging", entityData.get(CHARGING)); tag.putInt("Variety", entityData.get(VARIETY));
        tag.putInt("ChargeTicks", entityData.get(CHARGE)); tag.putInt("LiveTicks", entityData.get(LIVE));
        tag.putInt("Delay", entityData.get(DELAY)); tag.putFloat("Rotation", entityData.get(ROTATION));
    }
    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(CHARGING, tag.getBoolean("Charging")); entityData.set(VARIETY, tag.getInt("Variety"));
        entityData.set(CHARGE, tag.getInt("ChargeTicks")); entityData.set(LIVE, tag.getInt("LiveTicks"));
        entityData.set(DELAY, tag.getInt("Delay")); entityData.set(ROTATION, tag.getFloat("Rotation"));
    }
}
