package io.github.lounode.extrabotany.common.item.equipment.armor.goblin_slayer;

import com.google.common.base.Suppliers;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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

import io.github.lounode.extrabotany.api.ExtraBotanyAPI;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.item.equipment.armor.miku.MikuArmorItem;
import io.github.lounode.extrabotany.common.lib.ResourcesLib;

import java.util.List;
import java.util.function.Supplier;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 哥布林猎人套装的盔甲。穿着整套时：白天每一件增加 50% 基础生命上限和 4% 基础移动速度；魔力消耗减少（每件 15%）；猪灵保持中立。
 * The armor of the Goblin Slayer set. With the full set: in the day each piece adds 50% to the base maximum health and 4% to the base movement speed; mana costs are lower (15% for each piece); piglins stay neutral.
 *
 * <p>来源：ExtraMeteorP 1.16 的 {@code ItemGoblinSlayerArmor}（继承 {@code ItemMikuArmor}：折扣 15%，每点耐久 70 魔力）。重燃的光子速度 II 和头盔对亡灵、哥布林的加伤已经去掉。
 * <p>Source: {@code ItemGoblinSlayerArmor} of ExtraMeteorP 1.16 (it extends {@code ItemMikuArmor}: a discount of 15%, 70 mana for each point of durability). The photonium Speed II and the helmet damage bonus against undead and goblins of Reburn are removed.
 */
public class GoblinSlayerArmorItem extends MikuArmorItem {
	public static final String TAG_DAY = "isday";
	public static final double HEALTH_BONUS = 0.5;
	public static final double SPEED_BONUS = 0.04;

	public static final Supplier<ItemStack[]> ARMOR_SET = Suppliers.memoize(() -> new ItemStack[] {
			new ItemStack(ExtraBotanyItems.goblinSlayerHelmet),
			new ItemStack(ExtraBotanyItems.goblinSlayerChestplate),
			new ItemStack(ExtraBotanyItems.goblinSlayerLeggings),
			new ItemStack(ExtraBotanyItems.goblinSlayerBoots)
	});

	public GoblinSlayerArmorItem(Type type, Properties properties) {
		this(ExtraBotanyAPI.instance().getGoblinSlayerArmorMaterial(), type, properties);
	}

	public GoblinSlayerArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties) {
		super(material, type, properties);
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
		super.inventoryTick(stack, level, entity, slot, selected);
		if (!level.isClientSide() && entity instanceof Player player && isWorn(player, stack)) {
			setFlag(stack, TAG_DAY, hasArmorSet(player) && level.isDay());
		}
	}

	public static boolean isDayBonusActive(ItemStack stack) {
		return getFlag(stack, TAG_DAY);
	}

	@Override
	public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
		ItemAttributeModifiers modifiers = super.getDefaultAttributeModifiers(stack);
		if (!isDayBonusActive(stack)) {
			return modifiers;
		}
		var id = prefix("goblin_slayer_day_" + getType().getName());
		var group = EquipmentSlotGroup.bySlot(getType().getSlot());
		return modifiers
				.withModifierAdded(Attributes.MAX_HEALTH,
						new AttributeModifier(id, HEALTH_BONUS, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), group)
				.withModifierAdded(Attributes.MOVEMENT_SPEED,
						new AttributeModifier(id, SPEED_BONUS, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), group);
	}

	@Override
	public boolean makesPiglinsNeutral(ItemStack stack, LivingEntity wearer) {
		return true;
	}

	@Override
	public String getArmorTextureAfterInk(ItemStack stack, EquipmentSlot slot) {
		return ResourcesLib.MODEL_GOBLIN_SLAYER;
	}

	@Override
	public MutableComponent getArmorSetName() {
		return Component.translatable("extrabotany.armorset.goblin_slayer.name");
	}

	@Override
	public void addArmorSetDescription(ItemStack stack, List<Component> list, boolean hasArmorSet) {
		list.add(Component.translatable("extrabotany.armorset.goblin_slayer.desc0")
				.withStyle(hasArmorSet ? ChatFormatting.DARK_RED : ChatFormatting.GRAY));
		list.add(Component.translatable("extrabotany.armorset.goblin_slayer.desc1")
				.withStyle(hasArmorSet ? ChatFormatting.DARK_RED : ChatFormatting.GRAY));
	}

	@Override
	public boolean hasArmorSetItem(Player player, EquipmentSlot slot) {
		if (player == null) {
			return false;
		}
		ItemStack stack = player.getItemBySlot(slot);
		return switch (slot) {
			case HEAD -> stack.is(ExtraBotanyItems.goblinSlayerHelmet);
			case CHEST -> stack.is(ExtraBotanyItems.goblinSlayerChestplate);
			case LEGS -> stack.is(ExtraBotanyItems.goblinSlayerLeggings);
			case FEET -> stack.is(ExtraBotanyItems.goblinSlayerBoots);
			default -> false;
		};
	}

	@Override
	public ItemStack[] getArmorSetStacks() {
		return ARMOR_SET.get();
	}
}
