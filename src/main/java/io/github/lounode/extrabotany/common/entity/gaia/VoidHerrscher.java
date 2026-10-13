package io.github.lounode.extrabotany.common.entity.gaia;

import com.mojang.serialization.Dynamic;

import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.common.handler.BotaniaSounds;
import vazkii.botania.common.helper.PlayerHelper;
import vazkii.botania.common.helper.VecHelper;

import io.github.lounode.extrabotany.api.gaia.GaiaArena;
import io.github.lounode.extrabotany.common.bossevents.ServerGaiaBossEvent;
import io.github.lounode.extrabotany.common.brew.ExtraBotanyMobEffects;
import io.github.lounode.extrabotany.common.entity.ExtraBotanyEntityType;
import io.github.lounode.extrabotany.common.entity.SkullLandMineEntity;
import io.github.lounode.extrabotany.common.entity.SkullMissileEntity;
import io.github.lounode.extrabotany.common.entity.SubspaceLanceEntity;
import io.github.lounode.extrabotany.common.entity.SwordDomainEntity;
import io.github.lounode.extrabotany.common.entity.VoidFieldEntity;
import io.github.lounode.extrabotany.common.entity.subspace.SubspaceEntity;
import io.github.lounode.extrabotany.common.entity.subspace.SubspaceSpearEntity;
import io.github.lounode.extrabotany.common.handler.DamageHandler;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.item.relic.SpearOfSubspaceItem;
import io.github.lounode.extrabotany.common.lib.OriginalContributors;
import io.github.lounode.extrabotany.common.sounds.ExtraBotanySounds;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 空之律者。这个模组的最终 Boss。玩家潜行时用"来自虚空的呼唤"右键盖亚仪式场地中间的信标来召唤它。
 * Herrscher of the Void. The last boss of this mod. A player uses the Call of the Void, with sneak, on the beacon in the center of a Gaia ritual arena to summon it.
 *
 * <h2>战斗规则 / Rules of the fight</h2>
 * <ul>
 * <li>场地半径是 15 格。最大生命值是 400 x 召唤时在场的玩家数。在场玩家比召唤时多的时候，战斗被取消。
 * The radius of the arena is 15 blocks. The maximum health is 400 x the number of players that are there at the time of the summon. When there are more players than at the time of the summon, the fight is cancelled.</li>
 * <li>只有真正的玩家能伤害它。每次攻击最多造成 25 点伤害。每次受伤之后它有 10 tick 的无敌时间。
 * Only true players can hurt it. One attack gives a maximum of 25 damage. After each hit it cannot be hurt for 10 ticks.</li>
 * <li>战斗开始时，以及进入二阶段时，接下来的 5 次攻击完全无效。
 * At the start of the fight, and at the start of rank II, the subsequent 5 attacks have no effect at all.</li>
 * <li>闪避：每 300 tick 它可以完全闪避一次攻击，并在攻击者的位置放一个虚空力场。
 * Dodge: one time for each 300 ticks it can fully dodge an attack, and it puts a void field at the position of the attacker.</li>
 * <li>反击：每累计受到 80 点伤害，它传送一次，并对攻击者造成"最大生命值 10% + 6"的真实伤害。
 * Counter: for each 80 damage that it gets in total, it teleports and gives the attacker true damage of "10% of maximum health + 6".</li>
 * <li>场地里的玩家不能飞行，并且有巫毒诅咒效果。走出场地的玩家被推回来并得到霉运效果。
 * The players in the arena cannot fly, and they have the Witch Curse effect. A player that goes out of the arena is pushed back and gets the Bad Luck effect.</li>
 * </ul>
 *
 * <h2>阶段 / Ranks</h2>
 * <ul>
 * <li>一阶段：导弹、地雷、头顶的亚空间裂隙射出的矛，以及场地中心的亚空之矛枪。
 * Rank I: missiles, landmines, spears from the subspace rift above its head, and the Subspace Lance in the center of the arena.</li>
 * <li>二阶段（生命值 80% 以下）：它长出翅膀并浮在空中，得到一面护盾，立刻使用"天堂裁决"，并开始使用特殊攻击。
 * Rank II (health at 80% or less): it gets wings and floats in the air, gets one shield, immediately uses the "Divine Judge", and starts to use special attacks.</li>
 * <li>三阶段（生命值 25% 以下）：它把生命值回满（只有一次），得到三面护盾，每 80 tick 回复 1 点生命值。支援者开始帮助玩家。
 * Rank III (health at 25% or less): it restores its health fully (one time only), gets three shields, and regenerates 1 health for each 80 ticks. The supporters start to help the players.</li>
 * </ul>
 *
 * <h2>护盾 / Shields</h2>
 * <p>每面护盾让它受到的伤害和伤害上限都乘以 0.85。它有护盾时，每次被攻击都复制攻击者身上的全部药水效果。减免之后仍然超过 20 点的攻击打碎一面护盾，并且让它的闪避立刻可用。三面护盾齐全时，它把靠近的弹射物弹回去。
 * <p>Each shield multiplies the damage that it gets, and the damage limit, by 0.85. While it has shields, each attack makes it copy all potion effects of the attacker. An attack that is still more than 20 after the decrease breaks one shield and makes the dodge immediately available. With all three shields, it sends adjacent projectiles back.
 *
 * <h2>特殊攻击 / Special attacks</h2>
 * <p>从二阶段开始，特殊攻击按冷却时间轮流使用。它每次被攻击，冷却时间减少 15 tick（最低到 80）。
 * <p>From rank II, the special attacks are used in sequence with a cooldown. Each attack on the boss decreases the cooldown by 15 ticks (to a minimum of 80).
 * <ol>
 * <li>点名：随机一名玩家受到"最大生命值 20% + 6"的真实伤害，主手物品进入 120 tick 冷却，头顶落下一把矛枪。
 * Smite: one random player gets true damage of "20% of maximum health + 6", the item in the main hand gets a cooldown of 120 ticks, and a lance falls from above.</li>
 * <li>虚空裁决（只在三阶段）：它回到场地中心，120 tick 内无敌，身后打开 24 个亚空间裂隙向玩家射击。
 * Void Judge (rank III only): it goes back to the center of the arena, cannot be hurt for 120 ticks, and opens 24 subspace rifts behind it that shoot at the players.</li>
 * <li>天堂裁决：八把审判之剑把玩家拉向场地中心。
 * Divine Judge: eight swords of judgment pull the players to the center of the arena.</li>
 * <li>矛枪（只在三阶段）：一把矛枪落在场地里随机的位置。
 * Lance (rank III only): a lance falls at a random position in the arena.</li>
 * </ol>
 *
 * <p>存档的键名和这个移植版以前的版本相同：{@code ShieldLayers}、{@code RotatingShields}、{@code RankII}、{@code RankIII}、{@code HerrscherDamageTaken}、{@code HerrscherTpDelay}、{@code DodgeCd}、{@code SkillCd}、{@code SkillType}、{@code SupportCd}、{@code EmergeLanceDone} 和 {@code Supporters}。
 * <p>The keys in the save are the same as in the previous versions of this port: {@code ShieldLayers}, {@code RotatingShields}, {@code RankII}, {@code RankIII}, {@code HerrscherDamageTaken}, {@code HerrscherTpDelay}, {@code DodgeCd}, {@code SkillCd}, {@code SkillType}, {@code SupportCd}, {@code EmergeLanceDone} and {@code Supporters}.
 *
 * <p>来源：ExtraMeteorP 1.12 {@code EntityVoidHerrscher}。场地检查、Boss 血条、背景音乐和缴械使用这个模组里盖亚守护者的公共代码（{@link Gaia}）。
 * <p>Source: ExtraMeteorP 1.12 {@code EntityVoidHerrscher}. The arena checks, the boss bar, the music and the disarm use the common code of the Guardians of Gaia of this mod ({@link Gaia}).
 *
 * <p>和原版的差异记录在 {@code docs/PORT_AUDIT.md} 的第十批里。
 * <p>The differences from the original are recorded in batch 10 of {@code docs/PORT_AUDIT.md}.
 */
