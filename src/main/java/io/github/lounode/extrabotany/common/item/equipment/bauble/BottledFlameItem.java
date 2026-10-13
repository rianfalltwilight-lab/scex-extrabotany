package io.github.lounode.extrabotany.common.item.equipment.bauble;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import vazkii.botania.common.handler.EquipmentHandler;
import vazkii.botania.common.item.equipment.bauble.BaubleItem;

/**
 * 瓶中火焰。佩戴者在黑暗处时，它自动从背包里取出火把，放在佩戴者脚下。
 * Bottled Flame. When the wearer is in a dark location, the bauble takes a torch from the inventory and puts it at the feet of the wearer.
 *
 * <p>来源：ExtraMeteorP 1.12 {@code ItemBottledFlame}。
 * <p>Source: ExtraMeteorP 1.12 {@code ItemBottledFlame}.
 *
 * <p>和原版的差异：原版每 tick 检查一次。这里每 10 tick 检查一次，以降低开销。
 * <p>Difference from the original: the original does the check on each tick. This port does the check one time each 10 ticks to decrease the cost.
 */
public class BottledFlameItem extends BaubleItem {
	/**
	 * 触发放置的最高亮度。原版的条件是亮度系数低于 0.25，对应亮度等级 8 及以下。
	 * The maximum light level that starts the placement. The condition of the original is a brightness below 0.25, which agrees with light level 8 and below.
	 */
	private static final int MAX_LIGHT_LEVEL = 8;
	private static final int INTERVAL_TICKS = 10;

	public BottledFlameItem(Properties props) {
		super(props);
	}

	@Override
	public boolean canEquip(ItemStack stack, LivingEntity entity) {
		return EquipmentHandler.findOrEmpty(this, entity).isEmpty();
	}

	@Override
	public void onWornTick(ItemStack stack, LivingEntity entity) {
		super.onWornTick(stack, entity);
		if (!(entity instanceof Player player) || player.level().isClientSide() || player.tickCount % INTERVAL_TICKS != 0) {
			return;
		}

		Level level = player.level();
		BlockPos feet = player.blockPosition();
		if (level.getMaxLocalRawBrightness(feet) > MAX_LIGHT_LEVEL) {
			return;
		}
		BlockState feetState = level.getBlockState(feet);
		BlockState groundState = level.getBlockState(feet.below());
		// 火把需要空气格和实心地面。流体中不能放置。
		// A torch needs an air block and solid ground. The bauble cannot put a torch in a fluid.
		if (!feetState.isAir() || groundState.isAir() || !groundState.getFluidState().isEmpty()) {
			return;
		}

		for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
			ItemStack torch = player.getInventory().getItem(slot);
			if (!isTorch(torch) || !((BlockItem) torch.getItem()).getBlock().defaultBlockState().canSurvive(level, feet)) {
				continue;
			}
			placeTorch(player, torch, feet);
			return;
		}
	}

	private static boolean isTorch(ItemStack stack) {
		return !stack.isEmpty()
				&& stack.getItem() instanceof BlockItem
				&& BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().contains("torch");
	}

	/**
	 * 用正常的物品使用流程放置火把。这样方块放置事件和领地保护模组都能生效。
	 * Puts the torch with the normal item use procedure. Thus block placement events and protection mods apply.
	 */
	private static void placeTorch(Player player, ItemStack torch, BlockPos feet) {
		ItemStack previousOffhand = player.getOffhandItem();
		try {
			// 临时把火把放到副手。物品使用流程从手上读取物品。
			// Temporarily move the torch to the off hand. The item use procedure reads the item from the hand.
			player.setItemInHand(InteractionHand.OFF_HAND, torch);
			torch.useOn(new UseOnContext(player, InteractionHand.OFF_HAND,
					new BlockHitResult(feet.getCenter(), Direction.UP, feet.below(), false)));
		} finally {
			player.setItemInHand(InteractionHand.OFF_HAND, previousOffhand);
		}
	}
}
