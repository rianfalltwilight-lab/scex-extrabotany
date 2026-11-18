package io.github.lounode.extrabotany.common.item.equipment.bauble;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import vazkii.botania.common.item.equipment.bauble.BaubleItem;

import io.github.lounode.extrabotany.common.proxy.Proxy;

/**
 * 成龙指环。佩戴者不受摔落伤害，并且在贴着墙时按跳跃键可以蹬墙再跳一次。落地之前最多蹬墙跳 5 次，每次都要重新按跳跃键。
 * Jackie Chan Ring. The wearer gets no fall damage, and the wearer can jump again from a wall when the jump key is pressed against the wall. At most 5 wall jumps before the wearer lands, and each one needs a new press of the jump key.
 *
 * <p>次数来源：ExtraMeteorP 1.12 {@code WallJumpingShim#getMaxAllowedJumps}。
 * <p>Source of the count: ExtraMeteorP 1.12 {@code WallJumpingShim#getMaxAllowedJumps}.
 */
public class JackieChanRingItem extends BaubleItem {
	public static final int MAX_WALL_JUMPS = 5;

	public JackieChanRingItem(Properties props) {
		super(props);
	}

	public int getMaxAllowedJumps() {
		return MAX_WALL_JUMPS;
	}

	@Override
	public void onWornTick(ItemStack stack, LivingEntity entity) {
		super.onWornTick(stack, entity);
		entity.fallDistance = 0;

		// 蹬墙跳要读取按键，所以它在客户端执行，并且只对本机玩家有效。
		// The wall jump reads the keys, thus it is done on the client, and only for the local player.
		if (entity.level().isClientSide()) {
			Proxy.INSTANCE.tryWallJump(entity, getMaxAllowedJumps());
		}
	}
}
