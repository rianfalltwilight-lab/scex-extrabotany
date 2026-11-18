package io.github.lounode.extrabotany.mixin.botania;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import vazkii.botania.api.block_entity.SpecialFlowerBlockEntity;
import vazkii.botania.common.block.block_entity.flower.generating.HydroangeasBlockEntity;

import io.github.lounode.extrabotany.api.block.PassiveFlower;

import io.github.lounode.extrabotany.common.block.ExtraBotanyBlocks;
import io.github.lounode.extrabotany.mixin.accessor.SpecialFlowerBlockEntityAccessor;

/**
 * 让长在蕴魔土上的特殊花每 tick 运行两次。被动产能花不加速，和 1.12 植物魔法的附魔土一样（被动花在上面只是不枯萎）。
 * Makes a special flower on Enchanted Soil run two times on each tick. A passive generating flower is not made faster, as with the Enchanted Soil of Botania 1.12 (a passive flower on it only does not decay).
 */
@Mixin(value = SpecialFlowerBlockEntity.class, remap = false)
public abstract class EnchantedSoilFlowerBoostMixin {
	@Inject(method = "commonTick", at = @At("TAIL"))
	private static void extrabotany_tickOnEnchantedSoil(Level level, BlockPos pos, BlockState state,
			SpecialFlowerBlockEntity flower, CallbackInfo ci) {
		// 第一次 tick 可能已经让花消失了（例如被动花枯萎），所以要先确认花还在。
		// The first tick can remove the flower (for example, a passive flower that decays), thus first make sure that the flower is there.
		if (!flower.isFloating()
				&& !flower.isRemoved()
				&& !(flower instanceof PassiveFlower)
				&& !(flower instanceof HydroangeasBlockEntity)
				&& level.getBlockState(pos.below()).is(ExtraBotanyBlocks.enchantedSoil)) {
			((SpecialFlowerBlockEntityAccessor) flower).extrabotany_invokeTickFlower();
		}
	}
}
