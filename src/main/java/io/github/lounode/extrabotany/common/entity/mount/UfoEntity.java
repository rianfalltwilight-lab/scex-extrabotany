package io.github.lounode.extrabotany.common.entity.mount;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;

import org.jetbrains.annotations.Nullable;

import io.github.lounode.extrabotany.common.entity.ExtraBotanyEntityType;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

import java.util.UUID;

/**
 * 飞碟（UFO）。它不受重力影响，朝骑手看的水平方向飞行。跳跃键上升，疾跑键下降。
 * Cosmic Car (UFO). Gravity does not apply to it, and it flies in the horizontal direction that the rider looks at. The jump key goes up and the sprint key goes down.
 *
 * <p>骑手可以抓住飞碟正下方 16 格内最近的生物，并且带着它飞行。
 * <p>The rider can catch the nearest mob within 16 blocks directly below the car and carry it.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code EntityUfo}。
 * <p>Source: ExtraMeteorP 1.16 {@code EntityUfo}.
 *
 * <p>和原版的差异：
 * <p>Differences from the original:
 * <ul>
 * <li>原版由客户端选择要抓的实体，服务端直接信任客户端发来的实体编号。这里由服务端选择目标。
 * In the original the client selects the entity to catch, and the server trusts the entity ID from the client. In this port the server selects the target.</li>
 * <li>不能抓 Boss。只有在可以伤害对方玩家时才能抓玩家。
 * A boss cannot be caught. A player can only be caught when the rider can damage that player.</li>
 * <li>原版用实体编号保存被抓的实体，重新加载后编号失效。这里保存 UUID。
 * The original saves the caught entity by its entity ID, which is not valid after a reload. This port saves the UUID.</li>
 * <li>原版根据骑手视角的正负号决定车头方向，所以侧飞时车头可能朝反方向。这里按实际的运动方向计算。
 * The original selects the heading from the sign of the rider yaw, so the car can point the wrong way during a sideways flight. This port uses the actual movement direction.</li>
 * <li>原版的拉力速度固定是 0.75，所以被抓的实体在悬挂点附近来回抖动。这里在靠近悬挂点时减小速度。
 * The pull speed of the original is always 0.75, so the caught entity shakes near the hang point. This port decreases the speed near the hang point.</li>
 * </ul>
 */
public class UfoEntity extends MountEntity {
	private static final String TAG_CAUGHT = "CaughtUuid";
	private static final int NO_ENTITY = -1;

	private static final EntityDataAccessor<Integer> CAUGHT_ID = SynchedEntityData.defineId(UfoEntity.class, EntityDataSerializers.INT);

	private static final float SPEED = 0.75F;
	private static final float SIDE_FACTOR = 0.75F;
	private static final float BACK_FACTOR = 0.6F;
	private static final double VERTICAL_SPEED = 0.35;
	private static final float CATCH_RANGE = 16F;
	private static final double PULL_SPEED = 0.75;
	private static final double HANG_DISTANCE = 2.0;
	private static final double LIFT_UP = 0.33;
	private static final double LIFT_DOWN = 0.37;
	private static final double SEAT_HEIGHT = 0.855;

	@Nullable
	private UUID caughtUuid;

	public UfoEntity(EntityType<? extends UfoEntity> type, Level level) {
		super(type, level);
		setNoGravity(true);
	}

	public UfoEntity(Level level, Vec3 pos) {
		this(ExtraBotanyEntityType.UFO, level);
		setPos(pos);
		xo = pos.x;
		yo = pos.y;
		zo = pos.z;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(CAUGHT_ID, NO_ENTITY);
	}

	/**
	 * 被抓住的实体的编号。没有时是 -1。
	 * The entity ID of the caught entity. The value is -1 when there is none.
	 */
	public int getCaughtId() {
		return entityData.get(CAUGHT_ID);
	}

	@Override
	protected boolean hasMountGravity() {
		return false;
	}

	@Override
	protected double getSeatHeight() {
		return SEAT_HEIGHT;
	}

	@Override
	protected Item getDropItem() {
		return ExtraBotanyItems.cosmicCarKey;
	}

	@Override
	protected void steer() {
		// 飞碟的速度在 tickMount 中直接设置，所以这里不需要处理。
		// tickMount sets the velocity of the car directly, so no work is necessary here.
	}

	@Override
	protected void clampRotation(Entity passenger) {
		// 骑手可以自由转头。
		// The rider can turn the head freely.
	}

	@Override
	protected void tickMount(@Nullable Player rider) {
		if (rider != null && isControlledByLocalInstance()) {
			fly(rider);
		}
		tickCaughtEntity();
	}

