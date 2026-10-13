package io.github.lounode.extrabotany.mixin.botania;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import vazkii.botania.common.entity.MagicMissileEntity;

import io.github.lounode.extrabotany.common.entity.gaia.Gaia;

/**
 * 让植物魔法的魔法飞弹不把本模组的盖亚守护者当作目标。植物魔法对它自己的盖亚守护者也是这样处理的。
 * Makes the magic missiles of Botania not select a Guardian of Gaia of this mod as a target. Botania does the same for its own Guardian of Gaia.
 */
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
