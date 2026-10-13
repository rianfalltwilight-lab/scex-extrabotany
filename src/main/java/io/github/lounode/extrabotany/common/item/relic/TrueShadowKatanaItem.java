package io.github.lounode.extrabotany.common.item.relic;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import vazkii.botania.api.mana.ManaItemHandler;

import io.github.lounode.extrabotany.common.entity.projectile.TrueShadowKatanaProjectileEntity;
import io.github.lounode.extrabotany.common.handler.DamageHandler;

import java.util.List;

/**
 * 真影刃。左键消耗 800 点魔力，在身前召唤三把飞刃，飞向附近的敌人。
 * True Shadow Katana. The attack key uses 800 mana and summons three flying blades in front of the player. The blades fly to an enemy near the player.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code ItemTrueShadowKatana}。
 * <p>Source: ExtraMeteorP 1.16 {@code ItemTrueShadowKatana}.
 *
 * <p>和原版的差异：原版在没有选中目标时取范围内的第一个实体，可能是玩家自己或者不能攻击的生物。
 * 这里只从可以攻击的实体里选。
 * <p>Difference from the original: with no selected target, the original takes the first entity in range, which can be the player or a mob that cannot be attacked.
 * This port only selects from the entities that can be attacked.
 */
public class TrueShadowKatanaItem extends RelicSwordItem {
	public static final int MANA_PER_USE = 800;
	private static final double AIM_RANGE = 64;
	private static final double SEARCH_RANGE = 8;
	private static final double BLADE_SPEED = 0.75;
	private static final int BLADE_COUNT = 3;
	private static final double FORWARD_DISTANCE = 1.75;
	private static final double ARC_RADIUS = 2;
	private static final double ARC_STEP_RADIANS = Math.PI / 4;

	public TrueShadowKatanaItem(Properties props) {
		super(Tiers.DIAMOND, props.attributes(createAttributes(Tiers.DIAMOND, 5, -2F)));
	}

	@Override
	protected boolean useSkill(Player player, ItemStack stack, @Nullable Entity target) {
		if (!ManaItemHandler.instance().requestManaExactForTool(stack, player, MANA_PER_USE, true)) {
			return false;
		}
		attackEntity(player, target);
		return true;
	}

	/**
	 * 召唤飞刃。不消耗魔力。Boss 也用这个方法攻击。
	 * Summons the blades. This method takes no mana. A boss also attacks with this method.
	 */
	public void attackEntity(LivingEntity user, @Nullable Entity target) {
		Vec3 aim = selectAim(user, target);

		Vec3 look = user.getLookAngle().multiply(1, 0, 1);
		if (look.lengthSqr() < 1.0E-8) {
			look = Vec3.directionFromRotation(0, user.getYRot());
		}
		Vec3 forward = look.normalize();
		Vec3 base = user.position()
				.add(0, user.getBbHeight() / 2 + 0.1, 0)
				.add(forward.scale(FORWARD_DISTANCE));

		for (int i = 0; i < BLADE_COUNT; i++) {
			// 三把飞刃排成一道弧：左上、正上、右上。
			// The three blades make an arc: upper left, straight up, upper right.
			double angle = (i - 1) * ARC_STEP_RADIANS;
			Vector3d offset = new Vector3d(0, ARC_RADIUS, 0).rotateAxis(angle, forward.x, forward.y, forward.z);
			Vec3 start = base.add(offset.x, Math.abs(offset.y), offset.z);
			user.level().addFreshEntity(
					new TrueShadowKatanaProjectileEntity(user.level(), user, start, aim, BLADE_SPEED));
		}
	}

	/**
	 * 选择飞刃的目标点。优先级：生物当前的攻击目标，使用者最近攻击的敌人，8 格内的任意敌人，最后是准星指向的位置。
	 * Selects the aim of the blades. The priority: the attack target of a mob, the enemy that the user attacked last, an enemy within 8 blocks, then the position at the crosshair.
	 */
	private static Vec3 selectAim(LivingEntity user, @Nullable Entity target) {
		AABB area = new AABB(user.position(), user.position()).inflate(SEARCH_RANGE);
		List<LivingEntity> enemies = DamageHandler.INSTANCE.getFilteredEntities(
				user.level().getEntitiesOfClass(LivingEntity.class, area), user);
		if (enemies.isEmpty()) {
			return aimPosition(user, target, AIM_RANGE, true);
		}
		if (user instanceof Mob mob && mob.getTarget() != null && enemies.contains(mob.getTarget())) {
			return mob.getTarget().position();
		}
		LivingEntity lastAttacked = user.getLastHurtMob();
		if (lastAttacked != null && enemies.contains(lastAttacked)) {
			return lastAttacked.position();
		}
		return enemies.getFirst().position();
	}
}
