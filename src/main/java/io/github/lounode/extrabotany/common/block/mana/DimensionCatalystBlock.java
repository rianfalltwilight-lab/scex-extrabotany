package io.github.lounode.extrabotany.common.block.mana;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import vazkii.botania.api.mana.PoolOverlayProvider;

import io.github.lounode.extrabotany.common.block.ExtraBotanyBlock;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 次元催化器。把它放在魔力池的下面，魔力池就能进行次元类的魔力灌注配方。
 * Dimension Catalyst. When it is below a mana pool, the pool can do the dimension recipes of the mana infusion.
 */
public class DimensionCatalystBlock extends ExtraBotanyBlock implements PoolOverlayProvider {
	// 1.16 MiscellaneousIcons：魔力池上的叠加图就是方块贴图（对照原版审计 E7）。 / MiscellaneousIcons of 1.16: the overlay on the mana pool is the block texture (parity audit E7).
	private static final ResourceLocation OVERLAY_ICON = prefix("block/dimension_catalyst");

	public DimensionCatalystBlock(Properties builder) {
		super(builder);
	}

	@Override
	public ResourceLocation getIcon(Level world, BlockPos pos) {
		return OVERLAY_ICON;
	}
}
