package io.github.lounode.extrabotany.common.item.equipment.bauble;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import io.github.lounode.extrabotany.api.item.ClimbingItem;

public class ParkourRingItem extends JackieChanRingItem implements ClimbingItem {

	public ParkourRingItem(Properties props) {
		super(props);
	}

	@Override
	public boolean canClimb(ItemStack stack, LivingEntity entity) {
		return entity.horizontalCollision;
	}
}
