package io.github.lounode.extrabotany.common.entity.judah;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import vazkii.botania.client.fx.SparkleParticleData;

import io.github.lounode.extrabotany.common.entity.ExtraBotanyEntityType;
import io.github.lounode.extrabotany.common.handler.DamageHandler;
import io.github.lounode.extrabotany.common.item.relic.JudahOathItem.Variant;

/**
 * 被投出的犹大的誓约。它落到地面后进入"待命"状态，展开一个不断扩大的结界。
 * A thrown Judah Oath. After it comes down on the ground it goes into the standby state and opens a field that grows.
 *
 * <p>待命时：
 * <p>In standby:
 * <ul>
 * <li>结界的半径从 5 格长到 13.5 格。结界里的生物得到缓慢 X。
 * The radius of the field grows from 5 to 13.5 blocks. The mobs in the field get Slowness X.</li>
 * <li>犹大和 KIRA：先向上射出 13 支装饰用的光矛。20 tick 之后，每 10 tick 从目标上方 10 格落下一支真正的光矛，一共 13 支。
 * Judah and KIRA: first 13 spears for decoration go up. After 20 ticks, one real spear comes down from 10 blocks above the target on each 10th tick, 13 in total.</li>
 * <li>樱花：20 tick 之后，每 30 tick 放出 5 把沿五角星的边飞行的剑，一共 3 轮。
 * Sakura: after 20 ticks, 5 swords that fly along the edges of a five-pointed star are released on each 30th tick, for 3 rounds.</li>
 * </ul>
 *
 * <p>待命 140 tick 或者存在 300 tick 之后，誓约消失。
 * <p>The oath goes away after 140 ticks in standby or after 300 ticks in total.
 *
 * <p>来源：ExtraMeteorP 1.12 {@code EntityJudahOath}。
 * <p>Source: ExtraMeteorP 1.12 {@code EntityJudahOath}.
 *
 * <p>和原版的差异：原版用整数坐标反复旋转来计算五角星的顶点，每次旋转都有取整误差。这里用精确的角度计算。
 * <p>Difference from the original: the original calculates the points of the star with repeated rotations of integer coordinates, and each rotation has a rounding error. This port uses exact angles.
 */
public class JudahOathEntity extends ThrowableProjectile {
	private static final String TAG_VARIANT = "Variant";
	private static final String TAG_DAMAGE = "Damage";
	private static final String TAG_ROTATION = "Rotation";

