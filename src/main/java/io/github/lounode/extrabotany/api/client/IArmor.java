package io.github.lounode.extrabotany.api.client;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import vazkii.botania.common.annotations.SoftImplement;

/**
 * 让盔甲物品自己决定穿在身上时使用的贴图。
 * Lets an armor item select the texture that is used when it is worn.
 */
public interface IArmor {
	@SoftImplement("IForgeItem")
	net.minecraft.resources.ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot,
			net.minecraft.world.item.ArmorMaterial.Layer layer, boolean innerModel);
}
