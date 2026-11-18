package io.github.lounode.extrabotany.forge.gametest;

import io.github.lounode.extrabotany.api.ExtraBotanyAPI;
import io.github.lounode.extrabotany.api.gaia.GaiaArena;
import io.github.lounode.extrabotany.common.entity.DarkPixieEntity;
import io.github.lounode.extrabotany.common.entity.ExtraBotanyEntityType;
import io.github.lounode.extrabotany.common.entity.SkullLandMineEntity;
import io.github.lounode.extrabotany.common.entity.SkullMinionEntity;
import io.github.lounode.extrabotany.common.entity.SwordDomainEntity;
import io.github.lounode.extrabotany.common.entity.gaia.GaiaIII;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.item.relic.voidcore.CoreOfTheVoidItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.EffectCures;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.botania.common.block.BotaniaBlocks;

/**
 * 对照原版审计第 E4 批：Boss。盖亚守护者III、骷髅仆从、地雷和导弹按 1.12；空之律者的彩虹核心和回血按 1.12；暗影妖精按 1.12。
 * Batch E4 of the parity audit with the original: the bosses. Guardian of Gaia III, the skull minions, the landmines and the missiles follow 1.12; the rainbow core and the healing of the Herrscher of the Void follow 1.12; the dark pixie follows 1.12.
 */
@GameTestHolder("extrabotany")
@PrefixGameTestTemplate(false)
public final class E4ParityGameTests {

	private E4ParityGameTests() {}

	/** 建一个盖亚仪式场地，返回信标的位置。 / Builds a Ritual of Gaia arena and returns the position of the beacon. */
	private static BlockPos buildArena(GameTestHelper h, BlockPos relative) {
		var level = h.getLevel();
		BlockPos center = h.absolutePos(relative);
		for (int x = -14; x <= 14; x++) {
			for (int z = -14; z <= 14; z++) {
				for (int y = 0; y <= GaiaIII.ARENA_HEIGHT; y++) {
					level.setBlockAndUpdate(center.offset(x, y, z), Blocks.AIR.defaultBlockState());
				}
				level.setBlockAndUpdate(center.offset(x, -1, z), Blocks.IRON_BLOCK.defaultBlockState());
			}
		}
		level.setBlockAndUpdate(center, Blocks.BEACON.defaultBlockState());
		for (var offset : GaiaArena.PYLON_LOCATIONS) {
			level.setBlockAndUpdate(center.offset(offset), BotaniaBlocks.GAIA_PYLON.defaultBlockState());
		}
		return center;
	}

	private static GaiaIII summon(GameTestHelper h, BlockPos center, Player player) {
		var level = h.getLevel();
		var difficulty = level.getDifficulty();
		try {
			level.getServer().getWorldData().setDifficulty(Difficulty.NORMAL);
			h.assertTrue(GaiaIII.spawn(player, new ItemStack(ExtraBotanyItems.challengeTicket), level, center), "The summon failed");
		} finally {
			level.getServer().getWorldData().setDifficulty(difficulty);
		}
		return level.getEntitiesOfClass(GaiaIII.class, new AABB(center).inflate(16)).getFirst();
	}

	/**
	 * 这两个测试把场地建在远离自身结构的地方，会覆盖同时运行的别的测试，所以放在单独的批次里（批次按顺序运行）。
	 * These two tests build their arena far from their own structure and would overwrite another test that runs at the same time, so they are in a batch of their own (batches run one after another).
	 */
	private static final String ARENA_BATCH = "e4_gaia_arena";

	// Original 1.12 EntityGaiaIII: 600 health for each player, no armor, 5 shields that each stop one hit, at most 30 from one hit.
	@GameTest(template = "empty", timeoutTicks = 40, batch = ARENA_BATCH)
	public static void gaiaIIISummonShieldsAndCap(GameTestHelper h) {
		BlockPos center = buildArena(h, new BlockPos(0, 4, 240));
		Player player = h.makeMockPlayer(GameType.SURVIVAL);
		player.setPos(center.offset(2, 0, 0).getCenter());
		h.getLevel().addFreshEntity(player);
		GaiaIII gaia = summon(h, center, player);
		try {
			h.assertTrue(gaia.getMaxHealth() == 600F && gaia.getHealth() == 600F, "One player must give 600 health, got " + gaia.getMaxHealth());
			h.assertTrue(gaia.getAttributeValue(Attributes.ARMOR) == 0, "The 30 armor of Reburn is still there");
			h.assertTrue(gaia.getShield() == 5 && gaia.getInvulTime() == 0, "It must start with 5 shields and no invulnerability");
			for (int i = 0; i < 5; i++) {
				gaia.invulnerableTime = 0;
				gaia.hurt(player.damageSources().playerAttack(player), 20F);
			}
			h.assertTrue(gaia.getShield() == 0 && gaia.getHealth() == 600F, "Each shield must stop one hit");
			gaia.invulnerableTime = 0;
			gaia.hurt(player.damageSources().playerAttack(player), 100F);
			h.assertTrue(gaia.getHealth() == 570F, "One hit must deal at most 30, health " + gaia.getHealth());
		} finally {
			gaia.discard();
			player.discard();
		}
		h.succeed();
	}