	private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(JudahOathEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Float> ROTATION = SynchedEntityData.defineId(JudahOathEntity.class, EntityDataSerializers.FLOAT);

	// 每种外观的粒子颜色：红、绿、蓝。
	// The particle color of each appearance: red, green, blue.
	private static final float[][] COLORS = { { 0.85F, 0.6F, 0.02F }, { 0.01F, 0.6F, 0.75F }, { 1F, 0.8F, 0.8F } };

	private static final double GRAVITY = 0.15;
	private static final double FALL_DAMPING = 0.6;
	private static final float START_RANGE = 5F;
	private static final float MAX_RANGE = 13F;
	private static final float RANGE_GROWTH = 0.5F;
	private static final float FIELD_INSET = 2.5F;
	private static final int STANDBY_LIFETIME_TICKS = 140;
	private static final int LIFETIME_TICKS = 300;
	private static final int ATTACK_START_TICKS = 20;
	private static final int SPEAR_COUNT = 13;
	private static final int FAKE_SPEAR_INTERVAL_TICKS = 4;
	private static final int SPEAR_INTERVAL_TICKS = 10;
	private static final double SPEAR_DROP_HEIGHT = 10.0;
	private static final int SWORD_ROUND_INTERVAL_TICKS = 30;
	private static final int SWORD_ROUNDS = 3;
	private static final int STAR_POINTS = 5;
	private static final double STAR_RADIUS = 11.0;
	private static final float SWORD_DAMAGE = 6F;
	private static final int PARTICLE_STEP_DEGREES = 6;

	private int damage = 7;
	private boolean standby;
	private float range = START_RANGE;
	private int standbyTicks;
	private int fakeSpears;
	private int attacks;

	public JudahOathEntity(EntityType<? extends JudahOathEntity> type, Level level) {
		super(type, level);
	}

	/**
	 * @param rotation 誓约绕竖直轴的角度，单位是度。它决定图案的朝向和五角星的朝向。 The angle of the oath around the vertical axis, in degrees. It gives the direction of the picture and of the star.
	 */
	public JudahOathEntity(Level level, Player owner, Variant variant, int damage, float rotation) {
		this(ExtraBotanyEntityType.JUDAH_OATH, level);
		setOwner(owner);
		this.damage = damage;
		entityData.set(VARIANT, variant.ordinal());
		entityData.set(ROTATION, rotation);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(VARIANT, 0);
		builder.define(ROTATION, 0F);
	}

	public Variant getVariant() {
		return Variant.byId(entityData.get(VARIANT));
	}

	public float getRotation() {
		return entityData.get(ROTATION);
	}

	@Override
	protected double getDefaultGravity() {
		return GRAVITY;
	}

	@Override
	public void tick() {
		boolean wasStandby = standby;
		if (!standby) {
			setDeltaMovement(getDeltaMovement().multiply(1.0, FALL_DAMPING, 1.0));
			standby = !level().getBlockState(blockPosition().below()).isAir();
		}
		if (standby) {
			// 待命时誓约停在原地，所以不调用投射物的移动逻辑。
			// In standby the oath stays in place, so the movement logic of a projectile is not called.
			setDeltaMovement(Vec3.ZERO);
			baseTick();
		} else {
			super.tick();
		}

		if (!level().isClientSide() && !(getOwner() instanceof Player owner && owner.isAlive())) {
			discard();
			return;
		}
		if (standby) {
			if (!wasStandby && level().isClientSide()) {
				level().addParticle(ParticleTypes.EXPLOSION, getX(), getY(), getZ(), 1.0, 0.0, 0.0);
			}
			tickStandby();
		}
		if (!level().isClientSide() && (standbyTicks > STANDBY_LIFETIME_TICKS || tickCount > LIFETIME_TICKS)) {
			discard();
		}
	}

	private void tickStandby() {
		standbyTicks++;
		if (range <= MAX_RANGE) {
			range += RANGE_GROWTH;
		}
		if (level().isClientSide()) {
			spawnFieldParticles();
			return;
		}
		if (getVariant() == Variant.SAKURA) {
			tickSwords();
		} else {
			tickSpears();
		}
	}

	private void tickSpears() {
		Player owner = (Player) getOwner();
		if (tickCount % FAKE_SPEAR_INTERVAL_TICKS == 0 && fakeSpears < SPEAR_COUNT) {
			spawnSpear(owner, position(), true);
			fakeSpears++;
		}

		// 结界里的生物被减速。光矛瞄准其中的最后一只。结界里没有生物时，光矛落在誓约上。
		// The mobs in the field are slowed. The spears aim at the last of them. When no mob is in the field, the spears come down on the oath.
		Vec3 aim = position();
		AABB field = new AABB(position(), position()).inflate(range - FIELD_INSET);
		for (LivingEntity living : level().getEntitiesOfClass(LivingEntity.class, field)) {
			if (living == owner || !DamageHandler.INSTANCE.checkPassable(living, owner)) {
				continue;
			}
			living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 9));
			aim = living.position();
		}
		if (standbyTicks > ATTACK_START_TICKS && tickCount % SPEAR_INTERVAL_TICKS == 0 && attacks < SPEAR_COUNT) {
			spawnSpear(owner, aim.add(0.0, SPEAR_DROP_HEIGHT, 0.0), false);
			attacks++;
		}
	}

	private void spawnSpear(Player owner, Vec3 pos, boolean fake) {
		JudahSpearEntity spear = new JudahSpearEntity(level(), owner, getVariant(), damage, fake);
		spear.setPos(pos);
		level().addFreshEntity(spear);
	}

	private void tickSwords() {
		if (standbyTicks < ATTACK_START_TICKS || standbyTicks % SWORD_ROUND_INTERVAL_TICKS != 0 || attacks >= SWORD_ROUNDS) {
			return;
		}
		// 五角星的五个顶点。每一轮把星形转 36 度。
		// The five points of the star. Each round turns the star by 36 degrees.
		Vec3[] points = new Vec3[STAR_POINTS];
		for (int i = 0; i < STAR_POINTS; i++) {
			double angle = Math.PI + Math.toRadians(getRotation() + 72.0 * (i + 1) + 36.0 * attacks);
			points[i] = position().add(Math.cos(angle) * STAR_RADIUS, 1.0, Math.sin(angle) * STAR_RADIUS);
		}
		Entity owner = getOwner();
		for (int i = 0; i < STAR_POINTS; i++) {
			// 每把剑从一个顶点飞向隔一个的顶点，这样五条边组成五角星。
			// Each sword flies from one point to the point after the next, so that the five edges make a five-pointed star.
			level().addFreshEntity(new JudahSwordEntity(level(), owner, points[i], points[(i + 2) % STAR_POINTS], SWORD_DAMAGE));
		}
		attacks++;
	}

	private void spawnFieldParticles() {
		float[] color = COLORS[getVariant().ordinal()];
		SparkleParticleData sparkle = SparkleParticleData.sparkle(1F, color[0], color[1], color[2], 3);
		for (int degrees = 0; degrees < 360; degrees += PARTICLE_STEP_DEGREES) {
			double radians = Math.toRadians(degrees);
			level().addParticle(sparkle,
					blockPosition().getX() + 0.5 - Math.cos(radians) * range, getY() + 0.2,
					blockPosition().getZ() + 0.5 - Math.sin(radians) * range, 0.0, 0.0, 0.0);
		}
	}

	@Override
	protected void onHit(HitResult result) {
		// 誓约不通过投射物的命中回调起作用。它在下方有方块时进入待命状态。
		// The oath does not work through the hit callback of a projectile. It goes into standby when a block is below it.
	}

	@Override
	protected boolean canHitEntity(Entity target) {
		return false;
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putInt(TAG_VARIANT, entityData.get(VARIANT));
		tag.putInt(TAG_DAMAGE, damage);
		tag.putFloat(TAG_ROTATION, getRotation());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		entityData.set(VARIANT, Variant.byId(tag.getInt(TAG_VARIANT)).ordinal());
		if (tag.contains(TAG_DAMAGE)) {
			damage = tag.getInt(TAG_DAMAGE);
		}
		entityData.set(ROTATION, tag.getFloat(TAG_ROTATION));
	}
}
