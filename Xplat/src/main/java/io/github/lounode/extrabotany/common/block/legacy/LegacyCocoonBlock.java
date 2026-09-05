package io.github.lounode.extrabotany.common.block.legacy;

import io.github.lounode.extrabotany.common.block.block_entity.ExtraBotanyBlockEntities;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import vazkii.botania.common.block.BotaniaWaterloggedBlock;

public final class LegacyCocoonBlock extends BotaniaWaterloggedBlock implements EntityBlock {
    public LegacyCocoonBlock(Properties properties) { super(properties); }
    @Override public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return box(3, 0, 3, 13, 14, 13); }
    @Override public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) { return box(3, 0, 3, 13, 14, 13); }
    private InteractionResult extract(Level level, BlockPos pos, Player player) {
        if (!(level.getBlockEntity(pos) instanceof LegacyCocoonEntity cocoon) || cocoon.getItem().isEmpty()) return InteractionResult.PASS;
        if (!level.isClientSide()) {
            var stored = cocoon.getItem().copy(); cocoon.clearItem();
            if (!player.getInventory().add(stored)) Containers.dropItemStack(level, player.getX(), player.getY(), player.getZ(), stored);
            cocoon.pickupSound(SoundSource.PLAYERS);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) { return extract(level, pos, player); }
    @Override protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof LegacyCocoonEntity cocoon)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (!cocoon.getItem().isEmpty()) extract(level, pos, player);
        else {
            if (stack.isEmpty()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            if (!level.isClientSide()) { cocoon.setItem(stack.split(1)); cocoon.pickupSound(SoundSource.PLAYERS); }
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new LegacyCocoonEntity(pos, state); }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, ExtraBotanyBlockEntities.COCOON_OF_DESIRE,
                (world, pos, blockState, entity) -> entity.serverTick());
    }
    @Override protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState next, boolean moving) {
        if (!state.is(next.getBlock())) {
            if (level.getBlockEntity(pos) instanceof LegacyCocoonEntity cocoon && !cocoon.getItem().isEmpty()) {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), cocoon.getItem().copy()); cocoon.clearItem();
            }
            super.onRemove(state, level, pos, next, moving);
        }
    }
}
