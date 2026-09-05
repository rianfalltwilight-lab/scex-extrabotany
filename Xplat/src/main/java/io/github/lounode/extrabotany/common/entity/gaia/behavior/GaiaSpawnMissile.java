package io.github.lounode.extrabotany.common.entity.gaia.behavior;

import com.google.common.collect.ImmutableMap;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;

import vazkii.botania.common.entity.MagicMissileEntity;
import vazkii.botania.common.handler.BotaniaSounds;

import io.github.lounode.extrabotany.common.entity.gaia.Gaia;

public class GaiaSpawnMissile<E extends Gaia> extends Behavior<E> {

	public GaiaSpawnMissile() {
		super(ImmutableMap.of());

	}

	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, E owner) {
		return canSpawnMissile(owner.tickCount);
	}

	@Override
	protected void start(ServerLevel level, E gaia, long gameTime) {
		spawnMissile(gaia);
	}

	/// Every 15 tick run 4 times to spawn missile
	protected boolean canSpawnMissile(int tickCount) {
		return tickCount % 15 < 4;
	}

	protected void spawnMissile(Gaia gaia) {
		MagicMissileEntity missile = new MagicMissileEntity(gaia, true);
		missile.setPos(
				gaia.getX() + (Math.random() - 0.5 * 0.1),
				gaia.getY() + 2.4 + (Math.random() - 0.5 * 0.1),
				gaia.getZ() + (Math.random() - 0.5 * 0.1)
		);
		if (missile.findTarget()) {
			gaia.playSound(BotaniaSounds.MISSILE, 1F, 0.8F + (float) Math.random() * 0.2F);
			gaia.level().addFreshEntity(missile);
		}
	}
}
