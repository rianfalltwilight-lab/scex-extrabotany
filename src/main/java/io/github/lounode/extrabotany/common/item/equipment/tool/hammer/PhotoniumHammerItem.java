package io.github.lounode.extrabotany.common.item.equipment.tool.hammer;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;

import io.github.lounode.extrabotany.api.item.IPhotonium;

/**
 * 光子锤。它有光子材料的增益，见 {@code IPhotonium}。
 * Photonium Hammer. It has the buff of the photonium material, see {@code IPhotonium}.
 */
public class PhotoniumHammerItem extends ManasteelHammerItem implements IPhotonium {

	public PhotoniumHammerItem(Tier tier, int attackDamageModifier, float attackSpeedModifier, Properties properties) {
		super(tier, attackDamageModifier, attackSpeedModifier, properties);
	}

	@Override
	public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
		super.inventoryTick(stack, world, entity, slot, selected);
		photoniumTick(stack, world, entity, slot, selected);
	}
}
