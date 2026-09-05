package io.github.lounode.extrabotany.client.core.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

import io.github.lounode.extrabotany.client.core.handler.BossBarHandler;
import io.github.lounode.extrabotany.common.proxy.Proxy;

public class ClientProxy extends vazkii.botania.client.core.proxy.ClientProxy implements Proxy {

	@Override
	public void addBoss(LivingEntity boss) {
		BossBarHandler.bosses.add(boss);
	}

	@Override
	public void removeBoss(LivingEntity boss) {
		BossBarHandler.bosses.remove(boss);
	}

	@Override
	public void displayClientMessage(Component chatComponent, boolean actionBar) {
		if (Minecraft.getInstance().player != null) {
			Minecraft.getInstance().player.displayClientMessage(chatComponent, actionBar);
		}
	}
}
