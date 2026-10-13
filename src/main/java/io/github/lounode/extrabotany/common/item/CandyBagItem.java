package io.github.lounode.extrabotany.common.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 万圣糖惊魂夜（糖果袋）。它是一个奖励袋，打开后按糖果战利品表给出糖果。圣诞节期间它显示另一个名字。
 * Halloween Candy Night, the candy bag. It is a reward bag, and it gives candies from the candy loot table when it is opened. In the Christmas period it shows a different name.
 */
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
