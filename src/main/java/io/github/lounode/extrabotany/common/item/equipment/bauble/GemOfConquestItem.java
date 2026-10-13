package io.github.lounode.extrabotany.common.item.equipment.bauble;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import vazkii.botania.common.handler.EquipmentHandler;
import vazkii.botania.common.item.equipment.bauble.BaubleItem;

import io.github.lounode.extrabotany.common.handler.HerrscherHandler;

/**
 * 征服宝石。佩戴者成为"雷之律者"。
 * Gem of Conquest. The wearer becomes the Herrscher of Thunder.
 *
 * <p>宝石本身没有属性加成。它在佩戴时为律者充能。律者的能力在 {@link HerrscherHandler} 中。
 * <p>The gem gives no attribute bonus. While it is worn, it charges the energy of the Herrscher. The abilities of the Herrscher are in {@link HerrscherHandler}.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code ItemGemOfConquest} 和 {@code HerrscherHandler}。
 * <p>Source: ExtraMeteorP 1.16 {@code ItemGemOfConquest} and {@code HerrscherHandler}.
 */
public class GemOfConquestItem extends BaubleItem {
	public GemOfConquestItem(Properties props) {
		super(props);
	}

	@Override
	public void onWornTick(ItemStack stack, LivingEntity entity) {
		if (entity instanceof Player player) {
			HerrscherHandler.charge(player);
		}
	}

	@Override
	public void onEquipped(ItemStack stack, LivingEntity entity) {
		super.onEquipped(stack, entity);
		// 戴上宝石时同步一次能量，这样客户端从正确的数值开始计算。
		// Synchronize the energy one time when the gem is put on, so that the client starts its calculation from the correct value.
		if (entity instanceof Player player && !player.level().isClientSide()) {
			HerrscherHandler.setEnergy(player, HerrscherHandler.getEnergy(player));
		}
	}

	@Override
	public boolean canEquip(ItemStack stack, LivingEntity entity) {
		return EquipmentHandler.findOrEmpty(this, entity).isEmpty();
	}
}
