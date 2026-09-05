package io.github.lounode.extrabotany.common.impl;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import io.github.lounode.extrabotany.api.level.Wind;
import net.neoforged.neoforge.event.level.LevelEvent;

import java.util.Map;

public class WindImpl implements Wind {

	private static final Map<Level, LevelWind> WIND_MAP = new Reference2ReferenceOpenHashMap<>();

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
