package io.github.lounode.extrabotany.forge.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import io.github.lounode.extrabotany.common.handler.FlamescionHandler;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.lib.LibMisc;
import io.github.lounode.extrabotany.network.serverbound.FlamescionSkillPacket;

/**
 * 薪炎的按键输入。玩家在薪炎模式下：按下潜行键冲刺，按下技能键释放终结技。
 * The key input of the Flamescion. While the player is in the Flamescion mode: a push of the sneak key makes a dash, and a push of the skill key releases the ultimate.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code FlamescionHandler#onKeyInput}。原版写死了左 Shift 键和 R 键。这里使用玩家设置的潜行键和技能键。
 * <p>Source: ExtraMeteorP 1.16 {@code FlamescionHandler#onKeyInput}. The original has the left Shift key and the R key hard-coded. This port uses the sneak key and the skill key that the player sets.
 */
@EventBusSubscriber(modid = LibMisc.MOD_ID, value = Dist.CLIENT)
public final class FlamescionInputHandler {
	private static boolean skillWasDown;
	private static boolean sneakWasDown;

	private FlamescionInputHandler() {}

	@SubscribeEvent
	public static void onClientTick(ClientTickEvent.Post event) {
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		if (player == null) {
			skillWasDown = false;
			sneakWasDown = false;
			return;
		}
		boolean inGame = minecraft.screen == null;
		boolean skillDown = inGame && ExtraBotanyKeyMappings.SKILL.isDown();
		boolean sneakDown = inGame && minecraft.options.keyShift.isDown();

		// 只在按键刚被按下的那个 tick 触发。
		// Only the tick in which the key goes down starts the skill.
		if ((skillDown && !skillWasDown || sneakDown && !sneakWasDown) && FlamescionHandler.isFlamescionMode(player)) {
			if (skillDown && !skillWasDown) {
				PacketDistributor.sendToServer(FlamescionSkillPacket.ULTIMATE);
			} else if (!player.getCooldowns().isOnCooldown(ExtraBotanyItems.flamescionWeapon)) {
				PacketDistributor.sendToServer(FlamescionSkillPacket.DASH);
			}
		}
		skillWasDown = skillDown;
		sneakWasDown = sneakDown;
	}
}
