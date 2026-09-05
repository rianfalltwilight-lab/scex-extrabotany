package io.github.lounode.extrabotany.mixin.accessor;

import net.minecraft.world.phys.AABB;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import vazkii.botania.common.block.block_entity.flower.functional.DaffomillBlockEntity;

@Mixin(value = DaffomillBlockEntity.class, remap = false)
public interface DaffomillBlockEntityAccessor {

	@Invoker("aabbForOrientation")
	AABB extrabotany_aabbForOrientation();

	@Accessor("windTicks")
	int getWindTicks();
}
