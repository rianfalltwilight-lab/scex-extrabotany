package io.github.lounode.extrabotany.forge.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import vazkii.botania.common.block.BotaniaBlocks;

import io.github.lounode.extrabotany.api.gaia.GaiaArena;
import io.github.lounode.extrabotany.api.item.NatureEnergyItem;
import io.github.lounode.extrabotany.common.entity.EgoLandmineEntity;
import io.github.lounode.extrabotany.common.entity.EgoLandmineFormations;
import io.github.lounode.extrabotany.common.entity.EgoMinionEntity;
import io.github.lounode.extrabotany.common.entity.ExtraBotanyEntityType;
import io.github.lounode.extrabotany.common.entity.gaia.Ego;
import io.github.lounode.extrabotany.common.entity.gaia.GaiaIII;
import io.github.lounode.extrabotany.common.entity.gaia.behavior.EgoWeaponFire;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.item.equipment.bauble.NatureOrbItem;
import io.github.lounode.extrabotany.common.lib.OriginalContributors;
import io.github.lounode.extrabotany.common.sounds.ExtraBotanySounds;
import io.github.lounode.extrabotany.xplat.EXplatAbstractions;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 本我的回归测试：召唤、阶段、攻击和传送、伤害规则、战利品、地雷阵型、地雷和随从。
 * Regression tests for the Ego: the summon, the stages, the attacks and teleports, the damage rules, the loot, the landmine formations, the landmines and the minions.
 */
@GameTestHolder("extrabotany")
@PrefixGameTestTemplate(false)
public final class EgoGameTests {
	private static final BlockPos HOME = new BlockPos(32, 2, 32);

	private EgoGameTests() {}

	private static void makeFloor(GameTestHelper helper, int radius) {
		for (int x = -radius; x <= radius; x++) {
			for (int z = -radius; z <= radius; z++) {
				helper.setBlock(HOME.offset(x, -1, z), Blocks.STONE);
			}
		}
	}

