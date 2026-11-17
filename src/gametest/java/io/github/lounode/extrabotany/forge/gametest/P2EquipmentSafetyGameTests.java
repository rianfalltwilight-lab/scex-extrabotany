package io.github.lounode.extrabotany.forge.gametest;

import com.mojang.authlib.GameProfile;
import io.github.lounode.extrabotany.common.brew.ExtraBotanyMobEffects;
import io.github.lounode.extrabotany.common.entity.mount.MountEntity;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.item.equipment.bauble.CurseRingItem;
import io.github.lounode.extrabotany.common.item.equipment.bauble.FrostRingItem;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import top.theillusivec4.curios.api.CuriosApi;
import vazkii.botania.api.item.Relic;
import vazkii.botania.api.mana.ManaItem;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.item.equipment.bauble.BaubleItem;
import vazkii.botania.mixin.LivingEntityAccessor;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@GameTestHolder("extrabotany")
@PrefixGameTestTemplate(false)
public final class P2EquipmentSafetyGameTests {
	private static Player player(GameTestHelper helper) {
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		player.setPos(helper.absoluteVec(new Vec3(32.5, 5, 32.5)));
		return player;
	}

	private static ManaItem supplyMana(Player player, int amount) {
		ItemStack tablet = new ItemStack(BotaniaItems.MANA_TABLET);
		ManaItem mana = ManaItem.LOOKUP.find(tablet);
		mana.addMana(amount);
		player.getInventory().setItem(2, tablet);
		return mana;
	}

	private static ItemStack equip(Player player, Item item) {
		ItemStack stack = new ItemStack(item);
		Relic relic = Relic.LOOKUP.find(stack);
		if (relic != null) relic.bindToUUID(player.getUUID());
		CuriosApi.getCuriosInventory(player).orElseThrow().getCurios().values().stream()
				.filter(handler -> handler.getStacks().getSlots() > 0).findFirst().orElseThrow()
				.getStacks().setStackInSlot(0, stack);
		return stack;
	}

