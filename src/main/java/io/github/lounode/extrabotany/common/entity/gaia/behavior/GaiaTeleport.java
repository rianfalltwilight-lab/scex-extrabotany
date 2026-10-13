package io.github.lounode.extrabotany.common.entity.gaia.behavior;

import com.google.common.collect.ImmutableMap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.phys.Vec3;

import vazkii.botania.common.handler.BotaniaSounds;
import vazkii.botania.common.helper.MathHelper;

import io.github.lounode.extrabotany.common.entity.ExtraBotanyMemoryType;
import io.github.lounode.extrabotany.common.entity.gaia.Gaia;

/**
 * Boss 每隔一段时间传送到场地里的一个随机位置。受到伤害后，它在 4 tick 之后就传送。
 * At intervals the boss teleports to a random position in the arena. After a hit, it teleports after 4 ticks.
 */
public class GaiaTeleport<E extends Gaia> extends Behavior<E> {
	public static final int TELEPORT_DELAY = 35;
	public static final float TELEPORT_RANGE = 12;

	public GaiaTeleport() {
		super(ImmutableMap.of(
				ExtraBotanyMemoryType.TELEPORT_DELAY_BASE, MemoryStatus.REGISTERED,
				ExtraBotanyMemoryType.TELEPORT_DELAY, MemoryStatus.REGISTERED,
				ExtraBotanyMemoryType.TELEPORT_RANGE, MemoryStatus.REGISTERED,
				MemoryModuleType.HURT_BY, MemoryStatus.REGISTERED
		));
	}

	public static void initMemories(Brain<? extends Gaia> brain, float range, int delay, int initDelay) {
		brain.setMemory(ExtraBotanyMemoryType.TELEPORT_RANGE, range);
		brain.setMemory(ExtraBotanyMemoryType.TELEPORT_DELAY_BASE, delay);
		brain.setMemory(ExtraBotanyMemoryType.TELEPORT_DELAY, initDelay);
	}

	@Override
	protected boolean canStillUse(ServerLevel level, E entity, long gameTime) {
		return true;
	}

	@Override
	protected void start(ServerLevel level, E gaia, long gameTime) {

	}

	@Override
	protected void tick(ServerLevel level, E gaia, long gameTime) {
		int delay = getTeleportDelay(gaia);

		//When hurt reset delay
		if (isHurt(gaia)) {
			resolveHurt(gaia);
		}

		if (delay > 0) {
			setTeleportDelay(gaia, delay - 1);
			return;
		}

		randomTeleport(gaia, getHome(gaia).pos(), getTeleportRange(gaia));
	}

	protected void randomTeleport(Gaia gaia, BlockPos source, float range) {
		//choose a location to teleport to
		double oldX = gaia.getX(), oldY = gaia.getY(), oldZ = gaia.getZ();
		double newX, newY = source.getY(), newZ;
		int tries = 0;

		do {
			newX = source.getX() + (gaia.getRandom().nextDouble() - .5) * range;
			newZ = source.getZ() + (gaia.getRandom().nextDouble() - .5) * range;
			tries++;
			//ensure it's inside the arena ring, and not just its bounding square
		} while (tries < 50 && MathHelper.pointDistanceSpace(newX, newY, newZ, source.getX(), source.getY(), source.getZ()) > range - 2);

		if (tries == 50) {
			//failsafe: teleport to the beacon
			newX = source.getX() + .5;
			newY = source.getY() + 1.6;
			newZ = source.getZ() + .5;
		}

		//for low-floor arenas, ensure landing on the ground
		BlockPos tentativeFloorPos = BlockPos.containing(newX, newY - 1, newZ);
		if (gaia.level().getBlockState(tentativeFloorPos).getCollisionShape(gaia.level(), tentativeFloorPos).isEmpty()) {
			newY--;
		}

		//teleport there
		gaia.teleportTo(newX, newY, newZ);
		//gaia.setTpDelay(getTeleportDelay());
		setTeleportDelay(gaia, getTeleportDelayBase(gaia));

		//play sound
		gaia.level().playSound(null, oldX, oldY, oldZ, BotaniaSounds.GAIA_TELEPORT, gaia.getSoundSource(), 1F, 1F);
		gaia.playSound(BotaniaSounds.GAIA_TELEPORT, 1F, 1F);

		particles(gaia, new Vec3(oldX, oldY, oldZ), new Vec3(newX, newY, newZ));
	}

	/**
	 * 让客户端画出传送的粒子。
	 * Makes the clients draw the particles of the teleport.
	 *
	 * <p>这段代码在服务端运行，而服务端的 {@code addParticle} 什么也不做，所以以前这里的 128 个粒子从来没有出现过。现在改为发送原版的"传送"实体事件，末影人和紫颂果用的就是它。
	 * <p>This code runs on the server, and {@code addParticle} of the server does nothing, thus the 128 particles that were here before never appeared. Now it sends the vanilla "teleport" entity event, the same that the Enderman and the Chorus Fruit use.
	 */
	protected void particles(Gaia gaia, Vec3 oldPos, Vec3 newPos) {
		gaia.level().broadcastEntityEvent(gaia, EntityEvent.TELEPORT);
	}
	public GlobalPos getHome(Gaia gaia) {
		return gaia.getHome();
	}

	public void setTeleportDelay(Gaia gaia, int tpDelay) {
		gaia.getBrain().setMemory(ExtraBotanyMemoryType.TELEPORT_DELAY, tpDelay);
	}

	public int getTeleportDelay(Gaia gaia) {
		return gaia.getBrain().getMemory(ExtraBotanyMemoryType.TELEPORT_DELAY).orElse(TELEPORT_DELAY);
	}

	public float getTeleportRange(Gaia gaia) {
		return gaia.getBrain().getMemory(ExtraBotanyMemoryType.TELEPORT_RANGE).orElse(TELEPORT_RANGE);
	}

	public int getTeleportDelayBase(Gaia gaia) {
		return gaia.getBrain().getMemory(ExtraBotanyMemoryType.TELEPORT_DELAY_BASE).orElse(TELEPORT_DELAY);
	}

	public boolean isHurt(Gaia gaia) {
		return gaia.getBrain().hasMemoryValue(MemoryModuleType.HURT_BY);
	}

	protected void resolveHurt(Gaia gaia) {
		setTeleportDelay(gaia, 4);
		gaia.getBrain().eraseMemory(MemoryModuleType.HURT_BY);
	}

}
