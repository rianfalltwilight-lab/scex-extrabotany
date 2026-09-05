package io.github.lounode.extrabotany.network.serverbound;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import io.github.lounode.extrabotany.common.item.relic.void_archives.variants.Excalibur;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

import io.netty.buffer.ByteBuf;

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
	}
}
