package io.github.lounode.extrabotany.common.entity.gaia;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Unit;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import vazkii.botania.common.handler.BotaniaSounds;
import vazkii.botania.common.helper.PlayerHelper;
import vazkii.botania.common.helper.VecHelper;

import io.github.lounode.extrabotany.api.gaia.GaiaArena;
import io.github.lounode.extrabotany.common.bossevents.ServerGaiaBossEvent;
import io.github.lounode.extrabotany.common.entity.ExtraBotanyEntityType;
import io.github.lounode.extrabotany.common.proxy.Proxy;
import io.github.lounode.extrabotany.common.telemetry.ExtraBotanyTelemetry;
import io.github.lounode.extrabotany.network.clientbound.SpawnGaiaPacket;
import io.github.lounode.extrabotany.xplat.EXplatAbstractions;

import java.util.*;

import static vazkii.botania.common.helper.PlayerHelper.isTruePlayer;

public class Gaia extends Monster {

	private static final Logger LOGGER = LogUtils.getLogger();
	public static final float MAX_HP = 320F;
	public static final float DAMAGE_CAP = 32;

	private static final String TAG_INVUL_TIME = "InvulTime";
	private static final String TAG_HOME = "Home";
	private static final String TAG_PLAYER_COUNT = "PlayerCount";
	private static final String TAG_ARENA = "Arena";
	private static final String TAG_PLAYER_ATTACKED = "PlayersAttacked";
	private static final BlockPos NO_HOME = new BlockPos(0, -2048, 0);

	private static final EntityDataAccessor<Integer> INVUL_TIME = SynchedEntityData.defineId(Gaia.class, EntityDataSerializers.INT);

	protected int playerCount = 0;
	protected ServerGaiaBossEvent bossEvent;

	private final Set<UUID> playersWhoAttacked = new HashSet<>();
	@NotNull
	private GlobalPos home;
	@Nullable
	private GaiaArena arena;

	public Gaia(EntityType<? extends Gaia> type, Level world) {
		super(type, world);
		this.xpReward = 825;
		this.setHealth(getMaxHealth());
		this.setHome(GlobalPos.of(world.dimension(), NO_HOME));
		//TODO 持久化玩家数量
		this.bossEvent = (ServerGaiaBossEvent) new ServerGaiaBossEvent(type.getDescription(), BossEvent.BossBarColor.PINK, BossEvent.BossBarOverlay.PROGRESS).setCreateWorldFog(true);
		if (world.isClientSide) {
			Proxy.INSTANCE.addBoss(this);
		}
	}

	public Gaia(EntityType<? extends Gaia> type, Level world, BlockPos source) {
		this(type, world);
		this.setHome(GlobalPos.of(world.dimension(), source));
	}

	public Gaia(Level world, BlockPos source) {
		this(ExtraBotanyEntityType.GAIA_LEGACY, world, source);
	}

	public static AttributeSupplier.Builder createGaiaAttributes() {
		return Monster.createMonsterAttributes()
				.add(Attributes.MOVEMENT_SPEED, 0.4)
				.add(Attributes.MAX_HEALTH, MAX_HP)
				.add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
	}

