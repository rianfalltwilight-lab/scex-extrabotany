package io.github.lounode.extrabotany.forge.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.registries.RegisterEvent;

import top.theillusivec4.curios.api.CuriosApi;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.entity.PixieEntity;

import io.github.lounode.extrabotany.api.gaia.GaiaArena;
import io.github.lounode.extrabotany.common.entity.EgoLandmineEntity;
import io.github.lounode.extrabotany.common.entity.EgoMinionEntity;
import io.github.lounode.extrabotany.common.entity.ExtraBotanyEntityType;
import io.github.lounode.extrabotany.common.entity.ExtraBotanyMemoryType;
import io.github.lounode.extrabotany.common.entity.MagicLandMineEntity;
import io.github.lounode.extrabotany.common.entity.SkullLandMineEntity;
import io.github.lounode.extrabotany.common.entity.gaia.Ego;
import io.github.lounode.extrabotany.common.entity.gaia.Gaia;
import io.github.lounode.extrabotany.common.entity.gaia.GaiaIII;
import io.github.lounode.extrabotany.common.entity.gaia.VoidHerrscher;
import io.github.lounode.extrabotany.common.entity.gaia.behavior.GaiaDisarm;
import io.github.lounode.extrabotany.common.entity.gaia.behavior.GaiaSpawnPixies;
import io.github.lounode.extrabotany.common.entity.gaia.behavior.GaiaTeleport;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.item.equipment.bauble.NatureOrbItem;
import io.github.lounode.extrabotany.xplat.EXplatAbstractions;
import io.github.lounode.extrabotany.xplat.ExtraBotanyConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@GameTestHolder("extrabotany")
@PrefixGameTestTemplate(false)
public final class P2BossGameTests {
	private static final BlockPos HOME = new BlockPos(32, 3, 32);
	private static final ResourceLocation ALLOWED_RING = ResourceLocation.parse("enigmaticlegacy:cursed_ring");
	private static final ResourceLocation FORBIDDEN_SIBLING = ResourceLocation.parse("enigmaticlegacy:p2_test_forbidden");

	private P2BossGameTests() {}

