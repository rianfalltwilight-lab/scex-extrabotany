package io.github.lounode.extrabotany.common.block.flower;

import io.github.lounode.extrabotany.common.util.PlayerUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/** Block changes made by flowers still carry an identity and cancellable protection events. */
public final class FlowerBlockActions {

	private static final UUID UNOWNED_FLOWER = UUID.fromString("8aef4b0b-f184-4f8f-bc06-f18107aa9ee8");

	private FlowerBlockActions() {}

	public static boolean breakBlock(ServerLevel level, BlockPos pos, @Nullable UUID owner, boolean drops) {
		if (!level.isLoaded(pos)) {
			return false;
		}
		ServerPlayer player = actor(level, pos, owner);
		BlockState state = level.getBlockState(pos);
		if (state.isAir() || state.getDestroySpeed(level, pos) < 0 || !level.mayInteract(player, pos)) {
			return false;
		}
		ItemStack previous = player.getMainHandItem();
		player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		try {
			if (CommonHooks.fireBlockBreak(level, GameType.SURVIVAL, player, pos, state).isCanceled()) {
				return false;
			}
			return level.destroyBlock(pos, drops, player);
		} finally {
			player.setItemInHand(InteractionHand.MAIN_HAND, previous);
		}
	}

	public static boolean placeBlock(ServerLevel level, BlockPos pos, BlockState state, @Nullable UUID owner) {
		if (!level.isLoaded(pos)) {
			return false;
		}
		ServerPlayer player = actor(level, pos, owner);
		if (!level.mayInteract(player, pos) || !player.mayUseItemAt(pos, Direction.UP, ItemStack.EMPTY)) {
			return false;
		}
		// Dispatch before writing, while exposing the proposed state to protection listeners.
		var event = new BlockEvent.EntityPlaceEvent(BlockSnapshot.create(level.dimension(), level, pos),
				level.getBlockState(pos.below()), player) {
			@Override public BlockState getState() { return state; }
			@Override public BlockState getPlacedBlock() { return state; }
		};
		if (NeoForge.EVENT_BUS.post(event).isCanceled()) {
			return false;
		}
		return level.setBlockAndUpdate(pos, state);
	}

	private static ServerPlayer actor(ServerLevel level, BlockPos pos, @Nullable UUID owner) {
		ServerPlayer player = (ServerPlayer) PlayerUtil.createFakePlayer(level, owner == null ? UNOWNED_FLOWER : owner);
		player.setPos(pos.getCenter());
		return player;
	}
}
