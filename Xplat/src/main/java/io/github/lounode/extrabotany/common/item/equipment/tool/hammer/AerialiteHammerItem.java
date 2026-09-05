package io.github.lounode.extrabotany.common.item.equipment.tool.hammer;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;

import io.github.lounode.extrabotany.api.item.IAerialite;

public class AerialiteHammerItem extends ManasteelHammerItem implements IAerialite {

	private static final int MANA_PER_DAMAGE = 70;
	private static final int BUFF_MANA = 10;

	public AerialiteHammerItem(Tier tier, int attackDamageModifier, float attackSpeedModifier, Properties properties) {
		super(tier, attackDamageModifier, attackSpeedModifier, properties);
	}

	@Override
	public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
		super.inventoryTick(stack, world, entity, slot, selected);
		aerialiteTick(stack, world, entity, slot, selected);
	}

	@Override
	public int getManaPerDamage() {
		return MANA_PER_DAMAGE;
	}

	@Override
	public int getAerialiteBuffMana() {
		return BUFF_MANA;
	}
}
