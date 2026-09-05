package io.github.lounode.extrabotany.common.entity.gaia;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Dynamic;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Swim;
import net.minecraft.world.entity.schedule.Activity;

import io.github.lounode.extrabotany.common.entity.gaia.behavior.*;

public class GaiaIIIAI extends GaiaAI {
	public static final int EMERGE_TIME = 200;
	protected static final int TELEPORT_DELAY_INITIAL = 25;
	protected static final int TELEPORT_DELAY = 30;
	protected static final float TELEPORT_RANGE = 12;

	public GaiaIIIAI() {}

	public static void updateActivity(Gaia gaia) {
		gaia.getBrain().setActiveActivityToFirstValid(ImmutableList.of(
				Activity.EMERGE,
				gaia instanceof GaiaIII third && third.isEgo() ? Activity.RAID : Activity.FIGHT
		));
	}

	protected static void initMemories(Gaia gaia, ServerLevel level, BlockPos pos) {
		GaiaTeleport.initMemories(gaia.getBrain(), TELEPORT_RANGE, TELEPORT_DELAY, TELEPORT_DELAY_INITIAL);
		GaiaSpawnSkullLandMine.initMemories(gaia.getBrain(), GaiaSpawnSkullLandMine.LANDMINE_COUNTS);
		GaiaSpawnPixies.initMemories(gaia.getBrain(), PIXIES_PRE_SPAWN_MAX);
	}

	protected static Brain<?> makeBrain(Gaia gaia, Dynamic<?> ops) {
		Brain.Provider<GaiaIII> provider = Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
		Brain<GaiaIII> brain = provider.makeBrain(ops);
		initCoreActivity(brain);
		initSpawnActivity(brain, EMERGE_TIME);
		initFightActivity(brain);
		brain.addActivity(Activity.RAID, 10, ImmutableList.of(new GaiaTeleport<>(), new GaiaEgoSwordAttack<>(),
				new GaiaSmashBlocksAround<>(), new GaiaCleanPlayerUnstableEffects<>()));

		brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
		brain.setDefaultActivity(Activity.EMERGE);
		brain.useDefaultActivity();
		return brain;
	}

	protected static void initCoreActivity(Brain<? extends Gaia> brain) {
		brain.addActivity(Activity.CORE, 0, ImmutableList.of(
				new Swim(0.8F),
				new GaiaDisarm<>()
		));
	}

	private static void initFightActivity(Brain<? extends Gaia> brain) {
		brain.addActivity(Activity.FIGHT, 10, ImmutableList.of(
				new GaiaTeleport<>(),
				new GaiaSpawnSkullMissile<>(),
				new GaiaSpawnSkullLandMine<>(),
				new GaiaSpawnPixies<>(),
				new GaiaSmashBlocksAround<>(),
				new GaiaCleanPlayerUnstableEffects<>()
		));
	}
}