	@GameTest(template = "projectile_arena", timeoutTicks = 20)
	public static void twoBlockingTerrasteelShieldsReflectOnlyOnce(GameTestHelper helper) {
		Player defender = player(helper);
		Player attacker = player(helper);
		attacker.setPos(defender.position().add(0, 0, 3));
		defender.setYRot(0);
		defender.setYHeadRot(0);
		attacker.setYRot(180);
		attacker.setYHeadRot(180);
		for (Player player : List.of(defender, attacker)) {
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ExtraBotanyItems.terrasteelShield));
			player.startUsingItem(InteractionHand.MAIN_HAND);
			((LivingEntityAccessor) player).botania_setUseItemRemaining(71990);
			helper.assertTrue(player.isBlocking(), "Both players must be blocking");
		}
		// Shield direction uses head yaw, which a mock player's body rotation does not update.
		helper.assertTrue(attacker.isDamageSourceBlocked(defender.damageSources().thorns(defender)),
				"Second shield must face the reflected damage before exercising the chain");
		AtomicInteger entries = new AtomicInteger();
		Consumer<LivingIncomingDamageEvent> listener = event -> {
			if (event.getEntity() == defender || event.getEntity() == attacker) {
				helper.assertTrue(entries.incrementAndGet() <= 4, "Shield reflection re-entered the damage chain");
				if (event.getEntity() == attacker) {
					helper.assertTrue(event.getSource().is(DamageTypes.THORNS) && event.getAmount() == 4F, "Reflection must be half-damage thorns");
				}
			}
		};
		NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, listener);
		try {
			Arrow arrow = EntityType.ARROW.create(helper.getLevel());
			arrow.setOwner(attacker);
			arrow.setPos(attacker.getEyePosition());
			defender.hurt(defender.damageSources().arrow(arrow, attacker), 8F);
			helper.assertTrue(entries.get() == 2, "Expected initial hit and one reflection, got " + entries.get());
			helper.assertTrue(defender.getHealth() == defender.getMaxHealth(), "Initial arrow must be blocked");
			helper.assertTrue(attacker.getHealth() == attacker.getMaxHealth(), "Second shield may block thorns, but must not reflect it again");
			helper.assertTrue(defender.hasEffect(MobEffects.REGENERATION), "Shield must still grant regeneration");
			helper.assertTrue(attacker.hasEffect(MobEffects.REGENERATION), "Second shield must execute its blocked-thorns handler");
		} finally {
			NeoForge.EVENT_BUS.unregister(listener);
		}
		helper.succeed();
	}

	@GameTest(template = "projectile_arena", timeoutTicks = 20)
	public static void frostRingHonorsPlacementCancellationAndPreservesFluid(GameTestHelper helper) {
		Player player = player(helper);
		player.setOnGround(true);
		player.tickCount = 20;
		BlockPos water = helper.absolutePos(new BlockPos(32, 4, 32));
		BlockPos lava = water.east(2);
		helper.getLevel().setBlockAndUpdate(water, Blocks.WATER.defaultBlockState());
		helper.getLevel().setBlockAndUpdate(lava, Blocks.LAVA.defaultBlockState());
		AtomicInteger blocked = new AtomicInteger();
		Consumer<BlockEvent.EntityPlaceEvent> listener = event -> {
			if (event.getEntity() == player) {
				blocked.incrementAndGet();
				helper.assertTrue(event.getPlacedBlock().is(Blocks.FROSTED_ICE) || event.getPlacedBlock().is(Blocks.OBSIDIAN), "Event must expose intended placement");
				helper.assertTrue(event.getBlockSnapshot().getState().is(Blocks.WATER) || event.getBlockSnapshot().getState().is(Blocks.LAVA), "Snapshot must retain original fluid");
				event.setCanceled(true);
			}
		};
		ItemStack ring = new ItemStack(ExtraBotanyItems.frostRing);
		NeoForge.EVENT_BUS.addListener(listener);
		try {
			((BaubleItem) ring.getItem()).onWornTick(ring, player);
			helper.assertTrue(blocked.get() == 2, "Both fluid conversions must request permission");
			helper.assertTrue(helper.getLevel().getBlockState(water).is(Blocks.WATER), "Canceled ice changed water");
			helper.assertTrue(helper.getLevel().getBlockState(lava).is(Blocks.LAVA), "Canceled obsidian changed lava");
		} finally {
			NeoForge.EVENT_BUS.unregister(listener);
		}
		((BaubleItem) ring.getItem()).onWornTick(ring, player);
		helper.assertTrue(helper.getLevel().getBlockState(water).is(Blocks.FROSTED_ICE), "Allowed water must freeze");
		helper.assertTrue(helper.getLevel().getBlockState(lava).is(Blocks.OBSIDIAN), "Allowed lava must freeze");
		helper.succeed();
	}

	@GameTest(template = "projectile_arena", timeoutTicks = 20)
	public static void silentEternityFreezesOnlyAfterMovementOrInterval(GameTestHelper helper) {
		ServerPlayer player = curseOwner(helper);
		player.xo = player.getX();
		player.yo = player.getY();
		player.zo = player.getZ();
		player.setOnGround(false);
		ItemStack stack = new ItemStack(ExtraBotanyItems.silentEternity);
		Relic.LOOKUP.find(stack).bindToUUID(player.getUUID());
		BaubleItem item = (BaubleItem) stack.getItem();
		BlockPos water = player.blockPosition().below();
		BlockPos lava = water.east(2);
		helper.getLevel().setBlockAndUpdate(water, Blocks.WATER.defaultBlockState());
		helper.getLevel().setBlockAndUpdate(lava, Blocks.LAVA.defaultBlockState());
		AtomicInteger attempts = new AtomicInteger();
		Consumer<BlockEvent.EntityPlaceEvent> listener = event -> {
			if (event.getEntity() == player) {
				attempts.incrementAndGet();
				event.setCanceled(true);
			}
		};
		NeoForge.EVENT_BUS.addListener(listener);
		try {
			player.tickCount = 20;
			item.onWornTick(stack, player);
			helper.assertTrue(attempts.get() == 2, "Interval must scan both fluids");
			for (int tick : new int[] {21, 22}) {
				player.tickCount = tick;
				item.onWornTick(stack, player);
			}
			helper.assertTrue(attempts.get() == 2, "Stationary ticks must not repeat blocked placements");
			player.setPos(player.position().add(1, 0, 0));
			player.tickCount = 23;
			item.onWornTick(stack, player);
			helper.assertTrue(attempts.get() == 4, "Moving one block must resume freezing immediately");
			player.xo = player.getX();
			player.tickCount = 40;
			item.onWornTick(stack, player);
			helper.assertTrue(attempts.get() == 6, "Stationary interval must resume freezing");
			helper.assertTrue(helper.getLevel().getBlockState(water).is(Blocks.WATER)
					&& helper.getLevel().getBlockState(lava).is(Blocks.LAVA), "Canceled scans must retain both fluids");
			helper.assertFalse(player.onGround(), "Rejected freezing must restore airborne state");
		} finally {
			NeoForge.EVENT_BUS.unregister(listener);
		}
		player.tickCount = 60;
		item.onWornTick(stack, player);
		helper.assertTrue(helper.getLevel().getBlockState(water).is(Blocks.FROSTED_ICE)
				&& helper.getLevel().getBlockState(lava).is(Blocks.OBSIDIAN), "Allowed interval must freeze both fluids");
		helper.assertFalse(player.onGround(), "Allowed freezing must restore airborne state");
		helper.succeed();
	}

	@GameTest(template = "projectile_arena", timeoutTicks = 20)
	public static void frostRingHonorsAdventurePermission(GameTestHelper helper) {
		Player player = player(helper);
		player.getAbilities().mayBuild = false;
		player.setOnGround(true);
		BlockPos water = player.blockPosition().below();
		helper.getLevel().setBlockAndUpdate(water, Blocks.WATER.defaultBlockState());
		FrostRingItem.freezeWater(player, helper.getLevel(), player.blockPosition(), 1);
		helper.assertTrue(helper.getLevel().getBlockState(water).is(Blocks.WATER), "Adventure restriction must preserve water");
		helper.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void frostRingDoesNotLoadDistantChunks(GameTestHelper helper) {
		Player player = player(helper);
		BlockPos distant = new BlockPos(20000008, 100, 20000008);
		player.setPos(Vec3.atBottomCenterOf(distant));
		player.setOnGround(true);
		helper.assertFalse(helper.getLevel().hasChunkAt(distant), "Fixture requires an unloaded chunk");
		FrostRingItem.freezeWater(player, helper.getLevel(), distant, 1);
		FrostRingItem.freezeLava(player, helper.getLevel(), distant, 1);
		helper.assertFalse(helper.getLevel().hasChunkAt(distant), "Freezing loaded a distant chunk");
		helper.succeed();
	}

	@GameTest(template = "projectile_arena", timeoutTicks = 20)
	public static void killBypassesEternityAndPotatoChips(GameTestHelper helper) {
		Player player = player(helper);
		equip(player, ExtraBotanyItems.potatoChips);
		ManaItem mana = supplyMana(player, 6000);
		player.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.ETERNITY, 100));
		player.kill();
		helper.assertTrue(player.isDeadOrDying(), "kill() must bypass Eternity and death rescue");
		helper.assertTrue(mana.getMana() == 6000 && !player.getCooldowns().isOnCooldown(ExtraBotanyItems.potatoChips), "Forced death must not spend rescue resources");
		helper.succeed();
	}

	@GameTest(template = "projectile_arena", timeoutTicks = 20)
	public static void voidDamageBypassesEternityAndPotatoChips(GameTestHelper helper) {
		Player player = player(helper);
		equip(player, ExtraBotanyItems.potatoChips);
		ManaItem mana = supplyMana(player, 6000);
		player.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.ETERNITY, 100));
		player.hurt(player.damageSources().fellOutOfWorld(), Float.MAX_VALUE);
		helper.assertTrue(player.isDeadOrDying(), "Void damage must bypass Eternity and death rescue");
		helper.assertTrue(mana.getMana() == 6000 && !player.getCooldowns().isOnCooldown(ExtraBotanyItems.potatoChips), "Void death must not spend rescue resources");
		helper.succeed();
	}

	@GameTest(template = "projectile_arena", timeoutTicks = 20)
	public static void ordinaryMagicStillHasEternityAndDeathRescue(GameTestHelper helper) {
		Player player = player(helper);
		equip(player, ExtraBotanyItems.potatoChips);
		ManaItem mana = supplyMana(player, 6000);
		player.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.ETERNITY, 100));
		player.hurt(player.damageSources().magic(), 1000);
		helper.assertTrue(player.getHealth() == player.getMaxHealth() && mana.getMana() == 6000, "Eternity must still protect ordinary magic");
		player.removeEffect(ExtraBotanyMobEffects.ETERNITY);
		player.invulnerableTime = 0;
		player.hurt(player.damageSources().magic(), 1000);
		helper.assertTrue(player.isAlive() && player.getHealth() == 5, "Chips must still rescue ordinary fatal damage");
		helper.assertTrue(mana.getMana() == 3000 && player.getCooldowns().isOnCooldown(ExtraBotanyItems.potatoChips), "Successful rescue must spend mana and start cooldown");
		helper.succeed();
	}

	private static ServerPlayer curseOwner(GameTestHelper helper) {
		ServerPlayer owner = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(),
				new GameProfile(UUID.randomUUID(), "eb-curse-owner"), ClientInformation.createDefault());
		owner.setPos(helper.absoluteVec(new Vec3(32.5, 5, 32.5)));
		owner.setYRot(0);
		owner.setXRot(0);
		owner.tickCount = 30;
		return owner;
	}

	@GameTest(template = "projectile_arena", timeoutTicks = 20)
	public static void curseRingHonorsServerPvpAndChargesOnlyAllowedTarget(GameTestHelper helper) {
		ServerPlayer owner = curseOwner(helper);
		Player victim = player(helper);
		victim.setPos(owner.position().add(0, 0, 3));
		ManaItem mana = supplyMana(owner, 200);
		ItemStack ring = new ItemStack(ExtraBotanyItems.curseRing);
		boolean previousPvp = helper.getLevel().getServer().isPvpAllowed();
		helper.assertTrue(helper.getLevel().addFreshEntity(victim), "Target must enter entity query");
		try {
			helper.getLevel().getServer().setPvpAllowed(false);
			((CurseRingItem) ring.getItem()).onWornTick(ring, owner);
			helper.assertFalse(victim.hasEffect(MobEffects.WITHER), "PvP-disabled target received curse");
			helper.assertTrue(mana.getMana() == 200, "Protected target consumed mana");
			helper.getLevel().getServer().setPvpAllowed(true);
			((CurseRingItem) ring.getItem()).onWornTick(ring, owner);
			helper.assertTrue(victim.hasEffect(MobEffects.WITHER) && victim.hasEffect(MobEffects.UNLUCK), "Allowed target must receive both effects");
			helper.assertTrue(mana.getMana() == 150, "Allowed target must consume one curse cost");
		} finally {
			victim.discard();
			helper.getLevel().getServer().setPvpAllowed(previousPvp);
		}
		helper.succeed();
	}

	@GameTest(template = "projectile_arena", timeoutTicks = 20)
	public static void curseRingHonorsCreativeAndTeamProtection(GameTestHelper helper) {
		ServerPlayer owner = curseOwner(helper);
		Player victim = player(helper);
		victim.setPos(owner.position().add(0, 0, 3));
		ManaItem mana = supplyMana(owner, 200);
		ItemStack ring = new ItemStack(ExtraBotanyItems.curseRing);
		var scoreboard = helper.getLevel().getScoreboard();
		String name = "eb" + UUID.randomUUID().toString().substring(0, 10);
		var team = scoreboard.addPlayerTeam(name);
		team.setAllowFriendlyFire(false);
		boolean previousPvp = helper.getLevel().getServer().isPvpAllowed();
		helper.assertTrue(helper.getLevel().addFreshEntity(victim), "Target must enter entity query");
		try {
			helper.getLevel().getServer().setPvpAllowed(true);
			victim.getAbilities().invulnerable = true;
			((CurseRingItem) ring.getItem()).onWornTick(ring, owner);
			helper.assertFalse(victim.hasEffect(MobEffects.WITHER), "Invulnerable player received curse");
			victim.getAbilities().invulnerable = false;
			scoreboard.addPlayerToTeam(owner.getScoreboardName(), team);
			scoreboard.addPlayerToTeam(victim.getScoreboardName(), team);
			((CurseRingItem) ring.getItem()).onWornTick(ring, owner);
			helper.assertFalse(victim.hasEffect(MobEffects.WITHER), "Friendly-fire-protected teammate received curse");
			helper.assertTrue(mana.getMana() == 200, "Protected targets consumed mana");
		} finally {
			victim.discard();
			scoreboard.removePlayerTeam(team);
			helper.getLevel().getServer().setPvpAllowed(previousPvp);
		}
		helper.succeed();
	}

	@GameTest(template = "projectile_arena", timeoutTicks = 20)
	public static void canceledMountJoinPreservesItemAndBinding(GameTestHelper helper) {
		for (Item item : List.of(ExtraBotanyItems.motor, ExtraBotanyItems.cosmicCarKey)) {
			Player player = player(helper);
			player.setXRot(90);
			helper.getLevel().setBlockAndUpdate(player.blockPosition().below(), Blocks.STONE.defaultBlockState());
			ItemStack stack = new ItemStack(item);
			UUID owner = UUID.randomUUID();
			Relic relic = Relic.LOOKUP.find(stack);
			if (relic != null) relic.bindToUUID(owner);
			player.setItemInHand(InteractionHand.MAIN_HAND, stack);
			ItemStack before = stack.copy();
			AtomicInteger attempts = new AtomicInteger();
			Consumer<EntityJoinLevelEvent> listener = event -> {
				if (event.getEntity() instanceof MountEntity mount && mount.distanceToSqr(player) < 25) {
					attempts.incrementAndGet();
					event.setCanceled(true);
				}
			};
			NeoForge.EVENT_BUS.addListener(listener);
			try {
				helper.assertTrue(stack.use(helper.getLevel(), player, InteractionHand.MAIN_HAND).getResult() == InteractionResult.FAIL, "Canceled mount join must report failure");
				helper.assertTrue(attempts.get() == 1, "Placement must reach the actual entity-join hook");
				helper.assertTrue(ItemStack.matches(before, stack), "Canceled placement changed item or binding");
			} finally {
				NeoForge.EVENT_BUS.unregister(listener);
			}
			stack.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
			List<MountEntity> mounts = helper.getLevel().getEntitiesOfClass(MountEntity.class, player.getBoundingBox().inflate(5));
			try {
				helper.assertTrue(mounts.size() == 1 && stack.isEmpty(), "Accepted join must create exactly one mount and consume one item");
			} finally {
				mounts.forEach(MountEntity::discard);
			}
		}
		helper.succeed();
	}
}
