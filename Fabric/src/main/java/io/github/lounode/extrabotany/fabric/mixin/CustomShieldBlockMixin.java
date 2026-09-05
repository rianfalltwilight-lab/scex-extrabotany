package io.github.lounode.extrabotany.fabric.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import io.github.lounode.extrabotany.common.lib.ExtraBotanyTags;

@Mixin(Player.class)
public class CustomShieldBlockMixin {

	@ModifyExpressionValue(
		method = "hurtCurrentlyUsedShield",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z")
	)
	private boolean onHurtCurrentlyUsedShield(boolean original) {
		Player player = (Player) (Object) this;
		return original || player.getUseItem().is(ExtraBotanyTags.Items.SHIELDS);
	}
}
