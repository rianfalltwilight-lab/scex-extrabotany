package io.github.lounode.extrabotany.common.entity.gaia.behavior;

import com.google.common.collect.ImmutableMap;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.player.Player;

import io.github.lounode.extrabotany.common.entity.ExtraBotanyEntityType;
import io.github.lounode.extrabotany.common.entity.ExtraBotanyMemoryType;
import io.github.lounode.extrabotany.common.entity.MagicLandMineEntity;
import io.github.lounode.extrabotany.common.entity.gaia.Gaia;

import java.util.ArrayList;
import java.util.List;

import static io.github.lounode.extrabotany.common.entity.gaia.behavior.GaiaTeleport.TELEPORT_DELAY;

public class GaiaSpawnLandMine<E extends Gaia> extends Behavior<E> {

	public static final int LANDMINE_COUNTS = 6;

	public GaiaSpawnLandMine() {
		super(ImmutableMap.of(
				MemoryModuleType.NEAREST_PLAYERS, MemoryStatus.VALUE_PRESENT,
				ExtraBotanyMemoryType.LANDMINE_COUNT, MemoryStatus.REGISTERED,
				ExtraBotanyMemoryType.TELEPORT_DELAY, MemoryStatus.REGISTERED
		));
	}

	public static void initMemories(Brain<? extends Gaia> brain, int mineCount) {
		brain.setMemory(ExtraBotanyMemoryType.LANDMINE_COUNT, mineCount);
	}

	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, E gaia) {
		return getTeleportDelay(gaia) == 0;
	}

	@Override
	protected void start(ServerLevel level, E gaia, long gameTime) {
		spawnLandMines(gaia);
	}

	protected void spawnLandMines(Gaia gaia) {
		int count = getLandMineCount(gaia);
		BlockPos source = gaia.getHome().pos();
		List<Player> players = getPlayers(gaia);

		if (players.isEmpty()) {
			return;
		}

		for (int i = 0; i < count; i++) {
			int x = source.getX() - 10 + gaia.getRandom().nextInt(20);
			int y = (int) players.get(gaia.getRandom().nextInt(players.size())).getY();
			int z = source.getZ() - 10 + gaia.getRandom().nextInt(20);

			MagicLandMineEntity landmine = ExtraBotanyEntityType.MAGIC_LANDMINE.create(gaia.level());
			landmine.setPos(x + 0.5, y, z + 0.5);
			landmine.setOwner(gaia);
			gaia.level().addFreshEntity(landmine);
		}
	}

	protected int getTeleportDelay(Gaia gaia) {
		return gaia.getBrain().getMemory(ExtraBotanyMemoryType.TELEPORT_DELAY).orElse(TELEPORT_DELAY);
	}

	protected int getLandMineCount(Gaia gaia) {
		return gaia.getBrain().getMemory(ExtraBotanyMemoryType.LANDMINE_COUNT).orElse(LANDMINE_COUNTS);
	}

	protected List<Player> getPlayers(Gaia gaia) {
		return gaia.getBrain().getMemory(MemoryModuleType.NEAREST_PLAYERS).orElse(new ArrayList<>());
	}
}
