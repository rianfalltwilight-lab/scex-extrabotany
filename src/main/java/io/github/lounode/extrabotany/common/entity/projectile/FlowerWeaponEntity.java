package io.github.lounode.extrabotany.common.entity.projectile;

import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.client.fx.WispParticleData;
import vazkii.botania.common.handler.BotaniaSounds;

import io.github.lounode.extrabotany.common.entity.ExtraBotanyEntityType;
import io.github.lounode.extrabotany.common.handler.DamageHandler;
import io.github.lounode.extrabotany.common.item.KingGardenItem;

/**
 * 花之武器。王之花园召唤的投射物，一共 17 种，每种对应一朵花。
 * Flower Weapon. The projectile that the Garden of King's Law summons. There are 17 types, one for each flower.
 *
 * <p>玩家还在蓄力时，武器停在原地。蓄力结束后，武器再等待和它的序号相同的 tick 数，然后以每 tick 2 格的速度飞向玩家瞄准的位置。
 * <p>While the player still charges, the weapon stays in place. After the charge ends, the weapon waits for a number of ticks that is equal to its index, and then flies to the position that the player aims at, at 2 blocks for each tick.
 *
 * <p>武器命中 2 格内的第一个生物，或者撞上方块时消失。每种武器的伤害和附加效果不同，见 {@link #applyHit}。
 * <p>The weapon goes away when it hits the first mob within 2 blocks or when it hits a block. Each type has its own damage and extra effect. See {@link #applyHit}.
 *
 * <p>来源：ExtraMeteorP 1.12 {@code EntityFlowerWeapon}。
 * <p>Source: ExtraMeteorP 1.12 {@code EntityFlowerWeapon}.
 *
 * <p>和原版的差异：原版的魔法伤害没有攻击者。这里把玩家记为攻击者。
 * <p>Difference from the original: the magic damage of the original has no attacker. This port records the player as the attacker.
 */
public class FlowerWeaponEntity extends ThrowableProjectile {
	private static final String TAG_CHARGING = "Charging";
	private static final String TAG_VARIETY = "Variety";
	private static final String TAG_CHARGE_TICKS = "ChargeTicks";
	private static final String TAG_LIVE_TICKS = "LiveTicks";
	private static final String TAG_DELAY = "Delay";
	private static final String TAG_ROTATION = "Rotation";

