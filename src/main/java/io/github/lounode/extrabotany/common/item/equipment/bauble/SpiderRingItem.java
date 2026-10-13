package io.github.lounode.extrabotany.common.item.equipment.bauble;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import vazkii.botania.common.item.equipment.bauble.BaubleItem;

import io.github.lounode.extrabotany.api.item.ClimbingItem;

/**
 * 蛛行指环。佩戴者能爬墙，并且不受摔落伤害。
 * Spider Ring. The wearer can climb walls and gets no fall damage.
 */
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
