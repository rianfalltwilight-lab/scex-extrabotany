package io.github.lounode.extrabotany.common.entity;

import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;


import io.github.lounode.extrabotany.common.handler.DamageHandler;
import io.github.lounode.extrabotany.common.sounds.ExtraBotanySounds;

import java.util.List;

/**
 * 心斩。雷之律者的技能。它停在原地 35 tick，减速范围内的怪物，并且分五次造成雷伤害。
 * Thunder Slash. The skill of the Herrscher of Thunder. It stays in place for 35 ticks, slows the monsters in range, and does thunder damage five times.
 *
 * <p>伤害：第 6、15、23 tick 各 3 点，第 27、34 tick 各 6 点。范围是 3.5 格。每次伤害之后清除目标的无敌帧。
 * <p>Damage: 3 on ticks 6, 15 and 23, and 6 on ticks 27 and 34. The range is 3.5 blocks. After each hit the invulnerability time of the target is cleared.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code EntitySlash}。
 * <p>Source: ExtraMeteorP 1.16 {@code EntitySlash}.
 *
 * <p>和原版的差异：原版不保存主人，所以读档之后心斩不再造成伤害。这里保存主人的 UUID。
 * <p>Difference from the original: the original does not save the owner, so the slash does no damage after a load. This port saves the UUID of the owner.
 */
public class ThunderSlashEntity extends PlayerOwnedEntity {
	public static final int LIFETIME_TICKS = 35;

	private static final float RANGE = 3.5F;
	private static final float DAMAGE = 3F;
	private static final int SLOWNESS_TICKS = 2;
	private static final int SLOWNESS_LEVEL = 3;


	public ThunderSlashEntity(EntityType<? extends ThunderSlashEntity> type, Level level) {
		super(type, level);
	}

	public ThunderSlashEntity(Level level, Player owner) {
		this(ExtraBotanyEntityType.THUNDER_SLASH, level);
		setOwner(owner);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {}


	@Override
	public void tick() {
		super.tick();
		if (level().isClientSide()) {
			return;
		}
		if (tickCount == 1) {
			playSound(ExtraBotanySounds.THUNDER_SLASH, 1.2F, 1F);
		}

		List<LivingEntity> inRange = level().getEntitiesOfClass(LivingEntity.class, new AABB(blockPosition()).inflate(RANGE - 0.5));
		for (LivingEntity living : inRange) {
			if (living instanceof Enemy) {
				living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, SLOWNESS_TICKS, SLOWNESS_LEVEL));
			}
		}

		float damage = switch (tickCount) {
			case 6, 15, 23 -> DAMAGE;
			case 27, 34 -> DAMAGE * 2F;
			default -> 0F;
		};
		Player attacker = damage > 0F ? getOwner() : null;
		if (attacker != null) {
			for (LivingEntity living : inRange) {
				if (living == attacker || !DamageHandler.INSTANCE.checkPassable(living, attacker)) {
					continue;
				}
				DamageHandler.INSTANCE.thunderAttack(living, attacker, damage);
				// 清除无敌帧，这样下一次雷伤害一定生效。
				// Clear the invulnerability time, so that the next thunder hit always applies.
				living.invulnerableTime = 0;
			}
		}
		if (tickCount >= LIFETIME_TICKS) {
			discard();
		}
	}
}
