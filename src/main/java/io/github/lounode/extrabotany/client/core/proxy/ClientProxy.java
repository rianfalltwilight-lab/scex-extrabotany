package io.github.lounode.extrabotany.client.core.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import io.github.lounode.extrabotany.common.proxy.Proxy;
import vazkii.patchouli.client.base.ClientAdvancements;

/**
 * {@code Proxy} 的客户端实现。
 * The client implementation of {@code Proxy}.
 */
public class ClientProxy extends vazkii.botania.client.core.proxy.ClientProxy implements Proxy {

	@Override
	public void displayClientMessage(Component chatComponent, boolean actionBar) {
		if (Minecraft.getInstance().player != null) {
			Minecraft.getInstance().player.displayClientMessage(chatComponent, actionBar);
		}
	}

	/**
	 * 进度的状态由 Patchouli 保存：它记录服务端发来的全部进度，图鉴条目的解锁也用它。
	 * Patchouli keeps the state of the advancements: it records all advancements that the server sends, and the unlock of lexicon entries also uses it.
	 */
	@Override
	public boolean hasAdvancementOnClient(ResourceLocation id) {
		return ClientAdvancements.hasDone(id.toString());
	}

	@Override
	public void tryWallJump(LivingEntity entity) {
		if (!(entity instanceof LocalPlayer player) || player != Minecraft.getInstance().player) {
			return;
		}
		// 玩家还在上升时不能再跳，除非玩家在梯子或者藤蔓上。
		// The player cannot jump again while the player still goes up, unless the player is on a ladder or a vine.
		if (player.getDeltaMovement().y() > 0 && !player.onClimbable()) {
			return;
		}
		if (player.input.jumping && player.horizontalCollision) {
			player.jumpFromGround();
		}
	}
}
