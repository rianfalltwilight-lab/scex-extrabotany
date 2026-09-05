package io.github.lounode.extrabotany.network.clientbound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import io.github.lounode.extrabotany.api.gaia.GaiaArena;
import io.github.lounode.extrabotany.common.entity.gaia.Gaia;
import io.github.lounode.extrabotany.network.ExtrabotanyPacket;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public record SpawnGaiaPacket(ClientboundAddEntityPacket inner, Gaia.GaiaSpawnData data) implements ExtrabotanyPacket {

	public static final CustomPacketPayload.Type<SpawnGaiaPacket> ID = new CustomPacketPayload.Type<>(prefix("spg"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SpawnGaiaPacket> STREAM_CODEC = StreamCodec.of(
			(buf, packet) -> packet.encode(buf),
			SpawnGaiaPacket::decode
	);

	@Override
	public Type<SpawnGaiaPacket> type() {
		return ID;
	}

	private void encode(RegistryFriendlyByteBuf buf) {
		ClientboundAddEntityPacket.STREAM_CODEC.encode(buf, inner());
		buf.writeVarInt(data().getPlayerCount());
		buf.writeGlobalPos(data().getHome());
		//buf.writeUUID(data().getBossInfoUUID());
		buf.writeJsonWithCodec(GaiaArena.CODEC, data().getArena());
	}

	private static SpawnGaiaPacket decode(RegistryFriendlyByteBuf buf) {
		var inner = ClientboundAddEntityPacket.STREAM_CODEC.decode(buf);
		var data = new Gaia.GaiaSpawnData();
		data.setPlayerCount(buf.readVarInt());
		data.setHome(buf.readGlobalPos());
		//data.setBossInfoUUID(buf.readUUID());
		data.setArena(buf.readJsonWithCodec(GaiaArena.CODEC));
		return new SpawnGaiaPacket(inner, data);
	}

	public static class Handler {
		public static void handle(SpawnGaiaPacket packet, Player localPlayer) {
			var inner = packet.inner();
			var data = packet.data();

			if (localPlayer instanceof LocalPlayer player) {
				player.connection.handleAddEntity(inner);
				Entity e = player.level().getEntity(inner.getId());
				if (e instanceof Gaia gaia) {
					gaia.syncDataFormServer(data);
				}
			}
		}
	}
}
