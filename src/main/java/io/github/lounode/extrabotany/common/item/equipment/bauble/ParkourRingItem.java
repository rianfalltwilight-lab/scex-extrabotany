package io.github.lounode.extrabotany.common.item.equipment.bauble;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import io.github.lounode.extrabotany.api.item.ClimbingItem;

/**
 * 跑酷指环。它有成龙指环的全部效果，落地之前最多蹬墙跳 8 次（ExtraMeteorP 1.12 {@code ItemParkourRing}），并且佩戴者能爬墙。
 * Parkour Ring. It has all the effects of the Jackie Chan Ring with at most 8 wall jumps before the wearer lands (ExtraMeteorP 1.12 {@code ItemParkourRing}), and the wearer can also climb walls.
 */
public class ParkourRingItem extends JackieChanRingItem implements ClimbingItem {
	public static final int MAX_WALL_JUMPS = 8;

	@Override
	public int getMaxAllowedJumps() {
		return MAX_WALL_JUMPS;
	}

	public ParkourRingItem(Properties props) {
		super(props);
	}

	@Override
	public boolean canClimb(ItemStack stack, LivingEntity entity) {
		return entity.horizontalCollision;
	}
}
