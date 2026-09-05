package io.github.lounode.extrabotany.mixin.botania;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import vazkii.botania.common.entity.MagicMissileEntity;

import io.github.lounode.extrabotany.common.entity.gaia.Gaia;

@Mixin(value = MagicMissileEntity.class, remap = false)
public class MagicMissileTargetBypassGaiaIII {

	@Inject(
		method = "shouldTarget",
		at = @At("RETURN"),
		cancellable = true
	)
	private static void onShouldTarget(Entity owner, LivingEntity e, CallbackInfoReturnable<Boolean> cir) {
		if (e instanceof Gaia) {
			cir.setReturnValue(false);
		}
	}
}
