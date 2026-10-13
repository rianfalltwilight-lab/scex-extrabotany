package io.github.lounode.extrabotany.network.serverbound;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import io.github.lounode.extrabotany.common.item.relic.ExcaliburItem;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

import io.netty.buffer.ByteBuf;

/**
 * 玩家拿着王者圣剑对着空气挥剑。服务端发射剑气。
 * The player made a swing at the air with the Excalibur. The server shoots the sword beam.
 */
public class LeftClickPacketExcalibur extends LeftClickPack {
	public static final LeftClickPacketExcalibur INSTANCE = new LeftClickPacketExcalibur();
	public static final CustomPacketPayload.Type<LeftClickPacketExcalibur> ID = new CustomPacketPayload.Type<>(prefix("lc"));
	public static final StreamCodec<ByteBuf, LeftClickPacketExcalibur> STREAM_CODEC = StreamCodec.unit(INSTANCE);

	@Override
	public Type<LeftClickPacketExcalibur> type() {
		return ID;
	}

	@Override
	public void handle(ServerPlayer player) {
		ExcaliburItem.trySpawnBurst(player, player.getAttackStrengthScale(0F));
		// 正常的客户端紧接着会发来挥手的数据包，服务端在那时重置攻击冷却。这里也重置一次，这样不发挥手数据包的客户端不能每 tick 发射一道剑气。
		// A usual client sends the swing packet immediately after this one, and the server resets the attack cooldown then. The cooldown is also reset here, thus a client that sends no swing packet cannot shoot one beam on each tick.
		player.resetAttackStrengthTicker();
	}
}
