package io.github.lounode.extrabotany.common.handler;

import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import vazkii.botania.common.handler.EquipmentHandler;

import io.github.lounode.extrabotany.common.attachment.ExtraBotanyAttachments;
import io.github.lounode.extrabotany.common.attachment.HerrscherEnergy;
import io.github.lounode.extrabotany.common.entity.ThunderSlashEntity;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

/**
 * 雷之律者的能力。佩戴征服宝石的玩家是雷之律者。
 * The abilities of the Herrscher of Thunder. A player that wears the Gem of Conquest is the Herrscher of Thunder.
 *
 * <ul>
 * <li>能量：每 tick 增加 2 点，上限 600。
 * Energy: 2 points on each tick, to a maximum of 600.</li>
 * <li>反击：手持剑受到攻击时，消耗 200 点能量，抵消这次伤害并且反击攻击者。
 * Counterattack: when the player holds a sword and is attacked, 200 energy cancels the damage and the player attacks the attacker.</li>
 * <li>心斩：能量充满时，按住疾跑键并对着空气挥剑，在前方放出一道心斩。它用掉全部能量。
 * Thunder Slash: with full energy, hold the sprint key and swing a sword at the air to release a Thunder Slash ahead. It uses all the energy.</li>
 * </ul>
 *
 * <p>来源：ExtraMeteorP 1.16 {@code HerrscherHandler}。
 * <p>Source: ExtraMeteorP 1.16 {@code HerrscherHandler}.
 *
 * <p>和原版的差异：
 * <p>Differences from the original:
 * <ul>
 * <li>原版只在客户端扣除心斩的能量，服务端不检查能量，并且信任客户端发来的位置。这里由服务端检查能量并且计算位置。
 * The original only removes the energy of the Thunder Slash on the client. Its server does not check the energy and trusts the position from the client. In this port the server checks the energy and calculates the position.</li>
 * <li>原版每 tick 为每个玩家搜索一次饰品栏。这里由征服宝石自己的佩戴 tick 充能。
 * The original searches the accessory slots of each player on each tick. In this port the worn tick of the Gem of Conquest does the charge.</li>
 * </ul>
 */
public final class HerrscherHandler {
	public static final int MAX_ENERGY = HerrscherEnergy.MAX;
	public static final int COUNTER_COST = 200;

	private static final int ENERGY_GAIN = 2;
	private static final int SLASH_HEIGHT = 2;

	private HerrscherHandler() {}

	public static boolean isHerrscherOfThunder(Player player) {
		return !EquipmentHandler.findOrEmpty(ExtraBotanyItems.gemOfConquest, player).isEmpty();
	}

	public static int getEnergy(Player player) {
		return player.getData(ExtraBotanyAttachments.HERRSCHER_ENERGY).get();
	}

	/**
	 * 设置能量并且同步给客户端。只应该在服务端调用。
	 * Sets the energy and synchronizes it to the client. Only the server must call this.
	 */
	public static void setEnergy(Player player, int energy) {
		player.getData(ExtraBotanyAttachments.HERRSCHER_ENERGY).set(energy);
		sync(player);
	}

	private static void sync(Player player) {
		if (!player.level().isClientSide()) {
			player.syncData(ExtraBotanyAttachments.HERRSCHER_ENERGY);
		}
	}

	/**
	 * 充能一个 tick。征服宝石在佩戴时每 tick 调用一次，客户端和服务端都调用。
	 * Charges for one tick. The Gem of Conquest calls this one time on each tick while it is worn, on the client and on the server.
	 */
	public static void charge(Player player) {
		HerrscherEnergy energy = player.getData(ExtraBotanyAttachments.HERRSCHER_ENERGY);
		if (energy.isFull()) {
			return;
		}
		energy.set(energy.get() + ENERGY_GAIN);
		// 充满的那一刻同步一次，这样客户端和服务端对"能否释放心斩"的判断一致。
		// Synchronize one time at the moment of a full charge, so that the client and the server agree on "can the Thunder Slash be used".
		if (energy.isFull()) {
			sync(player);
		}
	}

	/**
	 * 反击。在伤害结算之前调用。
	 * The counterattack. Called before the damage is applied.
	 */
	public static void onIncomingDamage(LivingIncomingDamageEvent event) {
		Entity attacker = event.getSource().getDirectEntity();
		if (attacker == null || !(event.getEntity() instanceof Player player) || player.level().isClientSide()) {
			return;
		}
		// 挥剑动作还没有结束时不能再次反击。这也阻止了反击引起的伤害再次触发反击。
		// A second counterattack is not possible before the swing ends. This also stops damage from the counterattack from starting a counterattack again.
		if (player.swinging || !(player.getMainHandItem().getItem() instanceof SwordItem)) {
			return;
		}
		if (!isHerrscherOfThunder(player)) {
			return;
		}
		int energy = getEnergy(player);
		if (energy < COUNTER_COST && !player.isCreative()) {
			return;
		}

		player.swing(InteractionHand.MAIN_HAND, true);
		player.attack(attacker);
		setEnergy(player, energy - COUNTER_COST);
		event.setCanceled(true);
	}

	/**
	 * 释放心斩。只在服务端调用。
	 * Releases the Thunder Slash. Only the server calls this.
	 *
	 * @return 心斩放出时返回 {@code true}。 {@code true} when the slash was released.
	 */
	public static boolean useSkill(Player player) {
		if (player.level().isClientSide() || player.isSpectator()) {
			return false;
		}
		if (!(player.getMainHandItem().getItem() instanceof SwordItem) || !isHerrscherOfThunder(player)) {
			return false;
		}
		if (getEnergy(player) < MAX_ENERGY && !player.isCreative()) {
			return false;
		}
		setEnergy(player, 0);

		// 位置的算法和原版相同：视线上交互距离处的方块，再向上 2 格。
		// The position calculation is the same as in the original: the block at the interaction range along the view, then 2 blocks up.
		Vec3 target = player.getEyePosition().add(player.getViewVector(1F).scale(player.blockInteractionRange()));
		ThunderSlashEntity slash = new ThunderSlashEntity(player.level(), player);
		slash.setPos(Mth.floor(target.x), Mth.floor(target.y) + SLASH_HEIGHT, Mth.floor(target.z));
		player.level().addFreshEntity(slash);
		return true;
	}
}
