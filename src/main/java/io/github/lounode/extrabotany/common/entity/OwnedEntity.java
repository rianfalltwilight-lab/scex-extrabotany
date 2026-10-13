package io.github.lounode.extrabotany.common.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * 有主人的效果实体的基类。它保存主人的 UUID。
 * The base class for an effect entity that has an owner. It saves the UUID of the owner.
 *
 * <p>原版把主人保存为实体对象的引用，不写进存档，所以读档之后效果实体失去主人。有些原版实体甚至把主人放在静态字段里。
 * <p>The original keeps the owner as a reference to the entity object and does not save it, so an effect entity has no owner after a load. Some entities of the original even keep the owner in a static field.
 */
public abstract class OwnedEntity extends Entity {
	private static final String TAG_OWNER = "Owner";

	@Nullable
	private UUID ownerId;
	@Nullable
	private Entity owner;

	protected OwnedEntity(EntityType<?> type, Level level) {
		super(type, level);
	}

	public void setOwner(@Nullable Entity owner) {
		this.owner = owner;
		this.ownerId = owner == null ? null : owner.getUUID();
	}

	/**
	 * 返回主人。主人被移除之后（例如玩家重生），根据 UUID 重新查找。只有服务端能查找。
	 * Returns the owner. After the owner is removed (for example when a player respawns), the method finds the owner again from the UUID. Only the server can do this search.
	 */
	@Nullable
	public Entity getOwner() {
		if ((owner == null || owner.isRemoved()) && ownerId != null && level() instanceof ServerLevel serverLevel) {
			owner = serverLevel.getEntity(ownerId);
		}
		return owner;
	}

	/**
	 * 返回主人。主人不是活着的生物时返回 {@code null}。
	 * Returns the owner. The value is {@code null} when the owner is not a living entity that is alive.
	 */
	@Nullable
	public LivingEntity getLivingOwner() {
		return getOwner() instanceof LivingEntity living && living.isAlive() ? living : null;
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		if (ownerId != null) {
			tag.putUUID(TAG_OWNER, ownerId);
		}
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
		ownerId = tag.hasUUID(TAG_OWNER) ? tag.getUUID(TAG_OWNER) : null;
		owner = null;
	}
}
