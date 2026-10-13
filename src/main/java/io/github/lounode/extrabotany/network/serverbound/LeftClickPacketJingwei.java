package io.github.lounode.extrabotany.network.serverbound;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import io.github.lounode.extrabotany.common.item.equipment.bauble.FeatherOfJingweiItem;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

import io.netty.buffer.ByteBuf;

/**
 * 佩戴精卫之羽的玩家空手对着空气挥拳。服务端发射火焰。
 * A player that wears the Feather of Jingwei made a swing at the air with an empty hand. The server shoots the fire.
 */
public class LeftClickPacketJingwei extends LeftClickPack {
	public static final LeftClickPacketJingwei INSTANCE = new LeftClickPacketJingwei();

	public static final CustomPacketPayload.Type<LeftClickPacketJingwei> ID = new CustomPacketPayload.Type<>(prefix("lcj"));
	public static final StreamCodec<ByteBuf, LeftClickPacketJingwei> STREAM_CODEC = StreamCodec.unit(INSTANCE);

	@Override
	public Type<LeftClickPacketJingwei> type() {
		return ID;
	}

	@Override
	public void handle(ServerPlayer player) {
		FeatherOfJingweiItem.trySpawnAuraFire(player, player.getAttackStrengthScale(0F));
		// 见 LeftClickPacketExcalibur：不发挥手数据包的客户端不能每 tick 发射一次。
		// See LeftClickPacketExcalibur: a client that sends no swing packet cannot shoot on each tick.
		player.resetAttackStrengthTicker();
	}

}
