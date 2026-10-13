package io.github.lounode.extrabotany.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.fluids.FluidUtil;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.common.block.BotaniaWaterloggedBlock;

import io.github.lounode.extrabotany.common.block.block_entity.ExtraBotanyBlockEntities;
import io.github.lounode.extrabotany.common.block.block_entity.LivingrockBarrelBlockEntity;

/**
 * 活石桶。玩家用桶或者别的液体容器右键它，可以放入或取出液体。
 * Livingrock Barrel. A player uses a bucket or a different fluid container on it to put fluid in or to take fluid out.
 *
 * <p>来源：ExtraMeteorP 1.12 {@code BlockLivingrockBarrel}。
 * <p>Source: ExtraMeteorP 1.12 {@code BlockLivingrockBarrel}.
 */
public class LivingrockBarrelBlock extends BotaniaWaterloggedBlock implements EntityBlock {
	private static final VoxelShape SHAPE = box(0, 0, 0, 16, 14, 16);

	public LivingrockBarrelBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (FluidUtil.interactWithFluidHandler(player, hand, level, pos, hit.getDirection())) {
			return ItemInteractionResult.sidedSuccess(level.isClientSide());
		}
		// 和原版相同：液体容器在这里不做别的事。否则一桶水会被倒在活石桶旁边。
		// The same as in the original: a fluid container does nothing more here. If not, a bucket of water would be emptied adjacent to the barrel.
		return FluidUtil.getFluidHandler(stack).isPresent()
				? ItemInteractionResult.CONSUME
				: ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new LivingrockBarrelBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return level.isClientSide() ? null : createTickerHelper(type, ExtraBotanyBlockEntities.LIVINGROCK_BARREL,
				(tickLevel, pos, tickState, barrel) -> barrel.serverTick());
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}
}
