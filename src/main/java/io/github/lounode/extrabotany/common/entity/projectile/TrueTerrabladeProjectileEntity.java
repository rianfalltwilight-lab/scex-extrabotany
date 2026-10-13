package io.github.lounode.extrabotany.common.entity.projectile;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import vazkii.botania.client.fx.WispParticleData;

import io.github.lounode.extrabotany.common.entity.ExtraBotanyEntityType;
import io.github.lounode.extrabotany.common.handler.DamageHandler;
import io.github.lounode.extrabotany.common.handler.DamageHandler.DamageKind;

/**
 * 真泰拉之刃的剑气。它穿过所有目标，每 3 tick 结算一次伤害。
 * The beam of the True Terra Blade. The beam goes through all targets and applies damage one time each 3 ticks.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code EntityTrueTerrabladeProjectile}。
 * <p>Source: ExtraMeteorP 1.16 {@code EntityTrueTerrabladeProjectile}.
 */
public class TrueTerrabladeProjectileEntity extends TargetedProjectileEntity {
	public static final int LIFETIME_TICKS = 60;
	private static final int DAMAGE_INTERVAL_TICKS = 3;
	private static final double HIT_RADIUS = 2;

	public TrueTerrabladeProjectileEntity(EntityType<? extends TrueTerrabladeProjectileEntity> type, Level level) {
		super(type, level);
	}

	public TrueTerrabladeProjectileEntity(Level level, LivingEntity thrower, Vec3 start, Vec3 target, double speed) {
		this(ExtraBotanyEntityType.TRUE_TERRABLADE_PROJECTILE, level);
		setOwner(thrower);
		setPos(start);
		setTargetPos(target);
		faceTarget(speed);
	}

	@Override
	public void tick() {
		if (tickCount >= LIFETIME_TICKS) {
			discard();
			return;
		}
		LivingEntity thrower = getLivingOwner();
		if (!level().isClientSide() && thrower == null) {
			discard();
			return;
		}

		super.tick();

		if (level().isClientSide()) {
			if (tickCount % 2 == 0) {
				level().addParticle(WispParticleData.wisp(0.3F, 0.1F, 0.95F, 0.1F, 1F), getX(), getY(), getZ(), 0, 0, 0);
			}
			return;
		}
		if (tickCount % DAMAGE_INTERVAL_TICKS != 0) {
			return;
		}

		for (LivingEntity target : findTargets(HIT_RADIUS)) {
			if (thrower instanceof Player) {
				DamageHandler.INSTANCE.dmg(target, thrower, 11F, DamageKind.NEUTRAL);
			} else {
				// 非玩家发射者（例如 Boss）造成直接扣血加魔法伤害。
				// A thrower that is not a player (for example a boss) applies a direct health decrease and magic damage.
				if (target.invulnerableTime == 0) {
					DamageHandler.INSTANCE.dmg(target, thrower, 2.5F, DamageKind.LIFE_LOSING);
				}
				DamageHandler.INSTANCE.dmg(target, thrower, 7F, DamageKind.MAGIC);
			}
		}
	}
}
