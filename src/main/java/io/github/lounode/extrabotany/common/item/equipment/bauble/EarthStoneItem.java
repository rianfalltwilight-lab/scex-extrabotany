package io.github.lounode.extrabotany.common.item.equipment.bauble;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

import vazkii.botania.common.item.equipment.bauble.BaubleItem;

/**
 * 地之符石。佩戴后，护甲值增加 4 点。
 * Earth Stone. The bauble adds 4 armor points.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code ItemEarthStone}。
 * <p>Source: ExtraMeteorP 1.16 {@code ItemEarthStone}.
 */
public class EarthStoneItem extends BaubleItem {
	public static final double ARMOR_BONUS = 4;

	public EarthStoneItem(Properties props) {
		super(props);
	}

	@Override
	public Multimap<Holder<Attribute>, AttributeModifier> getEquippedAttributeModifiers(ItemStack stack, ResourceLocation slotId) {
		Multimap<Holder<Attribute>, AttributeModifier> attributes = HashMultimap.create();
		addArmorModifier(attributes, slotId);
		return attributes;
	}

	/**
	 * 把地之符石的属性加到表里。四元归一石也使用这个方法。
	 * Adds the attribute of the Earth Stone to the map. The Community also uses this method.
	 */
	static void addArmorModifier(Multimap<Holder<Attribute>, AttributeModifier> attributes, ResourceLocation slotId) {
		attributes.put(Attributes.ARMOR,
				new AttributeModifier(slotId, ARMOR_BONUS, AttributeModifier.Operation.ADD_VALUE));
	}

	@Override
	public boolean canEquip(ItemStack stack, LivingEntity entity) {
		return ElementalStones.canEquipStone(this, entity);
	}
}
