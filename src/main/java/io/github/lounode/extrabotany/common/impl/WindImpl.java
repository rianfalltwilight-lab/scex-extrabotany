package io.github.lounode.extrabotany.common.impl;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import io.github.lounode.extrabotany.api.level.Wind;
import net.neoforged.neoforge.event.level.LevelEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 风的实现。每个世界有自己的风。风力随高度变化，在海平面和建筑上限的中间最强。下雨和雷暴让风力变大。
 * The implementation of the wind. Each level has its own wind. The wind level changes with the height, and it is strongest in the middle between the sea level and the build limit. Rain and thunder increase the wind level.
 *
 * <p>风的基础强度是随机的，并且每一端分别生成。所以只有服务端的风力是有效的。客户端要显示风力时，必须使用服务端同步过来的数值。
 * <p>The base strength of the wind is random, and each side makes it independently. Thus only the wind level of the server is valid. When the client shows a wind level, it must use the value that the server synchronized.
 */
public class WindImpl implements Wind {

	// 单人游戏里，客户端线程和服务端线程都会访问这张表，所以它必须是线程安全的。Level 没有重写 equals，所以键按对象本身比较。
	// In a single-player game the client thread and the server thread both use this map, thus it must be thread-safe. Level does not override equals, thus the keys are compared by identity.
	private static final Map<Level, LevelWind> WIND_MAP = new ConcurrentHashMap<>();

	@Override
	public double getWindLevel(Level level, Vec3 position) {
		return WIND_MAP.computeIfAbsent(level, LevelWind::new).getWindLevel(position);
	}

	public static class LevelWind {

		protected final Level level;
		public int ticksExisted;
		private int baseStrength;

		private double peakHeight;
		private static final double CENTER_OFFSET_RANGE = 10.0;
		private static final int UPDATE_PEAK_HEIGHT_INTERVAL = 36000;
		private static final int UPDATE_STRENGTH_INTERVAL = 200;

		public LevelWind(Level level) {
			this.level = level;
			this.baseStrength = 5 + level.random.nextInt(10);
			this.updatePeakHeight();
		}

		public void tick() {
			if (ticksExisted % UPDATE_STRENGTH_INTERVAL == 0) {
				int delta = level.random.nextInt(3) - 1; // -1, 0, 1
				baseStrength = Math.max(1, Math.min(20, baseStrength + delta));
			}

			if (ticksExisted % UPDATE_PEAK_HEIGHT_INTERVAL == 0) {
				updatePeakHeight();
			}

			ticksExisted++;
		}

		private void updatePeakHeight() {
			double seaLevel = level.getSeaLevel();
			double maxHeight = level.getMaxBuildHeight();
			double center = (seaLevel + maxHeight) / 2.0;

			peakHeight = center + (level.random.nextDouble() * 2.0 - 1.0) * CENTER_OFFSET_RANGE;
		}

		public double getWindLevel(Vec3 position) {
			double height = position.y();

			double seaLevel = level.getSeaLevel();
			double maxHeight = level.getMaxBuildHeight();
			double range = maxHeight - seaLevel;

			double delta = height - peakHeight;
			double sigma = 20.0;

			double heightMultiplier = Math.exp(-Math.pow(delta / sigma, 2));

			double result = baseStrength * (0.5 + heightMultiplier * 1.5);

			double rainOffset = 1.0;
			if (level.isRaining()) {
				rainOffset += 0.25;
			}
			if (level.isThundering()) {
				rainOffset += 0.25;
			}

			return result * rainOffset;
		}
	}

	public static class EventHandler {

		public static void onLevelLoad(LevelEvent.Load event) {
			Level level = (Level) event.getLevel();
			WIND_MAP.computeIfAbsent(level, LevelWind::new);
		}

		public static void onLevelUnLoad(LevelEvent.Unload event) {
			Level level = (Level) event.getLevel();
			WIND_MAP.remove(level);
		}

		public static void onLevelTick(Level level) {
			LevelWind wind = WIND_MAP.get(level);
			if (wind != null) {
				wind.tick();
			}
		}
	}
}
