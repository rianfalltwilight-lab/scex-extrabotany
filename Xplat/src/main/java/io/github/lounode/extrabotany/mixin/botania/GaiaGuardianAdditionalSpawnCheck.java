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
