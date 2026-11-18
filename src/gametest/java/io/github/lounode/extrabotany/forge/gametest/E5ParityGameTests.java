package io.github.lounode.extrabotany.forge.gametest;

import io.github.lounode.extrabotany.common.crafting.PedestalsRecipe;
import io.github.lounode.extrabotany.common.entity.ExtraBotanyEntityType;
import io.github.lounode.extrabotany.common.handler.AllAdvancementsHandler;
import io.github.lounode.extrabotany.common.handler.OriginalLootHandler;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.lib.ExtraBotanyTags;
import io.github.lounode.extrabotany.common.lib.LibAdvancementNames;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.botania.api.recipe.ElvenTradeRecipe;
import vazkii.botania.api.recipe.ManaInfusionRecipe;
import vazkii.botania.api.recipe.RunicAltarRecipe;
import vazkii.botania.common.item.BotaniaItems;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 对照原版审计第 E5 批：配方、战利品和奖励袋。两个版本都有的东西按 1.16，只有 1.12 有的按 1.12。
 * Batch E5 of the parity audit with the original: recipes, loot and bags. What both versions have follows 1.16; what only 1.12 has follows 1.12.
 */
@GameTestHolder("extrabotany")
@PrefixGameTestTemplate(false)
public final class E5ParityGameTests {

	private E5ParityGameTests() {}

	private static Recipe<?> recipe(GameTestHelper h, ResourceLocation id) {
		var holder = h.getLevel().getRecipeManager().byKey(id);
		h.assertTrue(holder.isPresent(), "Missing recipe " + id);
		return holder.get().value();
	}

	private static void assertGone(GameTestHelper h, String... ids) {
		for (String id : ids) {
			h.assertTrue(h.getLevel().getRecipeManager().byKey(prefix(id)).isEmpty(), "The Reburn recipe " + id + " is still there");
		}
	}

	private static ItemStack result(GameTestHelper h, String id) {
		return recipe(h, prefix(id)).getResultItem(h.getLevel().registryAccess());
	}

	// 1.16 goldcloth.json 一次出 4 个莱茵之金；1.16 HolyGrenadeRecipe 一杯鸡尾酒出 8 个圣水手雷；1.16 gildedpotato 出 4 个。
	// 1.16 goldcloth.json makes 4 Das Rheingold; HolyGrenadeRecipe of 1.16 makes 8 grenades from one cocktail; 1.16 gildedpotato makes 4.
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void craftingCounts(GameTestHelper h) {
		h.assertTrue(result(h, "das_rheingold").getCount() == 4, "Das Rheingold must make 4");
		h.assertTrue(result(h, "holy_water_grenade").getCount() == 8, "The grenade must make 8");
		var potato = (RunicAltarRecipe) recipe(h, prefix("runic_altar/gilded_potato"));
		h.assertTrue(potato.getResultItem(h.getLevel().registryAccess()).getCount() == 4 && potato.getMana() == 2000,
				"The gilded potato must make 4 for 2000 mana");
		h.succeed();
	}

	// 1.16 ManaInfusionProvider：钻石变末影珍珠、烈焰棒翻倍都是 20000。
	// ManaInfusionProvider of 1.16: diamond to ender pearl and blaze rod doubling both cost 20000.
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void dimensionCatalystCosts(GameTestHelper h) {
		var pearl = (ManaInfusionRecipe) recipe(h, prefix("mana_infusion/diamond_to_ender_pearl"));
		h.assertTrue(pearl.matches(new ItemStack(Items.DIAMOND)) && pearl.getManaToConsume() == 20000, "Diamond to ender pearl must cost 20000");
		var blaze = (ManaInfusionRecipe) recipe(h, prefix("mana_infusion/blaze_rod_dupe"));
		h.assertTrue(blaze.getManaToConsume() == 20000, "Blaze rod doubling must cost 20000");
		assertGone(h, "mana_infusion/snowball_to_ender_pearl");
		h.succeed();
	}

