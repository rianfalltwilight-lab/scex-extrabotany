package io.github.lounode.extrabotany.client.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import io.github.lounode.extrabotany.client.gui.HUD;
import io.github.lounode.extrabotany.client.sound.GaiaMusic;
import io.github.lounode.extrabotany.common.entity.gaia.Gaia;
import io.github.lounode.extrabotany.network.clientbound.ColorfulBossEventPacket;
import io.github.lounode.extrabotany.network.clientbound.ItemActivationPacket;
import io.github.lounode.extrabotany.network.clientbound.ManaReaderPacket;
import io.github.lounode.extrabotany.network.clientbound.SpawnGaiaPacket;

/**
 * 客户端收到服务端的数据包之后做的事。
 * The actions of the client after it gets a packet from the server.
 *
 * <p>数据包的类只有数据和编码，两端都加载它们。这个类使用客户端的类，所以只有客户端加载它：登记处理方法的代码只在收到数据包时才引用这里的方法。
 * <p>The packet classes only have data and codecs, and the two sides load them. This class uses client classes, thus only the client loads it: the code that records the handlers refers to these methods only when a packet arrives.
 */
public final class ClientPacketHandlers {

	private ClientPacketHandlers() {}

	/**
	 * 在操作栏里显示魔力读取器读到的数值。
	 * Shows the value that the Mana Reader read in the action bar.
	 */
	public static void handleManaReader(ManaReaderPacket packet, Player localPlayer) {
		localPlayer.displayClientMessage(
				Component.translatable("message.extrabotany.actionbar.mana_left", packet.mana()), true);
	}

	/**
	 * 生成盖亚守护者，把场地数据交给它，然后开始播放战斗音乐。
	 * Spawns the Guardian of Gaia, gives the arena data to it, and then starts the fight music.
	 */
	public static void handleSpawnGaia(SpawnGaiaPacket packet, Player localPlayer) {
		if (!(localPlayer instanceof LocalPlayer player)) {
			return;
		}
		player.connection.handleAddEntity(packet.inner());
		Entity entity = player.level().getEntity(packet.inner().getId());
		if (entity instanceof Gaia gaia && gaia.syncDataFromServer(packet.data())) {
			GaiaMusic.play(gaia);
		}
	}

	public static void handleBossEvent(ColorfulBossEventPacket packet, Player localPlayer) {
		HUD.getInstance().getBossOverlay().update(packet);
	}

	/**
	 * 播放"物品激活"动画，和不死图腾的动画相同。
	 * Plays the item activation animation, the same as for the Totem of Undying.
	 */
	public static void handleItemActivation(ItemActivationPacket packet, Player localPlayer) {
		Minecraft.getInstance().gameRenderer.displayItemActivation(packet.stack());
	}
}
