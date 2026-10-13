package io.github.lounode.extrabotany.common.item.equipment.armor.shooting_guardian;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

/**
 * 银翼护卫头盔。它带有整套盔甲的套装效果。
 * Shooting Guardian helmet. The helmet has the set effects of the full armor.
 *
 * <p>穿齐四件时：攻击无视护甲，并回复伤害的 20% 作为生命值；拉弓速度加倍；受到的治疗只有 20%。
 * <p>With all four pieces: attacks ignore armor and give back 20% of the damage as health, a bow draws two times as fast, and healing is only 20%.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code ItemShootingGuardianHelm}。之前的移植没有实现这三个效果。
 * <p>Source: ExtraMeteorP 1.16 {@code ItemShootingGuardianHelm}. The earlier port did not implement these three effects.
 *
 * <p>和原版的差异：原版按护甲减免之前的伤害吸血。这里按实际造成的伤害吸血。
 * <p>Difference from the original: the original gives back health from the damage before armor. This port uses the damage that was actually applied.
 */
public class ShootingGuardianHelmetItem extends ShootingGuardianArmorItem {
	private static final float LIFESTEAL_RATIO = 0.2F;
	private static final float HEALING_RATIO = 0.2F;

	public ShootingGuardianHelmetItem(Properties props) {
		super(Type.HELMET, props);
	}

	private static boolean hasSet(Player player) {
		return ((ShootingGuardianArmorItem) ExtraBotanyItems.shootingGuardianHelm).hasArmorSet(player);
	}

	/**
	 * 让穿着套装的玩家的攻击无视护甲。
	 * Makes the attacks of a player with the set ignore armor.
	 */
	public static void onIncomingDamage(LivingIncomingDamageEvent event) {
		if (event.getSource().getEntity() instanceof Player attacker && hasSet(attacker)) {
			event.addReductionModifier(DamageContainer.Reduction.ARMOR, (container, reduction) -> 0F);
		}
	}

	/**
	 * 伤害生效后，为穿着套装的攻击者回复生命值。
	 * After the damage applies, gives health back to an attacker with the set.
	 */
	public static void onDamageDealt(LivingDamageEvent.Post event) {
		if (!(event.getSource().getEntity() instanceof Player attacker) || attacker.level().isClientSide()) {
			return;
		}
		if (event.getNewDamage() <= 0 || !hasSet(attacker)) {
			return;
		}
		// 直接设置生命值。这样不会触发治疗事件，也就不会被套装自己的治疗减弱影响。
		// Set the health directly. This does not start the heal event, so the healing penalty of the set does not apply.
		attacker.setHealth(Math.min(attacker.getMaxHealth(), attacker.getHealth() + event.getNewDamage() * LIFESTEAL_RATIO));
	}

	/**
	 * 让穿着套装的玩家拉弓更快。每 tick 多扣 1 tick 的使用时间。
	 * Makes a player with the set draw a bow faster. Each tick removes 1 more tick of use time.
	 */
	public static void onUseItemTick(LivingEntityUseItemEvent.Tick event) {
		if (event.getEntity() instanceof Player player
				&& event.getItem().getItem() instanceof BowItem
				&& hasSet(player)) {
			event.setDuration(event.getDuration() - 1);
		}
	}

	/**
	 * 减弱穿着套装的玩家受到的治疗。
	 * Decreases the healing that a player with the set gets.
	 */
	public static void onHeal(LivingHealEvent event) {
		if (event.getEntity() instanceof Player player && hasSet(player)) {
			event.setAmount(event.getAmount() * HEALING_RATIO);
		}
	}
}
