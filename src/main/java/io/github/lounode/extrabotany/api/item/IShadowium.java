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
 * 暗影材料的物品。生物在夜晚或者洞穴里把它拿在手里时，得到夜视效果。
 * An item of the shadowium material. When a creature holds it at night or in a cave, the creature gets the Night Vision effect.
 *
 * <p>效果每 {@value #BUFF_INTERVAL} tick 刷新一次，每次持续 20 秒。重制版的判断写反了，见 {@link IPhotonium}。
 * <p>The effect is applied again one time for each {@value #BUFF_INTERVAL} ticks, and it lasts 20 seconds each time. The condition in Reburn was inverted, see {@link IPhotonium}.
 */
public interface IShadowium {
	int BUFF_INTERVAL = 5 * 20;

	default void shadowiumTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
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

	/**
	 * @return 每次刷新效果消耗的魔力。 / the mana that each application of the effect uses.
	 */
	default int getShadowiumBuffMana() {
		return 0;
	}
}
