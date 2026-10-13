package io.github.lounode.extrabotany.forge.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import io.github.lounode.extrabotany.common.entity.mount.MountEntity;
import io.github.lounode.extrabotany.common.handler.FlamescionHandler;
import io.github.lounode.extrabotany.common.lib.LibMisc;
import io.github.lounode.extrabotany.network.serverbound.MountInputPacket;
import io.github.lounode.extrabotany.network.serverbound.MountSkillPacket;

/**
 * 把骑手的按键传给坐骑。
 * Sends the keys of the rider to the mount.
 *
 * <p>每个客户端 tick：读取移动键、跳跃键和疾跑键，写入本地的坐骑。按键状态改变时，再发给服务端。
 * <p>On each client tick: read the movement keys, the jump key and the sprint key, and write them to the local mount. When the key state changes, also send it to the server.
 *
 * <p>坐骑键（默认 R）：没有骑乘时召唤饰品坐骑，驾驶飞碟时抓住或者放开下方的生物。
 * <p>The mount key (R by default): summons the accessory mount when the player rides nothing, and catches or releases the mob below when the player drives a Cosmic Car.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code KeyInputHandler}。原版在每次键盘事件时发送数据包。这里只在状态改变时发送。
 * <p>Source: ExtraMeteorP 1.16 {@code KeyInputHandler}. The original sends a packet on each keyboard event. This port only sends a packet when the state changes.
 */
@EventBusSubscriber(modid = LibMisc.MOD_ID, value = Dist.CLIENT)
public final class MountInputHandler {
	private static final int NOT_RIDING = -1;

	private static int lastSentInput = NOT_RIDING;

	private MountInputHandler() {}

	@SubscribeEvent
	public static void onClientTick(ClientTickEvent.Post event) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player == null) {
			lastSentInput = NOT_RIDING;
			return;
		}
		boolean inGame = minecraft.screen == null;

		// consumeClick 每次按下只返回一次 true，所以按住按键不会重复触发。
		// consumeClick returns true one time for each push, so a held key does not trigger again.
		// 薪炎模式下，这个按键用来释放终结技。
		// In the Flamescion mode, this key starts the ultimate.
		while (ExtraBotanyKeyMappings.SKILL.consumeClick()) {
			if (inGame && !FlamescionHandler.isFlamescionMode(minecraft.player)) {
				PacketDistributor.sendToServer(MountSkillPacket.INSTANCE);
			}
		}

		if (!(minecraft.player.getVehicle() instanceof MountEntity mount) || mount.getControllingPassenger() != minecraft.player) {
			lastSentInput = NOT_RIDING;
			return;
		}
		int input = inGame ? readInput(minecraft.options) : 0;
		mount.setInput(input);
		if (input != lastSentInput) {
			PacketDistributor.sendToServer(new MountInputPacket(input));
			lastSentInput = input;
		}
	}

	private static int readInput(Options options) {
		int input = 0;
		if (options.keyUp.isDown()) {
			input |= MountEntity.INPUT_FORWARD;
		}
		if (options.keyDown.isDown()) {
			input |= MountEntity.INPUT_BACK;
		}
		if (options.keyLeft.isDown()) {
			input |= MountEntity.INPUT_LEFT;
		}
		if (options.keyRight.isDown()) {
			input |= MountEntity.INPUT_RIGHT;
		}
		if (options.keyJump.isDown()) {
			input |= MountEntity.INPUT_JUMP;
		}
		if (options.keySprint.isDown()) {
			input |= MountEntity.INPUT_SPRINT;
		}
		return input;
	}
}
