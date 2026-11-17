package io.github.lounode.extrabotany.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Runs the vanilla shape raycast inside loaded chunks, stopping at the first unloaded boundary. */
public final class LoadedAreaRaycast {

	private LoadedAreaRaycast() {}

	public static BlockHitResult clip(Level level, ClipContext context) {
		BlockHitResult hit = new LoadedBlocks(level).clip(context);
		if (hit.getType() == HitResult.Type.BLOCK && !level.hasChunkAt(hit.getBlockPos())) {
			// Keep the miss point on the loaded side, including when a caller rounds it to a block.
			Vec3 back = context.getFrom().subtract(context.getTo()).normalize().scale(1.0E-4);
			Vec3 edge = hit.getLocation().add(back);
			return BlockHitResult.miss(edge, hit.getDirection(), BlockPos.containing(edge));
		}
		return hit;
	}

	private record LoadedBlocks(Level level) implements BlockGetter {
		@Override
		public BlockState getBlockState(BlockPos pos) {
			return level.hasChunkAt(pos) ? level.getBlockState(pos) : Blocks.BARRIER.defaultBlockState();
		}

		@Override
		public FluidState getFluidState(BlockPos pos) {
			return getBlockState(pos).getFluidState();
		}

		@Override
		public BlockEntity getBlockEntity(BlockPos pos) {
			return level.hasChunkAt(pos) ? level.getBlockEntity(pos) : null;
		}

		@Override
		public int getHeight() {
			return level.getHeight();
		}

		@Override
		public int getMinBuildHeight() {
			return level.getMinBuildHeight();
		}
	}
}
