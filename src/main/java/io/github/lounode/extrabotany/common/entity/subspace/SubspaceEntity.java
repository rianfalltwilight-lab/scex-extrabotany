package io.github.lounode.extrabotany.common.entity.subspace;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidType;

import org.jetbrains.annotations.Nullable;

import io.github.lounode.extrabotany.common.entity.ExtraBotanyEntityType;
import io.github.lounode.extrabotany.common.entity.OwnedEntity;
import io.github.lounode.extrabotany.common.entity.gaia.VoidHerrscher;

/**
 * 亚空间裂隙。它停在原地，经过一段延迟后张开，然后发射投射物。
 * Subspace rift. It stays in place, opens after a delay, and then shoots projectiles.
 *
 * <p>裂隙有三种模式，见 {@link Mode}。
 * <p>A rift has three modes. See {@link Mode}.
 *
 * <p>来源：ExtraMeteorP 1.12 {@code EntitySubspace}。
 * <p>Source: ExtraMeteorP 1.12 {@code EntitySubspace}.
 */
public class SubspaceEntity extends OwnedEntity {
	/**
	 * 裂隙的模式。序号和原版的 {@code type} 数值相同，并且写进存档。
	 * The mode of a rift. The ordinal agrees with the {@code type} value of the original, and it is saved.
	 */
	public enum Mode {
		/**
		 * 玩家蓄力技能的裂隙：最多发射 5 道金色魔力脉冲。
		 * The rift of the charged skill of a player: it shoots a maximum of 5 golden mana bursts.
		 */
		BURSTS(5),
		/**
		 * 玩家左键技能的裂隙：发射 1 支亚空之矛。
		 * The rift of the left-click skill of a player: it shoots 1 Subspace Spear.
		 */
		SPEAR(1),
		/**
		 * 空之律者的裂隙：最多发射 6 道朝向玩家的魔力脉冲。
		 * The rift of the Herrscher of the Void: it shoots a maximum of 6 mana bursts in the direction of a player.
		 */
		BOSS_BURSTS(6);

		private final int maxShots;

		Mode(int maxShots) {
			this.maxShots = maxShots;
		}

		static Mode byId(int id) {
			return id >= 0 && id < values().length ? values()[id] : BURSTS;
		}
	}

	private static final String TAG_LIVE_TICKS = "LiveTicks";
	private static final String TAG_DELAY = "Delay";
	private static final String TAG_INTERVAL = "Interval";
	private static final String TAG_COUNT = "Count";
	private static final String TAG_MODE = "Type";
	private static final String TAG_SIZE = "Size";
	private static final String TAG_ROTATION = "Rotation";

