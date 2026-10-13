package io.github.lounode.extrabotany.forge.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.block.block_entity.flower.generating.HydroangeasBlockEntity;

import io.github.lounode.extrabotany.api.level.Wind;
import io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks;
import io.github.lounode.extrabotany.common.block.flower.functional.SerenitianBlockEntity;
import io.github.lounode.extrabotany.common.block.flower.functional.WoodieniaBlockEntity;
import io.github.lounode.extrabotany.common.block.flower.generating.BellflowerBlockEntity;
import io.github.lounode.extrabotany.common.block.flower.generating.ResoncundBlockEntity;
import io.github.lounode.extrabotany.common.block.flower.generating.StonesiaBlockEntity;
import io.github.lounode.extrabotany.common.block.flower.generating.SunshineLilyBlockEntity;
import io.github.lounode.extrabotany.common.brew.ExtraBotanyMobEffects;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 花的回归测试：迷之炖菜的效果、红石属性、永寂龙胆、风铃草的风力、回音花的监听、伐木花的扫描和石中姬的进食。
 * Regression tests for the flowers: the effects of the suspicious stew, the redstone property, the Serenitian, the wind level of the Bellflower, the listener of the Resoncund, the scan of the Woodienia, and the eating of the Stonesia.
 */
@GameTestHolder("extrabotany")
@PrefixGameTestTemplate(false)
public final class FlowerGameTests {
	private static final BlockPos CENTER = new BlockPos(2, 2, 2);
	private static final String TAG_DECAY = "passiveDecayTicks";
	private static final BlockPos WOODIENIA_POS = new BlockPos(32, 2, 32);

	private FlowerGameTests() {}

	/**
	 * 在草方块上放一朵花，并返回它的方块实体。
	 * Puts a flower on a grass block and returns its block entity.
	 */
	private static <T extends BlockEntity> T plant(GameTestHelper helper, BlockPos pos, BlockState state, Class<T> type) {
		helper.setBlock(pos.below(), Blocks.GRASS_BLOCK);
		helper.setBlock(pos, state);
		return type.cast(helper.getLevel().getBlockEntity(helper.absolutePos(pos)));
	}

	/**
	 * 花的炖菜效果必须使用注册表里的 Holder。生物用 Holder 作为键保存效果，所以用代理 Holder 加上的效果是找不到的。
	 * The stew effect of a flower must use the holder of the registry. A creature keeps its effects with the holder as the key, thus an effect that was added with a proxy holder cannot be found.
	 */
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void stewEffectsUseRegisteredHolders(GameTestHelper helper) {
		Map<Block, Holder<MobEffect>> expected = Map.of(
				ExtrabotanyFlowerBlocks.edelweiss, ExtraBotanyMobEffects.WARM,
				ExtrabotanyFlowerBlocks.serenitian, ExtraBotanyMobEffects.IMMOBILIZE,
				ExtrabotanyFlowerBlocks.mirrowtunia, ExtraBotanyMobEffects.THIRROR,
				ExtrabotanyFlowerBlocks.necrofleur, ExtraBotanyMobEffects.HEAL_REVERSE,
				ExtrabotanyFlowerBlocks.necrofleurChibi, ExtraBotanyMobEffects.HEAL_REVERSE);
		for (Map.Entry<Block, Holder<MobEffect>> entry : expected.entrySet()) {
			SuspiciousStewEffects effects = ((FlowerBlock) entry.getKey()).getSuspiciousEffects();
			helper.assertTrue(effects.effects().size() == 1 && effects.effects().getFirst().effect() == entry.getValue(),
					"The stew effect of " + entry.getKey() + " does not use the registered holder");
		}

		Husk husk = helper.spawnWithNoFreeWill(EntityType.HUSK, CENTER);
		SuspiciousStewEffects stew = ((FlowerBlock) ExtrabotanyFlowerBlocks.necrofleur).getSuspiciousEffects();
		husk.addEffect(stew.effects().getFirst().createEffectInstance());
		helper.assertTrue(husk.hasEffect(ExtraBotanyMobEffects.HEAL_REVERSE), "The effect of the stew cannot be found on the creature");
		husk.discard();
		helper.succeed();
	}

