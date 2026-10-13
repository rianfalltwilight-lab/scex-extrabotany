package io.github.lounode.extrabotany.common.bossevents;

import net.minecraft.network.chat.Component;
import net.minecraft.world.BossEvent;

import java.util.UUID;

/**
 * 本模组 Boss 血条的基类。
 * The base class of the boss bars of this mod.
 */
public abstract class ColorfulBossEvent extends BossEvent {
	public ColorfulBossEvent(UUID id, Component name, BossBarColor color, BossBarOverlay overlay) {
		super(id, name, color, overlay);
	}
}