public class VoidHerrscher extends Gaia {
	public static final float ARENA_RANGE = 15F;
	public static final int ARENA_HEIGHT = 5;

	private static final String TAG_HITS_TO_ABSORB = "ShieldLayers";
	private static final String TAG_SHIELDS = "RotatingShields";
	private static final String TAG_RANK_II = "RankII";
	private static final String TAG_RANK_III = "RankIII";
	private static final String TAG_DAMAGE_TAKEN = "HerrscherDamageTaken";
	private static final String TAG_TELEPORT_DELAY = "HerrscherTpDelay";
	private static final String TAG_DODGE_COOLDOWN = "DodgeCd";
	private static final String TAG_SKILL_COOLDOWN = "SkillCd";
	private static final String TAG_SKILL = "SkillType";
	private static final String TAG_SUPPORT_COOLDOWN = "SupportCd";
	private static final String TAG_FIRST_LANCE_DONE = "EmergeLanceDone";
	private static final String TAG_SUPPORTERS = "Supporters";

	private static final float HEALTH_PER_PLAYER = 400F;
	private static final float RANK_II_HEALTH = 0.8F;
	private static final float RANK_III_HEALTH = 0.25F;
	/** 在这个时间内击败它的玩家得到隐藏进度。 / A player that defeats it in this time gets the hidden advancement. */
	private static final int QUICK_KILL_TICKS = 3600;

	private static final int HIT_CAP = 25;
	private static final float SHIELD_FACTOR = 0.85F;
	private static final int MAX_SHIELDS = 3;
	private static final float SHIELD_BREAK_DAMAGE = 20F;
	private static final int HITS_TO_ABSORB = 5;
	private static final int HIT_INVUL_TICKS = 10;
	private static final float COUNTER_THRESHOLD = 80F;
	private static final int DODGE_COOLDOWN = 300;
	private static final int MIN_SKILL_COOLDOWN = 80;

