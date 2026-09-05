package io.github.lounode.extrabotany.common.entity.gaia.behavior;

import com.google.common.collect.ImmutableMap;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ServerLevelAccessor;
import net.neoforged.neoforge.event.EventHooks;

import vazkii.botania.common.entity.PixieEntity;

import io.github.lounode.extrabotany.common.entity.ExtraBotanyMemoryType;
import io.github.lounode.extrabotany.common.entity.gaia.Gaia;

import java.util.ArrayList;
import java.util.List;

import static io.github.lounode.extrabotany.common.entity.gaia.behavior.GaiaTeleport.TELEPORT_DELAY;

public class GaiaSpawnPixies<E extends Gaia> extends Behavior<E> {

	public GaiaSpawnPixies() {
		super(ImmutableMap.of(
				MemoryModuleType.NEAREST_PLAYERS, MemoryStatus.VALUE_PRESENT,
				ExtraBotanyMemoryType.TELEPORT_DELAY, MemoryStatus.VALUE_PRESENT,
				ExtraBotanyMemoryType.PIXIES_MAX, MemoryStatus.VALUE_PRESENT
		));
	}

	public static void initMemories(Brain<? extends Gaia> brain, int pixiesMax) {
		brain.setMemory(ExtraBotanyMemoryType.PIXIES_MAX, pixiesMax);
	}

	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, E gaia) {
		return getTeleportDelay(gaia) == 0;
	}

	@Override
	protected void start(ServerLevel level, E gaia, long gameTime) {
		spawnPixies(gaia);
	}

	protected void spawnPixies(Gaia gaia) {
		List<Player> players = getPlayers(gaia);

		for (int runTime = 0; runTime < players.size(); runTime++) {
			int spawnCount = 1;
			//Spawns when after hurt teleport
			if (gaia.hurtTime > 0) {
				spawnCount = gaia.level().random.nextInt(getPixiesMaxPerSpawn(gaia));
			}

			for (int i = 1; i <= spawnCount; i++) {
				PixieEntity pixie = new PixieEntity(gaia.level(), true);
				pixie.setProps(players.get(gaia.getRandom().nextInt(players.size())), gaia, 8);
				pixie.setPos(gaia.getX() + gaia.getBbWidth() / 2, gaia.getY() + 2, gaia.getZ() + gaia.getBbWidth() / 2);
				EventHooks.finalizeMobSpawn(pixie, (ServerLevelAccessor) gaia.level(),
						gaia.level().getCurrentDifficultyAt(pixie.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
				gaia.level().addFreshEntity(pixie);
			}
		}
	}

	protected int getTeleportDelay(Gaia gaia) {
		return gaia.getBrain().getMemory(ExtraBotanyMemoryType.TELEPORT_DELAY).orElse(TELEPORT_DELAY);
	}

	protected List<Player> getPlayers(Gaia gaia) {
		return gaia.getBrain().getMemory(MemoryModuleType.NEAREST_PLAYERS).orElse(new ArrayList<>());
	}

	protected int getPixiesMaxPerSpawn(Gaia gaia) {
		return gaia.getBrain().getMemory(ExtraBotanyMemoryType.PIXIES_MAX).orElse(1);
	}
}
