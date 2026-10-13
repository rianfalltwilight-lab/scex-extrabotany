package io.github.lounode.extrabotany.mixin.accessor;

import net.minecraft.world.entity.LightningBolt;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * 让雷卡兰能知道一道闪电是否只有视觉效果。
 * Lets the Reikarlily know if a lightning bolt only has a visual effect.
 */
@Mixin(LightningBolt.class)
public interface LightningBoltAccessor {
	@Accessor("visualOnly")
	boolean isVisualOnly();
}