	private static final EntityDataAccessor<Boolean> CHARGING = SynchedEntityData.defineId(FlowerWeaponEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Integer> VARIETY = SynchedEntityData.defineId(FlowerWeaponEntity.class, EntityDataSerializers.INT);

	private static final EntityDataAccessor<Integer> DELAY = SynchedEntityData.defineId(FlowerWeaponEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Float> ROTATION = SynchedEntityData.defineId(FlowerWeaponEntity.class, EntityDataSerializers.FLOAT);

	// 每种武器的伤害。下标是武器的种类编号。
	// The damage of each weapon. The index is the weapon type number.
	private static final float[] DAMAGE = { 9, 7, 7, 8, 12, 5, 7, 7, 10, 6, 6, 8, 9, 5, 5, 8, 8 };
	private static final int EXPLODING_VARIETY = 5;
	private static final float EXPLOSION_POWER = 2F;
	private static final double FLIGHT_SPEED = 2.0;
	private static final double AIM_RANGE = 64.0;
	private static final double HIT_RANGE = 2.0;
	private static final int FLIGHT_TICKS = 200;
	private static final int EFFECT_TICKS = 200;

	// 这两个计数器不同步。客户端根据同步的"蓄力中"标记自己计数。原版每 tick 同步它们，每把武器每 tick 发一个数据包。
	// These two counters are not synchronized. The client counts them itself from the synchronized "charging" flag. The original synchronizes them on each tick, which is one packet for each weapon on each tick.
	private int chargeTicks;
	private int liveTicks;

	public FlowerWeaponEntity(EntityType<? extends FlowerWeaponEntity> type, Level level) {
		super(type, level);
		setNoGravity(true);
	}

	/**
	 * @param variety  武器的种类编号，0 到 16。 The weapon type number, 0 to 16.
	 * @param delay    蓄力结束后起飞之前等待的 tick 数。 The number of ticks between the end of the charge and the start of the flight.
	 * @param rotation 武器绕竖直轴的角度，单位是度。只用于渲染。 The angle of the weapon around the vertical axis, in degrees. Only the renderer uses it.
	 */
	public FlowerWeaponEntity(Level level, LivingEntity owner, int variety, int delay, float rotation) {
		this(ExtraBotanyEntityType.FLOWER_WEAPON, level);
		setOwner(owner);
		// 武器总是在蓄力时被召唤。这个标记随生成数据包一起到达客户端。
		// A weapon is always summoned during a charge. This flag gets to the client together with the spawn packet.
		entityData.set(CHARGING, true);
		entityData.set(VARIETY, variety);
		entityData.set(DELAY, delay);
		entityData.set(ROTATION, rotation);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(CHARGING, false);
		builder.define(VARIETY, 0);
		builder.define(DELAY, 0);
		builder.define(ROTATION, 0F);
	}

	public int getVariety() {
		return Math.floorMod(entityData.get(VARIETY), KingGardenItem.WEAPON_TYPES);
	}

	/**
	 * 武器已经蓄力的 tick 数。渲染器用它做淡入。
	 * The number of ticks that the weapon has charged. The renderer uses it for the fade-in.
	 */
	public int getChargeTicks() {
		return chargeTicks;
	}

	/**
	 * 蓄力结束之后经过的 tick 数。
	 * The number of ticks after the end of the charge.
	 */
	public int getLiveTicks() {
		return liveTicks;
	}

	public int getDelay() {
		return entityData.get(DELAY);
	}

	public float getRotation() {
		return entityData.get(ROTATION);
	}

	@Override
	public void tick() {
		Player owner = getOwner() instanceof Player player && player.isAlive() ? player : null;
		if (!level().isClientSide()) {
			if (owner == null) {
				discard();
				return;
			}
			entityData.set(CHARGING, isOwnerCharging(owner));
		}

		int delay = getDelay();
		boolean charging = entityData.get(CHARGING) && liveTicks == 0;
		Vec3 motion = getDeltaMovement();
		if (charging) {
			setDeltaMovement(Vec3.ZERO);
			chargeTicks++;
			if (!level().isClientSide() && random.nextInt(20) == 0) {
				level().playSound(null, getX(), getY(), getZ(), BotaniaSounds.TREASURE_WEAPON_SPAWN, SoundSource.PLAYERS,
						0.1F, 1F + random.nextFloat() * 3F);
			}
		} else {
			if (liveTicks < delay) {
				setDeltaMovement(Vec3.ZERO);
			} else if (liveTicks == delay && owner != null) {
				motion = aimAt(owner);
				setDeltaMovement(motion);
				level().playSound(null, getX(), getY(), getZ(), BotaniaSounds.TREASURE_WEAPON_ATTACK, SoundSource.PLAYERS,
						1.4F, 0.1F + random.nextFloat() * 3F);
			}
			liveTicks++;
			if (!level().isClientSide() && hitFirstTarget(owner)) {
				return;
			}
		}

		super.tick();
		if (isRemoved()) {
			return;
		}
		// 投射物的 tick 会给速度加上阻力。花之武器以固定的速度飞行，所以这里把速度改回去。
		// The tick of a projectile applies drag to the velocity. A Flower Weapon flies at a constant speed, so the velocity is set back here.
		if (!charging) {
			setDeltaMovement(motion);
		}
		if (level().isClientSide() && liveTicks > delay + 1) {
			level().addParticle(WispParticleData.wisp(0.3F, 0F, 1F, 0F), getX(), getY(), getZ(), 0.0, 0.0, 0.0);
		}
		if (!level().isClientSide() && liveTicks > FLIGHT_TICKS + delay + 1) {
			discard();
		}
	}

	private static boolean isOwnerCharging(Player owner) {
		ItemStack held = owner.getMainHandItem().getItem() instanceof KingGardenItem ? owner.getMainHandItem() : owner.getOffhandItem();
		return held.getItem() instanceof KingGardenItem && KingGardenItem.isCharging(held);
	}

	/**
	 * 计算飞向玩家瞄准位置的速度。玩家没有瞄准方块时，瞄准视线上 64 格远的位置。
	 * Calculates the velocity to the position that the player aims at. When the player aims at no block, the aim is the position 64 blocks away along the view.
	 */
	private Vec3 aimAt(Player owner) {
		Vec3 eye = owner.getEyePosition();
		Vec3 end = eye.add(owner.getLookAngle().scale(AIM_RANGE));
		BlockHitResult hit = level().clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, owner));
		Vec3 target = hit.getType() == HitResult.Type.MISS ? end : hit.getBlockPos().getCenter();
		return target.subtract(position()).normalize().scale(FLIGHT_SPEED);
	}

