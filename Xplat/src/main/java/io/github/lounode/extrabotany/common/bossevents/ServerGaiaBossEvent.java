package io.github.lounode.extrabotany.common.bossevents;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import io.github.lounode.extrabotany.network.clientbound.GaiaBossEventPacket;

public class ServerGaiaBossEvent extends ServerColorfulBossEvent implements GaiaBossEvent {

	private int playerCount;
	private int grainTime;

	public ServerGaiaBossEvent(Component name, BossBarColor color, BossBarOverlay overlay) {
		super(name, color, overlay);
	}

	@Override
	public int getPlayerCount() {
		return this.playerCount;
	}

	@Override
	public void setPlayerCount(int playerCount) {
		if (this.playerCount != playerCount) {
			this.playerCount = playerCount;
			this.broadcast(GaiaBossEventPacket::createPlayersPacket);
		}
	}

	@Override
	public int getGrainTime() {
		return this.grainTime;
	}

	@Override
	public void setGrainTime(int time) {
		if (this.grainTime != time) {
			this.grainTime = time;
			this.broadcast(GaiaBossEventPacket::createGrainPacket);
		}
	}

	@Override
	public void addPlayer(ServerPlayer player) {
		super.addPlayer(player);
		this.broadcast(GaiaBossEventPacket::createPlayersPacket);
	}
}
