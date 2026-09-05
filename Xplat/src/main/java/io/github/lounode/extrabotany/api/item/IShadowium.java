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

public interface IShadowium {

	default void shadowiumTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
		if (!(entity instanceof LivingEntity living)) {
			return;
		}
		if ((living.getItemInHand(InteractionHand.MAIN_HAND) != stack) && (living.getItemInHand(InteractionHand.OFF_HAND) != stack)) {
			return;
		}
		if (living.tickCount % (5 * 20) == 0) {
			return;
		}

		if (living instanceof Player player &&
				!ManaItemHandler.instance().requestManaExactForTool(stack, player, getShadowiumBuffMana(), true)) {
			return;
		}

		if (isNight(level) || isInCave(level, entity.blockPosition())) {
			living.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 20 * 20));
		}
	}

	default boolean isNight(Level level) {
		return level.isNight();
	}

	default boolean isInCave(Level level, BlockPos pos) {
		return level.getBrightness(LightLayer.SKY, pos) < 8;
	}

	default int getShadowiumBuffMana() {
		return 0;
	}
}
