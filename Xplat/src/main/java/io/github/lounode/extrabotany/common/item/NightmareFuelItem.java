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

import net.neoforged.neoforge.event.furnace.FurnaceFuelBurnTimeEvent;

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

	public static void makeFuel(FurnaceFuelBurnTimeEvent wrapper) {
		if (wrapper.getItemStack().is(ExtraBotanyItems.nightmareFuel)) {
			wrapper.setBurnTime(3200);
		}
	}
}
