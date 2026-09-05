package io.github.lounode.extrabotany.common.util;

import com.google.common.collect.Multimap;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import javax.annotation.Nullable;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AttributeUtil {
	public static final DecimalFormat ATTRIBUTE_MODIFIER_FORMAT = Util.make(new DecimalFormat("#.##"), (format) -> {
		format.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
	});

	public static void addAttributeModifier(ItemStack stack, Holder<Attribute> attribute,
			AttributeModifier modifier, @Nullable EquipmentSlot slot) {
		ItemAttributeModifiers modifiers = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS,
				ItemAttributeModifiers.EMPTY);
		EquipmentSlotGroup slotGroup = slot == null ? EquipmentSlotGroup.ANY : EquipmentSlotGroup.bySlot(slot);
		stack.set(DataComponents.ATTRIBUTE_MODIFIERS,
				modifiers.withModifierAdded(attribute, modifier, slotGroup));
	}

	public static void removeAttributeModifier(ItemStack stack, ResourceLocation modifierId) {
		ItemAttributeModifiers modifiers = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
		if (modifiers == null) {
			return;
		}
		List<ItemAttributeModifiers.Entry> filtered = modifiers.modifiers().stream()
				.filter(entry -> !entry.modifier().is(modifierId))
				.toList();
		stack.set(DataComponents.ATTRIBUTE_MODIFIERS,
				new ItemAttributeModifiers(filtered, modifiers.showInTooltip()));
	}

	public static List<Component> getTooltips(Multimap<Holder<Attribute>, AttributeModifier> multimap) {
		List<Component> list = new ArrayList<>();
		for (Map.Entry<Holder<Attribute>, AttributeModifier> entry : multimap.entries()) {
			AttributeModifier attributemodifier = entry.getValue();
			double d0 = attributemodifier.amount();

			double d1;
			if (attributemodifier.operation() == AttributeModifier.Operation.ADD_VALUE) {
				if (entry.getKey().equals(Attributes.KNOCKBACK_RESISTANCE)) {
					d1 = d0 * 10.0D;
				} else {
					d1 = d0;
				}
			} else {
				d1 = d0 * 100.0D;
			}

			if (d0 > 0.0D) {
				list.add(Component.translatable("attribute.modifier.plus." + attributemodifier.operation().id(), ATTRIBUTE_MODIFIER_FORMAT.format(d1), Component.translatable(entry.getKey().value().getDescriptionId())).withStyle(ChatFormatting.BLUE));
			} else if (d0 < 0.0D) {
				d1 *= -1.0D;
				list.add(Component.translatable("attribute.modifier.take." + attributemodifier.operation().id(), ATTRIBUTE_MODIFIER_FORMAT.format(d1), Component.translatable(entry.getKey().value().getDescriptionId())).withStyle(ChatFormatting.RED));
			}
		}

		return list;
	}
}
