package io.github.lounode.extrabotany.common.item.equipment.tool.hammer;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import vazkii.botania.api.item.SortableTool;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.item.equipment.CustomDamageItem;
import vazkii.botania.common.item.equipment.tool.ToolCommons;

import java.util.function.Consumer;

/**
 * 魔力钢锤。它是一种镐，用魔力代替耐久，并且用魔力自动修复。祭坛的锤击配方使用锤子。它是其他锤子的基类。
 * Manasteel Hammer. It is a type of pickaxe that uses mana in place of durability and repairs itself with mana. The hammer recipes of the pedestal use hammers. It is the base class of the other hammers.
 */
public class ManasteelHammerItem extends PickaxeItem implements CustomDamageItem, SortableTool {
	private static final int MANA_PER_DAMAGE = 60;

	public ManasteelHammerItem(Tier tier, int attackDamageModifier, float attackSpeedModifier, Properties properties) {
		super(tier, properties.attributes(PickaxeItem.createAttributes(tier, attackDamageModifier, attackSpeedModifier)));
	}

	@Override
	public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<Item> onBroken) {
		int manaPerDamage = ((ManasteelHammerItem) stack.getItem()).getManaPerDamage();
		return ToolCommons.damageItemIfPossible(stack, amount, entity, manaPerDamage);
	}

	public int getManaPerDamage() {
		return MANA_PER_DAMAGE;
	}

	@Override
	public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
		if (!world.isClientSide && entity instanceof Player player && stack.getDamageValue() > 0 && ManaItemHandler.instance().requestManaExactForTool(stack, player, MANA_PER_DAMAGE * 2, true)) {
			stack.setDamageValue(stack.getDamageValue() - 1);
		}
	}

	@Override
	public int getSortingPriority(ItemStack stack, BlockState state) {
		return ToolCommons.getToolPriority(stack);
	}
}
