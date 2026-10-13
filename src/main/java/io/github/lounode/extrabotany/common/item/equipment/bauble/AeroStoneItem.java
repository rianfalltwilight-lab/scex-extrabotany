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
 * 风之符石。佩戴后，移动速度和飞行速度提高 15%。
 * Aero Stone. The bauble increases movement speed and flight speed by 15%.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code ItemAeroStone}。
 * <p>Source: ExtraMeteorP 1.16 {@code ItemAeroStone}.
 */
public class AeroStoneItem extends BaubleItem {
	public static final double SPEED_BONUS = 0.15;

	public AeroStoneItem(Properties props) {
		super(props);
	}

	@Override
	public Multimap<Holder<Attribute>, AttributeModifier> getEquippedAttributeModifiers(ItemStack stack, ResourceLocation slotId) {
		Multimap<Holder<Attribute>, AttributeModifier> attributes = HashMultimap.create();
		addSpeedModifiers(attributes, slotId);
		return attributes;
	}

	/**
	 * 把风之符石的属性加到表里。四元归一石也使用这个方法。
	 * Adds the attributes of the Aero Stone to the map. The Community also uses this method.
	 */
	static void addSpeedModifiers(Multimap<Holder<Attribute>, AttributeModifier> attributes, ResourceLocation slotId) {
		attributes.put(Attributes.MOVEMENT_SPEED,
				new AttributeModifier(slotId, SPEED_BONUS, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
		// 玩家没有飞行速度属性。这一条只对有该属性的生物生效，和原版一致。
		// A player does not have the flying speed attribute. This modifier only applies to mobs that have it, as in the original.
		attributes.put(Attributes.FLYING_SPEED,
				new AttributeModifier(slotId, SPEED_BONUS, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
	}

	@Override
	public boolean canEquip(ItemStack stack, LivingEntity entity) {
		return ElementalStones.canEquipStone(this, entity);
	}
}
