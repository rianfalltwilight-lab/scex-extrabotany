package io.github.lounode.extrabotany.api.level;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import vazkii.botania.api.ServiceUtil;

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
