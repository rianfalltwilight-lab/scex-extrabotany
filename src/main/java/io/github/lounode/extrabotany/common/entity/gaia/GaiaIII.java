package io.github.lounode.extrabotany.common.entity.gaia;

import com.mojang.serialization.Dynamic;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
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
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.common.handler.BotaniaSounds;
import vazkii.botania.common.helper.PlayerHelper;
import vazkii.botania.common.helper.VecHelper;

import io.github.lounode.extrabotany.api.gaia.GaiaArena;
import io.github.lounode.extrabotany.common.brew.ExtraBotanyMobEffects;
import io.github.lounode.extrabotany.common.entity.ExtraBotanyEntityType;
import io.github.lounode.extrabotany.common.entity.SkullLandMineEntity;
import io.github.lounode.extrabotany.common.entity.SkullMinionEntity;
import io.github.lounode.extrabotany.common.entity.SkullMissileEntity;
import io.github.lounode.extrabotany.common.entity.SwordDomainEntity;
import io.github.lounode.extrabotany.common.entity.gaia.behavior.GaiaSmashBlocksAround;
import io.github.lounode.extrabotany.common.handler.DamageHandler;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.lib.LibAdvancementNames;
import io.github.lounode.extrabotany.common.sounds.ExtraBotanySounds;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 盖亚守护者III。用挑战券在盖亚仪式场地上召唤。
 * Guardian of Gaia III. A Challenge Ticket summons it on a Ritual of Gaia arena.
 *
 * <ul>
 * <li>生命 600 × 在场人数，每次最多受 30 点伤害，生命一次最多减少 30。开场有 5 层护盾，每层挡掉一次伤害。
 * Health 600 × the players in the arena; at most 30 damage from one hit, and the health goes down by at most 30 at one time. It starts with 5 shields, and each shield stops one hit.</li>
 * <li>第二阶段（生命 ≤ 75%）和第三阶段（≤ 40%）：再得 5 层护盾，召唤 4 个骷髅仆从和 8 把审判之剑，并开始特殊攻击（点名、导弹雨、审判之剑、仆从）。
 * Rank II (health ≤ 75%) and rank III (≤ 40%): 5 more shields, 4 skull minions and 8 swords of judgment, and the special attacks start (a random player, a missile rain, the swords, the minions).</li>
 * <li>每 40 tick 一枚导弹，第二阶段每 55 tick 加一枚带火的，第三阶段每 65 tick 两枚带凋零的并回 1 点血。周期性地布下骷髅地雷阵。
 * One missile each 40 ticks, plus one with fire each 55 ticks in rank II, plus two with Wither each 65 ticks in rank III, which also heal it by 1. It lays skull landmine fields from time to time.</li>
 * <li>场内玩家得到巫毒诅咒，离开场地的被拉回并得到霉运；不能飞。受到 80 点伤害后瞬移。没打过盖亚守护者III的玩家受到的导弹、地雷和点名伤害 × 0.7。
 * The players in the arena get Witch Curse; a player who leaves is pulled back and gets Bad Luck; nobody can fly. After 80 damage it teleports. A player who has not beaten Guardian of Gaia III takes 0.7 times the damage of missiles, landmines and the random-player attack.</li>
 * </ul>
 *
 * <p>来源：ExtraMeteorP 1.12 {@code EntityGaiaIII}、{@code EntitySkullMinion}、{@code EntitySkullMissile}、{@code EntitySkullLandmine}。1.12 的困难模式没有任何入口，这里不做。重燃的设计（人数倍率 1 + 0.25n、护甲 30、200 tick 出场无敌、每 15 tick 四枚导弹、每次传送放地雷、半径 15 的场地）已经去掉。
 * <p>Source: {@code EntityGaiaIII}, {@code EntitySkullMinion}, {@code EntitySkullMissile} and {@code EntitySkullLandmine} of ExtraMeteorP 1.12. The hard mode of 1.12 had no way to start it and is not made here. The design of Reburn (a player factor of 1 + 0.25n, 30 armor, 200 ticks of invulnerability at the start, four missiles each 15 ticks, landmines at each teleport, an arena of radius 15) is removed.
 */
