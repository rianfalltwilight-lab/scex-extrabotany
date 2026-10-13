package io.github.lounode.extrabotany.mixin.botania;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import vazkii.botania.common.handler.EquipmentHandler;

import io.github.lounode.extrabotany.common.item.equipment.bauble.AllForOneRingItem;

import java.util.function.Predicate;

/**
 * 让植物魔法的装备查找也能找到全能指环模拟的饰品。
 * Lets the equipment lookup of Botania also find the accessories that the ALL FOR ONE ring simulates.
 *
 * <p>只有在植物魔法没有找到佩戴的饰品时，才去查找被模拟的饰品。
 * <p>The simulated accessories are only searched when Botania finds no worn accessory.
 */
@Mixin(EquipmentHandler.class)
public abstract class BoxEquipmentMixin {
	@Inject(
		method = "findOrEmpty(Lnet/minecraft/world/item/Item;Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/item/ItemStack;",
		at = @At("RETURN"),
		cancellable = true
	)
	private static void extrabotany$findSimulatedItem(Item item, LivingEntity user, CallbackInfoReturnable<ItemStack> result) {
		if (result.getReturnValue().isEmpty()) {
			result.setReturnValue(AllForOneRingItem.find(stack -> stack.is(item), user));
		}
	}

	@Inject(
		method = "findOrEmpty(Ljava/util/function/Predicate;Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/item/ItemStack;",
		at = @At("RETURN"),
		cancellable = true
	)
	private static void extrabotany$findSimulatedMatch(Predicate<ItemStack> predicate, LivingEntity user, CallbackInfoReturnable<ItemStack> result) {
		if (result.getReturnValue().isEmpty()) {
			result.setReturnValue(AllForOneRingItem.find(predicate, user));
		}
	}
}
