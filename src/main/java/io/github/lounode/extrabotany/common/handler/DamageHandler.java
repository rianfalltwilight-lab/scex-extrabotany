package io.github.lounode.extrabotany.common.handler;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.Tags;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.common.handler.EquipmentHandler;

import io.github.lounode.extrabotany.common.ExtraBotanyDamageTypes;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 统一的伤害入口。所有来自本模组物品和实体的伤害都必须经过这里。
 * The common entry point for damage. All damage from the items and entities of this mod must go through this class.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code DamageHandler}。
 * <p>Source: ExtraMeteorP 1.16 {@code DamageHandler}.
 *
 * <p>重制版把 {@code dmg} 的实现注释掉了。这个版本恢复了原版的五种伤害。
 * <p>Reburn disabled the body of {@code dmg}. This version restores the five damage kinds of the original.
 */
public final class DamageHandler {
	public static final DamageHandler INSTANCE = new DamageHandler();

	private DamageHandler() {}

	/**
	 * 原版的五种伤害。
	 * The five damage kinds of the original.
	 */
	public enum DamageKind {
		/**
		 * 普通攻击伤害。护甲和附魔可以减免。
		 * Normal attack damage. Armor and enchantments decrease the damage.
		 */
		NEUTRAL,
		/**
		 * 魔法伤害。护甲不能减免。
		 * Magic damage. Armor does not decrease the damage.
		 */
		MAGIC,
		/**
		 * 穿透攻击伤害。它无视护甲、药水效果、附魔和无敌帧。
		 * Piercing attack damage. The damage ignores armor, effects, enchantments and the invulnerability time.
		 */
		NEUTRAL_PIERCING,
		/**
		 * 穿透魔法伤害。它无视护甲、药水效果、附魔和无敌帧。
		 * Piercing magic damage. The damage ignores armor, effects, enchantments and the invulnerability time.
		 */
		MAGIC_PIERCING,
		/**
		 * 直接扣除生命值。目标最少保留 1 点生命值。
		 * A direct decrease of health. The target keeps a minimum of 1 health point.
		 */
		LIFE_LOSING
	}

	/**
	 * 判断攻击者能否伤害目标。
	 * Finds if the source can damage the target.
	 *
	 * @param target 受到伤害的实体。 The entity that receives the damage.
	 * @param source 造成伤害的实体。可以是 {@code null}。 The entity that causes the damage. The value can be {@code null}.
	 * @return 可以伤害时返回 {@code true}。 {@code true} when the damage is permitted.
	 */
	public boolean checkPassable(@Nullable Entity target, @Nullable Entity source) {
		if (target == null || !target.isAlive() || target.isSpectator()) {
			return false;
		}
		// 创造模式的玩家不受伤害。直接扣血会绕过原版的检查，所以这里必须先拦截。
		// A player in creative mode takes no damage. A direct health decrease bypasses the vanilla check, so this check must come first.
		if (target instanceof Player targetPlayer
				&& (targetPlayer.isCreative() || targetPlayer.getAbilities().invulnerable)) {
			return false;
		}
		if (target == source) {
			return false;
		}

		if (source instanceof Player sourcePlayer) {
			if (isBlockedByPeaceAmulet(target, sourcePlayer)) {
				return false;
			}
			if (target instanceof Player targetPlayer && !sourcePlayer.canHarmPlayer(targetPlayer)) {
				return false;
			}
		}

		// 怪物的攻击只对玩家有效。这样 Boss 的范围攻击不会伤到它的同伴。
		// An attack from a monster only applies to players. Thus an area attack of a boss does not hurt its allies.
		if (source instanceof Enemy) {
			return target instanceof Player;
		}

		return true;
	}

	/**
	 * 从列表中筛选出攻击者可以伤害的实体。
	 * Filters the list and keeps the entities that the source can damage.
	 */
	public List<LivingEntity> getFilteredEntities(List<LivingEntity> entities, @Nullable Entity source) {
		return entities.stream()
				.filter(living -> !living.isRemoved())
				.filter(living -> checkPassable(living, source))
				.collect(Collectors.toList());
	}

