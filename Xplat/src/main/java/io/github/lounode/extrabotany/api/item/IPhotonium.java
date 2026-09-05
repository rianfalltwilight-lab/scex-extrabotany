package io.github.lounode.extrabotany.api.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

import vazkii.botania.api.mana.ManaItemHandler;

public interface IPhotonium {

	default void photoniumTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
		if (!(entity instanceof LivingEntity living)) {
			return;
		}
		if ((living.getItemInHand(InteractionHand.MAIN_HAND) != stack) && (living.getItemInHand(InteractionHand.OFF_HAND) != stack)) {
			return;
		}
		if (living.tickCount % (3 * 20) == 0) {
			return;
		}

		if (living instanceof Player player &&
				!ManaItemHandler.instance().requestManaExactForTool(stack, player, getPhotoniumBuffMana(), true)) {
			return;
		}

		if (isDay(level) && isOutCave(level, entity.blockPosition())) {
			living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 5 * 20, 1));
		}
	}

	default boolean isDay(Level level) {
		return !level.isNight();
	}

	default boolean isOutCave(Level level, BlockPos pos) {
		return level.getBrightness(LightLayer.SKY, pos) >= 8;
	}

	default int getPhotoniumBuffMana() {
		return 0;
	}
}
