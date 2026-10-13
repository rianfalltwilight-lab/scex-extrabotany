package io.github.lounode.extrabotany.api.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 让佩戴者能爬墙的饰品。
 * An accessory that lets the wearer climb walls.
 */
public interface ClimbingItem {
	boolean canClimb(ItemStack stack, LivingEntity entity);
}
