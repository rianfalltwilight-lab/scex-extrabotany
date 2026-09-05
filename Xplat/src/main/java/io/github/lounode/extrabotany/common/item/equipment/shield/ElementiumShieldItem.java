package io.github.lounode.extrabotany.common.item.equipment.shield;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.common.handler.PixieHandler;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class ElementiumShieldItem extends ManasteelShieldItem {

	private static final int MANA_PER_DAMAGE = 70;

	public ElementiumShieldItem(Properties properties, Tier tier) {
		super(properties, tier);
	}

	public ElementiumShieldItem(Properties properties) {
		this(properties, BotaniaAPI.instance().getElementiumItemTier());
	}

	public static void onModifyAttributes(ItemAttributeModifierEvent event) {
		if (!(event.getItemStack().getItem() instanceof ElementiumShieldItem)) {
			return;
		}
		event.addModifier(PixieHandler.PIXIE_SPAWN_CHANCE,
				PixieHandler.makeModifier(prefix("elementium_shield_pixie_chance"), 0.2F),
				EquipmentSlotGroup.OFFHAND);
	}

	@Override
	public void onShieldBlock(ItemStack stack, LivingEntity blocker, DamageSource source, float damage) {
		super.onShieldBlock(stack, blocker, source, damage);
		Entity entity = source.getEntity();
		if (entity != null && !entity.fireImmune()) {
			entity.igniteForSeconds(5);
		}
	}

	@Override
	public int getManaPerDamage() {
		return MANA_PER_DAMAGE;
	}
}
