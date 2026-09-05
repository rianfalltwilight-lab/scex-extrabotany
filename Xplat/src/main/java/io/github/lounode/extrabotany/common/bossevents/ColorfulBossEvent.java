package io.github.lounode.extrabotany.common.bossevents;

import net.minecraft.network.chat.Component;
import net.minecraft.world.BossEvent;

import java.util.UUID;

public abstract class ColorfulBossEvent extends BossEvent {
	public ColorfulBossEvent(UUID id, Component name, BossBarColor color, BossBarOverlay overlay) {
		super(id, name, color, overlay);
	}
}