public class GaiaIII extends Gaia {
	public static final float ARENA_RANGE = 12F;
	public static final int ARENA_HEIGHT = 5;
	public static final float MAX_HP = 600F;
	public static final float DAMAGE_CAP = 30;
	public static final int SHIELDS_PER_RANK = 5;
	public static final float NOVICE_FACTOR = 0.7F;
	// Source: ExtraMeteorP 1.12 EntityGaiaIII (experienceValue = 1225).
	public static final int XP_REWARD = 1225;

	private static final int START_DELAY = 60;
	private static final float COUNTER_THRESHOLD = 80F;
	private static final BlockPos[] MINION_LOCATIONS = {
			new BlockPos(3, 1, 3), new BlockPos(3, 1, -3), new BlockPos(-3, 1, 3), new BlockPos(-3, 1, -3)
	};

	private static final String TAG_SHIELD = "shield";
	private static final String TAG_RANK_II = "rank2";
	private static final String TAG_RANK_III = "rank3";
	private static final String TAG_DAMAGE_TAKEN = "damagetaken";
	private static final String TAG_COOLDOWN = "skillCd";
	private static final String TAG_SKILL = "skillType";
	private static final String TAG_TELEPORT_DELAY = "tpDelay";

	private static final EntityDataAccessor<Integer> SHIELD = SynchedEntityData.defineId(GaiaIII.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> RANK_II = SynchedEntityData.defineId(GaiaIII.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> RANK_III = SynchedEntityData.defineId(GaiaIII.class, EntityDataSerializers.BOOLEAN);

	private float damageTaken;
	private int skillCooldown = 200;
	private int skillType;
	private int teleportDelay;
	private boolean killed;
	private boolean readingSaveData;

	public GaiaIII(EntityType<? extends GaiaIII> type, Level world) {
		super(type, world);
		this.xpReward = XP_REWARD;
	}

	public GaiaIII(EntityType<? extends GaiaIII> type, Level world, BlockPos source) {
		super(type, world, source);
		// Gaia(type, world, source) calls the constructor of Gaia, not the one above, so the reward must be set here too.
		this.xpReward = XP_REWARD;
	}

	public GaiaIII(Level world, BlockPos source) {
		this(ExtraBotanyEntityType.GAIA_III, world, source);
	}

	public static AttributeSupplier.Builder createGaiaAttributes() {
		return Monster.createMonsterAttributes()
				.add(Attributes.MOVEMENT_SPEED, 0.4)
				.add(Attributes.MAX_HEALTH, MAX_HP)
				.add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
	}

	/**
	 * 尝试召唤。检查没有通过时，玩家会收到说明原因的消息。
	 * Tries to summon it. When a check fails, the player gets a message with the cause.
	 *
	 * @param pos 信标的位置 / the position of the beacon
	 */
	public static boolean spawn(Player player, ItemStack stack, Level world, BlockPos pos) {
		GaiaArena arena = GaiaArena.of(GlobalPos.of(world.dimension(), pos), ARENA_RANGE, ARENA_HEIGHT);
		if (!arena.checksVanilla(player, world, pos) || arena.countGaiaAround(world, Gaia.class) > 0) {
			return false;
		}
		if (!arena.checkGuardianInventoryStrict(world, ExtraBotanyItems.voidArchives)) {
			if (!world.isClientSide()) {
				player.sendSystemMessage(Component.translatable("extrabotany.message.guardian_no_response").withStyle(ChatFormatting.RED));
			}
			return false;
		}
		if (world instanceof ServerLevel serverLevel) {
			GaiaIII gaia = new GaiaIII(world, pos);
			gaia.setArena(arena);
			gaia.setPos(pos.getX() + 0.5, pos.getY() + 3, pos.getZ() + 0.5);
			List<Player> players = arena.getPlayersAround(world);
			gaia.playerCount = Math.max(1, players.size());
			gaia.bossEvent.setPlayerCount(gaia.playerCount);
			gaia.getAttribute(Attributes.MAX_HEALTH).setBaseValue(MAX_HP * gaia.playerCount);
			gaia.setHealth(gaia.getMaxHealth());
			gaia.setShield(SHIELDS_PER_RANK);
			gaia.setCustomName(player.getName());
			EventHooks.finalizeMobSpawn(gaia, serverLevel, world.getCurrentDifficultyAt(gaia.blockPosition()), MobSpawnType.EVENT, null);
			if (!world.addFreshEntity(gaia)) {
				return false;
			}
			serverLevel.playSound(null, pos, SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 10F, 0.1F);
			for (Player nearby : players) {
				if (nearby instanceof ServerPlayer serverPlayer) {
					CriteriaTriggers.SUMMONED_ENTITY.trigger(serverPlayer, gaia);
				}
			}
		}
		return true;
	}

	/**
	 * 没打过盖亚守护者III的玩家受到的伤害倍数（1.12 {@code StatHandler.hasStat(player, GAIA_DEFEAT)}）。
	 * The damage factor for a player who has not beaten Guardian of Gaia III (1.12 {@code StatHandler.hasStat(player, GAIA_DEFEAT)}).
	 */
	public static float noviceFactor(Player player) {
		if (player instanceof ServerPlayer serverPlayer
				&& !PlayerHelper.hasAdvancement(serverPlayer, prefix("main/" + LibAdvancementNames.GAIA_TRIAL))) {
			return NOVICE_FACTOR;
		}
		return 1F;
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
	public ResourceKey<LootTable> getDefaultLootTable() {
		return this.getType().getDefaultLootTable();
	}

	@Override
	public float getArenaRange() {
		return ARENA_RANGE;
	}

	@Override
	public float getDamageCap() {
		return DAMAGE_CAP;
	}

	@Override
	public int getEmergeTime() {
		return 0;
	}

	@Override
	protected double getHurtHopStrength() {
		return 0.2;
	}

	@Override
	protected double getHurtHopHeight() {
		return 0.25;
	}

	@Override
	public SoundEvent getBGM() {
		return ExtraBotanySounds.MUSIC_GAIA3;
	}

	@Override
	public Item getGuardianBypassItem() {
		return ExtraBotanyItems.voidArchives;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(SHIELD, 0);
		builder.define(RANK_II, false);
		builder.define(RANK_III, false);
	}

	public int getShield() {
		return entityData.get(SHIELD);
	}

	public void setShield(int shield) {
		entityData.set(SHIELD, Math.max(0, shield));
	}

	public boolean isRankII() {
		return entityData.get(RANK_II);
	}

	public boolean isRankIII() {
		return entityData.get(RANK_III);
	}

	public int getSkillCooldown() {
		return skillCooldown;
	}

	public int getSkillType() {
		return skillType;
	}

	// ---------------------------------------------------------------------
	// 每 tick 的逻辑 / Logic for each tick
	// ---------------------------------------------------------------------

	@Override
	protected void customServerAiStep() {
		super.customServerAiStep();
		if (tickCount < START_DELAY) {
			return;
		}
		if (level().getDifficulty() == Difficulty.PEACEFUL) {
			discard();
			return;
		}
		List<Player> players = getPlayersAround();
		if (players.size() > Math.max(1, playerCount)) {
			broadcast(players, "illegal_battle", ChatFormatting.RED);
			discard();
			return;
		}
		if (players.isEmpty()) {
			if (!level().players().isEmpty()) {
				discard();
			}
			return;
		}
		lookAt(players.getFirst(), 360F, 360F);
		players.forEach(this::markPlayerAttacked);

		updateRank(players);
		players.forEach(this::applyArenaRules);
		usePeriodicAttacks(players);
		useSkills(players);

		if (teleportDelay > 0) {
			teleportDelay--;
		}
		if (teleportDelay == 0 && getHealth() > 0) {
			teleportRandomly();
			teleportDelay = isRankIII() ? 85 : 100;
		}
	}

	private void updateRank(List<Player> players) {
		if (!isRankII() && getHealth() <= getMaxHealth() * 0.75F) {
			entityData.set(RANK_II, true);
			enterRank(players);
		}
		if (!isRankIII() && getHealth() <= getMaxHealth() * 0.4F) {
			entityData.set(RANK_III, true);
			enterRank(players);
		}
	}

	private void enterRank(List<Player> players) {
		setShield(SHIELDS_PER_RANK);
		spawnMinions();
		spawnDivineJudge();
		broadcast(players, "minion_spawn", ChatFormatting.WHITE);
	}

	private void applyArenaRules(Player player) {
		BlockPos home = getHome().pos();
		// 1.12 的写法因为运算符优先级只有 0 级和（90 秒后）2 级两种。 / Because of operator precedence, 1.12 only has level 0 and (after 90 seconds) level 2.
		int amplifier = tickCount >= 1800 ? 2 : 0;
		MobEffectInstance curse = player.getEffect(ExtraBotanyMobEffects.WITCH_CURSE);
		if (curse == null || curse.getAmplifier() < amplifier || curse.getDuration() < 100) {
			player.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.WITCH_CURSE, 200, amplifier));
		}
		Vec3 center = Vec3.atCenterOf(home);
		if (center.distanceTo(player.position()) >= ARENA_RANGE) {
			Vec3 motion = center.subtract(VecHelper.fromEntityCenter(player)).normalize();
			player.setDeltaMovement(motion.x, 0.2, motion.z);
			player.hurtMarked = true;
			if (player.getVehicle() != null) {
				player.getVehicle().setDeltaMovement(motion.x, 0.2, motion.z);
				player.getVehicle().hurtMarked = true;
			}
			player.addEffect(new MobEffectInstance(MobEffects.UNLUCK, 400, 4));
		}
	}

	private void usePeriodicAttacks(List<Player> players) {
		if (isRankIII() && tickCount % 65 == 0) {
			spawnMissile(2);
			spawnMissile(2);
			heal(1F);
		}
		if (isRankII() && tickCount % 55 == 0) {
			spawnMissile(1);
		}
		if (tickCount % 40 == 0) {
			spawnMissile(0);
			if (random.nextInt(10) < 4) {
				spawnMissile(1);
			}
		}
		int interval = isRankIII() ? 170 : isRankII() ? 210 : 250;
		if (tickCount > 200 && tickCount % interval == 0) {
			spawnLandmines(players);
		}
	}

	private void useSkills(List<Player> players) {
		if (!isRankII()) {
			return;
		}
		if (skillCooldown > 0) {
			skillCooldown--;
			if (skillCooldown == 250 && skillType == 1) {
				broadcast(players, "preparing", ChatFormatting.WHITE);
			} else if (skillCooldown == 100 && skillType == 1) {
				broadcast(players, "warning", ChatFormatting.RED);
			} else if (skillCooldown == 100 && skillType == 0) {
				broadcast(players, "warning_target", ChatFormatting.RED);
			}
			return;
		}
		switch (skillType) {
			case 0 -> {
				Player target = players.get(random.nextInt(players.size()));
				target.sendSystemMessage(Component.translatable("extrabotany.message.gaia_iii.smite", getDisplayName()).withStyle(ChatFormatting.RED));
				DamageHandler.INSTANCE.dealTrueDamage(this, target, (target.getMaxHealth() * 0.2F + 6) * noviceFactor(target));
				skillCooldown = 380;
				skillType = isRankIII() ? 1 : random.nextInt(2);
			}
			case 1 -> {
				if (isRankIII()) {
					for (int i = 0; i < 17 + playerCount * 4; i++) {
						spawnMissile(3);
					}
					skillCooldown = 220;
					skillType = 2;
				} else {
					// 1.12 在二阶段抽到 1 号技能后什么也不做，冷却停在 0，直到三阶段。这里改回点名，避免卡住。
					// In rank II, 1.12 does nothing after it selects skill 1, and the cooldown stays at 0 until rank III. Here it goes back to the random-player attack, so it does not stop.
					skillCooldown = 200;
					skillType = 0;
				}
			}
			case 2 -> {
				spawnDivineJudge();
				skillCooldown = 320;
				skillType = isRankIII() ? 3 : 0;
			}
			default -> {
				spawnMinions();
				skillCooldown = 360;
				skillType = 0;
			}
		}
	}

	private void broadcast(List<Player> players, String key, ChatFormatting color) {
		for (Player player : players) {
			player.sendSystemMessage(Component.translatable("extrabotany.message.gaia_iii." + key, getDisplayName()).withStyle(color));
		}
	}

	// ---------------------------------------------------------------------
	// 召唤物 / Summons
	// ---------------------------------------------------------------------

	private void spawnMinions() {
		BlockPos home = getHome().pos();
		for (int i = 0; i < MINION_LOCATIONS.length; i++) {
			SkullMinionEntity minion = new SkullMinionEntity(ExtraBotanyEntityType.SKULL_MINION, level());
			BlockPos pos = home.offset(MINION_LOCATIONS[i]);
			minion.setPos(pos.getX(), pos.getY(), pos.getZ());
			minion.setMinionType(i);
			minion.setShield(2);
			level().addFreshEntity(minion);
		}
	}

	/**
	 * 在场地上空召唤八把审判之剑。每把剑对应一名参战的玩家；玩家不足八名时，最后一名玩家对应剩下的剑。
	 * Summons eight swords of judgment above the arena. Each sword has one fighter as its target; with less than eight fighters, the last one is the target of the remaining swords.
	 */
	private void spawnDivineJudge() {
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

	/**
	 * @param type 0：普通（5 + 2 点真实伤害）。1：带火。2：再带凋零，真实伤害 3。3：同 2，从场地里随机的位置发射。
	 *             0: usual (5 + 2 true damage). 1: with fire. 2: also with Wither, 3 true damage. 3: as 2, shot from a random position in the arena.
	 */
	private void spawnMissile(int type) {
		SkullMissileEntity missile = new SkullMissileEntity(level(), this);
		missile.setPos(getX() + (random.nextDouble() - 0.5) * 0.1, getY() + 1.8 + (random.nextDouble() - 0.5) * 0.1, getZ() + (random.nextDouble() - 0.5) * 0.1);
		missile.setDamage(5);
		missile.setTrueDamage(2);
		missile.setFire(type > 0);
		if (type > 1) {
			missile.setEffect(true);
			missile.setTrueDamage(3);
		}
		if (missile.findTarget()) {
			if (type > 2) {
				BlockPos home = getHome().pos();
				missile.setPos(home.getX() - 10 + random.nextInt(20), getY() + 1.8, home.getZ() - 10 + random.nextInt(20));
			}
			playSound(BotaniaSounds.MISSILE, 0.6F, 0.8F + random.nextFloat() * 0.2F);
			level().addFreshEntity(missile);
		}
	}

	private void spawnLandmines(List<Player> players) {
		BlockPos home = getHome().pos();
		int base = 8 + playerCount * 3;
		int count = isRankIII() ? base + 9 : isRankII() ? base + 5 : base;
		for (int i = 0; i < count; i++) {
			// 1.12：先看 6 的倍数（绿色），再看 5 的倍数（红色）覆盖。 / 1.12: multiples of 6 first (green), then multiples of 5 (red) override.
			SkullLandMineEntity mine;
			if (i % 5 == 0) {
				mine = new SkullLandMineEntity.Danger(level(), this);
			} else if (i % 6 == 0) {
				mine = new SkullLandMineEntity.Disarm(level(), this);
			} else {
				mine = new SkullLandMineEntity.Default(level(), this);
			}
			int x = home.getX() - 10 + random.nextInt(20);
			int z = home.getZ() - 10 + random.nextInt(20);
			int y = (int) players.get(random.nextInt(players.size())).getY();
			mine.setPos(x + 0.5, y, z + 0.5);
			level().addFreshEntity(mine);
		}
	}

	// ---------------------------------------------------------------------
	// 受伤 / Damage
	// ---------------------------------------------------------------------

	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (!(source.getEntity() instanceof Player player) || !PlayerHelper.isTruePlayer(player) || !Float.isFinite(amount)) {
			return false;
		}
		markPlayerAttacked(player);
		BlockPos home = getHome().pos();
		double dx = player.getX() - home.getX();
		double dz = player.getZ() - home.getZ();
		if (dx * dx + dz * dz > ARENA_RANGE * ARENA_RANGE) {
			player.teleportTo(home.getX(), home.getY(), home.getZ());
		}
		if (skillCooldown > 160) {
			skillCooldown -= 10;
		}
		float dealt = Math.min(DAMAGE_CAP, amount);
		// 1.12 的护盾：每层挡掉一次伤害。 / The shield of 1.12: each shield stops one hit.
		if (getShield() > 0) {
			setShield(getShield() - 1);
			dealt = 0F;
		}
		damageTaken += Math.min(DAMAGE_CAP, amount);
		if (damageTaken >= COUNTER_THRESHOLD) {
			damageTaken = 0;
			teleportRandomly();
			teleportDelay = 80;
		}
		return super.hurt(source, dealt);
	}

	@Override
	protected void actuallyHurt(DamageSource source, float amount) {
		super.actuallyHurt(source, amount);
		if (source.getDirectEntity() != null && getHealth() > 0) {
			teleportDelay = Math.max(teleportDelay - 3, 0);
		}
	}

	/**
	 * 生命值一次最多减少 30 点，这和原版相同。别的模组直接修改生命值时也受这个限制。
	 * The health goes down by at most 30 at one time, which is the same as in the original. This limit also applies when a different mod changes the health directly.
	 */
	@Override
	public void setHealth(float health) {
		super.setHealth(killed || readingSaveData ? health : Math.max(health, getHealth() - DAMAGE_CAP));
	}

	@Override
	public void kill() {
		killed = true;
		super.kill();
	}

	private void teleportRandomly() {
		if (!(level() instanceof ServerLevel serverLevel)) {
			return;
		}
		BlockPos home = getHome().pos();
		Vec3 oldCenter = VecHelper.fromEntityCenter(this);
		double x = home.getX() + 0.5;
		double y = home.getY();
		double z = home.getZ() + 0.5;
		for (int tries = 0; tries < 50; tries++) {
			double nx = home.getX() + (random.nextDouble() - 0.5) * ARENA_RANGE;
			double nz = home.getZ() + (random.nextDouble() - 0.5) * ARENA_RANGE;
			if (Mth.length(nx - home.getX(), nz - home.getZ()) <= 12) {
				x = nx;
				z = nz;
				break;
			}
		}
		BlockPos below = BlockPos.containing(x, y - 1, z);
		if (level().getBlockState(below).getCollisionShape(level(), below).isEmpty()) {
			y--;
		}
		getNavigation().stop();
		teleportTo(x, y, z);
		Vec3 newCenter = VecHelper.fromEntityCenter(this);
		serverLevel.playSound(null, oldCenter.x, oldCenter.y, oldCenter.z, SoundEvents.ENDERMAN_TELEPORT, getSoundSource(), 1F, 1F);
		playSound(SoundEvents.ENDERMAN_TELEPORT, 1F, 1F);
		for (int i = 0; i < 16; i++) {
			Vec3 point = oldCenter.lerp(newCenter, i / 15.0);
			serverLevel.sendParticles(ParticleTypes.PORTAL, point.x, point.y, point.z, 8, getBbWidth(), getBbHeight() / 2, getBbWidth(), 0.1);
		}
		if (oldCenter.distanceToSqr(newCenter) > 1) {
			for (Player player : getPlayersAround()) {
				AABB box = player.getBoundingBox().inflate(0.25);
				if (box.contains(oldCenter) || box.contains(newCenter) || box.clip(oldCenter, newCenter).isPresent()) {
					player.hurt(damageSources().mobAttack(this), 6F);
				}
			}
			int steps = (int) oldCenter.distanceTo(newCenter);
			for (int i = 0; steps >= 2 && i < steps; i++) {
				Vec3 point = oldCenter.lerp(newCenter, i / (double) (steps - 1));
				GaiaSmashBlocksAround.smashBlocksAround(this, Mth.floor(point.x), Mth.floor(point.y), Mth.floor(point.z), 1);
			}
		}
	}

	@Override
	public void die(DamageSource source) {
		super.die(source);
		if (level() instanceof ServerLevel serverLevel) {
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
		tag.putInt(TAG_SHIELD, getShield());
		tag.putBoolean(TAG_RANK_II, isRankII());
		tag.putBoolean(TAG_RANK_III, isRankIII());
		tag.putFloat(TAG_DAMAGE_TAKEN, damageTaken);
		tag.putInt(TAG_COOLDOWN, skillCooldown);
		tag.putInt(TAG_SKILL, skillType);
		tag.putInt(TAG_TELEPORT_DELAY, teleportDelay);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		readingSaveData = true;
		try {
			super.readAdditionalSaveData(tag);
		} finally {
			readingSaveData = false;
		}
		setShield(tag.getInt(TAG_SHIELD));
		entityData.set(RANK_II, tag.getBoolean(TAG_RANK_II));
		entityData.set(RANK_III, tag.getBoolean(TAG_RANK_III));
		damageTaken = tag.getFloat(TAG_DAMAGE_TAKEN);
		if (tag.contains(TAG_COOLDOWN, Tag.TAG_ANY_NUMERIC)) {
			skillCooldown = tag.getInt(TAG_COOLDOWN);
			skillType = tag.getInt(TAG_SKILL);
			teleportDelay = tag.getInt(TAG_TELEPORT_DELAY);
		}
	}
}
