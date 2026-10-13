package io.github.lounode.extrabotany.common.item.equipment.bauble;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import vazkii.botania.common.item.equipment.bauble.BaubleItem;

import io.github.lounode.extrabotany.common.proxy.Proxy;

/**
 * 成龙指环。佩戴者不受摔落伤害，并且在贴着墙时按跳跃键可以蹬墙再跳一次。
 * Jackie Chan Ring. The wearer gets no fall damage, and the wearer can jump again from a wall when the jump key is pressed against the wall.
 */
public class JackieChanRingItem extends BaubleItem {

	public JackieChanRingItem(Properties props) {
		super(props);
	}

	@Override
	public void onWornTick(ItemStack stack, LivingEntity entity) {
		super.onWornTick(stack, entity);
		entity.fallDistance = 0;

		// 蹬墙跳要读取按键，所以它在客户端执行，并且只对本机玩家有效。
		// The wall jump reads the keys, thus it is done on the client, and only for the local player.
		if (entity.level().isClientSide()) {
			Proxy.INSTANCE.tryWallJump(entity);
		}
	}
}
