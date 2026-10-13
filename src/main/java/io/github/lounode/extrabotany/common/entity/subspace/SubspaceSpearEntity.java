package io.github.lounode.extrabotany.common.entity.subspace;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.fluids.FluidType;

import io.github.lounode.extrabotany.common.entity.ExtraBotanyEntityType;
import io.github.lounode.extrabotany.common.handler.DamageHandler;
import io.github.lounode.extrabotany.common.handler.DamageHandler.DamageKind;

/**
 * 亚空之矛。它沿直线飞行，穿过方块和生物，对经过的每个生物造成伤害。
 * Subspace Spear. It flies in a straight line, goes through blocks and mobs, and damages each mob that it passes.
 *
 * <p>每次命中：真实伤害是"伤害值 × 0.4"，再加上"伤害值 × 1.5"（取整）的攻击伤害。处在无敌帧里的生物不会被命中。
 * <p>Each hit: true damage of "damage × 0.4", plus attack damage of "damage × 1.5" (as an integer). A mob in its invulnerability time is not hit.
 *
 * <p>来源：ExtraMeteorP 1.12 {@code EntitySubspaceSpear}。
 * <p>Source: ExtraMeteorP 1.12 {@code EntitySubspaceSpear}.
 */
public class SubspaceSpearEntity extends ThrowableProjectile {
	private static final String TAG_DAMAGE = "Damage";
	private static final String TAG_LIFE = "Life";
	private static final float TRUE_DAMAGE_FACTOR = 0.4F;
	private static final float ATTACK_DAMAGE_FACTOR = 1.5F;

	private float damage = 12F;
	private int life = 100;

	public SubspaceSpearEntity(EntityType<? extends SubspaceSpearEntity> type, Level level) {
		super(type, level);
		setNoGravity(true);
	}

	/**
	 * @param damage 矛的伤害值。 The damage value of the spear.
	 * @param life   矛存在的 tick 数。 The number of ticks that the spear stays.
	 */
	public SubspaceSpearEntity(Level level, LivingEntity owner, float damage, int life) {
		this(ExtraBotanyEntityType.SUBSPACE_SPEAR, level);
		setOwner(owner);
		this.damage = damage;
		this.life = life;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {}

	@Override
	public void tick() {
		if (!level().isClientSide()) {
			if (!(getOwner() instanceof LivingEntity owner) || !owner.isAlive()) {
				discard();
				return;
			}
			hitEntitiesOnPath(owner);
		}
		super.tick();
		if (!level().isClientSide() && tickCount > life) {
			discard();
		}
	}

	private void hitEntitiesOnPath(LivingEntity owner) {
		// 范围覆盖这一 tick 飞过的路径：水平方向宽 1 格，竖直方向高 0.45 格。
		// The area covers the path of this tick: 1 block to each side horizontally and 0.45 blocks vertically.
		AABB area = new AABB(getX(), getY(), getZ(), xOld, yOld, zOld).inflate(1.0, 0.45, 1.0);
		for (LivingEntity living : level().getEntitiesOfClass(LivingEntity.class, area)) {
			if (living == owner || living.invulnerableTime > 0) {
				continue;
			}
			if (!DamageHandler.INSTANCE.checkPassable(living, owner)) {
				continue;
			}
			DamageHandler.INSTANCE.dealTrueDamage(owner, living, damage * TRUE_DAMAGE_FACTOR);
			DamageHandler.INSTANCE.dmg(living, owner, (int) (damage * ATTACK_DAMAGE_FACTOR), DamageKind.NEUTRAL);
		}
	}

	@Override
	protected void onHit(HitResult result) {
		// 矛穿过方块和生物。伤害由 tick 里的范围检测造成。
		// The spear goes through blocks and mobs. The range check in the tick does the damage.
	}

	@Override
	protected boolean canHitEntity(Entity target) {
		return false;
	}

	@Override
	public boolean ignoreExplosion(Explosion explosion) {
		return true;
	}

	@Override
	public boolean isPushedByFluid(FluidType type) {
		return false;
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putFloat(TAG_DAMAGE, damage);
		tag.putInt(TAG_LIFE, life);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		damage = tag.getFloat(TAG_DAMAGE);
		life = tag.getInt(TAG_LIFE);
	}
}
