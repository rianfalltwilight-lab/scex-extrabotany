package io.github.lounode.extrabotany.common.entity.projectile;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import vazkii.botania.common.entity.FallingStarEntity;

import io.github.lounode.extrabotany.common.entity.ExtraBotanyEntityType;
import io.github.lounode.extrabotany.common.handler.DamageHandler;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

/**
 * 瓶中星召唤的星标。它停在原地 130 tick，持续召唤流星。
 * The star marker that the Bottled Star summons. The marker stays in position for 130 ticks and calls falling stars.
 *
 * <p>每 6 tick 一颗流星落在星标附近。每 4 tick 一颗流星落在 12 格内的一个敌人身上。
 * <p>One star falls near the marker each 6 ticks. One star falls on an enemy within 12 blocks each 4 ticks.
 *
 * <p>来源：ExtraMeteorP 1.12 {@code EntityBottledStar}。
 * <p>Source: ExtraMeteorP 1.12 {@code EntityBottledStar}.
 *
 * <p>和原版的差异：原版的流星从星标正上方向外散开，落点最远在 32 格之外；追踪流星的速度方向写反了，会飞向天空。
 * 这里让两种流星都飞向各自的落点。
 * <p>Difference from the original: the stars of the original start above the marker and spread out, so they land up to 32 blocks away.
 * The tracking star of the original has a reversed velocity and flies up into the sky.
 * In this port the two kinds of star fly to their impact points.
 */
public class BottledStarEntity extends ThrowableItemProjectile {
	public static final int LIFETIME_TICKS = 130;
	private static final int STAR_INTERVAL_TICKS = 6;
	private static final int TRACKING_STAR_INTERVAL_TICKS = 4;
	private static final double SCATTER = 1.8;
	private static final double TRACKING_RANGE = 12;
	private static final double START_HEIGHT = 24;
	private static final double START_SPREAD = 32.4;
	private static final double STAR_SPEED = 1.5;

	public BottledStarEntity(EntityType<? extends BottledStarEntity> type, Level level) {
		super(type, level);
		setNoGravity(true);
	}

	public BottledStarEntity(Level level, LivingEntity thrower) {
		this(ExtraBotanyEntityType.BOTTLED_STAR, level);
		setOwner(thrower);
	}

	@Override
	protected Item getDefaultItem() {
		return ExtraBotanyItems.bottledStar;
	}

	@Override
	public void tick() {
		setDeltaMovement(Vec3.ZERO);
		super.tick();
		setDeltaMovement(Vec3.ZERO);

		if (level().isClientSide()) {
			level().addParticle(ParticleTypes.END_ROD, getX(), getY() + 0.25, getZ(), 0, 0.01, 0);
			return;
		}
		if (tickCount > LIFETIME_TICKS) {
			discard();
			return;
		}
		if (!(getOwner() instanceof LivingEntity thrower) || !thrower.isAlive()) {
			discard();
			return;
		}

		if (tickCount % STAR_INTERVAL_TICKS == 0) {
			Vec3 impact = position().add(
					(random.nextDouble() * 2 - 1) * SCATTER, 0, (random.nextDouble() * 2 - 1) * SCATTER);
			spawnStar(thrower, impact);
		}
		if (tickCount % TRACKING_STAR_INTERVAL_TICKS == 0) {
			AABB area = getBoundingBox().inflate(TRACKING_RANGE);
			for (LivingEntity target : level().getEntitiesOfClass(LivingEntity.class, area)) {
				if (DamageHandler.INSTANCE.checkPassable(target, thrower)) {
					spawnStar(thrower, target.position());
					break;
				}
			}
		}
	}

	/**
	 * 生成一颗飞向指定落点的流星。
	 * Makes a falling star that flies to the given impact point.
	 */
	private void spawnStar(LivingEntity thrower, Vec3 impact) {
		Vec3 offset = new Vec3(
				(random.nextDouble() * 2 - 1) * START_SPREAD,
				START_HEIGHT,
				(random.nextDouble() * 2 - 1) * START_SPREAD);
		FallingStarEntity star = new FallingStarEntity(thrower, level());
		star.setPos(impact.add(offset));
		star.setDeltaMovement(offset.normalize().scale(-STAR_SPEED));
		level().addFreshEntity(star);
	}

	@Override
	protected void onHit(HitResult result) {
		// 星标不会被方块或实体挡住。
		// A block or an entity does not stop the marker.
	}

	@Override
	protected double getDefaultGravity() {
		return 0;
	}
}
