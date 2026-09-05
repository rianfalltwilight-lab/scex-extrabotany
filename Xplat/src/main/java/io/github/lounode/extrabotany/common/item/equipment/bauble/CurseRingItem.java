package io.github.lounode.extrabotany.common.item.equipment.bauble;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.item.equipment.bauble.BaubleItem;

public class CurseRingItem extends BaubleItem {

	private static final int RANGE = 6;
	private static final int CURSE_COST = 50;
	private static final int CURSE_COOLDOWN = 30;

	public CurseRingItem(Properties props) {
		super(props);
	}

	@Override
	public void onWornTick(ItemStack stack, LivingEntity entity) {
		super.onWornTick(stack, entity);
		if (!(entity instanceof ServerPlayer serverPlayer)) {
			return;
		}
		if (serverPlayer.tickCount % getCurseCooldown() != 0) {
			return;
		}

		var entities = serverPlayer.level().getEntitiesOfClass(LivingEntity.class, serverPlayer.getBoundingBox().inflate(getRange())).stream()
				.filter(e -> e != entity)
				.toList();

		for (var victim : entities) {
			if (!isLookingAt(entity, victim)) {
				continue;
			}
			if (ManaItemHandler.INSTANCE.requestManaExactForTool(stack, serverPlayer, getCurseCost(), true)) {
				curseEntity(victim, serverPlayer);
			}
		}
	}

	public void curseEntity(LivingEntity victim, LivingEntity source) {
		victim.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 1));
		victim.addEffect(new MobEffectInstance(MobEffects.UNLUCK, 60, 1));
	}

	private static boolean isLookingAt(LivingEntity from, LivingEntity toCheck) {
		Vec3 vec3 = from.getViewVector(1.0F).normalize();
		Vec3 vec31 = new Vec3(toCheck.getX() - from.getX(), toCheck.getEyeY() - from.getEyeY(), toCheck.getZ() - from.getZ());
		double d0 = vec31.length();
		vec31 = vec31.normalize();
		double d1 = vec3.dot(vec31);
		return d1 > (double) 1.0F - 0.025 / d0 && from.hasLineOfSight(toCheck);
	}

	public int getCurseCooldown() {
		return CURSE_COOLDOWN;
	}

	public int getCurseCost() {
		return CURSE_COST;
	}

	public int getRange() {
		return RANGE;
	}
}
