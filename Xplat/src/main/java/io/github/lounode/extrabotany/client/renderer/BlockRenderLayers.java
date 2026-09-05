package io.github.lounode.extrabotany.client.renderer;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.TallFlowerBlock;

import vazkii.botania.common.block.ShimmeringMushroomBlock;
import vazkii.botania.common.block.flower.FloatingFlowerBaseBlock;

import io.github.lounode.extrabotany.common.lib.LibMisc;

import java.util.function.BiConsumer;

public final class BlockRenderLayers {
	public static boolean skipPlatformBlocks;

	private BlockRenderLayers() {}

	public static void init(BiConsumer<Block, RenderType> consumer) {

		BuiltInRegistries.BLOCK.stream().filter(b -> BuiltInRegistries.BLOCK.getKey(b).getNamespace().equals(LibMisc.MOD_ID))
				.forEach(b -> {
					if (b instanceof FloatingFlowerBaseBlock || b instanceof FlowerBlock
							|| b instanceof TallFlowerBlock || b instanceof ShimmeringMushroomBlock
							|| b instanceof FlowerPotBlock) {
						consumer.accept(b, RenderType.cutout());
					}
				});
	}
}
