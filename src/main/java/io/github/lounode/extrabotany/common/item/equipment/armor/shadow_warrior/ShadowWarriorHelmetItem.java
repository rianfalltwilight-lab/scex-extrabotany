package io.github.lounode.extrabotany.common.item.equipment.armor.shadow_warrior;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import io.github.lounode.extrabotany.common.lib.ExtraBotanyTags;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * 暗影武士头盔。穿着整套时，带有 {@code shadow_warrior_protection} 标签的伤害对穿着者无效。
 * Shadow Warrior Helmet. With the full set, damage with the tag {@code shadow_warrior_protection} has no effect on the wearer.
 */
public class ShadowWarriorHelmetItem extends ShadowWarriorArmorItem {

	public ShadowWarriorHelmetItem(Properties properties) {
		super(Type.HELMET, properties);
	}

	@Override
	public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
		super.inventoryTick(stack, world, entity, slot, selected);
	}

	public static class EventHandler {
		public static void onPlayerAttacked(LivingIncomingDamageEvent event) {
			Entity target = event.getEntity();
			if (!(target instanceof Player player)) {
				return;
			}
			ItemStack armorStack = player.getItemBySlot(EquipmentSlot.HEAD);
			if (!(armorStack.getItem() instanceof ShadowWarriorHelmetItem suit)) {
				return;
			}
			if (!suit.hasArmorSet(player)) {
				return;
			}

			if (event.getSource().is(ExtraBotanyTags.DamageTypes.SHADOW_WARRIOR_PROTECTION)) {
				event.setAmount(0);
			}
		}
	}
}
