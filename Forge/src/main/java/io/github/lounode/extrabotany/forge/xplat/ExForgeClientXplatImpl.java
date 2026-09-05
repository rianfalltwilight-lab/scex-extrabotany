package io.github.lounode.extrabotany.forge.xplat;

import net.neoforged.neoforge.network.PacketDistributor;

import vazkii.botania.neoforge.client.NeoForgeClientXplatImpl;

import io.github.lounode.extrabotany.network.ExtrabotanyPacket;
import io.github.lounode.extrabotany.xplat.ExClientXplatAbstractions;

public class ExForgeClientXplatImpl extends NeoForgeClientXplatImpl implements ExClientXplatAbstractions {
	@Override
	public void sendToServer(ExtrabotanyPacket packet) {
		PacketDistributor.sendToServer(packet);
	}
}
