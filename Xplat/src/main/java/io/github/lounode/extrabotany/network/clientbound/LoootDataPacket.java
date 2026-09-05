package io.github.lounode.extrabotany.network.clientbound;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

import io.github.lounode.extrabotany.network.ExtrabotanyPacket;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

import io.netty.buffer.ByteBuf;

public record LoootDataPacket() implements ExtrabotanyPacket {

	public static final LoootDataPacket INSTANCE = new LoootDataPacket();
	public static final CustomPacketPayload.Type<LoootDataPacket> ID = new CustomPacketPayload.Type<>(prefix("ldp"));
	public static final StreamCodec<ByteBuf, LoootDataPacket> STREAM_CODEC = StreamCodec.unit(INSTANCE);

	@Override
	public Type<LoootDataPacket> type() {
		return ID;
	}

	public static class Handler {
		public static void handle(LoootDataPacket packet, Player localPlayer) {
			//int mana = packet.mana();
			/*
			localPlayer.displayClientMessage(
					Component.translatable("message.extrabotany.actionbar.mana_left", mana), true
			);
			*/
		}
	}
}
