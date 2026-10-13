package io.github.lounode.extrabotany.common.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.client.fx.WispParticleData;

import io.github.lounode.extrabotany.common.entity.ExtraBotanyEntityType;
import io.github.lounode.extrabotany.common.handler.DamageHandler;
import io.github.lounode.extrabotany.common.handler.DamageHandler.DamageKind;

import java.util.List;

/**
 * 波涌之刃的剑气。它命中目标后消失，然后从另一个方向再次打击，最多追加 3 次。
 * The beam of the Influx Waver. The beam disappears after a hit, then strikes again from a different direction. It strikes a maximum of 3 more times.
 *
 * <p>目标存活时追击同一个目标。目标死亡时追击附近的另一个目标。
 * <p>When the target is alive, the next strike follows the same target. When the target is dead, the next strike follows a different target near it.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code EntityInfluxWaverProjectile}。
 * <p>Source: ExtraMeteorP 1.16 {@code EntityInfluxWaverProjectile}.
 */
public class InfluxWaverProjectileEntity extends TargetedProjectileEntity {
	public static final int LIFETIME_TICKS = 60;
	private static final String TAG_STRIKES = "Strikes";
	private static final String TAG_REMOVE_AT = "RemoveAt";
	private static final String TAG_NEXT = "Next";
	private static final double HIT_RADIUS = 2;
	private static final double RETARGET_RADIUS = 5;
	private static final int RESPAWN_DELAY_TICKS = 4;
	private static final double RESPAWN_DISTANCE = 6;
	private static final double RESPAWN_SPEED = 0.8;
	private static final int NOT_SCHEDULED = -1;

	/**
	 * 剩余的追加打击次数。
	 * The number of strikes that remain.
	 */
	private int strikes;
	/**
	 * 命中发生时的 tick。{@link #NOT_SCHEDULED} 表示还没有命中。
	 * The tick of the hit. {@link #NOT_SCHEDULED} means that no hit occurred.
	 */
	private int hitTick = NOT_SCHEDULED;
	@Nullable
	private BlockPos nextTarget;

	public InfluxWaverProjectileEntity(EntityType<? extends InfluxWaverProjectileEntity> type, Level level) {
		super(type, level);
	}

	public InfluxWaverProjectileEntity(Level level, LivingEntity thrower, Vec3 start, Vec3 target, double speed, int strikes) {
		this(ExtraBotanyEntityType.INFLUX_WAVER_PROJECTILE, level);
		setOwner(thrower);
		setPos(start);
		setTargetPos(target);
		faceTarget(speed);
		this.strikes = strikes;
	}

	@Override
	public void tick() {
		LivingEntity thrower = getLivingOwner();
		if (!level().isClientSide()) {
			if (hitTick != NOT_SCHEDULED && tickCount >= hitTick + RESPAWN_DELAY_TICKS) {
				if (nextTarget != null && thrower != null) {
					level().addFreshEntity(makeNextStrike(thrower, nextTarget));
				}
				discard();
				return;
			}
			if (thrower == null) {
				discard();
				return;
			}
		}
		if (tickCount >= LIFETIME_TICKS) {
			discard();
			return;
		}

		super.tick();

		if (level().isClientSide()) {
			if (tickCount % 2 == 0) {
				level().addParticle(WispParticleData.wisp(0.3F, 0.1F, 0.1F, 0.85F, 1F), getX(), getY(), getZ(), 0, 0, 0);
			}
			return;
		}
		if (hitTick != NOT_SCHEDULED) {
			return;
		}

		List<LivingEntity> targets = findTargets(HIT_RADIUS);
		if (targets.isEmpty()) {
			return;
		}
		LivingEntity target = targets.getFirst();
		target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
		if (thrower instanceof Player) {
			DamageHandler.INSTANCE.dmg(target, thrower, 12F, DamageKind.NEUTRAL);
		} else {
			if (target.invulnerableTime == 0) {
				DamageHandler.INSTANCE.dmg(target, thrower, 2.5F, DamageKind.LIFE_LOSING);
			}
			DamageHandler.INSTANCE.dmg(target, thrower, 7F, DamageKind.MAGIC);
		}

		if (strikes <= 0) {
			return;
		}
		if (target.isAlive()) {
			scheduleNextStrike(target);
		} else {
			AABB area = new AABB(getX(), getY(), getZ(), xOld, yOld, zOld).inflate(HIT_RADIUS + RETARGET_RADIUS);
			List<LivingEntity> others = DamageHandler.INSTANCE.getFilteredEntities(
					level().getEntitiesOfClass(LivingEntity.class, area), thrower);
			if (!others.isEmpty()) {
				scheduleNextStrike(others.getFirst());
			}
		}
	}

	private void scheduleNextStrike(LivingEntity target) {
		nextTarget = target.blockPosition().above();
		hitTick = tickCount;
	}

	/**
	 * 在目标周围的球面上随机选一个起点，生成下一次打击。
	 * Selects a random start point on a sphere around the target and makes the next strike.
	 */
	private InfluxWaverProjectileEntity makeNextStrike(LivingEntity thrower, BlockPos target) {
		double yaw = -Math.PI + 2 * Math.PI * random.nextDouble();
		double pitch = 0.12 * Math.PI * random.nextDouble() + 0.28 * Math.PI;
		Vec3 start = new Vec3(
				target.getX() + RESPAWN_DISTANCE * Math.sin(pitch) * Math.cos(yaw),
				target.getY() + RESPAWN_DISTANCE * Math.cos(pitch),
				target.getZ() + RESPAWN_DISTANCE * Math.sin(pitch) * Math.sin(yaw));
		return new InfluxWaverProjectileEntity(level(), thrower, start, Vec3.atLowerCornerOf(target), RESPAWN_SPEED, strikes - 1);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putInt(TAG_STRIKES, strikes);
		tag.putInt(TAG_REMOVE_AT, hitTick);
		if (nextTarget != null) {
			tag.putLong(TAG_NEXT, nextTarget.asLong());
		}
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		strikes = tag.getInt(TAG_STRIKES);
		hitTick = tag.contains(TAG_REMOVE_AT) ? tag.getInt(TAG_REMOVE_AT) : NOT_SCHEDULED;
		nextTarget = tag.contains(TAG_NEXT) ? BlockPos.of(tag.getLong(TAG_NEXT)) : null;
	}
}
