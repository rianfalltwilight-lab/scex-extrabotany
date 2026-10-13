package io.github.lounode.extrabotany.api.level;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import vazkii.botania.api.ServiceUtil;

/**
 * 风。风铃草用风力来决定产生多少魔力。实现在 {@code WindImpl} 里。
 * The wind. The Bellflower uses the wind level to decide how much mana it makes. The implementation is in {@code WindImpl}.
 */
public interface Wind {
	Wind INSTANCE = ServiceUtil.findService(Wind.class, () -> new Wind() {});

	static Wind instance() {
		return INSTANCE;
	}

	/**
	 * Get wind level at this position.
	 * 
	 * @return Wind level.
	 */
	default double getWindLevel(Level level, Vec3 position) {
		return 0;
	}
}
