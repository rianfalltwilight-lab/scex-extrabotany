package io.github.lounode.extrabotany.common.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

/**
 * 属于某个玩家的效果实体的基类。
 * The base class for an effect entity that belongs to a player.
 */
public abstract class PlayerOwnedEntity extends OwnedEntity {
	protected PlayerOwnedEntity(EntityType<?> type, Level level) {
		super(type, level);
	}

	/**
	 * 返回主人。主人不是玩家时返回 {@code null}。
	 * Returns the owner. The value is {@code null} when the owner is not a player.
	 */
	@Nullable
	@Override
	public Player getOwner() {
		return super.getOwner() instanceof Player player ? player : null;
	}
}
