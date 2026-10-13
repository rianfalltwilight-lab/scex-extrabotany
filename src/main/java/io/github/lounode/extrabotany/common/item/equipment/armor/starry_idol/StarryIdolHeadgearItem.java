package io.github.lounode.extrabotany.common.item.equipment.armor.starry_idol;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.mana.ManaDiscountArmor;

/**
 * 星空歌姬头饰。穿着整套时，魔力消耗减少 60%。
 * Starry Idol Headgear. With the full set, the mana cost decreases by 60%.
 */
public class StarryIdolHeadgearItem extends StarryIdolArmorItem implements ManaDiscountArmor {
	public StarryIdolHeadgearItem(Properties properties) {
		super(Type.HELMET, properties);
	}

	@Override
	public float getDiscount(ItemStack stack, int slot, Player player, @Nullable ItemStack tool) {
		return hasArmorSet(player) ? 0.6F : 0F;
	}
}
