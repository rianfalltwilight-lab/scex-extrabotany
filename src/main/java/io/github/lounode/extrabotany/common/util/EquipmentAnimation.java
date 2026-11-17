package io.github.lounode.extrabotany.common.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;

/** Keeps mana repair and cooldown damage ticks from lowering the held item. */
public final class EquipmentAnimation {
	private EquipmentAnimation() {}

	public static boolean shouldReequip(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		if (slotChanged) {
			return true;
		}
		ItemStack oldAppearance = oldStack.copy();
		ItemStack newAppearance = newStack.copy();
		oldAppearance.remove(DataComponents.DAMAGE);
		newAppearance.remove(DataComponents.DAMAGE);
		return !ItemStack.matches(oldAppearance, newAppearance);
	}
}
