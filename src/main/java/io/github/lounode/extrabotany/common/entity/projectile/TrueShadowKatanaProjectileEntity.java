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

import java.util.List;

/**
 * 真影刃的飞刃。它先停留 3 tick，然后飞向目标，命中第一个目标后消失。
 * The flying blade of the True Shadow Katana. The blade stays for 3 ticks, then flies to the target. It disappears after it hits the first target.
 *
 * <p>命中时清除目标的无敌帧，所以三把飞刃都能造成伤害。
 * <p>A hit clears the invulnerability time of the target, so all three blades can apply damage.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code EntityTrueShadowKatanaProjectile}。
 * <p>Source: ExtraMeteorP 1.16 {@code EntityTrueShadowKatanaProjectile}.
 */
public class TrueShadowKatanaProjectileEntity extends TargetedProjectileEntity {
	public static final int LIFETIME_TICKS = 40;
	private static final int WAIT_TICKS = 3;
	private static final double HIT_RADIUS = 2;

	public TrueShadowKatanaProjectileEntity(EntityType<? extends TrueShadowKatanaProjectileEntity> type, Level level) {
		super(type, level);
	}

	public TrueShadowKatanaProjectileEntity(Level level, LivingEntity thrower, Vec3 start, Vec3 target, double speed) {
		this(ExtraBotanyEntityType.TRUE_SHADOW_KATANA_PROJECTILE, level);
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
		if (tickCount <= WAIT_TICKS) {
			return;
		}

		if (level().isClientSide() && tickCount % 2 == 0) {
			level().addParticle(WispParticleData.wisp(0.15F, 0F, 0F, 0F, 1F), getX(), getY(), getZ(), 0, 0, 0);
		}

		super.tick();

		if (level().isClientSide()) {
			return;
		}
		List<LivingEntity> targets = findTargets(HIT_RADIUS);
		if (targets.isEmpty()) {
			return;
		}
		LivingEntity target = targets.getFirst();
		target.invulnerableTime = 0;
		if (thrower instanceof Player) {
			DamageHandler.INSTANCE.dmg(target, thrower, 5F, DamageKind.NEUTRAL);
		} else {
			DamageHandler.INSTANCE.dmg(target, thrower, 2F, DamageKind.LIFE_LOSING);
			DamageHandler.INSTANCE.dmg(target, thrower, 5.5F, DamageKind.MAGIC);
		}
		discard();
	}
}
