package io.github.lounode.extrabotany.common.item.equipment.bauble;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import vazkii.botania.common.item.equipment.bauble.BaubleItem;

import io.github.lounode.extrabotany.common.handler.DamageHandler;
import io.github.lounode.extrabotany.common.lib.ExtraBotanyTags;
import io.github.lounode.extrabotany.common.lib.LibMisc;

/**
 * 和平友好之证。佩戴者的模组武器不会伤害其他玩家和被动生物。其他玩家的模组武器也不会伤害佩戴者。
 * Peace Amulet. The mod weapons of the wearer do not damage other players and passive mobs. The mod weapons of other players also do not damage the wearer.
 *
 * <p>判断规则在 {@link DamageHandler#isBlockedByPeaceAmulet} 中。
 * <p>The rule is in {@link DamageHandler#isBlockedByPeaceAmulet}.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code ModItems.peaceamulet} 和 {@code DamageHandler#checkPassable}。
 * <p>Source: ExtraMeteorP 1.16 {@code ModItems.peaceamulet} and {@code DamageHandler#checkPassable}.
 */
public class PeaceAmuletItem extends BaubleItem {

	public PeaceAmuletItem(Properties props) {
		super(props);
	}

	/**
	 * 拦截没有经过 {@link DamageHandler} 的模组伤害，例如直接用模组武器近战。
	 * Stops mod damage that does not go through {@link DamageHandler}, for example a melee hit with a mod weapon.
	 *
	 * <p>来源：重制版的 {@code peace_amulet_available} 伤害类型标签。
	 * <p>Source: the {@code peace_amulet_available} damage type tag of Reburn.
	 */
	public static void onIncomingDamage(LivingIncomingDamageEvent event) {
		if (!(event.getSource().getEntity() instanceof Player attacker)) {
			return;
		}
		LivingEntity target = event.getEntity();
		if (target == attacker || !isModDamage(event, attacker)) {
			return;
		}
		if (DamageHandler.INSTANCE.isBlockedByPeaceAmulet(target, attacker)) {
			event.setCanceled(true);
		}
	}

	private static boolean isModDamage(LivingIncomingDamageEvent event, Player attacker) {
		return event.getSource().is(ExtraBotanyTags.DamageTypes.PEACE_AMULET_AVAILABLE)
				|| isModItem(attacker.getMainHandItem())
				|| isModItem(attacker.getOffhandItem());
	}

	private static boolean isModItem(ItemStack stack) {
		return !stack.isEmpty()
				&& LibMisc.MOD_ID.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace());
	}
}
