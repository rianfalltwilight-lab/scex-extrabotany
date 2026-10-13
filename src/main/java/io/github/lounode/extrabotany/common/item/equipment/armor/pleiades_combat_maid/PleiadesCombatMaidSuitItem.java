package io.github.lounode.extrabotany.common.item.equipment.armor.pleiades_combat_maid;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.mana.ManaDiscountArmor;
import vazkii.botania.api.mana.ManaItemHandler;

import io.github.lounode.extrabotany.common.item.equipment.bauble.NatureOrbItem;
import io.github.lounode.extrabotany.common.lib.ExtraBotanyTags;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * 昴宿星团战斗女仆服。穿着整套时：魔力消耗减少 30%，每 tick 产生 1 点魔力，缓慢恢复生命值，每 2 秒清除负面效果，空手攻击增加 10 点伤害，攻击时按伤害恢复生命值，并且免疫带有 {@code maid_protection} 标签的伤害。
 * Pleiades Combat Maid Suit. With the full set: the mana cost decreases by 30%, it makes 1 mana on each tick, it heals slowly, it removes harmful effects one time for each 2 seconds, an attack with an empty hand does 10 more damage, an attack heals in proportion to the damage, and damage with the tag {@code maid_protection} has no effect.
 */
public class PleiadesCombatMaidSuitItem extends PleiadesCombatMaidArmorItem implements ManaDiscountArmor {
	private static final int COMMON_COST = 75;
	private static final float EMPTY_HAND_BONUS = 10.0F;

	public PleiadesCombatMaidSuitItem(Properties properties) {
		super(Type.CHESTPLATE, properties);
	}

	@Override
	public float getDiscount(ItemStack stack, int slot, Player player, @Nullable ItemStack tool) {
		return hasArmorSet(player) ? 0.3F : 0;
	}

	@Override
	public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
		super.inventoryTick(stack, world, entity, slot, selected);
		if (world.isClientSide() || !(entity instanceof Player player)) {
			return;
		}
		if (player.getItemBySlot(EquipmentSlot.CHEST) != stack) {
			return;
		}
		if (!hasArmorSet(player)) {
			return;
		}

		if (player.tickCount % 80 == 0 && ManaItemHandler.instance().requestManaExactForTool(stack, player, COMMON_COST, true)) {
			player.heal(1F);
		}

		if (player.tickCount % 40 == 0) {
			NatureOrbItem.clearHarmfulPotion(player);
		}

		ManaItemHandler.instance().dispatchManaExact(stack, player, 1, true);
	}

	public float getEmptyHandBonus() {
		return EMPTY_HAND_BONUS;
	}

	public static class EventHandler {

		public static void onEntityAttacked(LivingIncomingDamageEvent event) {
			Entity attacker = event.getSource().getEntity();
			LivingEntity target = event.getEntity();

			if (!(attacker instanceof Player player)) {
				return;
			}
			if (target == attacker) {
				return;
			}

			ItemStack suitStack = player.getItemBySlot(EquipmentSlot.CHEST);
			if (!(suitStack.getItem() instanceof PleiadesCombatMaidSuitItem suit)) {
				return;
			}
			if (!suit.hasArmorSet(player)) {
				return;
			}

			if (player.getMainHandItem().isEmpty() &&
					ManaItemHandler.instance().requestManaExactForTool(suitStack, player, COMMON_COST, true)) {
				event.setAmount(event.getAmount() + suit.getEmptyHandBonus());
			}

			if (player.isHurt() &&
					ManaItemHandler.instance().requestManaExactForTool(suitStack, player, COMMON_COST, true)) {
				player.heal(event.getAmount() / 8.0F);
			}
		}

		public static void onPlayerAttacked(LivingIncomingDamageEvent event) {
			Entity target = event.getEntity();
			if (!(target instanceof Player player)) {
				return;
			}
			ItemStack suitStack = player.getItemBySlot(EquipmentSlot.CHEST);
			if (!(suitStack.getItem() instanceof PleiadesCombatMaidSuitItem suit)) {
				return;
			}
			if (!suit.hasArmorSet(player)) {
				return;
			}

			if (event.getSource().is(ExtraBotanyTags.DamageTypes.MAID_PROTECTION)) {
				event.setAmount(0);
				event.setCanceled(true);
			}
		}
	}
}
