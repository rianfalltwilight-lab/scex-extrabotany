package io.github.lounode.extrabotany.common.item.equipment.armor.goblin_slayer;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.lib.ExtraBotanyTags;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

public class GoblinSlayerHelmetItem extends GoblinSlayerArmorItem {

	private static final float UNDEAD_DAMAGE_BONUS = 0.5F;

	public GoblinSlayerHelmetItem(Properties properties) {
		super(Type.HELMET, properties);
	}

	public static class EventHandler {

		public static void onPlayerAttack(LivingIncomingDamageEvent event) {
			if (!(event.getSource().getEntity() instanceof Player player)) {
				return;
			}
			ItemStack armorStack = player.getItemBySlot(EquipmentSlot.HEAD);
			if (!armorStack.is(ExtraBotanyItems.goblinSlayerHelmet)) {
				return;
			}
			if (!(armorStack.getItem() instanceof GoblinSlayerArmorItem suit)) {
				return;
			}
			if (!suit.hasArmorSet(player)) {
				return;
			}

			if (event.getEntity().getType().is(EntityTypeTags.SENSITIVE_TO_SMITE)) {
				float origin = event.getAmount();
				event.setAmount(origin * (1 + UNDEAD_DAMAGE_BONUS));
			}

			if (event.getEntity().getType().is(ExtraBotanyTags.Entities.GOBLINS)) {
				event.setAmount(Integer.MAX_VALUE);
			}
		}
	}
}
