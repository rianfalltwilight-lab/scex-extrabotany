package io.github.lounode.extrabotany.common.entity.flamescion;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import io.github.lounode.extrabotany.common.entity.ExtraBotanyEntityType;
import io.github.lounode.extrabotany.common.handler.DamageHandler;

import java.util.List;

/**
 * 薪炎虚空。它存在 40 tick，把 6 格内的生物拉向自己，并且每 15 tick 造成 1.5 点火焰伤害。
 * Flamescion Void. It stays for 40 ticks, pulls the mobs within 6 blocks to itself, and does 1.5 flame damage on each 15th tick.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code EntityFlamescionVoid}。
 * <p>Source: ExtraMeteorP 1.16 {@code EntityFlamescionVoid}.
 *
 * <p>和原版的差异：原版会拉动范围内的全部生物，包括关闭 PvP 时的其他玩家。这里只拉动主人可以伤害的生物。
 * <p>Difference from the original: the original pulls all living entities in range, other players with PvP off included. This port only pulls the entities that the owner can damage.
 */
public class FlamescionVoidEntity extends FlamescionAreaEntity {
	private static final int LIFETIME_TICKS = 40;
	private static final int DAMAGE_INTERVAL_TICKS = 15;
	private static final float RANGE = 6F;
	private static final float DAMAGE = 1.5F;
	private static final double PULL_SPEED = 1.5;

	public FlamescionVoidEntity(EntityType<? extends FlamescionVoidEntity> type, Level level) {
		super(type, level);
	}

	public FlamescionVoidEntity(Level level, Player owner) {
		this(ExtraBotanyEntityType.FLAMESCION_VOID, level);
		setOwner(owner);
	}

	@Override
	public void tick() {
		super.tick();
		if (level().isClientSide()) {
			return;
		}
		Player owner = getOwner();
		if (owner != null) {
			List<LivingEntity> inRange = livingInRange(RANGE);
			slowMonsters(inRange);
			for (LivingEntity living : inRange) {
				if (living == owner || !DamageHandler.INSTANCE.checkPassable(living, owner)) {
					continue;
				}
				Vec3 pull = position().subtract(living.position());
				if (pull.lengthSqr() > 1.0E-6) {
					living.setDeltaMovement(pull.normalize().scale(PULL_SPEED));
					// 玩家的运动由客户端控制，所以要通知客户端速度变了。
					// The client controls the movement of a player, so the client must be told that the velocity changed.
					living.hurtMarked = true;
				}
			}
		}
		if (tickCount % DAMAGE_INTERVAL_TICKS == 0) {
			damageInRange(RANGE, DAMAGE);
		}
		if (tickCount >= LIFETIME_TICKS) {
			discard();
		}
	}
}
