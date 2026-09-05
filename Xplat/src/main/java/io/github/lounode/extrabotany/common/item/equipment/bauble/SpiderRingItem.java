package io.github.lounode.extrabotany.common.item.equipment.bauble;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import vazkii.botania.common.item.equipment.bauble.BaubleItem;

import io.github.lounode.extrabotany.api.item.ClimbingItem;

public class SpiderRingItem extends BaubleItem implements ClimbingItem {
	public SpiderRingItem(Properties props) {
		super(props);
	}

	@Override
	public boolean canClimb(ItemStack stack, LivingEntity entity) {
		return entity.horizontalCollision;
	}

	@Override
	public void onWornTick(ItemStack stack, LivingEntity entity) {
		super.onWornTick(stack, entity);
		entity.fallDistance = 0;
	}
}
