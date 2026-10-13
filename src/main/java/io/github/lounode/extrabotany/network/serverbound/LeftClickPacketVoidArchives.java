package io.github.lounode.extrabotany.network.serverbound;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import io.github.lounode.extrabotany.common.item.relic.void_archives.variants.Excalibur;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

import io.netty.buffer.ByteBuf;

/**
 * 玩家拿着虚空万藏（王者圣剑形态）对着空气挥剑。服务端发射剑气。
 * The player made a swing at the air with the Void Archives (in its Excalibur form). The server shoots the sword beam.
 */
public class LeftClickPacketVoidArchives extends LeftClickPack {

	public static final LeftClickPacketVoidArchives INSTANCE = new LeftClickPacketVoidArchives();
	public static final CustomPacketPayload.Type<LeftClickPacketVoidArchives> ID = new CustomPacketPayload.Type<>(prefix("lca"));
	public static final StreamCodec<ByteBuf, LeftClickPacketVoidArchives> STREAM_CODEC = StreamCodec.unit(INSTANCE);

	@Override
	public Type<LeftClickPacketVoidArchives> type() {
		return ID;
	}

	@Override
	public void handle(ServerPlayer player) {
		Excalibur.INSTANCE.trySpawnBurst(player, player.getAttackStrengthScale(0F));
		// 见 LeftClickPacketExcalibur：不发挥手数据包的客户端不能每 tick 发射一道剑气。
		// See LeftClickPacketExcalibur: a client that sends no swing packet cannot shoot one beam on each tick.
		player.resetAttackStrengthTicker();
	}
}
