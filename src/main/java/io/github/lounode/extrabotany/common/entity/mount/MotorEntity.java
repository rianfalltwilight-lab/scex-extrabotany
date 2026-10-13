package io.github.lounode.extrabotany.common.entity.mount;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.item.Relic;

import io.github.lounode.extrabotany.common.entity.ExtraBotanyEntityType;
import io.github.lounode.extrabotany.common.entity.KeyOfTruthEntity;
import io.github.lounode.extrabotany.common.handler.DamageHandler;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.sounds.ExtraBotanySounds;

import java.util.Optional;
import java.util.UUID;

/**
 * 机车。玩家骑乘 120 tick 后，机车开始积累构造能量，并且解锁三种能力。
 * Armored Motor. After a player rides for 120 ticks, the motor starts to collect tectonic energy and gets three abilities.
 *
 * <ul>
 * <li>按住跳跃键：能量不少于 200 时加速冲刺，每 tick 消耗 6 点能量。
 * Hold the jump key: with 200 energy or more the motor makes a dash and uses 6 energy on each tick.</li>
 * <li>按疾跑键：消耗 400 点能量，做一次旋风攻击，对 4 格内的生物造成两次 4.5 点冰伤害。
 * Push the sprint key: uses 400 energy for one cyclone attack, which does 4.5 ice damage two times to the mobs within 4 blocks.</li>
 * <li>自动：每 15 tick 对 12 格内的一只怪物召唤一把真理之钥。
 * Automatic: on each 15th tick the motor summons one Key of Truth against a monster within 12 blocks.</li>
 * </ul>
 *
 * <p>机车撞到生物时造成 7 点冰伤害。骑手的生命值低于一半时，机车每 tick 治疗 0.5 点。
 * <p>The motor does 7 ice damage to a mob that it hits. When the health of the rider is below one half, the motor heals 0.5 on each tick.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code EntityMotor}。
 * <p>Source: ExtraMeteorP 1.16 {@code EntityMotor}.
 *
 * <p>和原版的差异：
 * <p>Differences from the original:
 * <ul>
 * <li>原版只在冲刺方向的方块不是空气时抬高 1 格，所以机车穿过草丛时会不断升高。这里只在前方有碰撞箱时抬高。
 * The original goes up 1 block when the block in the dash direction is not air, so the motor goes up continuously in tall grass. This port only goes up when the block ahead has a collision shape.</li>
 * <li>原版的冰伤害没有攻击者，所以它无视 PvP 设置和和平友好之证。这里把骑手记为攻击者。
 * The ice damage of the original has no attacker, so it ignores the PvP setting and the Peace Amulet. This port records the rider as the attacker.</li>
 * <li>原版每 tick 都搜索 12 格内的全部生物。这里只在每 15 tick 需要召唤钥匙时搜索。
 * The original searches all mobs within 12 blocks on each tick. This port only searches on each 15th tick, when a key is necessary.</li>
 * </ul>
 */
public class MotorEntity extends MountEntity {
	public static final int MAX_ENERGY = 800;

	private static final String TAG_ENERGY = "TectonicEnergy";
	private static final String TAG_CYCLONE = "CycloneTicks";
	private static final String TAG_OWNER = "Owner";

