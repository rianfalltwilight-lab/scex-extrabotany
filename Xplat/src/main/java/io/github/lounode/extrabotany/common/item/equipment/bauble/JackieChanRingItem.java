package io.github.lounode.extrabotany.common.item.equipment.bauble;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import vazkii.botania.common.item.equipment.bauble.BaubleItem;

import io.github.lounode.extrabotany.common.proxy.Proxy;

public class JackieChanRingItem extends BaubleItem {

	public JackieChanRingItem(Properties props) {
		super(props);
	}

	@Override
	public void onWornTick(ItemStack stack, LivingEntity entity) {
		super.onWornTick(stack, entity);
		entity.fallDistance = 0;

		if (entity.level().isClientSide()) {
			handleClientJumpLogic(stack, entity);
		}
	}

	private static void handleClientJumpLogic(ItemStack stack, LivingEntity entity) {
		if (!(entity instanceof net.minecraft.client.player.LocalPlayer player)) {
			return;
		}
		if (player != Proxy.INSTANCE.getClientPlayer()) {
			return;
		}
		JackieChanRingItem item = ((JackieChanRingItem) stack.getItem());

		if (player.getDeltaMovement().y() > 0 && !player.onClimbable()) {
			return;
		}
		if (player.input.jumping && player.horizontalCollision) {
			player.jumpFromGround();
		}
	}
}
