package io.github.lounode.extrabotany.common.bossevents;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;

import io.github.lounode.extrabotany.network.clientbound.GaiaBossEventPacket;
import io.github.lounode.extrabotany.xplat.EXplatAbstractions;

/**
 * 盖亚守护者的服务端血条。它在普通血条之外，还同步参战的玩家数和无敌时间。
 * The server boss bar of a Guardian of Gaia. In addition to the data of a usual boss bar, it synchronizes the number of players in the fight and the invulnerability time.
 *
 * <p>客户端只用无敌时间来计算血条的噪点强度，见 {@link GaiaBossEvent#getGrainIntensity}。
 * <p>The client only uses the invulnerability time to calculate the grain intensity of the bar, see {@link GaiaBossEvent#getGrainIntensity}.
 */
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
		int old = this.grainTime;
		this.grainTime = time;
		// 无敌时间每 tick 都变化，但是噪点强度只在 10 到 21 之间变化。只在客户端能看出差别时才发数据包。
		// The invulnerability time changes on each tick, but the grain intensity only changes in the range 10 to 21. A packet is only sent when the client can see a difference.
		if (visibleGrainTime(old) != visibleGrainTime(time)) {
			this.broadcast(GaiaBossEventPacket::createGrainPacket);
		}
	}

	private static int visibleGrainTime(int time) {
		return Mth.clamp(time, 10, 21);
	}

	@Override
	public void addPlayer(ServerPlayer player) {
		boolean isNew = !getPlayers().contains(player);
		super.addPlayer(player);
		if (isNew && isVisible()) {
			EXplatAbstractions.INSTANCE.sendToPlayer(player, GaiaBossEventPacket.createPlayersPacket(this));
			EXplatAbstractions.INSTANCE.sendToPlayer(player, GaiaBossEventPacket.createGrainPacket(this));
		}
	}
}