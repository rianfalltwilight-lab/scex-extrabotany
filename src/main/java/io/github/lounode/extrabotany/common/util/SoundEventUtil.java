package io.github.lounode.extrabotany.common.util;

import net.minecraft.world.level.Level;

/**
 * 声音的工具方法。
 * Utility methods for sounds.
 */
public class SoundEventUtil {
	public static float randomPitch(Level level) {
		return ((level.random.nextFloat() - level.random.nextFloat()) * .7f + 1) * 2;
	}
}
