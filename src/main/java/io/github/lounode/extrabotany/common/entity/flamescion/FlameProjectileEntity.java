package io.github.lounode.extrabotany.common.entity.flamescion;

import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 薪炎的飞行效果实体的基类。它沿直线飞行，穿过方块和实体，并且每隔几 tick 对周围的生物造成火焰伤害。
 * The base class for the flying effect entities of the Flamescion. It flies in a straight line, goes through blocks and entities, and does flame damage to the mobs around it at an interval of some ticks.
 */
public abstract class FlameProjectileEntity extends ThrowableProjectile {
	private static final double SPAWN_HEIGHT = 0.5;

	protected FlameProjectileEntity(EntityType<? extends FlameProjectileEntity> type, Level level) {
		super(type, level);
		setNoGravity(true);
	}

	/**
	 * 把实体放在玩家脚下上方 0.5 格处，并让它朝给定的方向以每 tick 1 格的速度飞行。
	 * Puts the entity 0.5 blocks above the feet of the player and makes it fly in the given direction at 1 block for each tick.
	 */
	protected void launchFrom(Player owner, Vec3 direction) {
		setOwner(owner);
		setPos(owner.getX(), owner.getY() + SPAWN_HEIGHT, owner.getZ());
		setDeltaMovement(direction.normalize());
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {}

	protected abstract int getLifetimeTicks();

	protected abstract int getDamageIntervalTicks();

	protected abstract float getDamageRange();

	protected abstract float getDamage();

	@Override
	public void tick() {
		super.tick();
		if (level().isClientSide()) {
			return;
		}
		if (tickCount % getDamageIntervalTicks() == 0) {
			FlamescionAreaEntity.damageInRange(this, getOwner() instanceof Player player ? player : null, getDamageRange(), getDamage());
		}
		if (tickCount >= getLifetimeTicks()) {
			discard();
		}
	}

	@Override
	protected void onHit(HitResult result) {
		// 实体穿过方块和生物。伤害由 tick 里的范围检测造成。
		// The entity goes through blocks and mobs. The range check in the tick does the damage.
	}

	@Override
	protected boolean canHitEntity(Entity target) {
		return false;
	}
}
