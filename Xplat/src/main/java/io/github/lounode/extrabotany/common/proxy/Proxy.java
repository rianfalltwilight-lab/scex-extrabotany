package io.github.lounode.extrabotany.common.proxy;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

import vazkii.botania.xplat.XplatAbstractions;

import io.github.lounode.extrabotany.client.core.proxy.ClientProxy;

public interface Proxy extends vazkii.botania.common.proxy.Proxy {
	Proxy INSTANCE = make();

	private static Proxy make() {
		if (XplatAbstractions.instance().isPhysicalClient()) {
			return new ClientProxy();
		} else {
			return new Proxy() {};
		}
	}

	default void addBoss(LivingEntity boss) {

	}

	default void removeBoss(LivingEntity boss) {

	}

	default void displayClientMessage(Component chatComponent, boolean actionBar) {

	}
}