	/**
	 * 原版预留的伤害加成入口。目前不修改数值。
	 * The damage bonus hook that the original reserves. At this time the method does not change the value.
	 */
	public static float calcDamage(float origin, @Nullable Player player) {
		return origin;
	}

	/**
	 * 对目标造成指定种类的伤害。只在服务端生效。
	 * Applies damage of the given kind to the target. The method only has an effect on the server.
	 *
	 * @param target 受到伤害的实体。 The entity that receives the damage.
	 * @param source 造成伤害的实体。可以是 {@code null}。 The entity that causes the damage. The value can be {@code null}.
	 * @param amount 伤害数值。 The amount of damage.
	 * @param kind   伤害种类。 The kind of damage.
	 * @return 伤害生效时返回 {@code true}。 {@code true} when the damage was applied.
	 */
	public boolean dmg(@Nullable Entity target, @Nullable Entity source, float amount, DamageKind kind) {
		if (target == null || target.level().isClientSide()) {
			return false;
		}
		if (!Float.isFinite(amount) || amount <= 0 || !checkPassable(target, source)) {
			return false;
		}

		DamageSources sources = target.damageSources();
		return switch (kind) {
			case NEUTRAL -> target.hurt(attackSource(sources, source), amount);
			case MAGIC -> target.hurt(magicSource(sources, source), amount);
			case NEUTRAL_PIERCING -> {
				target.invulnerableTime = 0;
				// 原版在攻击者不是生物时退回普通伤害。这里保持一致。
				// The original falls back to generic damage when the source is not a living entity. This port does the same.
				DamageSource damageSource = source instanceof LivingEntity
						? ExtraBotanyDamageTypes.Sources.piercingDamage(target.level().registryAccess(), source)
						: sources.generic();
				yield target.hurt(damageSource, amount);
			}
			case MAGIC_PIERCING -> {
				target.invulnerableTime = 0;
				yield target.hurt(
						ExtraBotanyDamageTypes.Sources.magicPiercingDamage(target.level().registryAccess(), source),
						amount);
			}
			case LIFE_LOSING -> {
				if (!(target instanceof LivingEntity living)) {
					yield false;
				}
				living.setHealth(Math.max(1F, living.getHealth() - amount));
				// 追加一次极小的普通伤害。它触发受击动画，并把攻击者记到目标身上。
				// Add a very small normal hit. The hit starts the hurt animation and records the attacker on the target.
				yield dmg(target, source, 0.01F, DamageKind.NEUTRAL);
			}
		};
	}

	/**
	 * 造成真实伤害：直接扣除生命值，并且可以杀死目标。Boss 只受到一半的真实伤害。
	 * Applies true damage: a direct health decrease that can kill the target. A boss only takes half of the true damage.
	 *
	 * <p>这是 1.12 的真实伤害。它和 {@link DamageKind#LIFE_LOSING} 不同：后者最少保留 1 点生命值。
	 * <p>This is the true damage of 1.12. It is different from {@link DamageKind#LIFE_LOSING}, which keeps a minimum of 1 health point.
	 *
	 * <p>来源：ExtraMeteorP 1.12 {@code ExtraBotanyAPI#dealTrueDamage}。
	 * <p>Source: ExtraMeteorP 1.12 {@code ExtraBotanyAPI#dealTrueDamage}.
	 *
	 * @return 实际扣除的生命值。 The health that was removed.
	 */
	public float dealTrueDamage(@Nullable Entity attacker, @Nullable LivingEntity target, float amount) {
		if (target == null || target.level().isClientSide() || !target.isAlive()) {
			return 0;
		}
		if (!Float.isFinite(amount) || amount < 0 || !checkPassable(target, attacker)) {
			return 0;
		}

		DamageSource source = ExtraBotanyDamageTypes.Sources.magicPiercingDamage(target.level().registryAccess(), attacker);
		// 先造成一次极小的伤害。它触发受击动画，并把攻击者记到目标身上。
		// Apply a very small hit first. The hit starts the hurt animation and records the attacker on the target.
		target.hurt(source, 0.01F);

		float health = target.getHealth();
		if (health <= 0) {
			return 0;
		}
		if (isBoss(target)) {
			amount *= 0.5F;
		}
		float healthAfter = Math.max(1F, health - amount);
		target.setHealth(healthAfter);
		if (health <= amount) {
			if (target instanceof Player) {
				target.kill();
			} else {
				target.invulnerableTime = 0;
				target.hurt(source, Float.MAX_VALUE);
			}
		}
		return health - healthAfter;
	}

