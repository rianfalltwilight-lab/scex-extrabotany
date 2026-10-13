package io.github.lounode.extrabotany.common.item.equipment.bauble;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import io.github.lounode.extrabotany.api.item.ClimbingItem;

/**
 * 跑酷指环。它有成龙指环的全部效果，并且佩戴者能爬墙。
 * Parkour Ring. It has all the effects of the Jackie Chan Ring, and the wearer can also climb walls.
 */
public class ParkourRingItem extends JackieChanRingItem implements ClimbingItem {

	public ParkourRingItem(Properties props) {
		super(props);
	}

	@Override
	public boolean canClimb(ItemStack stack, LivingEntity entity) {
		return entity.horizontalCollision;
	}
}
