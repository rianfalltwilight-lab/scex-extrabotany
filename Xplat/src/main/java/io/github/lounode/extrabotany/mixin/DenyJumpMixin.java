package io.github.lounode.extrabotany.mixin;

import net.minecraft.world.entity.LivingEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.lounode.extrabotany.common.brew.ExtraBotanyMobEffects;

@Mixin(LivingEntity.class)
public class DenyJumpMixin {
	@Inject(method = "getJumpPower", at = @At("RETURN"), cancellable = true)
	private void getJumpPower(CallbackInfoReturnable<Float> cir) {
		LivingEntity self = (LivingEntity) (Object) this;
		if (self.hasEffect(ExtraBotanyMobEffects.IMMOBILIZE)) {
			cir.setReturnValue(0.0f);
		}
	}
}
