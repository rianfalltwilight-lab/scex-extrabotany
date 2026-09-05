package io.github.lounode.extrabotany.common.item.equipment.tool.hammer;

import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.function.Predicate;

public class GaiaHammerItem extends ManasteelHammerItem {

	private static final int MANA_PER_DAMAGE = 200;
	private static final float HEAVY_SMASH_SOUND_FALL_DISTANCE_THRESHOLD = 5.0F;
	public static final float KNOCKBACK_RANGE = 3.5F;
	private static final float KNOCKBACK_POWER = 1.3F;

	public GaiaHammerItem(Tier tier, int attackDamageModifier, float attackSpeedModifier, Properties properties) {
		super(tier, attackDamageModifier, attackSpeedModifier, properties);
	}

	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		if (!shouldDealAdditionalDamage(attacker)) {
			return super.hurtEnemy(stack, target, attacker);
		}

		Level level = attacker.level();
		attacker.setDeltaMovement(attacker.getDeltaMovement().with(Direction.Axis.Y, 0.01F));

		if (attacker instanceof ServerPlayer player) {
			player.connection.send(new ClientboundSetEntityMotionPacket(player));
		}

		if (target.onGround()) {
			SoundEvent soundEvent = SoundEvents.ANVIL_LAND;
			level.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), soundEvent, attacker.getSoundSource(), 1.0F, 1.0F);
		} else {

		}
		knockbackNearbyEntities(level, attacker, target);
		return super.hurtEnemy(stack, target, attacker);
	}

	private void knockbackNearbyEntities(Level level, Entity attacker, Entity attacked) {
		if (attacker instanceof ServerPlayer serverPlayer) {
			serverPlayer.addDeltaMovement(new Vec3(0, getKnockbackPower(), 0));
			serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(serverPlayer));
		}
	}

	private static Predicate<LivingEntity> getKnockbackPredicate(Entity attacker, Entity attacked) {
		return (entity) -> {
			boolean bl = !entity.isSpectator();
			boolean bl2 = entity != attacked;
			boolean bl3 = !attacker.isAlliedTo(entity);
			boolean bl4 = true;

			/*
			if (entity instanceof TameableEntity) {
				TameableEntity tameable = (TameableEntity)entity;
				if (attacked instanceof LivingEntity && tameable.isTamed() && tameable.getOwner() == attacked) {
					bl4 = false;
				}
			}
			
			*/

			boolean bl5 = true;
			if (entity instanceof ArmorStand) {
				ArmorStand armorStand = (ArmorStand) entity;
				if (armorStand.isMarker()) {
					bl5 = false;
				}
			}

			boolean bl6 = attacked.distanceToSqr(entity) <= Math.pow(3.5F, 2.0F);
			return bl && bl2 && bl3 && bl4 && bl5 && bl6;
		};
	}

	private void explode(Level level, Entity attacker, Entity attacked) {

	}

	private double getKnockback(Entity attacker, LivingEntity attacked, Vec3 distance) {
		return ((double) getKnockbackRange() - distance.length()) * (double) getKnockbackPower() * (double) (attacker.fallDistance > getHeavySmashSoundFallDistanceThreshold() ? 2 : 1) * ((double) 1.0F - attacked.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
	}

	public float getKnockbackPower() {
		return KNOCKBACK_POWER;
	}

	public float getKnockbackRange() {
		return KNOCKBACK_RANGE;
	}

	public float getHeavySmashSoundFallDistanceThreshold() {
		return HEAVY_SMASH_SOUND_FALL_DISTANCE_THRESHOLD;
	}

	@Override
	public int getManaPerDamage() {
		return MANA_PER_DAMAGE;
	}

	public static boolean shouldDealAdditionalDamage(LivingEntity attacker) {
		return attacker.fallDistance > (double) 1.5F && !attacker.isFallFlying();
	}
}