	// 王者圣剑和阿喀琉斯之盾回到工作台；奥利哈钢只在泰拉凝聚板上做；虚空核心没有重燃的彩虹和重置配方。
	// The Excalibur and the Achilles Shield went back to the crafting table; orichalcos is only made on the terra plate; the Core of the Void has no rainbow or reset recipe of Reburn.
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void reburnRecipesRemoved(GameTestHelper h) {
		assertGone(h, "smithing/excalibur", "smithing/achilles_shield", "runic_altar/orichalcos_ingot",
				"core_of_the_void_rainbow", "core_of_the_void_reset");
		h.assertTrue(result(h, "excalibur").is(ExtraBotanyItems.excalibur), "The Excalibur crafting recipe is missing");
		h.assertTrue(result(h, "achilles_shield").is(ExtraBotanyItems.achillesShield), "The Achilles Shield crafting recipe is missing");
		recipe(h, prefix("terra_plate/orichalcos_ingot"));
		var core = recipe(h, prefix("core_of_the_void"));
		h.assertTrue(core.getIngredients().stream().anyMatch(i -> i.test(new ItemStack(BotaniaItems.FLUGEL_TIARA)))
						&& core.getIngredients().stream().noneMatch(i -> i.test(new ItemStack(ExtraBotanyItems.emptyCoreOfTheVoid))),
				"The Core of the Void must use the Flugel Tiara of 1.16, not the empty core of Reburn");
		h.succeed();
	}

	// 1.12 ModRuneRecipe：成龙指环、蜘蛛指环 2000；血染女仆服 50000，女仆服加四件暗影战士。
	// ModRuneRecipe of 1.12: the Jackie Chan ring and the spider ring 2000; the sanguine maid suit 50000 from the maid suit and four Shadow Warrior pieces.
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void runicAltarRecipesOf112(GameTestHelper h) {
		var jackie = (RunicAltarRecipe) recipe(h, prefix("runic_altar/jackie_chan_ring"));
		var spider = (RunicAltarRecipe) recipe(h, prefix("runic_altar/spider_ring"));
		h.assertTrue(jackie.getMana() == 2000 && spider.getMana() == 2000, "The rings must cost 2000");
		assertGone(h, "jackie_chan_ring", "spider_ring");
		var sanguine = (RunicAltarRecipe) recipe(h, prefix("runic_altar/sanguine_pleiades_combat_maid_suit"));
		h.assertTrue(sanguine.getMana() == 50000 && sanguine.getIngredients().size() == 5, "The sanguine suit must cost 50000 with 5 inputs");
		var shadowium = (RunicAltarRecipe) recipe(h, prefix("runic_altar/shadowium_ingot_triple"));
		h.assertTrue(shadowium.getMana() == 15000 && shadowium.getResultItem(h.getLevel().registryAccess()).getCount() == 3,
				"The triple shadowium recipe of 1.16 must make 3 for 15000");
		h.succeed();
	}

	// 1.12 ModPedestalRecipe：圆石、砂砾、燧石三条加回来，五条都是 5 下、1 点经验。1.16 的灵魂碎片由精灵交易得到。
	// ModPedestalRecipe of 1.12: cobblestone, gravel and flint are back, and all five need 5 strikes and give 1 experience. The spirit fragment of 1.16 comes from the elven trade.
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void pedestalAndElvenTrade(GameTestHelper h) {
		for (String id : List.of("gravel", "flint", "gunpowder", "gilded_potato_mashed", "spirit_fragment")) {
			var smash = (PedestalsRecipe) recipe(h, prefix("pedestal_smash/" + id));
			h.assertTrue(smash.getStrike() == 5 && smash.getExp() == 1, "The pedestal recipe " + id + " must need 5 strikes and give 1 experience");
		}
		var fuel = (ElvenTradeRecipe) recipe(h, prefix("elven_trade/spirit_fuel"));
		h.assertTrue(fuel.getOutputs().getFirst().is(ExtraBotanyItems.spiritFuel), "Nightmare fuel must trade into spirit fuel");
		var fragment = (ElvenTradeRecipe) recipe(h, prefix("elven_trade/spirit_fragment"));
		h.assertTrue(fragment.getOutputs().getFirst().is(ExtraBotanyItems.spiritFragment), "Spirit fuel must trade into a spirit fragment");
		h.succeed();
	}

