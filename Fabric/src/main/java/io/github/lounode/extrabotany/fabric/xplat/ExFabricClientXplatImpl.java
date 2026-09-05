package io.github.lounode.extrabotany.fabric.xplat;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import vazkii.botania.fabric.client.FabricClientXplatImpl;

import io.github.lounode.extrabotany.network.ExtrabotanyPacket;
import io.github.lounode.extrabotany.xplat.ExClientXplatAbstractions;

public class ExFabricClientXplatImpl extends FabricClientXplatImpl implements ExClientXplatAbstractions {
	@Override
	public void sendToServer(ExtrabotanyPacket packet) {
		ClientPlayNetworking.send(packet.getFabricId(), packet.toBuf());
	}
}
