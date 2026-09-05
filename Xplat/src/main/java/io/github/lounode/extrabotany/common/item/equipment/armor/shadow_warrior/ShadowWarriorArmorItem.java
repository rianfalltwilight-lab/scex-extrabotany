package io.github.lounode.extrabotany.common.item.equipment.armor.shadow_warrior;

import com.google.common.base.Suppliers;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;

import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.handler.PixieHandler;

import io.github.lounode.extrabotany.api.ExtraBotanyAPI;
import io.github.lounode.extrabotany.api.item.IShadowium;
import io.github.lounode.extrabotany.client.lib.ResourcesLib;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.item.equipment.armor.starry_idol.StarryIdolArmorItem;

import java.util.List;
import java.util.function.Supplier;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class ShadowWarriorArmorItem extends StarryIdolArmorItem implements IShadowium {

	public static final Supplier<ItemStack[]> ARMOR_SET = Suppliers.memoize(() -> new ItemStack[] {
			new ItemStack(ExtraBotanyItems.shadowWarriorHelmet),
			new ItemStack(ExtraBotanyItems.shadowWarriorChestplate),
			new ItemStack(ExtraBotanyItems.shadowWarriorLeggings),
			new ItemStack(ExtraBotanyItems.shadowWarriorBoots)
	});

	public ShadowWarriorArmorItem(Type type, Properties properties) {
		this(ExtraBotanyAPI.instance().getShadowWarriorArmorMaterial(), type, properties);
	}

	public ShadowWarriorArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties) {
		super(material, type, properties);
	}

	@Override
	public ItemAttributeModifiers getDefaultAttributeModifiers() {
		var modifierId = prefix("shadow_warrior_" + getType().getName());
		var slot = EquipmentSlotGroup.bySlot(getType().getSlot());
		return super.getDefaultAttributeModifiers()
				.withModifierAdded(Attributes.MAX_HEALTH,
						new AttributeModifier(modifierId, 5, AttributeModifier.Operation.ADD_VALUE), slot)
				.withModifierAdded(PixieHandler.PIXIE_SPAWN_CHANCE,
						PixieHandler.makeModifier(modifierId, 0.05), slot);
	}

	@Override
	public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
		super.inventoryTick(stack, world, entity, slot, selected);
		if (entity instanceof Player) {
			shadowiumTick(stack, world, entity, slot, selected);
		}
	}

	@Override
	public void shadowiumTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
		if (!(entity instanceof LivingEntity living)) {
			return;
		}
		if ((living.getItemBySlot(type.getSlot()) != stack)) {
			return;
		}
		if (living.tickCount % (5 * 20) == 0) {
			return;
		}

		if (living instanceof Player player &&
				!ManaItemHandler.instance().requestManaExactForTool(stack, player, getShadowiumBuffMana(), true)) {
			return;
		}

		if (isNight(level) || isInCave(level, entity.blockPosition())) {
			living.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 20 * 20));
		}
	}

	@Override
	public String getArmorTextureAfterInk(ItemStack stack, EquipmentSlot slot) {
		return ResourcesLib.MODEL_SHADOW_WARRIOR;
	}

	@Override
	public MutableComponent getArmorSetName() {
		return Component.translatable("extrabotany.armorset.shadow_warrior.name");
	}

	@Override
	public void addArmorSetDescription(ItemStack stack, List<Component> list, boolean hasArmorSet) {
		list.add(Component.translatable("extrabotany.armorset.shadow_warrior.desc0")
				.withStyle(hasArmorSet ? ChatFormatting.DARK_GRAY : ChatFormatting.GRAY));
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
			case HEAD -> stack.is(ExtraBotanyItems.shadowWarriorHelmet);
			case CHEST -> stack.is(ExtraBotanyItems.shadowWarriorChestplate);
			case LEGS -> stack.is(ExtraBotanyItems.shadowWarriorLeggings);
			case FEET -> stack.is(ExtraBotanyItems.shadowWarriorBoots);
			default -> false;
		};
	}

	@Override
	public ItemStack[] getArmorSetStacks() {
		return ARMOR_SET.get();
	}
}
