package io.github.lounode.extrabotany.api.item;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import org.jetbrains.annotations.Nullable;

/**
 * 在玩家左键挥动时有额外效果的物品。
 * An item that has one more effect when the player swings it with the attack key.
 *
 * <p>服务端在两种情况下调用它：玩家攻击实体，或者客户端报告了一次没有打中实体的挥动。
 * <p>The server calls it in two cases: the player attacks an entity, or the client reports a swing that did not hit an entity.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code IItemWithLeftClick}。
 * <p>Source: ExtraMeteorP 1.16 {@code IItemWithLeftClick}.
 */
public interface LeftClickItem {

	/**
	 * 处理一次左键挥动。只在服务端调用。
	 * Handles one swing. Only the server calls this method.
	 *
	 * @param player 挥动物品的玩家。 The player that swings the item.
	 * @param target 被攻击的实体。没有打中实体时是 {@code null}。 The attacked entity. The value is {@code null} when the swing did not hit an entity.
	 */
	void onLeftClick(Player player, @Nullable Entity target);
}
