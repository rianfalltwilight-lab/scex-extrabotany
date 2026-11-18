package io.github.lounode.extrabotany.forge.gametest;

import io.github.lounode.extrabotany.common.block.ExtraBotanyBlocks;
import io.github.lounode.extrabotany.common.block.flower.ExtraGeneratingFlowerBlockEntity;
import io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks;
import io.github.lounode.extrabotany.common.block.flower.functional.AnnoyingFlowerBlockEntity;
import io.github.lounode.extrabotany.common.block.flower.functional.EnchanterBlockEntity;
import io.github.lounode.extrabotany.common.block.flower.functional.ManalinkBlockEntity;
import io.github.lounode.extrabotany.common.block.flower.functional.MirrowtuniaBlockEntity;
import io.github.lounode.extrabotany.common.block.flower.functional.NecrofleurBlockEntity;
import io.github.lounode.extrabotany.common.block.flower.generating.BellflowerBlockEntity;
import io.github.lounode.extrabotany.common.block.flower.generating.BloodEnchantressBlockEntity;
import io.github.lounode.extrabotany.common.block.flower.generating.EdelweissBlockEntity;
import io.github.lounode.extrabotany.common.block.flower.generating.OmnivioletBlockEntity;
import io.github.lounode.extrabotany.common.block.flower.generating.StonesiaBlockEntity;
import io.github.lounode.extrabotany.common.block.flower.generating.TinkleBlockEntity;
import io.github.lounode.extrabotany.common.brew.ExtraBotanyMobEffects;
import io.github.lounode.extrabotany.mixin.accessor.SpecialFlowerBlockEntityAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.botania.api.block.PetalApothecary;
import vazkii.botania.api.block_entity.BindableSpecialFlowerBlockEntity;
import vazkii.botania.api.block_entity.SpecialFlowerBlockEntity;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.internal_caps.ItemLifetime;
import vazkii.botania.common.item.BotaniaItems;

import java.util.ArrayList;
import java.util.List;

/**
 * 对照原版审计第 E1 批：花。两个原版都有的花按 1.16，只有 1.12 有的花按 1.12。每个测试注明原版出处。
 * Batch E1 of the parity audit with the original: the flowers. A flower that both originals have follows 1.16, a flower that only 1.12 has follows 1.12. Each test names its source in the original.
 */
@GameTestHolder("extrabotany")
@PrefixGameTestTemplate(false)
public final class E1ParityGameTests {

	private E1ParityGameTests() {}

	/**
	 * 在 {@code spreaderPos} 放一个魔力发射器，并把花绑定到它上面。
	 * Puts a mana spreader at {@code spreaderPos} and binds the flower to it.
	 */
	static void bindToSpreader(GameTestHelper h, BindableSpecialFlowerBlockEntity<?> flower, BlockPos spreaderPos) {
		h.setBlock(spreaderPos, BotaniaBlocks.MANA_SPREADER);
		flower.setBindingPos(h.absolutePos(spreaderPos));
		h.assertTrue(flower.isValidBinding(), "The flower is not bound to the spreader");
	}

	/** 一朵不在世界里的花：只有测试调用它的 tick。 / A flower that is not in the level: only the test calls its tick. */
	private static <T extends BlockEntity> T detached(GameTestHelper h, T tile) {
		tile.setLevel(h.getLevel());
		return tile;
	}

	/** 通过访问器调用 protected 的 tickFlower。 / Calls the protected tickFlower through the accessor. */
	private static void tick(SpecialFlowerBlockEntity flower, int ticks) {
		for (int i = 0; i < ticks; i++) {
			((SpecialFlowerBlockEntityAccessor) flower).extrabotany_invokeTickFlower();
		}
	}

	private static ItemEntity drop(GameTestHelper h, BlockPos rel, ItemStack stack) {
		BlockPos abs = h.absolutePos(rel);
		ItemEntity item = new ItemEntity(h.getLevel(), abs.getX() + 0.5, abs.getY() + 0.1, abs.getZ() + 0.5, stack);
		item.setDeltaMovement(0, 0, 0);
		item.setNoGravity(true);
		ItemLifetime.set(item, 1000);
		h.getLevel().addFreshEntity(item);
		return item;
	}

	private static <T extends Entity> T placeEntity(GameTestHelper h, T entity, BlockPos rel) {
		BlockPos abs = h.absolutePos(rel);
		entity.setPos(abs.getX() + 0.5, abs.getY(), abs.getZ() + 0.5);
		h.getLevel().addFreshEntity(entity);
		return entity;
	}

