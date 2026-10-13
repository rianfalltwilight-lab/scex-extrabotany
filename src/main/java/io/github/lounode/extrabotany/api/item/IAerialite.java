package io.github.lounode.extrabotany.api.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import vazkii.botania.api.mana.ManaItemHandler;

/**
 * 天空材料的物品。生物把它拿在手里时，得到急迫 II 效果。
 * An item of the aerialite material. When a creature holds it, the creature gets the Haste II effect.
 *
 * <p>效果每 {@value #BUFF_INTERVAL} tick 刷新一次，每次持续 5 秒。重制版的判断写反了，见 {@link IPhotonium}。
 * <p>The effect is applied again one time for each {@value #BUFF_INTERVAL} ticks, and it lasts 5 seconds each time. The condition in Reburn was inverted, see {@link IPhotonium}.
 */
public interface IAerialite {
	int BUFF_INTERVAL = 3 * 20;

	default void aerialiteTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
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
				!ManaItemHandler.instance().requestManaExactForTool(stack, player, getAerialiteBuffMana(), true)) {
			return;
		}

		living.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 5 * 20, 1));
	}

	/**
	 * @return 每次刷新效果消耗的魔力。 / the mana that each application of the effect uses.
	 */
	default int getAerialiteBuffMana() {
		return 0;
	}
}
