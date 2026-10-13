package io.github.lounode.extrabotany.common.entity.projectile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import vazkii.botania.client.fx.WispParticleData;

import io.github.lounode.extrabotany.common.entity.ExtraBotanyEntityType;
import io.github.lounode.extrabotany.common.handler.DamageHandler;
import io.github.lounode.extrabotany.common.handler.DamageHandler.DamageKind;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

/**
 * 幻影剑。最初分型召唤它。它从目标周围的球面上出现，然后刺向目标。
 * Phantom sword. The First Fractal summons it. The sword appears on a sphere around the target, then flies to the target.
 *
 * <p>飞行中的剑每 6 tick 留下一个静止的残影。残影没有伤害。
 * <p>A sword in flight leaves a motionless afterimage one time each 6 ticks. An afterimage does no damage.
 *
 * <p>伤害分成三部分：65% 物理伤害、30% 魔法伤害、5% 真实伤害。
 * <p>The damage has three parts: 65% physical damage, 30% magic damage and 5% true damage.
 *
 * <p>来源：ExtraMeteorP 1.12 {@code EntityPhantomSword}。
 * <p>Source: ExtraMeteorP 1.12 {@code EntityPhantomSword}.
 */
public class PhantomSwordEntity extends TargetedProjectileEntity {
	public static final int LIFETIME_TICKS = 26;
	public static final int VARIETY_COUNT = 10;
	private static final String TAG_VARIETY = "Variety";
	private static final String TAG_DELAY = "Delay";
	private static final String TAG_FAKE = "Fake";
	private static final double SPAWN_DISTANCE = 13;
	private static final double SPEED = 1.05;
	private static final double HIT_RADIUS = 2;
	private static final int AFTERIMAGE_INTERVAL_TICKS = 6;
	private static final float DEFAULT_DAMAGE = 10F;
	private static final float PHYSICAL_PART = 0.65F;
	private static final float MAGIC_PART = 0.3F;
	private static final float TRUE_PART = 0.05F;

