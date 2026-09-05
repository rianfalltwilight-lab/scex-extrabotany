package io.github.lounode.extrabotany.common.item.equipment.shield;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.item.SortableTool;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.item.equipment.CustomDamageItem;
import vazkii.botania.common.item.equipment.tool.ToolCommons;

import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;

import java.util.function.Consumer;

public class ManasteelShieldItem extends ShieldItem implements CustomDamageItem, SortableTool {

	private static final int MANA_PER_DAMAGE = 60;

	private final Tier tier;

	public ManasteelShieldItem(Properties properties, Tier tier) {
		super(properties.durability((int) (tier.getUses() * 1.5F)));
		this.tier = tier;
	}

	public ManasteelShieldItem(Properties properties) {
		this(properties, BotaniaAPI.instance().getManasteelItemTier());
	}

	@Override
	public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
		if (!world.isClientSide && entity instanceof Player player && stack.getDamageValue() > 0 &&
				ManaItemHandler.instance().requestManaExactForTool(stack, player, getManaPerDamage(), true)) {
			stack.setDamageValue(stack.getDamageValue() - 1);
		}
	}

	@Override
	public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<Item> onBroken) {
		int manaPerDamage = ((ManasteelShieldItem) stack.getItem()).getManaPerDamage();
		return ToolCommons.damageItemIfPossible(stack, amount, entity, manaPerDamage);
	}

	public void onShieldBlock(ItemStack stack, LivingEntity blocker, DamageSource source, float damage) {

	}

	public int getManaPerDamage() {
		return MANA_PER_DAMAGE;
	}

	public Tier getTier() {
		return tier;
	}

	@Override
	public int getEnchantmentValue(ItemStack stack) {
		return getTier().getEnchantmentValue();
	}

	@Override
	public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
		return getTier().getRepairIngredient().test(repair) || super.isValidRepairItem(toRepair, repair);
	}

	@Override
	public int getSortingPriority(ItemStack stack, BlockState state) {
		return ToolCommons.getToolPriority(stack);
	}

	public static class EventHandler {

		public static void onShieldBlockDamage(LivingShieldBlockEvent event) {
			if (!(event.getEntity() instanceof Player player)) {
				return;
			}
			ItemStack stack = player.getUseItem();
			if (stack.getItem() instanceof ManasteelShieldItem shieldItem) {
				shieldItem.onShieldBlock(stack, event.getEntity(), event.getDamageSource(), event.getBlockedDamage());
			}
		}
	}
}
