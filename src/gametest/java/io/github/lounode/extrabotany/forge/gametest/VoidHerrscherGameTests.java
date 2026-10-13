package io.github.lounode.extrabotany.forge.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import io.github.lounode.extrabotany.common.entity.ExtraBotanyEntityType;
import io.github.lounode.extrabotany.common.entity.SubspaceLanceEntity;
import io.github.lounode.extrabotany.common.entity.SwordDomainEntity;
import io.github.lounode.extrabotany.common.entity.VoidFieldEntity;
import io.github.lounode.extrabotany.common.entity.gaia.VoidHerrscher;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

import java.util.List;

/**
 * 空之律者和它的召唤物的回归测试。
 * Regression tests for the Herrscher of the Void and the entities that it summons.
 */
@GameTestHolder("extrabotany")
@PrefixGameTestTemplate(false)
public final class VoidHerrscherGameTests {
	private static final BlockPos HOME = new BlockPos(32, 2, 32);

	private VoidHerrscherGameTests() {}

	/**
	 * 在场地中间铺一块地板，并创建一个以它为家的空之律者。Boss 还没有加入世界。
	 * Makes a floor in the center of the arena, and makes a Herrscher of the Void with that position as its home. The boss is not in the level at this time.
	 */
	private static VoidHerrscher createBoss(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		for (int x = -9; x <= 9; x++) {
			for (int z = -9; z <= 9; z++) {
				helper.setBlock(HOME.offset(x, -1, z), Blocks.STONE);
			}
		}
		BlockPos home = helper.absolutePos(HOME);
		VoidHerrscher boss = new VoidHerrscher(ExtraBotanyEntityType.VOID_HERRSCHER, level);
		CompoundTag tag = boss.saveWithoutId(new CompoundTag());
		tag.put("Home", GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, GlobalPos.of(level.dimension(), home)).getOrThrow());
		boss.load(tag);
		boss.setPos(Vec3.atBottomCenterOf(home));
		return boss;
	}

	private static Player player(GameTestHelper helper, BlockPos pos) {
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		player.setPos(Vec3.atBottomCenterOf(helper.absolutePos(pos)));
		return player;
	}

	private static <T extends Entity> List<T> near(GameTestHelper helper, Class<T> type) {
		return helper.getLevel().getEntitiesOfClass(type, new AABB(helper.absolutePos(HOME)).inflate(30));
	}

	/**
	 * 一次不受无敌时间影响的攻击。
	 * One attack that the invulnerability time does not stop.
	 */
	private static void hit(VoidHerrscher boss, Player player, float amount) {
		boss.setInvulTime(0);
		boss.invulnerableTime = 0;
		boss.hurt(player.damageSources().playerAttack(player), amount);
	}

	/**
	 * 存档的键名和以前的版本相同，读档之后阶段、护盾和冷却都不变。三面护盾时伤害上限是 14。
	 * The keys in the save are the same as in the previous versions, and the rank, the shields and the cooldowns stay after a load. With three shields the damage limit is 14.
	 */
	@GameTest(template = "projectile_arena", timeoutTicks = 20)
	public static void herrscherSaveDataRoundTrip(GameTestHelper helper) {
		VoidHerrscher boss = createBoss(helper);
		helper.assertTrue(boss.getDamageCap() == 25F && !boss.isRankII() && boss.getShields() == 0, "A new boss must have no shields and a damage limit of 25");

		CompoundTag tag = boss.saveWithoutId(new CompoundTag());
		tag.putInt("ShieldLayers", 2);
		tag.putInt("RotatingShields", 3);
		tag.putBoolean("RankII", true);
		tag.putBoolean("RankIII", true);
		tag.putFloat("HerrscherDamageTaken", 31.5F);
		tag.putInt("HerrscherTpDelay", 41);
		tag.putInt("DodgeCd", 117);
		tag.putInt("SkillCd", 89);
		tag.putInt("SkillType", 2);
		tag.putInt("SupportCd", 211);
		tag.putBoolean("EmergeLanceDone", true);
		ListTag supporters = new ListTag();
		supporters.add(StringTag.valueOf("ExtraMeteorP"));
		tag.put("Supporters", supporters);
		boss.load(tag);

		CompoundTag saved = boss.saveWithoutId(new CompoundTag());
		for (String key : List.of("ShieldLayers", "RotatingShields", "RankII", "RankIII", "HerrscherDamageTaken", "HerrscherTpDelay", "DodgeCd",
				"SkillCd", "SkillType", "SupportCd", "EmergeLanceDone", "Supporters")) {
			helper.assertTrue(tag.get(key).equals(saved.get(key)), "The save lost the key " + key);
		}
		helper.assertTrue(boss.isRankII() && boss.isRankIII() && boss.getShields() == 3, "The rank or the shields are incorrect after the load");
		helper.assertTrue(boss.getDamageCap() == 14F, "With three shields the damage limit must be 14, but it is " + boss.getDamageCap());

		// 用命令召唤的 Boss 没有这些键，它必须保留默认值。
		// A boss from a command does not have these keys, and it must keep the defaults.
		VoidHerrscher summoned = new VoidHerrscher(ExtraBotanyEntityType.VOID_HERRSCHER, helper.getLevel());
		summoned.load(new CompoundTag());
		helper.assertTrue(summoned.saveWithoutId(new CompoundTag()).getInt("ShieldLayers") == 5
				&& summoned.saveWithoutId(new CompoundTag()).getInt("DodgeCd") == 300, "A boss without save data lost its defaults");
		helper.succeed();
	}

	/**
	 * 开头的 5 次攻击无效。之后每次攻击最多造成 25 点伤害。累计 80 点伤害时，Boss 传送并反击攻击者。
	 * The first 5 attacks have no effect. After that one attack gives a maximum of 25 damage. At a total of 80 damage, the boss teleports and gives a counter to the attacker.
	 */
	@GameTest(template = "projectile_arena", timeoutTicks = 20)
	public static void herrscherAbsorbsHitsCapsDamageAndCounters(GameTestHelper helper) {
		VoidHerrscher boss = createBoss(helper);
		Player player = player(helper, HOME.offset(3, 0, 0));
		float maxHealth = boss.getMaxHealth();
		Vec3 start = boss.position();

		for (int i = 0; i < 3; i++) {
			hit(boss, player, 100F);
		}
		helper.assertTrue(boss.getHealth() == maxHealth && player.getHealth() == 20F && boss.position().equals(start),
				"The first three attacks must have no effect");
		hit(boss, player, 100F);
		// 真实伤害先造成 0.01 点普通伤害，所以这里允许很小的误差。
		// True damage first gives a usual hit of 0.01, thus a very small tolerance is permitted here.
		helper.assertTrue(boss.getHealth() == maxHealth && Math.abs(player.getHealth() - 12F) < 0.05F,
				"At 80 total damage the attacker must get true damage of 10% of maximum health + 6, but the player has " + player.getHealth());
		helper.assertTrue(!boss.position().equals(start), "At 80 total damage the boss must teleport");

		hit(boss, player, 100F);
		helper.assertTrue(boss.getHealth() == maxHealth, "The fifth attack must have no effect");
		hit(boss, player, 100F);
		helper.assertTrue(boss.getHealth() == maxHealth - 25F, "The sixth attack must give exactly 25 damage, but the boss lost " + (maxHealth - boss.getHealth()));

		boss.setHealth(1F);
		helper.assertTrue(boss.getHealth() == maxHealth - 50F, "The health must not decrease by more than 25 at one time");
		boss.hurt(helper.getLevel().damageSources().lava(), 10F);
		Husk husk = new Husk(EntityType.HUSK, helper.getLevel());
		boss.setInvulTime(0);
		boss.invulnerableTime = 0;
		boss.hurt(helper.getLevel().damageSources().mobAttack(husk), 10F);
		helper.assertTrue(boss.getHealth() == maxHealth - 50F, "Only players can hurt the boss");
		helper.succeed();
	}

	/**
	 * 护盾减少伤害，让 Boss 复制攻击者的药水效果。很重的攻击打碎一面护盾，并让 Boss 闪避下一次攻击。
	 * A shield decreases the damage and makes the boss copy the potion effects of the attacker. A very heavy attack breaks one shield and makes the boss dodge the subsequent attack.
	 */
	@GameTest(template = "projectile_arena", timeoutTicks = 20)
	public static void herrscherShieldsAndDodge(GameTestHelper helper) {
		VoidHerrscher boss = createBoss(helper);
		Player player = player(helper, HOME.offset(3, 0, 0));
		player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 1));
		CompoundTag tag = boss.saveWithoutId(new CompoundTag());
		tag.putInt("ShieldLayers", 0);
		tag.putInt("RotatingShields", 3);
		tag.putBoolean("RankII", true);
		boss.load(tag);
		float maxHealth = boss.getMaxHealth();

		// 20 x 0.85^3 = 12.3，没有超过 20，所以护盾不碎。
		// 20 x 0.85^3 = 12.3, which is not more than 20, thus no shield breaks.
		hit(boss, player, 20F);
		helper.assertTrue(boss.getShields() == 3 && Math.abs(maxHealth - boss.getHealth() - 20F * 0.85F * 0.85F * 0.85F) < 0.001F,
				"Three shields must decrease 20 damage to 12.28, but the boss lost " + (maxHealth - boss.getHealth()));
		helper.assertTrue(boss.hasEffect(MobEffects.MOVEMENT_SPEED), "A boss with shields must copy the potion effects of the attacker");

		float before = boss.getHealth();
		hit(boss, player, 100F);
		helper.assertTrue(boss.getShields() == 2 && Math.abs(before - boss.getHealth() - 14F) < 0.001F,
				"A heavy attack must break one shield and give the limit of 14 damage, but the boss lost " + (before - boss.getHealth()));

		before = boss.getHealth();
		hit(boss, player, 100F);
		helper.assertTrue(boss.getHealth() == before && near(helper, VoidFieldEntity.class).size() == 1,
				"After a shield breaks, the boss must dodge the subsequent attack and make one void field");
		hit(boss, player, 100F);
		helper.assertTrue(boss.getHealth() < before, "The dodge must have a cooldown");
		helper.succeed();
	}

	/**
	 * 生命值降到 80% 时进入二阶段：一面护盾、八把审判之剑和两把矛枪。降到 25% 时进入三阶段：三面护盾、生命值回满，再加四把矛枪。
	 * At 80% health rank II starts: one shield, eight swords of judgment and two lances. At 25% health rank III starts: three shields, full health and four more lances.
	 */
	@GameTest(template = "projectile_arena", timeoutTicks = 100)
	public static void herrscherRankTransitions(GameTestHelper helper) {
		VoidHerrscher boss = createBoss(helper);
		Player player = player(helper, HOME.offset(3, 0, 0));
		boss.markPlayerAttacked(player);
		helper.getLevel().addFreshEntity(boss);
		float maxHealth = boss.getMaxHealth();

		helper.startSequence()
				.thenExecute(() -> {
					while (boss.getHealth() > maxHealth * 0.8F) {
						boss.setHealth(boss.getHealth() - 20F);
					}
				})
				.thenWaitUntil(() -> helper.assertTrue(boss.isRankII() && !boss.isRankIII() && boss.getShields() == 1, "Rank II did not start at 80% health"))
				.thenExecute(() -> {
					helper.assertTrue(near(helper, SwordDomainEntity.class).size() == 8, "Rank II must start with eight swords of judgment");
					helper.assertTrue(near(helper, SubspaceLanceEntity.class).size() == 2, "Rank II must start with two lances");
					while (boss.getHealth() > maxHealth * 0.25F) {
						boss.setHealth(boss.getHealth() - 20F);
					}
				})
				.thenWaitUntil(() -> helper.assertTrue(boss.isRankIII() && boss.getShields() == 3, "Rank III did not start at 25% health"))
				.thenExecute(() -> {
					helper.assertTrue(boss.getHealth() == maxHealth, "Rank III must start with full health");
					helper.assertTrue(near(helper, SubspaceLanceEntity.class).size() == 6, "Rank III must add four lances");
					boss.discard();
				})
				.thenSucceed();
	}

	/**
	 * 矛枪落到地面上方然后停住。它旁边的生物引发闪电链，闪电链在附近的生物之间跳跃。主人死后矛枪消失。
	 * The lance falls to a position above the ground and then stops. A creature adjacent to it starts a lightning chain, which jumps between the creatures near it. The lance goes away after its owner dies.
	 *
	 * <p>这个模组里 Boss 的攻击只伤害玩家。所以这里的铁傀儡只得到虚弱效果，不受伤害。
	 * <p>In this mod the attacks of a boss only hurt players. Thus the iron golems here only get the Weakness effect and no damage.
	 */
	@GameTest(template = "projectile_arena", timeoutTicks = 200)
	public static void subspaceLanceStandsAndDischarges(GameTestHelper helper) {
		VoidHerrscher owner = createBoss(helper);
		ServerLevel level = helper.getLevel();
		BlockPos home = helper.absolutePos(HOME);
		SubspaceLanceEntity lance = new SubspaceLanceEntity(level, owner, 4F, 1200);
		lance.setPos(home.getX(), home.getY() + 6, home.getZ());
		level.addFreshEntity(lance);

		IronGolem trigger = helper.spawnWithNoFreeWill(EntityType.IRON_GOLEM, HOME);
		IronGolem other = helper.spawnWithNoFreeWill(EntityType.IRON_GOLEM, HOME.offset(5, 0, 0));
		float golemHealth = trigger.getMaxHealth();

		helper.startSequence()
				.thenWaitUntil(() -> helper.assertTrue(other.hasEffect(MobEffects.WEAKNESS), "The lightning chain did not jump to the creature near the lance"))
				.thenExecute(() -> {
					double height = lance.getY() - home.getY();
					helper.assertTrue(height > 1.7 && height <= 1.9, "The lance must stop between 1.75 and 1.9 blocks above the floor, but it is at " + height);
					helper.assertTrue(trigger.hasEffect(MobEffects.WEAKNESS), "The lightning chain must jump back to the creature that started it");
					helper.assertTrue(other.getHealth() == golemHealth && trigger.getHealth() == golemHealth, "The attack of a boss must not hurt creatures that are not players");
					owner.kill();
				})
				.thenWaitUntil(() -> helper.assertTrue(!lance.isAlive(), "The lance must go away after its owner dies"))
				.thenSucceed();
	}

	/**
	 * 虚空力场让里面的生物几乎动不了，60 tick 之后消失。
	 * The void field almost stops the creatures in it, and goes away after 60 ticks.
	 */
	@GameTest(template = "projectile_arena", timeoutTicks = 100)
	public static void voidFieldSlowsCreatures(GameTestHelper helper) {
		createBoss(helper);
		ServerLevel level = helper.getLevel();
		Husk husk = helper.spawnWithNoFreeWill(EntityType.HUSK, HOME);
		husk.setNoGravity(true);
		VoidFieldEntity field = new VoidFieldEntity(level);
		field.setPos(husk.position());
		level.addFreshEntity(field);

		husk.setDeltaMovement(1, 0, 0);
		field.tick();
		helper.assertTrue(Math.abs(husk.getDeltaMovement().x - 0.12) < 1.0E-6, "The void field must multiply the velocity by 0.12");
		helper.startSequence()
				.thenIdle(55)
				.thenExecute(() -> helper.assertTrue(field.isAlive(), "The void field went away too soon"))
				.thenWaitUntil(() -> helper.assertTrue(!field.isAlive(), "The void field must go away after 60 ticks"))
				.thenSucceed();
	}

	/**
	 * 每名参战的玩家得到的战利品：潘多拉魔盒、三个限量补给袋和唱片。
	 * The loot for each fighter: a Pandora's Box, three bags of the type Supply Bag Limited Edition and the music disc.
	 */
	@GameTest(template = "projectile_arena", timeoutTicks = 20)
	public static void herrscherLootTable(GameTestHelper helper) {
		VoidHerrscher boss = createBoss(helper);
		Player player = player(helper, HOME.offset(3, 0, 0));
		ServerLevel level = helper.getLevel();
		LootParams params = new LootParams.Builder(level)
				.withParameter(LootContextParams.THIS_ENTITY, boss)
				.withParameter(LootContextParams.ORIGIN, boss.position())
				.withParameter(LootContextParams.DAMAGE_SOURCE, player.damageSources().playerAttack(player))
				.withOptionalParameter(LootContextParams.ATTACKING_ENTITY, player)
				.withOptionalParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, player)
				.withParameter(LootContextParams.LAST_DAMAGE_PLAYER, player)
				.create(LootContextParamSets.ENTITY);
		List<ItemStack> loot = level.getServer().reloadableRegistries().getLootTable(boss.getLootTable()).getRandomItems(params);

		helper.assertTrue(loot.stream().anyMatch(stack -> stack.is(ExtraBotanyItems.pandorasBox)), "The loot does not have the Pandora's Box");
		helper.assertTrue(loot.stream().anyMatch(stack -> stack.is(ExtraBotanyItems.limitedEditionSupplyBag) && stack.getCount() == 3),
				"The loot does not have three Limited Edition Supply Bags");
		helper.assertTrue(loot.stream().anyMatch(stack -> stack.is(ExtraBotanyItems.recordHerrscherOfTheVoid)), "The loot does not have the music disc");
		helper.succeed();
	}

	/**
	 * "来自虚空的呼唤"只在潜行时起作用。场地不合格时它不被消耗。
	 * The Call of the Void only works with sneak. It is not used when the arena is not correct.
	 */
	@GameTest(template = "projectile_arena", timeoutTicks = 20)
	public static void voidCallerNeedsSneakAndArena(GameTestHelper helper) {
		helper.setBlock(HOME, Blocks.BEACON);
		BlockPos beacon = helper.absolutePos(HOME);
		Player player = player(helper, HOME.offset(2, 0, 0));
		ItemStack caller = new ItemStack(ExtraBotanyItems.voidCaller);
		player.setItemInHand(InteractionHand.MAIN_HAND, caller);
		BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(beacon), Direction.UP, beacon, false);

		helper.assertTrue(caller.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit)) == InteractionResult.PASS,
				"Without sneak the item must do nothing");
		player.setShiftKeyDown(true);
		InteractionResult result = caller.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
		helper.assertTrue(result == InteractionResult.FAIL && caller.getCount() == 1 && near(helper, VoidHerrscher.class).isEmpty(),
				"A beacon without pylons and an arena must not summon the boss, and the item must stay");
		helper.succeed();
	}
}
