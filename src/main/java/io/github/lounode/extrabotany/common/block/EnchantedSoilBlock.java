package io.github.lounode.extrabotany.common.block;

import net.minecraft.world.level.block.Block;

/**
 * 蕴魔土。植物魔法 1.21 删除了它的蕴魔土，这个方块代替它。长在它上面的特殊花每 tick 运行两次，被动花不会枯萎。
 * Enchanted Soil. Botania 1.21 removed its Enchanted Soil, and this block replaces it. A special flower on this block runs two times on each tick, and a passive flower does not decay.
 *
 * <p>加速的逻辑在 {@code EnchantedSoilFlowerBoostMixin} 里，所以它对别的模组的特殊花也有效。
 * <p>The acceleration logic is in {@code EnchantedSoilFlowerBoostMixin}, thus it also applies to the special flowers of other mods.
 */
public final class EnchantedSoilBlock extends Block {
	public EnchantedSoilBlock(Properties properties) {
		super(properties);
	}
}
