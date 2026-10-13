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

/**
 * 光子材料的物品。生物在白天、露天的地方把它拿在手里时，得到速度 II 效果。
 * An item of the photonium material. When a creature holds it in the day in an open area, the creature gets the Speed II effect.
 *
 * <p>效果每 {@value #BUFF_INTERVAL} tick 刷新一次，每次持续 5 秒。重制版的判断写反了，所以它在 60 tick 里有 59 tick 都会刷新效果并且消耗一次魔力。
 * <p>The effect is applied again one time for each {@value #BUFF_INTERVAL} ticks, and it lasts 5 seconds each time. The condition in Reburn was inverted, thus it applied the effect and used mana on 59 of each 60 ticks.
 */
public interface IPhotonium {
	int BUFF_INTERVAL = 3 * 20;

	default void photoniumTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
		if (level.isClientSide() || !(entity instanceof LivingEntity living)) {
			return;
		}
		if ((living.getItemInHand(InteractionHand.MAIN_HAND) != stack) && (living.getItemInHand(InteractionHand.OFF_HAND) != stack)) {
			return;
		}
		if (living.tickCount % BUFF_INTERVAL != 0) {
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

	/**
	 * @return 每次刷新效果消耗的魔力。 / the mana that each application of the effect uses.
	 */
	default int getPhotoniumBuffMana() {
		return 0;
	}
}