	private static final int FIRST_LANCE_TICK = 100;
	private static final int FIRST_LANCE_LIFE = 4800;
	private static final int LANCE_LIFE = 1200;
	private static final int LANCE_DROP_HEIGHT = 12;
	private static final int RIFT_INTERVAL = 72;
	private static final double FLEE_DISTANCE = 6;
	private static final int LATE_FIGHT_TICKS = 1800;

	private static final EntityDataAccessor<Integer> SHIELDS = SynchedEntityData.defineId(VoidHerrscher.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> RANK_II = SynchedEntityData.defineId(VoidHerrscher.class, EntityDataSerializers.BOOLEAN);

	/**
	 * 特殊攻击。这个顺序就是存档里的编号，所以不能改动。
	 * The special attacks. This sequence gives the numbers in the save data, so it must not change.
	 */
	private enum Skill {
		SMITE, VOID_JUDGE, DIVINE_JUDGE, LANCE;

		static Skill byId(int id) {
			return values()[Math.floorMod(id, values().length)];
		}
	}

	private int hitsToAbsorb = HITS_TO_ABSORB;
	private float damageTaken;
	private boolean rankIII;
	private boolean firstLanceDone;
	private int teleportDelay;
	private int dodgeCooldown = DODGE_COOLDOWN;
	private int skillCooldown = 200;
	private Skill skill = Skill.SMITE;
	private int supportCooldown = 300;
	private final List<String> supporters = new ArrayList<>(OriginalContributors.NAMES);
	private boolean killed;

	public VoidHerrscher(EntityType<? extends VoidHerrscher> type, Level level) {
		super(type, level);
		xpReward = 1725;
		bossEvent = (ServerGaiaBossEvent) new ServerGaiaBossEvent(type.getDescription(), BossEvent.BossBarColor.YELLOW, BossEvent.BossBarOverlay.PROGRESS)
				.setCreateWorldFog(true);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes()
				.add(Attributes.MOVEMENT_SPEED, 0.4)
				.add(Attributes.MAX_HEALTH, HEALTH_PER_PLAYER)
				.add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
	}

	/**
	 * 尝试召唤空之律者。检查没有通过时，玩家会收到说明原因的消息。
	 * Tries to summon the Herrscher of the Void. When a check fails, the player gets a message with the cause.
	 *
	 * @param pos 信标的位置 / the position of the beacon
	 * @return 召唤成功时是 {@code true} / {@code true} when the summon is successful
	 */
	public static boolean spawn(Player player, Level level, BlockPos pos) {
		GaiaArena arena = GaiaArena.of(GlobalPos.of(level.dimension(), pos), ARENA_RANGE, ARENA_HEIGHT);
		if (!arena.checksVanilla(player, level, pos) || arena.countGaiaAround(level, Gaia.class) > 0) {
			return false;
		}
		if (!arena.checkGuardianInventoryStrict(level, ExtraBotanyItems.firstFractal)) {
			if (!level.isClientSide()) {
				player.sendSystemMessage(Component.translatable("extrabotany.message.guardian_no_response").withStyle(ChatFormatting.RED));
			}
			return false;
		}
		if (level instanceof ServerLevel serverLevel) {
			VoidHerrscher boss = new VoidHerrscher(ExtraBotanyEntityType.VOID_HERRSCHER, level);
			boss.setHome(GlobalPos.of(level.dimension(), pos));
			boss.setArena(arena);
			boss.setPos(pos.getX() + 0.5, pos.getY() + 3, pos.getZ() + 0.5);

			List<Player> players = arena.getPlayersAround(level);
			boss.playerCount = Math.max(1, players.size());
			boss.bossEvent.setPlayerCount(boss.playerCount);
			boss.getAttribute(Attributes.MAX_HEALTH).setBaseValue(HEALTH_PER_PLAYER * boss.playerCount);
			boss.setHealth(boss.getMaxHealth());

			EventHooks.finalizeMobSpawn(boss, serverLevel, serverLevel.getCurrentDifficultyAt(pos), MobSpawnType.EVENT, null);
			serverLevel.addFreshEntity(boss);
			serverLevel.playSound(null, pos, SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 10F, 0.1F);
			for (Player nearby : players) {
				if (nearby instanceof ServerPlayer serverPlayer) {
					CriteriaTriggers.SUMMONED_ENTITY.trigger(serverPlayer, boss);
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
	public SoundEvent getBGM() {
		return ExtraBotanySounds.MUSIC_HERRSCHER;
	}

	@Override
	public Item getGuardianBypassItem() {
		return ExtraBotanyItems.firstFractal;
	}

	@Override
	public int getArenaRingColor() {
		return 0x4D00FF;
	}

	@Override
	public float getPylonBeamRedBase() {
		return 0F;
	}

	@Override
	protected double getHurtHopStrength() {
		return 0.2;
	}

	@Override
	protected double getHurtHopHeight() {
		return 0.25;
	}

	public int getShields() {
		return entityData.get(SHIELDS);
	}

	private void setShields(int shields) {
		entityData.set(SHIELDS, Mth.clamp(shields, 0, MAX_SHIELDS));
	}

	public boolean isRankII() {
		return entityData.get(RANK_II);
	}

	public boolean isRankIII() {
		return rankIII;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(SHIELDS, 0);
		builder.define(RANK_II, false);
	}

	// ---------------------------------------------------------------------
	// 每 tick 的逻辑 / Logic for each tick
	// ---------------------------------------------------------------------

	@Override
	protected void customServerAiStep() {
		super.customServerAiStep();
		if (level().getDifficulty() == Difficulty.PEACEFUL) {
			discard();
			return;
		}
		List<Player> players = getPlayersAround();
		if (cancelIllegalFight(players)) {
			return;
		}
		// 在场的每名玩家都算作参战者，都会得到战利品。
		// Each player in the arena counts as a fighter and gets loot.
		players.forEach(this::markPlayerAttacked);

		moveAndLook(players);
		updateRank();
		reflectProjectiles();
		if (tickCount % RIFT_INTERVAL == 0 || dodgeCooldown == DODGE_COOLDOWN - 20) {
			openSpearRift();
		}
		if (!firstLanceDone && tickCount >= FIRST_LANCE_TICK) {
			firstLanceDone = true;
			BlockPos home = getHome().pos();
			spawnLance(home.getX(), home.getY(), home.getZ(), 3, FIRST_LANCE_LIFE);
		}

		players.forEach(this::applyArenaRules);
		usePeriodicAttacks(players);
		useSkills(players);

		if (teleportDelay > 0) {
			teleportDelay--;
		}
		if (teleportDelay == 0 && getHealth() > 0) {
			teleportRandomly();
			teleportDelay = rankIII ? 75 : 90;
		}
		if (dodgeCooldown > 0) {
			dodgeCooldown--;
		}
		if (rankIII) {
			if (supportCooldown > 0) {
				supportCooldown--;
			} else if (!players.isEmpty()) {
				support(players.get(random.nextInt(players.size())));
			}
		}
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

	private void moveAndLook(List<Player> players) {
		if (getInvulTime() > 15) {
			setDeltaMovement(Vec3.ZERO);
		}
		if (!players.isEmpty()) {
			lookAt(players.getFirst(), 360F, 360F);
		}
		// 和玩家保持距离。
		// Keep a distance from the players.
		Player nearest = level().getNearestPlayer(this, FLEE_DISTANCE);
		if (nearest != null && (getNavigation().isDone() || tickCount % 10 == 0)) {
			Vec3 away = DefaultRandomPos.getPosAway(this, 16, 7, nearest.position());
			if (away != null) {
				getNavigation().moveTo(away.x, away.y, away.z, 1.2);
			}
		}
		if (isRankII() && getY() < getHome().pos().getY() + 2) {
			setDeltaMovement(getDeltaMovement().x, 0.1, getDeltaMovement().z);
		}
	}

	private void updateRank() {
		float healthFraction = getHealth() / getMaxHealth();
		if (!isRankII() && healthFraction <= RANK_II_HEALTH) {
			entityData.set(RANK_II, true);
			hitsToAbsorb = HITS_TO_ABSORB;
			setShields(1);
			useDivineJudge();
			for (int i = 0; i < 2; i++) {
				spawnLanceRandomly();
			}
		}
		if (!rankIII && healthFraction <= RANK_III_HEALTH) {
			rankIII = true;
			entityData.set(RANK_II, true);
			setShields(MAX_SHIELDS);
			for (int i = 0; i < 4; i++) {
				spawnLanceRandomly();
			}
			setHealth(getMaxHealth());
		}
	}

	/**
	 * 三面护盾齐全时，把靠近的弹射物沿自己的视线弹回去。导弹和亚空之矛不会被弹回。
	 * With all three shields, sends adjacent projectiles back along its line of view. Missiles and subspace spears are not sent back.
	 */
	private void reflectProjectiles() {
		if (getShields() < MAX_SHIELDS) {
			return;
		}
		Vec3 look = getLookAngle();
		for (Projectile projectile : level().getEntitiesOfClass(Projectile.class, new AABB(position(), position()).inflate(2.5))) {
			if (projectile.getOwner() == this || projectile instanceof SkullMissileEntity || projectile instanceof SubspaceSpearEntity) {
				continue;
			}
			projectile.shoot(look.x, look.y, look.z, 1.5F, 0F);
			projectile.setOwner(this);
			projectile.hurtMarked = true;
		}
	}

	private void applyArenaRules(Player player) {
		// 和原版相同：前 90 秒是 1 级，之后是 3 级。
		// The same as in the original: level 1 for the first 90 seconds, then level 3.
		int amplifier = tickCount >= LATE_FIGHT_TICKS ? 2 : 0;
		MobEffectInstance curse = player.getEffect(ExtraBotanyMobEffects.WITCH_CURSE);
		// 效果快结束时才续上，这样不会每 tick 给玩家发一个效果数据包。
		// The effect is only applied again when it is near its end, thus the player does not get an effect packet on each tick.
		if (curse == null || curse.getAmplifier() < amplifier || curse.getDuration() < 100) {
			player.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.WITCH_CURSE, 200, amplifier));
		}
		if (Vec3.atCenterOf(getHome().pos()).distanceTo(player.position()) >= ARENA_RANGE) {
			player.addEffect(new MobEffectInstance(MobEffects.UNLUCK, 400, 4));
		}
	}

	// ---------------------------------------------------------------------
	// 普通攻击 / Usual attacks
	// ---------------------------------------------------------------------

	private void usePeriodicAttacks(List<Player> players) {
		if (rankIII && tickCount % 80 == 0) {
			spawnMissile(2);
			spawnMissile(2);
			heal(1F);
		}
		if (isRankII() && tickCount % 110 == 0) {
			spawnMissile(1);
		}
		if (tickCount > 60 && tickCount % 60 == 0) {
			spawnMissile(0);
			if (random.nextInt(10) < 4) {
				spawnMissile(1);
			}
		}
		int mineInterval = rankIII ? 220 : isRankII() ? 260 : 310;
		if (tickCount > 200 && tickCount % mineInterval == 0 && !players.isEmpty()) {
			spawnLandmines(players);
		}
	}

	/**
	 * @param level 0：普通导弹。1：带火焰。2：带火焰、负面效果和真实伤害。 / 0: usual missile. 1: with fire. 2: with fire, harmful effects and true damage.
	 */
	private void spawnMissile(int level) {
		SkullMissileEntity missile = new SkullMissileEntity(level(), this);
		missile.setPos(getX() + (random.nextDouble() - 0.5) * 0.1, getY() + 1.8 + (random.nextDouble() - 0.5) * 0.1, getZ() + (random.nextDouble() - 0.5) * 0.1);
		missile.setDamage(5);
		missile.setFire(level >= 1);
		if (level >= 2) {
			missile.setEffect(true);
			missile.setTrueDamage(2);
		}
		if (missile.findTarget()) {
			playSound(BotaniaSounds.MISSILE, 0.6F, 0.8F + random.nextFloat() * 0.2F);
			level().addFreshEntity(missile);
		}
	}

	private void spawnLandmines(List<Player> players) {
		BlockPos home = getHome().pos();
		int count = 7 + playerCount * 3 + (rankIII ? 6 : isRankII() ? 2 : 0);
		for (int i = 0; i < count; i++) {
			SkullLandMineEntity mine;
			if (i % 6 == 0) {
				mine = new SkullLandMineEntity.Danger(level(), this);
			} else if (i % 8 == 0) {
				mine = new SkullLandMineEntity.Disarm(level(), this);
			} else {
				mine = new SkullLandMineEntity.Default(level(), this);
			}
			int x = home.getX() - 10 + random.nextInt(20);
			int z = home.getZ() - 10 + random.nextInt(20);
			int y = (int) players.get(random.nextInt(players.size())).getY();
			mine.setDamage(6);
			mine.setPos(x + 0.5, y, z + 0.5);
			level().addFreshEntity(mine);
		}
	}

	/**
	 * 在头顶打开一个亚空间裂隙，它射出一把矛。
	 * Opens a subspace rift above the head, which shoots one spear.
	 */
	private void openSpearRift() {
		SubspaceEntity rift = new SubspaceEntity(level(), this, SubspaceEntity.Mode.SPEAR, 32, 12, 10,
				0.4F + random.nextFloat() * 0.15F, Mth.wrapDegrees(-getYRot() + 180));
		rift.setPos(getX(), getY() + 1.9, getZ());
		rift.setYRot(getYRot());
		level().addFreshEntity(rift);
	}

	private void spawnLance(double x, double y, double z, float damage, int life) {
		SubspaceLanceEntity lance = new SubspaceLanceEntity(level(), this, damage, life);
		lance.setPos(x, y, z);
		level().addFreshEntity(lance);
	}

	private void spawnLanceAbove(BlockPos pos) {
		spawnLance(pos.getX(), pos.getY() + LANCE_DROP_HEIGHT, pos.getZ(), 4, LANCE_LIFE);
	}

	private void spawnLanceRandomly() {
		BlockPos home = getHome().pos();
		spawnLanceAbove(BlockPos.containing(
				home.getX() + (random.nextDouble() - 0.5) * ARENA_RANGE,
				home.getY() + (isRankII() ? 2 : 0),
				home.getZ() + (random.nextDouble() - 0.5) * ARENA_RANGE));
	}

	// ---------------------------------------------------------------------
	// 特殊攻击 / Special attacks
	// ---------------------------------------------------------------------

	private void useSkills(List<Player> players) {
		if (!isRankII()) {
			return;
		}
		if (skillCooldown > 0) {
			skillCooldown--;
			// 原版的代码里有这两条预警，但是因为一个判断写错了，它们从来没有被发出去。
			// The code of the original has these two warnings, but because of an incorrect condition they are never sent.
			if (skillCooldown == 100 && skill == Skill.VOID_JUDGE && rankIII) {
				broadcast(players, "warning_judge");
			} else if (skillCooldown == 100 && skill == Skill.SMITE) {
				broadcast(players, "warning_target");
			}
			return;
		}
		switch (skill) {
			case SMITE -> {
				if (players.isEmpty()) {
					return;
				}
				smite(players.get(random.nextInt(players.size())));
				skillCooldown = 270;
				skill = rankIII ? Skill.VOID_JUDGE : random.nextBoolean() ? Skill.VOID_JUDGE : Skill.SMITE;
			}
			case VOID_JUDGE -> {
				if (rankIII) {
					useVoidJudge(players);
					skillCooldown = 250;
					skill = Skill.DIVINE_JUDGE;
				} else {
					// 原版在这里卡住：二阶段抽到"虚空裁决"之后，直到三阶段都不再使用特殊攻击。这里改为重新回到"点名"。
					// The original stops here: after rank II selects the "Void Judge", it uses no special attack until rank III. This port goes back to the "Smite".
					skillCooldown = 200;
					skill = Skill.SMITE;
				}
			}
			case DIVINE_JUDGE -> {
				useDivineJudge();
				skillCooldown = 290;
				skill = rankIII ? Skill.LANCE : Skill.SMITE;
			}
			case LANCE -> {
				spawnLanceRandomly();
				skillCooldown = 180;
				skill = Skill.SMITE;
			}
		}
	}

	private void broadcast(List<Player> players, String key) {
		for (Player player : players) {
			player.sendSystemMessage(Component.translatable("extrabotany.message.herrscher." + key).withStyle(ChatFormatting.RED));
		}
	}

	private void smite(Player player) {
		player.sendSystemMessage(Component.translatable("extrabotany.message.herrscher.smite").withStyle(ChatFormatting.RED));
		if (!player.getMainHandItem().isEmpty()) {
			player.getCooldowns().addCooldown(player.getMainHandItem().getItem(), 120);
		}
		DamageHandler.INSTANCE.dealTrueDamage(this, player, player.getMaxHealth() * 0.2F + 6);
		spawnLanceAbove(player.blockPosition());
	}

	private void useVoidJudge(List<Player> players) {
		BlockPos home = getHome().pos();
		setInvulTime(120);
		teleportTo(home.getX(), home.getY() + 2, home.getZ());
		if (players.isEmpty()) {
			return;
		}
		Vec3 look = getLookAngle().multiply(1, 0, 1);
		if (look.lengthSqr() < 1.0E-6) {
			double yaw = Math.toRadians(getYRot() + 90);
			look = new Vec3(Math.cos(yaw), 0, Math.sin(yaw));
		}
		look = look.normalize().scale(-2);
		Vec3 base = VecHelper.fromEntityCenter(this).add(look).add(0, 1.6, 0);
		for (int i = 0; i < 24; i++) {
			Vec3 position = base.add(0, 0, (i / 8) * 0.1).add(SpearOfSubspaceItem.domainOffset(look, i));
			SubspaceEntity rift = new SubspaceEntity(level(), this, SubspaceEntity.Mode.BOSS_BURSTS, 120, 15 + random.nextInt(12),
					10 + random.nextInt(10), 3F + random.nextFloat() * 0.5F, Mth.wrapDegrees(-getYRot() + 180));
			rift.setPos(position.x, position.y - 0.5 + random.nextFloat(), position.z);
			rift.setYRot(getYRot());
			level().addFreshEntity(rift);
		}
		playSound(ExtraBotanySounds.SPEAR_OF_SUBSPACE_USE, 1F, 1F);
	}

	/**
	 * 在场地上空召唤八把审判之剑。每把剑对应一名参战的玩家。玩家不足八名时，最后一名玩家对应剩下的剑。
	 * Summons eight swords of judgment above the arena. Each sword has one fighter as its target. When there are less than eight fighters, the last fighter is the target of the remaining swords.
	 */
	private void useDivineJudge() {
		List<UUID> targets = new ArrayList<>(getPlayersWhoAttacked());
		if (targets.isEmpty()) {
			return;
		}
		BlockPos home = getHome().pos();
		int y = home.getY() + 7;
		for (int i = 0; i < 8; i++) {
			double angle = Math.toRadians(i * 45);
			double x = home.getX() + 0.5 - Math.cos(angle) * 5;
			double z = home.getZ() + 0.5 - Math.sin(angle) * 5;
			SwordDomainEntity sword = new SwordDomainEntity(level(), targets.get(Math.min(i, targets.size() - 1)), home, i, y - 2);
			sword.setPos(x, y, z + 2);
			level().addFreshEntity(sword);
		}
	}

	// ---------------------------------------------------------------------
	// 支援 / Support
	// ---------------------------------------------------------------------

	/**
	 * 一名支援者帮助这名玩家。每名支援者在一场战斗里只出现一次。
	 * One supporter helps this player. Each supporter only comes one time in a fight.
	 */
	private void support(Player player) {
		if (supporters.isEmpty()) {
			return;
		}
		String supporter = supporters.remove(random.nextInt(supporters.size()));
		supportCooldown = 400;
		int kind = random.nextInt(7);
		// 每种支援有几条不同的消息。
		// Each kind of support has some different messages.
		int message = 0;
		switch (kind) {
			case 0 -> {
				message = random.nextInt(2);
				player.heal(player.getMaxHealth() * 0.25F);
			}
			case 1 -> hurt(damageSources().playerAttack(player), 15F);
			case 2 -> teleportDelay += 200;
			case 3 -> {
				message = random.nextInt(2);
				skillCooldown += 120;
			}
			case 4 -> {
				message = random.nextInt(3);
				supportCooldown = 200;
			}
			case 5 -> {
				message = random.nextInt(2);
				player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 300, 5));
			}
			default -> {
				message = random.nextInt(3);
				Item candy = message == 0 ? ExtraBotanyItems.candyEins : message == 1 ? ExtraBotanyItems.candyZwei : ExtraBotanyItems.candyDrei;
				for (int i = 0; i < 2; i++) {
					new ItemStack(candy).finishUsingItem(level(), player);
				}
			}
		}
		player.sendSystemMessage(Component.translatable("extrabotany.message.herrscher.support" + kind + message, supporter).withStyle(ChatFormatting.AQUA));
	}

	// ---------------------------------------------------------------------
	// 受伤 / Damage
	// ---------------------------------------------------------------------

	/**
	 * 一次攻击的伤害上限。每面护盾把它乘以 0.85 并去掉小数，这和原版相同：25、21、17、14。
	 * The damage limit of one attack. Each shield multiplies it by 0.85 and removes the fraction, which is the same as in the original: 25, 21, 17, 14.
	 */
	@Override
	public float getDamageCap() {
		int cap = HIT_CAP;
		for (int i = 0; i < getShields(); i++) {
			cap = (int) (cap * SHIELD_FACTOR);
		}
		return cap;
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (!(source.getEntity() instanceof Player player) || !PlayerHelper.isTruePlayer(player) || getInvulTime() > 0 || !Float.isFinite(amount)) {
			return false;
		}
		markPlayerAttacked(player);
		BlockPos home = getHome().pos();
		double dx = player.getX() - home.getX();
		double dz = player.getZ() - home.getZ();
		if (dx * dx + dz * dz > ARENA_RANGE * ARENA_RANGE) {
			player.teleportTo(home.getX() + 0.5, home.getY(), home.getZ() + 0.5);
		}
		if (skillCooldown > MIN_SKILL_COOLDOWN) {
			skillCooldown -= 15;
		}

		if (dodgeCooldown == 0) {
			dodge(player);
			return false;
		}

		int shields = getShields();
		if (shields > 0) {
			for (MobEffectInstance effect : player.getActiveEffects()) {
				addEffect(new MobEffectInstance(effect));
			}
			amount *= (float) Math.pow(SHIELD_FACTOR, shields);
		}
		// 上限按打碎护盾之前的护盾数计算。
		// The limit is calculated with the number of shields before a shield breaks.
		float dealt = Math.min(getDamageCap(), amount);
		if (shields > 0 && amount > SHIELD_BREAK_DAMAGE) {
			setShields(shields - 1);
			dodgeCooldown = 0;
		}

		damageTaken += dealt;
		if (damageTaken >= COUNTER_THRESHOLD) {
			damageTaken = 0;
			teleportRandomly();
			teleportDelay = 65;
			DamageHandler.INSTANCE.dealTrueDamage(this, player, player.getMaxHealth() * 0.1F + 6);
		}

		if (hitsToAbsorb > 0) {
			hitsToAbsorb--;
			dealt = 0F;
		}
		boolean hurt = super.hurt(source, dealt);
		setInvulTime(getInvulTime() + HIT_INVUL_TICKS);
		return hurt;
	}

	private void dodge(Player attacker) {
		VoidFieldEntity field = new VoidFieldEntity(level());
		field.setPos(attacker.getX(), attacker.getY() + 0.7, attacker.getZ());
		level().addFreshEntity(field);
		dodgeCooldown = DODGE_COOLDOWN;
		setInvulTime(getInvulTime() + HIT_INVUL_TICKS);
		if (random.nextFloat() < 0.2F) {
			spawnLanceAbove(attacker.blockPosition());
		}
	}

	@Override
	protected void actuallyHurt(DamageSource source, float amount) {
		super.actuallyHurt(source, amount);
		if (source.getDirectEntity() != null && getHealth() > 0) {
			teleportDelay = Math.max(teleportDelay - 4, 0);
		}
	}

	/**
	 * 生命值一次最多减少 25 点，这和原版相同。别的模组直接修改生命值时也受这个限制。
	 * The health decreases by a maximum of 25 at one time, which is the same as in the original. This limit also applies when a different mod changes the health directly.
	 */
	@Override
	public void setHealth(float health) {
		super.setHealth(killed ? health : Math.max(health, getHealth() - HIT_CAP));
	}

	@Override
	public void kill() {
		killed = true;
		super.kill();
	}

	private void teleportRandomly() {
		if (getInvulTime() > 10 || !(level() instanceof ServerLevel serverLevel)) {
			return;
		}
		BlockPos home = getHome().pos();
		Vec3 oldCenter = VecHelper.fromEntityCenter(this);
		double x = home.getX() + (random.nextDouble() - 0.5) * ARENA_RANGE;
		double y = home.getY() + (isRankII() ? 2 : 0);
		double z = home.getZ() + (random.nextDouble() - 0.5) * ARENA_RANGE;
		getNavigation().stop();
		teleportTo(x, y, z);
		Vec3 newCenter = VecHelper.fromEntityCenter(this);

		serverLevel.playSound(null, oldCenter.x, oldCenter.y, oldCenter.z, SoundEvents.ENDERMAN_TELEPORT, getSoundSource(), 1F, 1F);
		playSound(SoundEvents.ENDERMAN_TELEPORT, 1F, 1F);
		int steps = 16;
		for (int i = 0; i < steps; i++) {
			Vec3 point = oldCenter.lerp(newCenter, i / (double) (steps - 1));
			serverLevel.sendParticles(ParticleTypes.PORTAL, point.x, point.y, point.z, 8, getBbWidth(), getBbHeight() / 2, getBbWidth(), 0.1);
		}
		if (oldCenter.distanceToSqr(newCenter) > 1) {
			// 传送路径上的玩家受到伤害。
			// The players on the path of the teleport get damage.
			for (Player player : getPlayersAround()) {
				AABB box = player.getBoundingBox().inflate(0.25);
				if (box.contains(oldCenter) || box.contains(newCenter) || box.clip(oldCenter, newCenter).isPresent()) {
					player.hurt(damageSources().mobAttack(this), 6F);
				}
			}
		}
	}

	// ---------------------------------------------------------------------
	// 死亡 / Death
	// ---------------------------------------------------------------------

	@Override
	public void die(DamageSource source) {
		super.die(source);
		if (level() instanceof ServerLevel serverLevel) {
			if (tickCount <= QUICK_KILL_TICKS) {
				for (UUID uuid : getPlayersWhoAttacked()) {
					if (serverLevel.getPlayerByUUID(uuid) instanceof ServerPlayer player) {
						PlayerHelper.grantCriterion(player, prefix("main/endgame_goal"), "code_triggered");
					}
				}
			}
			playSound(SoundEvents.GENERIC_EXPLODE.value(), 20F, (1F + (random.nextFloat() - random.nextFloat()) * 0.2F) * 0.7F);
			serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, getX(), getY(), getZ(), 1, 0, 0, 0, 0);
		}
	}

	// ---------------------------------------------------------------------
	// 存档 / Save data
	// ---------------------------------------------------------------------

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putInt(TAG_HITS_TO_ABSORB, hitsToAbsorb);
		tag.putInt(TAG_SHIELDS, getShields());
		tag.putBoolean(TAG_RANK_II, isRankII());
		tag.putBoolean(TAG_RANK_III, rankIII);
		tag.putFloat(TAG_DAMAGE_TAKEN, damageTaken);
		tag.putInt(TAG_TELEPORT_DELAY, teleportDelay);
		tag.putInt(TAG_DODGE_COOLDOWN, dodgeCooldown);
		tag.putInt(TAG_SKILL_COOLDOWN, skillCooldown);
		tag.putInt(TAG_SKILL, skill.ordinal());
		tag.putInt(TAG_SUPPORT_COOLDOWN, supportCooldown);
		tag.putBoolean(TAG_FIRST_LANCE_DONE, firstLanceDone);
		ListTag names = new ListTag();
		for (String supporter : supporters) {
			names.add(StringTag.valueOf(supporter));
		}
		tag.put(TAG_SUPPORTERS, names);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		// 用命令召唤的 Boss 没有这些键。这时保留默认值。
		// A boss from a command does not have these keys. Then the defaults stay.
		if (!tag.contains(TAG_SKILL_COOLDOWN, Tag.TAG_ANY_NUMERIC)) {
			return;
		}
		hitsToAbsorb = tag.getInt(TAG_HITS_TO_ABSORB);
		setShields(tag.getInt(TAG_SHIELDS));
		rankIII = tag.getBoolean(TAG_RANK_III);
		entityData.set(RANK_II, tag.getBoolean(TAG_RANK_II) || rankIII);
		damageTaken = tag.getFloat(TAG_DAMAGE_TAKEN);
		teleportDelay = tag.getInt(TAG_TELEPORT_DELAY);
		dodgeCooldown = tag.getInt(TAG_DODGE_COOLDOWN);
		skillCooldown = tag.getInt(TAG_SKILL_COOLDOWN);
		skill = Skill.byId(tag.getInt(TAG_SKILL));
		supportCooldown = tag.getInt(TAG_SUPPORT_COOLDOWN);
		firstLanceDone = tag.getBoolean(TAG_FIRST_LANCE_DONE);
		if (tag.contains(TAG_SUPPORTERS, Tag.TAG_LIST)) {
			supporters.clear();
			for (Tag name : tag.getList(TAG_SUPPORTERS, Tag.TAG_STRING)) {
				supporters.add(name.getAsString());
			}
		}
	}
}