	/**
	 * 命中这一 tick 路径附近的第一个生物。
	 * Hits the first mob near the path of this tick.
	 *
	 * @return 命中并且武器已经消失时返回 {@code true}。 {@code true} when there was a hit and the weapon is gone.
	 */
	private boolean hitFirstTarget(@Nullable Player owner) {
		if (owner == null) {
			return false;
		}
		AABB area = new AABB(getX(), getY(), getZ(), xOld, yOld, zOld).inflate(HIT_RANGE);
		for (LivingEntity living : level().getEntitiesOfClass(LivingEntity.class, area)) {
			if (living == owner || living.invulnerableTime != 0 || !DamageHandler.INSTANCE.checkPassable(living, owner)) {
				continue;
			}
			applyHit(living, owner);
			explodeAndDiscard();
			return true;
		}
		return false;
	}

	/**
	 * 对目标造成这种武器的伤害和附加效果。
	 * Applies the damage and the extra effect of this weapon type to the target.
	 *
	 * <p>编号 0、3、9、13、14 造成魔法伤害。编号 8（炽玫瑰）造成熔岩伤害。其他编号造成玩家攻击伤害。
	 * <p>Types 0, 3, 9, 13 and 14 do magic damage. Type 8 (Thermalily) does lava damage. The other types do player attack damage.
	 *
	 * <p>编号 13 到 16 的药水效果加给玩家自己。其他编号的药水效果加给目标。
	 * <p>The potion effects of types 13 to 16 go to the player. The potion effects of the other types go to the target.
	 */
	private void applyHit(LivingEntity target, Player owner) {
		int variety = getVariety();
		DamageSource source = switch (variety) {
			case 0, 3, 9, 13, 14 -> damageSources().indirectMagic(this, owner);
			case 8 -> damageSources().lava();
			default -> damageSources().playerAttack(owner);
		};
		target.hurt(source, DAMAGE[variety]);
		if (variety == 1) {
			target.igniteForSeconds(5);
		}

		Holder<MobEffect> effect = switch (variety) {
			case 2 -> MobEffects.BLINDNESS;
			case 3 -> MobEffects.CONFUSION;
			case 6 -> MobEffects.WITHER;
			case 7 -> MobEffects.MOVEMENT_SLOWDOWN;
			case 9 -> MobEffects.UNLUCK;
			case 10 -> MobEffects.POISON;
			case 11 -> MobEffects.WEAKNESS;
			case 13 -> MobEffects.DAMAGE_BOOST;
			case 14 -> MobEffects.ABSORPTION;
			case 15 -> MobEffects.JUMP;
			case 16 -> MobEffects.MOVEMENT_SPEED;
			default -> null;
		};
		if (effect != null) {
			LivingEntity receiver = variety >= 13 ? owner : target;
			// 凋零是 I 级，其他效果是 II 级。
			// Wither has level I, and the other effects have level II.
			receiver.addEffect(new MobEffectInstance(effect, EFFECT_TICKS, variety == 6 ? 0 : 1));
		}
	}

	private void explodeAndDiscard() {
		if (getVariety() == EXPLODING_VARIETY) {
			// 熵变花的武器会爆炸。爆炸不破坏方块。
			// The weapon of the Entropinnyum makes an explosion. The explosion does not break blocks.
			level().explode(this, getX(), getY(), getZ(), EXPLOSION_POWER, Level.ExplosionInteraction.NONE);
		}
		discard();
	}

	@Override
	protected void onHit(HitResult result) {
		if (level().isClientSide()) {
			return;
		}
		if (result instanceof EntityHitResult entityHit && entityHit.getEntity() == getOwner()) {
			return;
		}
		explodeAndDiscard();
	}

	@Override
	protected boolean canHitEntity(Entity target) {
		// 对生物的伤害由 tick 里的范围检测造成，所以投射物自己的实体碰撞关掉。
		// The range check in the tick does the damage to mobs, so the entity collision of the projectile is off.
		return false;
	}

	@Override
	public boolean ignoreExplosion(Explosion explosion) {
		return true;
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putBoolean(TAG_CHARGING, entityData.get(CHARGING));
		tag.putInt(TAG_VARIETY, entityData.get(VARIETY));
		tag.putInt(TAG_CHARGE_TICKS, chargeTicks);
		tag.putInt(TAG_LIVE_TICKS, liveTicks);
		tag.putInt(TAG_DELAY, getDelay());
		tag.putFloat(TAG_ROTATION, getRotation());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		entityData.set(CHARGING, tag.getBoolean(TAG_CHARGING));
		entityData.set(VARIETY, tag.getInt(TAG_VARIETY));
		chargeTicks = tag.getInt(TAG_CHARGE_TICKS);
		liveTicks = tag.getInt(TAG_LIVE_TICKS);
		entityData.set(DELAY, tag.getInt(TAG_DELAY));
		entityData.set(ROTATION, tag.getFloat(TAG_ROTATION));
	}
}
