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

/**
 * 每 tick 移除场地里玩家身上的信标类效果：环境效果、不是负面效果、并且剩余时间不到 160 tick。植物魔法的盖亚守护者有相同的规则。
 * On each tick, this removes the beacon-type effects of the players in the arena: ambient effects that are not harmful and that have less than 160 ticks left. The Guardian of Gaia of Botania has the same rule.
 */
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
