package io.github.lounode.extrabotany.xplat;

import vazkii.botania.api.ServiceUtil;
import vazkii.botania.xplat.ClientXplatAbstractions;

import io.github.lounode.extrabotany.network.ExtrabotanyPacket;

public interface ExClientXplatAbstractions extends ClientXplatAbstractions {
	ExClientXplatAbstractions INSTANCE = ServiceUtil.findService(ExClientXplatAbstractions.class, null);

	void sendToServer(ExtrabotanyPacket packet);
}
