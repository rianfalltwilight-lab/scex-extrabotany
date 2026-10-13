package io.github.lounode.extrabotany.common.item.equipment.bauble;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import vazkii.botania.api.item.BlockChangedListenerBauble;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.item.equipment.bauble.BaubleItem;

import io.github.lounode.extrabotany.common.handler.DamageHandler;
import io.github.lounode.extrabotany.common.util.FrostWalkerHelper;

/**
 * 冰霜之星。佩戴者走过的水面会结冰，周围的敌人会被减速。
 * Frost Star. The water below the wearer freezes, and the bauble slows the enemies near the wearer.
 *
 * <p>减速每秒触发一次。每个目标消耗 30 点魔力。
 * <p>The slow effect occurs one time each second. Each target costs 30 mana.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code ItemFrostStar}。
 * <p>Source: ExtraMeteorP 1.16 {@code ItemFrostStar}.
 */
public class FrostStarItem extends BaubleItem implements BlockChangedListenerBauble {
	private static final int RANGE = 6;
	private static final int MANA_PER_TARGET = 30;
	private static final int INTERVAL_TICKS = 20;
	private static final int FROST_WALKER_LEVEL = 8;
	private static final int SLOW_DURATION_TICKS = 60;
	private static final int SLOW_AMPLIFIER = 4;

	public FrostStarItem(Properties props) {
		super(props);
	}

	@Override
	public void onWornTick(ItemStack stack, LivingEntity entity) {
		super.onWornTick(stack, entity);
		// 先检查间隔，再搜索实体。这样每秒只搜索一次。
		// Check the interval first, then search for entities. Thus the search occurs only one time each second.
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
				// 魔力不足。剩下的目标也无法支付，所以停止。
				// There is not sufficient mana. The remaining targets also cannot be paid for, so stop.
				break;
			}
			target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, SLOW_DURATION_TICKS, SLOW_AMPLIFIER));
		}
	}

	@Override
	public void onChangedBlock(ItemStack stack, LivingEntity entity, ServerLevel level, BlockPos pos) {
		// 原版每 tick 调用一次结冰。1.21 在实体换格时调用，效果相同，开销更小。
		// The original runs the freeze on each tick. 1.21 runs it when the entity changes block. The result is the same and the cost is lower.
		FrostWalkerHelper.freeze(stack, entity, level, FROST_WALKER_LEVEL);
	}

	/**
	 * 判断目标是否对玩家可见。旁观模式的实体对其他玩家不可见。
	 * Finds if the player can see the target. An entity in spectator mode is not visible to other players.
	 */
	private static boolean isVisibleTo(LivingEntity target, Player player) {
		return !(player instanceof ServerPlayer serverPlayer) || target.broadcastToPlayer(serverPlayer);
	}
}