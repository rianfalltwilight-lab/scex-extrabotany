package io.github.lounode.extrabotany.common.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.common.helper.PlayerHelper;

import io.github.lounode.extrabotany.common.entity.gaia.Ego;
import io.github.lounode.extrabotany.common.entity.gaia.behavior.EgoWeaponFire;
import io.github.lounode.extrabotany.common.lib.OriginalContributors;

import java.util.List;
import java.util.UUID;

/**
 * 本我仆从。注释里也把它叫做"随从"。本我在第 2 阶段召唤四个随从，它们各拿一把不同的遗物剑。
 * Ego Minion. The comments also call it the minion. The Ego summons four minions in stage 2, and each one has a different relic sword.
 *
 * <p>随从绕着它的目标玩家走位：它停在玩家视线方向上的一个点，每 100 tick 换一个角度。生命值降到一半以下时，它退到玩家身后更远的地方，并且每 40 tick 回复 2 点生命值。它卡住 60 tick 之后直接传送到目标点。
 * <p>A minion moves around its target player: it stays at a point in the view direction of the player, and changes the angle for each 100 ticks. When its health is at half or less, it goes to a farther point behind the player and regenerates 2 health for each 40 ticks. After it is blocked for 60 ticks, it teleports to its target point.
 *
 * <p>每 90 到 130 tick，随从用自己的遗物剑的技能攻击一次。
 * <p>For each 90 to 130 ticks, a minion attacks one time with the skill of its relic sword.
 *
 * <p>只有真正的玩家能伤害随从。一次攻击最多造成 20 点伤害。8 格内每有一个随从（包括它自己），它受到的伤害就减少 15%，最多减少 60%。随从不会得到负面药水效果。
 * <p>Only true players can hurt a minion. One attack gives a maximum of 20 damage. Each minion within 8 blocks (itself included) decreases the damage that it gets by 15%, to a maximum of 60%. A minion cannot have harmful potion effects.
 *
 * <p>召唤它的 Boss 不在了，或者难度是和平时，随从会消失。
 * <p>A minion goes away when the boss that summoned it is gone, or when the difficulty is peaceful.
 *
 * <p>数据的键名是 {@code MinionType} 和 {@code Summoner}。旧存档使用相同的键名。旧存档里由盖亚守护者III召唤的随从在读取之后会消失，因为现在只有本我能召唤它们。
 * <p>The keys of the data are {@code MinionType} and {@code Summoner}. Old saves use the same keys. A minion that Guardian of Gaia III summoned in an old save goes away after the load, because now only the Ego can summon minions.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code EntityEGOMinion}。
 * <p>Source: ExtraMeteorP 1.16 {@code EntityEGOMinion}.
 */
public class EgoMinionEntity extends Monster {
	/** 随从的种类的数量。种类决定它使用的遗物剑。 / The number of minion types. The type gives the relic sword that the minion uses. */
	public static final int TYPES = 4;

	private static final String TAG_TYPE = "MinionType";
	private static final String TAG_SUMMONER = "Summoner";

	private static final double TARGET_RANGE = 16;
	private static final double ALLY_RANGE = 8;
	private static final float HIT_CAP = 20F;
	private static final int STUCK_TICKS = 60;
	private static final double SUMMONED_ARMOR = 10;
	/** 找不到本我的时间超过这个值，随从就消失。 / A minion goes away when it cannot find the Ego for more than this time. */
	private static final int MISSING_BOSS_TICKS = 100;
	// 和原版相同：四个随从出现在场地的四个角上。
	// The same as in the original: the four minions appear at the four corners of the arena.
	private static final int[][] SPAWN_OFFSETS = { { 6, 6 }, { 6, -6 }, { -6, 6 }, { -6, -6 } };

	private static final EntityDataAccessor<Integer> MINION_TYPE = SynchedEntityData.defineId(EgoMinionEntity.class, EntityDataSerializers.INT);

	@Nullable
	private UUID summoner;
	private int attackCooldown;
	private int stuckTicks;
	private int missingBossTicks;

	public EgoMinionEntity(EntityType<? extends EgoMinionEntity> type, Level level) {
		super(type, level);
		xpReward = 0;
	}

	public static AttributeSupplier.Builder createAttributes() {
		// 这些数值和原版相同。召唤出来的随从的护甲值是 10，见 spawnGroup。
		// These values are the same as in the original. The armor of a summoned minion is 10, see spawnGroup.
		return Monster.createMonsterAttributes()
				.add(Attributes.MAX_HEALTH, 60)
				.add(Attributes.MOVEMENT_SPEED, 0.5)
				.add(Attributes.ARMOR, 15)
				.add(Attributes.FOLLOW_RANGE, 35)
				.add(Attributes.ATTACK_DAMAGE, 7)
				.add(Attributes.KNOCKBACK_RESISTANCE, 1);
	}

