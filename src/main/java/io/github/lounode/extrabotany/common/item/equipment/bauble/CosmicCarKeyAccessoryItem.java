package io.github.lounode.extrabotany.common.item.equipment.bauble;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import vazkii.botania.common.item.equipment.bauble.BaubleItem;

import io.github.lounode.extrabotany.api.item.MountableAccessory;
import io.github.lounode.extrabotany.common.entity.mount.MountEntity;
import io.github.lounode.extrabotany.common.entity.mount.UfoEntity;

/**
 * 宇宙车钥匙饰品。佩戴时按坐骑键召唤一辆飞碟。玩家下车后飞碟消失。
 * Cosmic Car Key Accessory. When the player wears it, the mount key summons a Cosmic Car. The car goes away after the player gets off.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code ItemCosmicCarKeyAccessory}。
 * <p>Source: ExtraMeteorP 1.16 {@code ItemCosmicCarKeyAccessory}.
 */
public class CosmicCarKeyAccessoryItem extends BaubleItem implements MountableAccessory {
	public CosmicCarKeyAccessoryItem(Properties props) {
		super(props);
	}

	@Override
	public MountEntity createMount(Level level, Player player, ItemStack stack) {
		UfoEntity ufo = new UfoEntity(level, player.position());
		ufo.setSummoned(true);
		return ufo;
	}
}
