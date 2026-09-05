package io.github.lounode.extrabotany.client.gui;

import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.chat.Component;

import io.github.lounode.extrabotany.common.bossevents.GaiaBossEvent;

import java.util.UUID;

public class GaiaLerpingBossEvent extends LerpingBossEvent implements GaiaBossEvent {
	private int playerCount;
	private int grainTime;

	public GaiaLerpingBossEvent(UUID id, Component name, float progress, BossBarColor color, BossBarOverlay overlay, boolean darkenScreen, boolean bossMusic, boolean worldFog) {
		super(id, name, progress, color, overlay, darkenScreen, bossMusic, worldFog);
	}

	@Override
	public int getPlayerCount() {
		return playerCount;
	}

	@Override
	public void setPlayerCount(int playerCount) {
		this.playerCount = playerCount;
	}

	@Override
	public int getGrainTime() {
		return this.grainTime;
	}

	@Override
	public void setGrainTime(int time) {
		this.grainTime = time;
	}
}
