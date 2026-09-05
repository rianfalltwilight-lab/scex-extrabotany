package io.github.lounode.extrabotany.common.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidType;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.client.fx.SparkleParticleData;
import vazkii.botania.common.helper.VecHelper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

public class SkullMissileEntity extends ThrowableProjectile {

	//public static final UUID DEFAULT_SKIN = UUID.fromString("4f201c25-b473-4c2c-959a-dea681f79c23");
	public static final int MAX_LIVING_TIME = 100;

	//private static final String TAG_SKIN = "Skin";
	private static final String TAG_FIRE = "Fire";
	private static final String TAG_TICKS_EXISTED = "ticksExisted";
	private static final String TAG_TARGET = "Target";

	//private static final EntityDataAccessor<Optional<UUID>> SKIN = SynchedEntityData.defineId(SkullMissileEntity.class, EntityDataSerializers.OPTIONAL_UUID);
	private boolean fire;
	private static final net.minecraft.network.syncher.EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(SkullMissileEntity.class, net.minecraft.network.syncher.EntityDataSerializers.FLOAT);
	private static final net.minecraft.network.syncher.EntityDataAccessor<Float> TRUE_DAMAGE = SynchedEntityData.defineId(SkullMissileEntity.class, net.minecraft.network.syncher.EntityDataSerializers.FLOAT);
	private static final net.minecraft.network.syncher.EntityDataAccessor<Boolean> EFFECT = SynchedEntityData.defineId(SkullMissileEntity.class, net.minecraft.network.syncher.EntityDataSerializers.BOOLEAN);
	public void setDamage(float value) { entityData.set(DAMAGE, Float.isFinite(value) ? Math.max(0, value) : 0); }
	public float getDamage() { return entityData.get(DAMAGE); }
	public void setTrueDamage(float value) { entityData.set(TRUE_DAMAGE, Float.isFinite(value) ? Math.max(0, value) : 0); }
	public float getTrueDamage() { return entityData.get(TRUE_DAMAGE); }
	public void setEffect(boolean value) { entityData.set(EFFECT, value); }
	public boolean hasEffect() { return entityData.get(EFFECT); }
	private int _ticksExisted = 0;
	private double lockX, lockY = Integer.MIN_VALUE, lockZ;

	@Nullable
	private UUID targetUUID;
	@Nullable
	private Entity cachedTarget;

	public SkullMissileEntity(EntityType<? extends SkullMissileEntity> entityType, Level level) {
		super(entityType, level);
	}

	public SkullMissileEntity(Level level, double x, double y, double z) {
		super(ExtraBotanyEntityType.SKULL_MISSILE, x, y, z, level);
	}

	public SkullMissileEntity(Level level, LivingEntity shooter) {
		super(ExtraBotanyEntityType.SKULL_MISSILE, shooter, level);
	}

	@Override
	public void tick() {
		super.tick();

		particle();

		if (level().isClientSide()) {
			return;
		}

		if (getTarget().isEmpty() || getTicksExisted() > MAX_LIVING_TIME) {
			discard();
			return;
		}

		getTarget().ifPresent(target -> {
			if (lockY == Integer.MIN_VALUE) {
				lockX = target.getX();
				lockY = target.getY();
				lockZ = target.getZ();
			}

			Vec3 thisVec = VecHelper.fromEntityCenter(this);
			Vec3 targetVec = new Vec3(lockX, lockY, lockZ);

			Vec3 diffVec = targetVec.subtract(thisVec);
			Vec3 motionVec = diffVec.normalize().scale(0.6);

			this.setDeltaMovement(motionVec);

			if (getTicksExisted() < 10) {
				setDeltaMovement(getDeltaMovement().x(), Math.abs(getDeltaMovement().y()), getDeltaMovement().z());
			}
		});

		setTicksExisted(getTicksExisted() + 1);
	}

	public boolean findTarget() {
		Optional<Entity> target = getTarget();
		if (target.isPresent()) {
			Entity entity = target.get();
			if (entity.isAlive()) {
				return true;
			} else {
				setTarget(null);
			}
		}

		double range = 15;
		AABB bounds = new AABB(getOnPos()).inflate(range);
		DamageSource source = this.getDamageSource();
		Predicate<Entity> vulnerableTo = e -> !e.isInvulnerableTo(source);
		List<? extends LivingEntity> entities;

		entities = level().getEntitiesOfClass(Player.class, bounds,
				EntitySelector.LIVING_ENTITY_STILL_ALIVE.and(vulnerableTo));

		Entity found = null;
		if (!entities.isEmpty()) {
			found = entities.get(level().random.nextInt(entities.size()));
			setTarget(found);
		}

		return found != null;
	}

	@Override
	protected void onHitBlock(BlockHitResult hit) {
		super.onHitBlock(hit);
		if (level().isClientSide()) {
			return;
		}

		BlockState state = level().getBlockState(hit.getBlockPos());

		if (state.getBlock() instanceof BushBlock) {
			return;
		}
		if (state.is(BlockTags.LEAVES)) {
			return;
		}

		discard();
	}

