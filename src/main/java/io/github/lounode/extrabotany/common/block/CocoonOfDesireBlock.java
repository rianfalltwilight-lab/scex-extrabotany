package io.github.lounode.extrabotany.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.common.block.BotaniaWaterloggedBlock;

import io.github.lounode.extrabotany.common.block.block_entity.CocoonOfDesireBlockEntity;
import io.github.lounode.extrabotany.common.block.block_entity.ExtraBotanyBlockEntities;

/**
 * 不随想之茧。玩家右键空的茧，放入手里的一个物品。玩家右键有物品的茧，取回那个物品。
 * Cocoon of Desire. A player uses an item on an empty cocoon to put one item in. A player uses a cocoon that holds an item to take the item back.
 *
 * <p>方块实体渲染器绘制这个方块，因为茧在孵化时会摇动。
 * <p>The block entity renderer draws this block, because the cocoon wobbles during the incubation.
 *
 * <p>来源：ExtraMeteorP 1.12 {@code BlockCocoonDesire}。
 * <p>Source: ExtraMeteorP 1.12 {@code BlockCocoonDesire}.
 *
 * <p>和原版的差异：玩家的物品栏已满时，取出的物品掉在玩家脚下。原版里这时取不出物品。
 * <p>Difference from the original: when the inventory of the player is full, the item drops at the feet of the player. In the original the item cannot be taken out then.
 */
public class CocoonOfDesireBlock extends BotaniaWaterloggedBlock implements EntityBlock {
	private static final VoxelShape SHAPE = box(3, 0, 3, 13, 14, 13);

	public CocoonOfDesireBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (stack.isEmpty() || !(level.getBlockEntity(pos) instanceof CocoonOfDesireBlockEntity cocoon) || !cocoon.getItem().isEmpty()) {
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		}
		if (!level.isClientSide()) {
			cocoon.setItem(stack.consumeAndReturn(1, player));
			cocoon.playPickupSound(SoundSource.PLAYERS);
		}
		return ItemInteractionResult.sidedSuccess(level.isClientSide());
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (!(level.getBlockEntity(pos) instanceof CocoonOfDesireBlockEntity cocoon) || cocoon.getItem().isEmpty()) {
			return InteractionResult.PASS;
		}
		if (!level.isClientSide()) {
			ItemStack stored = cocoon.getItem();
			cocoon.setItem(ItemStack.EMPTY);
			if (!player.getInventory().add(stored)) {
				player.drop(stored, false);
			}
			cocoon.playPickupSound(SoundSource.PLAYERS);
		}
		return InteractionResult.sidedSuccess(level.isClientSide());
	}

	@Override
	protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
		if (!state.is(newState.getBlock())) {
			if (level.getBlockEntity(pos) instanceof CocoonOfDesireBlockEntity cocoon) {
				Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), cocoon.getItem());
			}
			super.onRemove(state, level, pos, newState, movedByPiston);
		}
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.ENTITYBLOCK_ANIMATED;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new CocoonOfDesireBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return createTickerHelper(type, ExtraBotanyBlockEntities.COCOON_OF_DESIRE, level.isClientSide()
				? (tickLevel, pos, tickState, cocoon) -> cocoon.clientTick()
				: (tickLevel, pos, tickState, cocoon) -> cocoon.serverTick());
	}
}