	private static List<ItemStack> roll(GameTestHelper h, String table) {
		LootTable loot = h.getLevel().getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, prefix(table)));
		LootParams params = new LootParams.Builder(h.getLevel())
				.withParameter(LootContextParams.ORIGIN, h.absoluteVec(net.minecraft.world.phys.Vec3.ZERO))
				.create(LootContextParamSets.CHEST);
		return loot.getRandomItems(params);
	}

	private static Set<String> rolls(GameTestHelper h, String table, int times) {
		Set<String> seen = new HashSet<>();
		for (int i = 0; i < times; i++) {
			for (ItemStack stack : roll(h, table)) {
				seen.add(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath() + "x" + stack.getCount());
			}
		}
		return seen;
	}

	// Eins 和 Zwei 按 1.16 categoryListA/B；九又四分之三按 1.12 REWARDBAG5。
	// Eins and Zwei follow categoryListA/B of 1.16; Nine and Three-Quarters follows REWARDBAG5 of 1.12.
	@GameTest(template = "empty", timeoutTicks = 40)
	public static void rewardBagContents(GameTestHelper h) {
		Set<String> eins = rolls(h, "reward_bags/eins", 200);
		h.assertTrue(Set.of("universal_petalx4", "universal_petalx6", "universal_petalx8").containsAll(eins), "Eins must give universal petals: " + eins);
		Set<String> zwei = rolls(h, "reward_bags/zwei", 200);
		h.assertTrue(Set.of("element_runex2", "sin_runex1").containsAll(zwei) && zwei.size() == 2, "Zwei must give element and sin runes: " + zwei);
		Set<String> nine = rolls(h, "reward_bags/nine_and_three_quarters", 400);
		h.assertTrue(Set.of("eins_reward_bagx16", "zwei_reward_bagx10", "drei_reward_bagx6", "vier_reward_bagx6", "gaia_ingotx1",
				"terrasteel_ingotx5", "challenge_ticketx1", "hero_medalx1").containsAll(nine) && nine.contains("terrasteel_ingotx5"),
				"Nine and Three-Quarters must follow 1.12: " + nine);
		h.succeed();
	}

	// 盖亚守护者III 只掉潘多拉魔盒；两张唱片是"终局"进度的奖励。
	// Guardian of Gaia III only drops the Pandora's Box; the two discs are the reward of the "End Day" advancement.
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void noDiscFromGaiaIII(GameTestHelper h) {
		var table = h.getLevel().getServer().reloadableRegistries().getLootTable(ExtraBotanyEntityType.GAIA_III.getDefaultLootTable());
		var player = net.neoforged.neoforge.common.util.FakePlayerFactory.getMinecraft(h.getLevel());
		var gaia = ExtraBotanyEntityType.GAIA_III.create(h.getLevel());
		h.assertTrue(gaia != null, "Could not create Guardian of Gaia III");
		gaia.setPos(h.absoluteVec(net.minecraft.world.phys.Vec3.ZERO));
		for (int i = 0; i < 50; i++) {
			var params = new LootParams.Builder(h.getLevel())
					.withParameter(LootContextParams.THIS_ENTITY, gaia)
					.withParameter(LootContextParams.ORIGIN, gaia.position())
					.withParameter(LootContextParams.DAMAGE_SOURCE, player.damageSources().playerAttack(player))
					.withParameter(LootContextParams.LAST_DAMAGE_PLAYER, player)
					.create(LootContextParamSets.ENTITY);
			h.assertTrue(table.getRandomItems(params).stream().noneMatch(s -> s.is(ExtraBotanyItems.recordGaia3) || s.is(ExtraBotanyItems.recordHerrscherOfTheVoid)),
					"Guardian of Gaia III still drops a music disc");
		}
		gaia.discard();
		h.succeed();
	}

	// 1.12 LootHandler：七种箱子各加一个奖池；1.12 ItemHalloweenCandy.onSpawn：僵尸和骷髅 2% 拿着糖果袋，必定掉落。
	// LootHandler of 1.12: seven chests get one pool each; ItemHalloweenCandy.onSpawn of 1.12: zombies and skeletons hold a candy bag 2% of the time, and it always drops.
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void chestInjectionAndCandyBag(GameTestHelper h) {
		for (String chest : List.of("simple_dungeon", "abandoned_mineshaft", "village/village_weaponsmith")) {
			LootTable table = h.getLevel().getServer().reloadableRegistries()
					.getLootTable(ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.withDefaultNamespace("chests/" + chest)));
			h.assertTrue(table.getPool("extrabotany_inject_pool") != null, "The chest " + chest + " has no injected pool");
		}
		h.assertTrue(EntityType.ZOMBIE.is(ExtraBotanyTags.Entities.CANDY_BAG_HOLDERS) && EntityType.SKELETON.is(ExtraBotanyTags.Entities.CANDY_BAG_HOLDERS)
				&& !EntityType.CREEPER.is(ExtraBotanyTags.Entities.CANDY_BAG_HOLDERS), "The candy bag holders must be the zombies and the skeleton");
		h.assertTrue(OriginalLootHandler.CANDY_BAG_CHANCE == 0.02F, "The candy bag chance must be 2%");
		Zombie zombie = new Zombie(h.getLevel());
		OriginalLootHandler.giveCandyBag(zombie);
		var saved = zombie.saveWithoutId(new net.minecraft.nbt.CompoundTag());
		h.assertTrue(zombie.getOffhandItem().is(ExtraBotanyItems.candyBag)
				&& saved.getList("HandDropChances", net.minecraft.nbt.Tag.TAG_FLOAT).getFloat(1) == 1F, "The candy bag must be in the off hand and always drop");
		h.succeed();
	}

	// 1.12 StatHandler：完成 6、12、18 个进度时的三个奖励进度。
	// StatHandler of 1.12: the three reward advancements at 6, 12 and 18 completed advancements.
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void countingRewardAdvancements(GameTestHelper h) {
		var manager = h.getLevel().getServer().getAdvancements();
		for (String name : List.of(LibAdvancementNames.MANA_DRIVER_GET, LibAdvancementNames.MAGIC_FINGER_GET, LibAdvancementNames.JINGWEI_FEATHER_GET)) {
			h.assertTrue(manager.get(prefix("main/" + name)) != null, "Missing advancement " + name);
		}
		h.assertTrue(java.util.Arrays.equals(AllAdvancementsHandler.COUNT_NEEDED, new int[] { 6, 12, 18 }), "The counts must be 6, 12 and 18");
		h.succeed();
	}

	// 1.16 的花瓣配方：日曜百合是 1 白 3 黄，铃铛花没有灵魂碎片。
	// The petal recipes of 1.16: the Sunshine Lily is 1 white and 3 yellow, and the bellflower has no spirit fragment.
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void petalRecipesOf116(GameTestHelper h) {
		var sun = recipe(h, ResourceLocation.fromNamespaceAndPath("botania", "petal_apothecary/sunshine_lily"));
		h.assertTrue(sun.getIngredients().size() == 4
				&& sun.getIngredients().stream().filter(i -> i.test(new ItemStack(BotaniaItems.YELLOW_MYSTICAL_PETAL))).count() == 3, "The Sunshine Lily must be 1 white and 3 yellow");
		var bell = recipe(h, ResourceLocation.fromNamespaceAndPath("botania", "petal_apothecary/bellflower"));
		Item fragment = ExtraBotanyItems.spiritFragment;
		h.assertTrue(bell.getIngredients().size() == 6 && bell.getIngredients().stream().noneMatch(i -> i.test(new ItemStack(fragment))),
				"The bellflower must use the 6 petals of 1.16");
		h.succeed();
	}
}
