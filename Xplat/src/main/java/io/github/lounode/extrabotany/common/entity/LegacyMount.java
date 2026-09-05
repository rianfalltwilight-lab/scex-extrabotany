package io.github.lounode.extrabotany.common.entity;

import io.github.lounode.extrabotany.common.handler.DamageHandler;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.Optional;
import java.util.UUID;

/** Retains both vehicle identities and the deployed NBT/control semantics. */
public final class LegacyMount extends Entity {
    public static final EntityType<LegacyMount> UFO = EntityType.Builder.<LegacyMount>of((type, level) -> new LegacyMount(type, level, true), MobCategory.MISC)
            .sized(2.5F, 1.2F).clientTrackingRange(10).updateInterval(2).build("extrabotany:ufo");
    public static final EntityType<LegacyMount> MOTOR = EntityType.Builder.<LegacyMount>of((type, level) -> new LegacyMount(type, level, false), MobCategory.MISC)
            .sized(1.4F, 1).clientTrackingRange(10).updateInterval(2).build("extrabotany:motor");
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(LegacyMount.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> ENERGY = SynchedEntityData.defineId(LegacyMount.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CYCLONE = SynchedEntityData.defineId(LegacyMount.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CAUGHT = SynchedEntityData.defineId(LegacyMount.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> LEAN = SynchedEntityData.defineId(LegacyMount.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> PITCH = SynchedEntityData.defineId(LegacyMount.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Optional<UUID>> OWNER = SynchedEntityData.defineId(LegacyMount.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Boolean> ACCESSORY = SynchedEntityData.defineId(LegacyMount.class, EntityDataSerializers.BOOLEAN);
    public final boolean flying;
    private UUID caught;
    private int controls, ridingTicks, lerpSteps;
    private double targetX, targetY, targetZ, targetYaw, targetPitch;
    private boolean special;
    public LegacyMount(EntityType<? extends LegacyMount> type, Level level, boolean flying) {
        super(type, level); this.flying = flying; blocksBuilding = true; setNoGravity(flying);
    }
    @Override protected void defineSynchedData(SynchedEntityData.Builder data) {
        data.define(DAMAGE, 0F); data.define(ENERGY, 0); data.define(CYCLONE, 0); data.define(CAUGHT, -1);
        data.define(LEAN, 0F); data.define(PITCH, 0F); data.define(OWNER, Optional.empty()); data.define(ACCESSORY, false);
    }
    public void accessory(boolean value) { entityData.set(ACCESSORY, value); }
    public void owner(UUID value) { entityData.set(OWNER, Optional.ofNullable(value)); }
    public int energy() { return entityData.get(ENERGY); }
    public int ridingTicks() { return ridingTicks; }
    public int cyclone() { return entityData.get(CYCLONE); }
    public float lean() { return entityData.get(LEAN); }
    public float pitch() { return entityData.get(PITCH); }
    public int caughtId() { return entityData.get(CAUGHT); }
    public void input(int controls, boolean special) {
        this.controls = controls & 31; this.special |= special;
        if (flying && special && !level().isClientSide()) toggleCatch();
    }
    private boolean key(int bit) { return (controls & bit) != 0; }
    @Override public void lerpTo(double x, double y, double z, float yaw, float pitch, int steps) {
        targetX = x; targetY = y; targetZ = z; targetYaw = yaw; targetPitch = pitch; lerpSteps = 10;
    }
    @Override public double lerpTargetX() { return lerpSteps > 0 ? targetX : getX(); }
    @Override public double lerpTargetY() { return lerpSteps > 0 ? targetY : getY(); }
    @Override public double lerpTargetZ() { return lerpSteps > 0 ? targetZ : getZ(); }
    @Override public float lerpTargetYRot() { return lerpSteps > 0 ? (float) targetYaw : getYRot(); }
    @Override public float lerpTargetXRot() { return lerpSteps > 0 ? (float) targetPitch : getXRot(); }
    @Override public void tick() {
        super.tick(); setNoGravity(flying);
        if (isControlledByLocalInstance()) { lerpSteps = 0; syncPacketPositionCodec(getX(), getY(), getZ()); }
        if (lerpSteps > 0) { lerpPositionAndRotationStep(lerpSteps, targetX, targetY, targetZ, targetYaw, targetPitch); lerpSteps--; }
        if (getControllingPassenger() instanceof Player player) {
            setYRot(player.getYRot()); ridingTicks++;
            if (isControlledByLocalInstance()) {
                var motion = flying ? airMotion(player) : groundMotion(player);
                setDeltaMovement(motion); move(MoverType.SELF, motion);
                setDeltaMovement(flying ? motion.scale(.9) : motion.multiply(.9, .98, .9));
            } else setDeltaMovement(Vec3.ZERO);
            if (!flying && !level().isClientSide()) motorEffects(player);
            if (!flying && level().isClientSide() && key(1)) {
                var direction = forward(player); level().addParticle(ParticleTypes.SMOKE, getX() - direction.x * .5, getY() + .2, getZ() - direction.z * .5, 0, .02, 0);
            }
        } else {
            ridingTicks = 0; controls = 0;
            if (!level().isClientSide() && entityData.get(ACCESSORY) && tickCount > 3) { discard(); return; }
            var motion = getDeltaMovement();
            if (!flying && !onGround()) motion = motion.add(0, -.04, 0);
            motion = flying ? motion.scale(.6) : motion.multiply(.85, 1, .85);
            setDeltaMovement(motion); move(MoverType.SELF, motion); setDeltaMovement(flying ? motion.scale(.9) : motion.multiply(.9, .98, .9));
        }
        if (flying && !level().isClientSide()) updateCaught();
        if (!flying) for (var entity : level().getEntities(this, getBoundingBox().inflate(.2, -.01, .2), entity -> entity.isPushable() && !hasPassenger(entity))) push(entity);
        fallDistance = 0;
    }
    private Vec3 forward(Player player) { float yaw = player.getYRot() * Mth.DEG_TO_RAD; return new Vec3(-Mth.sin(yaw), 0, Mth.cos(yaw)); }
    private Vec3 airMotion(Player player) {
        var look = player.getLookAngle(); var flat = new Vec3(look.x, 0, look.z); flat = flat.lengthSqr() > .0001 ? flat.normalize() : forward(player);
        var motion = Vec3.ZERO;
        if (key(1)) motion = motion.add(look.scale(.75)); if (key(2)) motion = motion.add(flat.scale(-.45));
        if (key(4)) motion = motion.add(flat.yRot(Mth.HALF_PI).scale(.55)); if (key(8)) motion = motion.add(flat.yRot(-Mth.HALF_PI).scale(.55));
        if (key(16)) motion = motion.add(0, .4, 0);
        return onGround() && motion.y < 0 ? new Vec3(motion.x, 0, motion.z) : motion;
    }
    private Vec3 groundMotion(Player player) {
        var forward = forward(player); var motion = new Vec3(0, getDeltaMovement().y, 0);
        if (key(1)) motion = motion.add(forward.scale(.28)); if (key(2)) motion = motion.add(forward.scale(-.08));
        if (!key(1) && !key(2)) { if (key(4)) motion = motion.add(forward.yRot(Mth.HALF_PI).scale(.06)); if (key(8)) motion = motion.add(forward.yRot(-Mth.HALF_PI).scale(.06)); }
        if (key(1) && horizontalCollision) motion = motion.add(0, .08, 0);
        if (ridingTicks >= 120 && key(16) && energy() >= 200) motion = motion.add(forward.scale(1.65)).add(0, .04, 0);
        if (!onGround() && !isInWater()) motion = motion.add(0, -.04, 0);
        return motion;
    }
    private void motorEffects(Player player) {
        boolean activate = special; special = false;
        entityData.set(LEAN, key(4) ? 5F : key(8) ? -5F : 0F); entityData.set(PITCH, 0F);
        if (ridingTicks < 120) return;
        entityData.set(ENERGY, Math.min(800, energy() + 2));
        if (key(16) && energy() >= 200) { entityData.set(ENERGY, Math.max(0, energy() - 6)); entityData.set(PITCH, -5F); }
        if (activate && cyclone() == 0 && energy() >= 400) {
            entityData.set(CYCLONE, 15); entityData.set(ENERGY, energy() - 400);
            level().playSound(null, blockPosition(), io.github.lounode.extrabotany.common.sounds.ExtraBotanySounds.MOTOR_CYCLONE, net.minecraft.sounds.SoundSource.PLAYERS, 1.2F, 1);
        }
        int ticks = cyclone();
        if (ticks > 0) {
            entityData.set(CYCLONE, ticks - 1); entityData.set(LEAN, ticks > 6 ? -12F : -5F);
            ((ServerLevel) level()).sendParticles(ParticleTypes.CLOUD, getX(), getY() + .5, getZ(), 6, 2, .3, 2, .02);
            if (ticks == 12 || ticks == 6) for (var target : level().getEntitiesOfClass(LivingEntity.class, new AABB(getX()-4,getY()-4,getZ()-4,getX()+4,getY()+4,getZ()+4))) {
                if (target == player || hasPassenger(target) || !DamageHandler.INSTANCE.checkPassable(target, player)) continue;
                target.hurt(damageSources().indirectMagic(this, player), 4.5F); target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2)); player.setLastHurtMob(target);
            }
        }
        if (player.getHealth() < player.getMaxHealth() * .5F) player.heal(.5F);
    }
    private void toggleCatch() {
        if (caught != null || caughtId() != -1) { caught = null; entityData.set(CAUGHT, -1); return; }
        var pos = blockPosition();
        for (var target : level().getEntitiesOfClass(LivingEntity.class, new AABB(pos.getX()-1.5,pos.getY()-16,pos.getZ()-1.5,pos.getX()+2,pos.getY()-.5,pos.getZ()+2)))
            if (target.isAlive() && target != getControllingPassenger() && !target.isPassenger()) { caught = target.getUUID(); entityData.set(CAUGHT, target.getId()); return; }
    }
    private void updateCaught() {
        if (caught == null && caughtId() == -1) return;
        var target = caught == null ? level().getEntity(caughtId()) : ((ServerLevel) level()).getEntity(caught);
        if (target == null) { entityData.set(CAUGHT, -1); return; }
        if (!(target instanceof LivingEntity living) || !living.isAlive() || distanceTo(target) >= 16 || target.isPassenger()) { caught = null; entityData.set(CAUGHT, -1); return; }
        caught = target.getUUID(); entityData.set(CAUGHT, target.getId());
        var motion = new Vec3(getX()-target.getX(),getY()-2-target.getY(),getZ()-target.getZ());
        if (motion.lengthSqr() > .0001) target.setDeltaMovement(motion.normalize().scale(.75));
        if (key(16)) target.setPos(target.getX(), target.getY()+.33, target.getZ()); target.fallDistance = 0;
    }
    @Override public void push(Entity target) {
        super.push(target);
        if (!flying && !level().isClientSide() && getControllingPassenger() instanceof Player player && target instanceof LivingEntity living
                && target != player && !hasPassenger(target) && DamageHandler.INSTANCE.checkPassable(target, player)
                && living.hurt(damageSources().indirectMagic(this, player), 7)) { living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,40,2)); player.setLastHurtMob(living); }
    }
    @Override public InteractionResult interact(Player player, InteractionHand hand) {
        if (player.isSecondaryUseActive()) return InteractionResult.PASS;
        return level().isClientSide() ? InteractionResult.SUCCESS : player.startRiding(this) ? InteractionResult.CONSUME : InteractionResult.PASS;
    }
    @Override public boolean hurt(DamageSource source, float amount) {
        if (isInvulnerableTo(source)) return false;
        if (!level().isClientSide() && !isRemoved()) {
            var attacker = source.getEntity(); if (attacker != null && hasPassenger(attacker)) return false;
            if (attacker instanceof Player) entityData.set(DAMAGE, entityData.get(DAMAGE) + amount * 10);
            boolean creative = attacker instanceof Player player && player.getAbilities().instabuild;
            if (creative || entityData.get(DAMAGE) > 40) {
                if (!creative && !entityData.get(ACCESSORY) && level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                    var stack = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(flying ? "extrabotany:cosmic_car_key" : "extrabotany:motor")));
                    if (!flying) entityData.get(OWNER).ifPresent(uuid -> io.github.lounode.extrabotany.common.helper.ItemNBTHelper.setString(stack,"soulbindUUID",uuid.toString()));
                    spawnAtLocation(stack);
                }
                discard();
            }
        }
        return true;
    }
    @Override protected void positionRider(Entity passenger, MoveFunction position) { if (hasPassenger(passenger)) position.accept(passenger,getX(),getY()+(flying?.6:.35),getZ()); }
    @Override public LivingEntity getControllingPassenger() { return getFirstPassenger() instanceof LivingEntity living ? living : null; }
    @Override protected boolean canAddPassenger(Entity passenger) { return getPassengers().isEmpty(); }
    @Override public boolean canBeCollidedWith() { return !flying; }
    @Override public boolean canCollideWith(Entity entity) { return entity.canBeCollidedWith() && !isPassengerOfSameVehicle(entity); }
    @Override public boolean isPickable() { return !isRemoved(); }
    @Override protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("DamageTaken",entityData.get(DAMAGE)); tag.putBoolean("AccessoryMount",entityData.get(ACCESSORY));
        if (flying) { if(caught!=null) tag.putUUID("CaughtUuid",caught); }
        else { tag.putInt("TectonicEnergy",energy()); tag.putInt("CycloneTicks",cyclone()); tag.putFloat("Lean",lean()); tag.putFloat("Pitch",pitch()); entityData.get(OWNER).ifPresent(uuid->tag.putUUID("Owner",uuid)); }
    }
    @Override protected void readAdditionalSaveData(CompoundTag tag) {
        entityData.set(DAMAGE,tag.getFloat("DamageTaken")); accessory(tag.getBoolean("AccessoryMount"));
        if(flying) { caught=tag.hasUUID("CaughtUuid")?tag.getUUID("CaughtUuid"):null; entityData.set(CAUGHT,-1); }
        else { entityData.set(ENERGY,Mth.clamp(tag.getInt("TectonicEnergy"),0,800)); entityData.set(CYCLONE,Math.max(0,tag.getInt("CycloneTicks"))); entityData.set(LEAN,tag.getFloat("Lean")); entityData.set(PITCH,tag.getFloat("Pitch")); owner(tag.hasUUID("Owner")?tag.getUUID("Owner"):null); }
    }
}
