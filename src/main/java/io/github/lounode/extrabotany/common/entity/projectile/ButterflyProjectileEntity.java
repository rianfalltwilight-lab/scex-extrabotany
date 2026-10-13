package io.github.lounode.extrabotany.common.entity.projectile;

import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

import io.github.lounode.extrabotany.common.entity.ExtraBotanyEntityType;

/**
 * 西行寺幽幽子的折扇发射的蝴蝶。它存在 100 tick，没有重力。
 * The butterfly that the Saigyouji Yuyuko's Fan shoots. The butterfly exists for 100 ticks and has no gravity.
 *
 * <p>原版没有实现命中效果，所以蝴蝶只是装饰。这个移植保持一致。
 * <p>The original does not implement a hit effect, so the butterfly is only decoration. This port does the same.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code EntityButterflyProjectile}。
 * <p>Source: ExtraMeteorP 1.16 {@code EntityButterflyProjectile}.
 */
public class ButterflyProjectileEntity extends ThrowableProjectile {
	public static final int LIFETIME_TICKS = 100;

	public ButterflyProjectileEntity(EntityType<? extends ButterflyProjectileEntity> type, Level level) {
		super(type, level);
		setNoGravity(true);
	}

	public ButterflyProjectileEntity(Level level, LivingEntity thrower) {
		this(ExtraBotanyEntityType.BUTTERFLY_PROJECTILE, level);
		setOwner(thrower);
	}

	@Override
	public void tick() {
		super.tick();
		if (!level().isClientSide() && tickCount > LIFETIME_TICKS) {
			discard();
		}
	}

	@Override
	protected void onHit(HitResult result) {
		// 原版的蝴蝶穿过方块和实体。
		// The butterfly of the original goes through blocks and entities.
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {}

	@Override
	protected double getDefaultGravity() {
		return 0;
	}
}
