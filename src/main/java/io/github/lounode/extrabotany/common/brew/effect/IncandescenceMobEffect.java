package io.github.lounode.extrabotany.common.brew.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import io.github.lounode.extrabotany.common.handler.FlamescionHandler;

/**
 * 灼热模式。薪炎模式的标志效果。它让下落速度减到 5%，所以玩家几乎悬停在空中。
 * Incandescence Mode. The marker effect of the Flamescion mode. It decreases the fall speed to 5%, so the player almost stays in the air.
 *
 * <p>玩家不在薪炎模式时（落地、换了武器或者过载），效果立刻消失。
 * <p>When the player is not in the Flamescion mode (on the ground, with a different weapon, or with an overload), the effect goes away immediately.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code PotionIncandescence}。
 * <p>Source: ExtraMeteorP 1.16 {@code PotionIncandescence}.
 */
public final class IncandescenceMobEffect extends MobEffect {
	private static final double FALL_SPEED_FACTOR = 0.05;

	public IncandescenceMobEffect() {
		super(MobEffectCategory.BENEFICIAL, 0xDC143C);
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
		return true;
	}

	/**
	 * @return 返回 {@code false} 时游戏移除这个效果。 When this returns {@code false}, the game removes the effect.
	 */
	@Override
	public boolean applyEffectTick(LivingEntity entity, int amplifier) {
		Vec3 motion = entity.getDeltaMovement();
		if (motion.y < 0) {
			entity.setDeltaMovement(motion.multiply(1.0, FALL_SPEED_FACTOR, 1.0));
		}
		return !(entity instanceof Player player) || FlamescionHandler.isFlamescionMode(player);
	}
}
