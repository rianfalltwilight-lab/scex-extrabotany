package io.github.lounode.extrabotany.common.block;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 奖杯。它是一个装饰方块，朝向放置它的玩家面对的方向。
 * Trophy. It is a decoration block that points in the direction that the player looks in when the player puts it down.
 *
 * <p>来源：ExtraMeteorP 1.12 {@code BlockTrophy}。
 * <p>Source: ExtraMeteorP 1.12 {@code BlockTrophy}.
 */
public class TrophyBlock extends HorizontalDirectionalBlock {
	private static final MapCodec<TrophyBlock> CODEC = simpleCodec(TrophyBlock::new);
	private static final VoxelShape SHAPE = box(5, 0, 5, 11, 12, 11);

	public TrophyBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
	}

	@Override
	protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState().setValue(FACING, context.getHorizontalDirection());
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}
}
