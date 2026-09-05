package io.github.lounode.extrabotany.mixin.botania;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import vazkii.botania.common.item.WandOfTheForestItem;

import io.github.lounode.extrabotany.api.level.Wind;
import io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks;

@Mixin(value = WandOfTheForestItem.class)
public abstract class DisplayWindLevel {

	@Inject(
		method = "use",
		at = @At("HEAD"),
		cancellable = true
	)
	private void onUse(Level level, Player player, InteractionHand usedHand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
		ItemStack offHand = player.getItemInHand(InteractionHand.OFF_HAND);
		ItemStack mainHand = player.getItemInHand(InteractionHand.MAIN_HAND);

		if (mainHand.getItem() instanceof WandOfTheForestItem && offHand.is(ExtrabotanyFlowerBlocks.bellflower.asItem())) {
			if (level.isClientSide()) {
				return;
			}

			if (player.pick(5.0F, 0.0F, false).getType() == HitResult.Type.MISS) {
				Component component = Component.translatable(
						"message.extrabotany.chat.wind_level",
						Wind.instance().getWindLevel(level, player.position())
				);
				player.sendSystemMessage(component);
				cir.setReturnValue(InteractionResultHolder.success(player.getItemInHand(usedHand)));
			}
		}
	}
}
