package io.github.lounode.extrabotany.common.bossevents;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;

import io.github.lounode.extrabotany.network.clientbound.ColorfulBossEventPacket;
import io.github.lounode.extrabotany.xplat.EXplatAbstractions;

import java.util.Set;
import java.util.function.Function;

/**
 * 服务端的血条。数据变化时，它把更新发给所有能看到 Boss 的玩家。它对应原版的 {@code ServerBossEvent}，但是使用本模组自己的数据包，这样客户端能画出植物魔法风格的血条。
 * The server boss bar. When the data changes, it sends the update to all players that can see the boss. It corresponds to the vanilla {@code ServerBossEvent}, but it uses the packets of this mod, thus the client can draw a boss bar in the style of Botania.
 */
public class ServerColorfulBossEvent extends ColorfulBossEvent {

	private final Set<ServerPlayer> players = Sets.newHashSet();
	private boolean visible;

	public ServerColorfulBossEvent(Component name, BossEvent.BossBarColor color, BossEvent.BossBarOverlay overlay) {
		super(Mth.createInsecureUUID(), name, color, overlay);
		this.visible = true;
	}

	@Override
	public void setProgress(float progress) {
		if (progress != this.progress) {
			super.setProgress(progress);
			this.broadcast(ColorfulBossEventPacket::createUpdateProgressPacket);
		}

	}

	@Override
	public void setColor(BossEvent.BossBarColor color) {
		if (color != this.color) {
			super.setColor(color);
			this.broadcast(ColorfulBossEventPacket::createUpdateStylePacket);
		}

	}

	@Override
	public void setOverlay(BossEvent.BossBarOverlay overlay) {
		if (overlay != this.overlay) {
			super.setOverlay(overlay);
			this.broadcast(ColorfulBossEventPacket::createUpdateStylePacket);
		}

	}

	@Override
	public BossEvent setDarkenScreen(boolean darkenSky) {
		if (darkenSky != this.darkenScreen) {
			super.setDarkenScreen(darkenSky);
			this.broadcast(ColorfulBossEventPacket::createUpdatePropertiesPacket);
		}

		return this;
	}

	@Override
	public BossEvent setPlayBossMusic(boolean playEndBossMusic) {
		if (playEndBossMusic != this.playBossMusic) {
			super.setPlayBossMusic(playEndBossMusic);
			this.broadcast(ColorfulBossEventPacket::createUpdatePropertiesPacket);
		}

		return this;
	}

	@Override
	public BossEvent setCreateWorldFog(boolean createFog) {
		if (createFog != this.createWorldFog) {
			super.setCreateWorldFog(createFog);
			this.broadcast(ColorfulBossEventPacket::createUpdatePropertiesPacket);
		}

		return this;
	}

	@Override
	public void setName(Component name) {
		if (!Objects.equal(name, this.name)) {
			super.setName(name);
			this.broadcast(ColorfulBossEventPacket::createUpdateNamePacket);
		}

	}

	protected void broadcast(Function<BossEvent, ColorfulBossEventPacket> packetGetter) {
		if (this.visible) {
			ColorfulBossEventPacket packet = packetGetter.apply(this);

			for (ServerPlayer serverplayer : this.players) {
				EXplatAbstractions.INSTANCE.sendToPlayer(serverplayer, packet);
			}
		}
	}

	public void addPlayer(ServerPlayer player) {
		if (this.players.add(player) && this.visible) {
			EXplatAbstractions.INSTANCE.sendToPlayer(player, ColorfulBossEventPacket.createAddPacket(this));
		}

	}

	public void removePlayer(ServerPlayer player) {
		if (this.players.remove(player) && this.visible) {
			EXplatAbstractions.INSTANCE.sendToPlayer(player, ColorfulBossEventPacket.createRemovePacket(this.getId()));
		}

	}

	public void removeAllPlayers() {
		if (!this.players.isEmpty()) {
			for (ServerPlayer serverplayer : Lists.newArrayList(this.players)) {
				this.removePlayer(serverplayer);
			}
		}

	}

	public boolean isVisible() {
		return this.visible;
	}

	public void setVisible(boolean visible) {
		if (visible != this.visible) {
			this.visible = visible;

			for (ServerPlayer serverplayer : this.players) {
				EXplatAbstractions.INSTANCE.sendToPlayer(serverplayer, visible ? ColorfulBossEventPacket.createAddPacket(this) : ColorfulBossEventPacket.createRemovePacket(this.getId()));
			}
		}
	}

	public Set<ServerPlayer> getPlayers() {
		return this.players;
	}
}
