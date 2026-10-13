package io.github.lounode.extrabotany.common.entity.projectile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import io.github.lounode.extrabotany.common.handler.DamageHandler;

import java.util.List;

/**
 * 飞向一个固定坐标的投射物的基类。它没有重力，并且穿过方块和实体。
 * The base class for a projectile that flies to a fixed position. The projectile has no gravity and goes through blocks and entities.
 *
 * <p>子类在 {@link #tick()} 里用 {@link #findTargets(double)} 搜索目标并造成伤害。
 * <p>A subclass uses {@link #findTargets(double)} in {@link #tick()} to find targets and to apply damage.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code EntityProjectileBase}。
 * <p>Source: ExtraMeteorP 1.16 {@code EntityProjectileBase}.
 */
public abstract class TargetedProjectileEntity extends ThrowableProjectile {
	private static final String TAG_TARGET_X = "TargetX";
	private static final String TAG_TARGET_Y = "TargetY";
	private static final String TAG_TARGET_Z = "TargetZ";

	private Vec3 targetPos = Vec3.ZERO;

	protected TargetedProjectileEntity(EntityType<? extends TargetedProjectileEntity> type, Level level) {
		super(type, level);
		setNoGravity(true);
	}

	public Vec3 getTargetPos() {
		return targetPos;
	}

	public void setTargetPos(Vec3 targetPos) {
		this.targetPos = targetPos;
	}

	/**
	 * 让投射物朝向目标坐标，并以指定速度飞行。
	 * Points the projectile at the target position and makes it fly at the given speed.
	 *
	 * @param speed 每 tick 移动的格数。 The number of blocks for each tick.
	 */
	public void faceTarget(double speed) {
		Vec3 direction = targetPos.subtract(position());
		if (direction.lengthSqr() < 1.0E-8) {
			return;
		}
		Vec3 motion = direction.normalize().scale(speed);
		setDeltaMovement(motion);
		setYRot((float) (Mth.atan2(motion.x, motion.z) * Mth.RAD_TO_DEG));
		setXRot((float) (Mth.atan2(motion.y, motion.horizontalDistance()) * Mth.RAD_TO_DEG));
		yRotO = getYRot();
		xRotO = getXRot();
	}

	/**
	 * 取得发射者。发射者不存在或者已死亡时返回 {@code null}。
	 * Gets the thrower. Returns {@code null} when the thrower does not exist or is dead.
	 */
	@Nullable
	protected LivingEntity getLivingOwner() {
		return getOwner() instanceof LivingEntity living && living.isAlive() ? living : null;
	}

	/**
	 * 搜索这一 tick 飞过的路径附近、发射者可以伤害的生物。
	 * Finds the living entities near the path of this tick that the thrower can damage.
	 *
	 * @param radius 路径包围盒向外扩展的格数。 The number of blocks by which the path box grows.
	 */
	protected List<LivingEntity> findTargets(double radius) {
		AABB path = new AABB(getX(), getY(), getZ(), xOld, yOld, zOld).inflate(radius);
		List<LivingEntity> entities = level().getEntitiesOfClass(LivingEntity.class, path);
		return DamageHandler.INSTANCE.getFilteredEntities(entities, getOwner());
	}

	@Override
	protected void onHit(HitResult result) {
		// 原版的投射物不会被方块或实体挡住。伤害由子类按范围结算。
		// The projectile of the original does not stop at a block or an entity. The subclass applies damage by area.
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {}

	@Override
	protected double getDefaultGravity() {
		return 0;
	}

	@Override
	public boolean ignoreExplosion(Explosion explosion) {
		return true;
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putDouble(TAG_TARGET_X, targetPos.x);
		tag.putDouble(TAG_TARGET_Y, targetPos.y);
		tag.putDouble(TAG_TARGET_Z, targetPos.z);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		targetPos = new Vec3(tag.getDouble(TAG_TARGET_X), tag.getDouble(TAG_TARGET_Y), tag.getDouble(TAG_TARGET_Z));
	}
}
