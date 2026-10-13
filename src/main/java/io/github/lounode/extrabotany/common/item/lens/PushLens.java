package io.github.lounode.extrabotany.common.item.lens;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import vazkii.botania.api.internal.ManaBurst;
import vazkii.botania.common.item.lens.Lens;

import java.util.List;

/**
 * 魔力透镜：冲击。脉冲把碰到的生物沿着自己的飞行方向推走。
 * Push Lens. The burst pushes the creatures that it touches along its flight direction.
 */
public class PushLens extends Lens {
	@Override
	public void updateBurst(ManaBurst burst, ItemStack stack) {
		// 假脉冲只用来计算路径，不能推动生物。
		// A fake burst only calculates the path. It must not push creatures.
		if (burst.isFake()) {
			return;
		}
		Projectile burstEntity = burst.entity();
		List<LivingEntity> entities = burstEntity.level().getEntitiesOfClass(LivingEntity.class, getPushAABB(burst));

		for (var entity : entities) {
			entity.setDeltaMovement(burstEntity.getDeltaMovement());
		}
	}

	public AABB getPushAABB(ManaBurst burst) {
		Projectile entity = burst.entity();
		return new AABB(
				entity.getX(), entity.getY(), entity.getZ(),
				entity.xOld, entity.yOld, entity.zOld
		);
	}
}
