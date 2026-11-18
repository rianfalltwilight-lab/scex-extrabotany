package io.github.lounode.extrabotany.common.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import vazkii.botania.common.handler.BotaniaSounds;

import io.github.lounode.extrabotany.common.entity.gaia.GaiaIII;
import io.github.lounode.extrabotany.common.util.PlayerDropHelper;
import io.github.lounode.extrabotany.xplat.ExtraBotanyConfig;

import java.util.List;

/**
 * 盖亚守护者III召唤的骷髅仆从。它不动；55 tick 后每 15 tick 射一枚带火的导弹（4 点）；每 400 tick 让周围玩家丢下手里的东西；第三阶段每 160 tick 给 Boss 回 1 点血。它有护盾，每层护盾挡掉一次伤害；一次最多受 10 点伤害；周围没有盖亚守护者III时消失。
 * A skull minion that Guardian of Gaia III summons. It does not move; after 55 ticks it shoots a missile with fire (4 damage) each 15 ticks; each 400 ticks it makes the players around drop what they hold; in rank III it heals the boss by 1 each 160 ticks. It has shields, and each shield stops one hit; it takes at most 10 damage from one hit; it goes away when no Guardian of Gaia III is near it.
 *
 * <p>来源：ExtraMeteorP 1.12 {@code EntitySkullMinion}。
 * <p>Source: {@code EntitySkullMinion} of ExtraMeteorP 1.12.
 */
public class SkullMinionEntity extends Monster {
	private static final String TAG_TYPE = "type";
	private static final String TAG_SHIELD = "shield";
	public static final float RANGE = 15F;
	public static final int DAMAGE_CAP = 10;
	private static final EntityDataAccessor<Integer> TYPE = SynchedEntityData.defineId(SkullMinionEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> SHIELD = SynchedEntityData.defineId(SkullMinionEntity.class, EntityDataSerializers.INT);

	public SkullMinionEntity(EntityType<? extends SkullMinionEntity> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes()
				.add(Attributes.MOVEMENT_SPEED, 0.4)
				.add(Attributes.MAX_HEALTH, 24)
				.add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, RANGE * 1.5F));
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(TYPE, 0);
		builder.define(SHIELD, 0);
	}

	@Override
	public void aiStep() {
		setDeltaMovement(Vec3.ZERO);
		super.aiStep();
		if (level().isClientSide()) {
			return;
		}
		List<GaiaIII> hosts = level().getEntitiesOfClass(GaiaIII.class, new AABB(blockPosition()).inflate(RANGE));
		if (level().getDifficulty() == Difficulty.PEACEFUL || hosts.isEmpty()) {
			discard();
			return;
		}
		if (tickCount > 55 && tickCount % 15 == 0) {
			spawnMissile();
		}
		if (tickCount > 400 && tickCount % 400 == 0 && !ExtraBotanyConfig.common().disableGaiaDisArm()) {
			for (Player player : level().getEntitiesOfClass(Player.class, new AABB(blockPosition()).inflate(RANGE))) {
				if (!player.isCreative() && !player.isSpectator() && PlayerDropHelper.tryDrop(player, player.getMainHandItem())) {
					player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, net.minecraft.world.item.ItemStack.EMPTY);
				}
			}
		}
		if (tickCount % 160 == 0) {
			for (GaiaIII host : hosts) {
				if (host.isRankIII()) {
					host.heal(1F);
				}
			}
		}
	}

	private void spawnMissile() {
		SkullMissileEntity missile = new SkullMissileEntity(level(), this);
		missile.setPos(getX() + (random.nextDouble() - 0.5) * 0.1, getY() + 1.8 + (random.nextDouble() - 0.5) * 0.1, getZ() + (random.nextDouble() - 0.5) * 0.1);
		missile.setDamage(4);
		missile.setFire(true);
		if (missile.findTarget()) {
			playSound(BotaniaSounds.MISSILE, 0.6F, 0.8F + random.nextFloat() * 0.2F);
			level().addFreshEntity(missile);
		}
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (getShield() > 0 && !source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) {
			setShield(getShield() - 1);
			return super.hurt(source, 0F);
		}
		return super.hurt(source, Math.min(DAMAGE_CAP, amount));
	}

	public int getMinionType() {
		return entityData.get(TYPE);
	}

	public void setMinionType(int type) {
		entityData.set(TYPE, type);
	}

	public int getShield() {
		return entityData.get(SHIELD);
	}

	public void setShield(int shield) {
		entityData.set(SHIELD, Math.max(0, shield));
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putInt(TAG_TYPE, getMinionType());
		tag.putInt(TAG_SHIELD, getShield());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		setMinionType(tag.getInt(TAG_TYPE));
		setShield(tag.getInt(TAG_SHIELD));
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	public boolean removeWhenFarAway(double distance) {
		return false;
	}
}
