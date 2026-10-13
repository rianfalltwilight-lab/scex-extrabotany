package io.github.lounode.extrabotany.common.entity.flamescion;

import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.fluids.FluidType;

import io.github.lounode.extrabotany.common.entity.PlayerOwnedEntity;
import io.github.lounode.extrabotany.common.handler.DamageHandler;

import java.util.List;

/**
 * 薪炎的范围效果实体的基类。这些实体停在原地，对周围的生物造成火焰伤害。
 * The base class for the area effect entities of the Flamescion. These entities stay in place and do flame damage to the mobs around them.
 */
public abstract class FlamescionAreaEntity extends PlayerOwnedEntity {
	private static final int SLOWNESS_TICKS = 2;
	private static final int SLOWNESS_LEVEL = 3;

	protected FlamescionAreaEntity(EntityType<?> type, Level level) {
		super(type, level);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {}

	/**
	 * 返回范围内的全部生物。范围是以实体所在方块的中心为中心的立方体，算法和原版相同。
	 * Returns all living entities in range. The range is a cube with its center at the center of the block that the entity is in, as in the original.
	 */
	protected List<LivingEntity> livingInRange(float range) {
		return livingInRange(this, range);
	}

	static List<LivingEntity> livingInRange(Entity center, float range) {
		return center.level().getEntitiesOfClass(LivingEntity.class, new AABB(center.blockPosition()).inflate(range - 0.5));
	}

	/**
	 * 给范围内的怪物加上 2 tick 的缓慢 IV。每 tick 调用时，怪物一直被减速。
	 * Gives Slowness IV for 2 ticks to the monsters in range. With a call on each tick, the monsters stay slow.
	 */
	protected static void slowMonsters(List<LivingEntity> entities) {
		for (LivingEntity living : entities) {
			if (living instanceof Enemy) {
				living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, SLOWNESS_TICKS, SLOWNESS_LEVEL));
			}
		}
	}

	protected void damageInRange(float range, float damage) {
		damageInRange(this, getOwner(), range, damage);
	}

	/**
	 * 对范围内除主人以外的全部生物造成火焰伤害。没有主人时不造成伤害。
	 * Does flame damage to all living entities in range except the owner. There is no damage when there is no owner.
	 */
	static void damageInRange(Entity center, Player owner, float range, float damage) {
		if (owner == null) {
			return;
		}
		for (LivingEntity living : livingInRange(center, range)) {
			if (living != owner) {
				DamageHandler.INSTANCE.flameAttack(living, owner, damage);
			}
		}
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	public boolean isPushedByFluid(FluidType type) {
		return false;
	}
}
