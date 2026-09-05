package io.github.lounode.extrabotany.common.item.equipment.armor.pleiades_combat_maid;

import com.google.common.base.Suppliers;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.client.gui.TooltipHandler;

import io.github.lounode.extrabotany.api.ExtraBotanyAPI;
import io.github.lounode.extrabotany.client.lib.ResourcesLib;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.item.equipment.armor.starry_idol.StarryIdolArmorItem;
import io.github.lounode.extrabotany.xplat.ExtraBotanyConfig;

import java.util.List;
import java.util.function.Supplier;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class PleiadesCombatMaidArmorItem extends StarryIdolArmorItem {

	public static final Supplier<ItemStack[]> ARMOR_SET = Suppliers.memoize(() -> new ItemStack[] {
			new ItemStack(ExtraBotanyItems.pleiadesCombatMaidHeadgear),
			new ItemStack(ExtraBotanyItems.pleiadesCombatMaidSuit),
			new ItemStack(ExtraBotanyItems.pleiadesCombatMaidSkirt),
			new ItemStack(ExtraBotanyItems.pleiadesCombatMaidBoots)
	});

	public PleiadesCombatMaidArmorItem(Type type, Properties properties) {
		this(ExtraBotanyAPI.instance().getPleiadsMaidCombatArmorMaterial(), type, properties);
	}

	public PleiadesCombatMaidArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties) {
		super(material, type, properties);
	}

	@Override
	public ItemAttributeModifiers getDefaultAttributeModifiers() {
		var modifierId = prefix("pleiades_combat_maid_" + getType().getName());
		var slot = EquipmentSlotGroup.bySlot(getType().getSlot());
		int reduction = getMaterial().value().getDefense(getType());
		return super.getDefaultAttributeModifiers()
				.withModifierAdded(Attributes.KNOCKBACK_RESISTANCE,
						new AttributeModifier(modifierId, (double) reduction / 20,
								AttributeModifier.Operation.ADD_VALUE), slot)
				.withModifierAdded(Attributes.MAX_HEALTH,
						new AttributeModifier(modifierId, 5, AttributeModifier.Operation.ADD_VALUE), slot);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flags) {
		if (ExtraBotanyConfig.client().otakuMode()) {
			switch (getType()) {
				case HELMET -> list.add(Component.translatable("tooltip.extrabotany.pleiades_combat_maid_headgear").withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.ITALIC));
				case CHESTPLATE -> list.add(Component.translatable("tooltip.extrabotany.pleiades_combat_maid_suit").withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.ITALIC));
				case LEGGINGS -> list.add(Component.translatable("tooltip.extrabotany.pleiades_combat_maid_skirt").withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.ITALIC));
				case BOOTS -> list.add(Component.translatable("tooltip.extrabotany.pleiades_combat_maid_boots").withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.ITALIC));
			}
			list.add(Component.empty());
		}
		TooltipHandler.addOnShift(list, flags, () -> addInformation(stack, context.level(), list, flags));
	}

	@Override
	public String getArmorTextureAfterInk(ItemStack stack, EquipmentSlot slot) {
		return ResourcesLib.MODEL_PLEIADES_COMBAT_MAID;
	}

	@Override
	public MutableComponent getArmorSetName() {
		return Component.translatable("extrabotany.armorset.pleiades_combat_maid.name");
	}

	@Override
	public void addArmorSetDescription(ItemStack stack, List<Component> list, boolean hasArmorSet) {
		list.add(Component.translatable("extrabotany.armorset.pleiades_combat_maid.desc0")
				.withStyle(hasArmorSet ? ChatFormatting.YELLOW : ChatFormatting.GRAY));
		list.add(Component.translatable("extrabotany.armorset.pleiades_combat_maid.desc1")
				.withStyle(hasArmorSet ? ChatFormatting.RED : ChatFormatting.GRAY));
		list.add(Component.translatable("extrabotany.armorset.pleiades_combat_maid.desc2")
				.withStyle(hasArmorSet ? ChatFormatting.AQUA : ChatFormatting.GRAY));
	}

	@Override
	public boolean hasArmorSetItem(Player player, EquipmentSlot slot) {
		if (player == null || player.getInventory() == null || player.getInventory().armor == null) {
			return false;
		}

		ItemStack stack = player.getItemBySlot(slot);
		if (stack.isEmpty()) {
			return false;
		}

		return switch (slot) {
			case HEAD -> stack.is(ExtraBotanyItems.pleiadesCombatMaidHeadgear);
			case CHEST -> stack.is(ExtraBotanyItems.pleiadesCombatMaidSuit) || stack.is(ExtraBotanyItems.sanguinePleiadesCombatMaidSuit);
			case LEGS -> stack.is(ExtraBotanyItems.pleiadesCombatMaidSkirt);
			case FEET -> stack.is(ExtraBotanyItems.pleiadesCombatMaidBoots);
			default -> false;
		};
	}

	@Override
	public ItemStack[] getArmorSetStacks() {
		return ARMOR_SET.get();
	}
}
