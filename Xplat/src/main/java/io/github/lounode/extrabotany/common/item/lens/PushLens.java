package io.github.lounode.extrabotany.common.item.lens;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import vazkii.botania.api.internal.ManaBurst;
import vazkii.botania.common.item.lens.Lens;

import java.util.List;

public class PushLens extends Lens {
	@Override
	public void updateBurst(ManaBurst burst, ItemStack stack) {
		Projectile burstEntity = burst.entity();
		List<LivingEntity> entities = burstEntity.level().getEntitiesOfClass(LivingEntity.class, getPushAABB(burst));

		if (burst.isFake()) {
			return;
		}

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
