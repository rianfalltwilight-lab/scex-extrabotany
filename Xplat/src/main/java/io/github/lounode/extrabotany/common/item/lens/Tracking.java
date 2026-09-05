package io.github.lounode.extrabotany.common.item.lens;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import vazkii.botania.api.internal.ManaBurst;
import vazkii.botania.common.entity.ManaBurstEntity;
import vazkii.botania.common.helper.VecHelper;
import vazkii.botania.common.item.lens.Lens;

import io.github.lounode.extrabotany.common.ExtraBotanyDamageTypes;
import io.github.lounode.extrabotany.common.item.material.ItemTiers;

import java.util.Comparator;

public class Tracking extends Lens {
	public static final double SEARCH_TARGET_RADIUS = 5.0D;
	private static final int MANA_PRE_DAMAGE = 65;

	@Override
	public void updateBurst(ManaBurst burst, ItemStack stack) {
		ManaBurstEntity burstEntity = (ManaBurstEntity) burst.entity();
		Entity thrower = burstEntity.getOwner();

		if (!(thrower instanceof Player player) || !thrower.isAlive()) {
			burstEntity.discard();
			return;
		}

		rotateToEnemy(burstEntity);

		if (burstEntity.level().isClientSide()) {
			return;
		}

		AABB axis = new AABB(burstEntity.getX(), burstEntity.getY(), burstEntity.getZ(), burstEntity.xOld, burstEntity.yOld, burstEntity.zOld).inflate(1);
		var entities = burstEntity.level().getEntitiesOfClass(LivingEntity.class, axis).stream()
				.filter(e -> e != thrower)
				.filter(e -> !(e instanceof Player other && !player.canHarmPlayer(other)))
				.filter(e -> e.hurtTime == 0)
				.toList();

		for (var entity : entities) {
			int cost = MANA_PRE_DAMAGE;
			int mana = burst.getMana();
			if (mana < cost) {
				break;
			}
			burst.setMana(mana - cost);

			float damage = 5F + ItemTiers.EXCALIBUR.getAttackDamageBonus();
			DamageSource source = ExtraBotanyDamageTypes.Sources.excaliburDamage(player.level().registryAccess(), player);
			entity.hurt(source, damage);

			burstEntity.discard();
			break;
		}
	}

	private void rotateToEnemy(ManaBurstEntity burstEntity) {
		AABB searchBox = new AABB(burstEntity.getX(), burstEntity.getY(), burstEntity.getZ(),
				burstEntity.xOld, burstEntity.yOld, burstEntity.zOld).inflate(SEARCH_TARGET_RADIUS);
		burstEntity.level().getEntitiesOfClass(LivingEntity.class, searchBox).stream()
				.filter(this::canTargetEntity)
				.filter(living -> living.hurtTime == 0)
				.filter(living -> living != burstEntity.getOwner())
				.sorted(Comparator.comparingInt(this::getEntityPriority))
				.findFirst()
				.ifPresent(target -> {
					Vec3 thisVec = VecHelper.fromEntityCenter(burstEntity);

					Vec3 targetVec = VecHelper.fromEntityCenter(target);
					Vec3 diffVec = targetVec.subtract(thisVec);

					Vec3 motionVec = diffVec.normalize().scale(0.6);

					burstEntity.setDeltaMovement(motionVec);
				});
	}

	public boolean canTargetEntity(LivingEntity entity) {
		return entity instanceof Mob || entity instanceof Player;
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