	private void fly(Player rider) {
		float yaw = rider.getYRot() * Mth.DEG_TO_RAD;
		float pitch = rider.getXRot() * Mth.DEG_TO_RAD;
		Vec3 forward = new Vec3(-Mth.sin(yaw) * Mth.cos(pitch) * SPEED, 0.0, Mth.cos(yaw) * Mth.cos(pitch) * SPEED);

		Vec3 motion = Vec3.ZERO;
		if (isInputDown(INPUT_FORWARD)) {
			motion = motion.add(forward);
		}
		if (isInputDown(INPUT_LEFT)) {
			motion = motion.add(forward.yRot(Mth.HALF_PI).scale(SIDE_FACTOR));
		}
		if (isInputDown(INPUT_RIGHT)) {
			motion = motion.add(forward.yRot(-Mth.HALF_PI).scale(SIDE_FACTOR));
		}
		if (isInputDown(INPUT_BACK)) {
			motion = motion.add(forward.yRot(Mth.PI).scale(BACK_FACTOR));
		}
		if (motion.horizontalDistanceSqr() > 1.0E-6) {
			setYRot((float) (Mth.atan2(-motion.x, motion.z) * Mth.RAD_TO_DEG));
		}

		if (isInputDown(INPUT_JUMP)) {
			motion = motion.add(0.0, VERTICAL_SPEED, 0.0);
		} else if (isInputDown(INPUT_SPRINT)) {
			motion = motion.add(0.0, -VERTICAL_SPEED, 0.0);
		}
		setDeltaMovement(motion);
	}

	/**
	 * 抓住或者放开飞碟下方的生物。只在服务端调用。
	 * Catches or releases the mob below the car. Only the server calls this.
	 */
	public void toggleCatch() {
		if (level().isClientSide()) {
			return;
		}
		if (getCaughtId() != NO_ENTITY || caughtUuid != null) {
			release();
			return;
		}

		BlockPos pos = blockPosition();
		AABB below = new AABB(pos.getX() - 1.5, pos.getY() - CATCH_RANGE, pos.getZ() - 1.5,
				pos.getX() + 2.0, pos.getY() - 0.5, pos.getZ() + 2.0);
		LivingEntity nearest = null;
		double nearestDistance = CATCH_RANGE;
		for (LivingEntity living : level().getEntitiesOfClass(LivingEntity.class, below, this::canCatch)) {
			double distance = living.distanceTo(this);
			if (distance < nearestDistance) {
				nearestDistance = distance;
				nearest = living;
			}
		}
		if (nearest != null) {
			caughtUuid = nearest.getUUID();
			entityData.set(CAUGHT_ID, nearest.getId());
		}
	}

	private boolean canCatch(LivingEntity target) {
		if (!target.isAlive() || target.isSpectator() || hasPassenger(target) || target.isPassenger()) {
			return false;
		}
		if (target.getType().is(Tags.EntityTypes.BOSSES)) {
			return false;
		}
		if (target instanceof Player targetPlayer) {
			return getFirstPassenger() instanceof Player rider && rider.canHarmPlayer(targetPlayer);
		}
		return true;
	}

	private void release() {
		caughtUuid = null;
		entityData.set(CAUGHT_ID, NO_ENTITY);
	}

	private void tickCaughtEntity() {
		if (!level().isClientSide() && getCaughtId() == NO_ENTITY && caughtUuid != null) {
			// 读档之后，根据 UUID 重新找到被抓的实体。
			// After a load, find the caught entity again from its UUID.
			Entity loaded = ((ServerLevel) level()).getEntity(caughtUuid);
			if (loaded == null) {
				return;
			}
			entityData.set(CAUGHT_ID, loaded.getId());
		}

		int id = getCaughtId();
		if (id == NO_ENTITY) {
			return;
		}
		Entity caught = level().getEntity(id);
		if (caught == null || !caught.isAlive() || caught.isPassenger() || caught.distanceTo(this) >= CATCH_RANGE) {
			if (!level().isClientSide()) {
				release();
			}
			return;
		}

		// 玩家的运动由他自己的客户端控制。其他实体的运动由服务端控制。
		// The client of a player controls the movement of that player. The server controls the movement of other entities.
		boolean movesHere = caught instanceof Player player ? player.isLocalPlayer() : !level().isClientSide();
		if (!movesHere) {
			return;
		}
		Vec3 pull = new Vec3(getX() - caught.getX(), getY() - HANG_DISTANCE - caught.getY(), getZ() - caught.getZ());
		// 距离小于拉力速度时直接移动到悬挂点，这样实体不会越过悬挂点来回抖动。
		// When the distance is less than the pull speed, move directly to the hang point, so that the entity does not go past the point and shake.
		caught.setDeltaMovement(pull.lengthSqr() > PULL_SPEED * PULL_SPEED ? pull.normalize().scale(PULL_SPEED) : pull);
		if (isInputDown(INPUT_JUMP)) {
			caught.setPos(caught.getX(), caught.getY() + LIFT_UP, caught.getZ());
		} else if (isInputDown(INPUT_SPRINT)) {
			caught.setPos(caught.getX(), caught.getY() - LIFT_DOWN, caught.getZ());
		}
		caught.resetFallDistance();
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		if (caughtUuid != null) {
			tag.putUUID(TAG_CAUGHT, caughtUuid);
		}
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		caughtUuid = tag.hasUUID(TAG_CAUGHT) ? tag.getUUID(TAG_CAUGHT) : null;
		entityData.set(CAUGHT_ID, NO_ENTITY);
	}
}
