package io.github.lounode.extrabotany.common.item.equipment.bauble;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import vazkii.botania.api.neoforge.mana.ManaDiscountEvent;
import vazkii.botania.common.item.equipment.bauble.BaubleItem;

/**
 * 水之符石。佩戴后，工具的魔力消耗降低 10%。
 * Aqua Stone. The bauble decreases the mana cost of tools by 10%.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code ItemAquaStone}。
 * <p>Source: ExtraMeteorP 1.16 {@code ItemAquaStone}.
 */
public class AquaStoneItem extends BaubleItem {
	public static final float MANA_DISCOUNT = 0.1F;

	public AquaStoneItem(Properties props) {
		super(props);
	}

	/**
	 * 处理魔力折扣事件。水之符石和四元归一石都提供折扣。
	 * Handles the mana discount event. The Aqua Stone and The Community give the discount.
	 */
	public static void onManaDiscount(ManaDiscountEvent event) {
		if (ElementalStones.hasAquaDiscount(event.getEntityPlayer())) {
			event.setDiscount(event.getDiscount() + MANA_DISCOUNT);
		}
	}

	@Override
	public boolean canEquip(ItemStack stack, LivingEntity entity) {
		return ElementalStones.canEquipStone(this, entity);
	}
}
