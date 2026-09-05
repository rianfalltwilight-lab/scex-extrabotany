package io.github.lounode.extrabotany.common.util;

import com.mojang.authlib.GameProfile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;

import io.github.lounode.extrabotany.xplat.EXplatAbstractions;
import io.github.lounode.extrabotany.xplat.ExtraBotanyConfig;

import java.util.UUID;

public class PlayerUtil {
	public static Player createFakePlayer(ServerLevel level, UUID uuid) {
		return EXplatAbstractions.INSTANCE.createFakePlayer(level, new GameProfile(uuid, ExtraBotanyConfig.common().fakePlayerId()));
	}

	public static boolean tryBreakBlock(Player player, ItemStack stack, Level world, BlockPos pos) {
		if (world.isClientSide()) {
			return false;
		}
		if (!world.getChunkSource().hasChunk(
				SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()))) {
			return false;
		}
		BlockState blockstate = world.getBlockState(pos);
		boolean unminable = blockstate.getDestroyProgress(player, world, pos) == 0;
		if (unminable) {
			return false;
		}
		if (blockstate.isAir()) {
			return false;
		}
		ItemStack save = player.getMainHandItem();
		player.setItemInHand(InteractionHand.MAIN_HAND, stack);

		SimulateDestroyBlockPos simulatePos = new SimulateDestroyBlockPos(pos.getX(), pos.getY(), pos.getZ());

		boolean result = ((ServerPlayer) player).gameMode.destroyBlock(simulatePos);
		if (result) {
			((ServerPlayer) player).connection.send(
					new ClientboundLevelEventPacket(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(blockstate), false));
		}
		player.setItemInHand(InteractionHand.MAIN_HAND, save);
		return result;
	}

	public static class SimulateDestroyBlockPos extends BlockPos {

		public SimulateDestroyBlockPos(int x, int y, int z) {
			super(x, y, z);
		}
	}
}
