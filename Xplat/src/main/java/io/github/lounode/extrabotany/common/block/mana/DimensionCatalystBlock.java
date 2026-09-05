package io.github.lounode.extrabotany.common.block.mana;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import vazkii.botania.api.mana.PoolOverlayProvider;

import io.github.lounode.extrabotany.common.block.ExtraBotanyBlock;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class DimensionCatalystBlock extends ExtraBotanyBlock implements PoolOverlayProvider {
	private static final ResourceLocation OVERLAY_ICON = prefix("block/dimension_catalyst_overlay");

	public DimensionCatalystBlock(Properties builder) {
		super(builder);
	}

	@Override
	public ResourceLocation getIcon(Level world, BlockPos pos) {
		return OVERLAY_ICON;
	}
}
