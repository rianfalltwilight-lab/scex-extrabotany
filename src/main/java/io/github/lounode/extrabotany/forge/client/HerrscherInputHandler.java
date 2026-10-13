package io.github.lounode.extrabotany.forge.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.SwordItem;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import io.github.lounode.extrabotany.common.handler.HerrscherHandler;
import io.github.lounode.extrabotany.common.lib.LibMisc;
import io.github.lounode.extrabotany.network.serverbound.HerrscherSkillPacket;

/**
 * 心斩的按键输入：能量充满的雷之律者按住疾跑键，并且拿着剑对着空气挥动。
 * The key input for the Thunder Slash: a Herrscher of Thunder with full energy holds the sprint key and swings a sword at the air.
 *
 * <p>客户端只做初步判断，避免发送没有用的数据包。服务端再检查一次全部条件。
 * <p>The client only does a first check, so that it does not send packets that have no use. The server checks all the conditions again.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code HerrscherHandler#onPlayerLeftClick}。
 * <p>Source: ExtraMeteorP 1.16 {@code HerrscherHandler#onPlayerLeftClick}.
 */
@EventBusSubscriber(modid = LibMisc.MOD_ID, value = Dist.CLIENT)
public final class HerrscherInputHandler {
	private HerrscherInputHandler() {}

	@SubscribeEvent
	public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
		Player player = event.getEntity();
		if (!(event.getItemStack().getItem() instanceof SwordItem) || !Minecraft.getInstance().options.keySprint.isDown()) {
			return;
		}
		if (!HerrscherHandler.isHerrscherOfThunder(player)) {
			return;
		}
		if (HerrscherHandler.getEnergy(player) >= HerrscherHandler.MAX_ENERGY || player.isCreative()) {
			PacketDistributor.sendToServer(HerrscherSkillPacket.INSTANCE);
		}
	}
}
