package io.github.lounode.extrabotany.common.handler;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import io.github.lounode.extrabotany.common.attachment.ExtraBotanyAttachments;
import io.github.lounode.extrabotany.common.attachment.FlamescionState;
import io.github.lounode.extrabotany.common.brew.ExtraBotanyMobEffects;
import io.github.lounode.extrabotany.common.entity.flamescion.FlamescionSlashEntity;
import io.github.lounode.extrabotany.common.entity.flamescion.FlamescionUltEntity;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

/**
 * 薪炎的规则。
 * The rules of the Flamescion.
 *
 * <p>薪炎模式：玩家在空中、主手拿着薪炎之钥、有"灼热模式"效果，并且没有过载。
 * <p>The Flamescion mode: the player is in the air, has the Key of Flamescion in the main hand, has the Incandescence Mode effect, and has no overload.
 *
 * <p>能量：在薪炎模式下每 tick 增加 2 点。到达 600 点时过载。过载时每 tick 减少 3 点，降到零时过载结束。
 * <p>Energy: 2 points on each tick in the Flamescion mode. An overload starts at 600 points. During an overload the energy goes down 3 points on each tick, and the overload ends at zero.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code FlamescionHandler}、{@code FlamescionShiftPack} 和 {@code FlamescionQPack}。
 * <p>Source: ExtraMeteorP 1.16 {@code FlamescionHandler}, {@code FlamescionShiftPack} and {@code FlamescionQPack}.
 *
 * <p>和原版的差异：
 * <p>Differences from the original:
 * <ul>
 * <li>原版的服务端不检查冲刺和终结技的条件，只有客户端检查。这里服务端也检查。
 * The server of the original does not check the conditions of the dash and of the ultimate. Only the client checks. In this port the server also checks.</li>
 * <li>原版的冲刺直接把玩家传送 4 格，可以把玩家送进方块里。这里在终点有方块时缩短距离。
 * The dash of the original moves the player 4 blocks with no check, and can put the player into a block. This port makes the distance shorter when a block is at the end point.</li>
 * <li>火焰伤害把玩家记为攻击者，所以 PvP 规则和和平友好之证都生效。
 * The flame damage records the player as the attacker, so the PvP rules and the Peace Amulet apply.</li>
 * <li>原版在能量变化的每个 tick 都发送同步数据包。这里客户端自己计算，服务端只在过载状态改变时和每 10 tick 同步一次。
 * The original sends a synchronization packet on each tick that the energy changes. In this port the client calculates the energy itself, and the server only synchronizes when the overload state changes and on each 10th tick.</li>
 * </ul>
 */
public final class FlamescionHandler {
	public static final int MAX_ENERGY = FlamescionState.MAX_ENERGY;

	private static final int ENERGY_GAIN = 2;
	private static final int OVERLOAD_DRAIN = 3;
	private static final int SYNC_INTERVAL_TICKS = 10;

	private static final double DASH_DISTANCE = 4.0;
	private static final double DASH_STEP = 0.5;
	private static final float DASH_RANGE = 8F;
	private static final double DASH_HIT_WIDTH = 4.0;
	private static final float DASH_DAMAGE = 6F;
	private static final int DASH_COOLDOWN_TICKS = 20;
	private static final double ULT_DISTANCE = 5.0;

	// 旧版本把状态存在玩家的持久数据里。登录时迁移一次。
	// Older versions kept the state in the persistent data of the player. It is migrated one time at login.
	private static final String LEGACY_STATE_KEY = "extrabotany:flamescion";

	private FlamescionHandler() {}

	public static FlamescionState getState(Player player) {
		return player.getData(ExtraBotanyAttachments.FLAMESCION_STATE);
	}

	public static boolean isOverloaded(Player player) {
		return getState(player).isOverloaded();
	}

	public static int getEnergy(Player player) {
		return getState(player).getEnergy();
	}

	public static boolean isFlamescionMode(Player player) {
		return !player.onGround()
				&& player.getMainHandItem().is(ExtraBotanyItems.flamescionWeapon)
				&& player.hasEffect(ExtraBotanyMobEffects.INCANDESCENCE)
				&& !isOverloaded(player);
	}

	private static void sync(Player player) {
		if (!player.level().isClientSide()) {
			player.syncData(ExtraBotanyAttachments.FLAMESCION_STATE);
		}
	}

	/**
	 * 每 tick 更新能量和过载状态。客户端和服务端都调用。
	 * Updates the energy and the overload state on each tick. The client and the server call this.
	 */
	public static void tickPlayer(Player player) {
		FlamescionState state = getState(player);
		boolean inMode = isFlamescionMode(player);
		if (!inMode && !state.isOverloaded()) {
			return;
		}

		int energy = state.getEnergy();
		boolean wasOverloaded = state.isOverloaded();
		if (inMode) {
			if (energy < MAX_ENERGY) {
				state.setEnergy(energy + ENERGY_GAIN);
			} else {
				state.setOverloaded(true);
			}
		}
		if (state.isOverloaded()) {
			if (energy > 0) {
				state.setEnergy(energy - OVERLOAD_DRAIN);
			} else {
				state.setOverloaded(false);
			}
		}
		if (state.isOverloaded() != wasOverloaded || player.tickCount % SYNC_INTERVAL_TICKS == 0) {
			sync(player);
		}
	}

