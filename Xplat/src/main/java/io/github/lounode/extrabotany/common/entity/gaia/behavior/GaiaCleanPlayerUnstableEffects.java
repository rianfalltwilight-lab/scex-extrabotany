package io.github.lounode.extrabotany.common.entity.gaia.behavior;

import com.google.common.collect.ImmutableMap;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.player.Player;

import io.github.lounode.extrabotany.common.entity.gaia.Gaia;

import java.util.ArrayList;
import java.util.List;

public class GaiaCleanPlayerUnstableEffects<E extends Gaia> extends Behavior<E> {

	public GaiaCleanPlayerUnstableEffects() {
		super(ImmutableMap.of(
				MemoryModuleType.NEAREST_PLAYERS, MemoryStatus.VALUE_PRESENT
		));
	}

	@Override
	protected boolean canStillUse(ServerLevel level, E entity, long gameTime) {
		return true;
	}

	@Override
	protected void tick(ServerLevel level, E gaia, long gameTime) {
		List<Player> players = getPlayers(gaia);
		for (Player player : players) {
			clearUnstablePotions(player);
		}
	}

	protected void clearUnstablePotions(Player player) {
		List<MobEffectInstance> effects = player.getActiveEffects().stream()
				.filter(effectInstance -> effectInstance.getDuration() < 160)
				.filter(MobEffectInstance::isAmbient)
				.filter(effect -> effect.getEffect().value().getCategory() != MobEffectCategory.HARMFUL)
				.toList();

		effects.forEach(e -> player.removeEffect(e.getEffect()));
	}

	protected List<Player> getPlayers(Gaia gaia) {
		return gaia.getBrain().getMemory(MemoryModuleType.NEAREST_PLAYERS).orElse(new ArrayList<>());
	}
}
