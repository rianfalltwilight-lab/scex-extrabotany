package io.github.lounode.extrabotany.common.item.equipment.tool.hammer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;

import io.github.lounode.extrabotany.api.item.IOrichalcos;

public class OrichalcosHammer extends TerrasteelHammerItem implements IOrichalcos {

	private static final int MANA_PER_DAMAGE = 200;
	private static final int ACTIVE_COST = 20;
	public static final int RANGE = 3;

	public OrichalcosHammer(Tier tier, int attackDamageModifier, float attackSpeedModifier, Properties properties) {
		super(tier, attackDamageModifier, attackSpeedModifier, properties);
	}

	@Override
	public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
		super.inventoryTick(stack, world, entity, slot, selected);
		orichalcosTick(stack, world, entity, slot, selected);
	}

	@Override
	public boolean canBeHurtBy(ItemStack stack, DamageSource damageSource) {
		return IOrichalcos.canBeHurtBy(damageSource);
	}

	@Override
	public int getManaPerDamage() {
		return MANA_PER_DAMAGE;
	}

	@Override
	public int getActiveCost() {
		return ACTIVE_COST;
	}

	@Override
	public int getRange(Player player, ItemStack stack, BlockPos pos, BlockPos originPos, Direction side) {
		return RANGE;
	}
}
