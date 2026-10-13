package io.github.lounode.extrabotany.network.serverbound;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import io.github.lounode.extrabotany.common.handler.HerrscherHandler;
import io.github.lounode.extrabotany.network.ExtrabotanyPacket;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 客户端请求释放心斩。
 * The client requests the release of the Thunder Slash.
 *
 * <p>数据包不带任何数据。服务端自己检查能量并且计算心斩的位置。
 * <p>The packet has no data. The server checks the energy and calculates the position of the slash itself.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code HerrscherSkillPack}。原版的数据包带着位置，服务端不做任何检查。
 * <p>Source: ExtraMeteorP 1.16 {@code HerrscherSkillPack}. The packet of the original contains a position, and the server does no checks.
 */
public final class HerrscherSkillPacket implements ExtrabotanyPacket {
	public static final HerrscherSkillPacket INSTANCE = new HerrscherSkillPacket();
	public static final CustomPacketPayload.Type<HerrscherSkillPacket> ID =
			new CustomPacketPayload.Type<>(prefix("herrscher_skill"));
	public static final StreamCodec<ByteBuf, HerrscherSkillPacket> STREAM_CODEC = StreamCodec.unit(INSTANCE);

	private HerrscherSkillPacket() {}

	@Override
	public Type<HerrscherSkillPacket> type() {
		return ID;
	}

	public void handle(ServerPlayer player) {
		HerrscherHandler.useSkill(player);
	}
}
