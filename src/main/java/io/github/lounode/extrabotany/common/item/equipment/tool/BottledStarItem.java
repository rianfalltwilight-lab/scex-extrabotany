package io.github.lounode.extrabotany.common.item.equipment.tool;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import vazkii.botania.api.mana.ManaItemHandler;

import io.github.lounode.extrabotany.common.entity.projectile.BottledStarEntity;

/**
 * 瓶中星。右键消耗 300 点魔力，在玩家位置放置一个星标，它会持续召唤流星。冷却 5 秒。
 * Bottled Star. A use takes 300 mana and puts a star marker at the position of the player. The marker calls falling stars. The cooldown is 5 seconds.
 *
 * <p>来源：ExtraMeteorP 1.12 {@code ItemBottledStar}。
 * <p>Source: ExtraMeteorP 1.12 {@code ItemBottledStar}.
 */
public class BottledStarItem extends Item {
	private static final int MANA_PER_USE = 300;
	private static final int COOLDOWN_TICKS = 100;

	public BottledStarItem(Properties props) {
		super(props);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (level.isClientSide()) {
			return InteractionResultHolder.success(stack);
		}
		if (!ManaItemHandler.instance().requestManaExactForTool(stack, player, MANA_PER_USE, true)) {
			return InteractionResultHolder.fail(stack);
		}

		BottledStarEntity star = new BottledStarEntity(level, player);
		star.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
		level.addFreshEntity(star);
		player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
		return InteractionResultHolder.success(stack);
	}
}
