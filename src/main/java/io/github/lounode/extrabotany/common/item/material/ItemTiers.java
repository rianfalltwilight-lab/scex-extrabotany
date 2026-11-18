package io.github.lounode.extrabotany.common.item.material;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

import java.util.function.Supplier;

/**
 * 王者圣剑和勇士盾的工具等级。
 * The tool tiers of the Excalibur and the Achilles Shield.
 */
public enum ItemTiers implements Tier {
	EXCALIBUR(2031, 9.0F, 4, BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 30, () -> ExtraBotanyItems.dasRheingold),
	// 1.12 ItemAchilleshield 用植物魔法的泰拉钢材料（2300 次、附魔 26、泰拉钢锭修理），盾牌的耐久再乘 2。 / 1.12 ItemAchilleshield uses the terrasteel material of Botania (2300 uses, enchantability 26, repaired with terrasteel ingots), and the shield doubles the durability.
	ACHILLES_SHIELD(2300, 9.0F, 4.0F, BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 26, () -> vazkii.botania.common.item.BotaniaItems.TERRASTEEL_INGOT);

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
