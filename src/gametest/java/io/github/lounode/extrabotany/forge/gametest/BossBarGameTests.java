package io.github.lounode.extrabotany.forge.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Husk;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import io.github.lounode.extrabotany.common.bossevents.GaiaBossEvent;
import io.github.lounode.extrabotany.common.bossevents.ServerGaiaBossEvent;
import io.github.lounode.extrabotany.common.entity.SkullLandMineEntity;
import io.github.lounode.extrabotany.network.clientbound.ColorfulBossEventPacket;
import io.github.lounode.extrabotany.network.clientbound.GaiaBossEventPacket;

import java.util.function.Function;

/**
 * 盖亚战斗里辅助部件的回归测试：血条的数据包和骷髅地雷。
 * Regression tests for the support parts of the Gaia fights: the packets of the boss bar, and the skull landmines.
 */
@GameTestHolder("extrabotany")
@PrefixGameTestTemplate(false)
public final class BossBarGameTests {
	private static final BlockPos CENTER = new BlockPos(2, 2, 2);

	private BossBarGameTests() {}

	/**
	 * 记录服务端血条发出的"无敌时间"数据包，并像客户端那样保存收到的数值。
	 * Records the "invulnerability time" packets that the server boss bar sends, and keeps the received value as the client does.
	 */
	private static final class RecordingBossEvent extends ServerGaiaBossEvent {
		private int packets;
		private int clientTime;

		private RecordingBossEvent() {
			super(Component.literal("test"), BossBarColor.PINK, BossBarOverlay.PROGRESS);
		}

		@Override
		protected void broadcast(Function<BossEvent, ColorfulBossEventPacket> packetGetter) {
			if (packetGetter.apply(this).operation() instanceof GaiaBossEventPacket.UpdateGrainTimeOperation operation) {
				packets++;
				clientTime = operation.time();
			}
		}
	}

	/** 客户端用收到的时间算出的噪点强度。 / The grain intensity that the client calculates from the received time. */
	private static float intensity(int time) {
		return new GaiaBossEvent() {
			@Override
			public int getPlayerCount() {
				return 0;
			}

			@Override
			public void setPlayerCount(int playerCount) {}

			@Override
			public int getGrainTime() {
				return time;
			}

			@Override
			public void setGrainTime(int ignored) {}
		}.getGrainIntensity();
	}

	/**
	 * 无敌时间从 200 降到 0 的过程中，服务端只发 12 个数据包，而客户端算出的噪点强度始终和服务端相同。以前这个过程要发 200 个数据包。
	 * While the invulnerability time decreases from 200 to 0, the server only sends 12 packets, and the grain intensity that the client calculates is always the same as that of the server. Before, this sequence sent 200 packets.
	 */
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void grainTimePacketsOnlyWhenTheIntensityChanges(GameTestHelper helper) {
		RecordingBossEvent event = new RecordingBossEvent();
		for (int time = 200; time >= 0; time--) {
			event.setGrainTime(time);
			helper.assertTrue(event.getGrainTime() == time, "The server lost the invulnerability time");
			helper.assertTrue(intensity(event.clientTime) == event.getGrainIntensity(),
					"The client shows a different grain intensity at time " + time);
		}
		helper.assertTrue(event.packets == 12, "Expected 12 packets, sent " + event.packets);
		helper.succeed();
	}

	/**
	 * 骷髅地雷只伤害玩家。它在服务端运行粒子代码时不出错，并且在倒计时结束后消失。
	 * A skull landmine only hurts players. Its particle code runs on the server without an error, and the landmine goes away when the countdown is complete.
	 */
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void skullLandmineExplodesAndIgnoresMobs(GameTestHelper helper) {
		Husk husk = helper.spawnWithNoFreeWill(EntityType.HUSK, CENTER);
		float health = husk.getHealth();
		SkullLandMineEntity mine = new SkullLandMineEntity.Danger(helper.getLevel(), husk);
		mine.setPos(husk.position());
		helper.getLevel().addFreshEntity(mine);

		mine.setExplodeCountDown(1);
		mine.tick();
		helper.assertTrue(mine.isAlive() && mine.getExplodeCountDown() == 0, "The landmine exploded too early");
		mine.tick();
		helper.assertFalse(mine.isAlive(), "The landmine did not go away after the explosion");
		helper.assertTrue(husk.getHealth() == health, "The landmine hurt a creature that is not a player");
		husk.discard();
		helper.succeed();
	}
}