	private static final EntityDataAccessor<Integer> LIVE_TICKS = SynchedEntityData.defineId(SubspaceEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DELAY = SynchedEntityData.defineId(SubspaceEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Float> SIZE = SynchedEntityData.defineId(SubspaceEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> ROTATION = SynchedEntityData.defineId(SubspaceEntity.class, EntityDataSerializers.FLOAT);

	private static final float SPEAR_DAMAGE = 12F;
	private static final float BOSS_SPEAR_DAMAGE = 14F;
	private static final float SPEAR_SPEED = 2.45F;
	private static final float SPEAR_INACCURACY = 1F;
	private static final int SPEAR_LIFE_TICKS = 100;
	private static final int SPEAR_DELAY_TICKS = 8;

	private Mode mode = Mode.BURSTS;
	private int interval = 10;
	private int shots;

	public SubspaceEntity(EntityType<? extends SubspaceEntity> type, Level level) {
		super(type, level);
	}

	/**
	 * @param liveTicks 裂隙张开之后存在的 tick 数。 The number of ticks that the rift stays after it opens.
	 * @param delay     裂隙张开之前等待的 tick 数。 The number of ticks before the rift opens.
	 * @param interval  两次发射之间的 tick 数。 The number of ticks between two shots.
	 * @param size      裂隙的半径，单位是格。 The radius of the rift, in blocks.
	 * @param rotation  裂隙绕竖直轴的角度，单位是度。 The angle of the rift around the vertical axis, in degrees.
	 */
	public SubspaceEntity(Level level, LivingEntity owner, Mode mode, int liveTicks, int delay, int interval, float size, float rotation) {
		this(ExtraBotanyEntityType.SUBSPACE, level);
		setOwner(owner);
		configure(mode, liveTicks, delay, interval, size, rotation);
	}

	private void configure(Mode mode, int liveTicks, int delay, int interval, float size, float rotation) {
		this.mode = mode;
		this.interval = Math.max(1, interval);
		entityData.set(LIVE_TICKS, liveTicks);
		entityData.set(DELAY, delay);
		entityData.set(SIZE, size);
		entityData.set(ROTATION, rotation);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(LIVE_TICKS, 0);
		builder.define(DELAY, 0);
		builder.define(SIZE, 0F);
		builder.define(ROTATION, 0F);
	}

	public int getLiveTicks() {
		return entityData.get(LIVE_TICKS);
	}

	public int getDelay() {
		return entityData.get(DELAY);
	}

	public float getSize() {
		return entityData.get(SIZE);
	}

	public float getRotation() {
		return entityData.get(ROTATION);
	}

	/**
	 * 裂隙当前的显示大小。它在延迟结束后用 10 tick 张开，在存在时间结束后用 5 tick 闭合。
	 * The size of the rift on the screen at this time. It opens in 10 ticks after the delay, and it closes in 5 ticks after its live time.
	 */
	public float getRenderScale(float partialTick) {
		float age = tickCount + partialTick;
		if (age < getLiveTicks()) {
			return Math.min(getSize(), Math.max(0F, (age - getDelay()) / 10F));
		}
		return Math.max(0F, getSize() - (age - getLiveTicks()) / 5F);
	}

	@Override
	public void tick() {
		super.tick();
		if (level().isClientSide()) {
			return;
		}
		LivingEntity owner = getLivingOwner();
		if (owner == null) {
			discard();
			return;
		}
		int delay = getDelay();
		int liveTicks = getLiveTicks();
		if (tickCount < delay) {
			return;
		}
		if (tickCount > liveTicks + delay) {
			discard();
			return;
		}
		if (shots >= mode.maxShots) {
			return;
		}

		if (mode == Mode.SPEAR) {
			if (tickCount > delay + SPEAR_DELAY_TICKS) {
				shootSpear(owner);
				shots++;
			}
			return;
		}
		if (tickCount % interval == 0 && tickCount > delay + 5 && tickCount < liveTicks - delay - 10) {
			Player aimingPlayer = findAimingPlayer(owner);
			if (aimingPlayer == null) {
				discard();
				return;
			}
			level().addFreshEntity(SubspaceBursts.create(owner, aimingPlayer, position(), mode == Mode.BOSS_BURSTS));
			shots++;
		}
	}

	/**
	 * 返回决定脉冲朝向的玩家。玩家的裂隙沿玩家自己的视线发射。律者的裂隙朝向它附近的一名玩家。
	 * Returns the player that gives the direction of the bursts. The rift of a player shoots along the view of that player. The rift of the Herrscher points at a player near it.
	 */
	@Nullable
	private Player findAimingPlayer(LivingEntity owner) {
		if (mode == Mode.BURSTS) {
			return owner instanceof Player player ? player : null;
		}
		if (owner instanceof VoidHerrscher boss) {
			return boss.getPlayersAround().stream().findFirst().orElse(null);
		}
		return null;
	}

	private void shootSpear(LivingEntity owner) {
		float damage = owner instanceof VoidHerrscher ? BOSS_SPEAR_DAMAGE : SPEAR_DAMAGE;
		SubspaceSpearEntity spear = new SubspaceSpearEntity(level(), owner, damage, SPEAR_LIFE_TICKS);
		spear.setPos(getX(), getY() - 0.75, getZ());
		spear.shootFromRotation(owner, owner.getXRot(), owner.getYRot(), 0F, SPEAR_SPEED, SPEAR_INACCURACY);
		level().addFreshEntity(spear);
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putInt(TAG_LIVE_TICKS, getLiveTicks());
		tag.putInt(TAG_DELAY, getDelay());
		tag.putInt(TAG_INTERVAL, interval);
		tag.putInt(TAG_COUNT, shots);
		tag.putInt(TAG_MODE, mode.ordinal());
		tag.putFloat(TAG_SIZE, getSize());
		tag.putFloat(TAG_ROTATION, getRotation());
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		configure(Mode.byId(tag.getInt(TAG_MODE)), tag.getInt(TAG_LIVE_TICKS), tag.getInt(TAG_DELAY),
				tag.getInt(TAG_INTERVAL), tag.getFloat(TAG_SIZE), tag.getFloat(TAG_ROTATION));
		shots = tag.getInt(TAG_COUNT);
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	public boolean isPushedByFluid(FluidType type) {
		return false;
	}
}