	/**
	 * 只有会对红石做出反应的花才有"充能"属性。这和植物魔法的做法相同。
	 * Only the flowers that react to redstone have the "powered" property. This agrees with the practice of Botania.
	 */
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void onlyRedstoneFlowersHaveThePoweredProperty(GameTestHelper helper) {
		List<Block> plain = List.of(
				ExtrabotanyFlowerBlocks.reikarlily, ExtrabotanyFlowerBlocks.reikarlilyFloating,
				ExtrabotanyFlowerBlocks.bellflower, ExtrabotanyFlowerBlocks.bellflowerFloating,
				ExtrabotanyFlowerBlocks.stonesia, ExtrabotanyFlowerBlocks.stonesiaFloating,
				ExtrabotanyFlowerBlocks.edelweiss, ExtrabotanyFlowerBlocks.edelweissFloating,
				ExtrabotanyFlowerBlocks.omniviolet, ExtrabotanyFlowerBlocks.omnivioletFloating,
				ExtrabotanyFlowerBlocks.bloodEnchantress, ExtrabotanyFlowerBlocks.bloodEnchantressFloating,
				ExtrabotanyFlowerBlocks.resoncund, ExtrabotanyFlowerBlocks.sunshineLily,
				ExtrabotanyFlowerBlocks.moonlightLily, ExtrabotanyFlowerBlocks.twinstar);
		for (Block block : plain) {
			helper.assertFalse(block.defaultBlockState().hasProperty(BlockStateProperties.POWERED),
					block + " does not react to redstone, but it has the powered property");
		}
		List<Block> powered = List.of(
				ExtrabotanyFlowerBlocks.tinkle, ExtrabotanyFlowerBlocks.tinkleFloating,
				ExtrabotanyFlowerBlocks.serenitian, ExtrabotanyFlowerBlocks.serenitianFloating,
				ExtrabotanyFlowerBlocks.annoyingflower, ExtrabotanyFlowerBlocks.tradeOrchid,
				ExtrabotanyFlowerBlocks.woodienia, ExtrabotanyFlowerBlocks.mirrowtunia,
				ExtrabotanyFlowerBlocks.necrofleur, ExtrabotanyFlowerBlocks.necrofleurChibi,
				ExtrabotanyFlowerBlocks.manalink, ExtrabotanyFlowerBlocks.enchanter,
				ExtrabotanyFlowerBlocks.stardustLotus);
		for (Block block : powered) {
			helper.assertTrue(block.defaultBlockState().hasProperty(BlockStateProperties.POWERED),
					block + " reacts to redstone, but it has no powered property");
		}
		helper.succeed();
	}

	/**
	 * 永寂龙胆把本模组的被动花和植物魔法的水绣球的枯萎计数器清零。红石信号让它停止工作。
	 * The Serenitian sets the decay counter of the passive flowers of this mod and of the Hydroangeas of Botania to zero. A redstone signal stops it.
	 */
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void serenitianStopsDecay(GameTestHelper helper) {
		HolderLookup.Provider registries = helper.getLevel().registryAccess();
		SunshineLilyBlockEntity lily = plant(helper, CENTER.east(), ExtrabotanyFlowerBlocks.sunshineLily.defaultBlockState(), SunshineLilyBlockEntity.class);
		HydroangeasBlockEntity hydroangeas = plant(helper, CENTER.west(), BotaniaBlocks.HYDROANGEAS.defaultBlockState(), HydroangeasBlockEntity.class);
		CompoundTag old = hydroangeas.saveCustomOnly(registries);
		old.putInt(TAG_DECAY, 500);
		hydroangeas.loadCustomOnly(old, registries);
		lily.setPassiveDecayTicks(500);

		// 充能的永寂龙胆不工作。 / A powered Serenitian does no work.
		SerenitianBlockEntity powered = plant(helper, CENTER,
				ExtrabotanyFlowerBlocks.serenitian.defaultBlockState().setValue(BlockStateProperties.POWERED, true), SerenitianBlockEntity.class);
		for (int i = 0; i < 20; i++) {
			powered.tickFlower();
		}
		helper.assertTrue(lily.getPassiveDecayTicks() == 500, "A powered Serenitian changed the decay counter");

		SerenitianBlockEntity serenitian = plant(helper, CENTER, ExtrabotanyFlowerBlocks.serenitian.defaultBlockState(), SerenitianBlockEntity.class);
		for (int i = 0; i < 20; i++) {
			serenitian.tickFlower();
		}
		helper.assertTrue(lily.getPassiveDecayTicks() == 0, "The Serenitian did not set the decay counter of the Sunshine Lily to zero");
		helper.assertTrue(hydroangeas.saveCustomOnly(registries).getInt(TAG_DECAY) == 0,
				"The Serenitian did not set the decay counter of the Hydroangeas to zero");
		helper.succeed();
	}

