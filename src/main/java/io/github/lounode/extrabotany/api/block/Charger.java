package io.github.lounode.extrabotany.api.block;

import net.minecraft.world.item.ItemStack;

import io.github.lounode.extrabotany.xplat.EXplatAbstractions;

/**
 * 魔力充能方块：它保存一个魔力物品，并报告充能的进度。
 * A mana charging block: it holds one mana item and reports the charge progress.
 */
public interface Charger {
	ItemStack getItem();
	void setItem(ItemStack stack);
	float getChargeProcess();

	default boolean isValidItem(ItemStack stack) {
		return io.github.lounode.extrabotany.xplat.EXplatAbstractions.INSTANCE.findManaItem(stack) != null;
	}
}
