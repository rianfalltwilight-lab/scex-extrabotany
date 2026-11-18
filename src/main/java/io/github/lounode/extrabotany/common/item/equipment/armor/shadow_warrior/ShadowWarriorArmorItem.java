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
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;

import io.github.lounode.extrabotany.api.ExtraBotanyAPI;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.item.equipment.armor.miku.MikuArmorItem;
import io.github.lounode.extrabotany.common.lib.ResourcesLib;

import java.util.List;
import java.util.function.Supplier;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 暗影武士套装的盔甲。穿着整套时：夜晚每一件增加 25% 基础生命上限、12.5% 基础攻击速度和 5% 基础攻击伤害，并给夜视；魔力消耗减少（每件 15%）。
 * The armor of the Shadow Warrior set. With the full set: at night each piece adds 25% to the base maximum health, 12.5% to the base attack speed and 5% to the base attack damage, and gives Night Vision; mana costs are lower (15% for each piece).
 *
 * <p>来源：ExtraMeteorP 1.16 的 {@code ItemShadowWarriorArmor}（继承 {@code ItemMikuArmor}）。重燃的每件固定 +5 生命、小精灵几率、暗影材料的夜视增益和头盔的摔落、爆炸免疫已经去掉。
 * <p>Source: {@code ItemShadowWarriorArmor} of ExtraMeteorP 1.16 (it extends {@code ItemMikuArmor}). The fixed +5 health for each piece, the pixie chance, the shadowium Night Vision buff and the fall and explosion immunity of the helmet, all from Reburn, are removed.
 */
public class ShadowWarriorArmorItem extends MikuArmorItem {
	public static final String TAG_NIGHT = "isnight";
	public static final double HEALTH_BONUS = 0.25;
	public static final double ATTACK_SPEED_BONUS = 0.125;
	public static final double ATTACK_DAMAGE_BONUS = 0.05;

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
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
		super.inventoryTick(stack, level, entity, slot, selected);
		if (!level.isClientSide() && entity instanceof Player player && isWorn(player, stack)) {
			boolean night = hasArmorSet(player) && !level.isDay();
			setFlag(stack, TAG_NIGHT, night);
			if (night) {
				player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 1));
			}
		}
	}

	public static boolean isNightBonusActive(ItemStack stack) {
		return getFlag(stack, TAG_NIGHT);
	}

	@Override
	public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
		ItemAttributeModifiers modifiers = super.getDefaultAttributeModifiers(stack);
		if (!isNightBonusActive(stack)) {
			return modifiers;
		}
		var id = prefix("shadow_warrior_night_" + getType().getName());
		var group = EquipmentSlotGroup.bySlot(getType().getSlot());
		return modifiers
				.withModifierAdded(Attributes.MAX_HEALTH,
						new AttributeModifier(id, HEALTH_BONUS, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), group)
				.withModifierAdded(Attributes.ATTACK_SPEED,
						new AttributeModifier(id, ATTACK_SPEED_BONUS, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), group)
				.withModifierAdded(Attributes.ATTACK_DAMAGE,
						new AttributeModifier(id, ATTACK_DAMAGE_BONUS, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), group);
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
		list.add(Component.translatable("extrabotany.armorset.shadow_warrior.desc1")
				.withStyle(hasArmorSet ? ChatFormatting.DARK_GRAY : ChatFormatting.GRAY));
	}

	@Override
	public boolean hasArmorSetItem(Player player, EquipmentSlot slot) {
		if (player == null) {
			return false;
		}
		ItemStack stack = player.getItemBySlot(slot);
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
