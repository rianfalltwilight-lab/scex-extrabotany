package io.github.lounode.extrabotany.common.entity.gaia;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Dynamic;

import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Swim;
import net.minecraft.world.entity.schedule.Activity;

import io.github.lounode.extrabotany.common.entity.gaia.behavior.GaiaCleanPlayerUnstableEffects;
import io.github.lounode.extrabotany.common.entity.gaia.behavior.GaiaDisarm;
import io.github.lounode.extrabotany.common.entity.gaia.behavior.GaiaSmashBlocksAround;

/**
 * 空之律者和本我的行为树。
 * The behavior tree of the Herrscher of the Void and of the Ego.
 *
 * <p>它只包含这两个 Boss 和盖亚守护者共用的规则：缴械、清除信标效果和打碎身边的方块。它们自己的攻击都写在 {@link VoidHerrscher} 和 {@link Ego} 里，按原版的固定节奏进行。
 * <p>It only has the rules that these two bosses share with the Guardians of Gaia: the disarm, the removal of beacon effects and the destruction of adjacent blocks. Their attacks are in {@link VoidHerrscher} and {@link Ego} and follow the fixed timing of the original.
 *
 * <p>以前空之律者使用盖亚守护者III的行为树。那样它会同时做两套攻击，还会召唤精灵。
 * <p>Before, the Herrscher used the behavior tree of Guardian of Gaia III. Then it did two sets of attacks at the same time and also summoned pixies.
 */
public final class ScriptedBossAI extends GaiaAI {
	private ScriptedBossAI() {}

	static Brain<?> makeBrain(Dynamic<?> ops) {
		Brain<Gaia> brain = Brain.<Gaia>provider(MEMORY_TYPES, SENSOR_TYPES).makeBrain(ops);
		brain.addActivity(Activity.CORE, 0, ImmutableList.of(
				new Swim(0.8F),
				new GaiaDisarm<>()
		));
		brain.addActivity(Activity.FIGHT, 10, ImmutableList.of(
				new GaiaSmashBlocksAround<>(),
				new GaiaCleanPlayerUnstableEffects<>()
		));
		brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
		brain.setDefaultActivity(Activity.FIGHT);
		brain.useDefaultActivity();
		return brain;
	}

	public static void updateActivity(Gaia boss) {
		boss.getBrain().setActiveActivityIfPossible(Activity.FIGHT);
	}
}
