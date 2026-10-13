package io.github.lounode.extrabotany.common.proxy;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import vazkii.botania.xplat.XplatAbstractions;

import io.github.lounode.extrabotany.client.core.proxy.ClientProxy;

/**
 * 只能在客户端做的操作的入口。在专用服务器上，这些方法什么也不做。
 * The entry point for operations that are only possible on the client. On a dedicated server these methods do nothing.
 *
 * <p>除了这个接口，公共代码不引用客户端的类。专用服务器上没有那些类，所以 {@link ClientProxy} 只在客户端创建。
 * <p>Apart from this interface, common code does not refer to client classes. A dedicated server does not have those classes, thus {@link ClientProxy} is only made on the client.
 */
public interface Proxy extends vazkii.botania.common.proxy.Proxy {
	Proxy INSTANCE = make();

	private static Proxy make() {
		if (XplatAbstractions.instance().isPhysicalClient()) {
			return new ClientProxy();
		} else {
			return new Proxy() {};
		}
	}

	default void displayClientMessage(Component chatComponent, boolean actionBar) {}

	/**
	 * 如果这个生物是本机玩家，并且玩家正贴着墙按跳跃键，就让玩家再跳一次。
	 * If this entity is the local player, and the player presses the jump key against a wall, the player jumps again.
	 */
	default void tryWallJump(LivingEntity entity) {}

	/**
	 * 本机玩家是不是已经完成了这个进度。服务端上总是 {@code false}。
	 * Tells if the local player completed this advancement. Always {@code false} on a server.
	 */
	default boolean hasAdvancementOnClient(ResourceLocation id) {
		return false;
	}
}