	// Original 1.16 SubTileTinkleFlower: (player position − chasingPos) × 10, × 1.2 with Speed, limited to 0..8; 30 mana per 10 counts; no redstone.
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void tinkleCountsTheCapeDistanceAndMakesThirty(GameTestHelper h) {
		Player player = h.makeMockPlayer(GameType.SURVIVAL);
		player.setPos(10, 0, 0);
		player.xCloak = 10;
		player.yCloak = 0;
		player.zCloak = 0;
		h.assertTrue(TinkleBlockEntity.getTimeAdd(player) == 0, "A still player added to the count");
		player.xCloak = 9.95;
		h.assertTrue(TinkleBlockEntity.getTimeAdd(player) == 0, "0.05 blocks must add (int) 0.5 = 0");
		player.xCloak = 9.55;
		h.assertTrue(TinkleBlockEntity.getTimeAdd(player) == 4, "0.45 blocks must add 4");
		player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100));
		h.assertTrue(TinkleBlockEntity.getTimeAdd(player) == 5, "Speed must multiply the distance by 1.2 (0.45 → 5)");
		player.xCloak = 5;
		h.assertTrue(TinkleBlockEntity.getTimeAdd(player) == 8, "The count must stop at 8 for one player");
		h.assertTrue(TinkleBlockEntity.PRODUCE_MANA == 30 && TinkleBlockEntity.MAX_MANA == 1000, "The Tinkle must make 30 mana with a maximum of 1000");
		h.succeed();
	}

	// Original 1.16 SubTileBellFlower: sky and y > 90; every 10 ticks (10 + 3 in rain) × y / 90; maximum 300.
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void bellflowerMakesManaByHeight(GameTestHelper h) {
		var level = h.getLevel();
		BlockPos base = h.absolutePos(BlockPos.ZERO);
		h.assertFalse(level.isRaining(), "This test needs clear weather");
		var high = detached(h, new BellflowerBlockEntity(new BlockPos(base.getX(), 180, base.getZ()), ExtrabotanyFlowerBlocks.bellflower.defaultBlockState()));
		var low = detached(h, new BellflowerBlockEntity(new BlockPos(base.getX(), 90, base.getZ()), ExtrabotanyFlowerBlocks.bellflower.defaultBlockState()));
		var just = detached(h, new BellflowerBlockEntity(new BlockPos(base.getX(), 91, base.getZ()), ExtrabotanyFlowerBlocks.bellflower.defaultBlockState()));
		h.assertTrue(level.canSeeSky(high.getBlockPos()), "This test needs the sky above Y 180");
		h.assertTrue(high.getGenerateMana() == 20, "At Y 180 the Bellflower must make 10 × 180 / 90 = 20, but it makes " + high.getGenerateMana());
		h.assertTrue(low.getGenerateMana() == 0, "At Y 90 the Bellflower must make nothing");
		h.assertTrue(just.getGenerateMana() == 10, "At Y 91 the Bellflower must make 10 × 91 / 90 = 10");
		tick(high, 10);
		h.assertTrue(high.getMana() == 20, "The Bellflower must make its mana once in 10 ticks, but it has " + high.getMana());
		h.assertTrue(high.getMaxMana() == 300, "The Bellflower must hold 300");
		h.succeed();
	}

	// Original 1.12 SubTileStonesia: maximum 800, cooldown 40, stopped by redstone.
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void stonesiaStopsWithRedstone(GameTestHelper h) {
		BlockPos rel = new BlockPos(2, 2, 2);
		h.setBlock(rel.below(), Blocks.DIRT);
		h.setBlock(rel, ExtrabotanyFlowerBlocks.stonesia);
		h.setBlock(rel.east(), Blocks.STONE);
		h.setBlock(rel.north(), Blocks.REDSTONE_BLOCK);
		var flower = (StonesiaBlockEntity) h.getBlockEntity(rel);
		h.assertTrue(flower.isPowered(), "The Stonesia did not see the redstone signal");
		tick(flower, 5);
		h.assertTrue(h.getBlockState(rel.east()).is(Blocks.STONE), "A powered Stonesia ate a block");
		h.assertTrue(flower.getMaxMana() == 800 && StonesiaBlockEntity.COOLDOWN == 40, "The Stonesia must hold 800 and rest 40 ticks");
		h.succeed();
	}

	// Original 1.16 SubTileEdelweiss: only with a bound spreader; 1600 for each snow golem; 5 ticks of rest. Maximum from 1.12 (25000).
	@GameTest(template = "projectile_arena", timeoutTicks = 20)
	public static void edelweissNeedsASpreaderAndGives1600(GameTestHelper h) {
		BlockPos rel = new BlockPos(2, 2, 2);
		var flower = detached(h, new EdelweissBlockEntity(h.absolutePos(rel), ExtrabotanyFlowerBlocks.edelweiss.defaultBlockState()));
		var golem = placeEntity(h, EntityType.SNOW_GOLEM.create(h.getLevel()), rel);
		golem.setNoAi(true);
		tick(flower, 1);
		h.assertTrue(!golem.isRemoved() && flower.getMana() == 0, "An unbound Edelweiss ate a snow golem");
		bindToSpreader(h, flower, new BlockPos(5, 2, 2));
		tick(flower, 1);
		h.assertTrue(golem.isRemoved(), "A bound Edelweiss did not eat the snow golem");
		h.assertTrue(flower.getMana() == 1600, "One snow golem must give 1600, but the flower has " + flower.getMana());
		h.assertTrue(flower.getCooldown() == 5, "The Edelweiss must rest 5 ticks");
		h.assertTrue(flower.getMaxMana() == 25000, "The Edelweiss must hold 25000");
		h.succeed();
	}

	// Original 1.16 SubTileOmniViolet: only eats a book with a bound spreader; a book burns 50 ticks.
	@GameTest(template = "projectile_arena", timeoutTicks = 20)
	public static void omnivioletNeedsASpreader(GameTestHelper h) {
		BlockPos rel = new BlockPos(2, 2, 2);
		var flower = detached(h, new OmnivioletBlockEntity(h.absolutePos(rel), ExtrabotanyFlowerBlocks.omniviolet.defaultBlockState()));
		var book = drop(h, rel, new ItemStack(Items.BOOK));
		tick(flower, 1);
		h.assertTrue(!book.getItem().isEmpty() && flower.getBurnTime() == 0, "An unbound Omniviolet ate a book");
		bindToSpreader(h, flower, new BlockPos(5, 2, 2));
		tick(flower, 6);
		// 吃掉的物品堆叠变空；实体在它自己的下一 tick 才移除。 / An eaten item has an empty stack; the entity goes away on its own next tick.
		h.assertTrue(book.getItem().isEmpty() && flower.getBurnTime() == 45, "A bound Omniviolet did not burn the book for 50 ticks");
		h.succeed();
	}

	// Original 1.16 SubTileBloodyEnchantress: only with a bound spreader; 22 × 12 = 264 for a fresh creature, 3 magic damage, 20 ticks of rest; no mana while resting.
	@GameTest(template = "projectile_arena", timeoutTicks = 20)
	public static void bloodEnchantressGives264OnceAndRests(GameTestHelper h) {
		BlockPos rel = new BlockPos(2, 2, 2);
		var flower = detached(h, new BloodEnchantressBlockEntity(h.absolutePos(rel), ExtrabotanyFlowerBlocks.bloodEnchantress.defaultBlockState()));
		var pig = placeEntity(h, EntityType.PIG.create(h.getLevel()), rel);
		pig.setNoAi(true);
		float health = pig.getHealth();
		tick(flower, 1);
		h.assertTrue(flower.getMana() == 0 && pig.getHealth() == health, "An unbound Blood Enchantress hurt a creature");
		bindToSpreader(h, flower, new BlockPos(5, 2, 2));
		tick(flower, 1);
		h.assertTrue(flower.getMana() == 264, "A fresh creature must give 264, but the flower has " + flower.getMana());
		h.assertTrue(Math.abs(health - pig.getHealth() - 3F) < 0.01F, "The flower must deal 3 damage");
		h.assertTrue(flower.getBurnTime() == 20, "The flower must rest 20 ticks after one creature");
		h.assertTrue(BloodEnchantressBlockEntity.getManaFor(5, 10) == (int) (264F * (1F - 0.2F - 0.2F)), "The mana formula is not the one of 1.16");
		h.succeed();
	}

	// Original 1.16 SubTileAnnoyingFlower: water in a Petal Apothecary on the same layer; cooked chicken; 300 mana; 900 ticks, 360 when fed. The count goes down for each catch as in 1.12, with no limit of 12.
	@GameTest(template = "projectile_arena", timeoutTicks = 20)
	public static void annoyingFlowerUsesApothecaryWaterAndCookedChicken(GameTestHelper h) {
		BlockPos rel = new BlockPos(3, 2, 3);
		var flower = detached(h, new AnnoyingFlowerBlockEntity(h.absolutePos(rel), ExtrabotanyFlowerBlocks.annoyingflower.defaultBlockState()));
		h.setBlock(rel.above().east(2), BotaniaBlocks.PETAL_APOTHECARY);
		((PetalApothecary) h.getBlockEntity(rel.above().east(2))).setFluid(PetalApothecary.State.WATER);
		h.assertFalse(flower.hasWater(), "An apothecary on the layer above counted as water");
		h.setBlock(rel.west(3), BotaniaBlocks.PETAL_APOTHECARY);
		h.assertFalse(flower.hasWater(), "An empty apothecary counted as water");
		((PetalApothecary) h.getBlockEntity(rel.west(3))).setFluid(PetalApothecary.State.WATER);
		h.assertTrue(flower.hasWater(), "Water in an apothecary on the same layer did not count");

		for (int i = 0; i < 5; i++) {
			drop(h, rel, new ItemStack(Items.COOKED_CHICKEN));
			h.assertTrue(flower.tryEatChicken(), "The flower did not eat cooked chicken");
		}
		h.assertTrue(flower.getBoostLeft() == 15, "Five meals must give 15 treasure catches with no limit, but it has " + flower.getBoostLeft());
		h.assertTrue(flower.getFishingCost() == 300 && flower.getCooldownAfterWork() == 900
				&& (int) (900 * flower.getFoodBoostCooldownMultiplier()) == 360, "The flower must use 300 mana each 900 ticks, 360 when fed");
		h.succeed();
	}

	// Original 1.12 SubTileNecrofleur: below half health, 10 magic damage and Witch Curse V for 300 ticks, 200 mana.
	@GameTest(template = "projectile_arena", timeoutTicks = 20)
	public static void necrofleurGivesWitchCurse(GameTestHelper h) {
		BlockPos rel = new BlockPos(2, 2, 2);
		var flower = detached(h, new NecrofleurBlockEntity(h.absolutePos(rel), ExtrabotanyFlowerBlocks.necrofleur.defaultBlockState()));
		flower.addMana(200);
		var zombie = placeEntity(h, EntityType.IRON_GOLEM.create(h.getLevel()), rel.east());
		zombie.setNoAi(true);
		zombie.setHealth(zombie.getMaxHealth() * 0.4F);
		tick(flower, 20);
		var curse = zombie.getEffect(ExtraBotanyMobEffects.WITCH_CURSE);
		h.assertTrue(curse != null && curse.getAmplifier() == 4 && curse.getDuration() > 280, "The Necrofleur must give Witch Curse V for 300 ticks");
		h.assertFalse(zombie.hasEffect(ExtraBotanyMobEffects.HEAL_REVERSE), "The Necrofleur still gives Heal Reverse");
		h.assertTrue(flower.getMana() == 0, "The Necrofleur must use 200 mana");
		h.succeed();
	}

	// Original 1.12 SubTileMirrortunia: each 50 ticks, 90 mana for each player.
	@GameTest(template = "projectile_arena", timeoutTicks = 20)
	public static void mirrowtuniaCosts90ForEachPlayer(GameTestHelper h) {
		BlockPos rel = new BlockPos(2, 2, 2);
		var flower = detached(h, new MirrowtuniaBlockEntity(h.absolutePos(rel), ExtrabotanyFlowerBlocks.mirrowtunia.defaultBlockState()));
		flower.addMana(1000);
		var player = placeEntity(h, h.makeMockPlayer(GameType.SURVIVAL), rel.east());
		try {
			tick(flower, 50);
			h.assertTrue(player.hasEffect(ExtraBotanyMobEffects.THIRROR), "The player did not get the effect");
			h.assertTrue(flower.getMana() == 910, "One player must cost 90, but the flower has " + flower.getMana());
		} finally {
			player.discard();
		}
		h.succeed();
	}

	// Original 1.12 SubTileManalinkium: the structure does not examine the middle block; maximum 10000.
	@GameTest(template = "projectile_arena", timeoutTicks = 20)
	public static void manalinkAcceptsAnyBlockUnderIt(GameTestHelper h) {
		BlockPos flowerRel = new BlockPos(3, 3, 3);
		BlockPos below = flowerRel.below();
		String[] rows = ManalinkBlockEntity.PATTERN[0];
		for (int z = 0; z < 5; z++) {
			for (int x = 0; x < 5; x++) {
				Block block = switch (rows[z].charAt(x)) {
					case 'Q' -> Blocks.QUARTZ_BLOCK;
					case 'L' -> Blocks.LAPIS_BLOCK;
					case 'O' -> Blocks.OBSIDIAN;
					default -> Blocks.STONE;
				};
				h.setBlock(below.offset(x - 2, 0, z - 2), block);
			}
		}
		BlockPos abs = h.absolutePos(below);
		h.assertTrue(ManalinkBlockEntity.PATTERN_BUILTIN.matches(h.getLevel(), abs.offset(2, 0, 2), Direction.DOWN, Direction.SOUTH) != null,
				"The structure must accept stone under the flower");
		h.setBlock(below, Blocks.AIR);
		h.assertTrue(ManalinkBlockEntity.PATTERN_BUILTIN.matches(h.getLevel(), abs.offset(2, 0, 2), Direction.DOWN, Direction.SOUTH) != null,
				"The structure must accept air under the flower (a floating flower)");
		var flower = detached(h, new ManalinkBlockEntity(h.absolutePos(flowerRel), ExtrabotanyFlowerBlocks.manalink.defaultBlockState()));
		h.assertTrue(flower.getMaxMana() == 10000, "The Manalink must hold 10000");
		h.succeed();
	}

	// Original 1.12 SubTileEnchantedOrchid: a Black Lotus, then 200000 mana at 400 a tick, then a grass block on the layer below within 4.
	@GameTest(template = "projectile_arena", timeoutTicks = 20)
	public static void enchanterNeedsABlackLotus(GameTestHelper h) {
		BlockPos rel = new BlockPos(5, 3, 5);
		BlockPos grass = rel.below().offset(4, 0, -4);
		h.setBlock(grass, Blocks.GRASS_BLOCK);
		h.setBlock(rel.offset(3, 0, 0), Blocks.GRASS_BLOCK);
		var empty = detached(h, new EnchanterBlockEntity(h.absolutePos(rel.west(4)), ExtrabotanyFlowerBlocks.enchanter.defaultBlockState()));
		empty.addMana(5000);
		tick(empty, 1);
		h.assertTrue(empty.getConsumedMana() == 0 && !empty.hasLotus(), "The Enchanter used mana with no Black Lotus");

		var flower = detached(h, new EnchanterBlockEntity(h.absolutePos(rel), ExtrabotanyFlowerBlocks.enchanter.defaultBlockState()));
		flower.addMana(5000);
		var lotus = drop(h, rel, new ItemStack(BotaniaItems.BLACK_LOTUS));
		tick(flower, 6);
		h.assertTrue(lotus.getItem().isEmpty() && flower.hasLotus(), "The Enchanter did not take the Black Lotus");
		h.assertTrue(flower.getConsumedMana() == 400 * 6, "The Enchanter must use 400 on each tick, but it used " + flower.getConsumedMana());
		h.assertTrue(flower.getTransformCost() == 200000 && flower.getMaxMana() == 10000, "The Enchanter must need 200000 and hold 10000");
		flower.setConsumedMana(flower.getTransformCost());
		tick(flower, 20);
		h.assertTrue(h.getBlockState(grass).is(ExtraBotanyBlocks.enchantedSoil), "The grass on the layer below did not become Enchanted Soil");
		h.assertTrue(h.getBlockState(rel.offset(3, 0, 0)).is(Blocks.GRASS_BLOCK), "The Enchanter changed grass on its own layer");
		h.assertTrue(!flower.hasLotus() && flower.getConsumedMana() == 0, "The Enchanter did not use up the Black Lotus");
		h.succeed();
	}

	// Botania 1.12 SubTileGenerating.isOvergrowthAffected: a passive flower is not made faster on the soil.
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void enchantedSoilDoesNotSpeedUpPassiveFlowers(GameTestHelper h) {
		BlockPos soil = new BlockPos(1, 1, 1);
		h.setBlock(soil, ExtraBotanyBlocks.enchantedSoil);
		h.setBlock(soil.above(), ExtrabotanyFlowerBlocks.sunshineLily);
		SpecialFlowerBlockEntity flower = h.getBlockEntity(soil.above());
		long before = ticksExisted(flower);
		SpecialFlowerBlockEntity.commonTick(h.getLevel(), flower.getBlockPos(), flower.getBlockState(), flower);
		h.assertTrue(ticksExisted(flower) - before == 1, "A passive flower ran two ticks on Enchanted Soil");
		h.succeed();
	}

	private static long ticksExisted(SpecialFlowerBlockEntity flower) {
		try {
			var field = ExtraGeneratingFlowerBlockEntity.class.getDeclaredField("ticksExisted");
			field.setAccessible(true);
			return field.getLong(flower);
		} catch (ReflectiveOperationException e) {
			throw new AssertionError(e);
		}
	}
}
