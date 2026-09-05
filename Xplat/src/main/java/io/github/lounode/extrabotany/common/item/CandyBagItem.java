package io.github.lounode.extrabotany.common.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/** Functional compatibility item: opens the candy loot table through the maintained bag implementation. */
public class CandyBagItem extends RewardBagItem {
	public CandyBagItem(Properties properties) {
		super(properties, prefix("candy"));
	}

	@Override
	public Component getName(ItemStack stack) {
		return SeasonalItemHelper.isChristmas()
				? Component.translatable(getDescriptionId(stack) + ".christmas") : super.getName(stack);
	}
}
