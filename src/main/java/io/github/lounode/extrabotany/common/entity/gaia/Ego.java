package io.github.lounode.extrabotany.common.entity.gaia;

import com.mojang.serialization.Dynamic;

import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.common.helper.PlayerHelper;

import io.github.lounode.extrabotany.api.gaia.GaiaArena;
import io.github.lounode.extrabotany.common.entity.EgoLandmineEntity;
import io.github.lounode.extrabotany.common.entity.EgoLandmineFormations;
import io.github.lounode.extrabotany.common.entity.EgoMinionEntity;
import io.github.lounode.extrabotany.common.entity.ExtraBotanyEntityType;
import io.github.lounode.extrabotany.common.entity.gaia.behavior.EgoWeaponFire;
import io.github.lounode.extrabotany.common.entity.gaia.behavior.GaiaSmashBlocksAround;
import io.github.lounode.extrabotany.common.sounds.ExtraBotanySounds;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 本我。一个单独的 Boss：它用召唤者的名字和皮肤，用遗物剑的技能攻击。玩家拿着或者戴着自然蕴息宝珠，潜行时右键盖亚仪式场地中间的信标来召唤它，这会消耗 200000 点自然之息。
 * Ego. A separate boss: it has the name and the skin of its summoner, and attacks with the skills of the relic swords. A player holds or wears the Nature Orb and uses it, with sneak, on the beacon in the center of a Gaia ritual arena to summon it; this uses 200000 Nature Aura.
 *
 * <h2>战斗规则 / Rules of the fight</h2>
 * <ul>
 * <li>场地和植物魔法的盖亚仪式相同：半径 12 格。最大生命值是 600 x 召唤时在场的玩家数，护甲值是 20。在场玩家比召唤时多的时候，战斗被取消。
 * The arena is the same as for the Gaia ritual of Botania: a radius of 12 blocks. The maximum health is 600 x the number of players that are there at the time of the summon, and the armor is 20. When there are more players than at the time of the summon, the fight is cancelled.</li>
 * <li>只有真正的玩家能伤害它。每次攻击最多造成 25 点伤害。它不会被击退。
 * Only true players can hurt it. One attack gives a maximum of 25 damage. It gets no knockback.</li>
 * <li>反击：每累计受到 50 点伤害，它攻击一次并且传送。
 * Counter: for each 50 damage that it gets in total, it attacks one time and teleports.</li>
 * <li>它每 80 tick 左右攻击一次，每 100 tick 左右先攻击再传送，每 100 tick 换一把武器。阶段越靠后，这些间隔越短。传送路线上的玩家受到 6 点伤害，路线上的方块被打碎。
 * It attacks approximately one time for each 80 ticks, attacks and then teleports approximately one time for each 100 ticks, and changes its weapon for each 100 ticks. These intervals are shorter in the later stages. The players on the path of a teleport get 6 damage, and the blocks on the path are broken.</li>
 * </ul>
 *
 * <h2>阶段 / Stages</h2>
 * <ul>
 * <li>第 0 阶段：它使用真影刃或者真泰拉之刃。
 * Stage 0: it uses the True Shadow Katana or the True Terra Blade.</li>
 * <li>第 1 阶段（生命值低于 75%）：地雷弹幕。它回到场地中心上方，{@value #BARRAGE_INVUL_TICKS} tick 内无敌，每 60 tick 排出一种地雷阵型，一共六种。之后它还可以使用波涌之刃和狂星之怒，并且每传送六次再随机排出一种阵型。
 * Stage 1 (health below 75%): the landmine barrage. It goes back above the center of the arena, cannot be hurt for {@value #BARRAGE_INVUL_TICKS} ticks, and puts down one landmine formation for each 60 ticks, six formations in total. After that it can also use the Influx Waver and the Star Wrath, and it puts down one more random formation after each six teleports.</li>
 * <li>第 2 阶段（生命值低于 25%）：仆从。它回到场地中心上方，召唤四个本我仆从，{@value #MINION_INVUL_TICKS} tick 内无敌并且每 tick 回复 0.25 点生命值。仆从全部被击败之后，无敌立刻结束。这个阶段它只使用最初分型。
 * Stage 2 (health below 25%): the minions. It goes back above the center of the arena, summons four Ego Minions, cannot be hurt for {@value #MINION_INVUL_TICKS} ticks and regenerates 0.25 health on each tick. After all minions are defeated, it can be hurt again immediately. In this stage it only uses the First Fractal.</li>
 * </ul>
 *
 * <p>它只在第 2 阶段被击败时才掉落战利品。每个攻击过它的玩家各得到一份。
 * <p>It only drops loot when it is defeated in stage 2. Each player that attacked it gets one set.
 *
 * <p>数据的键名是 {@code Stage}、{@code WeaponType}、{@code Summoner}、{@code BarrageWave}、{@code AttackDelay}、{@code WeaponChangeDelay}、{@code TeleportDelay}、{@code Teleports} 和 {@code DamageTaken}。
 * <p>The keys of the data are {@code Stage}, {@code WeaponType}, {@code Summoner}, {@code BarrageWave}, {@code AttackDelay}, {@code WeaponChangeDelay}, {@code TeleportDelay}, {@code Teleports} and {@code DamageTaken}.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code EntityEGO}。场地检查、Boss 血条、背景音乐和缴械使用这个模组里盖亚守护者的公共代码（{@link Gaia}）。
 * <p>Source: ExtraMeteorP 1.16 {@code EntityEGO}. The arena checks, the boss bar, the music and the disarm use the common code of the Guardians of Gaia of this mod ({@link Gaia}).
 *
 * <p>和原版的差异记录在 {@code docs/PORT_AUDIT.md} 的"本我成为单独的 Boss"一节里。
 * <p>The differences from the original are recorded in the section "The Ego becomes a separate boss" of {@code docs/PORT_AUDIT.md}.
 */
public class Ego extends Gaia {
	public static final float ARENA_RANGE = 12F;
	public static final int ARENA_HEIGHT = 5;
	public static final float HEALTH_PER_PLAYER = 600F;

	/** 第 2 阶段的编号。只有到了这个阶段，Boss 才掉落战利品。 / The number of stage 2. The boss only drops loot when it is in this stage. */
	public static final int LAST_STAGE = 2;

	private static final String TAG_STAGE = "Stage";
	private static final String TAG_WEAPON = "WeaponType";
	private static final String TAG_SUMMONER = "Summoner";
	private static final String TAG_BARRAGE_WAVE = "BarrageWave";
	private static final String TAG_ATTACK_DELAY = "AttackDelay";
	private static final String TAG_WEAPON_CHANGE_DELAY = "WeaponChangeDelay";
	private static final String TAG_TELEPORT_DELAY = "TeleportDelay";
	private static final String TAG_TELEPORTS = "Teleports";
	private static final String TAG_DAMAGE_TAKEN = "DamageTaken";

	private static final float ARMOR = 20F;
	private static final float HIT_CAP = 25F;
	private static final float COUNTER_THRESHOLD = 50F;
	private static final float BARRAGE_HEALTH = 0.75F;
	private static final float MINION_HEALTH = 0.25F;

	private static final int BARRAGE_INVUL_TICKS = 460;
	private static final int BARRAGE_WAVE_INTERVAL = 60;
	private static final int MINION_INVUL_TICKS = 600;
	/** 无敌时间少于这个值之后，Boss 重新开始攻击和传送。 / When the invulnerable time is less than this value, the boss starts to attack and teleport again. */
	private static final int INVUL_END_TICKS = 20;
	private static final float MINION_HEALTH_PER_PLAYER = 60F;
	private static final float REGENERATION = 0.25F;
	private static final int WEAPON_CHANGE_INTERVAL = 100;
	/** 每传送这么多次，Boss 排出一种随机的阵型。 / The boss puts down one random formation after this number of teleports. */
	private static final int TELEPORTS_PER_FORMATION = 7;
	private static final float TELEPORT_DAMAGE = 6F;

	private static final EntityDataAccessor<Integer> STAGE = SynchedEntityData.defineId(Ego.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> WEAPON = SynchedEntityData.defineId(Ego.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Optional<UUID>> SUMMONER = SynchedEntityData.defineId(Ego.class, EntityDataSerializers.OPTIONAL_UUID);

	private int barrageWave;
	private int attackDelay;
	private int weaponChangeDelay;
	private int teleportDelay;
	private int teleports;
	private float damageTaken;

	public Ego(EntityType<? extends Ego> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		// 这些数值和原版相同。击退抗性是 1，所以它不会被击退。
		// These values are the same as in the original. The knockback resistance is 1, thus it gets no knockback.
		return Monster.createMonsterAttributes()
				.add(Attributes.MOVEMENT_SPEED, 0.55)
				.add(Attributes.MAX_HEALTH, HEALTH_PER_PLAYER)
				.add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
				.add(Attributes.ARMOR, ARMOR)
				.add(Attributes.FOLLOW_RANGE, 35)
				.add(Attributes.ATTACK_DAMAGE, 8);
	}

	/**
	 * 尝试召唤本我。检查没有通过时，玩家会收到说明原因的消息。这个方法不消耗物品：调用的地方在它返回 {@code true} 之后扣除自然之息。
	 * Tries to summon the Ego. When a check fails, the player gets a message with the cause. This method does not use up the item: the caller removes the Nature Aura after the method returns {@code true}.
	 *
	 * @param pos 信标的位置 / the position of the beacon
	 * @return 召唤成功时是 {@code true} / {@code true} when the summon is successful
	 */
	public static boolean spawn(Player player, Level level, BlockPos pos) {
		GaiaArena arena = GaiaArena.of(GlobalPos.of(level.dimension(), pos), ARENA_RANGE, ARENA_HEIGHT);
		if (!arena.checksVanilla(player, level, pos) || arena.countGaiaAround(level, Gaia.class) > 0) {
			return false;
		}
		if (!arena.checkGuardianInventoryStrict(level, (Item) null)) {
			if (!level.isClientSide()) {
				player.sendSystemMessage(Component.translatable("extrabotany.message.guardian_no_response").withStyle(ChatFormatting.RED));
			}
			return false;
		}
		if (level instanceof ServerLevel serverLevel) {
			Ego ego = new Ego(ExtraBotanyEntityType.EGO, level);
			ego.setHome(GlobalPos.of(level.dimension(), pos));
			ego.setArena(arena);
			ego.setPos(pos.getX() + 0.5, pos.getY() + 3, pos.getZ() + 0.5);
			ego.entityData.set(SUMMONER, Optional.of(player.getUUID()));
			ego.setCustomName(player.getDisplayName());

			List<Player> players = arena.getPlayersAround(level);
			ego.playerCount = Math.max(1, players.size());
			ego.bossEvent.setPlayerCount(ego.playerCount);
			ego.getAttribute(Attributes.MAX_HEALTH).setBaseValue(HEALTH_PER_PLAYER * ego.playerCount);
			ego.getAttribute(Attributes.ARMOR).setBaseValue(ARMOR);
			ego.setHealth(ego.getMaxHealth());

			EventHooks.finalizeMobSpawn(ego, serverLevel, serverLevel.getCurrentDifficultyAt(pos), MobSpawnType.EVENT, null);
			serverLevel.addFreshEntity(ego);
			serverLevel.playSound(null, pos, SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 10F, 0.1F);
			for (Player nearby : players) {
				if (nearby instanceof ServerPlayer serverPlayer) {
					CriteriaTriggers.SUMMONED_ENTITY.trigger(serverPlayer, ego);
				}
			}
		}
		return true;
	}

	// ---------------------------------------------------------------------
	// 公共代码的接入点 / Hooks of the common code
	// ---------------------------------------------------------------------

	@Override
	protected Brain<?> makeBrain(Dynamic<?> dynamic) {
		return ScriptedBossAI.makeBrain(dynamic);
	}

	@Override
	protected void initMemories(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData spawnData) {}

	@Override
	protected void updateAI() {
		ScriptedBossAI.updateActivity(this);
	}

	@Override
	public float getArenaRange() {
		return ARENA_RANGE;
	}

	@Override
	public float getDamageCap() {
		return HIT_CAP;
	}

	@Override
	public SoundEvent getBGM() {
		return ExtraBotanySounds.MUSIC_EGO;
	}

	@Override
	protected boolean hopsAfterHit() {
		return false;
	}

	/**
	 * 只有在最后一个阶段被击败时才有战利品。这样玩家不能用别的办法提前杀死它来拿奖励。
	 * There is only loot when the boss is defeated in the last stage. Thus a player cannot kill it early by a different method to get the reward.
	 */
	@Override
	public ResourceKey<LootTable> getDefaultLootTable() {
		return getStage() < LAST_STAGE ? BuiltInLootTables.EMPTY : getType().getDefaultLootTable();
	}

	/**
	 * 阶段：0、1 或者 2。
	 * The stage: 0, 1 or 2.
	 */
	public int getStage() {
		return entityData.get(STAGE);
	}

	private void setStage(int stage) {
		entityData.set(STAGE, Mth.clamp(stage, 0, LAST_STAGE));
	}

	/**
	 * 现在使用的武器的编号，见 {@link EgoWeaponFire}。
	 * The number of the weapon that is in use, see {@link EgoWeaponFire}.
	 */
	public int getWeaponType() {
		return entityData.get(WEAPON);
	}

	private void setWeaponType(int weapon) {
		entityData.set(WEAPON, Mth.clamp(weapon, EgoWeaponFire.WEAPON_TRUE_SHADOW_KATANA, EgoWeaponFire.WEAPON_FIRST_FRACTAL));
	}

	/**
	 * 手里的遗物剑。它只用来显示。
	 * The relic sword in the hand. It is only for display.
	 */
	public ItemStack getWeapon() {
		return EgoWeaponFire.getWeapon(getWeaponType());
	}

	/**
	 * 召唤者的 UUID。客户端用它找到召唤者的皮肤。
	 * The UUID of the summoner. The client uses it to find the skin of the summoner.
	 */
	public Optional<UUID> getSummoner() {
		return entityData.get(SUMMONER);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(STAGE, 0);
		builder.define(WEAPON, EgoWeaponFire.WEAPON_TRUE_SHADOW_KATANA);
		builder.define(SUMMONER, Optional.empty());
	}

	// ---------------------------------------------------------------------
	// 每 tick 的逻辑 / Logic for each tick
	// ---------------------------------------------------------------------

	@Override
	protected void customServerAiStep() {
		// 公共代码会把无敌时间减 1。原版的节奏按减 1 之前的值计算，所以先记下它。
		// The common code decreases the invulnerable time by 1. The timing of the original uses the value before the decrease, thus it is recorded first.
		int invul = getInvulTime();
		super.customServerAiStep();
		if (level().getDifficulty() == Difficulty.PEACEFUL) {
			discard();
			return;
		}
		List<Player> players = getPlayersAround();
		if (cancelIllegalFight(players)) {
			return;
		}

		if (invul >= INVUL_END_TICKS) {
			if (getStage() == 1) {
				tickBarrage(invul);
				return;
			}
			if (getStage() == LAST_STAGE) {
				tickMinionPhase();
				return;
			}
		}

		if (attackDelay > 0) {
			attackDelay--;
		} else if (tryAttack(players)) {
			attackDelay = (int) (80 - getStage() * 15 + 15 * random.nextDouble());
		}
		if (!isAlive() || players.isEmpty()) {
			return;
		}

		if (weaponChangeDelay > 0) {
			weaponChangeDelay--;
		} else {
			weaponChangeDelay = WEAPON_CHANGE_INTERVAL;
			setWeaponType(switch (getStage()) {
				case 0 -> random.nextInt(2);
				case 1 -> random.nextInt(4);
				default -> EgoWeaponFire.WEAPON_FIRST_FRACTAL;
			});
		}

		if (teleportDelay > 0) {
			teleportDelay--;
		} else if (tryAttack(players)) {
			teleportRandomly();
			teleports++;
			teleportDelay = 100 - getStage() * 10;
		}
		if (getStage() >= 1 && teleports % TELEPORTS_PER_FORMATION == 0) {
			EgoLandmineFormations.spawn(random.nextInt(EgoLandmineFormations.COUNT), level(), getHome().pos(), this);
			teleports++;
		}

		updateStage();
	}

	/**
	 * 在场玩家比召唤时多的时候，取消战斗并移除 Boss。原版用这条规则阻止玩家中途加入。
	 * Cancels the fight and removes the boss when there are more players than at the time of the summon. The original uses this rule to stop players that join during the fight.
	 */
	private boolean cancelIllegalFight(List<Player> players) {
		if (players.size() <= Math.max(1, playerCount)) {
			return false;
		}
		for (Player player : players) {
			player.sendSystemMessage(Component.translatable("extrabotany.message.herrscher.illegal_battle").withStyle(ChatFormatting.RED));
		}
		discard();
		return true;
	}

	private void updateStage() {
		if (getStage() == 0 && getHealth() < BARRAGE_HEALTH * getMaxHealth()) {
			setStage(1);
			setInvulTime(BARRAGE_INVUL_TICKS);
			barrageWave = 0;
			moveAboveHome();
		}
		if (getStage() == 1 && getHealth() < MINION_HEALTH * getMaxHealth()) {
			setStage(LAST_STAGE);
			setInvulTime(MINION_INVUL_TICKS);
			setWeaponType(EgoWeaponFire.WEAPON_FIRST_FRACTAL);
			if (level() instanceof ServerLevel serverLevel) {
				EgoMinionEntity.spawnGroup(serverLevel, this, getHome().pos(), MINION_HEALTH_PER_PLAYER * Math.max(1, playerCount));
			}
			moveAboveHome();
		}
	}

	/**
	 * 地雷弹幕：Boss 停在空中，无敌时间每经过 {@value #BARRAGE_WAVE_INTERVAL} tick 排出下一种阵型。第一种阵型在弹幕开始 40 tick 之后出现。
	 * The landmine barrage: the boss stays in the air and puts down the subsequent formation for each {@value #BARRAGE_WAVE_INTERVAL} ticks of the invulnerable time. The first formation appears 40 ticks after the start of the barrage.
	 */
	private void tickBarrage(int invul) {
		stayInTheAir();
		if (invul % BARRAGE_WAVE_INTERVAL == 0 && barrageWave < EgoLandmineFormations.BARRAGE_COUNT) {
			EgoLandmineFormations.spawn(barrageWave++, level(), getHome().pos(), this);
		}
	}

	/**
	 * 仆从阶段：Boss 停在空中并且回血。仆从全部消失之后，无敌立刻结束。
	 * The minion phase: the boss stays in the air and regenerates. After all minions are gone, the invulnerable time stops immediately.
	 */
	private void tickMinionPhase() {
		stayInTheAir();
		setHealth(getHealth() + REGENERATION);
		if (!hasMinions()) {
			setInvulTime(0);
		}
	}

	private void stayInTheAir() {
		Vec3 motion = getDeltaMovement();
		setDeltaMovement(motion.x, 0, motion.z);
	}

	private void moveAboveHome() {
		BlockPos home = getHome().pos();
		teleportTo(home.getX() + 0.5, home.getY() + 3, home.getZ() + 0.5);
		setDeltaMovement(Vec3.ZERO);
	}

	private AABB getHelperArea() {
		return GaiaArena.getArenaBB(getHome().pos(), ARENA_RANGE + 3);
	}

	private boolean hasMinions() {
		return !level().getEntitiesOfClass(EgoMinionEntity.class, getHelperArea(), LivingEntity::isAlive).isEmpty();
	}

	// ---------------------------------------------------------------------
	// 攻击和传送 / Attacks and teleports
	// ---------------------------------------------------------------------

	/**
	 * 用现在的武器攻击离它最近的玩家。
	 * Attacks the nearest player with the weapon that is in use.
	 *
	 * @return 场地里有可以攻击的玩家时是 {@code true} / {@code true} when the arena has a player that can be attacked
	 */
	private boolean tryAttack(List<Player> players) {
		Optional<Player> target = players.stream().filter(Player::isAlive).min(Comparator.comparingDouble(this::distanceToSqr));
		if (target.isEmpty()) {
			return false;
		}
		swing(InteractionHand.MAIN_HAND);
		EgoWeaponFire.fire(this, target.get(), getWeaponType());
		return true;
	}

	private void teleportRandomly() {
		if (!(level() instanceof ServerLevel serverLevel)) {
			return;
		}
		BlockPos home = getHome().pos();
		Vec3 oldPos = position();
		double x = home.getX() + (random.nextDouble() - 0.5) * ARENA_RANGE;
		double y = home.getY();
		double z = home.getZ() + (random.nextDouble() - 0.5) * ARENA_RANGE;
		// 地板比信标低一格的场地：让它落在地上。
		// An arena with a floor that is one block lower than the beacon: make it land on the ground.
		BlockPos floor = BlockPos.containing(x, y - 1, z);
		if (level().getBlockState(floor).getCollisionShape(level(), floor).isEmpty()) {
			y--;
		}
		teleportTo(x, y, z);
		Vec3 newPos = position();

		serverLevel.playSound(null, oldPos.x, oldPos.y, oldPos.z, SoundEvents.ENDERMAN_TELEPORT, getSoundSource(), 1F, 1F);
		playSound(SoundEvents.ENDERMAN_TELEPORT, 1F, 1F);
		int steps = 16;
		for (int i = 0; i < steps; i++) {
			Vec3 point = oldPos.lerp(newPos, i / (double) (steps - 1));
			serverLevel.sendParticles(ParticleTypes.PORTAL, point.x, point.y + getBbHeight() / 2, point.z, 8, getBbWidth(), getBbHeight() / 2, getBbWidth(), 0.1);
		}

		Vec3 oldCenter = oldPos.add(0, getBbHeight() / 2, 0);
		Vec3 newCenter = newPos.add(0, getBbHeight() / 2, 0);
		if (oldCenter.distanceToSqr(newCenter) <= 1) {
			return;
		}
		// 和原版相同：只有路线穿进玩家的碰撞箱时才算命中。紧贴着本我出发位置的玩家不受伤害。
		// The same as in the original: it is only a hit when the path goes into the collision box of the player. A player directly at the start position of the Ego gets no damage.
		for (Player player : getPlayersAround()) {
			if (player.getBoundingBox().inflate(0.25).clip(oldCenter, newCenter).isPresent()) {
				player.hurt(damageSources().mobAttack(this), TELEPORT_DAMAGE);
			}
		}
		int breakSteps = (int) oldCenter.distanceTo(newCenter);
		for (int i = 0; breakSteps >= 2 && i < breakSteps; i++) {
			Vec3 point = oldPos.lerp(newPos, i / (double) (breakSteps - 1));
			GaiaSmashBlocksAround.smashBlocksAround(this, Mth.floor(point.x), Mth.floor(point.y), Mth.floor(point.z), 1);
		}
	}

	// ---------------------------------------------------------------------
	// 受伤和死亡 / Damage and death
	// ---------------------------------------------------------------------

	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (level().isClientSide() || !(source.getEntity() instanceof Player player) || !PlayerHelper.isTruePlayer(player)
				|| getInvulTime() > 0 || !Float.isFinite(amount)) {
			return false;
		}
		float dealt = Math.min(HIT_CAP, amount);
		damageTaken += dealt;
		if (damageTaken >= COUNTER_THRESHOLD && tryAttack(getPlayersAround())) {
			damageTaken = 0;
			teleportRandomly();
		}
		return super.hurt(source, dealt);
	}

	@Override
	public void die(DamageSource source) {
		super.die(source);
		if (level() instanceof ServerLevel serverLevel) {
			playSound(SoundEvents.GENERIC_EXPLODE.value(), 20F, (1F + (random.nextFloat() - random.nextFloat()) * 0.2F) * 0.7F);
			serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, getX(), getY(), getZ(), 1, 0, 0, 0, 0);
			serverLevel.getEntitiesOfClass(EgoLandmineEntity.class, getHelperArea()).forEach(Entity::discard);
		}
	}

	/**
	 * 本我死亡或者离开时，它的仆从立刻消失。区块卸载时仆从留下，下次加载时它们还在。
	 * When the Ego dies or goes away, its minions go away immediately. The minions stay when the chunk unloads, and they are there at the subsequent load.
	 */
	@Override
	public void remove(RemovalReason reason) {
		if (reason.shouldDestroy() && level() instanceof ServerLevel serverLevel) {
			serverLevel.getEntitiesOfClass(EgoMinionEntity.class, getHelperArea()).forEach(Entity::discard);
		}
		super.remove(reason);
	}

	// ---------------------------------------------------------------------
	// 存档 / Save data
	// ---------------------------------------------------------------------

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putInt(TAG_STAGE, getStage());
		tag.putInt(TAG_WEAPON, getWeaponType());
		getSummoner().ifPresent(uuid -> tag.putUUID(TAG_SUMMONER, uuid));
		tag.putInt(TAG_BARRAGE_WAVE, barrageWave);
		tag.putInt(TAG_ATTACK_DELAY, attackDelay);
		tag.putInt(TAG_WEAPON_CHANGE_DELAY, weaponChangeDelay);
		tag.putInt(TAG_TELEPORT_DELAY, teleportDelay);
		tag.putInt(TAG_TELEPORTS, teleports);
		tag.putFloat(TAG_DAMAGE_TAKEN, damageTaken);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		setStage(tag.getInt(TAG_STAGE));
		setWeaponType(tag.getInt(TAG_WEAPON));
		entityData.set(SUMMONER, tag.hasUUID(TAG_SUMMONER) ? Optional.of(tag.getUUID(TAG_SUMMONER)) : Optional.empty());
		barrageWave = tag.getInt(TAG_BARRAGE_WAVE);
		attackDelay = tag.getInt(TAG_ATTACK_DELAY);
		weaponChangeDelay = tag.getInt(TAG_WEAPON_CHANGE_DELAY);
		teleportDelay = tag.getInt(TAG_TELEPORT_DELAY);
		teleports = tag.getInt(TAG_TELEPORTS);
		damageTaken = tag.getFloat(TAG_DAMAGE_TAKEN);
	}
}
