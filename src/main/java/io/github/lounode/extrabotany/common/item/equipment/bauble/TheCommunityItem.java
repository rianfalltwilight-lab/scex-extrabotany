package io.github.lounode.extrabotany.common.item.equipment.bauble;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

import vazkii.botania.common.item.equipment.bauble.BaubleItem;

/**
 * 四元归一石。它合并了四种符石的全部效果。
 * The Community. The bauble has all the effects of the four elemental stones.
 *
 * <p>水之符石的魔力折扣在 {@link AquaStoneItem#onManaDiscount} 中处理。
 * <p>{@link AquaStoneItem#onManaDiscount} applies the mana discount of the Aqua Stone.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code ItemTheCommunity}。
 * <p>Source: ExtraMeteorP 1.16 {@code ItemTheCommunity}.
 */
public class TheCommunityItem extends BaubleItem {

	public TheCommunityItem(Properties props) {
		super(props);
	}

	@Override
	public Multimap<Holder<Attribute>, AttributeModifier> getEquippedAttributeModifiers(ItemStack stack, ResourceLocation slotId) {
		Multimap<Holder<Attribute>, AttributeModifier> attributes = HashMultimap.create();
		EarthStoneItem.addArmorModifier(attributes, slotId);
		IgnisStoneItem.addDamageModifier(attributes, slotId);
		AeroStoneItem.addSpeedModifiers(attributes, slotId);
		return attributes;
	}

	@Override
	public boolean canEquip(ItemStack stack, LivingEntity entity) {
		return ElementalStones.canEquipCommunity(entity);
	}
}
