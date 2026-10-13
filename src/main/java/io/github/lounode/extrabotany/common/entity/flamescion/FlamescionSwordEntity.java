package io.github.lounode.extrabotany.common.entity.flamescion;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import io.github.lounode.extrabotany.common.entity.ExtraBotanyEntityType;

/**
 * 薪炎剑。别的注释把它叫做"剑气"。薪炎模式下挥剑时放出。它飞行 30 tick，每 4 tick 对 3.5 格内的生物造成 4 点火焰伤害。
 * Flamescion Sword. The other comments call it the "sword wave". A swing in the Flamescion mode releases it. It flies for 30 ticks and does 4 flame damage to the mobs within 3.5 blocks on each 4th tick.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code EntityFlamescionSword}。
 * <p>Source: ExtraMeteorP 1.16 {@code EntityFlamescionSword}.
 */
public class FlamescionSwordEntity extends FlameProjectileEntity {
	public FlamescionSwordEntity(EntityType<? extends FlamescionSwordEntity> type, Level level) {
		super(type, level);
	}

	public FlamescionSwordEntity(Level level, Player owner, Vec3 direction) {
		this(ExtraBotanyEntityType.FLAMESCION_SWORD, level);
		launchFrom(owner, direction);
	}

	@Override
	protected int getLifetimeTicks() {
		return 30;
	}

	@Override
	protected int getDamageIntervalTicks() {
		return 4;
	}

	@Override
	protected float getDamageRange() {
		return 3.5F;
	}

	@Override
	protected float getDamage() {
		return 4F;
	}
}