	// Original 1.12 EntityGaiaIII.onLivingUpdate: at 75% health, rank II with 5 shields, 4 skull minions and 8 swords of judgment.
	@GameTest(template = "empty", timeoutTicks = 40, batch = ARENA_BATCH)
	public static void gaiaIIIRankTwoSummonsMinionsAndSwords(GameTestHelper h) {
		BlockPos center = buildArena(h, new BlockPos(0, 4, 300));
		Player player = h.makeMockPlayer(GameType.SURVIVAL);
		player.setPos(center.offset(2, 0, 0).getCenter());
		h.getLevel().addFreshEntity(player);
		GaiaIII gaia = summon(h, center, player);
		var area = new AABB(center).inflate(16);
		try {
			gaia.setShield(0);
			while (gaia.getHealth() > gaia.getMaxHealth() * 0.7F) {
				gaia.setHealth(gaia.getHealth() - 30F);
			}
			gaia.markPlayerAttacked(player);
			gaia.tickCount = 60;
			gaia.tick();
			h.assertTrue(gaia.isRankII() && !gaia.isRankIII(), "It must be in rank II");
			h.assertTrue(gaia.getShield() == 5, "Rank II must give 5 shields");
			var minions = h.getLevel().getEntitiesOfClass(SkullMinionEntity.class, area, m -> m.blockPosition().closerThan(center.above(), 5));
			h.assertTrue(minions.size() == 4 && minions.stream().allMatch(m -> m.getShield() == 2), "Rank II must summon 4 minions with 2 shields");
			// 只数以这个场地为中心的剑：并行的别的 Boss 测试可能在附近也召唤了剑。 / Only count the swords of this arena: another boss test that runs at the same time can summon swords nearby.
			var swords = h.getLevel().getEntitiesOfClass(SwordDomainEntity.class, area, s -> center.equals(s.getSource()));
			h.assertTrue(swords.size() == 8, "Rank II must summon 8 swords, got " + swords.size());
			minions.forEach(m -> m.discard());
			swords.forEach(s -> s.discard());
		} finally {
			gaia.discard();
			player.discard();
		}
		h.succeed();
	}

	// Original 1.12 EntitySkullMinion: 24 health, 2 shields stop 2 hits, at most 10 from one hit.
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void skullMinionFollows112(GameTestHelper h) {
		Player player = h.makeMockPlayer(GameType.SURVIVAL);
		SkullMinionEntity minion = new SkullMinionEntity(ExtraBotanyEntityType.SKULL_MINION, h.getLevel());
		minion.setPos(h.absolutePos(new BlockPos(2, 2, 2)).getCenter());
		minion.setShield(2);
		h.assertTrue(minion.getMaxHealth() == 24F, "The minion must have 24 health");
		for (int i = 0; i < 2; i++) {
			minion.invulnerableTime = 0;
			minion.hurt(player.damageSources().playerAttack(player), 8F);
		}
		h.assertTrue(minion.getHealth() == 24F && minion.getShield() == 0, "Two shields must stop two hits");
		minion.invulnerableTime = 0;
		minion.hurt(player.damageSources().playerAttack(player), 50F);
		h.assertTrue(minion.getHealth() == 14F, "One hit must deal at most 10");
		h.succeed();
	}

	// Original 1.12 EntitySkullLandmine: 50 blocks up and down; 6 damage; Blindness, Slowness IV and Wither II that milk cannot remove.
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void skullLandmineFollows112(GameTestHelper h) {
		Player player = h.makeMockPlayer(GameType.SURVIVAL);
		BlockPos minePos = h.absolutePos(new BlockPos(2, 2, 2));
		player.setPos(minePos.getX() + 0.5, minePos.getY() + 30, minePos.getZ() + 0.5);
		h.getLevel().addFreshEntity(player);
		try {
			var mine = new SkullLandMineEntity.Default(h.getLevel(), null);
			mine.setPos(minePos.getX() + 0.5, minePos.getY(), minePos.getZ() + 0.5);
			float health = player.getHealth();
			mine.explode();
			h.assertTrue(health - player.getHealth() == 6F, "A player 30 blocks above must take 6");
			var wither = player.getEffect(MobEffects.WITHER);
			h.assertTrue(wither != null && wither.getAmplifier() == 1 && !wither.getCures().contains(EffectCures.MILK), "Wither II must not be milk-curable");
			var slowness = player.getEffect(MobEffects.MOVEMENT_SLOWDOWN);
			h.assertTrue(slowness != null && slowness.getAmplifier() == 3 && player.hasEffect(MobEffects.BLINDNESS), "Slowness IV and Blindness missing");
		} finally {
			player.discard();
		}
		h.succeed();
	}

	// Original 1.12 StatHandler.hasStat(player, GAIA_DEFEAT): 0.7 for a player who has not beaten Guardian of Gaia III.
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void noviceTakesLessDamage(GameTestHelper h) {
		h.assertTrue(GaiaIII.noviceFactor(FakePlayerFactory.getMinecraft(h.getLevel())) == 0.7F, "A player without the advancement must take 0.7");
		h.succeed();
	}

	// Original 1.12 ItemCoreGod appearance 2 and loot_tables/void_herrscher.json: the rainbow core.
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void rainbowCoreExists(GameTestHelper h) {
		h.assertTrue(ExtraBotanyAPI.instance().getCOVVariants().containsKey(CoreOfTheVoidItem.VARIANT_RAINBOW), "The rainbow appearance is not registered");
		h.succeed();
	}

	// Original 1.12 EntityDarkPixie: a dark pixie with 2 health.
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void darkPixieExists(GameTestHelper h) {
		var pixie = new DarkPixieEntity(h.getLevel());
		h.assertTrue(pixie.getType() == ExtraBotanyEntityType.DARK_PIXIE && pixie.getMaxHealth() == 2F, "The dark pixie must have 2 health");
		h.succeed();
	}
}
