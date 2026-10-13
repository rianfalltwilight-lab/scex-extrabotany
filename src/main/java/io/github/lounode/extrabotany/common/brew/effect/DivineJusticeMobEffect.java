package io.github.lounode.extrabotany.common.brew.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.common.EffectCure;

import io.github.lounode.extrabotany.common.brew.ExtraBotanyMobEffects;
import io.github.lounode.extrabotany.common.handler.DamageHandler;

import java.util.Set;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 神圣制裁。每一级让最大生命值降低 5%。效果快结束时，造成"等级 × 2"点真实伤害。牛奶不能解除它。
 * Divine Justice. Each level decreases the maximum health by 5%. Near the end of the effect, it does true damage of "level × 2". Milk cannot remove it.
 *
 * <p>犹大的誓约的光矛每命中一次，这个效果升一级，最高五级。
 * <p>Each hit of a spear of the Judah Oath adds one level to this effect, to a maximum of five levels.
 *
 * <p>来源：ExtraMeteorP 1.12 {@code PotionDivineJustice} 和 {@code ExtraBotanyAPI#addPotionEffect}。
 * <p>Source: ExtraMeteorP 1.12 {@code PotionDivineJustice} and {@code ExtraBotanyAPI#addPotionEffect}.
 */
public final class DivineJusticeMobEffect extends MobEffect {
	private static final double MAX_HEALTH_PER_LEVEL = -0.05;
	private static final int DAMAGE_AT_REMAINING_TICKS = 5;
	private static final int STACK_DURATION_TICKS = 100;
	private static final int MAX_AMPLIFIER = 4;

	public DivineJusticeMobEffect() {
		super(MobEffectCategory.HARMFUL, 0xFF7F50);
		addAttributeModifier(Attributes.MAX_HEALTH, prefix("effect.divine_justice"), MAX_HEALTH_PER_LEVEL,
				AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
	}

	/**
	 * 给目标加上这个效果。目标已经有这个效果时升一级，并且刷新持续时间。
	 * Gives this effect to the target. When the target already has the effect, the level goes up by one and the duration starts again.
	 */
	public static void stack(LivingEntity target) {
		MobEffectInstance current = target.getEffect(ExtraBotanyMobEffects.DIVINE_JUSTICE);
		int amplifier = current == null ? 0 : Math.min(MAX_AMPLIFIER, current.getAmplifier() + 1);
		target.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.DIVINE_JUSTICE, STACK_DURATION_TICKS, amplifier));
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
		return duration == DAMAGE_AT_REMAINING_TICKS;
	}

	@Override
	public boolean applyEffectTick(LivingEntity entity, int amplifier) {
		DamageHandler.INSTANCE.dealTrueDamage(null, entity, amplifier * 2F);
		return true;
	}

	@Override
	public void fillEffectCures(Set<EffectCure> cures, MobEffectInstance effectInstance) {
		// 不加入任何解除方式，所以牛奶和其他物品都不能解除这个效果。
		// No cure is added, so milk and other items cannot remove this effect.
	}
}