	/**
	 * 风铃草保存服务端的风力，并把它同步到客户端。客户端的法杖界面显示这个数值。
	 * The Bellflower keeps the wind level of the server and synchronizes it to the client. The wand HUD of the client shows this value.
	 */
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void bellflowerSynchronizesTheWindLevel(GameTestHelper helper) {
		HolderLookup.Provider registries = helper.getLevel().registryAccess();
		BellflowerBlockEntity flower = plant(helper, CENTER, ExtrabotanyFlowerBlocks.bellflower.defaultBlockState(), BellflowerBlockEntity.class);
		BlockPos pos = flower.getEffectivePos();
		double wind = Wind.instance().getWindLevel(helper.getLevel(), new Vec3(pos.getX(), pos.getY(), pos.getZ()));
		helper.assertTrue(wind > 0, "The server has no wind");

		flower.getGenerateMana();
		helper.assertTrue(Math.abs(flower.getWindLevel() - wind) < 0.005, "The Bellflower did not keep the wind level of the server");

		// 客户端用 loadCustomOnly 读取更新数据包。 / The client reads the update packet with loadCustomOnly.
		BellflowerBlockEntity client = new BellflowerBlockEntity(flower.getBlockPos(), flower.getBlockState());
		client.loadCustomOnly(flower.getUpdateTag(registries), registries);
		helper.assertTrue(Math.abs(client.getWindLevel() - wind) < 0.005, "The update packet does not have the wind level");
		helper.succeed();
	}

	/**
	 * 回音花只在服务端的世界里监听声音，并且在被移除后停止监听。
	 * The Resoncund only listens to sounds in the level of the server, and it stops when it is removed.
	 */
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void resoncundListensOnlyInAServerLevel(GameTestHelper helper) {
		ResoncundBlockEntity flower = new ResoncundBlockEntity(helper.absolutePos(CENTER), ExtrabotanyFlowerBlocks.resoncund.defaultBlockState());
		helper.assertFalse(ResoncundBlockEntity.listeners().contains(flower), "A Resoncund without a level listens to sounds");
		flower.setLevel(helper.getLevel());
		helper.assertTrue(ResoncundBlockEntity.listeners().contains(flower), "A Resoncund in a server level does not listen to sounds");
		flower.setRemoved();
		helper.assertFalse(ResoncundBlockEntity.listeners().contains(flower), "A removed Resoncund continues to listen to sounds");
		helper.succeed();
	}

