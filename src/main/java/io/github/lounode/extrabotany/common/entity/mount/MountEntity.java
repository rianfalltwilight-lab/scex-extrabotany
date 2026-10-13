package io.github.lounode.extrabotany.common.entity.mount;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 坐骑的基类。坐骑的运动规则和船相同：骑乘的玩家在自己的客户端上控制坐骑，服务端接受客户端的位置。
 * The base class for mounts. A mount moves with the rules of a boat: the riding player controls the mount on the client of that player, and the server accepts the position from the client.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code EntityMountable}，以及它继承的 1.16 {@code BoatEntity} 的运动逻辑。
 * <p>Source: ExtraMeteorP 1.16 {@code EntityMountable}, and the movement logic of the 1.16 {@code BoatEntity} that it extends.
 *
 * <p>和原版的差异：1.21.1 的 {@code Boat} 把运动方法设为私有，子类不能重写。所以这个类直接继承 {@link VehicleEntity}，并且自己实现船的运动逻辑。
 * <p>Difference from the original: the {@code Boat} of 1.21.1 has private movement methods, and a subclass cannot override them. Thus this class extends {@link VehicleEntity} directly and has its own copy of the boat movement logic.
 */
public abstract class MountEntity extends VehicleEntity {
	public static final int INPUT_FORWARD = 1;
	public static final int INPUT_BACK = 1 << 1;
	public static final int INPUT_LEFT = 1 << 2;
	public static final int INPUT_RIGHT = 1 << 3;
	public static final int INPUT_JUMP = 1 << 4;
	public static final int INPUT_SPRINT = 1 << 5;
	public static final int INPUT_MASK = (1 << 6) - 1;

	private static final String TAG_DAMAGE = "DamageTaken";
	private static final String TAG_SUMMONED = "AccessoryMount";
	private static final String TAG_ROLL = "Lean";
	private static final String TAG_PITCH = "Pitch";

