package io.github.lounode.extrabotany.common.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import org.jetbrains.annotations.Nullable;

import io.github.lounode.extrabotany.common.handler.DamageHandler;
import io.github.lounode.extrabotany.common.sounds.ExtraBotanySounds;


/**
 * 真理之钥。它悬浮在空中 45 tick，每 10 tick 向目标发射一次持续 4 tick 的光束，光束每 tick 造成 4 点冰伤害。
 * Key of Truth. It floats for 45 ticks. On each 10th tick it shoots a beam at its target for 4 ticks, and the beam does 4 ice damage on each tick.
 *
 * <p>目标优先选择主人最近攻击的生物。没有时选择 12 格内的一只怪物。
 * <p>The target is the mob that the owner attacked most recently. If there is none, the target is a monster within 12 blocks.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code EntityKeyOfTruth}。
 * <p>Source: ExtraMeteorP 1.16 {@code EntityKeyOfTruth}.
 *
 * <p>和原版的差异：原版把主人保存在一个静态字段里，所以全部钥匙共用最后一把钥匙的主人。没有主人的钥匙永远不会消失。
 * 这里每把钥匙保存自己的主人，找不到主人或者目标时钥匙消失。
 * <p>Differences from the original: the original keeps the owner in a static field, so all keys share the owner of the newest key. A key with no owner never goes away.
 * In this port each key keeps its own owner, and a key goes away when it cannot find its owner or a target.
 */
public class KeyOfTruthEntity extends PlayerOwnedEntity {
	private static final int NO_TARGET = -1;

	private static final EntityDataAccessor<Float> ROTATION = SynchedEntityData.defineId(KeyOfTruthEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> PITCH = SynchedEntityData.defineId(KeyOfTruthEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Integer> TARGET = SynchedEntityData.defineId(KeyOfTruthEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> SHOOTING = SynchedEntityData.defineId(KeyOfTruthEntity.class, EntityDataSerializers.BOOLEAN);

	private static final int LIFETIME_TICKS = 45;
	private static final int SHOOT_INTERVAL_TICKS = 10;
	private static final int BEAM_TICKS = 4;
	private static final float BEAM_DAMAGE = 4F;
	private static final float TARGET_RANGE = 12F;

	// 原版的初始值是 5，所以钥匙在出现后的前 5 tick 也造成伤害。
	// The initial value in the original is 5, so the key also does damage in the first 5 ticks after it appears.
	private int beamTicks = 5;

	public KeyOfTruthEntity(EntityType<? extends KeyOfTruthEntity> type, Level level) {
		super(type, level);
	}

	public KeyOfTruthEntity(Level level, Player owner) {
		this(ExtraBotanyEntityType.KEY_OF_TRUTH, level);
		setOwner(owner);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(ROTATION, 0F);
		builder.define(PITCH, 0F);
		builder.define(TARGET, NO_TARGET);
		builder.define(SHOOTING, false);
	}

	/**
	 * 钥匙模型绕竖直轴的角度，单位是度。只用于渲染。
	 * The angle of the key model around the vertical axis, in degrees. Only the renderer uses this value.
	 */
	public float getRotation() {
		return entityData.get(ROTATION);
	}

	public void setRotation(float rotation) {
		entityData.set(ROTATION, rotation);
	}

	public float getPitch() {
		return entityData.get(PITCH);
	}

	public void setPitch(float pitch) {
		entityData.set(PITCH, pitch);
	}

	/**
	 * 目标的实体编号。没有目标时是 -1。
	 * The entity ID of the target. The value is -1 when there is no target.
	 */
	public int getTargetId() {
		return entityData.get(TARGET);
	}

	public boolean isShooting() {
		return entityData.get(SHOOTING);
	}


	@Override
	public void tick() {
		super.tick();
		if (level().isClientSide()) {
			return;
		}
		Player owner = getOwner();
		if (owner == null || tickCount >= LIFETIME_TICKS) {
			discard();
			return;
		}

		if (getTargetId() == NO_TARGET) {
			LivingEntity found = findTarget(owner);
			if (found != null) {
				entityData.set(TARGET, found.getId());
			}
		}
		Entity target = level().getEntity(getTargetId());
		if (target == null || !target.isAlive()) {
			discard();
			return;
		}

		lookAt(target);
		setRotation(Mth.wrapDegrees(-getYRot() + 180F));
		setPitch(-getXRot() + 360F);

		if (tickCount % SHOOT_INTERVAL_TICKS == 0 && !isShooting()) {
			level().playSound(null, getX(), getY(), getZ(), ExtraBotanySounds.KEY_OF_TRUTH_SHOOT, SoundSource.PLAYERS, 0.25F, 1F);
			entityData.set(SHOOTING, true);
			beamTicks = BEAM_TICKS;
		}
		if (beamTicks > 0) {
			beamTicks--;
			DamageHandler.INSTANCE.iceAttack(target, owner, BEAM_DAMAGE);
		}
		if (beamTicks == 0) {
			entityData.set(SHOOTING, false);
		}
	}

	@Nullable
	private LivingEntity findTarget(Player owner) {
		LivingEntity lastHurt = owner.getLastHurtMob();
		if (lastHurt != null && lastHurt.isAlive() && lastHurt.hasLineOfSight(owner)
				&& DamageHandler.INSTANCE.checkPassable(lastHurt, owner)) {
			return lastHurt;
		}
		AABB area = new AABB(blockPosition()).inflate(TARGET_RANGE - 0.5);
		for (LivingEntity living : level().getEntitiesOfClass(LivingEntity.class, area, entity -> entity instanceof Enemy)) {
			if (living != owner && DamageHandler.INSTANCE.checkPassable(living, owner)) {
				return living;
			}
		}
		return null;
	}

	/**
	 * 让钥匙朝向目标。目标是生物时朝向它的眼睛。
	 * Turns the key to the target. When the target is a living entity, the key points at its eyes.
	 */
	private void lookAt(Entity target) {
		double dx = target.getX() - getX();
		double dz = target.getZ() - getZ();
		double dy = (target instanceof LivingEntity living ? living.getEyeY() : target.getBoundingBox().getCenter().y) - getEyeY();
		double horizontal = Math.sqrt(dx * dx + dz * dz);
		setYRot((float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90F);
		setXRot((float) -(Mth.atan2(dy, horizontal) * Mth.RAD_TO_DEG));
	}

	@Override
	public boolean ignoreExplosion(Explosion explosion) {
		return true;
	}
}