	/**
	 * 在场地中间铺一块地板，并创建一个以它为家的本我。Boss 还没有加入世界。它没有场地数据，所以场地里没有玩家时它不会消失。
	 * Makes a floor in the center of the arena, and makes an Ego with that position as its home. The boss is not in the level at this time. It has no arena data, thus it does not go away when the arena has no players.
	 */
	private static Ego createEgo(GameTestHelper helper) {
		makeFloor(helper, 9);
		ServerLevel level = helper.getLevel();
		BlockPos home = helper.absolutePos(HOME);
		Ego ego = new Ego(ExtraBotanyEntityType.EGO, level);
		CompoundTag tag = ego.saveWithoutId(new CompoundTag());
		tag.put("Home", GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, GlobalPos.of(level.dimension(), home)).getOrThrow());
		tag.putInt("PlayerCount", 1);
		ego.load(tag);
		ego.setPos(Vec3.atBottomCenterOf(home));
		return ego;
	}

	private static <T extends Entity> List<T> near(GameTestHelper helper, Class<T> type) {
		return helper.getLevel().getEntitiesOfClass(type, new AABB(helper.absolutePos(HOME)).inflate(40));
	}

	/**
	 * 创建一名加入了世界的模拟玩家。这样范围查找才能找到它。
	 * Makes a mock player that is in the level. Only then can an area search find it.
	 */
	private static Player playerInLevel(GameTestHelper helper, BlockPos pos, GameType gameType) {
		Player player = helper.makeMockPlayer(gameType);
		player.setPos(Vec3.atBottomCenterOf(helper.absolutePos(pos)));
		helper.getLevel().addFreshEntity(player);
		return player;
	}

	/**
	 * 一次不受无敌帧影响的攻击。
	 * One attack that the invulnerability frames do not stop.
	 */
	private static void hit(Ego ego, Player player, float amount) {
		ego.invulnerableTime = 0;
		ego.hurt(player.damageSources().playerAttack(player), amount);
	}

	private static void cleanUp(GameTestHelper helper, Entity... entities) {
		for (Entity entity : entities) {
			entity.discard();
		}
		near(helper, Ego.class).forEach(Entity::discard);
		near(helper, EgoLandmineEntity.class).forEach(Entity::discard);
		near(helper, EgoMinionEntity.class).forEach(Entity::discard);
	}

	/**
	 * 自然蕴息宝珠在盖亚仪式场地的信标上召唤本我，并且消耗 200000 点自然之息。本我用召唤者的名字，生命值是 600，护甲值是 20，没有出场的无敌时间。自然之息不够、场地里已经有 Boss、或者玩家带着别的模组的物品时，召唤失败。
	 * The Nature Orb summons the Ego on the beacon of a Gaia ritual arena, and uses 200000 Nature Aura. The Ego has the name of its summoner, 600 health and 20 armor, and no invulnerable time at the start. The summon fails when there is too little Nature Aura, when the arena already has a boss, or when the player has an item of a different mod.
	 */
	@GameTest(template = "projectile_arena", timeoutTicks = 40)
	public static void natureOrbSummonsTheEgo(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		makeFloor(helper, 13);
		helper.setBlock(HOME, Blocks.BEACON);
		for (BlockPos offset : GaiaArena.PYLON_LOCATIONS) {
			helper.setBlock(HOME.offset(offset), BotaniaBlocks.GAIA_PYLON);
		}
		BlockPos beacon = helper.absolutePos(HOME);
		Player player = playerInLevel(helper, HOME.offset(2, 0, 0), GameType.SURVIVAL);
		player.setShiftKeyDown(true);
		ItemStack orb = new ItemStack(ExtraBotanyItems.natureOrb);
		player.setItemInHand(InteractionHand.MAIN_HAND, orb);
		NatureEnergyItem energy = EXplatAbstractions.INSTANCE.findNatureEnergyItem(orb);
		BlockHitResult hitResult = new BlockHitResult(Vec3.atCenterOf(beacon), Direction.UP, beacon, false);
		Runnable use = () -> NatureOrbItem.onPlayerInteract(new PlayerInteractEvent.RightClickBlock(player, InteractionHand.MAIN_HAND, beacon, hitResult));

		Difficulty difficulty = level.getDifficulty();
		try {
			level.getServer().getWorldData().setDifficulty(Difficulty.NORMAL);

			energy.addEnergy(NatureOrbItem.SUMMON_EGO_COST - 1);
			use.run();
			helper.assertTrue(near(helper, Ego.class).isEmpty() && energy.getEnergy() == NatureOrbItem.SUMMON_EGO_COST - 1,
					"With too little Nature Aura the orb must do nothing");

			energy.addEnergy(NatureOrbItem.SUMMON_EGO_COST + 1);
			ItemStack forbidden = new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.parse("patchouli:guide_book")));
			helper.assertTrue(!forbidden.isEmpty(), "The test needs an item of a different mod");
			player.getInventory().setItem(12, forbidden);
			use.run();
			helper.assertTrue(near(helper, Ego.class).isEmpty() && energy.getEnergy() == 2L * NatureOrbItem.SUMMON_EGO_COST,
					"With an item of a different mod in the inventory the summon must fail, and the Nature Aura must stay");
			player.getInventory().setItem(12, ItemStack.EMPTY);

			use.run();
			List<Ego> summoned = near(helper, Ego.class);
			helper.assertTrue(summoned.size() == 1, "The Nature Orb must summon one Ego, but there are " + summoned.size());
			helper.assertTrue(energy.getEnergy() == NatureOrbItem.SUMMON_EGO_COST, "The summon must use 200000 Nature Aura, but the orb has " + energy.getEnergy());
			helper.assertTrue(near(helper, GaiaIII.class).isEmpty(), "The Nature Orb must not summon Guardian of Gaia III");

			Ego ego = summoned.getFirst();
			helper.assertTrue(ego.getMaxHealth() == Ego.HEALTH_PER_PLAYER && ego.getHealth() == ego.getMaxHealth(),
					"With one player the Ego must have 600 health, but it has " + ego.getHealth() + " of " + ego.getMaxHealth());
			helper.assertTrue(ego.getAttributeValue(Attributes.ARMOR) == 20, "The Ego must have 20 armor");
			helper.assertTrue(ego.getAttributeBaseValue(Attributes.KNOCKBACK_RESISTANCE) == 1 && ego.getAttributeBaseValue(Attributes.MOVEMENT_SPEED) == 0.55
					&& ego.getAttributeBaseValue(Attributes.FOLLOW_RANGE) == 35 && ego.getAttributeBaseValue(Attributes.ATTACK_DAMAGE) == 8,
					"The Ego must have the attributes of the original: knockback resistance 1, speed 0.55, follow range 35, attack damage 8");
			helper.assertTrue(ego.getInvulTime() == 0 && ego.getStage() == 0, "The Ego must start in stage 0 with no invulnerable time");
			helper.assertTrue(ego.getSummoner().isPresent() && ego.getSummoner().get().equals(player.getUUID()), "The Ego must record its summoner");
			helper.assertTrue(ego.hasCustomName() && ego.getCustomName().getString().equals(player.getDisplayName().getString()),
					"The Ego must have the name of its summoner");
			helper.assertTrue(ego.getArena().isPresent() && ego.getArena().get().radius() == Ego.ARENA_RANGE && ego.getPlayerCount() == 1,
					"The Ego must have an arena with a radius of 12 and a player count of 1");
			helper.assertTrue(ego.getBGM() == ExtraBotanySounds.MUSIC_EGO, "The Ego must use the Ego music");
			helper.assertTrue(ego.getType().is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE,
					net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("c", "bosses"))), "The Ego must have the tag c:bosses");

			use.run();
			helper.assertTrue(near(helper, Ego.class).size() == 1 && energy.getEnergy() == NatureOrbItem.SUMMON_EGO_COST,
					"A second summon in the same arena must fail, and the Nature Aura must stay");
		} finally {
			level.getServer().getWorldData().setDifficulty(difficulty);
			cleanUp(helper, player);
		}
		helper.succeed();
	}

	/**
	 * 阶段数据读档之后不变。旧的盖亚守护者III存档里的阶段键不会让读档出错。
	 * The stage data stays after a load. The phase keys in an old save of Guardian of Gaia III do not make the load fail.
	 */
	@GameTest(template = "projectile_arena", timeoutTicks = 20)
	public static void egoSaveDataRoundTrip(GameTestHelper helper) {
		Ego ego = createEgo(helper);
		helper.assertTrue(ego.getStage() == 0 && ego.getWeaponType() == EgoWeaponFire.WEAPON_TRUE_SHADOW_KATANA, "A new Ego must start in stage 0 with the True Shadow Katana");

		CompoundTag tag = ego.saveWithoutId(new CompoundTag());
		tag.putInt("Stage", 1);
		tag.putInt("WeaponType", 3);
		tag.putUUID("Summoner", new java.util.UUID(12, 34));
		tag.putInt("BarrageWave", 4);
		tag.putInt("AttackDelay", 17);
		tag.putInt("WeaponChangeDelay", 55);
		tag.putInt("TeleportDelay", 23);
		tag.putInt("Teleports", 9);
		tag.putFloat("DamageTaken", 31.5F);
		ego.load(tag);

		CompoundTag saved = ego.saveWithoutId(new CompoundTag());
		for (String key : List.of("Stage", "WeaponType", "Summoner", "BarrageWave", "AttackDelay", "WeaponChangeDelay", "TeleportDelay", "Teleports", "DamageTaken")) {
			helper.assertTrue(tag.get(key).equals(saved.get(key)), "The save lost the key " + key);
		}
		helper.assertTrue(ego.getStage() == 1 && ego.getWeapon().is(ExtraBotanyItems.starWrath), "The stage or the weapon is incorrect after the load");

		GaiaIII gaia = new GaiaIII(helper.getLevel(), helper.absolutePos(HOME));
		CompoundTag old = gaia.saveWithoutId(new CompoundTag());
		old.putInt("BigPhase", 2);
		old.putInt("EgoStage", 1);
		old.putBoolean("InFogBarrage", true);
		old.putBoolean("InMinionPhase", true);
		gaia.load(old);
		helper.assertTrue(gaia.getBGM() == ExtraBotanySounds.MUSIC_GAIA3, "Guardian of Gaia III must use its own music");
		helper.succeed();
	}

	/**
	 * 八种阵型的地雷数量和原版相同。
	 * The number of landmines in each of the eight formations is the same as in the original.
	 */
	@GameTest(template = "projectile_arena", timeoutTicks = 20)
	public static void egoLandmineFormationCounts(GameTestHelper helper) {
		Ego ego = createEgo(helper);
		ServerLevel level = helper.getLevel();
		int[] expected = { 64, 80, 72, 80, 128, 102, 252, 318 };
		helper.assertTrue(EgoLandmineFormations.COUNT == expected.length, "There must be eight formations");
		for (int formation = 0; formation < expected.length; formation++) {
			EgoLandmineFormations.spawn(formation, level, helper.absolutePos(HOME), ego);
			List<EgoLandmineEntity> mines = near(helper, EgoLandmineEntity.class);
			helper.assertTrue(mines.size() == expected[formation], "Formation " + formation + " must have " + expected[formation] + " landmines, but it has " + mines.size());
			helper.assertTrue(mines.stream().allMatch(mine -> mine.getOwner() == ego), "A landmine of formation " + formation + " has no owner");
			if (formation == 5) {
				long blue = mines.stream().filter(mine -> mine.getLandmineType() == EgoLandmineEntity.TYPE_BLUE).count();
				long red = mines.stream().filter(mine -> mine.getLandmineType() == EgoLandmineEntity.TYPE_RED).count();
				helper.assertTrue(blue == 6 && red == 96, "Formation 5 must have 6 blue and 96 red landmines");
			}
			mines.forEach(Entity::discard);
		}
		helper.succeed();
	}

	/**
	 * 地雷在 50 tick 之后爆炸。蓝色地雷造成伤害、失明和不能解除的凋零。红色地雷把玩家打到只剩 1 点生命值，但是不会杀死玩家。绿色地雷让玩家丢出主手的物品。
	 * A landmine explodes after 50 ticks. The blue landmine gives damage, Blindness and a Wither that cannot be removed. The red landmine decreases the health of the player to 1, but does not kill the player. The green landmine makes the player drop the item in the main hand.
	 */
	@GameTest(template = "projectile_arena", timeoutTicks = 100)
	public static void egoLandmineTypes(GameTestHelper helper) {
		Ego ego = createEgo(helper);
		ServerLevel level = helper.getLevel();
		Player bluePlayer = playerInLevel(helper, HOME.offset(-6, 0, 0), GameType.SURVIVAL);
		Player redPlayer = playerInLevel(helper, HOME, GameType.SURVIVAL);
		Player greenPlayer = playerInLevel(helper, HOME.offset(6, 0, 0), GameType.SURVIVAL);
		greenPlayer.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND_SWORD));
		level.addFreshEntity(new EgoLandmineEntity(level, ego, EgoLandmineEntity.TYPE_BLUE, bluePlayer.position()));
		level.addFreshEntity(new EgoLandmineEntity(level, ego, EgoLandmineEntity.TYPE_RED, redPlayer.position()));
		level.addFreshEntity(new EgoLandmineEntity(level, ego, EgoLandmineEntity.TYPE_GREEN, greenPlayer.position()));

		helper.startSequence()
				.thenIdle(40)
				.thenExecute(() -> helper.assertTrue(bluePlayer.getHealth() == 20F && near(helper, EgoLandmineEntity.class).size() == 3,
						"A landmine exploded before 50 ticks"))
				.thenWaitUntil(() -> helper.assertTrue(near(helper, EgoLandmineEntity.class).isEmpty(), "The landmines did not explode"))
				.thenExecute(() -> {
					helper.assertTrue(bluePlayer.isAlive() && bluePlayer.getHealth() <= 15F && bluePlayer.getHealth() > 1F,
							"The blue landmine must give 5 life-losing damage and some magic damage, but the player has " + bluePlayer.getHealth());
					MobEffectInstance wither = bluePlayer.getEffect(MobEffects.WITHER);
					helper.assertTrue(wither != null && wither.getAmplifier() == 2 && wither.getCures().isEmpty() && bluePlayer.hasEffect(MobEffects.BLINDNESS),
							"The landmine must give Blindness and a Wither that cannot be removed");
					helper.assertTrue(redPlayer.isAlive() && redPlayer.getHealth() == 1F,
							"The red landmine must decrease the health to exactly 1, but the player has " + redPlayer.getHealth());
					// 模拟玩家丢出的物品不会进入世界，所以这里只检查主手已经空了。
					// The item that a mock player drops does not go into the level, thus this only examines that the main hand is empty.
					helper.assertTrue(greenPlayer.getMainHandItem().isEmpty(), "The green landmine must make the player drop the item in the main hand");
					cleanUp(helper, bluePlayer, redPlayer, greenPlayer);
				})
				.thenSucceed();
	}

	/**
	 * 四个随从出现在场地的四个角上，名字来自原版的贡献者名单，各拿一把不同的遗物剑。只有玩家能伤害它们，一次最多 20 点。本我消失之后它们也消失。
	 * Four minions appear at the four corners of the arena, with names from the list of contributors of the original, and each one has a different relic sword. Only players can hurt them, with a maximum of 20 for one attack. They go away after the Ego goes away.
	 */
	@GameTest(template = "projectile_arena", timeoutTicks = 100)
	public static void egoMinionRules(GameTestHelper helper) {
		Ego ego = createEgo(helper);
		ego.setNoAi(true);
		ServerLevel level = helper.getLevel();
		level.addFreshEntity(ego);
		EgoMinionEntity.spawnGroup(level, ego, helper.absolutePos(HOME), 60F);

		List<EgoMinionEntity> minions = near(helper, EgoMinionEntity.class);
		helper.assertTrue(minions.size() == EgoMinionEntity.TYPES, "There must be four minions, but there are " + minions.size());
		Set<String> names = new HashSet<>();
		Set<Integer> types = new HashSet<>();
		for (EgoMinionEntity minion : minions) {
			names.add(minion.getCustomName().getString());
			types.add(minion.getMinionType());
			helper.assertTrue(minion.getMaxHealth() == 60F && minion.getHealth() == 60F, "A minion must have the given health");
			helper.assertTrue(minion.getAttributeBaseValue(Attributes.ARMOR) == 10 && minion.getAttributeBaseValue(Attributes.MOVEMENT_SPEED) == 0.5
					&& minion.getAttributeBaseValue(Attributes.FOLLOW_RANGE) == 35 && minion.getAttributeBaseValue(Attributes.KNOCKBACK_RESISTANCE) == 1,
					"A summoned minion must have the attributes of the original: armor 10, speed 0.5, follow range 35, knockback resistance 1");
		}
		helper.assertTrue(names.size() == 4 && OriginalContributors.NAMES.containsAll(names), "The minions must have four different names of original contributors");
		helper.assertTrue(types.size() == 4, "The minions must have four different types");

		EgoMinionEntity katana = minions.stream().filter(minion -> minion.getMinionType() == 0).findFirst().orElseThrow();
		helper.assertTrue(katana.getWeapon().is(ExtraBotanyItems.trueShadowKatana), "The minion of type 0 must hold the True Shadow Katana");
		helper.assertTrue(minions.stream().filter(minion -> minion.getMinionType() == 3).findFirst().orElseThrow().getWeapon().is(ExtraBotanyItems.starWrath),
				"The minion of type 3 must hold the Star Wrath");

		Husk husk = new Husk(EntityType.HUSK, level);
		helper.assertTrue(!katana.hurt(level.damageSources().mobAttack(husk), 10F) && !katana.hurt(level.damageSources().lava(), 10F)
				&& katana.getHealth() == 60F, "Only players can hurt a minion");
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		katana.hurt(player.damageSources().playerAttack(player), 1000F);
		helper.assertTrue(katana.getHealth() < 60F && katana.getHealth() >= 40F, "One attack must give a maximum of 20 damage, but the minion has " + katana.getHealth());
		katana.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 0));

		helper.startSequence()
				.thenIdle(2)
				.thenExecute(() -> {
					helper.assertTrue(katana.isAlive() && !katana.hasEffect(MobEffects.POISON), "A minion must remove harmful effects");
					ego.discard();
				})
				.thenWaitUntil(() -> helper.assertTrue(near(helper, EgoMinionEntity.class).isEmpty(), "The minions must go away after the Ego goes away"))
				.thenSucceed();
	}

	/**
	 * 本我和它的仆从能用全部五种武器攻击：每一次攻击都会放出投射物，本我被攻击时不会被击退。
	 * The Ego and its minions can attack with all five weapons: each attack releases projectiles, and the Ego gets no knockback from a hit.
	 */
	@GameTest(template = "projectile_arena", timeoutTicks = 20)
	public static void egoAndMinionsFireEveryWeapon(GameTestHelper helper) {
		Ego ego = createEgo(helper);
		ego.setNoAi(true);
		ServerLevel level = helper.getLevel();
		level.addFreshEntity(ego);
		Player player = playerInLevel(helper, HOME.offset(4, 0, 0), GameType.CREATIVE);
		EgoMinionEntity.spawnGroup(level, ego, helper.absolutePos(HOME), 60F);
		EgoMinionEntity minion = near(helper, EgoMinionEntity.class).getFirst();
		minion.setNoAi(true);

		AABB area = new AABB(helper.absolutePos(HOME)).inflate(40);
		java.util.function.Supplier<List<Entity>> projectiles = () -> level.getEntitiesOfClass(Entity.class, area,
				entity -> !(entity instanceof Player) && !(entity instanceof Ego) && !(entity instanceof EgoMinionEntity));
		projectiles.get().forEach(Entity::discard);
		for (int weapon = EgoWeaponFire.WEAPON_TRUE_SHADOW_KATANA; weapon <= EgoWeaponFire.WEAPON_FIRST_FRACTAL; weapon++) {
			for (net.minecraft.world.entity.LivingEntity shooter : List.of(ego, minion)) {
				EgoWeaponFire.fire(shooter, player, weapon);
				List<Entity> fired = projectiles.get();
				helper.assertTrue(!fired.isEmpty(), shooter.getType() + " released no projectile with weapon " + weapon);
				fired.forEach(Entity::discard);
			}
		}

		Vec3 before = ego.getDeltaMovement();
		hit(ego, player, 10F);
		helper.assertTrue(ego.getDeltaMovement().equals(before), "A hit must not move the Ego, but its velocity changed from " + before + " to " + ego.getDeltaMovement());
		cleanUp(helper, player);
		helper.succeed();
	}

	/**
	 * 只有玩家能伤害本我，一次最多 25 点。累计受到 50 点伤害之后，它反击并且传送，累计值清零。
	 * Only players can hurt the Ego, with a maximum of 25 for one attack. After it gets 50 damage in total, it does a counter and teleports, and the total goes back to zero.
	 */
	@GameTest(template = "projectile_arena", timeoutTicks = 20)
	public static void egoDamageRules(GameTestHelper helper) {
		Ego ego = createEgo(helper);
		ego.setNoAi(true);
		ServerLevel level = helper.getLevel();
		level.addFreshEntity(ego);
		Player player = playerInLevel(helper, HOME.offset(3, 0, 0), GameType.CREATIVE);
		float full = ego.getHealth();

		Husk husk = new Husk(EntityType.HUSK, level);
		helper.assertTrue(!ego.hurt(level.damageSources().mobAttack(husk), 10F) && !ego.hurt(level.damageSources().lava(), 10F)
				&& !ego.hurt(level.damageSources().fellOutOfWorld(), 10F) && ego.getHealth() == full, "Only players can hurt the Ego");

		Vec3 start = ego.position();
		hit(ego, player, 1000F);
		float afterFirst = ego.getHealth();
		helper.assertTrue(afterFirst < full && afterFirst >= full - 25F, "One attack must give a maximum of 25 damage, but the Ego lost " + (full - afterFirst));
		helper.assertTrue(ego.saveWithoutId(new CompoundTag()).getFloat("DamageTaken") == 25F && ego.position().equals(start),
				"After 25 damage the Ego must not do a counter");

		hit(ego, player, 1000F);
		helper.assertTrue(ego.saveWithoutId(new CompoundTag()).getFloat("DamageTaken") == 0F, "After 50 damage the total must go back to zero");
		helper.assertTrue(!ego.position().equals(start), "After 50 damage the Ego must teleport");
		BlockPos home = helper.absolutePos(HOME);
		helper.assertTrue(Math.abs(ego.getX() - home.getX()) <= Ego.ARENA_RANGE / 2 && Math.abs(ego.getZ() - home.getZ()) <= Ego.ARENA_RANGE / 2,
				"The Ego must teleport to a position in the arena");

		ego.setInvulTime(30);
		float before = ego.getHealth();
		hit(ego, player, 1000F);
		helper.assertTrue(ego.getHealth() == before, "An invulnerable Ego must take no damage");
		cleanUp(helper, player);
		helper.succeed();
	}

	/**
	 * 本我在第一个 tick 就攻击并且传送。第 0 阶段它只用真影刃或者真泰拉之刃。
	 * The Ego attacks and teleports on the first tick. In stage 0 it only uses the True Shadow Katana or the True Terra Blade.
	 */
	@GameTest(template = "projectile_arena", timeoutTicks = 150)
	public static void egoAttacksAndTeleports(GameTestHelper helper) {
		Ego ego = createEgo(helper);
		helper.getLevel().addFreshEntity(ego);
		Player player = playerInLevel(helper, HOME.offset(4, 0, 0), GameType.CREATIVE);
		Vec3 start = ego.position();

		helper.startSequence()
				.thenWaitUntil(() -> helper.assertTrue(ego.saveWithoutId(new CompoundTag()).getInt("Teleports") >= 1, "The Ego did not teleport"))
				.thenExecute(() -> {
					CompoundTag tag = ego.saveWithoutId(new CompoundTag());
					helper.assertTrue(!ego.position().equals(start), "The Ego did not move at the teleport");
					helper.assertTrue(tag.getInt("AttackDelay") > 0 && tag.getInt("TeleportDelay") > 0, "The Ego must wait before the subsequent attack and teleport");
					helper.assertTrue(ego.getWeaponType() <= EgoWeaponFire.WEAPON_TRUE_TERRABLADE, "In stage 0 the Ego must use the True Shadow Katana or the True Terra Blade");
				})
				.thenIdle(105)
				.thenExecute(() -> {
					helper.assertTrue(ego.saveWithoutId(new CompoundTag()).getInt("Teleports") >= 2, "The Ego must teleport again after 100 ticks");
					helper.assertTrue(ego.getWeaponType() <= EgoWeaponFire.WEAPON_TRUE_TERRABLADE, "In stage 0 the Ego must use the True Shadow Katana or the True Terra Blade");
					helper.assertTrue(near(helper, EgoLandmineEntity.class).isEmpty(), "In stage 0 the Ego must not put down landmines");
					cleanUp(helper, player);
				})
				.thenSucceed();
	}

	/**
	 * 生命值低于 75% 时地雷弹幕开始：本我无敌，停在场地中心上方，40 tick 之后排出第一种阵型。
	 * Below 75% health the landmine barrage starts: the Ego cannot be hurt, stays above the center of the arena, and puts down the first formation after 40 ticks.
	 */
	@GameTest(template = "projectile_arena", timeoutTicks = 150)
	public static void egoStartsTheLandmineBarrage(GameTestHelper helper) {
		Ego ego = createEgo(helper);
		BlockPos home = helper.absolutePos(HOME);
		helper.getLevel().addFreshEntity(ego);
		Player player = playerInLevel(helper, HOME.offset(4, 0, 0), GameType.CREATIVE);
		ego.setHealth(ego.getMaxHealth() * 0.76F);

		helper.startSequence()
				.thenIdle(5)
				.thenExecute(() -> {
					helper.assertTrue(ego.getStage() == 0, "At 76% health the Ego must stay in stage 0");
					ego.setHealth(ego.getMaxHealth() * 0.74F);
				})
				.thenWaitUntil(() -> helper.assertTrue(ego.getStage() == 1 && ego.getInvulTime() > 400, "The landmine barrage did not start at 74% health"))
				.thenExecute(() -> helper.assertTrue(near(helper, EgoLandmineEntity.class).isEmpty(), "The first formation must not appear at the start of the barrage"))
				.thenWaitUntil(() -> helper.assertTrue(near(helper, EgoLandmineEntity.class).size() >= 64, "The first formation of the barrage did not appear"))
				.thenExecute(() -> {
					helper.assertTrue(ego.getInvulTime() <= 420 && ego.getInvulTime() >= 410, "The first formation must appear 40 ticks after the start, but the invulnerable time is " + ego.getInvulTime());
					helper.assertTrue(ego.blockPosition().equals(home.above(3)), "During the barrage the Ego must stay 3 blocks above the center of the arena");
					helper.assertTrue(ego.getLootTable() == BuiltInLootTables.EMPTY, "Before the last stage the Ego must have no loot");
					helper.assertTrue(near(helper, EgoMinionEntity.class).isEmpty(), "In stage 1 the Ego must not summon minions");
					cleanUp(helper, player);
				})
				.thenSucceed();
	}

	/**
	 * 生命值低于 25% 时本我召唤四个随从，无敌并且回血，只用最初分型。随从全部消失之后，无敌立刻结束。这时它才有战利品：一个潘多拉魔盒。
	 * Below 25% health the Ego summons four minions, cannot be hurt and regenerates, and only uses the First Fractal. After all minions are gone, it can be hurt again immediately. Only now it has loot: one Pandora's Box.
	 */
	@GameTest(template = "projectile_arena", timeoutTicks = 150)
	public static void egoMinionPhaseAndLoot(GameTestHelper helper) {
		Ego ego = createEgo(helper);
		CompoundTag tag = ego.saveWithoutId(new CompoundTag());
		tag.putInt("Stage", 1);
		tag.putInt("Teleports", 1);
		ego.load(tag);
		ego.setPos(Vec3.atBottomCenterOf(helper.absolutePos(HOME)));
		ServerLevel level = helper.getLevel();
		level.addFreshEntity(ego);
		Player player = playerInLevel(helper, HOME.offset(4, 0, 0), GameType.CREATIVE);
		float low = ego.getMaxHealth() * 0.2F;
		ego.setHealth(low);

		helper.startSequence()
				.thenWaitUntil(() -> helper.assertTrue(ego.getStage() == Ego.LAST_STAGE && near(helper, EgoMinionEntity.class).size() == 4, "The Ego did not summon four minions at 20% health"))
				.thenIdle(5)
				.thenExecute(() -> {
					helper.assertTrue(ego.getInvulTime() > 500 && ego.getHealth() > low, "With minions the Ego must be invulnerable and must regenerate");
					helper.assertTrue(ego.getWeaponType() == EgoWeaponFire.WEAPON_FIRST_FRACTAL, "In the last stage the Ego must use the First Fractal");
					helper.assertTrue(near(helper, EgoMinionEntity.class).stream().allMatch(minion -> minion.getMaxHealth() == 60F), "With one player each minion must have 60 health");
					near(helper, EgoMinionEntity.class).forEach(Entity::discard);
				})
				.thenWaitUntil(() -> helper.assertTrue(ego.getInvulTime() == 0, "After the minions are gone the Ego must lose its invulnerability"))
				.thenExecute(() -> {
					LootParams params = new LootParams.Builder(level)
							.withParameter(LootContextParams.THIS_ENTITY, ego)
							.withParameter(LootContextParams.ORIGIN, ego.position())
							.withParameter(LootContextParams.DAMAGE_SOURCE, player.damageSources().playerAttack(player))
							.withOptionalParameter(LootContextParams.ATTACKING_ENTITY, player)
							.withOptionalParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, player)
							.withParameter(LootContextParams.LAST_DAMAGE_PLAYER, player)
							.create(LootContextParamSets.ENTITY);
					helper.assertTrue(ego.getLootTable() != BuiltInLootTables.EMPTY, "In the last stage the Ego must have a loot table");
					List<ItemStack> loot = level.getServer().reloadableRegistries().getLootTable(ego.getLootTable()).getRandomItems(params);
					helper.assertTrue(loot.stream().filter(stack -> stack.is(ExtraBotanyItems.pandorasBox)).count() == 1, "The loot must have one Pandora's Box, but it is " + loot);
					helper.assertTrue(loot.stream().allMatch(stack -> stack.is(ExtraBotanyItems.pandorasBox) || stack.is(ExtraBotanyItems.recordEgo)),
							"The loot can only have the Pandora's Box and the Ego music disc, but it is " + loot);
					cleanUp(helper, player);
				})
				.thenSucceed();
	}

	/**
	 * 在场玩家比召唤时多的时候，本我离开。
	 * When there are more players than at the time of the summon, the Ego goes away.
	 */
	@GameTest(template = "projectile_arena", timeoutTicks = 40)
	public static void egoLeavesWhenMorePlayersJoin(GameTestHelper helper) {
		Ego ego = createEgo(helper);
		helper.getLevel().addFreshEntity(ego);
		Player first = playerInLevel(helper, HOME.offset(4, 0, 0), GameType.CREATIVE);
		Player[] second = new Player[1];

		helper.startSequence()
				.thenIdle(5)
				.thenExecute(() -> {
					helper.assertTrue(ego.isAlive(), "With the same number of players as at the summon the Ego must stay");
					second[0] = playerInLevel(helper, HOME.offset(-4, 0, 0), GameType.CREATIVE);
				})
				.thenWaitUntil(() -> helper.assertTrue(ego.isRemoved(), "With more players than at the summon the Ego must go away"))
				.thenExecute(() -> cleanUp(helper, first, second[0]))
				.thenSucceed();
	}

	/**
	 * 盖亚守护者III不再有本我的阶段：生命值很低时它不排出本我的地雷，也不召唤本我仆从。
	 * Guardian of Gaia III no longer has the Ego phase: with very low health it does not put down landmines of the Ego and does not summon Ego Minions.
	 */
	@GameTest(template = "projectile_arena", timeoutTicks = 100)
	public static void gaiaThirdHasNoEgoPhase(GameTestHelper helper) {
		makeFloor(helper, 9);
		BlockPos home = helper.absolutePos(HOME);
		GaiaIII gaia = new GaiaIII(helper.getLevel(), home);
		gaia.setPos(Vec3.atBottomCenterOf(home));
		helper.getLevel().addFreshEntity(gaia);
		gaia.setHealth(gaia.getMaxHealth() * 0.1F);

		helper.startSequence()
				.thenIdle(80)
				.thenExecute(() -> {
					helper.assertTrue(near(helper, EgoLandmineEntity.class).isEmpty() && near(helper, EgoMinionEntity.class).isEmpty(),
							"Guardian of Gaia III must not use the landmines and the minions of the Ego");
					helper.assertTrue(gaia.getBGM() == ExtraBotanySounds.MUSIC_GAIA3, "Guardian of Gaia III must use its own music");
					gaia.discard();
					// 只清理这个场地中间的实体，不碰旁边别的测试。
					// Only the entities in the middle of this arena are removed, and not those of the adjacent tests.
					helper.getLevel().getEntitiesOfClass(Entity.class, new AABB(home).inflate(24), entity -> !(entity instanceof Player)).forEach(Entity::discard);
				})
				.thenSucceed();
	}
}
