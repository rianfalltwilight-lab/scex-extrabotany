package io.github.lounode.extrabotany.api.block;

import net.minecraft.world.item.ItemStack;

import io.github.lounode.extrabotany.xplat.EXplatAbstractions;

public interface Charger {
	ItemStack getItem();
	void setItem(ItemStack stack);
	float getChargeProcess();

	default boolean isValidItem(ItemStack stack) {
		return io.github.lounode.extrabotany.xplat.EXplatAbstractions.INSTANCE.findManaItem(stack) != null;
	}
}
