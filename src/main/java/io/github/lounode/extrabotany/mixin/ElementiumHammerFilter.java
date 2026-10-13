package io.github.lounode.extrabotany.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import io.github.lounode.extrabotany.common.item.equipment.tool.hammer.ElementiumHammerItem;

import java.util.function.Consumer;

/**
 * 源质钢锤破坏方块时，从掉落物里去掉"一次性"的方块。这和植物魔法的源质钢镐相同。
 * When an Elementium Hammer breaks a block, this removes the "disposable" blocks from the drops. This is the same as the Elementium Pickaxe of Botania.
 */
@Mixin(value = LootTable.class, priority = 999)
public class ElementiumHammerFilter {

	@ModifyVariable(method = "getRandomItemsRaw(Lnet/minecraft/world/level/storage/loot/LootContext;Ljava/util/function/Consumer;)V", at = @At("HEAD"), argsOnly = true)
	private Consumer<ItemStack> filterDisposables(Consumer<ItemStack> inner, LootContext context) {
		return stack -> {
			Entity e = context.getParamOrNull(LootContextParams.THIS_ENTITY);
			ItemStack tool = context.getParamOrNull(LootContextParams.TOOL);

			if (e != null && tool != null) {
				if (ElementiumHammerItem.shouldFilterOut(e, tool, stack)) {
					return;
				}
			}

			inner.accept(stack);
		};
	}
}