	private static final EntityDataAccessor<Integer> ENERGY = SynchedEntityData.defineId(MotorEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> CYCLONE_TICKS = SynchedEntityData.defineId(MotorEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Optional<UUID>> OWNER = SynchedEntityData.defineId(MotorEntity.class, EntityDataSerializers.OPTIONAL_UUID);

	private static final int CHARGE_DELAY_TICKS = 120;
	private static final int ENERGY_GAIN = 2;
	private static final int DASH_MIN_ENERGY = 200;
	private static final int DASH_COST = 6;
	private static final float DASH_SPEED = 1.65F;
	private static final int CYCLONE_COST = 400;
	private static final int CYCLONE_DURATION = 15;
	private static final float CYCLONE_RANGE = 4F;
	private static final float CYCLONE_DAMAGE = 4.5F;
	private static final float COLLISION_DAMAGE = 7F;
	private static final int KEY_INTERVAL_TICKS = 15;
	private static final float KEY_RANGE = 12F;
	private static final float THRUST_FORWARD = 0.0625F;
	private static final float THRUST_BACK = 0.006F;
	private static final float THRUST_TURN = 0.005F;
	private static final double CLIMB_SPEED = 0.08;
	private static final double SEAT_HEIGHT = 0.475;
	private static final float WHEEL_RADIUS = 0.75F;

	private int ridingTicks;
	private float wheelAngle;
	private float wheelAngleO;

	public MotorEntity(EntityType<? extends MotorEntity> type, Level level) {
		super(type, level);
	}

	public MotorEntity(Level level, Vec3 pos) {
		this(ExtraBotanyEntityType.MOTOR, level);
		setPos(pos);
		xo = pos.x;
		yo = pos.y;
		zo = pos.z;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(ENERGY, 0);
		builder.define(CYCLONE_TICKS, 0);
		builder.define(OWNER, Optional.empty());
	}

	public int getEnergy() {
		return entityData.get(ENERGY);
	}

	public void setEnergy(int energy) {
		entityData.set(ENERGY, Mth.clamp(energy, 0, MAX_ENERGY));
	}

	public int getCycloneTicks() {
		return entityData.get(CYCLONE_TICKS);
	}

	public void setCycloneTicks(int ticks) {
		entityData.set(CYCLONE_TICKS, Math.max(0, ticks));
	}

	/**
	 * 机车物品绑定的玩家。机车被拆下时，掉落的物品绑定回这名玩家。
	 * The player that the motor item is bound to. When the motor comes apart, the dropped item is bound to this player again.
	 */
	@Nullable
	public UUID getOwnerId() {
		return entityData.get(OWNER).orElse(null);
	}

	public void setOwnerId(@Nullable UUID owner) {
		entityData.set(OWNER, Optional.ofNullable(owner));
	}

	/**
	 * 玩家已经连续骑乘的 tick 数。渲染器用它转动车轮。
	 * The number of ticks that a player has been on the motor without a stop. The renderer uses it to turn the wheels.
	 */
	public int getRidingTicks() {
		return ridingTicks;
	}

	/**
	 * 车轮转过的角度，单位是弧度。只在客户端更新，只用于渲染。
	 * The angle that the wheels have turned, in radians. Only the client updates this value, and only the renderer uses it.
	 */
	public float getWheelAngle(float partialTick) {
		return Mth.lerp(partialTick, wheelAngleO, wheelAngle);
	}

	private void turnWheels() {
		float yaw = getYRot() * Mth.DEG_TO_RAD;
		double forward = (getX() - xo) * -Mth.sin(yaw) + (getZ() - zo) * Mth.cos(yaw);
		wheelAngleO = wheelAngle;
		wheelAngle += (float) (forward / WHEEL_RADIUS);
	}

	@Override
	protected boolean hasMountGravity() {
		// 冲刺时机车不下落。
		// The motor does not fall during a dash.
		return !isDashing();
	}

	private boolean isDashing() {
		return ridingTicks >= CHARGE_DELAY_TICKS && isInputDown(INPUT_JUMP) && getEnergy() >= DASH_MIN_ENERGY;
	}

	@Override
	protected double getSeatHeight() {
		return SEAT_HEIGHT;
	}

	@Override
	protected Item getDropItem() {
		return ExtraBotanyItems.motor;
	}

	@Override
	protected ItemStack createDropStack() {
		ItemStack stack = super.createDropStack();
		UUID owner = getOwnerId();
		if (!stack.isEmpty() && owner != null) {
			Relic relic = Relic.LOOKUP.find(stack);
			if (relic != null) {
				relic.bindToUUID(owner);
			}
		}
		return stack;
	}

	@Override
	protected void onPlayerMounted(Player player) {
		level().playSound(null, getX(), getY(), getZ(), ExtraBotanySounds.MOTOR_RIDE_ON, SoundSource.PLAYERS, 4F, 1F);
	}

	@Override
	protected void steer() {
		if (!isVehicle()) {
			return;
		}
		boolean left = isInputDown(INPUT_LEFT);
		boolean right = isInputDown(INPUT_RIGHT);
		boolean forward = isInputDown(INPUT_FORWARD);
		boolean back = isInputDown(INPUT_BACK);

		float thrust = 0F;
		if (left) {
			deltaRotation -= 1F;
		}
		if (right) {
			deltaRotation += 1F;
		}
		if (right != left && !forward && !back) {
			thrust += THRUST_TURN;
		}
		setYRot(getYRot() + deltaRotation);
		if (forward) {
			thrust += THRUST_FORWARD;
		}
		if (back) {
			thrust -= THRUST_BACK;
		}
		float yaw = getYRot() * Mth.DEG_TO_RAD;
		setDeltaMovement(getDeltaMovement().add(Mth.sin(-yaw) * thrust, 0.0, Mth.cos(yaw) * thrust));
	}

	@Override
	protected void tickMount(@Nullable Player rider) {
		if (level().isClientSide()) {
			turnWheels();
		}
		if (rider == null) {
			ridingTicks = 0;
			if (!level().isClientSide()) {
				setRoll(0F);
				setPitch(0F);
			}
			return;
		}
		ridingTicks++;

		float yaw = getYRot() * Mth.DEG_TO_RAD;
		float pitch = getXRot() * Mth.DEG_TO_RAD;
		Vec3 dash = new Vec3(-Mth.sin(yaw) * Mth.cos(pitch) * DASH_SPEED, getDeltaMovement().y, Mth.cos(yaw) * Mth.cos(pitch) * DASH_SPEED);

		if (level().isClientSide() && isInputDown(INPUT_FORWARD)) {
			level().addParticle(ParticleTypes.FLAME, getX(), getY(), getZ(), -dash.x, 0.15, -dash.z);
		}

		// 骑手的客户端自己控制运动。其他客户端只显示服务端同步的数据。
		// The client of the rider controls the movement itself. Other clients only show the data that the server synchronizes.
		boolean controlling = isControlledByLocalInstance();
		if (controlling || !level().isClientSide()) {
			updateLean();
		}
		if (controlling && isInputDown(INPUT_FORWARD) && horizontalCollision) {
			setDeltaMovement(getDeltaMovement().add(0.0, CLIMB_SPEED, 0.0));
		}
		if (ridingTicks < CHARGE_DELAY_TICKS) {
			return;
		}

		if (controlling && isDashing()) {
			setDeltaMovement(dash);
			BlockPos ahead = BlockPos.containing(position().add(dash));
			if (!level().getBlockState(ahead).getCollisionShape(level(), ahead).isEmpty()) {
				setPos(getX(), getY() + 1.0, getZ());
			}
		}
		if (controlling && getCycloneTicks() > 0) {
			// 旋风攻击时车身快速旋转。
			// The body turns quickly during the cyclone attack.
			deltaRotation += 5F;
			if (getCycloneTicks() > 6) {
				rider.setYRot(rider.getYRot() - 1F);
				rider.setYHeadRot(rider.getYHeadRot() - 1F);
				clampRotation(rider);
			}
		}
		if (!level().isClientSide()) {
			tickAbilities(rider);
		}
	}

	/**
	 * 根据转向和旋风攻击设置车身的姿态。
	 * Sets the pose of the body from the steering and the cyclone attack.
	 */
	private void updateLean() {
		float roll = isInputDown(INPUT_RIGHT) ? -5F : isInputDown(INPUT_LEFT) ? 5F : 0F;
		float pitch = 0F;
		int cyclone = getCycloneTicks();
		if (cyclone > 0) {
			pitch = -5F;
			if (cyclone > 6) {
				roll = -5F;
			}
		}
		setRoll(roll);
		setPitch(pitch);
	}

	/**
	 * 服务端的能力逻辑：能量、真理之钥、旋风攻击和治疗。
	 * The ability logic on the server: energy, the Key of Truth, the cyclone attack and healing.
	 */
	private void tickAbilities(Player rider) {
		int energy = Math.min(MAX_ENERGY, getEnergy() + ENERGY_GAIN);
		if (isInputDown(INPUT_JUMP)) {
			// 能量不足时按住跳跃键会清空能量。这是原版的规则：玩家必须松开按键等待充能。
			// When the energy is too low, a held jump key sets the energy to zero. This is the rule of the original: the player must release the key and wait.
			energy = energy >= DASH_MIN_ENERGY ? Math.max(0, energy - DASH_COST) : 0;
		}

		if (tickCount % KEY_INTERVAL_TICKS == 0) {
			summonKeyOfTruth(rider);
		}

		int cyclone = getCycloneTicks();
		if (cyclone == 0 && isInputDown(INPUT_SPRINT) && energy >= CYCLONE_COST) {
			cyclone = CYCLONE_DURATION;
			energy -= CYCLONE_COST;
			level().playSound(null, getX(), getY(), getZ(), ExtraBotanySounds.MOTOR_CYCLONE, SoundSource.PLAYERS, 1.2F, 1F);
		}
		setEnergy(energy);

		if (rider.getHealth() < rider.getMaxHealth() * 0.5F) {
			rider.heal(0.5F);
		}

		if (cyclone == 12 || cyclone == 6) {
			for (LivingEntity living : level().getEntitiesOfClass(LivingEntity.class, boxAround(rider, CYCLONE_RANGE))) {
				if (living != rider && !hasPassenger(living)
						&& DamageHandler.INSTANCE.iceAttack(living, rider, CYCLONE_DAMAGE)) {
					rider.setLastHurtMob(living);
				}
			}
		}
		if (cyclone > 0) {
			setCycloneTicks(cyclone - 1);
		}
	}

	private void summonKeyOfTruth(Player rider) {
		for (LivingEntity living : level().getEntitiesOfClass(LivingEntity.class, boxAround(this, KEY_RANGE))) {
			if (living == rider || hasPassenger(living) || !living.hasLineOfSight(rider)) {
				continue;
			}
			if (!(living instanceof Enemy) && rider.getLastHurtMob() != living) {
				continue;
			}
			if (!DamageHandler.INSTANCE.checkPassable(living, rider)) {
				continue;
			}

			// 钥匙随机出现在骑手或者目标的上方。
			// The key comes into view above the rider or above the target, at random.
			Entity anchor = random.nextBoolean() ? living : rider;
			KeyOfTruthEntity key = new KeyOfTruthEntity(level(), rider);
			key.setPos(anchor.getX() - random.nextDouble() * 2.0 + 1.0, anchor.getY() + 2.2, anchor.getZ() - random.nextDouble() * 2.0 + 1.0);
			key.setYRot(rider.getYRot());
			key.setPitch(-rider.getXRot());
			key.setRotation(Mth.wrapDegrees(-rider.getYRot() + 180F));
			level().addFreshEntity(key);
			return;
		}
	}

	/**
	 * 以实体所在方块的中心为中心的立方体。范围的算法和原版相同。
	 * A cube with its center at the center of the block that the entity is in. The range calculation is the same as in the original.
	 */
	private static AABB boxAround(Entity entity, float range) {
		return new AABB(entity.blockPosition()).inflate(range - 0.5);
	}

	@Override
	public void push(Entity entity) {
		super.push(entity);
		if (level().isClientSide() || !(getFirstPassenger() instanceof Player rider)) {
			return;
		}
		if (entity == rider || hasPassenger(entity)) {
			return;
		}
		if (DamageHandler.INSTANCE.iceAttack(entity, rider, COLLISION_DAMAGE) && entity instanceof LivingEntity living
				&& !(entity instanceof Player)) {
			rider.setLastHurtMob(living);
		}
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putInt(TAG_ENERGY, getEnergy());
		tag.putInt(TAG_CYCLONE, getCycloneTicks());
		UUID owner = getOwnerId();
		if (owner != null) {
			tag.putUUID(TAG_OWNER, owner);
		}
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		setEnergy(tag.getInt(TAG_ENERGY));
		setCycloneTicks(tag.getInt(TAG_CYCLONE));
		setOwnerId(tag.hasUUID(TAG_OWNER) ? tag.getUUID(TAG_OWNER) : null);
	}
}
