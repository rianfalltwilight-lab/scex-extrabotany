package io.github.lounode.extrabotany.network.clientbound;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

import io.github.lounode.extrabotany.api.gaia.GaiaArena;
import io.github.lounode.extrabotany.common.entity.gaia.Gaia;
import io.github.lounode.extrabotany.network.ExtrabotanyPacket;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 盖亚守护者的生成数据包。它在原版的生成数据之外带有场地、家的位置和玩家数。客户端需要这些数据来画出场地的粒子和播放 Boss 的音乐。
 * The spawn packet of a Guardian of Gaia. In addition to the vanilla spawn data, it holds the arena, the home position and the player count. The client needs this data to draw the particles of the arena and to play the music of the boss.
 */
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
}