	@Override
	protected Brain<?> makeBrain(Dynamic<?> dynamic) {
		return GaiaAI.makeBrain(this, dynamic);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Brain<Gaia> getBrain() {
		return (Brain<Gaia>) super.getBrain();
	}

	public static boolean spawn(Player player, ItemStack stack, Level world, BlockPos pos) {
		//initial checks
		GaiaArena arena = GaiaArena.of(GlobalPos.of(world.dimension(), pos));
		if (!arena.checksVanilla(player, world, pos)) {
			return false;
		}

		//all checks ok, spawn the boss
		if (!world.isClientSide()) {
			stack.shrink(1);

			Gaia gaia = new Gaia(world, pos);
			gaia.setArena(arena);
			gaia.setPos(pos.getX() + 0.5, pos.getY() + 3, pos.getZ() + 0.5);

			gaia.getBrain().setMemoryWithExpiry(MemoryModuleType.IS_EMERGING, Unit.INSTANCE, (long) GaiaAI.EMERGE_TIME);
			gaia.setInvulTime(GaiaAI.EMERGE_TIME);
			gaia.bossEvent.setProgress(0.0F);
			gaia.setHealth(1F);

			List<Player> playersAround = arena.getPlayersAround(world);

			int playerCount = playersAround.size();
			gaia.playerCount = playerCount;
			gaia.bossEvent.setPlayerCount(playerCount);

			float healthMultiplier = 1;
			if (playerCount > 1) {
				healthMultiplier += playerCount * 0.25F;
			}
			gaia.getAttribute(Attributes.MAX_HEALTH).setBaseValue(MAX_HP * healthMultiplier);
			gaia.getAttribute(Attributes.ARMOR).setBaseValue(15);

			gaia.playSound(BotaniaSounds.GAIA_SUMMON, .1F, 1F);
			EventHooks.finalizeMobSpawn(gaia, (ServerLevelAccessor) world,
					world.getCurrentDifficultyAt(gaia.blockPosition()), MobSpawnType.EVENT, null);
			world.addFreshEntity(gaia);

			for (Player nearbyPlayer : playersAround) {
				if (nearbyPlayer instanceof ServerPlayer serverPlayer) {
					CriteriaTriggers.SUMMONED_ENTITY.trigger(serverPlayer, gaia);
				}
			}
		}

		return true;
	}

	@Override
	@SuppressWarnings("deprecation") // Override-only hook; external callers use EventHooks.finalizeMobSpawn.
	public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData spawnData) {
		if (this.getHome().pos().equals(NO_HOME)) {
			this.setHome(GlobalPos.of(level.getLevel().dimension(), this.findSafeHomePos()));
		}
		initMemories(level, difficulty, reason, spawnData);

		return super.finalizeSpawn(level, difficulty, reason, spawnData);
	}

	private BlockPos findSafeHomePos() {
		BlockPos spawnPos = this.getOnPos();
		Level level = this.level();

		for (int i = 0; i <= 10; i++) {
			BlockPos checkPos = spawnPos.below(i);

			if (level.getBlockState(checkPos).isFaceSturdy(level, checkPos, Direction.UP)) {
				return checkPos.above();
			}
		}

		return spawnPos;
	}

