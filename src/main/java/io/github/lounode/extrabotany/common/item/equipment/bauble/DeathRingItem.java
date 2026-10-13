package io.github.lounode.extrabotany.common.item.equipment.bauble;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.item.equipment.bauble.BaubleItem;

import io.github.lounode.extrabotany.common.handler.DamageHandler;

/**
 * 死亡戒指。它让周围的敌人凋零、霉运，并直接扣除生命值。
 * Death Ring. The bauble gives Wither and Bad Luck to the enemies near the wearer and decreases their health directly.
 *
 * <p>效果每 30 tick 触发一次。每个目标消耗 80 点魔力。
 * <p>The effect occurs one time each 30 ticks. Each target costs 80 mana.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code ItemDeathRing}。
 * <p>Source: ExtraMeteorP 1.16 {@code ItemDeathRing}.
 *
 * <p>和原版的差异：原版先扣魔力，再检查间隔。结果是每 tick 都扣魔力，但效果不触发。
 * 这里先检查间隔，所以魔力只在效果触发时扣除。
 * <p>Difference from the original: the original takes the mana first, then checks the interval.
 * As a result the original takes mana on each tick with no effect.
 * This port checks the interval first, so the mana cost only occurs together with the effect.
 */
public class DeathRingItem extends BaubleItem {
	private static final int RANGE = 6;
	private static final int MANA_PER_TARGET = 80;
	private static final int INTERVAL_TICKS = 30;
	private static final int EFFECT_DURATION_TICKS = 60;
	private static final float LIFE_LOSS = 0.5F;

	public DeathRingItem(Properties props) {
		super(props);
	}

	@Override
	public void onWornTick(ItemStack stack, LivingEntity entity) {
		super.onWornTick(stack, entity);
		if (!(entity instanceof Player player) || player.level().isClientSide() || player.tickCount % INTERVAL_TICKS != 0) {
			return;
		}

		AABB area = new AABB(player.blockPosition()).inflate(RANGE);
		for (LivingEntity target : player.level().getEntitiesOfClass(LivingEntity.class, area)) {
			if (target == player
					|| !isVisibleTo(target, player)
					|| !DamageHandler.INSTANCE.checkPassable(target, player)) {
				continue;
			}
			if (!ManaItemHandler.instance().requestManaExactForTool(stack, player, MANA_PER_TARGET, true)) {
				break;
			}
			target.addEffect(new MobEffectInstance(MobEffects.WITHER, EFFECT_DURATION_TICKS, 1));
			target.addEffect(new MobEffectInstance(MobEffects.UNLUCK, EFFECT_DURATION_TICKS, 1));
			DamageHandler.INSTANCE.dmg(target, player, LIFE_LOSS, DamageHandler.DamageKind.LIFE_LOSING);
		}
	}

	/**
	 * 判断目标是否对玩家可见。旁观模式的实体对其他玩家不可见。
	 * Finds if the player can see the target. An entity in spectator mode is not visible to other players.
	 */
	private static boolean isVisibleTo(LivingEntity target, Player player) {
		return !(player instanceof ServerPlayer serverPlayer) || target.broadcastToPlayer(serverPlayer);
	}
}