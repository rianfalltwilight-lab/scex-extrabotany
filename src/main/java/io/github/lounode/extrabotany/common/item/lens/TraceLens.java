package io.github.lounode.extrabotany.common.item.lens;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.internal.ManaBurst;
import vazkii.botania.common.helper.VecHelper;
import vazkii.botania.common.item.lens.Lens;

import java.util.Comparator;

/**
 * 魔力透镜：追踪。脉冲飞出 {@value #INIT_TICKS} tick 之后，转向 {@value #SEARCH_TARGET_RADIUS} 格内的生物。
 * Trace Lens. After the burst has flown for {@value #INIT_TICKS} ticks, it turns to a creature in {@value #SEARCH_TARGET_RADIUS} blocks.
 */
public class TraceLens extends Lens {

	public static final double SEARCH_TARGET_RADIUS = 5.0D;
	public static final int INIT_TICKS = 5;

	@Override
	public void updateBurst(ManaBurst burst, ItemStack stack) {
		// 假脉冲只用来计算路径。它在一个 tick 里模拟整段飞行，所以不能让它每一步都查找生物。
		// A fake burst only calculates the path. It simulates the full flight in one tick, thus it must not search for creatures at each step.
		if (burst.isFake()) {
			return;
		}
		Projectile burstEntity = burst.entity();
		if (burstEntity.tickCount < INIT_TICKS) {
			return;
		}
		rotateToEnemy(burst);
	}

	public void rotateToEnemy(ManaBurst burst) {
		Projectile burstEntity = burst.entity();
		LivingEntity target = findTarget(burst);
		if (target == null) {
			return;
		}

		Vec3 thisVec = VecHelper.fromEntityCenter(burstEntity);

		Vec3 targetVec = VecHelper.fromEntityCenter(target);
		Vec3 diffVec = targetVec.subtract(thisVec);

		Vec3 motionVec = diffVec.normalize().scale(0.6);

		burstEntity.setDeltaMovement(motionVec);
	}

	@Nullable
	public LivingEntity findTarget(ManaBurst burst) {
		Projectile burstEntity = burst.entity();
		AABB searchBox = new AABB(burstEntity.getX(), burstEntity.getY(), burstEntity.getZ(),
				burstEntity.xOld, burstEntity.yOld, burstEntity.zOld).inflate(SEARCH_TARGET_RADIUS);
		return burstEntity.level().getEntitiesOfClass(LivingEntity.class, searchBox).stream()
				.filter(this::canTargetEntity)
				.filter(living -> living.hurtTime == 0)
				.filter(living -> living != burstEntity.getOwner())
				.min(Comparator.comparingInt(this::getEntityPriority))
				.orElse(null);
	}

	public boolean canTargetEntity(LivingEntity entity) {
		return true;
	}

	public int getEntityPriority(LivingEntity entity) {
		if (entity instanceof Mob) {
			return 3;
		} else if (entity instanceof Player) {
			return 2;
		} else if (entity instanceof Animal) {
			return 1;
		}
		return 0;
	}
}
