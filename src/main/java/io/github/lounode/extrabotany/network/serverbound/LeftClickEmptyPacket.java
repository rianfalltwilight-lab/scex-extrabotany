package io.github.lounode.extrabotany.network.serverbound;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import io.github.lounode.extrabotany.api.item.LeftClickItem;
import io.github.lounode.extrabotany.network.ExtrabotanyPacket;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 客户端报告一次没有打中实体的左键挥动。
 * The client reports a swing with the attack key that did not hit an entity.
 *
 * <p>数据包不带任何数据。服务端自己读取玩家手上的物品，并且自己检查全部条件。
 * <p>The packet has no data. The server reads the item in the hand of the player and does all the checks itself.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code LeftClickPack}。原版的数据包带着物品，服务端信任客户端发来的物品。
 * <p>Source: ExtraMeteorP 1.16 {@code LeftClickPack}. The packet of the original contains the item, and the server trusts the item from the client.
 */
public final class LeftClickEmptyPacket implements ExtrabotanyPacket {
	public static final LeftClickEmptyPacket INSTANCE = new LeftClickEmptyPacket();
	public static final CustomPacketPayload.Type<LeftClickEmptyPacket> ID =
			new CustomPacketPayload.Type<>(prefix("left_click_empty"));
	public static final StreamCodec<ByteBuf, LeftClickEmptyPacket> STREAM_CODEC = StreamCodec.unit(INSTANCE);

	private LeftClickEmptyPacket() {}

	@Override
	public Type<LeftClickEmptyPacket> type() {
		return ID;
	}

	public void handle(ServerPlayer player) {
		if (player.getMainHandItem().getItem() instanceof LeftClickItem item) {
			item.onLeftClick(player, null);
		}
	}
}
