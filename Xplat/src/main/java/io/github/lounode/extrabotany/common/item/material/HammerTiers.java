package io.github.lounode.extrabotany.common.item.material;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

import vazkii.botania.common.item.BotaniaItems;

import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

import java.util.function.Supplier;

public enum HammerTiers implements Tier {
	MANASTEEL(300, 6.2F, 2, BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 20, () -> BotaniaItems.MANASTEEL_INGOT),
	ELEMENTIUM(720, 6.2F, 2, BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 20, () -> BotaniaItems.ELEMENTIUM_INGOT),
	TERRASTEEL(2300, 9, 4, BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 26, () -> BotaniaItems.TERRASTEEL_INGOT),

	GAIA(3600, 10, 5, BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 28, () -> BotaniaItems.GAIA_INGOT),
	PHOTONIUM(900, 6.2F, 2, BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 20, () -> ExtraBotanyItems.photonium),
	SHADOWIUM(900, 6.2F, 2, BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 20, () -> ExtraBotanyItems.shadowium),
	AERIALITE(2300, 9, 4, BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 26, () -> ExtraBotanyItems.aerialite),
	ORICHALCOS(4200, 10, 6, BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 30, () -> ExtraBotanyItems.orichalcos),
	RHEIN(5000, 12, 10, BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 50, () -> ExtraBotanyItems.dasRheingold);

	private final int maxUses;
	private final float efficiency;
	private final float attackDamage;
	private final TagKey<Block> incorrectBlocksForDrops;
	private final int enchantability;
	private final Supplier<Item> repairItem;

	HammerTiers(int maxUses, float efficiency, float attackDamage, TagKey<Block> incorrectBlocksForDrops, int enchantability, Supplier<Item> repairItem) {
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