	/**
	 * 薪炎模式下的近战攻击命中时，在目标身上放出一道斩击。在伤害结算之前调用。
	 * When a melee attack hits in the Flamescion mode, releases a slash on the target. Called before the damage is applied.
	 */
	public static void onIncomingDamage(LivingIncomingDamageEvent event) {
		LivingEntity target = event.getEntity();
		// 只有玩家亲手造成的伤害才触发。薪炎实体造成的伤害没有直接来源，所以不会再次触发这里。
		// Only damage that the player does directly starts this. Damage from the Flamescion entities has no direct source, so it does not start this again.
		if (target.level().isClientSide() || !(event.getSource().getDirectEntity() instanceof Player player)
				|| event.getSource().getEntity() != player) {
			return;
		}
		if (!isFlamescionMode(player)) {
			return;
		}
		FlamescionSlashEntity slash = new FlamescionSlashEntity(player.level(), player);
		slash.setPos(target.getX(), target.getY() + 1.0, target.getZ());
		player.level().addFreshEntity(slash);
		player.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.INCANDESCENCE, 30));
		target.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.TIMELOCK, 30));
	}

	/**
	 * 冲刺：沿视线方向移动 4 格，并对路径附近的生物造成 6 点火焰伤害。只在服务端调用。
	 * The dash: moves 4 blocks along the view and does 6 flame damage to the mobs near the path. Only the server calls this.
	 *
	 * <p>命中生物时，玩家得到"薪炎强化"效果，下一次挥剑放出强化斩击。
	 * <p>When the dash hits a mob, the player gets the Flamescion Strength effect, and the next swing releases the Strengthened Slashes.
	 *
	 * @return 冲刺发生时返回 {@code true}。 {@code true} when the dash occurred.
	 */
	public static boolean dash(Player player) {
		if (player.level().isClientSide() || player.isSpectator() || !isFlamescionMode(player)
				|| player.getCooldowns().isOnCooldown(ExtraBotanyItems.flamescionWeapon)) {
			return false;
		}
		Vec3 start = player.position();
		Vec3 look = player.getLookAngle().normalize();
		Vec3 fullMove = look.scale(DASH_DISTANCE);
		Vec3 end = start.add(findFreeDashMove(player, look));
		player.teleportTo(end.x, end.y, end.z);

		// 命中判定用完整的 4 格路径，和原版相同：路径前后各延长 4 格，生物的碰撞箱放大 4 格。
		// The hit check uses the full path of 4 blocks, as in the original: the path is made 4 blocks longer at each end, and the collision box of a mob is made 4 blocks larger.
		Vec3 fullEnd = start.add(fullMove);
		Vec3 rayStart = start.subtract(fullMove);
		Vec3 rayEnd = fullEnd.add(fullMove);
		boolean hit = false;
		AABB area = new AABB(BlockPos.containing(fullEnd)).inflate(DASH_RANGE - 0.5);
		for (LivingEntity living : player.level().getEntitiesOfClass(LivingEntity.class, area)) {
			if (living == player || living.getBoundingBox().inflate(DASH_HIT_WIDTH).clip(rayStart, rayEnd).isEmpty()) {
				continue;
			}
			if (!DamageHandler.INSTANCE.checkPassable(living, player)) {
				continue;
			}
			living.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.TIMELOCK, 40));
			DamageHandler.INSTANCE.flameAttack(living, player, DASH_DAMAGE);
			hit = true;
		}
		if (hit) {
			player.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.INCANDESCENCE, 80));
			player.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.FLAMESCION, 200));
		}
		player.getCooldowns().addCooldown(ExtraBotanyItems.flamescionWeapon, DASH_COOLDOWN_TICKS);
		return true;
	}

	/**
	 * 找到不会让玩家卡进方块的最远冲刺距离。
	 * Finds the longest dash that does not put the player into a block.
	 */
	private static Vec3 findFreeDashMove(Player player, Vec3 look) {
		for (double distance = DASH_DISTANCE; distance > 0.0; distance -= DASH_STEP) {
			Vec3 move = look.scale(distance);
			if (player.level().noCollision(player, player.getBoundingBox().move(move))) {
				return move;
			}
		}
		return Vec3.ZERO;
	}

	/**
	 * 终结技：在前方 5 格放出薪炎的终结技，并且立刻进入过载。只在服务端调用。
	 * The ultimate: releases the ultimate of the Flamescion 5 blocks ahead and starts an overload immediately. Only the server calls this.
	 *
	 * @return 终结技放出时返回 {@code true}。 {@code true} when the ultimate was released.
	 */
	public static boolean useUltimate(Player player) {
		if (player.level().isClientSide() || player.isSpectator() || !isFlamescionMode(player)) {
			return false;
		}
		FlamescionState state = getState(player);
		state.setEnergy(MAX_ENERGY);
		state.setOverloaded(true);
		sync(player);

		Vec3 look = player.getLookAngle().normalize().scale(ULT_DISTANCE);
		FlamescionUltEntity ult = new FlamescionUltEntity(player.level(), player);
		ult.setPos(player.getX() + look.x, player.getY() + 0.25, player.getZ() + look.z);
		player.level().addFreshEntity(ult);
		player.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.TIMELOCK, 40));
		return true;
	}

	/**
	 * 把旧版本保存在玩家持久数据里的状态迁移到数据附加里。玩家登录时调用。
	 * Moves the state that older versions kept in the persistent data of the player to the data attachment. Called when a player logs in.
	 */
	public static void migrateLegacyState(Player player) {
		CompoundTag data = player.getPersistentData();
		if (!data.contains(LEGACY_STATE_KEY, Tag.TAG_COMPOUND)) {
			return;
		}
		CompoundTag legacy = data.getCompound(LEGACY_STATE_KEY);
		FlamescionState state = getState(player);
		state.setEnergy(legacy.getInt("energy"));
		state.setOverloaded(legacy.getBoolean("overloaded"));
		data.remove(LEGACY_STATE_KEY);
		sync(player);
	}
}