	protected void initMemories(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData spawnData) {
		GaiaAI.initMemories(this, level.getLevel(), getHome().pos());
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, getArenaRange() * 1.5F));
	}

	@Override
	protected void customServerAiStep() {
		ServerLevel serverlevel = (ServerLevel) this.level();
		serverlevel.getProfiler().push("gaiaBrain");
		getBrain().tick(serverlevel, this);
		this.level().getProfiler().pop();
		super.customServerAiStep();

		updateAI();

		int invul = getInvulTime();
		if (invul > 0) {
			setInvulTime(invul - 1);
		}

		bossEvent.setProgress(getHealth() / getMaxHealth());

		if (this.isPassenger()) {
			this.stopRiding();
		}

		List<Player> players = getPlayersAround();
		for (Player player : players) {
			if (player.isSleeping()) {
				player.stopSleeping();
			}
			if (player.isPassenger()) {
				player.stopRiding();
			}

			getArena().ifPresent(arena -> {
				player.getAbilities().flying &= player.getAbilities().instabuild;
			});
		}
	}

	protected void updateAI() {
		GaiaAI.updateActivity(this);
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (this.arena != null && this.isAlive() && !isNoAi()) {
			this.arena.tick(this);
		}
	}

	///
	@Override
	public boolean hurt(@NotNull DamageSource source, float amount) {
		Entity e = source.getEntity();
		if (e instanceof Player player && isTruePlayer(e) && getInvulTime() == 0) {

			markPlayerAttacked(player);

			return super.hurt(source, Math.min(getDamageCap(), amount));
		}

		return false;
	}

	@Override
	protected void actuallyHurt(@NotNull DamageSource source, float amount) {
		super.actuallyHurt(source, Math.min(getDamageCap(), amount));

		Entity attacker = source.getDirectEntity();
		if (attacker != null) {
			Vec3 thisVector = VecHelper.fromEntityCenter(this);
			Vec3 playerVector = VecHelper.fromEntityCenter(attacker);
			Vec3 motionVector = thisVector.subtract(playerVector).normalize().scale(0.75);

			if (getHealth() > 0) {
				setDeltaMovement(-motionVector.x, 0.5, -motionVector.z);
			}
		}
		invulnerableTime = Math.max(invulnerableTime, 20);
	}

	@Override
	public void die(@NotNull DamageSource source) {
		super.die(source);
		LivingEntity lastAttacker = getKillCredit();

		getArena().ifPresent(arena -> {
			arena.cleanup(this.level());
			ExtraBotanyTelemetry.Event.onGaiaBattleFinish(this);
		});

		if (!level().isClientSide) {
			for (UUID u : getPlayersWhoAttacked()) {
				Player player = level().getPlayerByUUID(u);
				if (!isTruePlayer(player)) {
					continue;
				}
				DamageSource currSource = player == lastAttacker ? source : player.damageSources().playerAttack(player);
				if (player != lastAttacker) {
					// Vanilla handles this in attack code, but only for the killer
					CriteriaTriggers.PLAYER_KILLED_ENTITY.trigger((ServerPlayer) player, this, currSource);
				}
			}
		}

		playSound(BotaniaSounds.GAIA_DEATH, 1F, (1F + (level().random.nextFloat() - level().random.nextFloat()) * 0.2F) * 0.7F);
		level().addParticle(ParticleTypes.EXPLOSION_EMITTER, getX(), getY(), getZ(), 1D, 0D, 0D);
	}

	@Override
	protected void dropFromLootTable(@NotNull DamageSource source, boolean wasRecentlyHit) {
		// Save true killer, they get extra loot
		//if (wasRecentlyHit && isTruePlayer(source.getEntity())) {
		//    trueKiller = (Player) source.getEntity();
		//}

		// Generate loot table for every single attacking player
		for (UUID u : playersWhoAttacked) {
			Player player = level().getPlayerByUUID(u);
			if (!isTruePlayer(player)) {
				continue;
			}

			Player saveLastAttacker = lastHurtByPlayer;
			Vec3 savePos = position();

			lastHurtByPlayer = player; // Fake attacking player as the killer
			// Spoof pos so drops spawn at the player
			setPos(player.getX(), player.getY(), player.getZ());
			super.dropFromLootTable(player.damageSources().playerAttack(player), wasRecentlyHit);
			setPos(savePos.x(), savePos.y(), savePos.z());
			lastHurtByPlayer = saveLastAttacker;
		}

		//trueKiller = null;
	}

	///
	@Override
	protected float getDamageAfterArmorAbsorb(DamageSource source, float damage) {
		return super.getDamageAfterArmorAbsorb(source, Math.min(DAMAGE_CAP, damage));
	}

	@Override
	public void heal(float amount) {
		if (getInvulTime() == 0) {
			super.heal(amount);
		}
	}

	@Override
	public void kill() {
		//this.remove(Entity.RemovalReason.KILLED);
		//this.gameEvent(GameEvent.ENTITY_DIE);
		this.setHealth(0.0F);
		this.bossEvent.setProgress(0.0F);
		getArena().ifPresent(arena -> {
			arena.cleanup(this.level());
		});
	}

	@Override
	public void remove(RemovalReason reason) {
		if (level().isClientSide) {
			Proxy.INSTANCE.removeBoss(this);
		}
		super.remove(reason);
	}

	@Override
	public void startSeenByPlayer(ServerPlayer player) {
		super.startSeenByPlayer(player);
		bossEvent.addPlayer(player);

	}

	@Override
	public void stopSeenByPlayer(ServerPlayer player) {
		super.stopSeenByPlayer(player);
		bossEvent.removePlayer(player);
	}

	@Override
	protected void pushEntities() {
		if (getInvulTime() == 0) {
			super.pushEntities();
		}
	}

	@Override
	public boolean removeWhenFarAway(double dist) {
		return false;
	}

	@Override
	public boolean canChangeDimensions(Level oldLevel, Level newLevel) {
		return false;
	}

	@Override
	public boolean isPushable() {
		return super.isPushable() && getInvulTime() == 0;
	}

	@Override
	public boolean canBeLeashed() {
		return false;
	}

	@Override
	protected boolean canRide(Entity vehicle) {
		return false;
	}

	@Override
	public boolean canDisableShield() {
		return true;
	}

	///Getter setter
	@Override
	public void setCustomName(@Nullable Component name) {
		super.setCustomName(name);
		this.bossEvent.setName(getDisplayName());
	}

	public List<Player> getPlayersAround() {
		return PlayerHelper.getRealPlayersIn(level(), getArenaBB());
	}

	@NotNull
	private AABB getArenaBB() {
		double range = getArena().map(GaiaArena::radius).orElse(getArenaRange()) + 3;

		BlockPos source = getHome().pos();
		return new AABB(
				source.getX() + 0.5,
				source.getY() + 0.5,
				source.getZ() + 0.5,
				source.getX() + 0.5,
				source.getY() + 0.5,
				source.getZ() + 0.5
		).inflate(range);
	}

	public int getPlayerCount() {
		return playerCount;
	}

	public Optional<GaiaArena> getArena() {
		return Optional.ofNullable(this.arena);
	}

	protected void setArena(@Nullable GaiaArena arena) {
		this.arena = arena;
	}

	public void setInvulTime(int time) {
		entityData.set(INVUL_TIME, time);
		this.bossEvent.setGrainTime(time);
	}

	public int getInvulTime() {
		return entityData.get(INVUL_TIME);
	}

	@NotNull
	public GlobalPos getHome() {
		return this.home;
	}

	protected void setHome(@NotNull GlobalPos pos) {
		this.home = pos;
	}

	public net.minecraft.world.item.Item getGuardianBypassItem() { return null; }
	public Set<UUID> getPlayersWhoAttacked() {
		return this.playersWhoAttacked;
	}

	public void markPlayerAttacked(Player player) {
		if (PlayerHelper.isTruePlayer(player)) {
			this.playersWhoAttacked.add(player.getUUID());
		}
	}

	public float getArenaRange() {
		return this.getArena().map(GaiaArena::radius).orElse(GaiaArena.ARENA_RANGE);
	}

	public float getDamageCap() {
		return DAMAGE_CAP;
	}

	public int getEmergeTime() {
		return GaiaAI.EMERGE_TIME;
	}

	/// SyncData
	public void syncDataFormServer(GaiaSpawnData data) {
		this.playerCount = data.getPlayerCount();
		//this.bossInfoUUID = data.getBossInfoUUID();
		this.setHome(data.getHome());
		if (!NO_HOME.equals(data.getArena().center().pos())) {
			this.setArena(data.getArena());
			Proxy.INSTANCE.runOnClient(() -> () -> DopplegangerMusic.play(this));
		}
	}

	@Override
	public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity serverEntity) {
		var spawnData = new GaiaSpawnData();
		spawnData.setHome(getHome());
		//spawnData.setBossInfoUUID(getBossInfoUuid());
		spawnData.setPlayerCount(getPlayerCount());
		spawnData.setArena(getArena().orElse(GaiaArena.of(GlobalPos.of(level().dimension(), NO_HOME))));

		return EXplatAbstractions.INSTANCE.toVanillaClientboundPacket(
				new SpawnGaiaPacket(new ClientboundAddEntityPacket(this, serverEntity), spawnData));
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(INVUL_TIME, 0);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag cmp) {
		super.addAdditionalSaveData(cmp);
		cmp.putInt(TAG_INVUL_TIME, getInvulTime());

		GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, getHome()).resultOrPartial(LOGGER::error).ifPresent((tag) -> {
			cmp.put(TAG_HOME, tag);
		});

		getArena().flatMap(arena -> GaiaArena.CODEC.encodeStart(NbtOps.INSTANCE, arena).resultOrPartial(LOGGER::error))
				.ifPresent((tag) -> cmp.put(TAG_ARENA, tag));

		cmp.putInt(TAG_PLAYER_COUNT, playerCount);

		if (!getPlayersWhoAttacked().isEmpty()) {
			ListTag players = new ListTag();
			for (var uuid : getPlayersWhoAttacked()) {
				players.add(NbtUtils.createUUID(uuid));
			}
			cmp.put(TAG_PLAYER_ATTACKED, players);
		}
	}

	@Override
	public void readAdditionalSaveData(CompoundTag cmp) {
		super.readAdditionalSaveData(cmp);
		setInvulTime(cmp.getInt(TAG_INVUL_TIME));

		if (cmp.contains(TAG_HOME)) {
			this.setHome(GlobalPos.CODEC.parse(NbtOps.INSTANCE, cmp.get(TAG_HOME)).resultOrPartial(LOGGER::error)
					.orElse(GlobalPos.of(this.level().dimension(), NO_HOME)));
		} else {
			this.setHome(GlobalPos.of(this.level().dimension(), NO_HOME));
		}

		if (cmp.contains(TAG_ARENA)) {
			this.setArena(GaiaArena.CODEC.parse(NbtOps.INSTANCE, cmp.get(TAG_ARENA)).resultOrPartial(LOGGER::error)
					.orElse(null));
		}

		if (cmp.contains(TAG_PLAYER_COUNT)) {
			playerCount = cmp.getInt(TAG_PLAYER_COUNT);
		} else {
			playerCount = 1;
		}

		if (cmp.contains(TAG_PLAYER_ATTACKED, CompoundTag.TAG_LIST)) {
			ListTag uuids = cmp.getList(TAG_PLAYER_ATTACKED, CompoundTag.TAG_LIST);
			getPlayersWhoAttacked().clear();
			for (var uuid : uuids) {
				playersWhoAttacked.add(NbtUtils.loadUUID(uuid));
			}
		}

		if (this.hasCustomName()) {
			this.bossEvent.setName(getDisplayName());
		}
	}

	public static class GaiaSpawnData {
		private int playerCount;
		private GlobalPos home;
		private UUID bossInfoUUID;
		private GaiaArena arena;

		public GaiaSpawnData() {}

		public GaiaArena getArena() {
			return arena;
		}

		public void setArena(GaiaArena arena) {
			this.arena = arena;
		}

		public int getPlayerCount() {
			return playerCount;
		}

		public void setPlayerCount(int playerCount) {
			this.playerCount = playerCount;
		}

		public GlobalPos getHome() {
			return home;
		}

		public void setHome(GlobalPos home) {
			this.home = home;
		}

		public UUID getBossInfoUUID() {
			return bossInfoUUID;
		}

		public void setBossInfoUUID(UUID bossInfoUUID) {
			this.bossInfoUUID = bossInfoUUID;
		}
	}

	/// Music
	public SoundEvent getBGM() {
		return BotaniaSounds.MUSIC_GAIA_BOSS_2.value();
	}

	private static class DopplegangerMusic extends AbstractTickableSoundInstance {
		private final Gaia guardian;

		private DopplegangerMusic(Gaia guardian) {
			super(guardian.getBGM(), SoundSource.RECORDS, SoundInstance.createUnseededRandom());
			this.guardian = guardian;
			this.x = guardian.getHome().pos().getX();
			this.y = guardian.getHome().pos().getY();
			this.z = guardian.getHome().pos().getZ();
			this.looping = true;
		}

		public static void play(Gaia guardian) {
			Minecraft.getInstance().getSoundManager().play(new DopplegangerMusic(guardian));
		}

		@Override
		public void tick() {
			if (!guardian.isAlive()) {
				stop();
			}
		}
	}
}
