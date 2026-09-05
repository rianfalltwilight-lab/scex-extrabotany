package io.github.lounode.extrabotany.mixin.botania;

import io.github.lounode.extrabotany.common.item.legacy.LegacyBoxSimulator;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vazkii.botania.common.handler.EquipmentHandler;
import java.util.function.Predicate;

@Mixin(EquipmentHandler.class)
public abstract class LegacyBoxEquipmentMixin {
    @Inject(method = "findOrEmpty(Lnet/minecraft/world/item/Item;Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/item/ItemStack;", at = @At("RETURN"), cancellable = true)
    private static void item(Item item, LivingEntity user, CallbackInfoReturnable<ItemStack> result) {
        if (result.getReturnValue().isEmpty()) result.setReturnValue(LegacyBoxSimulator.find(stack -> stack.is(item), user));
    }
    @Inject(method = "findOrEmpty(Ljava/util/function/Predicate;Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/item/ItemStack;", at = @At("RETURN"), cancellable = true)
    private static void predicate(Predicate<ItemStack> predicate, LivingEntity user, CallbackInfoReturnable<ItemStack> result) {
        if (result.getReturnValue().isEmpty()) result.setReturnValue(LegacyBoxSimulator.find(predicate, user));
    }
}
