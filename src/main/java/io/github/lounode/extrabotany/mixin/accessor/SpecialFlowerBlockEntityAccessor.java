package io.github.lounode.extrabotany.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import vazkii.botania.api.block_entity.SpecialFlowerBlockEntity;

/**
 * 让蕴魔土能再调用一次特殊花的 tick 方法。
 * Lets the Enchanted Soil call the tick method of a special flower one more time.
 */
@Mixin(value = SpecialFlowerBlockEntity.class, remap = false)
public interface SpecialFlowerBlockEntityAccessor {
	@Invoker("tickFlower")
	void extrabotany_invokeTickFlower();
}