	/**
	 * 造成律者的冰伤害。它无视护甲、药水效果和附魔，没有击退，并且遵守无敌帧。只在服务端生效。
	 * Applies the ice damage of the Herrscher. The damage ignores armor, effects and enchantments, has no knockback, and obeys the invulnerability time. The method only has an effect on the server.
	 *
	 * <p>来源：ExtraMeteorP 1.16 {@code HerrscherHandler#iceAttack}。
	 * <p>Source: ExtraMeteorP 1.16 {@code HerrscherHandler#iceAttack}.
	 *
	 * <p>和原版的差异：原版的伤害没有攻击者。这里把玩家记为攻击者，所以 PvP 规则和和平友好之证都生效，击杀也归玩家。
	 * <p>Difference from the original: the damage of the original has no attacker. This port records the player as the attacker, so the PvP rules and the Peace Amulet apply, and the player gets the kill.
	 *
	 * @return 伤害生效时返回 {@code true}。 {@code true} when the damage was applied.
	 */
	public boolean iceAttack(@Nullable Entity target, @Nullable Player attacker, float amount) {
		return herrscherAttack(target, attacker, amount, ExtraBotanyDamageTypes.HERRSCHER_ICE_DAMAGE);
	}

	/**
	 * 造成律者的雷伤害。规则和 {@link #iceAttack} 相同，只是死亡消息不同。
	 * Applies the thunder damage of the Herrscher. The rules are the same as for {@link #iceAttack}. Only the death message is different.
	 *
	 * <p>来源：ExtraMeteorP 1.16 {@code HerrscherHandler#thunderAttack}。
	 * <p>Source: ExtraMeteorP 1.16 {@code HerrscherHandler#thunderAttack}.
	 *
	 * @return 伤害生效时返回 {@code true}。 {@code true} when the damage was applied.
	 */
	public boolean thunderAttack(@Nullable Entity target, @Nullable Player attacker, float amount) {
		return herrscherAttack(target, attacker, amount, ExtraBotanyDamageTypes.HERRSCHER_THUNDER_DAMAGE);
	}

	private boolean herrscherAttack(@Nullable Entity target, @Nullable Player attacker, float amount, ResourceKey<DamageType> type) {
		if (target == null || target.level().isClientSide()) {
			return false;
		}
		if (!Float.isFinite(amount) || amount <= 0 || !checkPassable(target, attacker)) {
			return false;
		}
		return target.hurt(ExtraBotanyDamageTypes.Sources.withoutDirectEntity(target.level().registryAccess(), type, attacker), amount);
	}

