package io.github.lounode.extrabotany.network.serverbound;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import io.github.lounode.extrabotany.common.item.relic.ExcaliburItem;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

import io.netty.buffer.ByteBuf;

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
	}
}
