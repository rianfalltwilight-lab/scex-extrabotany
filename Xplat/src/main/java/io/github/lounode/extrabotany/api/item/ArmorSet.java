package io.github.lounode.extrabotany.api.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import io.github.lounode.extrabotany.common.advancements.HasArmorSetTrigger;
import io.github.lounode.extrabotany.common.proxy.Proxy;

import java.util.List;

public interface ArmorSet {

	ItemStack[] getArmorSetStacks();

	default boolean hasArmorSet(Player player) {
		return hasArmorSetItem(player, EquipmentSlot.HEAD) && hasArmorSetItem(player, EquipmentSlot.CHEST) && hasArmorSetItem(player, EquipmentSlot.LEGS) && hasArmorSetItem(player, EquipmentSlot.FEET);
	}

	default void triggerAdvancement(Entity entity) {
		if (entity instanceof ServerPlayer serverPlayer) {
			HasArmorSetTrigger.INSTANCE.trigger(serverPlayer);
		}
	}

	boolean hasArmorSetItem(Player player, EquipmentSlot slot);
	MutableComponent getArmorSetName();

	private int getSetPiecesEquipped(Player player) {
		int pieces = 0;
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR && hasArmorSetItem(player, slot)) {
				pieces++;
			}
		}

		return pieces;
	}

	private Component getArmorSetTitle(Player player) {
		Component end = getArmorSetName()
				.append(" (" + getSetPiecesEquipped(player) + "/" + getArmorSetStacks().length + ")")
				.withStyle(ChatFormatting.GRAY);
		return Component.translatable("botaniamisc.armorset")
				.append(" ")
				.append(end);
	}

	default void addInformation(ItemStack stack, Level world, List<Component> list, TooltipFlag flags) {
		Player player = Proxy.INSTANCE.getClientPlayer();
		if (player == null) {
			return;
		}

		list.add(getArmorSetTitle(player));
		addArmorSetDescription(stack, list, hasArmorSet(player));
		ItemStack[] stacks = getArmorSetStacks();
		for (ItemStack armor : stacks) {
			MutableComponent cmp = Component.literal(" - ").append(armor.getHoverName());
			EquipmentSlot slot = ((ArmorItem) armor.getItem()).getEquipmentSlot();
			cmp.withStyle(hasArmorSetItem(player, slot) ? ChatFormatting.GREEN : ChatFormatting.GRAY);
			list.add(cmp);
		}
	}

	void addArmorSetDescription(ItemStack stack, List<Component> list, boolean hasArmorSet);
}
