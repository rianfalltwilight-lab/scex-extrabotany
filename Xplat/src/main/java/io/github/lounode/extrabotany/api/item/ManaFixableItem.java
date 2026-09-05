package io.github.lounode.extrabotany.api.item;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import vazkii.botania.api.mana.ManaItemHandler;

public interface ManaFixableItem {
	default void tickManaFix(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
		if (!(entity instanceof Player player)) {
			return;
		}
		if (world.isClientSide()) {
			return;
		}
		if (stack.getDamageValue() <= 0) {
			return;
		}

		if (ManaItemHandler.instance().requestManaExact(stack, player, getManaPerDamage(), true)) {
			stack.setDamageValue(stack.getDamageValue() - 1);
		}
	}

	int getManaPerDamage();
}
