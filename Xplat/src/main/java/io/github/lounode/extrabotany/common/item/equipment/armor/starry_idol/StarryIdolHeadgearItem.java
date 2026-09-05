package io.github.lounode.extrabotany.common.item.equipment.armor.starry_idol;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.mana.ManaDiscountArmor;

public class StarryIdolHeadgearItem extends StarryIdolArmorItem implements ManaDiscountArmor {
	public StarryIdolHeadgearItem(Properties properties) {
		super(Type.HELMET, properties);
	}

	@Override
	public float getDiscount(ItemStack stack, int slot, Player player, @Nullable ItemStack tool) {
		return hasArmorSet(player) ? 0.6F : 0F;
	}
}
