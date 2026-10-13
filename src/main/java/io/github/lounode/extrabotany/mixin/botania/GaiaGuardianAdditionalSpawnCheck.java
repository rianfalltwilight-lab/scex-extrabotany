package io.github.lounode.extrabotany.mixin.botania;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import vazkii.botania.common.entity.GaiaGuardianEntity;

import io.github.lounode.extrabotany.api.gaia.GaiaArena;
import io.github.lounode.extrabotany.common.entity.gaia.GaiaIII;

/**
 * 附近有本模组的盖亚守护者III时，不允许再召唤植物魔法的盖亚守护者。
 * When a Guardian of Gaia III of this mod is near, this does not let a Guardian of Gaia of Botania be summoned.
 */
@Mixin(value = GaiaGuardianEntity.class, remap = false)
public abstract class GaiaGuardianAdditionalSpawnCheck {

	@Inject(
		method = "anyActiveArenasAround",
		at = @At("RETURN"),
		cancellable = true
	)
	private static void onCheckActiveArena(Level world, BlockPos source, CallbackInfoReturnable<Boolean> cir) {
		if (!cir.getReturnValue() && GaiaArena.countGaiaAround(world, source, 15.0F, GaiaIII.class) > 0) {
			cir.setReturnValue(true);
		}
	}
}
