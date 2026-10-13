package io.github.lounode.extrabotany.common.entity.flamescion;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import io.github.lounode.extrabotany.common.entity.ExtraBotanyEntityType;

/**
 * 薪炎斩击。薪炎模式下的近战攻击命中时，它出现在目标身上。它存在 6 tick，在第 2 和第 5 tick 各造成 1 点火焰伤害。
 * Flamescion Slash. It appears on the target when a melee attack hits in the Flamescion mode. It stays for 6 ticks and does 1 flame damage on tick 2 and on tick 5.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code EntityFlamescionSlash}。
 * <p>Source: ExtraMeteorP 1.16 {@code EntityFlamescionSlash}.
 */
public class FlamescionSlashEntity extends FlamescionAreaEntity {
	private static final String TAG_ROTATION = "rotation";
	private static final String TAG_PITCH = "pitch";

	private static final EntityDataAccessor<Float> ROTATION = SynchedEntityData.defineId(FlamescionSlashEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> PITCH = SynchedEntityData.defineId(FlamescionSlashEntity.class, EntityDataSerializers.FLOAT);

	private static final int LIFETIME_TICKS = 6;
	private static final float RANGE = 3.5F;
	private static final float DAMAGE = 1F;

	public FlamescionSlashEntity(EntityType<? extends FlamescionSlashEntity> type, Level level) {
		super(type, level);
	}

	public FlamescionSlashEntity(Level level, Player owner) {
		this(ExtraBotanyEntityType.FLAMESCION_SLASH, level);
		setOwner(owner);
		// 每一道斩击的朝向是随机的。
		// Each slash has a random orientation.
		entityData.set(ROTATION, random.nextFloat() * 120F - 60F);
		entityData.set(PITCH, random.nextFloat() * 360F);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(ROTATION, 0F);
		builder.define(PITCH, 0F);
	}

	public float getRotation() {
		return entityData.get(ROTATION);
	}

	public float getPitch() {
		return entityData.get(PITCH);
	}

	@Override
	public void tick() {
		super.tick();
		if (level().isClientSide()) {
			return;
		}
		slowMonsters(livingInRange(RANGE));
		if (tickCount == 2 || tickCount == 5) {
			damageInRange(RANGE, DAMAGE);
		}
		if (tickCount >= LIFETIME_TICKS) {
			discard();
		}
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putFloat(TAG_ROTATION, getRotation());
		tag.putFloat(TAG_PITCH, getPitch());
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		entityData.set(ROTATION, tag.getFloat(TAG_ROTATION));
		entityData.set(PITCH, tag.getFloat(TAG_PITCH));
	}
}