	/**
	 * 伐木花只选范围里的原木，并且每个原木被选中的概率相同。范围的边上、别的区块里和别的区块段里的原木也能选到。
	 * The Woodienia only selects logs in its range, and each log has the same chance. Logs on the edge of the range, in other chunks and in other chunk sections can also be selected.
	 */
	@GameTest(template = "projectile_arena", timeoutTicks = 20)
	public static void woodieniaSelectsOnlyLogsInRange(GameTestHelper helper) {
		// 这朵花没有魔力，所以它自己不会破坏原木。
		// This flower has no mana, thus it does not break logs by itself.
		WoodieniaBlockEntity flower = plant(helper, WOODIENIA_POS, ExtrabotanyFlowerBlocks.woodienia.defaultBlockState(), WoodieniaBlockEntity.class);
		helper.assertTrue(flower.getValidBreakPos() == null, "The Woodienia selected a block in an area with no log");

		// 默认范围是水平 8 格、向上 16 格。 / The default range is 8 blocks horizontally and 16 blocks up.
		helper.setBlock(WOODIENIA_POS.east(9), Blocks.OAK_LOG);
		helper.setBlock(WOODIENIA_POS.above(17), Blocks.OAK_LOG);
		helper.assertTrue(flower.getValidBreakPos() == null, "The Woodienia selected a log that is out of its range");

		List<BlockPos> inRange = List.of(WOODIENIA_POS.east(8), WOODIENIA_POS.offset(-8, 0, -8), WOODIENIA_POS.above(16));
		helper.setBlock(inRange.get(0), Blocks.OAK_LOG);
		helper.setBlock(inRange.get(1), Blocks.STRIPPED_BIRCH_LOG);
		helper.setBlock(inRange.get(2), Blocks.CRIMSON_STEM);
		Map<BlockPos, Integer> counts = new HashMap<>();
		for (int i = 0; i < 300; i++) {
			BlockPos chosen = flower.getValidBreakPos();
			helper.assertTrue(chosen != null, "The Woodienia did not find the logs in its range");
			counts.merge(chosen, 1, Integer::sum);
		}
		helper.assertTrue(counts.size() == inRange.size(), "The Woodienia selected a block that is not one of the three logs: " + counts.keySet());
		for (BlockPos pos : inRange) {
			// 期望值是 100 次。少于 50 次的概率小于十亿分之一。
			// The expected value is 100 times. The chance of less than 50 times is smaller than one in a billion.
			int count = counts.getOrDefault(helper.absolutePos(pos), 0);
			helper.assertTrue(count >= 50, "The log at " + pos + " was selected " + count + " times out of 300; each log must have the same chance");
		}
		helper.succeed();
	}

	/**
	 * 有魔力的伐木花破坏范围里的原木，每破坏一个消耗一次魔力。范围里出现新的原木之后，空闲的花在 20 tick 之内发现它。充能的花不工作。
	 * A Woodienia with mana breaks the logs in its range and uses mana one time for each log. After a new log appears in the range, an idle flower finds it in 20 ticks or less. A powered flower does no work.
	 */
	@GameTest(template = "projectile_arena", timeoutTicks = 200)
	public static void woodieniaBreaksLogsAndUsesMana(GameTestHelper helper) {
		// 两朵花相距 24 格，所以它们的范围不重叠。
		// The two flowers are 24 blocks apart, thus their ranges do not overlap.
		BlockPos poweredPos = WOODIENIA_POS.offset(-24, 0, -24);
		WoodieniaBlockEntity flower = plant(helper, WOODIENIA_POS, ExtrabotanyFlowerBlocks.woodienia.defaultBlockState(), WoodieniaBlockEntity.class);
		WoodieniaBlockEntity powered = plant(helper, poweredPos, ExtrabotanyFlowerBlocks.woodienia.defaultBlockState(), WoodieniaBlockEntity.class);
		helper.setBlock(poweredPos.north(), Blocks.REDSTONE_BLOCK);
		flower.addMana(flower.getMaxMana());
		powered.addMana(powered.getMaxMana());
		int startMana = flower.getMana();
		int cost = flower.getManaPerUse();
		helper.assertTrue(startMana >= 2 * cost && cost > 0, "The test needs a Woodienia that has mana for two logs");

		BlockPos first = WOODIENIA_POS.east(8);
		BlockPos second = WOODIENIA_POS.above(16);
		BlockPos outOfRange = WOODIENIA_POS.east(9);
		BlockPos nearPowered = poweredPos.east(2);

		// 先让花空闲 30 tick，然后才放原木。这样能测到空闲的花会再次扫描。
		// The flower is idle for 30 ticks first, and only then the logs are put down. This shows that an idle flower scans again.
		helper.runAfterDelay(30, () -> {
			helper.assertTrue(flower.getMana() == startMana, "The Woodienia used mana when there was no log");
			for (BlockPos pos : List.of(first, second, outOfRange, nearPowered)) {
				helper.setBlock(pos, Blocks.OAK_LOG);
			}
		});
		helper.runAfterDelay(30 + 22, () -> {
			boolean firstGone = helper.getBlockState(first).isAir();
			boolean secondGone = helper.getBlockState(second).isAir();
			helper.assertTrue(firstGone || secondGone, "An idle Woodienia did not find a new log in 20 ticks");
		});
		helper.runAfterDelay(30 + 22 + 30, () -> {
			helper.assertTrue(helper.getBlockState(first).isAir() && helper.getBlockState(second).isAir(),
					"The Woodienia did not break the two logs in its range");
			helper.assertTrue(flower.getMana() == startMana - 2 * cost,
					"The Woodienia must use " + (2 * cost) + " mana for two logs, but it used " + (startMana - flower.getMana()));
			helper.assertTrue(helper.getBlockState(outOfRange).is(Blocks.OAK_LOG), "The Woodienia broke a log that is out of its range");

			helper.assertTrue(powered.getBlockState().getValue(BlockStateProperties.POWERED), "The Woodienia next to a redstone block is not powered");
			helper.assertTrue(helper.getBlockState(nearPowered).is(Blocks.OAK_LOG), "A powered Woodienia broke a log");
			helper.assertTrue(powered.getMana() == powered.getMaxMana(), "A powered Woodienia used mana");

			AABB area = new AABB(helper.absolutePos(WOODIENIA_POS)).inflate(32);
			helper.getLevel().getEntitiesOfClass(ItemEntity.class, area).forEach(ItemEntity::discard);
			helper.succeed();
		});
	}

