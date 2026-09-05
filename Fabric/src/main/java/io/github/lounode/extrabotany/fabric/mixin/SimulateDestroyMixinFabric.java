package io.github.lounode.extrabotany.fabric.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayerGameMode;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.lounode.extrabotany.common.util.PlayerUtil;

@Mixin(ServerPlayerGameMode.class)
public class SimulateDestroyMixinFabric {

	@Inject(
		method = "destroyBlock",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/block/Block;playerWillDestroy(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/player/Player;)V"
		),
		cancellable = true
	)
	private void onDestroyBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		if (pos instanceof PlayerUtil.SimulateDestroyBlockPos) {
			cir.setReturnValue(true);
		}
	}
}