	/**
	 * 造成薪炎的火焰伤害。它无视护甲、药水效果、附魔和无敌帧，没有击退。只在服务端生效。
	 * Applies the flame damage of the Flamescion. The damage ignores armor, effects, enchantments and the invulnerability time, and has no knockback. The method only has an effect on the server.
	 *
	 * <p>它不是原版意义上的"火焰伤害"，所以抗火的生物也会受伤。
	 * <p>It is not "fire damage" in the vanilla sense, so mobs that resist fire also take the damage.
	 *
	 * <p>来源：ExtraMeteorP 1.16 {@code FlamescionHandler#flameSource}。原版在每次伤害之前清除目标的无敌帧。
	 * <p>Source: ExtraMeteorP 1.16 {@code FlamescionHandler#flameSource}. The original clears the invulnerability time of the target before each hit.
	 *
	 * @return 伤害生效时返回 {@code true}。 {@code true} when the damage was applied.
	 */
	public boolean flameAttack(@Nullable Entity target, @Nullable Player attacker, float amount) {
		if (target == null || target.level().isClientSide()) {
			return false;
		}
		if (!Float.isFinite(amount) || amount <= 0 || !checkPassable(target, attacker)) {
			return false;
		}
		target.invulnerableTime = 0;
		return target.hurt(ExtraBotanyDamageTypes.Sources.withoutDirectEntity(
				target.level().registryAccess(), ExtraBotanyDamageTypes.FLAMESCION_FLAME_DAMAGE, attacker), amount);
	}

	/**
	 * 造成穿透火焰伤害。它无视护甲、药水效果、附魔和无敌帧，没有击退。抗火的生物免疫这种伤害。只在服务端生效。
	 * Applies piercing fire damage. The damage ignores armor, effects, enchantments and the invulnerability time, and has no knockback. Mobs that resist fire are immune to it. The method only has an effect on the server.
	 *
	 * <p>来源：ExtraMeteorP 1.12 的樱花的誓约使用"无视护甲的熔岩伤害"。原版直接修改了全局的熔岩伤害对象。
	 * <p>Source: the Oath of Sakura of ExtraMeteorP 1.12 uses "lava damage that ignores armor". The original changes the global lava damage object.
	 *
	 * @return 伤害生效时返回 {@code true}。 {@code true} when the damage was applied.
	 */
	public boolean fireAttack(@Nullable Entity target, @Nullable Entity attacker, float amount) {
		if (target == null || target.level().isClientSide()) {
			return false;
		}
		if (!Float.isFinite(amount) || amount <= 0 || !checkPassable(target, attacker)) {
			return false;
		}
		target.invulnerableTime = 0;
		return target.hurt(ExtraBotanyDamageTypes.Sources.withoutDirectEntity(
				target.level().registryAccess(), ExtraBotanyDamageTypes.PIERCING_FIRE_DAMAGE, attacker), amount);
	}

	private static DamageSource attackSource(DamageSources sources, @Nullable Entity source) {
		if (source instanceof Player player) {
			return sources.playerAttack(player);
		}
		if (source instanceof LivingEntity living) {
			return sources.mobAttack(living);
		}
		return sources.generic();
	}

	private static DamageSource magicSource(DamageSources sources, @Nullable Entity source) {
		return source == null ? sources.magic() : sources.indirectMagic(source, source);
	}

	/**
	 * 判断和平友好之证是否阻止这次伤害。
	 * Finds if a Peace Amulet stops this damage.
	 *
	 * <p>规则一：攻击者或者被攻击的玩家佩戴护符时，玩家之间的伤害无效。
	 * <p>Rule 1: when the attacker or the attacked player wears the amulet, damage between players has no effect.
	 *
	 * <p>规则二：佩戴护符的玩家只能伤害怪物和 Boss。
	 * <p>Rule 2: a player with the amulet can only damage monsters and bosses.
	 */
	public boolean isBlockedByPeaceAmulet(Entity target, Player attacker) {
		boolean attackerIsPeaceful = hasPeaceAmulet(attacker);
		if (target instanceof Player targetPlayer) {
			return attackerIsPeaceful || hasPeaceAmulet(targetPlayer);
		}
		return attackerIsPeaceful && !(target instanceof Enemy) && !isBoss(target);
	}

	private static boolean hasPeaceAmulet(Player player) {
		return !EquipmentHandler.findOrEmpty(ExtraBotanyItems.peaceAmulet, player).isEmpty();
	}

	private static boolean isBoss(Entity entity) {
		return entity.getType().is(Tags.EntityTypes.BOSSES);
	}
}
