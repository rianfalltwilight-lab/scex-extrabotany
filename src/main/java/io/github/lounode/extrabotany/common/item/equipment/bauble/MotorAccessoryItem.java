package io.github.lounode.extrabotany.common.item.equipment.bauble;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import vazkii.botania.api.item.Relic;
import vazkii.botania.common.item.relic.RelicBaubleItem;
import vazkii.botania.common.item.relic.RelicImpl;

import io.github.lounode.extrabotany.api.item.MountableAccessory;
import io.github.lounode.extrabotany.common.entity.mount.MotorEntity;
import io.github.lounode.extrabotany.common.entity.mount.MountEntity;

/**
 * 重装机车饰品。遗物饰品。佩戴时按坐骑键召唤一辆机车。玩家下车后机车消失。
 * Armored Motor Accessory. A relic accessory. When the player wears it, the mount key summons a motor. The motor goes away after the player gets off.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code ItemMotorAccessory}。
 * <p>Source: ExtraMeteorP 1.16 {@code ItemMotorAccessory}.
 */
public class MotorAccessoryItem extends RelicBaubleItem implements MountableAccessory {
	public MotorAccessoryItem(Properties props) {
		super(props);
	}

	public static Relic makeRelic(ItemStack stack) {
		return new RelicImpl(stack, null);
	}

	@Override
	public MountEntity createMount(Level level, Player player, ItemStack stack) {
		MotorEntity motor = new MotorEntity(level, player.position());
		motor.setOwnerId(player.getUUID());
		motor.setSummoned(true);
		return motor;
	}
}
