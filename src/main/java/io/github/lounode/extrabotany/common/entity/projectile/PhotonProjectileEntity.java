package io.github.lounode.extrabotany.common.entity.projectile;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;

import io.github.lounode.extrabotany.common.entity.ExtraBotanyEntityType;
import io.github.lounode.extrabotany.common.handler.DamageHandler;
import io.github.lounode.extrabotany.common.handler.DamageHandler.DamageKind;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * 光子霰弹枪的弹丸。它飞行 10 tick，对路径上的非玩家生物造成 5 点伤害和 0.5 点真实伤害。
 * The pellet of the Photon Shotgun. The pellet flies for 10 ticks. It applies 5 damage and 0.5 true damage to living entities on its path that are not players.
 *
 * <p>一颗弹丸对同一个目标只结算一次。弹丸碰到方块时消失。
 * <p>One pellet applies damage to the same target only one time. The pellet disappears when it hits a block.
 *
 * <p>来源：ExtraMeteorP 1.12 {@code ItemPhotonShotgun#updateBurst}。原版用魔力脉冲实现弹丸，并且没有注册这个物品。
 * <p>Source: ExtraMeteorP 1.12 {@code ItemPhotonShotgun#updateBurst}. The original uses mana bursts as pellets and does not register this item.
 */
public class PhotonProjectileEntity extends ThrowableItemProjectile {
	public static final int LIFETIME_TICKS = 10;
	private static final double HIT_RADIUS = 0.7;
	private static final float DAMAGE = 5F;
	private static final float TRUE_DAMAGE = 0.5F;

	private final Set<UUID> damagedTargets = new HashSet<>();

	public PhotonProjectileEntity(EntityType<? extends PhotonProjectileEntity> type, Level level) {
		super(type, level);
		setNoGravity(true);
	}

	public PhotonProjectileEntity(Level level, LivingEntity thrower) {
		this(ExtraBotanyEntityType.PHOTON_SHOTGUN_PROJECTILE, level);
		setOwner(thrower);
	}

	@Override
	protected Item getDefaultItem() {
		return ExtraBotanyItems.photonShotgun;
	}

	@Override
	public void tick() {
		super.tick();
		if (level().isClientSide()) {
			level().addParticle(ParticleTypes.END_ROD, getX(), getY(), getZ(), 0, 0, 0);
			return;
		}
		damageTargetsOnPath();
		if (tickCount > LIFETIME_TICKS) {
			discard();
		}
	}

	private void damageTargetsOnPath() {
		Entity thrower = getOwner();
		AABB path = new AABB(getX(), getY(), getZ(), xOld, yOld, zOld).inflate(HIT_RADIUS);
		for (LivingEntity target : level().getEntitiesOfClass(LivingEntity.class, path)) {
			if (target instanceof Player || damagedTargets.contains(target.getUUID())) {
				continue;
			}
			if (!DamageHandler.INSTANCE.dmg(target, thrower, DAMAGE, DamageKind.NEUTRAL)) {
				continue;
			}
			DamageHandler.INSTANCE.dealTrueDamage(thrower, target, TRUE_DAMAGE);
			damagedTargets.add(target.getUUID());
		}
	}

	@Override
	protected void onHit(HitResult result) {
		if (!level().isClientSide() && result.getType() == HitResult.Type.BLOCK) {
			discard();
		}
	}

	@Override
	protected double getDefaultGravity() {
		return 0;
	}
}
