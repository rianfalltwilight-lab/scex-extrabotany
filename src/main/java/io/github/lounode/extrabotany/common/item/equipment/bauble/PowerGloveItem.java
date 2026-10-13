package io.github.lounode.extrabotany.common.item.equipment.bauble;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import vazkii.botania.common.handler.EquipmentHandler;
import vazkii.botania.common.item.equipment.bauble.BaubleItem;

import java.util.List;

/**
 * 强力手套。攻击速度提高 12%。按住攻击键时，冷却结束后会自动再次攻击。
 * Power Glove. The bauble increases attack speed by 12%. While the player holds the attack key, the next attack starts automatically when the cooldown ends.
 *
 * <p>自动攻击在客户端完成。见 {@code PowerGloveInputMixin}。
 * <p>The client does the automatic attack. Refer to {@code PowerGloveInputMixin}.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code ItemPowerGlove} 和 {@code KeyInputHandler}。
 * <p>Source: ExtraMeteorP 1.16 {@code ItemPowerGlove} and {@code KeyInputHandler}.
 */
public class PowerGloveItem extends BaubleItem {
	public static final double ATTACK_SPEED_BONUS = 0.12;

	public PowerGloveItem(Properties props) {
		super(props);
	}

	@Override
	public Multimap<Holder<Attribute>, AttributeModifier> getEquippedAttributeModifiers(ItemStack stack, ResourceLocation slotId) {
		Multimap<Holder<Attribute>, AttributeModifier> attributes = HashMultimap.create();
		attributes.put(Attributes.ATTACK_SPEED,
				new AttributeModifier(slotId, ATTACK_SPEED_BONUS, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
		return attributes;
	}

	@Override
	public boolean canEquip(ItemStack stack, LivingEntity entity) {
		return EquipmentHandler.findOrEmpty(this, entity).isEmpty();
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flags) {
		super.appendHoverText(stack, context, tooltip, flags);
		tooltip.add(Component.translatable("tooltip.extrabotany.power_glove").withStyle(ChatFormatting.GRAY));
	}
}
