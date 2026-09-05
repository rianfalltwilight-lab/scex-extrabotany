package io.github.lounode.extrabotany.mixin.botania;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import vazkii.botania.api.mana.ManaPool;
import vazkii.botania.common.handler.BotaniaSounds;
import io.github.lounode.extrabotany.common.helper.ItemNBTHelper;
import vazkii.botania.common.item.WandOfTheForestItem;

import io.github.lounode.extrabotany.common.block.flower.functional.ManalinkBlockEntity;
import io.github.lounode.extrabotany.common.item.WandOfTheForestItemExtension;
import io.github.lounode.extrabotany.common.proxy.Proxy;

@Mixin(value = WandOfTheForestItem.class, remap = false)
public abstract class WandOfTheForestExtension {

	@Shadow
	public static boolean getBindMode(ItemStack stack) {
		return false;
	}

	@Inject(
		method = "setBindingAttempt",
		at = @At("RETURN")
	)
	private static void onSetBind(ItemStack stack, @Nullable GlobalPos pos, @Nullable Direction side,
			@Nullable Block block, CallbackInfo ci) {
		if (pos == null) {
			ItemNBTHelper.removeEntry(stack, WandOfTheForestItemExtension.TAG_EXTEND_BOUND);
		}
	}

	@Inject(
		method = "useOn",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/Level;getBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;",
			shift = At.Shift.AFTER
		),
		cancellable = true,
		remap = true
	)
	private void bindForManaLink(UseOnContext ctx, CallbackInfoReturnable<InteractionResult> cir) {
		ItemStack stack = ctx.getItemInHand();
		Level world = ctx.getLevel();
		BlockPos pos = ctx.getClickedPos();
		Player player = ctx.getPlayer();
		BlockState state = world.getBlockState(pos);
		BlockEntity tile = world.getBlockEntity(pos);

		if (player == null || !player.isSecondaryUseActive()) {
			return;
		}

		if (tile instanceof ManaPool && getBindMode(stack)) {
			GlobalPos globalPos = GlobalPos.of(ctx.getLevel().dimension(), pos);
			WandOfTheForestItemExtension.setBindingAttemptExtend(ctx.getItemInHand(), globalPos);

			if (world.isClientSide) {
				player.playSound(BotaniaSounds.DING, 0.11F, 1F);
				String dimensionKey = world.dimension().location().toString();

				Proxy.INSTANCE.displayClientMessage(Component.translatable("message.extrabotany.actionbar.selected_pos",
						dimensionKey + "[x=" + pos.getX() + ",y=" + pos.getY() + ",z=" + pos.getZ() + "]").withStyle(ChatFormatting.GREEN), true);
			}
			cir.setReturnValue(InteractionResult.SUCCESS);
		}

		if (tile instanceof ManalinkBlockEntity manalink && getBindMode(stack)) {
			WandOfTheForestItemExtension.getBindingAttempt(stack).ifPresent(globalPos -> {
				manalink.setLinkPos(globalPos);
				if (world.isClientSide) {
					player.playSound(BotaniaSounds.DING, 0.11F, 1F);

					Proxy.INSTANCE.displayClientMessage(Component.translatable("message.extrabotany.actionbar.bind_to_pos",
							globalPos.dimension().location() +
									"[x=" + globalPos.pos().getX() + ",y=" + globalPos.pos().getY() + ",z=" + globalPos.pos().getZ() + "]")
							.withStyle(ChatFormatting.GREEN), true);
				}
				cir.setReturnValue(InteractionResult.SUCCESS);
			});
		}
	}
}
