package io.github.lounode.extrabotany.common.brew.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * 定身。有这个效果的生物不能移动，也不能跳跃。移动速度由属性修饰符降为零，跳跃在 {@code DenyJumpMixin} 里禁止。
 * Immobilize. A creature with this effect cannot move and cannot jump. An attribute modifier decreases the movement speed to zero, and {@code DenyJumpMixin} prevents the jump.
 */
public class ImmobilizeMobEffect extends MobEffect {
	public ImmobilizeMobEffect(MobEffectCategory category, int color) {
		super(category, color);
	}

}
