package io.github.lounode.extrabotany.common.item.relic;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;

import vazkii.botania.api.item.Relic;
import vazkii.botania.common.item.relic.RelicImpl;
import vazkii.botania.common.item.relic.RelicItem;

import io.github.lounode.extrabotany.common.entity.mount.MotorEntity;
import io.github.lounode.extrabotany.common.item.MountItems;

import java.util.UUID;

/**
 * 机车。遗物物品。对着方块使用时放下一辆机车。
 * Armored Motor. A relic item. A use on a block puts down a motor.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code ItemMotor}。
 * <p>Source: ExtraMeteorP 1.16 {@code ItemMotor}.
 *
 * <p>和原版的差异：原版把放置机车的玩家记为主人，所以别人放下再拆掉就能拿到绑定自己的机车。这里保留物品原来绑定的玩家。
 * <p>Difference from the original: the original records the player that places the motor as the owner, so a different player gets a motor bound to that player after a place and a break. This port keeps the player that the item is bound to.
 */
public class MotorItem extends RelicItem {
	public MotorItem(Properties props) {
		super(props);
	}

	public static Relic makeRelic(ItemStack stack) {
		return new RelicImpl(stack, null);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		return MountItems.place(level, player, hand, getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY),
				(mountLevel, pos, placer, stack) -> {
					MotorEntity motor = new MotorEntity(mountLevel, pos);
					motor.setOwnerId(ownerOf(stack, placer));
					return motor;
				});
	}

	private static UUID ownerOf(ItemStack stack, Player placer) {
		Relic relic = Relic.LOOKUP.find(stack);
		UUID bound = relic == null ? null : relic.getSoulbindUUID();
		return bound == null ? placer.getUUID() : bound;
	}
}
