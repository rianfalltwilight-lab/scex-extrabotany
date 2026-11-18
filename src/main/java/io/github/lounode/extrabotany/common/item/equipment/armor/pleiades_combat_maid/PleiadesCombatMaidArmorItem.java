package io.github.lounode.extrabotany.common.item.equipment.armor.pleiades_combat_maid;

import com.google.common.base.Suppliers;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.client.gui.TooltipHandler;

import io.github.lounode.extrabotany.api.ExtraBotanyAPI;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.item.equipment.armor.miku.MikuArmorItem;
import io.github.lounode.extrabotany.common.item.equipment.bauble.NatureOrbItem;
import io.github.lounode.extrabotany.common.lib.ExtraBotanyTags;
import io.github.lounode.extrabotany.common.lib.ResourcesLib;
import io.github.lounode.extrabotany.xplat.ExtraBotanyConfig;

import java.util.List;
import java.util.function.Supplier;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 昴宿星团战斗女仆套装的盔甲。每一件增加 5 点最大生命值。穿着整套时：魔力消耗减少（每件 15%）；每 tick 给物品栏送 1 点魔力；每 2 秒用 20 魔力回 1 点生命，并用每个 100 魔力清除能被牛奶清除的负面效果；空手攻击用 200 魔力多造成 8 点伤害；攻击时用 80 魔力恢复伤害的十分之一；不受 {@code maid_protection} 标签的环境伤害。
 * The armor of the Pleiades Combat Maid set. Each piece adds 5 maximum health. With the full set: mana costs are lower (15% for each piece); it sends 1 mana to the inventory on each tick; each 2 seconds it heals 1 for 20 mana and removes the harmful effects that milk can remove for 100 mana each; an attack with empty hands does 8 more damage for 200 mana; an attack heals a tenth of the damage for 80 mana; the environmental damage of the tag {@code maid_protection} has no effect.
 *
 * <p>来源：ExtraMeteorP 1.16 的 {@code ItemMaidArmor} 和 {@code ItemMaidHelm}（继承 {@code ItemMikuArmor}）。重燃的 30% 折扣、空手 +10、每 80 tick 75 魔力回血和更宽的免疫列表已经去掉。
 * <p>Source: {@code ItemMaidArmor} and {@code ItemMaidHelm} of ExtraMeteorP 1.16 (they extend {@code ItemMikuArmor}). The 30% discount, the +10 with empty hands, the healing of 75 mana each 80 ticks and the wider immunity list of Reburn are removed.
 */
public class PleiadesCombatMaidArmorItem extends MikuArmorItem {
	public static final float HEALTH_PER_PIECE = 5;
	public static final int HEAL_COST = 20;
	public static final int CLEAR_COST = 100;
	public static final int EMPTY_HAND_COST = 200;
	public static final float EMPTY_HAND_BONUS = 8F;
	public static final int LIFE_STEAL_COST = 80;

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
		return super.getDefaultAttributeModifiers()
				.withModifierAdded(Attributes.MAX_HEALTH,
						new AttributeModifier(modifierId, HEALTH_PER_PIECE, AttributeModifier.Operation.ADD_VALUE), slot);
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
		super.inventoryTick(stack, level, entity, slot, selected);
		// 1.16 的套装效果写在头盔里。 / In 1.16 the set effects are in the helmet.
		if (level.isClientSide() || getType() != Type.HELMET || !(entity instanceof Player player)
				|| !isWorn(player, stack) || !hasArmorSet(player)) {
			return;
		}
		ManaItemHandler.instance().dispatchManaExact(stack, player, 1, true);
		if (player.tickCount % 40 == 0) {
			if (player.isHurt() && ManaItemHandler.instance().requestManaExactForTool(stack, player, HEAL_COST, true)) {
				player.heal(1F);
			}
			clearPotions(stack, player);
		}
	}

	private static void clearPotions(ItemStack stack, Player player) {
		List<net.minecraft.core.Holder<MobEffect>> effects = player.getActiveEffects().stream()
				.filter(NatureOrbItem::isMilkCurableHarmful)
				.map(MobEffectInstance::getEffect)
				.distinct()
				.toList();
		for (var effect : effects) {
			if (ManaItemHandler.instance().requestManaExactForTool(stack, player, CLEAR_COST, true)) {
				player.removeEffect(effect);
			}
		}
	}

	/** 穿着整套时返回头盔，否则返回空。 / Returns the helmet with the full set, otherwise empty. */
	public static ItemStack getFullSetHelmet(Player player) {
		ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
		if (helmet.getItem() instanceof PleiadesCombatMaidArmorItem maid && maid.hasArmorSet(player)) {
			return helmet;
		}
		return ItemStack.EMPTY;
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
		if (player == null) {
			return false;
		}
		ItemStack stack = player.getItemBySlot(slot);
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

	public static class EventHandler {
		/** 穿着整套的玩家打别的生物。 / A player with the full set hits another creature. */
		public static void onEntityAttacked(LivingIncomingDamageEvent event) {
			if (!(event.getSource().getDirectEntity() instanceof Player player) || event.getEntity() == player) {
				return;
			}
			ItemStack helmet = getFullSetHelmet(player);
			if (helmet.isEmpty()) {
				return;
			}
			if (player.getMainHandItem().isEmpty() && player.getOffhandItem().isEmpty()
					&& ManaItemHandler.instance().requestManaExactForTool(helmet, player, EMPTY_HAND_COST, true)) {
				event.setAmount(event.getAmount() + EMPTY_HAND_BONUS);
			}
			if (player.isHurt() && ManaItemHandler.instance().requestManaExactForTool(helmet, player, LIFE_STEAL_COST, true)) {
				player.heal(event.getAmount() / 10F);
			}
		}

		/** 穿着整套的玩家受到环境伤害。 / A player with the full set takes environmental damage. */
		public static void onPlayerAttacked(LivingIncomingDamageEvent event) {
			LivingEntity target = event.getEntity();
			if (target instanceof Player player && !getFullSetHelmet(player).isEmpty()
					&& event.getSource().is(ExtraBotanyTags.DamageTypes.MAID_PROTECTION)) {
				event.setCanceled(true);
			}
		}
	}
}
