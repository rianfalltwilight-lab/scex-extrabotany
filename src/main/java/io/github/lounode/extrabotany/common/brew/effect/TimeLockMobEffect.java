package io.github.lounode.extrabotany.common.brew.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * 时空锁定。有这个效果的生物在下落时，速度每 tick 减到 3%，所以它几乎停在空中。薪炎的技能给目标这个效果。
 * Time Lock. When a creature with this effect falls, its speed decreases to 3% on each tick, thus it almost stays in the air. The skills of the Flamescion give this effect to the targets.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code PotionTimeLock}。
 * <p>Source: ExtraMeteorP 1.16 {@code PotionTimeLock}.
 */
public final class TimeLockMobEffect extends MobEffect {
    public TimeLockMobEffect() { super(MobEffectCategory.HARMFUL, 0xFFD700); }
    @Override public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) { return true; }
    @Override public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.getDeltaMovement().y < 0) entity.setDeltaMovement(entity.getDeltaMovement().scale(.03));
        return true;
    }
}
