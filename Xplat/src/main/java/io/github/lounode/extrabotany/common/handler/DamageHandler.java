package io.github.lounode.extrabotany.common.handler;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.stream.Collectors;

public class DamageHandler {
	public static final DamageHandler INSTANCE = new DamageHandler();

	public boolean checkPassable(Entity target, Entity source) {
		if (target == source) {
			return false;
		}
		if (source instanceof Player player) {
			boolean peaceful = io.github.lounode.extrabotany.common.item.legacy.LegacyAccessories.worn("peace_amulet", player);
			if (target instanceof Player targetPlayer) return !peaceful
					&& !io.github.lounode.extrabotany.common.item.legacy.LegacyAccessories.worn("peace_amulet", targetPlayer);
			if (peaceful && target instanceof Mob && !(target instanceof net.minecraft.world.entity.monster.Enemy)) return false;
		}

		if (source instanceof Mob) {
			return target instanceof Player;
		}

		return true;
	}

	public List<LivingEntity> getFilteredEntities(List<LivingEntity> entities, Entity source) {
		List<LivingEntity> list = entities.stream()
				.filter(living -> !living.isRemoved())
				.filter(living -> checkPassable(living, source))
				.collect(Collectors.toList());
		return list;
	}

	public static float calcDamage(float origin, Player player) {
		if (player == null) {
			return origin;
		}

		double value = 0F;
		return (float) (origin + value);
	}

	public boolean dmg(Entity target, Entity source, float amount, DamageType type) {
		if (target == null || !checkPassable(target, source)) {
			return false;
		}
		/*
		switch (type){
			case NETURAL: {
				if (source instanceof Player) {
					Player player = (Player) source;
					DamageSource s = DamageSource.causePlayerDamage(player);
					return target.attackEntityFrom(s, amount);
				} else if (source instanceof LivingEntity) {
					LivingEntity living = (LivingEntity) source;
					DamageSource s = DamageSource.causeMobDamage(living);
					return target.attackEntityFrom(s, amount);
				} else {
					return target.attackEntityFrom(DamageSource.GENERIC, amount);
				}
			}
			case MAGIC: {
				if(source == null){
					DamageSource s = DamageSource.MAGIC;
					return target.attackEntityFrom(s, amount);
				}else{
					DamageSource s = DamageSource.causeIndirectMagicDamage(source, source);
					return target.attackEntityFrom(s, amount);
				}
			}
			case NETURAL_PIERCING: {
				target.hurtResistantTime=0;
				if (source instanceof Player) {
					Player player = (Player) source;
					DamageSource s = DamageSource.causePlayerDamage(player).setDamageBypassesArmor().setDamageIsAbsolute();
					return target.attackEntityFrom(s, amount);
				} else if (source instanceof LivingEntity) {
					LivingEntity living = (LivingEntity) source;
					DamageSource s = DamageSource.causeMobDamage(living).setDamageBypassesArmor().setDamageIsAbsolute();
					return target.attackEntityFrom(s, amount);
				} else {
					return target.attackEntityFrom(DamageSource.GENERIC, amount);
				}
			}
			case MAGIC_PIERCING: {
				target.hurtResistantTime=0;
				if(source == null){
					DamageSource s = DamageSource.MAGIC.setDamageBypassesArmor().setDamageIsAbsolute();
					return target.attackEntityFrom(s, amount);
				}else{
					DamageSource s = DamageSource.causeIndirectMagicDamage(source, source).setDamageBypassesArmor().setDamageIsAbsolute();
					return target.attackEntityFrom(s, amount);
				}
			}
			case LIFE_LOSING:{
				if(!(target instanceof LivingEntity))
					return false;
				LivingEntity living = (LivingEntity) target;
				float currentHealth = living.getHealth();
				float trueHealth = Math.max(1F, currentHealth - amount);
				living.setHealth(trueHealth);
				return dmg(target, source, 0.01F, NETURAL);
			}
		}
		*/
		return false;
	}

	public enum DamageType {
		NETURAL,
		MAGIC,
		NETURAL_PIERCING,
		MAGIC_PIERCING,
		LIFE_LOSING;
	}
}
