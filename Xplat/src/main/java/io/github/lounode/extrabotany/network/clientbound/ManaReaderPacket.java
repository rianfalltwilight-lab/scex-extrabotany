package io.github.lounode.extrabotany.network.clientbound;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

import io.github.lounode.extrabotany.network.ExtrabotanyPacket;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public record ManaReaderPacket(int mana) implements ExtrabotanyPacket {
	public static final CustomPacketPayload.Type<ManaReaderPacket> ID = new CustomPacketPayload.Type<>(prefix("mrd"));
	public static final StreamCodec<RegistryFriendlyByteBuf, ManaReaderPacket> STREAM_CODEC = StreamCodec.of(
			(buf, packet) -> buf.writeInt(packet.mana()),
			buf -> new ManaReaderPacket(buf.readInt())
	);

	@Override
	public Type<ManaReaderPacket> type() {
		return ID;
	}

	public static class Handler {
		public static void handle(ManaReaderPacket packet, Player localPlayer) {
			localPlayer.displayClientMessage(
					Component.translatable("message.extrabotany.actionbar.mana_left", packet.mana()), true
			);
		}
	}
}
