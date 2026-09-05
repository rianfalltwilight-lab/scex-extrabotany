package io.github.lounode.extrabotany.common.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import vazkii.botania.common.helper.PlayerHelper;

import io.github.lounode.extrabotany.common.ExtraBotanyDamageTypes;
import io.github.lounode.extrabotany.common.lib.LibAdvancementNames;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class AuraFireEntity extends ThrowableProjectile {
	private static final String TAG_GRAVITY = "gravity";
	private static final String TAG_TICKS_EXISTED = "ticksExisted";
	private static final String TAG_MAX_LIVING_TIME = "maxLivingTime";
	private static final String TAG_DAMAGE = "Damage";

	private static final EntityDataAccessor<Float> GRAVITY = SynchedEntityData.defineId(AuraFireEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Integer> MAX_LIVING_TIME = SynchedEntityData.defineId(AuraFireEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(AuraFireEntity.class, EntityDataSerializers.FLOAT);
	public static final float ADVANCEMENT_REQUIRE = 40;
	private static final int LONG_LIVING = -1;
	private static final float BASE_DAMAGE = 4.0F;
	private static final float DAMAGE_ROUND = 1.0F;
	private static final int ABSORPTION_MAX = 10;
	private int _ticksExisted = 0;

	public AuraFireEntity(EntityType<? extends AuraFireEntity> entityType, Level level) {
		super(entityType, level);
	}

	public AuraFireEntity(Level level, double x, double y, double z, float rotX, float rotY) {
		super(ExtraBotanyEntityType.AURA_FIRE, x, y, z, level);

		setRot(rotY, rotX);
		setDeltaMovement(calculateVelocity(getXRot(), getYRot()));
	}

	public AuraFireEntity(LivingEntity shooter) {
		super(ExtraBotanyEntityType.AURA_FIRE, shooter, shooter.level());

		setRot(shooter.getYRot() + 180, -shooter.getXRot());
		setDeltaMovement(calculateVelocity(getXRot(), getYRot()));
		setDamage((float) (BASE_DAMAGE + shooter.getAttributeValue(Attributes.ATTACK_DAMAGE)));
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(GRAVITY, 0F);
		builder.define(MAX_LIVING_TIME, 80);
		builder.define(DAMAGE, BASE_DAMAGE);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putFloat(TAG_GRAVITY, (float) getGravity());
		tag.putInt(TAG_TICKS_EXISTED, getTicksExisted());
		tag.putInt(TAG_MAX_LIVING_TIME, getMaxLivingTime());
		tag.putFloat(TAG_DAMAGE, getDamage());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag cmp) {
		super.readAdditionalSaveData(cmp);
		setTicksExisted(cmp.getInt(TAG_TICKS_EXISTED));
		setMaxLivingTime(cmp.getInt(TAG_MAX_LIVING_TIME));
		setGravity(cmp.getFloat(TAG_GRAVITY));
		setDamage(cmp.getFloat(TAG_DAMAGE));
	}

	@Override
	public void tick() {
		setTicksExisted(getTicksExisted() + 1);
		super.tick();
		particles();

		if (level().isClientSide()) {
			return;
		}

		if (getMaxLivingTime() != LONG_LIVING && getTicksExisted() > getMaxLivingTime()) {
			discard();
		}

		Entity owner = getOwner();

		AABB axis = new AABB(this.getX(), this.getY(), this.getZ(), this.xOld, this.yOld, this.zOld).inflate(DAMAGE_ROUND);
		var entities = this.level().getEntitiesOfClass(LivingEntity.class, axis).stream()
				.filter(e -> owner == null || e != owner)
				.filter(e -> {
					if (owner instanceof Player player && e instanceof Player other) {
						return player.canHarmPlayer(other);
					}
					return true;
				})
				.filter(e -> e.hurtTime == 0)
				.toList();
		for (var entity : entities) {
			float damage = getDamage();

			DamageSource source = createDamageSource(entity, owner);

			if (!entity.hurt(source, damage)) {
				continue;
			}

			handlePostDamage(owner, damage);

			discard();
			break;
		}
	}

	private void handlePostDamage(Entity owner, float damage) {
		if (!(owner instanceof ServerPlayer player)) {
			return;
		}

		player.setAbsorptionAmount(Math.min(ABSORPTION_MAX, player.getAbsorptionAmount() + 1F));

		if (damage >= ADVANCEMENT_REQUIRE) {
			PlayerHelper.grantCriterion(player, prefix("main/" + LibAdvancementNames.ONE_PUNCH), "code_triggered");
		}
	}

	private DamageSource createDamageSource(Entity target, Entity owner) {
		return ExtraBotanyDamageTypes.Sources.jingweiDamage(target.level().registryAccess(), owner);
	}

	protected void particles() {
		if (!isAlive() || !level().isClientSide) {
			return;
		}

		for (int i = 1; i <= 5; i++) {
			level().addParticle(ParticleTypes.FLAME,
					getX() + Math.random() * 0.4F - 0.2F,
					getY() + Math.random() * 0.4F - 0.2F,
					getZ() + Math.random() * 0.4F - 0.2F,
					0, 0, 0
			);
		}
	}

	public static Vec3 calculateVelocity(float xRot, float yRot) {
		float f = 0.4F;
		double mx = Mth.sin(yRot / 180.0F * (float) Math.PI) * Mth.cos(xRot / 180.0F * (float) Math.PI) * f / 2D;
		double mz = -(Mth.cos(yRot / 180.0F * (float) Math.PI) * Mth.cos(xRot / 180.0F * (float) Math.PI) * f) / 2D;
		double my = Mth.sin(xRot / 180.0F * (float) Math.PI) * f / 2D;
		return new Vec3(mx, my, mz);
	}

	public void setTicksExisted(int ticks) {
		_ticksExisted = ticks;
	}

	public int getTicksExisted() {
		return _ticksExisted;
	}

	public void setGravity(float gravity) {
		entityData.set(GRAVITY, gravity);
	}

	@Override
	protected double getDefaultGravity() {
		return entityData.get(GRAVITY);
	}

	public void setMaxLivingTime(int time) {
		entityData.set(MAX_LIVING_TIME, time);
	}

	public int getMaxLivingTime() {
		return entityData.get(MAX_LIVING_TIME);
	}

	public void setLongLiving() {
		entityData.set(MAX_LIVING_TIME, LONG_LIVING);
	}

	public float getDamage() {
		return entityData.get(DAMAGE);
	}

	public void setDamage(float damage) {
		entityData.set(DAMAGE, damage);
	}

	@Override
	public boolean isInLava() {
		return false;
	}

	@Override
	public void updateFluidHeightAndDoFluidPushing() {
	}
}
