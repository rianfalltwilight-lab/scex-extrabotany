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
 * 梦魇燃料。它可以吃，吃下后变成精神燃料。它也是熔炉燃料，燃烧时间在数据映射 {@code neoforge:furnace_fuels} 里。
 * Nightmare Fuel. It can be eaten, and it becomes Spirit Fuel after that. It is also a furnace fuel, and its burn time is in the data map {@code neoforge:furnace_fuels}.
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
