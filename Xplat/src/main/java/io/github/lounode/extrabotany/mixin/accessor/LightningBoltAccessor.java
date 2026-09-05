package io.github.lounode.extrabotany.mixin.accessor;

import net.minecraft.world.entity.LightningBolt;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LightningBolt.class)
public interface LightningBoltAccessor {
	@Accessor("visualOnly")
	boolean isVisualOnly();
}
