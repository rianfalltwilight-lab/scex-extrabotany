package io.github.lounode.extrabotany.api.block;

import net.minecraft.world.item.ItemStack;

public interface Pedestal {
	int getStrikes();

	void setStrikes(int strikes);

	int getTier();

	void setItem(ItemStack stack);
	ItemStack getItem();
}
