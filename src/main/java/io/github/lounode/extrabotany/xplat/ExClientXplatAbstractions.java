package io.github.lounode.extrabotany.xplat;

import vazkii.botania.api.ServiceUtil;
import vazkii.botania.xplat.ClientXplatAbstractions;

import io.github.lounode.extrabotany.network.ExtrabotanyPacket;

/**
 * 客户端的平台相关操作：向服务端发送数据包。
 * The platform operations of the client: to send packets to the server.
 */
public interface ExClientXplatAbstractions extends ClientXplatAbstractions {
	ExClientXplatAbstractions INSTANCE = ServiceUtil.findService(ExClientXplatAbstractions.class, null);

	void sendToServer(ExtrabotanyPacket packet);
}
