package io.github.lounode.extrabotany.network.serverbound;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import io.github.lounode.extrabotany.common.item.equipment.bauble.FeatherOfJingweiItem;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

import io.netty.buffer.ByteBuf;

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
	}

}
