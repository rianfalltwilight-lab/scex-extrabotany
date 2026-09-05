package io.github.lounode.extrabotany.common.entity.gaia;

import com.mojang.serialization.Dynamic;

import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Unit;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.loot.LootTable;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.common.handler.BotaniaSounds;

import io.github.lounode.extrabotany.api.gaia.GaiaArena;
import io.github.lounode.extrabotany.common.entity.ExtraBotanyEntityType;
import io.github.lounode.extrabotany.common.sounds.ExtraBotanySounds;

import java.util.List;

public class GaiaIII extends Gaia {
	private static final net.minecraft.network.syncher.EntityDataAccessor<Integer> BIG_PHASE = net.minecraft.network.syncher.SynchedEntityData.defineId(GaiaIII.class, net.minecraft.network.syncher.EntityDataSerializers.INT);
	private static final net.minecraft.network.syncher.EntityDataAccessor<Integer> EGO_STAGE = net.minecraft.network.syncher.SynchedEntityData.defineId(GaiaIII.class, net.minecraft.network.syncher.EntityDataSerializers.INT);
	private static final net.minecraft.network.syncher.EntityDataAccessor<Integer> EGO_FLICKER = net.minecraft.network.syncher.SynchedEntityData.defineId(GaiaIII.class, net.minecraft.network.syncher.EntityDataSerializers.INT);
	private boolean inFogBarrage, inMinionPhase, nameShowingEgo;
	private int fogWaveIndex, fogWaveTimer, minionPhaseTimer, flickerCooldown = 240;
	public static final float ARENA_RANGE = 15F;
	public static final int ARENA_HEIGHT = 7;
	public static final float MAX_HP = 600F;
	private static final float DAMAGE_CAP = 30;

	public GaiaIII(EntityType<? extends GaiaIII> type, Level world) {
		super(type, world);
		this.xpReward = 1000;
	}

	public GaiaIII(EntityType<? extends GaiaIII> type, Level world, BlockPos source) {
		super(type, world, source);
	}

	public GaiaIII(Level world, BlockPos source) {
		this(ExtraBotanyEntityType.GAIA_III, world, source);
	}

	public static boolean spawn(Player player, ItemStack stack, Level world, BlockPos pos) {
		GaiaArena arena = GaiaArena.of(GlobalPos.of(world.dimension(), pos), ARENA_RANGE, ARENA_HEIGHT);
		if (!arena.checksModern(player, world, stack)) {
			return false;
		}
		if (!arena.checkGuardianInventoryStrict(world, io.github.lounode.extrabotany.common.item.ExtraBotanyItems.voidArchives)) {
			if (!world.isClientSide()) {
				player.sendSystemMessage(Component.translatable("extrabotany.message.guardian_no_response").withStyle(ChatFormatting.RED));
			}
			return false;
		}

		//all checks ok, spawn the boss
		if (!world.isClientSide()) {
			GaiaIII gaia = new GaiaIII(world, pos);
			gaia.setArena(arena);
			gaia.setPos(pos.getX() + 0.5, pos.getY() + 3, pos.getZ() + 0.5);

			gaia.getBrain().setMemoryWithExpiry(MemoryModuleType.IS_EMERGING, Unit.INSTANCE, (long) GaiaIIIAI.EMERGE_TIME);
			gaia.setInvulTime(GaiaIIIAI.EMERGE_TIME);
			gaia.setHealth(1F);
			gaia.bossEvent.setProgress(0.0F);

			List<Player> playersAround = arena.getPlayersAround(world);

			int playerCount = playersAround.size();
			gaia.playerCount = playerCount;
			gaia.bossEvent.setPlayerCount(playerCount);

			float healthMultiplier = 1;
			if (playerCount > 1) {
				healthMultiplier += playerCount * 0.25F;
			}
			gaia.getAttribute(Attributes.MAX_HEALTH).setBaseValue(MAX_HP * healthMultiplier);
			gaia.getAttribute(Attributes.ARMOR).setBaseValue(30);

			gaia.playSound(BotaniaSounds.GAIA_SUMMON, 0.05F, 1F);
			gaia.finalizeSpawn((ServerLevelAccessor) world, world.getCurrentDifficultyAt(gaia.blockPosition()), MobSpawnType.EVENT, null);
			world.addFreshEntity(gaia);

			for (Player nearbyPlayer : playersAround) {
				if (nearbyPlayer instanceof ServerPlayer serverPlayer) {
					CriteriaTriggers.SUMMONED_ENTITY.trigger(serverPlayer, gaia);
				}
			}
		}
		return true;
	}

	public static AttributeSupplier.Builder createGaiaAttributes() {
		return Monster.createMonsterAttributes()
				.add(Attributes.MOVEMENT_SPEED, 0.4)
				.add(Attributes.MAX_HEALTH, MAX_HP)
				.add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
	}

	@Override
	protected Brain<?> makeBrain(Dynamic<?> dynamic) {
		return GaiaIIIAI.makeBrain(this, dynamic);
	}

