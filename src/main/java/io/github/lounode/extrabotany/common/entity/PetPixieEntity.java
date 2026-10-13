package io.github.lounode.extrabotany.common.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.client.fx.SparkleParticleData;

import java.util.UUID;

/**
 * 宠物精灵。瓶中精灵召唤它。它跟随主人攻击过的目标，没有目标时攻击附近的怪物。
 * Pet pixie. The Bottled Pixie summons it. The pixie follows the target that its owner attacked. With no target, it attacks a monster near it.
 *
 * <p>它存活 1000 tick，受到的伤害减为五分之一，并且不受主人的伤害。
 * <p>The pixie lives for 1000 ticks, takes one fifth of the damage, and takes no damage from its owner.
 *
 * <p>来源：ExtraMeteorP 1.12 {@code EntityPetPixie}。
 * <p>Source: ExtraMeteorP 1.12 {@code EntityPetPixie}.
 */
public class PetPixieEntity extends FlyingMob {
	private static final String TAG_OWNER = "Owner";
	private static final String TAG_DAMAGE = "Damage";
	private static final int LIFETIME_TICKS = 1000;
	private static final int SEARCH_RANGE = 24;
	private static final int SEARCH_INTERVAL_TICKS = 10;
	private static final double ATTACK_DISTANCE = 3.0;
	private static final double ACCELERATION = 0.75;
	private static final float DAMAGE_TAKEN_FACTOR = 0.2F;

	@Nullable
	private UUID ownerId;
	private float damage;

	public PetPixieEntity(EntityType<? extends PetPixieEntity> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 16.0);
	}

	/**
	 * 设置主人和每次攻击的伤害。
	 * Sets the owner and the damage of each attack.
	 */
	public void setProps(LivingEntity owner, float damage) {
		this.ownerId = owner.getUUID();
		this.damage = damage;
	}

	@Nullable
	private LivingEntity getOwner() {
		if (ownerId == null) {
			return null;
		}
		Player player = level().getPlayerByUUID(ownerId);
		return player != null && player.isAlive() ? player : null;
	}

	@Override
	protected void customServerAiStep() {
		LivingEntity target = getTarget();
		if (target != null) {
			Vec3 toTarget = target.getBoundingBox().getCenter().subtract(position());
			double distanceSqr = toTarget.lengthSqr();
			if (distanceSqr > 1.0E-6) {
				// 加速度和距离成反比。精灵离目标越近，飞得越快。
				// The acceleration is in inverse proportion to the distance. The pixie flies faster when it is nearer to the target.
				setDeltaMovement(getDeltaMovement().add(toTarget.scale(ACCELERATION / distanceSqr)));
			}
			if (distanceSqr < ATTACK_DISTANCE * ATTACK_DISTANCE) {
				attack(target);
			}
		}
		Vec3 motion = getDeltaMovement();
		yBodyRot = (float) Math.atan2(motion.x, motion.z) * (-180.0F / (float) Math.PI);
		setYRot(yBodyRot);
	}

	private void attack(LivingEntity target) {
		LivingEntity owner = getOwner();
		DamageSource source;
		if (owner instanceof Player player) {
			source = damageSources().playerAttack(player);
		} else if (owner != null) {
			source = damageSources().mobAttack(owner);
		} else {
			source = damageSources().mobAttack(this);
		}
		target.hurt(source, damage);
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (source.is(DamageTypes.IN_WALL)) {
			return false;
		}
		if (ownerId != null && source.getEntity() != null && ownerId.equals(source.getEntity().getUUID())) {
			return false;
		}
		return super.hurt(source, amount * DAMAGE_TAKEN_FACTOR);
	}

	@Override
	public void baseTick() {
		super.baseTick();
		if (level().isClientSide()) {
			spawnTrailParticles();
			return;
		}
		if (tickCount > LIFETIME_TICKS) {
			discard();
			return;
		}
		updateTarget();
	}

	/**
	 * 选择攻击目标。主人最近攻击的实体优先。
	 * Selects the attack target. The entity that the owner attacked last has priority.
	 */
	private void updateTarget() {
		LivingEntity owner = getOwner();
		if (owner != null) {
			LivingEntity lastAttacked = owner.getLastHurtMob();
			if (lastAttacked != null && lastAttacked.isAlive() && lastAttacked != this) {
				setTarget(lastAttacked);
				return;
			}
		}
		LivingEntity current = getTarget();
		if (current != null && current.isAlive()) {
			return;
		}
		setTarget(null);
		// 原版每 tick 搜索一次。这里每 10 tick 搜索一次，以降低开销。
		// The original searches on each tick. This port searches one time each 10 ticks to decrease the cost.
		if (tickCount % SEARCH_INTERVAL_TICKS != 0) {
			return;
		}
		AABB area = new AABB(blockPosition()).inflate(SEARCH_RANGE);
		LivingEntity nearest = null;
		double nearestDistance = Double.MAX_VALUE;
		for (LivingEntity candidate : level().getEntitiesOfClass(LivingEntity.class, area,
				entity -> entity instanceof Enemy && entity.isAlive())) {
			double distance = distanceToSqr(candidate);
			if (distance < nearestDistance) {
				nearest = candidate;
				nearestDistance = distance;
			}
		}
		setTarget(nearest);
	}

	private void spawnTrailParticles() {
		level().addParticle(SparkleParticleData.sparkle(3F, 0.08F, 1F, 0.08F, 2), getX(), getY(), getZ(), 0, 0, 0);
		level().addParticle(
				SparkleParticleData.sparkle(0.01F + random.nextFloat() * 0.25F, 0.08F, 1F, 0.08F, 20),
				getX() + (random.nextDouble() - 0.5) * 0.25,
				getY() + 0.5 + (random.nextDouble() - 0.5) * 0.25,
				getZ() + (random.nextDouble() - 0.5) * 0.25,
				0, 0, 0);
	}

	@Override
	public void remove(RemovalReason reason) {
		if (level().isClientSide()) {
			for (int i = 0; i < 12; i++) {
				level().addParticle(
						SparkleParticleData.sparkle(1F + random.nextFloat() * 0.25F, 0.08F, 1F, 0.08F, 5),
						getX() + (random.nextDouble() - 0.5) * 0.25,
						getY() + 0.5 + (random.nextDouble() - 0.5) * 0.25,
						getZ() + (random.nextDouble() - 0.5) * 0.25,
						0, 0, 0);
			}
		}
		super.remove(reason);
	}

	@Override
	public boolean removeWhenFarAway(double distanceToClosestPlayer) {
		return false;
	}

	@Override
	public boolean canBeLeashed() {
		return false;
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		if (ownerId != null) {
			tag.putUUID(TAG_OWNER, ownerId);
		}
		tag.putFloat(TAG_DAMAGE, damage);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		ownerId = tag.hasUUID(TAG_OWNER) ? tag.getUUID(TAG_OWNER) : null;
		damage = tag.getFloat(TAG_DAMAGE);
	}
}