	@Override
	protected void onHitEntity(EntityHitResult hit) {
		super.onHitEntity(hit);
		if (level().isClientSide()) {
			return;
		}

		getTarget().ifPresent(target -> {
			if (hit.getEntity() == target) {
				//TODO ConfigAble damage
				target.hurt(getDamageSource(), getDamage());
				target.hurt(damageSources().generic(), .5F);
				if (target instanceof LivingEntity living) {
					LegacySwordProjectile.trueMagicDamage(living, getOwner(), getTrueDamage());
					if (isFire()) living.igniteForSeconds(5);
					if (hasEffect()) living.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WITHER, 200, 1));
				}
				discard();
			}
		});

	}

	protected DamageSource getDamageSource() {
		Entity owner = this.getOwner();
		if (owner instanceof LivingEntity livingOwner) {
			return owner instanceof Player playerOwner
					? damageSources().playerAttack(playerOwner)
					: damageSources().mobAttack(livingOwner);
		} else {
			return damageSources().generic();
		}
	}

	public void setTarget(@Nullable Entity target) {
		this.cachedTarget = target;
		this.targetUUID = target == null ? null : target.getUUID();
	}

	public Optional<Entity> getTarget() {
		if (this.cachedTarget != null && !this.cachedTarget.isRemoved()) {
			return Optional.of(this.cachedTarget);
		} else if (this.targetUUID != null && this.level() instanceof ServerLevel serverLevel) {
			this.cachedTarget = serverLevel.getEntity(this.targetUUID);
			return this.cachedTarget != null ? Optional.of(this.cachedTarget) : Optional.empty();
		} else {
			return Optional.empty();
		}
	}

	protected void particle() {
		if (!level().isClientSide()) {
			return;
		}

		double lastTickPosX = this.xOld;
		double lastTickPosY = this.yOld;
		double lastTickPosZ = this.zOld;

		Vec3 thisVec = VecHelper.fromEntityCenter(this);
		Vec3 oldPos = new Vec3(lastTickPosX, lastTickPosY, lastTickPosZ);

		Vec3 diff = thisVec.subtract(oldPos);
		Vec3 step = diff.normalize().scale(0.05);
		int steps = (int) (diff.length() / step.length());
		Vec3 particlePos = oldPos;

		SparkleParticleData data = SparkleParticleData.corrupt(0.8F, 1F, 0.0F, 1F, 2);

		for (int i = 0; i < steps; i++) {
			level().addParticle(data, particlePos.x, particlePos.y, particlePos.z, 0, 0, 0);

			if (level().random.nextInt(steps) <= 1) {
				level().addParticle(data, particlePos.x + (Math.random() - 0.5) * 0.4, particlePos.y + (Math.random() - 0.5) * 0.4, particlePos.z + (Math.random() - 0.5) * 0.4, 0, 0, 0);
			}

			particlePos = particlePos.add(step);
		}
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(DAMAGE, 12F); builder.define(TRUE_DAMAGE, 0F); builder.define(EFFECT, false);
		//this.entityData.define(SKIN, Optional.of(DEFAULT_SKIN));
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag cmp) {
		super.addAdditionalSaveData(cmp);
		//cmp.putUUID(TAG_SKIN, this.getSkin());
		cmp.putBoolean(TAG_FIRE, this.isFire());
		cmp.putBoolean("Effect", hasEffect()); cmp.putFloat("Damage", getDamage()); cmp.putFloat("TrueDamage", getTrueDamage());
		cmp.putInt(TAG_TICKS_EXISTED, this.getTicksExisted());

		getTarget().ifPresent(target -> {
			cmp.putUUID(TAG_TARGET, target.getUUID());
		});
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag cmp) {
		super.readAdditionalSaveData(cmp);
		/*
		if (cmp.hasUUID(TAG_SKIN)) {
			this.setSkin(cmp.getUUID(TAG_SKIN));
		}
		*/

		this.setFire(cmp.getBoolean(TAG_FIRE));
		setEffect(cmp.getBoolean("Effect")); setDamage(cmp.contains("Damage") ? cmp.getFloat("Damage") : 12); setTrueDamage(cmp.getFloat("TrueDamage"));
		this.setTicksExisted(cmp.getInt(TAG_TICKS_EXISTED));

		if (cmp.hasUUID(TAG_TARGET)) {
			this.targetUUID = cmp.getUUID(TAG_TARGET);
			this.cachedTarget = null;
		}
	}

	public void setFire(boolean fire) {
		this.fire = fire;
	}

	public boolean isFire() {
		return fire;
	}
	/*
	public UUID getSkin() {
		return this.entityData.get(SKIN).orElse(DEFAULT_SKIN);
	}
	
	public void setSkin(UUID uuid) {
		this.entityData.set(SKIN, Optional.of(uuid));
	}
	
	*/

	public void setTicksExisted(int ticks) {
		_ticksExisted = ticks;
	}

	public int getTicksExisted() {
		return _ticksExisted;
	}

	@Override
	public boolean canBeCollidedWith() {
		return false;
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
