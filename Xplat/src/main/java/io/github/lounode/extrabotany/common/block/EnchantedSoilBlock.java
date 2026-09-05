package io.github.lounode.extrabotany.common.block;

import net.minecraft.world.level.block.Block;

/**
 * Compatibility replacement for Botania's removed enchanted soil.
 *
 * <p>The acceleration behavior is applied from the Botania flower tick mixin so it also works
 * for third-party special flowers without coupling this block to their implementations.</p>
 */
public final class EnchantedSoilBlock extends Block {
	public EnchantedSoilBlock(Properties properties) {
		super(properties);
	}
}
