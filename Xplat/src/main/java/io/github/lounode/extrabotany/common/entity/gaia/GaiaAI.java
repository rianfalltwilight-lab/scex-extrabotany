package io.github.lounode.extrabotany.common.entity.gaia;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Dynamic;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Swim;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;

import io.github.lounode.extrabotany.common.entity.ExtraBotanyMemoryType;
import io.github.lounode.extrabotany.common.entity.gaia.behavior.*;

import java.util.List;

public class GaiaAI {

	public static final int EMERGE_TIME = 160;
	protected static final int TELEPORT_DELAY_INITIAL = 30;
	protected static final int TELEPORT_DELAY = 35;
	protected static final float TELEPORT_RANGE = 12;
	protected static final int LANDMINE_COUNTS = 6;
	protected static final int PIXIES_PRE_SPAWN_MAX = 6;

	protected static final float MOB_SPAWN_THRESHOLD = 0.2F;
	public static final int MOB_SPAWN_START_TICKS = 20;
	public static final int MOB_SPAWN_END_TICKS = 80;
	protected static final int MOB_SPAWN_BASE_TICKS = 800;
	public static final int MOB_SPAWN_TICKS = MOB_SPAWN_BASE_TICKS + MOB_SPAWN_START_TICKS + MOB_SPAWN_END_TICKS;

	public static final int MOB_SPAWN_WAVES = 10;
	public static final int MOB_SPAWN_WAVE_TIME = MOB_SPAWN_BASE_TICKS / MOB_SPAWN_WAVES;

	protected static final List<SensorType<? extends Sensor<? super Gaia>>> SENSOR_TYPES = List.of(
			SensorType.NEAREST_PLAYERS,
			SensorType.HURT_BY
	);
	protected static final List<MemoryModuleType<?>> MEMORY_TYPES = List.of(
			MemoryModuleType.NEAREST_LIVING_ENTITIES,
			MemoryModuleType.IS_EMERGING,
			MemoryModuleType.WALK_TARGET,
			MemoryModuleType.LOOK_TARGET,
			ExtraBotanyMemoryType.TELEPORT_RANGE,
			ExtraBotanyMemoryType.TELEPORT_DELAY,
			ExtraBotanyMemoryType.TELEPORT_DELAY_BASE,
			ExtraBotanyMemoryType.LANDMINE_COUNT,
			ExtraBotanyMemoryType.PIXIES_MAX,
			ExtraBotanyMemoryType.MOB_SPAWN_TICKS,
			MemoryModuleType.NEAREST_PLAYERS,
			MemoryModuleType.NEAREST_VISIBLE_PLAYER,
			MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER,
			MemoryModuleType.HURT_BY
	);

	public GaiaAI() {}

	public static void updateActivity(Gaia gaia) {
		boolean isNotEmerging = !gaia.getBrain().isActive(Activity.EMERGE);
		boolean lowHealth = (gaia.getHealth() / gaia.getMaxHealth()) <= MOB_SPAWN_THRESHOLD;
		boolean hasSpawnTicks = gaia.getBrain().hasMemoryValue(ExtraBotanyMemoryType.MOB_SPAWN_TICKS);

		if (isNotEmerging && lowHealth && hasSpawnTicks) {
			gaia.getBrain().setActiveActivityIfPossible(Activity.RAID);
		} else {
			gaia.getBrain().setActiveActivityToFirstValid(ImmutableList.of(
					Activity.EMERGE,
					Activity.FIGHT
			));
		}
	}

	protected static Brain<?> makeBrain(Gaia gaia, Dynamic<?> ops) {
		Brain.Provider<Gaia> provider = Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
		Brain<Gaia> brain = provider.makeBrain(ops);
		initCoreActivity(brain);
		initSpawnActivity(brain, EMERGE_TIME);
		initMobSpawnActivity(brain, MOB_SPAWN_TICKS);
		initFightActivity(brain);

		brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
		brain.setDefaultActivity(Activity.EMERGE);
		brain.useDefaultActivity();
		return brain;
	}

	protected static void initMemories(Gaia gaia, ServerLevel level, BlockPos pos) {
		GaiaTeleport.initMemories(gaia.getBrain(), TELEPORT_RANGE, TELEPORT_DELAY, TELEPORT_DELAY_INITIAL);
		GaiaSpawnLandMine.initMemories(gaia.getBrain(), LANDMINE_COUNTS);
		GaiaSpawnPixies.initMemories(gaia.getBrain(), PIXIES_PRE_SPAWN_MAX);

		gaia.getBrain().setMemoryWithExpiry(ExtraBotanyMemoryType.MOB_SPAWN_TICKS, MOB_SPAWN_TICKS, MOB_SPAWN_TICKS);
	}

	protected static void initCoreActivity(Brain<? extends Gaia> brain) {
		brain.addActivity(Activity.CORE, 0, ImmutableList.of(
				new Swim(0.8F)
		));
	}

	protected static void initSpawnActivity(Brain<? extends Gaia> brain, int emergeTime) {
		brain.addActivityAndRemoveMemoryWhenStopped(Activity.EMERGE, 1, ImmutableList.of(
				new GaiaEmerging<>(emergeTime)
		), MemoryModuleType.IS_EMERGING);
	}

	private static void initFightActivity(Brain<? extends Gaia> brain) {
		brain.addActivity(Activity.FIGHT, 10, ImmutableList.of(
				new GaiaTeleport<>(),
				new GaiaSpawnMissile<>(),
				new GaiaSpawnLandMine<>(),
				new GaiaSpawnPixies<>(),
				new GaiaSmashBlocksAround<>(),
				new GaiaCleanPlayerUnstableEffects<>()
		));
	}

	private static void initMobSpawnActivity(Brain<? extends Gaia> brain, int spawnTicks) {
		brain.addActivityAndRemoveMemoryWhenStopped(Activity.RAID, 5, ImmutableList.of(
				new GaiaSpawnMob<>(spawnTicks)
		), ExtraBotanyMemoryType.MOB_SPAWN_TICKS);
	}
}
