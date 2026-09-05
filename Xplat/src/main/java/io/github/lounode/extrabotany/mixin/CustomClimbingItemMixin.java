package io.github.lounode.extrabotany.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import vazkii.botania.common.handler.EquipmentHandler;

import io.github.lounode.extrabotany.api.item.ClimbingItem;

@Mixin(LivingEntity.class)
public class CustomClimbingItemMixin {

	@Inject(
		method = "onClimbable",
		at = @At("RETURN"),
		cancellable = true
	)
	private void onClimbable(CallbackInfoReturnable<Boolean> cir) {
		if (cir.getReturnValue()) {
			return;
		}
		LivingEntity self = (LivingEntity) (Object) this;
		ItemStack stack = EquipmentHandler.findOrEmpty((s) -> s.getItem() instanceof ClimbingItem, self);
		if (!stack.isEmpty() && stack.getItem() instanceof ClimbingItem climbingItem) {
			cir.setReturnValue(climbingItem.canClimb(stack, self));
		}
	}
}