	private static final EntityDataAccessor<Float> ROLL = SynchedEntityData.defineId(MountEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> PITCH = SynchedEntityData.defineId(MountEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Boolean> SUMMONED = SynchedEntityData.defineId(MountEntity.class, EntityDataSerializers.BOOLEAN);

	private static final double GRAVITY = 0.04;
	private static final float MAX_OUT_OF_CONTROL_TICKS = 60F;
	private static final float DESTROY_DAMAGE = 40F;
	private static final int MAX_PASSENGERS = 2;
	// 被召唤的坐骑在前 3 tick 不检查骑手。这段时间留给服务端让玩家骑上去。
	// A summoned mount does not check for its rider in the first 3 ticks. The server uses this time to put the player on the mount.
	private static final int SUMMON_GRACE_TICKS = 3;

	/**
	 * 坐骑和水、地面的关系。含义和船的状态相同。
	 * The relation of the mount to water and ground. The meaning is the same as for the status of a boat.
	 */
	protected enum Status {
		IN_WATER,
		UNDER_WATER,
		UNDER_FLOWING_WATER,
		ON_LAND,
		IN_AIR
	}

	protected Status status = Status.IN_AIR;
	protected Status oldStatus = Status.IN_AIR;
	protected float deltaRotation;
	private double waterLevel;
	private double lastYd;
	private float outOfControlTicks;
	private int input;

	private int lerpSteps;
	private double lerpX;
	private double lerpY;
	private double lerpZ;
	private double lerpYRot;
	private double lerpXRot;

	protected MountEntity(EntityType<? extends MountEntity> type, Level level) {
		super(type, level);
		this.blocksBuilding = true;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(ROLL, 0F);
		builder.define(PITCH, 0F);
		builder.define(SUMMONED, false);
	}

	// ---------------------------------------------------------------------
	// 输入 / Input
	// ---------------------------------------------------------------------

	/**
	 * 设置骑手当前按下的按键。客户端每 tick 调用，服务端在收到数据包时调用。
	 * Sets the keys that the rider holds at this time. The client calls this on each tick, and the server calls this when the packet arrives.
	 *
	 * @param input {@code INPUT_*} 常量的按位组合。 A bitwise combination of the {@code INPUT_*} constants.
	 */
	public void setInput(int input) {
		this.input = input & INPUT_MASK;
	}

	public int getInput() {
		return input;
	}

	protected boolean isInputDown(int flag) {
		return (input & flag) != 0;
	}

	// ---------------------------------------------------------------------
	// 同步数据 / Synchronized data
	// ---------------------------------------------------------------------

	/**
	 * 车身向左右倾斜的角度，单位是度。只用于渲染。
	 * The angle that the body leans to the left or the right, in degrees. Only the renderer uses this value.
	 */
	public float getRoll() {
		return entityData.get(ROLL);
	}

	public void setRoll(float roll) {
		entityData.set(ROLL, roll);
	}

	/**
	 * 车头抬起的角度，单位是度。只用于渲染。
	 * The angle that the front goes up, in degrees. Only the renderer uses this value.
	 */
	public float getPitch() {
		return entityData.get(PITCH);
	}

	public void setPitch(float pitch) {
		entityData.set(PITCH, pitch);
	}

	/**
	 * 坐骑是否由饰品召唤。被召唤的坐骑在没有玩家骑乘时消失，并且不掉落物品。
	 * Tells if an accessory summoned the mount. A summoned mount goes away when no player rides it, and it drops no item.
	 */
	public boolean isSummoned() {
		return entityData.get(SUMMONED);
	}

	public void setSummoned(boolean summoned) {
		entityData.set(SUMMONED, summoned);
	}

	// ---------------------------------------------------------------------
	// Tick
	// ---------------------------------------------------------------------

	@Override
	public void tick() {
		if (!level().isClientSide() && isSummoned() && tickCount > SUMMON_GRACE_TICKS
				&& !(getFirstPassenger() instanceof Player)) {
			discard();
			return;
		}

		oldStatus = status;
		status = findStatus();
		if (status == Status.UNDER_WATER || status == Status.UNDER_FLOWING_WATER) {
			outOfControlTicks++;
		} else {
			outOfControlTicks = 0F;
		}
		if (!level().isClientSide() && outOfControlTicks >= MAX_OUT_OF_CONTROL_TICKS) {
			ejectPassengers();
		}

		if (getHurtTime() > 0) {
			setHurtTime(getHurtTime() - 1);
		}
		if (getDamage() > 0F) {
			setDamage(getDamage() - 1F);
		}

		super.tick();
		tickLerp();

		if (isControlledByLocalInstance()) {
			applyFriction();
			if (level().isClientSide()) {
				steer();
			}
			move(MoverType.SELF, getDeltaMovement());
		} else {
			setDeltaMovement(Vec3.ZERO);
		}

		checkInsideBlocks();
		pushNearbyEntities();
		tickMount(getFirstPassenger() instanceof Player player ? player : null);
	}

	/**
	 * 每 tick 在运动之后调用一次。子类在这里实现自己的能力。
	 * Called one time on each tick, after the movement. A subclass puts its abilities here.
	 *
	 * @param rider 骑在第一个座位上的玩家。没有玩家时是 {@code null}。 The player on the first seat. The value is {@code null} when there is no player.
	 */
	protected abstract void tickMount(@Nullable Player rider);

	/**
	 * 根据骑手的输入改变速度和朝向。只在控制坐骑的客户端上调用。
	 * Changes the velocity and the direction from the input of the rider. Only the client that controls the mount calls this.
	 */
	protected abstract void steer();

	/**
	 * 坐骑是否受重力影响。
	 * Tells if gravity applies to the mount.
	 */
	protected abstract boolean hasMountGravity();

	private void tickLerp() {
		if (isControlledByLocalInstance()) {
			lerpSteps = 0;
			syncPacketPositionCodec(getX(), getY(), getZ());
		}
		if (lerpSteps > 0) {
			lerpPositionAndRotationStep(lerpSteps, lerpX, lerpY, lerpZ, lerpYRot, lerpXRot);
			lerpSteps--;
		}
	}

	private void pushNearbyEntities() {
		List<Entity> entities = level().getEntities(this, getBoundingBox().inflate(0.2, -0.01, 0.2), EntitySelector.pushableBy(this));
		if (entities.isEmpty()) {
			return;
		}
		// 没有玩家驾驶时，坐骑像船一样把碰到的生物载上来。
		// When no player drives the mount, it takes the mobs that touch it on board, as a boat does.
		boolean canBoard = !level().isClientSide() && !(getControllingPassenger() instanceof Player);
		for (Entity entity : entities) {
			if (entity.hasPassenger(this)) {
				continue;
			}
			if (canBoard
					&& getPassengers().size() < MAX_PASSENGERS
					&& !entity.isPassenger()
					&& entity.getBbWidth() < getBbWidth()
					&& entity instanceof LivingEntity
					&& !(entity instanceof WaterAnimal)
					&& !(entity instanceof Player)) {
				entity.startRiding(this);
			} else {
				push(entity);
			}
		}
	}

	// ---------------------------------------------------------------------
	// 运动 / Movement
	// ---------------------------------------------------------------------

	/**
	 * 应用重力、阻力和浮力。数值来自原版。
	 * Applies gravity, friction and buoyancy. The values come from the original.
	 */
	private void applyFriction() {
		double gravity = hasMountGravity() && !isNoGravity() ? -GRAVITY : 0.0;
		double buoyancy = 0.0;
		float momentum;

		if (oldStatus == Status.IN_AIR && status != Status.IN_AIR && status != Status.ON_LAND) {
			// 坐骑从空中落进水里。把它放到水面上。
			// The mount fell from the air into water. Put it on the water surface.
			waterLevel = getY(1.0);
			double surfaceY = getWaterLevelAbove() - getBbHeight() + 0.101;
			if (level().noCollision(this, getBoundingBox().move(0.0, surfaceY - getY(), 0.0))) {
				setPos(getX(), surfaceY, getZ());
				setDeltaMovement(getDeltaMovement().multiply(1.0, 0.0, 1.0));
				lastYd = 0.0;
			}
			status = Status.IN_WATER;
			return;
		}

		switch (status) {
			case IN_WATER -> {
				buoyancy = (waterLevel - getBoundingBox().minY + 0.1) / getBbHeight();
				momentum = 0.9F;
			}
			case UNDER_FLOWING_WATER -> {
				gravity = -7.0E-4;
				momentum = 0.9F;
			}
			case UNDER_WATER -> {
				buoyancy = 0.01;
				momentum = 0.8F;
			}
			default -> momentum = 0.9F;
		}

		Vec3 motion = getDeltaMovement();
		setDeltaMovement(motion.x * momentum, motion.y + gravity, motion.z * momentum);
		deltaRotation *= momentum;
		if (buoyancy > 0.0) {
			Vec3 floating = getDeltaMovement();
			setDeltaMovement(floating.x, (floating.y + buoyancy * 0.06153846016296973) * 0.75, floating.z);
		}
	}

	private Status findStatus() {
		Status underwater = findUnderwaterStatus();
		if (underwater != null) {
			waterLevel = getBoundingBox().maxY;
			return underwater;
		}
		if (checkInWater()) {
			return Status.IN_WATER;
		}
		return isOnSolidGround() ? Status.ON_LAND : Status.IN_AIR;
	}

	private static boolean isWater(FluidState fluid) {
		return fluid.is(FluidTags.WATER);
	}

	private float getWaterLevelAbove() {
		AABB box = getBoundingBox();
		int minX = Mth.floor(box.minX);
		int maxX = Mth.ceil(box.maxX);
		int minY = Mth.floor(box.maxY);
		int maxY = Mth.ceil(box.maxY - lastYd);
		int minZ = Mth.floor(box.minZ);
		int maxZ = Mth.ceil(box.maxZ);
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

		nextLayer:
		for (int y = minY; y < maxY; y++) {
			float height = 0F;
			for (int x = minX; x < maxX; x++) {
				for (int z = minZ; z < maxZ; z++) {
					pos.set(x, y, z);
					FluidState fluid = level().getFluidState(pos);
					if (isWater(fluid)) {
						height = Math.max(height, fluid.getHeight(level(), pos));
					}
					if (height >= 1F) {
						continue nextLayer;
					}
				}
			}
			if (height < 1F) {
				return pos.getY() + height;
			}
		}
		return maxY + 1;
	}

	private boolean checkInWater() {
		AABB box = getBoundingBox();
		int minX = Mth.floor(box.minX);
		int maxX = Mth.ceil(box.maxX);
		int minY = Mth.floor(box.minY);
		int maxY = Mth.ceil(box.minY + 0.001);
		int minZ = Mth.floor(box.minZ);
		int maxZ = Mth.ceil(box.maxZ);
		boolean inWater = false;
		waterLevel = -Double.MAX_VALUE;
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

		for (int x = minX; x < maxX; x++) {
			for (int y = minY; y < maxY; y++) {
				for (int z = minZ; z < maxZ; z++) {
					pos.set(x, y, z);
					FluidState fluid = level().getFluidState(pos);
					if (isWater(fluid)) {
						float surface = y + fluid.getHeight(level(), pos);
						waterLevel = Math.max(surface, waterLevel);
						inWater |= box.minY < surface;
					}
				}
			}
		}
		return inWater;
	}

	@Nullable
	private Status findUnderwaterStatus() {
		AABB box = getBoundingBox();
		double top = box.maxY + 0.001;
		int minX = Mth.floor(box.minX);
		int maxX = Mth.ceil(box.maxX);
		int minY = Mth.floor(box.maxY);
		int maxY = Mth.ceil(top);
		int minZ = Mth.floor(box.minZ);
		int maxZ = Mth.ceil(box.maxZ);
		boolean underwater = false;
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

		for (int x = minX; x < maxX; x++) {
			for (int y = minY; y < maxY; y++) {
				for (int z = minZ; z < maxZ; z++) {
					pos.set(x, y, z);
					FluidState fluid = level().getFluidState(pos);
					if (isWater(fluid) && top < pos.getY() + fluid.getHeight(level(), pos)) {
						if (!fluid.isSource()) {
							return Status.UNDER_FLOWING_WATER;
						}
						underwater = true;
					}
				}
			}
		}
		return underwater ? Status.UNDER_WATER : null;
	}

	/**
	 * 判断坐骑的底面是否接触到有碰撞箱的方块。
	 * Finds if the bottom of the mount touches a block that has a collision shape.
	 */
	private boolean isOnSolidGround() {
		AABB box = getBoundingBox();
		AABB bottom = new AABB(box.minX, box.minY - 0.001, box.minZ, box.maxX, box.minY, box.maxZ);
		VoxelShape bottomShape = Shapes.create(bottom);
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		// 栅栏这类方块的碰撞箱高于 1 格，所以还要检查下面一层。
		// A block such as a fence has a collision shape taller than 1 block, so the layer below is also checked.
		int topY = Mth.floor(bottom.minY);
		for (int x = Mth.floor(bottom.minX); x < Mth.ceil(bottom.maxX); x++) {
			for (int z = Mth.floor(bottom.minZ); z < Mth.ceil(bottom.maxZ); z++) {
				for (int y = topY - 1; y <= topY; y++) {
					pos.set(x, y, z);
					BlockState state = level().getBlockState(pos);
					VoxelShape shape = state.getCollisionShape(level(), pos);
					if (!shape.isEmpty() && Shapes.joinIsNotEmpty(shape.move(x, y, z), bottomShape, BooleanOp.AND)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
		lastYd = getDeltaMovement().y;
		resetFallDistance();
	}

	@Override
	public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps) {
		lerpX = x;
		lerpY = y;
		lerpZ = z;
		lerpYRot = yRot;
		lerpXRot = xRot;
		lerpSteps = 10;
	}

	@Override
	public double lerpTargetX() {
		return lerpSteps > 0 ? lerpX : getX();
	}

	@Override
	public double lerpTargetY() {
		return lerpSteps > 0 ? lerpY : getY();
	}

	@Override
	public double lerpTargetZ() {
		return lerpSteps > 0 ? lerpZ : getZ();
	}

	@Override
	public float lerpTargetXRot() {
		return lerpSteps > 0 ? (float) lerpXRot : getXRot();
	}

	@Override
	public float lerpTargetYRot() {
		return lerpSteps > 0 ? (float) lerpYRot : getYRot();
	}

	// ---------------------------------------------------------------------
	// 乘客 / Passengers
	// ---------------------------------------------------------------------

	/**
	 * 骑手的座位高度，从坐骑的底面算起。
	 * The seat height of the rider, measured from the bottom of the mount.
	 */
	protected abstract double getSeatHeight();

	@Override
	protected Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions, float scale) {
		float forward = 0F;
		if (getPassengers().size() > 1) {
			forward = getPassengers().indexOf(passenger) == 0 ? 0.2F : -0.6F;
		}
		return new Vec3(0.0, getSeatHeight(), forward).yRot(-getYRot() * Mth.DEG_TO_RAD);
	}

	@Override
	protected void positionRider(Entity passenger, Entity.MoveFunction callback) {
		super.positionRider(passenger, callback);
		passenger.setYRot(passenger.getYRot() + deltaRotation);
		passenger.setYHeadRot(passenger.getYHeadRot() + deltaRotation);
		clampRotation(passenger);
	}

	@Override
	public void onPassengerTurned(Entity passenger) {
		clampRotation(passenger);
	}

	/**
	 * 限制乘客相对于坐骑的转头角度。默认和船相同：左右各 105 度。
	 * Limits the angle that a passenger can turn relative to the mount. The default is the same as for a boat: 105 degrees to each side.
	 */
	protected void clampRotation(Entity passenger) {
		passenger.setYBodyRot(getYRot());
		float relative = Mth.wrapDegrees(passenger.getYRot() - getYRot());
		float clamped = Mth.clamp(relative, -105F, 105F);
		passenger.yRotO += clamped - relative;
		passenger.setYRot(passenger.getYRot() + clamped - relative);
		passenger.setYHeadRot(passenger.getYRot());
	}

	@Override
	protected boolean canAddPassenger(Entity passenger) {
		return getPassengers().size() < MAX_PASSENGERS;
	}

	@Nullable
	@Override
	public LivingEntity getControllingPassenger() {
		return getFirstPassenger() instanceof LivingEntity living ? living : super.getControllingPassenger();
	}

	@Override
	public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
		Vec3 escape = getCollisionHorizontalEscapeVector(getBbWidth() * Mth.SQRT_OF_TWO, passenger.getBbWidth(), passenger.getYRot());
		double x = getX() + escape.x;
		double z = getZ() + escape.z;
		BlockPos upper = BlockPos.containing(x, getBoundingBox().maxY, z);
		BlockPos lower = upper.below();
		if (!level().isWaterAt(lower)) {
			List<Vec3> candidates = new ArrayList<>(2);
			double upperFloor = level().getBlockFloorHeight(upper);
			if (DismountHelper.isBlockFloorValid(upperFloor)) {
				candidates.add(new Vec3(x, upper.getY() + upperFloor, z));
			}
			double lowerFloor = level().getBlockFloorHeight(lower);
			if (DismountHelper.isBlockFloorValid(lowerFloor)) {
				candidates.add(new Vec3(x, lower.getY() + lowerFloor, z));
			}
			for (Pose pose : passenger.getDismountPoses()) {
				for (Vec3 candidate : candidates) {
					if (DismountHelper.canDismountTo(level(), candidate, passenger, pose)) {
						passenger.setPose(pose);
						return candidate;
					}
				}
			}
		}
		return super.getDismountLocationForPassenger(passenger);
	}

	@Override
	public InteractionResult interact(Player player, InteractionHand hand) {
		if (player.isSecondaryUseActive() || outOfControlTicks >= MAX_OUT_OF_CONTROL_TICKS) {
			return InteractionResult.PASS;
		}
		if (level().isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		if (!player.startRiding(this)) {
			return InteractionResult.PASS;
		}
		onPlayerMounted(player);
		return InteractionResult.CONSUME;
	}

	/**
	 * 玩家通过右键骑上坐骑后在服务端调用。
	 * Called on the server after a player gets on the mount with the use key.
	 */
	protected void onPlayerMounted(Player player) {}

	// ---------------------------------------------------------------------
	// 碰撞和伤害 / Collision and damage
	// ---------------------------------------------------------------------

	@Override
	public boolean canCollideWith(Entity entity) {
		return Boat.canVehicleCollide(this, entity);
	}

	@Override
	public boolean canBeCollidedWith() {
		return true;
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	public boolean isPickable() {
		return !isRemoved();
	}

	@Override
	protected Entity.MovementEmission getMovementEmission() {
		return Entity.MovementEmission.EVENTS;
	}

	@Override
	public boolean ignoreExplosion(Explosion explosion) {
		return true;
	}

	@Override
	public void push(Entity entity) {
		if (entity.getBoundingBox().minY <= getBoundingBox().minY) {
			super.push(entity);
		}
	}

	@Override
	public void animateHurt(float yaw) {
		setHurtDir(-getHurtDir());
		setHurtTime(10);
		setDamage(getDamage() * 11F);
	}

	/**
	 * 只有玩家的攻击才会累积损伤。损伤超过 40 时坐骑被拆下。创造模式的玩家一次攻击就拆下坐骑。
	 * Only an attack from a player adds damage. The mount comes apart when the damage is more than 40. A player in creative mode takes the mount apart with one attack.
	 */
	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (isInvulnerableTo(source)) {
			return false;
		}
		if (level().isClientSide() || isRemoved()) {
			return true;
		}
		Entity attacker = source.getEntity();
		// 乘客自己的投射物不会伤到坐骑。
		// A projectile from a passenger does not damage the mount.
		if (!source.isDirect() && attacker != null && hasPassenger(attacker)) {
			return false;
		}

		setHurtDir(-getHurtDir());
		setHurtTime(10);
		markHurt();
		boolean creative = attacker instanceof Player player && player.getAbilities().instabuild;
		if (attacker instanceof Player) {
			setDamage(getDamage() + amount * 10F);
		}
		if (creative || getDamage() > DESTROY_DAMAGE) {
			if (!creative && level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
				ItemStack drop = createDropStack();
				if (!drop.isEmpty()) {
					spawnAtLocation(drop);
				}
			}
			discard();
		}
		return true;
	}

	/**
	 * 创建坐骑被拆下时掉落的物品。被召唤的坐骑不掉落物品。
	 * Makes the item that drops when the mount comes apart. A summoned mount drops no item.
	 */
	protected ItemStack createDropStack() {
		return isSummoned() ? ItemStack.EMPTY : new ItemStack(getDropItem());
	}

	@Nullable
	@Override
	public ItemStack getPickResult() {
		return new ItemStack(getDropItem());
	}

	// ---------------------------------------------------------------------
	// 存档 / Save data
	// ---------------------------------------------------------------------

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		tag.putFloat(TAG_DAMAGE, getDamage());
		tag.putBoolean(TAG_SUMMONED, isSummoned());
		tag.putFloat(TAG_ROLL, getRoll());
		tag.putFloat(TAG_PITCH, getPitch());
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
		setDamage(tag.getFloat(TAG_DAMAGE));
		setSummoned(tag.getBoolean(TAG_SUMMONED));
		setRoll(tag.getFloat(TAG_ROLL));
		setPitch(tag.getFloat(TAG_PITCH));
	}
}
