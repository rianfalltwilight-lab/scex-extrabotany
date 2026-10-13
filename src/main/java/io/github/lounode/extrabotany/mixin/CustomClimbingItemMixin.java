package io.github.lounode.extrabotany.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import vazkii.botania.common.handler.EquipmentHandler;

import io.github.lounode.extrabotany.api.item.ClimbingItem;

/**
 * 让佩戴攀爬饰品（蛛行指环、跑酷指环）的玩家能爬墙。
 * Lets a player that wears a climbing accessory (the Spider Ring, the Parkour Ring) climb walls.
 */
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
		// 只有玩家会佩戴攀爬戒指。游戏每 tick 为每个生物多次调用这个方法，所以不要为别的生物查找饰品。
		// Only players wear the climbing rings. The game calls this method some times on each tick for each creature, thus do not search the accessories of other creatures.
		if (!(self instanceof Player)) {
			return;
		}
		ItemStack stack = EquipmentHandler.findOrEmpty((s) -> s.getItem() instanceof ClimbingItem, self);
		if (!stack.isEmpty() && stack.getItem() instanceof ClimbingItem climbingItem) {
			cir.setReturnValue(climbingItem.canClimb(stack, self));
		}
	}
}