	// Exercise the existing exact-ID allowlist without installing an optional mod.
	// These registrations exist only in the GameTest source set.
	@EventBusSubscriber(modid = "extrabotany", bus = EventBusSubscriber.Bus.MOD)
	public static final class TestItems {
		@SubscribeEvent
		public static void registerItems(RegisterEvent event) {
			event.register(Registries.ITEM, registry -> {
				if (!ModList.get().isLoaded("enigmaticlegacy")) {
					registry.register(ALLOWED_RING, new Item(new Item.Properties()));
				}
				registry.register(FORBIDDEN_SIBLING, new Item(new Item.Properties()));
			});
		}
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void lowHealthHerrscherRestoresIntoNewInstance(GameTestHelper h) {
		assertHealthRoundTrip(h, 400, 400, 100);
		h.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void multiplayerHerrscherRestoresHealthAndAttributes(GameTestHelper h) {
		// Three players store a 1200 base, but vanilla MAX_HEALTH clamps its effective value to 1024.
		assertHealthRoundTrip(h, 1200, 1024, 87.5F);
		h.succeed();
	}

	private static void assertHealthRoundTrip(GameTestHelper h, float maximum, float effectiveMaximum, float remaining) {
		VoidHerrscher source = new VoidHerrscher(ExtraBotanyEntityType.VOID_HERRSCHER, h.getLevel());
		source.getAttribute(Attributes.MAX_HEALTH).setBaseValue(maximum);
		source.setHealth(maximum);
		h.assertTrue(source.getHealth() == effectiveMaximum && source.getMaxHealth() == effectiveMaximum
				&& source.getAttribute(Attributes.MAX_HEALTH).getBaseValue() == maximum,
				"Fixture did not preserve the base attribute and its effective health limit");
		CompoundTag state = source.saveWithoutId(new CompoundTag());
		state.putInt("PlayerCount", (int) maximum / 400);
		state.putInt("ShieldLayers", 2);
		state.putInt("RotatingShields", 3);
		state.putBoolean("RankII", true);
		state.putBoolean("RankIII", true);
		state.putInt("SkillCd", 89);
		state.putInt("SkillType", 2);
		state.putInt("DodgeCd", 117);
		state.putInt("HerrscherTpDelay", 41);
		source.load(state);
		while (source.getHealth() > remaining) {
			source.setHealth(Math.max(remaining, source.getHealth() - 20));
		}
		h.assertTrue(source.getHealth() == remaining, "Fixture did not reach the saved low health");
		CompoundTag saved = source.saveWithoutId(new CompoundTag());
		VoidHerrscher restored = new VoidHerrscher(ExtraBotanyEntityType.VOID_HERRSCHER, h.getLevel());
		h.assertTrue(restored != source && restored.getHealth() == 400, "Fixture must load into a fresh default-health boss");
		restored.load(saved);
		h.assertTrue(restored.getHealth() == remaining,
				"Loading health was incorrectly treated as one capped hit: " + restored.getHealth());
		h.assertTrue(restored.getMaxHealth() == effectiveMaximum
				&& restored.getAttribute(Attributes.MAX_HEALTH).getBaseValue() == maximum,
				"Loading changed the multiplayer base attribute or its effective health limit");
		h.assertTrue(restored.getPlayerCount() == maximum / 400 && restored.isRankII() && restored.isRankIII() && restored.getShields() == 3,
				"Restored health is inconsistent with multiplayer attributes or combat phase");
		CompoundTag roundTrip = restored.saveWithoutId(new CompoundTag());
		for (String key : List.of("Health", "attributes", "PlayerCount", "ShieldLayers", "RotatingShields", "RankII", "RankIII", "SkillCd", "SkillType", "DodgeCd", "HerrscherTpDelay")) {
			h.assertTrue(saved.get(key).equals(roundTrip.get(key)), "Round trip changed " + key);
		}
		restored.setHealth(1);
		h.assertTrue(restored.getHealth() == remaining - 25, "Load bypass leaked into subsequent combat health changes");
		restored.kill();
		h.assertTrue(restored.getHealth() == 0, "Explicit kill no longer bypasses the hit cap");
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void exactItemAllowlistSurvivesCombatDisarm(GameTestHelper h) {
		h.assertTrue(ExtraBotanyConfig.common().guardianItemCheck() && !ExtraBotanyConfig.common().disableGaiaDisArm(), "Fixture needs item checks enabled");
		h.assertTrue(BuiltInRegistries.ITEM.containsKey(ALLOWED_RING), "Allowlisted test item missing");
		Player player = h.makeMockPlayer(GameType.SURVIVAL);
		player.setPos(h.absolutePos(new BlockPos(2, 3, 2)).getCenter());
		ItemStack allowed = marked(new ItemStack(BuiltInRegistries.ITEM.get(ALLOWED_RING)), "allowed");
		ItemStack forbidden = marked(new ItemStack(BuiltInRegistries.ITEM.get(FORBIDDEN_SIBLING), 3), "forbidden");
		h.assertTrue(GaiaArena.checkGuardianAllowed(allowed) && !GaiaArena.checkFeasibility(allowed), "Test must rely on the exact-ID exception, not a namespace/config exemption");
		h.assertTrue(!GaiaArena.checkGuardianAllowed(forbidden) && !GaiaArena.checkFeasibility(forbidden), "Sibling item must remain forbidden");
		player.setItemInHand(InteractionHand.MAIN_HAND, allowed.copy());
		player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD, allowed.copy());
		var curios = CuriosApi.getCuriosInventory(player).orElseThrow().getCurios().values().stream()
				.filter(handler -> handler.getStacks().getSlots() > 0).findFirst().orElseThrow().getStacks();
		curios.setStackInSlot(0, allowed.copy());
		h.assertTrue(GaiaArena.checkGuardianInventoryPass(player), "Summon inventory check rejected the explicit exception");
		player.setItemInHand(InteractionHand.OFF_HAND, forbidden.copy());
		try {
			new DisarmStep().apply(player);
			h.assertTrue(ItemStack.matches(player.getMainHandItem(), allowed)
					&& ItemStack.matches(player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD), allowed)
					&& ItemStack.matches(curios.getStackInSlot(0), allowed), "Combat disarm removed an explicitly allowed equipped item");
			h.assertTrue(player.getOffhandItem().isEmpty(), "Fix exempted the entire third-party namespace");
			var drops = h.getLevel().getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(3));
			h.assertTrue(drops.size() == 1 && ItemStack.matches(drops.getFirst().getItem(), forbidden), "Forbidden sibling did not drop exactly once with its components/count");
			new DisarmStep().apply(player);
			h.assertTrue(h.getLevel().getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(3)).size() == 1, "Repeated disarm duplicated the drop");
		} finally {
			h.getLevel().getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(3)).forEach(Entity::discard);
			player.discard();
		}
		h.succeed();
	}

	private static final class DisarmStep extends GaiaDisarm<Gaia> {
		private void apply(Player player) { disArm(player); }
	}

	private static ItemStack marked(ItemStack stack, String value) {
		CompoundTag data = new CompoundTag();
		data.putString("p2_boss", value);
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
		return stack;
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void hurtTeleportUsesFourTickDelayForBothGaias(GameTestHelper h) {
		BlockPos home = h.absolutePos(new BlockPos(2, 3, 2));
		for (boolean third : new boolean[] { false, true }) {
			for (int previousDelay : new int[] { 30, 0 }) {
				Gaia boss = third ? new GaiaIII(h.getLevel(), home) : new Gaia(h.getLevel(), home);
				boss.setPos(Vec3.atCenterOf(home.above(3)));
				TeleportStep behavior = new TeleportStep();
				GaiaTeleport.initMemories(boss.getBrain(), 12, 35, previousDelay);
				boss.getBrain().setMemory(MemoryModuleType.HURT_BY, boss.damageSources().generic());
				Vec3 before = boss.position();
				behavior.step(h.getLevel(), boss);
				h.assertTrue(behavior.getTeleportDelay(boss) == 3 && !behavior.isHurt(boss) && boss.position().equals(before), "The new hurt delay was overwritten or teleported immediately");
				for (int i = 0; i < 3; i++) behavior.step(h.getLevel(), boss);
				h.assertTrue(boss.position().equals(before) && behavior.getTeleportDelay(boss) == 0, "Teleport happened before four ticks elapsed");
				behavior.step(h.getLevel(), boss);
				h.assertTrue(!boss.position().equals(before) && behavior.getTeleportDelay(boss) == 35, "The hurt-triggered teleport did not happen or reset its normal delay");
				before = boss.position();
				behavior.setTeleportDelay(boss, 2);
				behavior.step(h.getLevel(), boss);
				behavior.step(h.getLevel(), boss);
				h.assertTrue(boss.position().equals(before) && behavior.getTeleportDelay(boss) == 0, "Unhurt countdown changed");
				behavior.step(h.getLevel(), boss);
				h.assertTrue(!boss.position().equals(before) && behavior.getTeleportDelay(boss) == 35, "Normal teleport stopped working");
			}
		}
		h.succeed();
	}

	private static final class TeleportStep extends GaiaTeleport<Gaia> {
		private void step(ServerLevel level, Gaia boss) { tick(level, boss, level.getGameTime()); }
	}

	@GameTest(template = "projectile_arena", timeoutTicks = 20)
	public static void cancelledGaiaIIISummonKeepsTicket(GameTestHelper h) { exerciseSummon(h, Summon.GAIA_III); }

	@GameTest(template = "projectile_arena", timeoutTicks = 20)
	public static void cancelledEgoSummonKeepsNatureEnergy(GameTestHelper h) { exerciseSummon(h, Summon.EGO); }

	@GameTest(template = "projectile_arena", timeoutTicks = 20)
	public static void cancelledHerrscherSummonKeepsVoidCaller(GameTestHelper h) { exerciseSummon(h, Summon.HERRSCHER); }

	@GameTest(template = "projectile_arena", timeoutTicks = 20)
	public static void cancelledLegacyGaiaSummonKeepsInput(GameTestHelper h) { exerciseSummon(h, Summon.LEGACY); }

	private enum Summon { GAIA_III, EGO, HERRSCHER, LEGACY }

	private static void exerciseSummon(GameTestHelper h, Summon type) {
		BlockPos home = makeArena(h);
		Player player = player(h, home.offset(2, 0, 0));
		player.setShiftKeyDown(true);
		Item item = type == Summon.EGO ? ExtraBotanyItems.natureOrb : type == Summon.HERRSCHER ? ExtraBotanyItems.voidCaller : ExtraBotanyItems.challengeTicket;
		ItemStack input = marked(new ItemStack(item, type == Summon.EGO ? 1 : 3), "summon");
		player.setItemInHand(InteractionHand.MAIN_HAND, input);
		if (type == Summon.EGO) EXplatAbstractions.INSTANCE.findNatureEnergyItem(input).addEnergy(400000);
		ItemStack before = input.copy();
		AtomicInteger attempts = new AtomicInteger();
		Consumer<EntityJoinLevelEvent> cancel = event -> {
			if (event.getEntity() instanceof Gaia boss && boss.getHome().pos().equals(home)) {
				attempts.incrementAndGet();
				event.setCanceled(true);
			}
		};
		Difficulty difficulty = h.getLevel().getDifficulty();
		NeoForge.EVENT_BUS.addListener(cancel);
		try {
			h.getLevel().getServer().getWorldData().setDifficulty(Difficulty.NORMAL);
			invokeSummon(h, player, home, type, false);
			h.assertTrue(attempts.get() == 1, "Legal ritual never reached the cancelled boss join");
			h.assertTrue(ItemStack.matches(input, before), "Cancelled spawn consumed or changed the ritual input/energy");
			h.assertTrue(bosses(h, home).isEmpty(), "Cancelled boss entered the level");
			NeoForge.EVENT_BUS.unregister(cancel);
			invokeSummon(h, player, home, type, true);
			List<Gaia> spawned = bosses(h, home);
			h.assertTrue(spawned.size() == 1, "Successful ritual must create exactly one boss");
			h.assertTrue(switch (type) {
				case GAIA_III -> spawned.getFirst() instanceof GaiaIII;
				case EGO -> spawned.getFirst() instanceof Ego;
				case HERRSCHER -> spawned.getFirst() instanceof VoidHerrscher;
				case LEGACY -> spawned.getFirst().getType() == ExtraBotanyEntityType.GAIA_LEGACY;
			}, "Ritual created the wrong boss");
			if (type == Summon.EGO) {
				h.assertTrue(input.getCount() == 1 && EXplatAbstractions.INSTANCE.findNatureEnergyItem(input).getEnergy() == 200000,
						"Successful Ego ritual did not charge exactly one summon cost");
			} else {
				h.assertTrue(input.getCount() == 2 && ItemStack.isSameItemSameComponents(input, before), "Successful ritual did not consume exactly one original item");
			}
		} finally {
			NeoForge.EVENT_BUS.unregister(cancel);
			bosses(h, home).forEach(Entity::discard);
			player.discard();
			h.getLevel().getServer().getWorldData().setDifficulty(difficulty);
		}
		h.succeed();
	}

	private static void invokeSummon(GameTestHelper h, Player player, BlockPos home, Summon type, boolean success) {
		BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(home), Direction.UP, home, false);
		if (type == Summon.EGO) {
			var event = new PlayerInteractEvent.RightClickBlock(player, InteractionHand.MAIN_HAND, home, hit);
			NatureOrbItem.onPlayerInteract(event);
			h.assertTrue(event.isCanceled() == success, "Ego summon reported the wrong interaction outcome");
		} else if (type == Summon.LEGACY) {
			h.assertTrue(Gaia.spawn(player, player.getMainHandItem(), h.getLevel(), home) == success, "Legacy spawn reported the wrong result");
		} else {
			InteractionResult result = player.getMainHandItem().getItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
			if (type == Summon.HERRSCHER) h.assertTrue((result != InteractionResult.FAIL) == success, "Void Caller reported the wrong result");
		}
	}

	private static BlockPos makeArena(GameTestHelper h) {
		BlockPos home = h.absolutePos(HOME);
		for (int x = -17; x <= 17; x++) for (int z = -17; z <= 17; z++) {
			for (int y = 0; y <= 8; y++) h.getLevel().setBlockAndUpdate(home.offset(x, y, z), Blocks.AIR.defaultBlockState());
			h.getLevel().setBlockAndUpdate(home.offset(x, -1, z), Blocks.IRON_BLOCK.defaultBlockState());
		}
		h.getLevel().setBlockAndUpdate(home, Blocks.BEACON.defaultBlockState());
		for (BlockPos offset : GaiaArena.PYLON_LOCATIONS) h.getLevel().setBlockAndUpdate(home.offset(offset), BotaniaBlocks.GAIA_PYLON.defaultBlockState());
		return home;
	}

	private static Player player(GameTestHelper h, BlockPos pos) {
		Player player = h.makeMockPlayer(GameType.SURVIVAL);
		player.setPos(Vec3.atBottomCenterOf(pos));
		player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200);
		player.setHealth(200);
		h.assertTrue(h.getLevel().addFreshEntity(player), "Failed to add test player");
		return player;
	}

	private static List<Gaia> bosses(GameTestHelper h, BlockPos home) {
		return h.getLevel().getEntitiesOfClass(Gaia.class, GaiaArena.getArenaBB(home, 20));
	}

	@GameTest(template = "projectile_arena", timeoutTicks = 30)
	public static void extraPlayerCancelsEgoAndOnlyItsOwnSummons(GameTestHelper h) { exerciseIllegalFight(h, true); }

	@GameTest(template = "projectile_arena", timeoutTicks = 30)
	public static void extraPlayerCancelsHerrscherAndOnlyItsOwnMines(GameTestHelper h) { exerciseIllegalFight(h, false); }

	private static void exerciseIllegalFight(GameTestHelper h, boolean egoFight) {
		BlockPos home = h.absolutePos(HOME);
		Player first = player(h, home.offset(2, 0, 0));
		Player second = player(h, home.offset(-2, 0, 0));
		ItemStack held = marked(new ItemStack(Items.DIAMOND, 7), "mine-victim");
		first.setItemInHand(InteractionHand.MAIN_HAND, held);
		Gaia boss = egoFight ? new EgoStep(h.getLevel()) : new HerrscherStep(h.getLevel());
		setArena(boss, home);
		h.getLevel().addFreshEntity(boss);
		Ego other = new Ego(ExtraBotanyEntityType.EGO, h.getLevel());
		setArena(other, home);
		List<Entity> own = new ArrayList<>();
		List<Entity> foreign = new ArrayList<>();
		own.add(addSkullMine(h, boss, first.position(), 0));
		foreign.add(addSkullMine(h, other, Vec3.atCenterOf(home.offset(8, 0, 0)), 100));
		if (egoFight) {
			EgoLandmineEntity mine = new EgoLandmineEntity(h.getLevel(), boss, EgoLandmineEntity.TYPE_GREEN, first.position());
			mine.tickCount = 49;
			h.getLevel().addFreshEntity(mine);
			own.add(mine);
			own.add(addMinion(h, (Ego) boss, home.offset(4, 0, 0)));
			EgoLandmineEntity otherMine = new EgoLandmineEntity(h.getLevel(), other, EgoLandmineEntity.TYPE_GREEN, Vec3.atCenterOf(home.offset(8, 0, 0)));
			h.getLevel().addFreshEntity(otherMine);
			foreign.add(otherMine);
			foreign.add(addMinion(h, other, home.offset(7, 0, 0)));
		}
		Difficulty difficulty = h.getLevel().getDifficulty();
		try {
			h.getLevel().getServer().getWorldData().setDifficulty(Difficulty.NORMAL);
			h.assertTrue(boss.getPlayersAround().size() == 2 && boss.getPlayerCount() == 1, "Fixture must trigger the extra-player cancellation rule");
			if (boss instanceof EgoStep ego) ego.step(); else ((HerrscherStep) boss).step();
			h.assertTrue(boss.isRemoved() && own.stream().allMatch(Entity::isRemoved), "Cancelled fight left its live mines/minions behind");
			h.assertTrue(foreign.stream().noneMatch(Entity::isRemoved), "Cancellation removed a different boss's summons");
			// Their survival is verified; keep their independent attacks out of the
			// following world-tick check for this cancelled fight's damage.
			foreign.forEach(Entity::discard);
		} catch (RuntimeException | Error failure) {
			cleanup(first, second, boss, other);
			own.forEach(Entity::discard);
			foreign.forEach(Entity::discard);
			throw failure;
		} finally {
			h.getLevel().getServer().getWorldData().setDifficulty(difficulty);
		}
		float health = first.getHealth();
		h.runAfterDelay(2, () -> {
			try {
				h.assertTrue(first.getHealth() == health && !first.hasEffect(MobEffects.WITHER) && first.getMainHandItem() == held,
						"A cancelled mine still damaged, afflicted or disarmed the player on a later world tick");
				h.assertTrue(own.stream().allMatch(Entity::isRemoved), "Cancelled summons returned");
			} finally {
				cleanup(first, second, boss, other);
				own.forEach(Entity::discard);
				foreign.forEach(Entity::discard);
			}
			h.succeed();
		});
	}

	private static final class EgoStep extends Ego {
		private EgoStep(ServerLevel level) { super(ExtraBotanyEntityType.EGO, level); playerCount = 1; }
		private void step() { customServerAiStep(); }
	}

	private static final class HerrscherStep extends VoidHerrscher {
		private HerrscherStep(ServerLevel level) { super(ExtraBotanyEntityType.VOID_HERRSCHER, level); playerCount = 1; }
		private void step() { customServerAiStep(); }
	}

	private static void setArena(Gaia boss, BlockPos home) {
		GlobalPos center = GlobalPos.of(boss.level().dimension(), home);
		CompoundTag tag = boss.saveWithoutId(new CompoundTag());
		tag.put("Home", GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, center).getOrThrow());
		tag.put("Arena", GaiaArena.CODEC.encodeStart(NbtOps.INSTANCE, GaiaArena.of(center, boss.getArenaRange(), 7)).getOrThrow());
		boss.load(tag);
		boss.setPos(Vec3.atCenterOf(home.above(3)));
	}

	private static MagicLandMineEntity addSkullMine(GameTestHelper h, Gaia boss, Vec3 pos, int countdown) {
		MagicLandMineEntity mine = new SkullLandMineEntity.Disarm(h.getLevel(), boss);
		mine.setPos(pos);
		mine.setExplodeCountDown(countdown);
		h.assertTrue(h.getLevel().addFreshEntity(mine), "Failed to add mine");
		return mine;
	}

	private static EgoMinionEntity addMinion(GameTestHelper h, Ego boss, BlockPos pos) {
		EgoMinionEntity minion = new EgoMinionEntity(ExtraBotanyEntityType.EGO_MINION, h.getLevel());
		CompoundTag tag = minion.saveWithoutId(new CompoundTag());
		tag.putUUID("Summoner", boss.getUUID());
		minion.load(tag);
		minion.setPos(Vec3.atCenterOf(pos));
		minion.setNoAi(true);
		h.assertTrue(h.getLevel().addFreshEntity(minion), "Failed to add minion");
		return minion;
	}

	@GameTest(template = "projectile_arena", timeoutTicks = 20)
	public static void gaiaDiscardCleansItsMinesAndPixiesOnly(GameTestHelper h) {
		BlockPos home = h.absolutePos(HOME);
		GaiaIII boss = new GaiaIII(h.getLevel(), home);
		GaiaIII other = new GaiaIII(h.getLevel(), home);
		setArena(boss, home);
		setArena(other, home);
		Player target = player(h, home.offset(2, 0, 0));
		boss.getBrain().setMemory(MemoryModuleType.NEAREST_PLAYERS, List.of(target));
		new PixieStep().spawn(boss);
		List<PixieEntity> pixies = h.getLevel().getEntitiesOfClass(PixieEntity.class, GaiaArena.getArenaBB(home, 20));
		h.assertTrue(pixies.size() == 1, "Real Gaia pixie behavior did not create one pixie");
		PixieEntity foreignPixie = new PixieEntity(h.getLevel(), true);
		foreignPixie.setProps(target, other, 8);
		foreignPixie.setPos(boss.position());
		h.getLevel().addFreshEntity(foreignPixie);
		MagicLandMineEntity ownMine = addSkullMine(h, boss, target.position(), 10);
		MagicLandMineEntity foreignMine = addSkullMine(h, other, target.position(), 10);
		try {
			boss.discard();
			h.assertTrue(ownMine.isRemoved() && pixies.getFirst().isRemoved(), "Gaia permanent removal left its mines/pixies behind");
			h.assertTrue(!foreignMine.isRemoved() && !foreignPixie.isRemoved(), "Gaia cleanup removed another fight's summons");
		} finally {
			cleanup(target, boss, other, ownMine, foreignMine, foreignPixie);
			pixies.forEach(Entity::discard);
		}
		h.succeed();
	}

	private static final class PixieStep extends GaiaSpawnPixies<Gaia> {
		private void spawn(Gaia boss) { spawnPixies(boss); }
	}

	@GameTest(template = "projectile_arena", timeoutTicks = 20)
	public static void chunkUnloadPreservesSummonsAndReloadReconnectsOwner(GameTestHelper h) {
		BlockPos home = h.absolutePos(HOME);
		Ego boss = new EgoStep(h.getLevel());
		setArena(boss, home);
		h.getLevel().addFreshEntity(boss);
		MagicLandMineEntity skull = addSkullMine(h, boss, Vec3.atCenterOf(home), 50);
		EgoLandmineEntity mine = new EgoLandmineEntity(h.getLevel(), boss, EgoLandmineEntity.TYPE_GREEN, Vec3.atCenterOf(home));
		h.getLevel().addFreshEntity(mine);
		EgoMinionEntity minion = addMinion(h, boss, home.offset(3, 0, 0));
		CompoundTag saved = boss.saveWithoutId(new CompoundTag());
		Ego reloaded = new Ego(ExtraBotanyEntityType.EGO, h.getLevel());
		try {
			boss.remove(Entity.RemovalReason.UNLOADED_TO_CHUNK);
			h.assertTrue(!skull.isRemoved() && !mine.isRemoved() && !minion.isRemoved(), "Chunk unloading permanently cleared the fight");
			reloaded.load(saved);
			h.assertTrue(h.getLevel().addFreshEntity(reloaded), "Could not reload the boss with its original UUID");
			h.assertTrue(skull.getOwner() == reloaded && mine.getOwner() == reloaded && minion.isSummonedBy(reloaded), "Surviving summons did not reconnect to the loaded boss");
			reloaded.discard();
			h.assertTrue(skull.isRemoved() && mine.isRemoved() && minion.isRemoved(), "Permanent removal after reload failed to clean owned summons");
		} finally {
			cleanup(reloaded, skull, mine, minion);
		}
		h.succeed();
	}

	@GameTest(template = "projectile_arena", timeoutTicks = 20)
	public static void egoDeathAndKillUseTheSameOwnedCleanup(GameTestHelper h) {
		BlockPos home = h.absolutePos(HOME);
		for (boolean kill : new boolean[] { false, true }) {
			Ego boss = new EgoStep(h.getLevel());
			setArena(boss, home);
			Ego other = new EgoStep(h.getLevel());
			setArena(other, home);
			MagicLandMineEntity skull = addSkullMine(h, boss, Vec3.atCenterOf(home), 50);
			EgoLandmineEntity mine = new EgoLandmineEntity(h.getLevel(), boss, EgoLandmineEntity.TYPE_GREEN, Vec3.atCenterOf(home));
			h.getLevel().addFreshEntity(mine);
			EgoMinionEntity minion = addMinion(h, boss, home.offset(3, 0, 0));
			MagicLandMineEntity foreign = addSkullMine(h, other, Vec3.atCenterOf(home), 50);
			try {
				if (kill) boss.kill(); else boss.die(boss.damageSources().generic());
				h.assertTrue(skull.isRemoved() && mine.isRemoved() && minion.isRemoved(), "Death/kill did not immediately clear all owned summons");
				h.assertTrue(!foreign.isRemoved(), "Death/kill removed a different boss's mine");
			} finally {
				cleanup(boss, other, skull, mine, minion, foreign);
			}
		}
		h.succeed();
	}

	private static void cleanup(Entity... entities) {
		for (Entity entity : entities) entity.discard();
	}
}
