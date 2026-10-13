package io.github.lounode.extrabotany.api.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import io.github.lounode.extrabotany.common.entity.mount.MountEntity;

/**
 * 可以召唤坐骑的饰品。佩戴饰品的玩家按下坐骑键时，服务端召唤坐骑并且让玩家骑上去。
 * An accessory that can summon a mount. When the player that wears the accessory pushes the mount key, the server summons the mount and puts the player on it.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code IMountableAccessory}。
 * <p>Source: ExtraMeteorP 1.16 {@code IMountableAccessory}.
 */
public interface MountableAccessory {
	/**
	 * 创建一个被召唤的坐骑。调用方负责设置位置并把它加入世界。
	 * Makes a summoned mount. The caller sets the position and adds the mount to the level.
	 *
	 * @param level  坐骑所在的世界。 The level of the mount.
	 * @param player 召唤坐骑的玩家。 The player that summons the mount.
	 * @param stack  玩家佩戴的饰品。 The accessory that the player wears.
	 */
	MountEntity createMount(Level level, Player player, ItemStack stack);
}
