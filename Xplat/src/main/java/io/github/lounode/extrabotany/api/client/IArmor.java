package io.github.lounode.extrabotany.api.client;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import vazkii.botania.common.annotations.SoftImplement;

public interface IArmor {
	@SoftImplement("IForgeItem")
	net.minecraft.resources.ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot,
			net.minecraft.world.item.ArmorMaterial.Layer layer, boolean innerModel);
}
