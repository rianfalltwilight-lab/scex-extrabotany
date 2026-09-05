package io.github.lounode.extrabotany.api.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface ClimbingItem {
	boolean canClimb(ItemStack stack, LivingEntity entity);
}
