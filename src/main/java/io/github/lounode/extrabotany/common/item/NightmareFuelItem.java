package io.github.lounode.extrabotany.common.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.common.advancements.UseItemSuccessTrigger;


/**
 * 梦魇燃料。它可以吃，吃下后变成精神燃料。来源：ExtraMeteorP 1.16 {@code ItemNightmareFuel}；1.16 不是熔炉燃料。
 * Nightmare Fuel. It can be eaten, and it becomes Spirit Fuel after that. Source: {@code ItemNightmareFuel} of ExtraMeteorP 1.16; in 1.16 it is not a furnace fuel.
 */
public class NightmareFuelItem extends Item {
	public NightmareFuelItem(Properties properties) {
		super(properties);
	}

	@NotNull
	@Override
	public ItemStack finishUsingItem(@NotNull ItemStack stack, Level level, LivingEntity livingEntity) {
		super.finishUsingItem(stack, level, livingEntity);

		if (livingEntity instanceof ServerPlayer serverPlayer) {
			CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, stack);
			serverPlayer.awardStat(Stats.ITEM_USED.get(this));
			UseItemSuccessTrigger.INSTANCE.trigger(serverPlayer, stack, (ServerLevel) level, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ());
		}

		if (stack.isEmpty()) {
			return new ItemStack(ExtraBotanyItems.spiritFuel);
		} else {
			if (livingEntity instanceof Player player) {
				ItemStack result = new ItemStack(ExtraBotanyItems.spiritFuel);
				if (!player.getInventory().add(result)) {
					player.drop(result, false);
				}
			}
			return stack;
		}
	}

}