	@Override
	protected void initMemories(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData spawnData) {
		GaiaIIIAI.initMemories(this, level.getLevel(), getHome().pos());
	}

	@Override
	protected void updateAI() {
		GaiaIIIAI.updateActivity(this);
	}

	@Override
	public ResourceKey<LootTable> getDefaultLootTable() {
		return this.getType().getDefaultLootTable();
	}

	@Override
	public float getDamageCap() {
		return DAMAGE_CAP;
	}

	@Override
	public int getEmergeTime() {
		return GaiaIIIAI.EMERGE_TIME;
	}

	@Override
	public SoundEvent getBGM() {
		return ExtraBotanySounds.MUSIC_GAIA3;
	}

	public int getBigPhase() { return entityData.get(BIG_PHASE); }
	@Override public net.minecraft.world.item.Item getGuardianBypassItem() { return io.github.lounode.extrabotany.common.item.ExtraBotanyItems.voidArchives; }
	public boolean isEgo() { return getBigPhase() >= 2; }
	public int getEgoStage() { return entityData.get(EGO_STAGE); }
	public int getEgoFlicker() { return entityData.get(EGO_FLICKER); }
	@Override protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder); builder.define(BIG_PHASE, 1); builder.define(EGO_STAGE, 0); builder.define(EGO_FLICKER, 0);
	}
	@Override public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putInt("BigPhase", getBigPhase()); tag.putInt("EgoStage", getEgoStage());
		tag.putBoolean("InFogBarrage", inFogBarrage); tag.putInt("FogWaveIndex", fogWaveIndex); tag.putInt("FogWaveTimer", fogWaveTimer);
		tag.putBoolean("InMinionPhase", inMinionPhase); tag.putInt("MinionPhaseTimer", minionPhaseTimer); tag.putInt("FlickerCooldown", flickerCooldown);
	}
	@Override public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		if (tag.contains("BigPhase")) entityData.set(BIG_PHASE, tag.getInt("BigPhase"));
		if (tag.contains("EgoStage")) entityData.set(EGO_STAGE, tag.getInt("EgoStage"));
		inFogBarrage = tag.getBoolean("InFogBarrage"); fogWaveIndex = tag.getInt("FogWaveIndex"); fogWaveTimer = tag.getInt("FogWaveTimer");
		inMinionPhase = tag.getBoolean("InMinionPhase"); minionPhaseTimer = tag.getInt("MinionPhaseTimer");
		if (tag.contains("FlickerCooldown")) flickerCooldown = tag.getInt("FlickerCooldown");
	}
	@Override protected void customServerAiStep() {
		super.customServerAiStep();
		if (inFogBarrage) {
			holdAtHome();
			if (fogWaveIndex < 6) {
				if (fogWaveTimer <= 0) { spawnFogWave(fogWaveIndex++); fogWaveTimer = 60; } else fogWaveTimer--;
			} else if (getInvulTime() <= 0) inFogBarrage = false;
		}
		if (inMinionPhase) {
			holdAtHome(); minionPhaseTimer++;
			boolean remaining = level().getEntitiesOfClass(io.github.lounode.extrabotany.common.entity.LegacyEgoMinion.class,
					new net.minecraft.world.phys.AABB(getHome().pos()).inflate(getArenaRange() + 4), net.minecraft.world.entity.LivingEntity::isAlive).size() > 0;
			if (remaining && minionPhaseTimer < 600) {
				setInvulTime(Math.max(getInvulTime(), 40)); float cap = getMaxHealth() * .6666667F;
				if (getHealth() < cap) setHealth(Math.min(cap, getHealth() + .25F));
			} else { inMinionPhase = false; setInvulTime(0); }
		}
		if (getInvulTime() == 0) {
			float fraction = getHealth() / getMaxHealth();
			if (getBigPhase() == 1 && fraction <= .6666667F) {
				entityData.set(BIG_PHASE, 2); entityData.set(EGO_STAGE, 0); entityData.set(EGO_FLICKER, 0); flickerCooldown = 240;
				playSound(BotaniaSounds.GAIA_SUMMON, 1, .6F);
			}
			if (isEgo()) {
				if (getEgoStage() < 1 && fraction <= .5F) {
					entityData.set(EGO_STAGE, 1); inFogBarrage = true; fogWaveIndex = 0; fogWaveTimer = 0;
					setInvulTime(430); playSound(BotaniaSounds.GAIA_SUMMON, 1, .8F);
				}
				if (getEgoStage() < 2 && fraction <= .16666667F) { entityData.set(EGO_STAGE, 2); startMinionPhase(); }
			}
		}
		if (isEgo()) {
			if (getEgoFlicker() > 0) entityData.set(EGO_FLICKER, getEgoFlicker() - 1);
			if (flickerCooldown > 0) flickerCooldown--; else { entityData.set(EGO_FLICKER, 8); flickerCooldown = 240; }
			boolean showing = getEgoFlicker() > 0;
			if (showing != nameShowingEgo) { nameShowingEgo = showing; bossEvent.setName(showing ? Component.translatable("entity.extrabotany.ego") : getType().getDescription()); }
		}
	}
	private void holdAtHome() {
		var home = getHome().pos(); setPos(home.getX() + .5, home.getY() + 3, home.getZ() + .5); setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
	}
	private void spawnFogMine(net.minecraft.world.phys.Vec3 point, int type) {
		io.github.lounode.extrabotany.common.entity.SkullLandMineEntity mine = switch (type) {
			case 1 -> new io.github.lounode.extrabotany.common.entity.SkullLandMineEntity.Disarm(level(), this);
			case 2 -> new io.github.lounode.extrabotany.common.entity.SkullLandMineEntity.Danger(level(), this);
			default -> new io.github.lounode.extrabotany.common.entity.SkullLandMineEntity.Default(level(), this);
		};
		mine.setPos(point); level().addFreshEntity(mine);
	}
	private void spawnFogWave(int wave) {
		var home = getHome().pos(); var center = new net.minecraft.world.phys.Vec3(home.getX() + .5, home.getY(), home.getZ() + .5);
		var unit = new net.minecraft.world.phys.Vec3(2, 0, 0);
		switch (wave) {
			case 0 -> {
				for (int spoke = 0; spoke < 8; spoke++) {
					// The original formation accumulates rotation between spokes.
					unit = unit.yRot((float) (Math.PI / 4 * spoke));
					for (int point = 0; point < 8; point++) spawnFogMine(center.add(unit.scale(point + 1)), point % 4 == 0 ? 2 : 0);
				}
			}
			case 1 -> { for (int ring = 0; ring < 5; ring++) for (int point = 0; point < 16; point++)
				spawnFogMine(center.add(unit.add(3 * ring, 0, 0).yRot((float) (Math.PI / 8 * point))), ring % 3); }
			case 2, 3 -> {
				for (int point = 0; point < (wave == 2 ? 72 : 80); point++) {
					double angle = point * Math.PI / (wave == 2 ? 12 : 80), radius = wave == 2 ? 1 + angle : 24 * Math.sin(5 * angle);
					spawnFogMine(center.add(radius * Math.cos(angle), 0, radius * Math.sin(angle)), point % (wave == 2 ? 5 : 4) == 0 ? 2 : 0);
				}
			}
			case 4 -> {
				for (int ring = 0; ring < 8; ring++) {
					var origin = center.add(unit.scale(6).yRot((float) (Math.PI / 4 * ring)));
					for (int point = 0; point < 16; point++) spawnFogMine(origin.add(unit.scale(3).yRot((float) (Math.PI / 8 * point))), ring % 3);
				}
			}
			default -> {
				for (int ring = 0; ring < 6; ring++) {
					var origin = center.add(unit.scale(5).yRot((float) (Math.PI / 3 * ring))); spawnFogMine(origin, 0);
					for (int point = 0; point < 16; point++) spawnFogMine(origin.add(unit.scale(2).yRot((float) (Math.PI / 8 * point))), 2);
				}
			}
		}
	}
	private void startMinionPhase() {
		inMinionPhase = true; minionPhaseTimer = 0; setInvulTime(60); var home = getHome().pos();
		if (level() instanceof net.minecraft.server.level.ServerLevel server) for (int index = 0; index < 4; index++) {
			var minion = new io.github.lounode.extrabotany.common.entity.LegacyEgoMinion(io.github.lounode.extrabotany.common.entity.LegacyEgoMinion.TYPE, server);
			minion.setMinionType(index); minion.setSummoner(this); minion.setCustomName(io.github.lounode.extrabotany.common.entity.LegacyEgoMinion.pickName(index)); minion.setCustomNameVisible(true);
			minion.getAttribute(Attributes.MAX_HEALTH).setBaseValue(60.0 * Math.max(1, playerCount)); minion.setHealth(minion.getMaxHealth());
			minion.setPos(home.getX() + (index < 2 ? 6 : -6) + .5, home.getY() + 1, home.getZ() + (index % 2 == 0 ? 6 : -6) + .5);
			minion.finalizeSpawn(server, server.getCurrentDifficultyAt(minion.blockPosition()), MobSpawnType.MOB_SUMMONED, null); server.addFreshEntity(minion);
		}
		playSound(BotaniaSounds.GAIA_SUMMON, 1, .7F);
	}
	private void discardMinions() {
		if (!level().isClientSide()) level().getEntitiesOfClass(io.github.lounode.extrabotany.common.entity.LegacyEgoMinion.class,
				new net.minecraft.world.phys.AABB(getHome().pos()).inflate(getArenaRange() + 8)).forEach(net.minecraft.world.entity.Entity::discard);
	}
	@Override public void die(net.minecraft.world.damagesource.DamageSource source) { discardMinions(); super.die(source); }
	@Override public void remove(RemovalReason reason) {
		// Unloading a saved arena must retain minions for the next load.
		if (reason.shouldDestroy()) discardMinions(); super.remove(reason);
	}
}