	/**
	 * 在场地的四个角上各召唤一个随从。它们的名字是原版贡献者名单里随机的四个。
	 * Summons one minion at each of the four corners of the arena. Their names are four random names from the list of contributors of the original.
	 *
	 * @param home   场地中心的信标 / the beacon in the center of the arena
	 * @param health 每个随从的最大生命值 / the maximum health of each minion
	 */
	public static void spawnGroup(ServerLevel level, Ego boss, BlockPos home, float health) {
		List<String> names = OriginalContributors.pick(boss.getRandom(), TYPES);
		for (int type = 0; type < TYPES; type++) {
			EgoMinionEntity minion = new EgoMinionEntity(ExtraBotanyEntityType.EGO_MINION, level);
			minion.summoner = boss.getUUID();
			minion.setMinionType(type);
			minion.setCustomName(Component.literal(names.get(type)));
			minion.setCustomNameVisible(true);
			minion.getAttribute(Attributes.MAX_HEALTH).setBaseValue(health);
			minion.getAttribute(Attributes.ARMOR).setBaseValue(SUMMONED_ARMOR);
			minion.setHealth(health);
			minion.setPos(home.getX() + SPAWN_OFFSETS[type][0] + 0.5, home.getY() + 1, home.getZ() + SPAWN_OFFSETS[type][1] + 0.5);
			EventHooks.finalizeMobSpawn(minion, level, level.getCurrentDifficultyAt(minion.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
			level.addFreshEntity(minion);
		}
	}

	public int getMinionType() {
		return entityData.get(MINION_TYPE);
	}

	public void setMinionType(int type) {
		entityData.set(MINION_TYPE, Math.floorMod(type, TYPES));
	}

	/**
	 * 随从手里的遗物剑。它只用来显示。
	 * The relic sword in the hand of the minion. It is only for display.
	 */
	public ItemStack getWeapon() {
		return EgoWeaponFire.getWeapon(getMinionType());
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(MINION_TYPE, 0);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 16F));
		goalSelector.addGoal(8, new RandomLookAroundGoal(this));
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (!(level() instanceof ServerLevel serverLevel)) {
			return;
		}
		if (level().getDifficulty() == Difficulty.PEACEFUL || summoner == null) {
			discard();
			return;
		}
		// 本我死亡或者离开时会带走它的随从（见 Ego#remove）。这里处理剩下的情况：读档之后，随从所在的区块可能比本我所在的区块先加载，所以找不到本我时先等一会儿。
		// The Ego removes its minions when it dies or goes away (see Ego#remove). This handles the remaining case: after a load, the chunk of a minion can load before the chunk of the Ego, thus the minion waits for some time when it cannot find the Ego.
		Entity boss = serverLevel.getEntity(summoner);
		if (boss instanceof Ego ego && ego.isAlive()) {
			missingBossTicks = 0;
		} else if (boss != null || ++missingBossTicks > MISSING_BOSS_TICKS) {
			discard();
			return;
		}
		removeHarmfulEffects();

		LivingEntity target = getTarget();
		if (!(target instanceof Player) || !target.isAlive()) {
			target = level().getEntitiesOfClass(Player.class, getBoundingBox().inflate(TARGET_RANGE), player -> player.isAlive() && !player.isSpectator())
					.stream().findFirst().orElse(null);
			setTarget(target);
		}
		if (target == null) {
			return;
		}
		moveAround(target);
		// 和原版相同：先倒数，数到 0 的那个 tick 就攻击。
		// The same as in the original: the countdown comes first, and the attack is on the tick on which it gets to 0.
		if (attackCooldown > 0) {
			attackCooldown--;
		}
		if (attackCooldown == 0) {
			swing(InteractionHand.MAIN_HAND);
			EgoWeaponFire.fire(this, target, getMinionType());
			attackCooldown = 90 + random.nextInt(40);
		}
	}

	private void removeHarmfulEffects() {
		if (getActiveEffects().isEmpty()) {
			return;
		}
		List<Holder<MobEffect>> harmful = getActiveEffects().stream()
				.map(MobEffectInstance::getEffect)
				.filter(effect -> effect.value().getCategory() == MobEffectCategory.HARMFUL)
				.toList();
		harmful.forEach(this::removeEffect);
	}

	private void moveAround(LivingEntity target) {
		Vec3 look = target.getLookAngle().multiply(1, 0, 1);
		if (look.horizontalDistanceSqr() == 0) {
			double yaw = Math.toRadians(target.getYRot() + 90);
			look = new Vec3(Math.cos(yaw), 0, Math.sin(yaw));
		}
		look = look.normalize().scale(3.5);
		if (getHealth() <= getMaxHealth() * 0.5F) {
			look = look.scale(-2);
			if (tickCount % 40 == 0) {
				heal(2F);
			}
		}
		// 四个随从各占一个方向，并且每 100 tick 一起转 45 度。
		// Each of the four minions has one direction, and all of them turn 45 degrees for each 100 ticks.
		float angle = (float) (Math.PI / 2 * getMinionType() + Math.floor(tickCount / 100.0) * Math.PI / 4);
		Vec3 destination = target.position().add(look.yRot(angle));

		if (position().distanceTo(destination) >= 0.5) {
			getMoveControl().setWantedPosition(destination.x, destination.y, destination.z, 0.7);
			stuckTicks++;
		} else {
			stuckTicks = 0;
		}
		if (stuckTicks >= STUCK_TICKS) {
			teleportTo(destination.x, destination.y, destination.z);
			stuckTicks = 0;
		}
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		Entity attacker = source.getEntity();
		if (!(attacker instanceof Player) || !PlayerHelper.isTruePlayer(attacker) || !Float.isFinite(amount)) {
			return false;
		}
		int allies = level().getEntitiesOfClass(EgoMinionEntity.class, getBoundingBox().inflate(ALLY_RANGE), Entity::isAlive).size();
		float resistance = Math.min(0.6F, allies * 0.15F);
		return super.hurt(source, Math.min(HIT_CAP, amount * (1F - resistance)));
	}

	@Override
	public boolean removeWhenFarAway(double distance) {
		return false;
	}

	@Override
	public boolean canBeLeashed() {
		return false;
	}

	@Override
	public boolean isPersistenceRequired() {
		return true;
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putInt(TAG_TYPE, getMinionType());
		if (summoner != null) {
			tag.putUUID(TAG_SUMMONER, summoner);
		}
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		setMinionType(tag.getInt(TAG_TYPE));
		summoner = tag.hasUUID(TAG_SUMMONER) ? tag.getUUID(TAG_SUMMONER) : null;
	}
}
