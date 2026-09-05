package io.github.lounode.extrabotany.mixin.botania;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import vazkii.botania.api.block_entity.SpecialFlowerBlockEntity;

import io.github.lounode.extrabotany.common.block.ExtraBotanyBlocks;
import io.github.lounode.extrabotany.mixin.accessor.SpecialFlowerBlockEntityAccessor;

@Mixin(value = SpecialFlowerBlockEntity.class, remap = false)
public abstract class EnchantedSoilFlowerBoostMixin {
	@Inject(method = "commonTick", at = @At("TAIL"))
	private static void extrabotany_tickOnEnchantedSoil(Level level, BlockPos pos, BlockState state,
			SpecialFlowerBlockEntity flower, CallbackInfo ci) {
		if (level.getBlockEntity(pos) == flower
				&& !flower.isFloating()
				&& level.getBlockState(pos.below()).is(ExtraBotanyBlocks.enchantedSoil)) {
			((SpecialFlowerBlockEntityAccessor) flower).extrabotany_invokeTickFlower();
		}
	}
}
