package io.github.lounode.extrabotany.common.entity;

import io.github.lounode.extrabotany.common.handler.DamageHandler;
import io.github.lounode.extrabotany.common.item.legacy.LegacyFirstFractal;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import vazkii.botania.client.fx.WispParticleData;

public final class LegacyPhantomSword extends ThrowableItemProjectile {
    public static final EntityType<LegacyPhantomSword> TYPE = EntityType.Builder.<LegacyPhantomSword>of(LegacyPhantomSword::new, MobCategory.MISC)
            .sized(.5F, .5F).clientTrackingRange(4).updateInterval(2).build("extrabotany:phantom_sword");
    private static final EntityDataAccessor<Integer> VARIETY = SynchedEntityData.defineId(LegacyPhantomSword.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DELAY = SynchedEntityData.defineId(LegacyPhantomSword.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> FAKE = SynchedEntityData.defineId(LegacyPhantomSword.class, EntityDataSerializers.BOOLEAN);
    private static final float[][] COLORS = {{.82F,.2F,.58F},{0,.71F,.1F},{.74F,.07F,.32F},{.01F,.45F,.8F},{.05F,.39F,.9F},
            {.38F,.34F,.42F},{.41F,.31F,.14F},{.92F,.92F,.21F},{.61F,.92F,.98F},{.18F,.45F,.43F}};
    private Vec3 targetPos = Vec3.ZERO;
    private float attackBonus;
    private int lifeTicks;
    public LegacyPhantomSword(EntityType<? extends LegacyPhantomSword> type, Level level) { super(type, level); setNoGravity(true); }
    public static LegacyPhantomSword create(LivingEntity owner, Vec3 start, Vec3 target, int delay, int variety) {
        var sword = new LegacyPhantomSword(TYPE, owner.level()); sword.setOwner(owner); sword.setPos(start); sword.targetPos = target;
        sword.entityData.set(DELAY, delay); sword.entityData.set(VARIETY, Math.floorMod(variety, 10));
        sword.attackBonus = owner instanceof Player ? (float) Math.max(0, owner.getAttributeValue(Attributes.ATTACK_DAMAGE) - 9) : 0;
        if (delay <= 0) sword.launch(); return sword;
    }
    private void launch() {
        var delta = targetPos.subtract(position()).normalize().scale(1.05); setDeltaMovement(delta);
        setXRot((float) Math.toDegrees(Math.atan2(delta.y, delta.horizontalDistance())));
        setYRot((float) Math.toDegrees(Math.atan2(delta.x, delta.z)));
    }
    public int delay() { return entityData.get(DELAY); }
    public int variety() { return entityData.get(VARIETY); }
    public boolean fake() { return entityData.get(FAKE); }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder); builder.define(VARIETY, 0); builder.define(DELAY, 0); builder.define(FAKE, false);
    }
    @Override protected Item getDefaultItem() { return BuiltInRegistries.ITEM.get(ResourceLocation.parse("extrabotany:first_fractal")); }
    @Override protected void onHit(HitResult hit) { if (!level().isClientSide() && hit.getType() == HitResult.Type.BLOCK) discard(); }
    @Override public void tick() {
        if (!level().isClientSide() && delay() > 0) { entityData.set(DELAY, delay() - 1); if (delay() == 0) launch(); return; }
        if (fake()) setDeltaMovement(Vec3.ZERO);
        super.tick(); setNoGravity(true);
        if (level().isClientSide()) {
            var color = COLORS[Math.floorMod(variety(), 10)];
            level().addParticle(WispParticleData.wisp(.25F, color[0], color[1], color[2]), getX(), getY(), getZ(), 0, 0, 0);
            return;
        }
        if (getOwner() instanceof LivingEntity owner && !owner.isAlive() || tickCount >= 26) { discard(); return; }
        if (!fake()) for (var target : level().getEntitiesOfClass(LivingEntity.class, new AABB(getX(), getY(), getZ(), xOld, yOld, zOld).inflate(2))) {
            if (target instanceof Animal || target.invulnerableTime > 5 || !DamageHandler.INSTANCE.checkPassable(target, getOwner())) continue;
            float damage = 10;
            var owner = getOwner();
            if (owner instanceof Player player && player.getMainHandItem().getItem() instanceof LegacyFirstFractal)
                damage = (float) (player.getAttributeValue(Attributes.ATTACK_DAMAGE) * Math.min(1 + player.getAttributeValue(Attributes.MOVEMENT_SPEED), 2));
            if (!Float.isFinite(damage) || damage <= 0) continue;
            LegacySwordProjectile.trueMagicDamage(target, owner, damage * .05F);
            target.hurt(damageSources().magic(), damage * .3F);
            var source = owner instanceof Player player ? damageSources().playerAttack(player)
                    : owner instanceof LivingEntity living ? damageSources().mobAttack(living) : damageSources().generic();
            if (target.hurt(source, damage * .65F)) break;
        }
        lifeTicks++;
        if (!fake() && lifeTicks % 6 == 0) {
            var illusion = new LegacyPhantomSword(TYPE, level()); illusion.setOwner(getOwner());
            illusion.entityData.set(FAKE, true); illusion.entityData.set(VARIETY, variety()); illusion.targetPos = targetPos;
            illusion.setPos(position()); illusion.setYRot(getYRot()); illusion.setXRot(getXRot()); level().addFreshEntity(illusion);
        }
    }
    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putDouble("TargetX", targetPos.x); tag.putDouble("TargetY", targetPos.y); tag.putDouble("TargetZ", targetPos.z); tag.putFloat("AttackBonus", attackBonus);
        tag.putInt("Delay", delay()); tag.putInt("LifeTicks", lifeTicks); tag.putInt("Variety", variety()); tag.putBoolean("Fake", fake());
    }
    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        targetPos = new Vec3(tag.getDouble("TargetX"), tag.getDouble("TargetY"), tag.getDouble("TargetZ")); attackBonus = tag.getFloat("AttackBonus");
        entityData.set(DELAY, tag.getInt("Delay")); lifeTicks = tag.getInt("LifeTicks");
        entityData.set(VARIETY, Math.floorMod(tag.getInt("Variety"), 10)); entityData.set(FAKE, tag.getBoolean("Fake"));
    }
}
