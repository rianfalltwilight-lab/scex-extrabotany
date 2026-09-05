package io.github.lounode.extrabotany.common.entity.gaia.behavior;

import com.google.common.collect.ImmutableMap;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import io.github.lounode.extrabotany.common.entity.gaia.Gaia;

public class GaiaEmerging<E extends Gaia> extends Behavior<E> {
	public GaiaEmerging(int duration) {
		super(ImmutableMap.of(MemoryModuleType.IS_EMERGING, MemoryStatus.VALUE_PRESENT), duration);
	}

	@Override
	protected boolean canStillUse(ServerLevel level, E entity, long gameTime) {
		return true;
	}

	@Override
	protected void tick(ServerLevel level, E gaia, long gameTime) {
		int invul = gaia.getInvulTime();

		if (invul > 0) {
			if (invul < gaia.getEmergeTime()) {
				if (invul > gaia.getEmergeTime() / 2 && gaia.level().random.nextInt(gaia.getEmergeTime() - invul + 1) == 0) {
					for (int i = 0; i < 2; i++) {
						gaia.spawnAnim();
					}
				}
			}
			gaia.setHealth(gaia.getHealth() + (gaia.getMaxHealth() - 1F) / gaia.getEmergeTime());
			gaia.setDeltaMovement(gaia.getDeltaMovement().x(), 0, gaia.getDeltaMovement().z());
		}
	}
}
