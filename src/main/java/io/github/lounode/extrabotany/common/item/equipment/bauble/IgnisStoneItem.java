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
 * 火之符石。佩戴后，攻击伤害提高 10%。
 * Ignis Stone. The bauble increases attack damage by 10%.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code ItemIgnisStone}。
 * <p>Source: ExtraMeteorP 1.16 {@code ItemIgnisStone}.
 */
public class IgnisStoneItem extends BaubleItem {
	public static final double DAMAGE_BONUS = 0.10;

	public IgnisStoneItem(Properties props) {
		super(props);
	}

	@Override
	public Multimap<Holder<Attribute>, AttributeModifier> getEquippedAttributeModifiers(ItemStack stack, ResourceLocation slotId) {
		Multimap<Holder<Attribute>, AttributeModifier> attributes = HashMultimap.create();
		addDamageModifier(attributes, slotId);
		return attributes;
	}

	/**
	 * 把火之符石的属性加到表里。四元归一石也使用这个方法。
	 * Adds the attribute of the Ignis Stone to the map. The Community also uses this method.
	 */
	static void addDamageModifier(Multimap<Holder<Attribute>, AttributeModifier> attributes, ResourceLocation slotId) {
		attributes.put(Attributes.ATTACK_DAMAGE,
				new AttributeModifier(slotId, DAMAGE_BONUS, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
	}

	@Override
	public boolean canEquip(ItemStack stack, LivingEntity entity) {
		return ElementalStones.canEquipStone(this, entity);
	}
}
