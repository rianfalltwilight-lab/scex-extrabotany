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

	// 本机玩家在空中已经蹬墙跳了几次，以及上一 tick 是否按着跳跃键。来源：ExtraMeteorP 1.12 ItemWallJumpingRing。
	// How many wall jumps the local player made in the air, and if the jump key was down in the last tick. Source: ExtraMeteorP 1.12 ItemWallJumpingRing.
	private int wallJumps;
	private boolean wallJumpKeyDown;

	@Override
	public void tryWallJump(LivingEntity entity, int maxJumps) {
		if (!(entity instanceof LocalPlayer player) || player != Minecraft.getInstance().player) {
			return;
		}
		if (player.onGround()) {
			wallJumps = 0;
			return;
		}
		if (player.input.jumping && player.horizontalCollision) {
			// 必须重新按下跳跃键，按住不放不会连续跳。
			// The jump key must be pressed again; holding it down does not jump again and again.
			if (!wallJumpKeyDown && wallJumps < maxJumps) {
				player.jumpFromGround();
				wallJumps++;
			}
			wallJumpKeyDown = true;
		} else {
			wallJumpKeyDown = false;
		}
	}
}
