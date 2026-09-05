package io.github.lounode.extrabotany.common.entity.gaia;

import com.mojang.serialization.Dynamic;
import io.github.lounode.extrabotany.api.gaia.GaiaArena;
import io.github.lounode.extrabotany.common.bossevents.ServerGaiaBossEvent;
import io.github.lounode.extrabotany.common.entity.*;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.item.legacy.LegacyRelicSword;
import io.github.lounode.extrabotany.common.sounds.ExtraBotanySounds;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import vazkii.botania.common.handler.BotaniaSounds;
import vazkii.botania.common.helper.PlayerHelper;
import java.util.ArrayList;
import java.util.List;

/** Reimplemented scex.1 encounter; all persistent phase names retain their original save contract. */
public final class LegacyVoidHerrscher extends Gaia {
    public static final EntityType<LegacyVoidHerrscher> TYPE = EntityType.Builder.<LegacyVoidHerrscher>of(LegacyVoidHerrscher::new, MobCategory.MONSTER)
            .sized(.6F, 1.8F).fireImmune().clientTrackingRange(10).updateInterval(10).build("extrabotany:void_herrscher");
    private static final EntityDataAccessor<Integer> SHIELDS = SynchedEntityData.defineId(LegacyVoidHerrscher.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> WINGS = SynchedEntityData.defineId(LegacyVoidHerrscher.class, EntityDataSerializers.BOOLEAN);
    private int shieldLayers = 5, rotatingShields, tpDelay = 90, dodgeCd = 300, skillCd = 200, skillType, supportCd = 300;
    private float damageTaken;
    private boolean rankII, rankIII, emergeLanceDone;
    private final List<String> supporters = new ArrayList<>(List.of("Akasha", "Sirin", "Selene", "Helios", "Aster", "Nyx", "Iris"));
    public LegacyVoidHerrscher(EntityType<? extends LegacyVoidHerrscher> type, Level level) {
        super(type, level); xpReward = 1725;
        bossEvent = (ServerGaiaBossEvent) new ServerGaiaBossEvent(type.getDescription(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS).setCreateWorldFog(true);
    }
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MOVEMENT_SPEED, .4).add(Attributes.MAX_HEALTH, 400).add(Attributes.KNOCKBACK_RESISTANCE, 1);
    }
    public static boolean spawn(Player player, ItemStack stack, Level level, BlockPos position) {
        var arena = GaiaArena.of(GlobalPos.of(level.dimension(), position), 15, 7);
        if (!arena.checksModern(player, level, stack) || arena.countGaiaAround(level, LegacyVoidHerrscher.class) > 0) return false;
        if (!arena.checkGuardianInventoryStrict(level, LegacyRelicSword.ITEMS.get("first_fractal"))) {
            if (!level.isClientSide()) player.sendSystemMessage(Component.translatable("extrabotany.message.guardian_no_response").withStyle(ChatFormatting.RED));
            return false;
        }
        if (level instanceof ServerLevel server) {
            var boss = new LegacyVoidHerrscher(TYPE, level); boss.setHome(GlobalPos.of(level.dimension(), position)); boss.setArena(arena);
            boss.setPos(position.getX() + .5, position.getY() + 3, position.getZ() + .5);
            boss.getBrain().setMemoryWithExpiry(MemoryModuleType.IS_EMERGING, Unit.INSTANCE, 200L); boss.setInvulTime(200); boss.setHealth(1); boss.bossEvent.setProgress(0);
            var players = arena.getPlayersAround(level); boss.playerCount = Math.max(1, players.size()); boss.bossEvent.setPlayerCount(boss.playerCount);
            boss.getAttribute(Attributes.MAX_HEALTH).setBaseValue(400.0 * boss.playerCount);
            if (level.getDifficulty() == Difficulty.HARD) boss.getAttribute(Attributes.ARMOR).setBaseValue(15);
            boss.finalizeSpawn(server, server.getCurrentDifficultyAt(position), MobSpawnType.EVENT, null); server.addFreshEntity(boss);
            server.playSound(null, position, SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 10, .1F);
            for (var nearby : players) if (nearby instanceof ServerPlayer real) CriteriaTriggers.SUMMONED_ENTITY.trigger(real, boss);
        }
        return true;
    }
    @Override protected Brain<?> makeBrain(Dynamic<?> data) { return GaiaIIIAI.makeBrain(this, data); }
    @Override protected void initMemories(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, SpawnGroupData data) { GaiaIIIAI.initMemories(this, level.getLevel(), getHome().pos()); }
    @Override protected void updateAI() { GaiaIIIAI.updateActivity(this); }
    @Override public int getEmergeTime() { return 200; }
    @Override public float getArenaRange() { return 15; }
    @Override public SoundEvent getBGM() { return ExtraBotanySounds.MUSIC_HERRSCHER; }
    @Override public Item getGuardianBypassItem() { return LegacyRelicSword.ITEMS.get("first_fractal"); }
    public boolean isRankIIRenderState() { return entityData.get(WINGS); }
    public int getRotatingShieldsRenderState() { return entityData.get(SHIELDS); }
    private void syncShields() { entityData.set(SHIELDS, Mth.clamp(rotatingShields, 0, 3)); entityData.set(WINGS, rankII || rankIII); }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) { super.defineSynchedData(builder); builder.define(SHIELDS, 0); builder.define(WINGS, false); }
    @Override protected void customServerAiStep() {
        super.customServerAiStep();
        if (isNoAi()) return;
        if (level().getDifficulty() == Difficulty.PEACEFUL) { discard(); return; }
        if (getBrain().hasMemoryValue(MemoryModuleType.IS_EMERGING)) return;
        float fraction = getHealth() / Math.max(1, getMaxHealth());
        if (!rankII && fraction <= .8F) {
            rankII = true; shieldLayers = 5; rotatingShields = Math.max(1, rotatingShields); syncShields(); spawnDomains();
            for (int i = 0; i < 2; i++) randomLance(); broadcast("rank2");
        }
        if (!rankIII && fraction <= .25F) {
            rankIII = true; rotatingShields = 3; syncShields(); for (int i = 0; i < 4; i++) randomLance();
            if (!getPlayersWhoAttacked().isEmpty()) super.heal(getMaxHealth()); broadcast("rank3");
        }
        var home = getHome().pos();
        if (!emergeLanceDone && tickCount >= 300) { lance(new Vec3(home.getX() + .5, home.getY() + 12, home.getZ() + .5), 3, 4800); emergeLanceDone = true; }
        if (rotatingShields >= 3) for (var projectile : level().getEntitiesOfClass(Projectile.class, getBoundingBox().inflate(2.5))) {
            if (projectile.getOwner() == this || projectile instanceof LegacySubspaceSpear || projectile instanceof SkullMissileEntity) continue;
            projectile.setDeltaMovement(getLookAngle().scale(1.5)); projectile.hurtMarked = true;
        }
        for (var player : getPlayersAround()) {
            if (player.getDeltaMovement().y > 0 && !player.getAbilities().instabuild) player.setDeltaMovement(player.getDeltaMovement().multiply(1, -1, 1));
            player.getActiveEffects().stream().filter(effect -> effect.getDuration() < 160 && effect.isAmbient() && effect.getEffect().value().isBeneficial())
                    .map(MobEffectInstance::getEffect).distinct().toList().forEach(player::removeEffect);
            player.addEffect(new MobEffectInstance(io.github.lounode.extrabotany.common.brew.ExtraBotanyMobEffects.WITCH_CURSE, 200,
                    1 + (rankIII ? 2 : 0) + (tickCount >= 1800 ? 2 : 0), true, true));
            if (player.distanceToSqr(home.getX() + .5, player.getY(), home.getZ() + .5) > 225) player.addEffect(new MobEffectInstance(MobEffects.UNLUCK, 400, 4));
        }
        if (rankII && getY() < home.getY() + 2) setDeltaMovement(getDeltaMovement().add(0, .1, 0));
        if (tickCount % 72 == 0) spearPortal();
        if (tickCount > 200 && tickCount % (rankIII ? 220 : rankII ? 260 : 310) == 0) spawnMines();
        if (tickCount > 60 && tickCount % 60 == 0) { missile(0); if (random.nextFloat() < .4F) missile(1); }
        if (rankII && tickCount % 110 == 0) missile(1);
        if (rankIII && tickCount % 80 == 0) { missile(2); missile(2); heal(1); }
        if (--tpDelay <= 0) { randomTeleport(); tpDelay = rankIII ? 75 : 90; }
        if (dodgeCd > 0) dodgeCd--;
        if (rankII) { if (--skillCd <= 0) skill(); else if (skillCd == 100) broadcast(skillType == 1 ? "warning_judge" : "warning_target"); }
        if (rankIII && --supportCd <= 0) { support(); supportCd = 400; }
    }
    @Override public float getDamageCap() { float cap = 25; for (int i = 0; i < rotatingShields; i++) cap *= .85F; return cap; }
    @Override public boolean hurt(DamageSource source, float amount) {
        if (!Float.isFinite(amount) || amount <= 0) return false;
        if (source.is(DamageTypes.GENERIC_KILL)) return super.hurt(source, amount);
        if (!(source.getEntity() instanceof Player player) || !PlayerHelper.isTruePlayer(player) || getInvulTime() > 0) return false;
        var home = getHome().pos();
        if (player.distanceToSqr(home.getX() + .5, player.getY(), home.getZ() + .5) > 225) { player.teleportTo(home.getX() + .5, home.getY() + 1, home.getZ() + .5); return false; }
        markPlayerAttacked(player);
        if (shieldLayers > 0) { shieldLayers--; return false; }
        if (dodgeCd <= 0) {
            var field = new LegacyVoidField(LegacyVoidField.TYPE, level()); field.setPos(player.position()); level().addFreshEntity(field);
            dodgeCd = 300; spearPortal(); if (random.nextFloat() < .2F) randomLance(); return false;
        }
        for (int i = 0; i < rotatingShields; i++) amount *= .85F;
        if (rotatingShields > 0) {
            player.getActiveEffects().forEach(effect -> addEffect(new MobEffectInstance(effect)));
            if (amount > 20) { rotatingShields--; syncShields(); dodgeCd = 0; }
        }
        amount = Math.min(getDamageCap(), amount); damageTaken += amount;
        if (damageTaken >= 80) { damageTaken = 0; randomTeleport(); tpDelay = 65; LegacySwordProjectile.trueMagicDamage(player, this, player.getMaxHealth() * .1F + 6); }
        if (skillCd > 80) skillCd -= 15;
        boolean hit = super.hurt(source, amount); setInvulTime(getInvulTime() + 10);
        var away = position().subtract(player.position()); if (away.lengthSqr() > 1E-6) { away = away.normalize().scale(.2); setDeltaMovement(away.x, .25, away.z); }
        return hit;
    }
    @Override protected void actuallyHurt(DamageSource source, float amount) {
        if (!Float.isFinite(amount) || amount <= 0) return;
        if (!source.is(DamageTypes.GENERIC_KILL)) tpDelay = Math.max(0, tpDelay - 4);
        super.actuallyHurt(source, Math.min(getDamageCap(), amount));
    }
    @Override public void heal(float amount) { if (Float.isFinite(amount) && amount > 0) super.heal(amount); }
    private Player randomPlayer() { var players = getPlayersAround().stream().filter(LivingEntity::isAlive).toList(); return players.isEmpty() ? null : players.get(random.nextInt(players.size())); }
    private void broadcast(String suffix) { getPlayersAround().forEach(player -> player.sendSystemMessage(Component.translatable("extrabotany.message.herrscher." + suffix).withStyle(ChatFormatting.LIGHT_PURPLE))); }
    private void randomTeleport() { var home = getHome().pos(); teleportTo(home.getX() + .5 + (random.nextDouble() - .5) * 15, home.getY() + (rankII ? 2 : 1), home.getZ() + .5 + (random.nextDouble() - .5) * 15); }
    private void lance(Vec3 start, float damage, int life) { var lance = new LegacyLance(LegacyLance.TYPE, level()); lance.setOwner(this); lance.configure(damage, life); lance.setPos(start); level().addFreshEntity(lance); }
    private void randomLance() { var home = getHome().pos(); lance(new Vec3(home.getX() + (random.nextDouble() - .5) * 15, home.getY() + 12 + (rankII ? 2 : 0), home.getZ() + (random.nextDouble() - .5) * 15), 4, 1200); }
    private void spearPortal() {
        var portal = new LegacySubspace(LegacySubspace.TYPE, level()); portal.setOwner(this);
        portal.configure(1, 32, 12, 10, .4F + random.nextFloat() * .15F, Mth.wrapDegrees(-getYRot() + 180));
        portal.setPos(getX(), getY() + 1.9, getZ()); portal.setYRot(getYRot()); level().addFreshEntity(portal);
    }
    private void spawnMines() {
        var home = getHome().pos(); int count = (level().getDifficulty() == Difficulty.HARD ? 9 : 7) + 3 * Math.max(1, playerCount) + (rankIII ? 6 : rankII ? 2 : 0);
        for (int i = 0; i < count; i++) {
            SkullLandMineEntity mine = i % 6 == 0 ? new SkullLandMineEntity.Danger(level(), this) : i % 8 == 0 ? new SkullLandMineEntity.Disarm(level(), this) : new SkullLandMineEntity.Default(level(), this);
            var target = randomPlayer(); mine.setDamage(6); mine.setPos(home.getX() + (random.nextDouble() - .5) * 20, target == null ? home.getY() + 1 : target.getY(), home.getZ() + (random.nextDouble() - .5) * 20); level().addFreshEntity(mine);
        }
    }
    private void missile(int mode) {
        var player = randomPlayer(); if (player == null) return;
        boolean hard = level().getDifficulty() == Difficulty.HARD;
        var missile = new SkullMissileEntity(level(), this); missile.setTarget(player); missile.setFire(mode >= 1); missile.setDamage(hard ? 7 : 5);
        if (mode >= 2) { missile.setEffect(true); missile.setTrueDamage(hard ? 3 : 2); }
        missile.setPos(getX() + (random.nextDouble() - .5) * .1, getY() + 1.8, getZ() + (random.nextDouble() - .5) * .1);
        if (missile.findTarget()) { playSound(BotaniaSounds.MISSILE, .6F, .8F + random.nextFloat() * .2F); level().addFreshEntity(missile); }
    }
    private void spawnDomains() {
        var targets = new ArrayList<>(getPlayersWhoAttacked()); if (targets.isEmpty()) getPlayersAround().forEach(player -> targets.add(player.getUUID()));
        if (targets.isEmpty()) return;
        var home = getHome().pos();
        for (int i = 0; i < 8; i++) {
            var domain = new LegacySwordDomain(LegacySwordDomain.TYPE, level()); domain.configure(targets.get(Math.min(i, targets.size() - 1)), home, i);
            double angle = i * Math.PI / 4; domain.setPos(home.getX() + 5 * Math.cos(angle), home.getY() + 7, home.getZ() + 2 + 5 * Math.sin(angle)); level().addFreshEntity(domain);
        }
    }
    private void skill() {
        switch (skillType) {
            case 0 -> { var player = randomPlayer(); if (player != null) { player.getCooldowns().addCooldown(player.getMainHandItem().getItem(), 120); LegacySwordProjectile.trueMagicDamage(player, this, player.getMaxHealth() * .2F + 6); randomLance(); } skillCd = 270; skillType = rankIII ? 1 : random.nextInt(2); }
            case 1 -> { if (!rankIII) { skillCd = 200; skillType = 0; } else { voidJudge(); skillCd = 250; skillType = 2; } }
            case 2 -> { spawnDomains(); skillCd = 290; skillType = rankIII ? 3 : 0; }
            default -> { randomLance(); skillCd = 180; skillType = 0; }
        }
    }
    private void voidJudge() {
        setInvulTime(120); var home = getHome().pos(); teleportTo(home.getX() + .5, home.getY() + 2, home.getZ() + .5);
        var look = getLookAngle().multiply(1, 0, 1); if (look.lengthSqr() < 1E-6) look = Vec3.directionFromRotation(0, getYRot()); look = look.normalize().scale(-2);
        var direction = look.normalize(); var axis = direction.cross(new Vec3(-1, 0, -1)); if (axis.lengthSqr() < 1E-6) axis = new Vec3(1, 0, 0); axis = axis.normalize();
        for (int i = 0; i < 24; i++) {
            int row = i / 8, column = i % 8; double angle = column * Math.PI / 7 - Math.PI / 2;
            var vector = axis.scale(row * 3.5 + 5); vector = vector.scale(Math.cos(angle)).add(direction.cross(vector).scale(Math.sin(angle))).add(direction.scale(direction.dot(vector) * (1 - Math.cos(angle))));
            if (vector.y < 0) vector = vector.multiply(1, -1, 1);
            var end = position().add(0, getBbHeight() * .5, 0).add(look).add(0, 1.6, row * .1).add(vector);
            var portal = new LegacySubspace(LegacySubspace.TYPE, level()); portal.setOwner(this); portal.configure(0, 120, 15 + random.nextInt(12), 10 + random.nextInt(10), 3 + random.nextFloat() * .5F, Mth.wrapDegrees(-getYRot() + 180));
            portal.setPos(end.x, end.y - .5 + random.nextFloat(), end.z); portal.setYRot(getYRot()); level().addFreshEntity(portal);
            if (i == 1) playSound(ExtraBotanySounds.SPEAR_OF_SUBSPACE_USE, 1, 1);
        }
    }
    private void support() {
        var player = randomPlayer(); if (player == null || supporters.isEmpty()) return;
        String supporter = supporters.remove(random.nextInt(supporters.size())); int effect = random.nextInt(7), variant = 0;
        switch (effect) {
            case 0 -> { variant = random.nextInt(2); player.heal(player.getMaxHealth() * .25F); }
            case 1 -> hurt(damageSources().playerAttack(player), 15);
            case 2 -> tpDelay += 200;
            case 3 -> { variant = random.nextInt(2); skillCd += 120; }
            case 4 -> { variant = random.nextInt(3); supportCd = 200; }
            case 5 -> { variant = random.nextInt(2); player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 300, 5)); }
            case 6 -> { variant = random.nextInt(3); for (int i = 0; i < 2; i++) { if (player.getHealth() < player.getMaxHealth()) player.heal(4); player.addEffect(new MobEffectInstance(variant == 1 ? MobEffects.JUMP : variant == 2 ? MobEffects.DIG_SPEED : MobEffects.MOVEMENT_SPEED, 200, 1)); } }
        }
        player.sendSystemMessage(Component.translatable("extrabotany.message.herrscher.support" + effect + variant, supporter).withStyle(ChatFormatting.AQUA));
    }
    @Override protected void dropFromLootTable(DamageSource source, boolean recentHit) {
        for (var uuid : getPlayersWhoAttacked()) {
            var player = level().getPlayerByUUID(uuid); if (player == null) continue;
            player.spawnAtLocation(new ItemStack(ExtraBotanyItems.pandorasBox)); player.spawnAtLocation(new ItemStack(io.github.lounode.extrabotany.common.item.legacy.LegacySupplyBagItem.INSTANCE, 3));
            player.spawnAtLocation(new ItemStack(ExtraBotanyItems.recordHerrscherOfTheVoid)); if (random.nextFloat() < .02F) player.spawnAtLocation(new ItemStack(ExtraBotanyItems.coreOfTheVoid));
        }
    }
    @Override public void die(DamageSource source) {
        super.die(source);
        if (level() instanceof ServerLevel server) {
            for (var uuid : getPlayersWhoAttacked()) if (server.getPlayerByUUID(uuid) instanceof ServerPlayer player) {
                award(player, "herrscher_defeat"); if (tickCount < 3600) award(player, "endgame_goal");
            }
            playSound(SoundEvents.GENERIC_EXPLODE.value(), 20, (1 + (random.nextFloat() - random.nextFloat()) * .2F) * .7F);
            server.sendParticles(ParticleTypes.EXPLOSION_EMITTER, getX(), getY(), getZ(), 1, 1, 0, 0, 0);
        }
    }
    private static void award(ServerPlayer player, String name) {
        var advancement = player.server.getAdvancements().get(ResourceLocation.parse("extrabotany:main/" + name));
        if (advancement != null) for (var criterion : player.getAdvancements().getOrStartProgress(advancement).getRemainingCriteria()) player.getAdvancements().award(advancement, criterion);
    }
    @Override public void remove(RemovalReason reason) {
        if (!level().isClientSide() && reason.shouldDestroy()) for (var entity : level().getEntitiesOfClass(Entity.class, getBoundingBox().inflate(64))) {
            if (entity instanceof LegacyOwnedEntity owned && owned.getOwner() == this || entity instanceof MagicLandMineEntity mine && mine.getOwner() == this
                    || entity instanceof SkullMissileEntity missile && missile.getOwner() == this || entity instanceof LegacyVoidField || entity instanceof LegacySwordDomain) entity.discard();
        }
        super.remove(reason);
    }
    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag); tag.putInt("ShieldLayers", shieldLayers); tag.putInt("RotatingShields", rotatingShields); tag.putBoolean("RankII", rankII); tag.putBoolean("RankIII", rankIII);
        tag.putFloat("HerrscherDamageTaken", damageTaken); tag.putInt("HerrscherTpDelay", tpDelay); tag.putInt("DodgeCd", dodgeCd); tag.putInt("SkillCd", skillCd); tag.putInt("SkillType", skillType);
        tag.putInt("SupportCd", supportCd); tag.putBoolean("EmergeLanceDone", emergeLanceDone); var list = new ListTag(); supporters.forEach(name -> list.add(StringTag.valueOf(name))); tag.put("Supporters", list);
    }
    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag); shieldLayers = tag.getInt("ShieldLayers"); rotatingShields = Mth.clamp(tag.getInt("RotatingShields"), 0, 3);
        rankII = tag.getBoolean("RankII"); rankIII = tag.getBoolean("RankIII"); syncShields(); damageTaken = tag.getFloat("HerrscherDamageTaken"); tpDelay = tag.getInt("HerrscherTpDelay");
        dodgeCd = tag.getInt("DodgeCd"); skillCd = tag.getInt("SkillCd"); skillType = tag.getInt("SkillType"); supportCd = tag.getInt("SupportCd"); emergeLanceDone = tag.getBoolean("EmergeLanceDone");
        if (tag.contains("Supporters", 9)) { supporters.clear(); for (var value : tag.getList("Supporters", 8)) supporters.add(value.getAsString()); }
    }
}
