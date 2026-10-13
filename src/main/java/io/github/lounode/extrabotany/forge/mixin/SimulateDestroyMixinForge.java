package io.github.lounode.extrabotany.forge.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayerGameMode;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.lounode.extrabotany.common.util.PlayerUtil;

/**
 * 让 {@code PlayerUtil#tryBreakBlock} 能"模拟"一次破坏方块：原版的流程照常进行（所以领地保护类的模组能够阻止它），但是在真正移除方块之前返回。
 * Lets {@code PlayerUtil#tryBreakBlock} "simulate" a block break: the vanilla sequence runs as usual (thus protection mods can prevent it), but it returns before the block is really removed.
 */
@Mixin(ServerPlayerGameMode.class)
public abstract class SimulateDestroyMixinForge {

	@Shadow
	public abstract boolean isCreative();

	@Inject(
		method = "destroyBlock",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/item/ItemStack;mineBlock(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;)V"
		),
		cancellable = true
	)
	private void onDestroyBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		if (pos instanceof PlayerUtil.SimulateDestroyBlockPos) {
			cir.setReturnValue(true);
		}
	}

	@Inject(
		method = "destroyBlock",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/level/ServerPlayerGameMode;isCreative()Z"
		),
		cancellable = true
	)
	private void onCreativeDestroyBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		if (!isCreative()) {
			return;
		}
		if (pos instanceof PlayerUtil.SimulateDestroyBlockPos) {
			cir.setReturnValue(true);
		}
	}
}
