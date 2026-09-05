package io.github.lounode.extrabotany.fabric.mixin;

import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import vazkii.botania.common.helper.ItemNBTHelper;

import io.github.lounode.extrabotany.fabric.xplat.ExFabricXplatImpl;

import java.util.UUID;

@Mixin(EnchantmentMenu.class)
public class GetBlockEnchantLevelMixin {

	@Inject(
		method = "method_17411(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/util/RandomSource;setSeed(J)V"
		)
	)
	private void afterLevelCalc(ItemStack itemStack, Level level, BlockPos blockPos, CallbackInfo ci, @Local int i) {
		if (itemStack.is(Items.GOLDEN_HOE)) {
			String uuidString = ItemNBTHelper.getString(itemStack, "[EXTRABOTANY_QUERY_ENCHANT_LEVEL]", "null");
			if (!"null".equals(uuidString)) {
				try {
					UUID uuid = UUID.fromString(uuidString);
					ExFabricXplatImpl.SIMULATE_QUERY.put(uuid, i);
				} catch (Exception ignored) {}
			}
		}
	}
}
