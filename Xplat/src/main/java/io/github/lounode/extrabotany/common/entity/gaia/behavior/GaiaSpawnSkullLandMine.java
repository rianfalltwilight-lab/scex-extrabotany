package io.github.lounode.extrabotany.common.entity.gaia.behavior;

import net.minecraft.core.BlockPos;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;

import io.github.lounode.extrabotany.common.entity.ExtraBotanyEntityType;
import io.github.lounode.extrabotany.common.entity.SkullLandMineEntity;
import io.github.lounode.extrabotany.common.entity.gaia.Gaia;
import io.github.lounode.extrabotany.xplat.ExtraBotanyConfig;

import java.util.List;

public class GaiaSpawnSkullLandMine<E extends Gaia> extends GaiaSpawnLandMine<E> {
	public static final List<WeightedEntry.Wrapper<EntityType<? extends SkullLandMineEntity>>> TYPES_POOL = List.of(
			WeightedEntry.wrap(ExtraBotanyEntityType.SKULL_LANDMINE_BLUE, 70),
			WeightedEntry.wrap(ExtraBotanyEntityType.SKULL_LANDMINE_RED, 20),
			WeightedEntry.wrap(ExtraBotanyEntityType.SKULL_LANDMINE_GREEN, 10)
	);

	@Override
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

			SkullLandMineEntity landmine = getNewLandMine(gaia);
			landmine.setPos(x + 0.5, y, z + 0.5);
			gaia.level().addFreshEntity(landmine);
		}
	}

	protected SkullLandMineEntity getNewLandMine(Gaia gaia) {
		EntityType<? extends SkullLandMineEntity> landmineType = WeightedRandom.getRandomItem(gaia.getRandom(), TYPES_POOL)
				.orElse(WeightedEntry.wrap(ExtraBotanyEntityType.SKULL_LANDMINE_BLUE, 70))
				.data();

		if (landmineType == ExtraBotanyEntityType.SKULL_LANDMINE_RED) {
			return new SkullLandMineEntity.Danger(gaia.level(), gaia);
		} else if (landmineType == ExtraBotanyEntityType.SKULL_LANDMINE_GREEN && !ExtraBotanyConfig.common().disableGaiaDisArm()) {
			return new SkullLandMineEntity.Disarm(gaia.level(), gaia);
		} else {
			return new SkullLandMineEntity.Default(gaia.level(), gaia);
		}
	}
}
