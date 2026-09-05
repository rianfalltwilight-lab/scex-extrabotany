package io.github.lounode.extrabotany.network.serverbound;

import net.minecraft.server.level.ServerPlayer;

import io.github.lounode.extrabotany.network.ExtrabotanyPacket;

public abstract class LeftClickPack implements ExtrabotanyPacket {
	public abstract void handle(ServerPlayer player);
}
