package io.github.lounode.extrabotany.api.block;

import net.minecraft.world.item.ItemStack;

/**
 * 祭坛：它保存一个物品、当前的敲击次数和结构的等级。
 * A pedestal: it holds one item, the current number of hits and the tier of the structure.
 */
public interface Pedestal {
	int getStrikes();

	void setStrikes(int strikes);

	int getTier();

	void setItem(ItemStack stack);
	ItemStack getItem();
}
