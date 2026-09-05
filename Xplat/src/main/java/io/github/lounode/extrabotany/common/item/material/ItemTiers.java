package io.github.lounode.extrabotany.common.item.material;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

import java.util.function.Supplier;

public enum ItemTiers implements Tier {
	EXCALIBUR(2031, 9.0F, 4, BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 30, () -> ExtraBotanyItems.dasRheingold),
	ACHILLES_SHIELD(4600, 9.0F, 4.0F, BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 26, () -> ExtraBotanyItems.orichalcos);

	private final int maxUses;
	private final float efficiency;
	private final float attackDamage;
	private final TagKey<Block> incorrectBlocksForDrops;
	private final int enchantability;
	private final Supplier<Item> repairItem;

	ItemTiers(int maxUses, float efficiency, float attackDamage, TagKey<Block> incorrectBlocksForDrops, int enchantability, Supplier<Item> repairItem) {
		this.maxUses = maxUses;
		this.efficiency = efficiency;
		this.attackDamage = attackDamage;
		this.incorrectBlocksForDrops = incorrectBlocksForDrops;
		this.enchantability = enchantability;
		this.repairItem = repairItem;
	}

	@Override
	public int getUses() {
		return maxUses;
	}

	@Override
	public float getSpeed() {
		return efficiency;
	}

	@Override
	public float getAttackDamageBonus() {
		return attackDamage;
	}

	@Override
	public TagKey<Block> getIncorrectBlocksForDrops() {
		return incorrectBlocksForDrops;
	}

	@Override
	public int getEnchantmentValue() {
		return enchantability;
	}

	@Override
	public Ingredient getRepairIngredient() {
		return Ingredient.of(repairItem.get());
	}
}
