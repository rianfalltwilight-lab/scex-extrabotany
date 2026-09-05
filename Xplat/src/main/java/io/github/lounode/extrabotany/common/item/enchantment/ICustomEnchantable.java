package io.github.lounode.extrabotany.common.item.enchantment;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

public interface ICustomEnchantable {
	boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment);
	boolean isPrimaryItemFor(ItemStack stack, Holder<Enchantment> enchantment);
}
