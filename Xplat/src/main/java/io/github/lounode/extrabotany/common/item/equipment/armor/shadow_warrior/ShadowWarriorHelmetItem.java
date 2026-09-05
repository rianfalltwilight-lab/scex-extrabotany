package io.github.lounode.extrabotany.common.item.equipment.armor.shadow_warrior;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import io.github.lounode.extrabotany.common.lib.ExtraBotanyTags;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

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
