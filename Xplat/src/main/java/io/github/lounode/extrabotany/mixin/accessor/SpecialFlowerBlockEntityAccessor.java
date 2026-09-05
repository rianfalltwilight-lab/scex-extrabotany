package io.github.lounode.extrabotany.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import vazkii.botania.api.block_entity.SpecialFlowerBlockEntity;

@Mixin(value = SpecialFlowerBlockEntity.class, remap = false)
public interface SpecialFlowerBlockEntityAccessor {
	@Invoker("tickFlower")
	void extrabotany_invokeTickFlower();
}
