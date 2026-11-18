package io.github.lounode.extrabotany.common.item.equipment.armor.pleiades_combat_maid;

import com.google.common.base.Suppliers;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;



import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 血染昴宿星团战斗女仆服。夜晚穿着整套时：这件的生命加成从 5 变成 15，并增加 30% 基础移动速度、100% 基础攻击伤害、20% 基础攻击速度，给夜视。其余和普通女仆服相同。
 * Sanguine Pleiades Combat Maid Suit. At night with the full set: the health bonus of this piece becomes 15 instead of 5, and it adds 30% to the base movement speed, 100% to the base attack damage and 20% to the base attack speed, and gives Night Vision. The rest is the same as the usual maid suit.
 *
 * <p>来源：ExtraMeteorP 1.12 的 {@code ItemCombatMaidChestDarkened} 和 {@code ItemCombatMaidArmor} 的夜晚加成（这件只有 1.12 有）。重燃的力量 II、30% 吸血和常驻 +15 生命已经去掉。
 * <p>Source: the night bonus of {@code ItemCombatMaidChestDarkened} and {@code ItemCombatMaidArmor} of ExtraMeteorP 1.12 (only 1.12 has this piece). The Strength II, the 30% life steal and the permanent +15 health of Reburn are removed.
 *
 * <p>获得方法：1.12 ModRuneRecipe.recipeDarkened，符文祭坛 50000 魔力，普通女仆服和四件暗影战士盔甲（对照原版审计 E5）。重燃的嗜血击杀计数已经去掉。
 * <p>How to get it: recipeDarkened of ModRuneRecipe of 1.12, runic altar 50000 mana, the usual maid suit and the four Shadow Warrior pieces (parity audit E5). The Bloodthirst kill count of Reburn is removed.
 */
public class SanguinePleiadesCombatMaidSuitItem extends PleiadesCombatMaidSuitItem {
	public static final Supplier<ItemStack[]> ARMOR_SET = Suppliers.memoize(() -> new ItemStack[] {
			new ItemStack(ExtraBotanyItems.pleiadesCombatMaidHeadgear),
			new ItemStack(ExtraBotanyItems.sanguinePleiadesCombatMaidSuit),
			new ItemStack(ExtraBotanyItems.pleiadesCombatMaidSkirt),
			new ItemStack(ExtraBotanyItems.pleiadesCombatMaidBoots)
	});
	public static final String TAG_NIGHT = "isnight";

	public SanguinePleiadesCombatMaidSuitItem(Properties properties) {
		super(properties);
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
		super.inventoryTick(stack, level, entity, slot, selected);
		if (!level.isClientSide() && entity instanceof Player player && isWorn(player, stack)) {
			boolean night = hasArmorSet(player) && !level.isDay();
			setFlag(stack, TAG_NIGHT, night);
			if (night) {
				player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 1));
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
		var id = prefix("sanguine_pleiades_combat_maid_night");
		var group = EquipmentSlotGroup.bySlot(getType().getSlot());
		return modifiers
				.withModifierAdded(Attributes.MAX_HEALTH,
						new AttributeModifier(id, 10, AttributeModifier.Operation.ADD_VALUE), group)
				.withModifierAdded(Attributes.MOVEMENT_SPEED,
						new AttributeModifier(id, 0.3, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), group)
				.withModifierAdded(Attributes.ATTACK_DAMAGE,
						new AttributeModifier(id, 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), group)
				.withModifierAdded(Attributes.ATTACK_SPEED,
						new AttributeModifier(id, 0.2, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), group);
	}

	@Override
	public ItemStack[] getArmorSetStacks() {
		return ARMOR_SET.get();
	}

	public static boolean isSenketsu(ItemStack stack) {
		String name = stack.getHoverName().getString().toLowerCase(Locale.ROOT).trim();
		return "senketsu".equals(name);
	}
}