	private static final EntityDataAccessor<Integer> VARIETY =
			SynchedEntityData.defineId(PhantomSwordEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DELAY =
			SynchedEntityData.defineId(PhantomSwordEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> FAKE =
			SynchedEntityData.defineId(PhantomSwordEntity.class, EntityDataSerializers.BOOLEAN);

	/**
	 * 每种外观的粒子颜色（红、绿、蓝）。
	 * The particle color of each variety (red, green, blue).
	 */
	private static final float[][] COLORS = {
			{ 0.82F, 0.2F, 0.58F }, { 0F, 0.71F, 0.10F }, { 0.74F, 0.07F, 0.32F },
			{ 0.01F, 0.45F, 0.8F }, { 0.05F, 0.39F, 0.9F }, { 0.38F, 0.34F, 0.42F }, { 0.41F, 0.31F, 0.14F },
			{ 0.92F, 0.92F, 0.21F }, { 0.61F, 0.92F, 0.98F }, { 0.18F, 0.45F, 0.43F }
	};

	public PhantomSwordEntity(EntityType<? extends PhantomSwordEntity> type, Level level) {
		super(type, level);
	}

	/**
	 * 在目标周围随机选一个起点，生成一把飞向目标的剑。
	 * Selects a random start point around the target and makes a sword that flies to the target.
	 *
	 * @param delay 起飞前等待的 tick 数。 The number of ticks before the flight starts.
	 */
	public static PhantomSwordEntity create(LivingEntity thrower, Vec3 target, int delay) {
		PhantomSwordEntity sword = new PhantomSwordEntity(ExtraBotanyEntityType.PHANTOM_SWORD, thrower.level());
		sword.setOwner(thrower);
		sword.setTargetPos(target);
		sword.setVariety(thrower.getRandom().nextInt(VARIETY_COUNT));
		sword.setDelay(delay);

		double pitch = 0.6 * Math.PI * thrower.getRandom().nextDouble();
		double yaw = -Math.PI + 2 * Math.PI * thrower.getRandom().nextDouble();
		sword.setPos(
				target.x + SPAWN_DISTANCE * Math.sin(pitch) * Math.cos(yaw),
				target.y + SPAWN_DISTANCE * Math.cos(pitch),
				target.z + SPAWN_DISTANCE * Math.sin(pitch) * Math.sin(yaw));
		sword.faceTarget(SPEED);
		return sword;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(VARIETY, 0);
		builder.define(DELAY, 0);
		builder.define(FAKE, false);
	}

	@Override
	public void tick() {
		if (getDelay() > 0) {
			if (!level().isClientSide()) {
				setDelay(getDelay() - 1);
			}
			return;
		}
		if (tickCount >= LIFETIME_TICKS) {
			discard();
			return;
		}
		if (isFake()) {
			setDeltaMovement(Vec3.ZERO);
			return;
		}
		LivingEntity thrower = getLivingOwner();
		if (!level().isClientSide() && thrower == null) {
			discard();
			return;
		}

		super.tick();

		if (level().isClientSide()) {
			float[] color = COLORS[getVariety()];
			level().addParticle(WispParticleData.wisp(0.25F, color[0], color[1], color[2]), getX(), getY(), getZ(), 0, 0, 0);
			return;
		}

		if (tickCount % AFTERIMAGE_INTERVAL_TICKS == 0) {
			level().addFreshEntity(makeAfterimage());
		}

		if (thrower instanceof Player player) {
			damageAsPlayer(player);
		} else {
			damageAsMob(thrower);
		}
	}

	/**
	 * 玩家召唤的剑。伤害规则来自 1.12。
	 * A sword that a player summons. The damage rules come from 1.12.
	 */
	private void damageAsPlayer(Player player) {
		float damage = computeDamage(player);
		for (LivingEntity target : findTargets(HIT_RADIUS)) {
			if (target instanceof Animal || target.invulnerableTime > 5) {
				continue;
			}
			DamageHandler.INSTANCE.dealTrueDamage(player, target, damage * TRUE_PART);
			DamageHandler.INSTANCE.dmg(target, null, damage * MAGIC_PART, DamageKind.MAGIC);
			DamageHandler.INSTANCE.dmg(target, player, damage * PHYSICAL_PART, DamageKind.NEUTRAL);
		}
	}

	/**
	 * Boss 召唤的剑。伤害规则来自 1.16 的 {@code EntityPhantomSword}。
	 * A sword that a boss summons. The damage rules come from {@code EntityPhantomSword} of 1.16.
	 */
	private void damageAsMob(LivingEntity thrower) {
		for (LivingEntity target : findTargets(HIT_RADIUS)) {
			if (target.invulnerableTime == 0) {
				DamageHandler.INSTANCE.dmg(target, thrower, 2.5F, DamageKind.LIFE_LOSING);
			}
			DamageHandler.INSTANCE.dmg(target, thrower, 7.5F, DamageKind.MAGIC);
		}
	}

	/**
	 * 计算基础伤害。玩家手持最初分型时，伤害随攻击力和移动速度提高。
	 * Calculates the base damage. When the player holds the First Fractal, the damage increases with attack damage and movement speed.
	 */
	private static float computeDamage(Player player) {
		if (!player.getMainHandItem().is(ExtraBotanyItems.firstFractal)) {
			return DEFAULT_DAMAGE;
		}
		double speed = player.getAttributeValue(Attributes.MOVEMENT_SPEED);
		double damage = player.getAttributeValue(Attributes.ATTACK_DAMAGE) * Math.min(1 + speed, 2);
		return Double.isFinite(damage) ? (float) damage : DEFAULT_DAMAGE;
	}

	private PhantomSwordEntity makeAfterimage() {
		PhantomSwordEntity afterimage = new PhantomSwordEntity(ExtraBotanyEntityType.PHANTOM_SWORD, level());
		afterimage.setOwner(getOwner());
		afterimage.entityData.set(FAKE, true);
		afterimage.setVariety(getVariety());
		afterimage.setPos(position());
		afterimage.setYRot(getYRot());
		afterimage.setXRot(getXRot());
		afterimage.yRotO = getYRot();
		afterimage.xRotO = getXRot();
		return afterimage;
	}

	public int getVariety() {
		return Mth.clamp(entityData.get(VARIETY), 0, VARIETY_COUNT - 1);
	}

	public void setVariety(int variety) {
		entityData.set(VARIETY, Math.floorMod(variety, VARIETY_COUNT));
	}

	public int getDelay() {
		return entityData.get(DELAY);
	}

	public void setDelay(int delay) {
		entityData.set(DELAY, Math.max(0, delay));
	}

	public boolean isFake() {
		return entityData.get(FAKE);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putInt(TAG_VARIETY, getVariety());
		tag.putInt(TAG_DELAY, getDelay());
		tag.putBoolean(TAG_FAKE, isFake());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		setVariety(tag.getInt(TAG_VARIETY));
		setDelay(tag.getInt(TAG_DELAY));
		entityData.set(FAKE, tag.getBoolean(TAG_FAKE));
	}
}
