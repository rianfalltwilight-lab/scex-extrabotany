package io.github.lounode.extrabotany.common.entity;

import io.github.lounode.extrabotany.common.entity.gaia.GaiaIII;
import io.github.lounode.extrabotany.common.entity.gaia.behavior.EgoWeaponFire;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import java.util.UUID;

public final class LegacyEgoMinion extends Monster {
    public static final EntityType<LegacyEgoMinion> TYPE = EntityType.Builder.<LegacyEgoMinion>of(LegacyEgoMinion::new, MobCategory.MONSTER)
            .sized(.6F, 1.8F).fireImmune().clientTrackingRange(10).updateInterval(10).build("extrabotany:ego_minion");
    private static final EntityDataAccessor<Integer> VARIETY = SynchedEntityData.defineId(LegacyEgoMinion.class, EntityDataSerializers.INT);
    private UUID summoner;
    private int attackCooldown, stuckTicks;
    public LegacyEgoMinion(EntityType<? extends LegacyEgoMinion> type, Level level) { super(type, level); xpReward = 0; }
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 60).add(Attributes.MOVEMENT_SPEED, .4)
                .add(Attributes.ARMOR, 10).add(Attributes.FOLLOW_RANGE, 32).add(Attributes.KNOCKBACK_RESISTANCE, 1);
    }
    public void setSummoner(GaiaIII boss) { summoner = boss.getUUID(); }
    public int getMinionType() { return entityData.get(VARIETY); }
    public void setMinionType(int value) { entityData.set(VARIETY, value); }
    public static Component pickName(int index) { return Component.literal(new String[]{"ExtraMeteorP", "Vazkii", "Notch", "LexManos"}[Math.floorMod(index, 4)]); }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) { super.defineSynchedData(builder); builder.define(VARIETY, 0); }
    @Override protected void registerGoals() {
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 16)); goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }
    @Override public void aiStep() {
        super.aiStep();
        if (!(level() instanceof ServerLevel server)) return;
        getActiveEffects().stream().filter(effect -> effect.getEffect().value().getCategory() == MobEffectCategory.HARMFUL)
                .map(effect -> effect.getEffect()).toList().forEach(this::removeEffect);
        if (level().getDifficulty() == Difficulty.PEACEFUL || summoner == null
                || !(server.getEntity(summoner) instanceof GaiaIII boss) || !boss.isAlive()) { discard(); return; }
        LivingEntity target = getTarget();
        if (!(target instanceof Player) || !target.isAlive()) {
            target = level().getEntitiesOfClass(Player.class, getBoundingBox().inflate(16), player -> player.isAlive() && !player.isSpectator())
                    .stream().findFirst().orElse(null);
            setTarget(target);
        }
        if (target != null) orbit(target);
        if (attackCooldown > 0) attackCooldown--;
        else if (target instanceof Player) {
            swing(InteractionHand.MAIN_HAND); EgoWeaponFire.fire(this, target, Math.floorMod(getMinionType(), 4));
            attackCooldown = 90 + random.nextInt(40);
        }
    }
    private void orbit(LivingEntity target) {
        var direction = target.getLookAngle().multiply(1, 0, 1);
        if (direction.horizontalDistanceSqr() == 0) {
            double yaw = Math.toRadians(target.getYRot() + 90); direction = new Vec3(Math.cos(yaw), 0, Math.sin(yaw));
        }
        direction = direction.normalize().scale(3.5);
        if (getHealth() <= getMaxHealth() * .5F) { direction = direction.scale(-2); if (tickCount % 40 == 0) heal(2); }
        float angle = (float) (Math.PI / 2 * getMinionType() + Math.floor(tickCount / 100.0) * Math.PI / 4);
        var destination = target.position().add(direction.yRot(angle));
        if (position().distanceTo(destination) >= .5) {
            getMoveControl().setWantedPosition(destination.x, destination.y, destination.z, .7F); stuckTicks++;
        } else stuckTicks = 0;
        if (stuckTicks >= 60) { teleportTo(destination.x, destination.y, destination.z); stuckTicks = 0; }
    }
    @Override public boolean hurt(DamageSource source, float amount) {
        if (!Float.isFinite(amount) || amount <= 0) return false;
        int count = level().getEntitiesOfClass(LegacyEgoMinion.class, getBoundingBox().inflate(8)).size();
        return super.hurt(source, Math.min(20, amount * (1 - Math.min(.6F, count * .15F))));
    }
    @Override public boolean removeWhenFarAway(double distance) { return false; }
    @Override public boolean canBeLeashed() { return false; }
    @Override public boolean isPersistenceRequired() { return true; }
    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag); tag.putInt("MinionType", getMinionType()); if (summoner != null) tag.putUUID("Summoner", summoner);
    }
    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag); setMinionType(tag.getInt("MinionType")); summoner = tag.hasUUID("Summoner") ? tag.getUUID("Summoner") : null;
    }
}