	/**
	 * 石中姬吃掉旁边符合配方的方块，得到配方里的魔力。不符合配方的方块不受影响。它只在自己周围的 17 个位置里选，不选自己的位置。
	 * The Stonesia eats an adjacent block that agrees with a recipe, and gets the mana of the recipe. Blocks that agree with no recipe are not changed. It only selects from the 17 positions around it, and not its own position.
	 */
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void stonesiaEatsOnlyRecipeBlocksAroundIt(GameTestHelper helper) {
		StonesiaBlockEntity flower = plant(helper, CENTER, ExtrabotanyFlowerBlocks.stonesia.defaultBlockState(), StonesiaBlockEntity.class);
		helper.assertTrue(flower.getManaFormBlock(Blocks.STONE.defaultBlockState()) == 20, "Stone must give 20 mana");
		helper.assertTrue(flower.getManaFormBlock(Blocks.DIAMOND_ORE.defaultBlockState()) == 750, "Diamond ore must give 750 mana");
		helper.assertTrue(flower.getManaFormBlock(Blocks.AIR.defaultBlockState()) == 0, "Air must give no mana");
		helper.assertTrue(flower.getManaFormBlock(Blocks.OAK_PLANKS.defaultBlockState()) == 0, "Oak planks must give no mana");

		BlockPos center = flower.getEffectivePos();
		Set<BlockPos> seen = new HashSet<>();
		for (int i = 0; i < 2000; i++) {
			BlockPos pos = flower.getRadomBlockPos();
			boolean inBox = Math.abs(pos.getX() - center.getX()) <= 1 && Math.abs(pos.getZ() - center.getZ()) <= 1
					&& pos.getY() >= center.getY() && pos.getY() <= center.getY() + 1;
			helper.assertTrue(inBox && !pos.equals(center), "The Stonesia selected the position " + pos + ", which is not one of the 17 positions around " + center);
			seen.add(pos);
		}
		helper.assertTrue(seen.size() == 17, "The Stonesia must select from 17 positions, but it selected from " + seen.size());

		helper.setBlock(CENTER.east(), Blocks.STONE);
		helper.setBlock(CENTER.west(), Blocks.OAK_PLANKS);
		for (int i = 0; i < 2000 && !helper.getBlockState(CENTER.east()).isAir(); i++) {
			flower.tickFlower();
		}
		helper.assertTrue(helper.getBlockState(CENTER.east()).isAir(), "The Stonesia did not eat the stone next to it");
		helper.assertTrue(flower.getMana() == 20, "The Stonesia must get 20 mana from one stone, but it has " + flower.getMana());
		helper.assertTrue(flower.getCooldown() == flower.getAfterWorkCooldown(), "The Stonesia must start its cooldown after it eats a block");
		helper.assertTrue(helper.getBlockState(CENTER.west()).is(Blocks.OAK_PLANKS), "The Stonesia ate a block that has no recipe");
		helper.assertTrue(helper.getBlockState(CENTER).is(ExtrabotanyFlowerBlocks.stonesia), "The Stonesia ate itself");
		helper.succeed();
	}
}