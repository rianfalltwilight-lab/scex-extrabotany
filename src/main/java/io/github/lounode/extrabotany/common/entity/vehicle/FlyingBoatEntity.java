package io.github.lounode.extrabotany.common.entity.vehicle;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import vazkii.botania.api.mana.ManaItemHandler;

import io.github.lounode.extrabotany.common.entity.ExtraBotanyEntityType;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

import java.util.function.BooleanSupplier;

/**
 * 飞艇。和船一样可以划，按跳跃键上升，松开后在空中慢慢下降；骑手每 tick 用 1 点魔力，魔力不够时被放下来。
 * Flying boat. It rows like a boat, rises with the jump key and sinks slowly in the air after the key is released; the rider uses 1 mana each tick and is put down when there is not enough mana.
 *
 * <p>来源：ExtraMeteorP 1.12 {@code EntityFlyingBoat}（对照原版审计 E6）。三种：魔力钢最高离地 32 格，元素钢 64 格（前进 × 1.3），泰拉钢 255 格（前进 × 1.6）。
 * <p>Source: {@code EntityFlyingBoat} of ExtraMeteorP 1.12 (parity audit E6). Three kinds: manasteel at most 32 blocks above the ground, elementium 64 (forward × 1.3), terrasteel 255 (forward × 1.6).
 *
 * <p>1.12 直接改坐标（上升 0.35、下降 0.15 格每 tick），会穿过方块；这里用同样的速度但经过碰撞。摔落不会把飞艇摔碎。
 * <p>1.12 changed the position directly (0.35 up, 0.15 down each tick), which goes through blocks; here the same speeds go through the collision. A fall does not break the flying boat.
 */
public class FlyingBoatEntity extends Boat {
	private static final EntityDataAccessor<Integer> DATA_KIND = SynchedEntityData.defineId(FlyingBoatEntity.class, EntityDataSerializers.INT);
	public static final double RISE = 0.35;
	public static final double SINK = 0.15;
	/**
	 * 客户端在这里放"跳跃键是否按下"。服务端一直是 false。
	 * The client puts "is the jump key down" here. On the server it is always false.
	 */
	public static BooleanSupplier ascendKey = () -> false;

	private boolean inputForward;
	private boolean inputBack;

	public FlyingBoatEntity(EntityType<? extends Boat> type, Level level) {
		super(type, level);
	}

	public FlyingBoatEntity(Level level, double x, double y, double z, Kind kind) {
		this(ExtraBotanyEntityType.FLYING_BOAT, level);
		setPos(x, y, z);
		xo = x;
		yo = y;
		zo = z;
		setKind(kind);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_KIND, Kind.MANASTEEL.ordinal());
	}

	public Kind getKind() {
		return Kind.byId(entityData.get(DATA_KIND));
	}

	public void setKind(Kind kind) {
		entityData.set(DATA_KIND, kind.ordinal());
	}

	// 船在空中不受重力；升降由 tick 控制。 / The boat has no gravity in the air; tick controls the height.
	@Override
	protected double getDefaultGravity() {
		return 0;
	}

	@Override
	public void setInput(boolean left, boolean right, boolean forward, boolean back) {
		super.setInput(left, right, forward, back);
		inputForward = forward;
		inputBack = back;
	}

	/**
	 * 到下面第一个不是空气的方块的格数（加 1），最多 256。
	 * The number of blocks down to the first block that is not air (plus 1), at most 256.
	 */
	public int heightAboveGround() {
		BlockPos pos = blockPosition();
		int height = 0;
		for (int y = 0; y < 255; y++) {
			height++;
			if (!level().getBlockState(pos.below(y)).isAir()) {
				break;
			}
		}
		return height;
	}

	@Override
	public void tick() {
		if (isControlledByLocalInstance()) {
			boolean up = getControllingPassenger() instanceof Player player && player.isLocalPlayer() && ascendKey.getAsBoolean();
			double dy = 0;
			if (up && heightAboveGround() <= getKind().maxHeight) {
				dy = RISE;
			} else if (level().getBlockState(blockPosition().below()).isAir()) {
				dy = -SINK;
			}
			Vec3 motion = getDeltaMovement();
			setDeltaMovement(motion.x, dy, motion.z);
			// 原版船前进每 tick 加 0.04；1.12 飞艇是 0.0625 × 倍数，后退 0.00725 × 倍数。
			// A vanilla boat adds 0.04 forward each tick; the 1.12 flying boat adds 0.0625 × factor, and 0.00725 × factor backward.
			if (level().isClientSide() && isVehicle()) {
				float extra = 0;
				if (inputForward) {
					extra += 0.0625F * getKind().forward - 0.04F;
				}
				if (inputBack) {
					extra -= 0.00725F * getKind().back - 0.005F;
				}
				if (extra != 0) {
					setDeltaMovement(getDeltaMovement().add(Mth.sin(-getYRot() * Mth.DEG_TO_RAD) * extra, 0, Mth.cos(getYRot() * Mth.DEG_TO_RAD) * extra));
				}
			}
		}
		super.tick();
		if (!level().isClientSide() && getFirstPassenger() instanceof Player player
				&& !ManaItemHandler.instance().requestManaExact(new ItemStack(ExtraBotanyItems.manasteelFlyingBoat), player, 1, true)) {
			ejectPassengers();
		}
	}

	@Override
	protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
		resetFallDistance();
	}

	@Override
	public Item getDropItem() {
		return getKind().item();
	}

	@Override
	public ItemStack getPickResult() {
		return new ItemStack(getDropItem());
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putString("Kind", getKind().name);
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		setKind(Kind.byName(tag.getString("Kind")));
	}

	public enum Kind {
		MANASTEEL("manasteel", 32, 1F, 1F),
		ELEMENTIUM("elementium", 64, 1.3F, 1.75F),
		TERRASTEEL("terrasteel", 255, 1.6F, 2.5F);

		public final String name;
		public final int maxHeight;
		public final float forward;
		public final float back;

		Kind(String name, int maxHeight, float forward, float back) {
			this.name = name;
			this.maxHeight = maxHeight;
			this.forward = forward;
			this.back = back;
		}

		public Item item() {
			return switch (this) {
				case MANASTEEL -> ExtraBotanyItems.manasteelFlyingBoat;
				case ELEMENTIUM -> ExtraBotanyItems.elementiumFlyingBoat;
				case TERRASTEEL -> ExtraBotanyItems.terrasteelFlyingBoat;
			};
		}

		public static Kind byId(int id) {
			return id >= 0 && id < values().length ? values()[id] : MANASTEEL;
		}

		public static Kind byName(String name) {
			for (Kind kind : values()) {
				if (kind.name.equals(name)) {
					return kind;
				}
			}
			return MANASTEEL;
		}
	}
}
