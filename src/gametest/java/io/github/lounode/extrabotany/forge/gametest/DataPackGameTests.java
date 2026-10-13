package io.github.lounode.extrabotany.forge.gametest;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import vazkii.botania.common.lib.BotaniaTags;

import io.github.lounode.extrabotany.common.block.ExtraBotanyBlocks;
import io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.item.record.ExtraBotanyJukeboxSongs;
import io.github.lounode.extrabotany.common.lib.LibMisc;
import io.github.lounode.extrabotany.common.sounds.ExtraBotanySounds;

import java.util.List;
import java.util.Map;

/**
 * 数据包内容的回归测试：标签、战利品表、唱片曲目和配方分类。这些文件一部分是生成的，一部分是手写的，测试确认两种来源合在一起之后内容完整。
 * Regression tests for the data pack content: tags, loot tables, jukebox songs and recipe categories. Some of these files are generated and some are written by hand, and the tests make sure that the content is complete when the two sources are put together.
 */
@GameTestHolder("extrabotany")
@PrefixGameTestTemplate(false)
public final class DataPackGameTests {
	private DataPackGameTests() {}

	private static TagKey<Item> itemTag(String namespace, String path) {
		return ItemTags.create(ResourceLocation.fromNamespaceAndPath(namespace, path));
	}

	/**
	 * 从原版恢复的内容在标签里。以前这些条目是直接写进生成文件的，生成器不知道它们。
	 * The content that was restored from the original is in the tags. Before, these entries were typed directly into the generated files, and the generators did not know them.
	 */
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void restoredContentIsInTheTags(GameTestHelper helper) {
		Map<TagKey<Item>, List<Item>> itemTags = Map.of(
				BotaniaTags.Items.MANA_USING_ITEMS, List.of(ExtraBotanyItems.spearOfSubspace, ExtraBotanyItems.flamescionWeapon,
						ExtraBotanyItems.silverBullet, ExtraBotanyItems.sunRing, ExtraBotanyItems.silentSagesBoots, ExtraBotanyItems.excalibur),
				ItemTags.SWORDS, List.of(ExtraBotanyItems.excalibur, ExtraBotanyItems.shadowKatana, ExtraBotanyItems.judahOathSakura),
				BotaniaTags.Items.RUNES, List.of(ExtraBotanyItems.elementRune, ExtraBotanyItems.sinRune),
				ItemTags.HEAD_ARMOR, List.of(ExtraBotanyItems.mikuHelm, ExtraBotanyItems.shootingGuardianHelm, ExtraBotanyItems.silentSagesHelm),
				itemTag("curios", "ring"), List.of(ExtraBotanyItems.sunRing, ExtraBotanyItems.deathRing, ExtraBotanyItems.allForOne),
				itemTag("curios", "necklace"), List.of(ExtraBotanyItems.moonPendant, ExtraBotanyItems.pureDaisyPendant),
				itemTag("curios", "curio"), List.of(ExtraBotanyItems.peaceAmulet, ExtraBotanyItems.theCommunity, ExtraBotanyItems.gemOfConquest),
				itemTag("curios", "body"), List.of(ExtraBotanyItems.redScarf, ExtraBotanyItems.coreOfTheVoid),
				itemTag("c", "music_discs"), List.of(ExtraBotanyItems.recordGaia3, ExtraBotanyItems.recordEgo, ExtraBotanyItems.recordHerrscherOfTheVoid),
				itemTag("c", "ingots/orichalcos"), List.of(ExtraBotanyItems.orichalcos));
		for (var entry : itemTags.entrySet()) {
			for (Item item : entry.getValue()) {
				helper.assertTrue(new ItemStack(item).is(entry.getKey()),
						BuiltInRegistries.ITEM.getKey(item) + " must be in the item tag " + entry.getKey().location());
			}
		}

		Map<TagKey<Block>, List<Block>> blockTags = Map.of(
				BotaniaTags.Blocks.SPECIAL_FLOWERS, List.of(ExtrabotanyFlowerBlocks.stardustLotus, ExtrabotanyFlowerBlocks.tradeOrchid),
				BotaniaTags.Blocks.FUNCTIONAL_SPECIAL_FLOWERS, List.of(ExtrabotanyFlowerBlocks.stardustLotus),
				BlockTags.FLOWER_POTS, List.of(ExtrabotanyFlowerBlocks.stardustLotusPotted),
				BlockTags.MINEABLE_WITH_PICKAXE, List.of(ExtraBotanyBlocks.manaBuffer, ExtraBotanyBlocks.quantumManaBuffer,
						ExtraBotanyBlocks.manaGenerator, ExtraBotanyBlocks.manaLiquefaction, ExtraBotanyBlocks.livingrockBarrel, ExtraBotanyBlocks.trophy));
		for (var entry : blockTags.entrySet()) {
			for (Block block : entry.getValue()) {
				helper.assertTrue(block.defaultBlockState().is(entry.getKey()),
						BuiltInRegistries.BLOCK.getKey(block) + " must be in the block tag " + entry.getKey().location());
			}
		}
		helper.succeed();
	}

	/**
	 * 每个有战利品表的方块和每个 Boss 都能在数据包里找到自己的战利品表。
	 * Each block that has a loot table, and each boss, finds its loot table in the data pack.
	 */
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void lootTablesExist(GameTestHelper helper) {
		var lootTables = helper.getLevel().getServer().reloadableRegistries();
		int checked = 0;
		for (Block block : BuiltInRegistries.BLOCK) {
			if (!LibMisc.MOD_ID.equals(BuiltInRegistries.BLOCK.getKey(block).getNamespace())) {
				continue;
			}
			ResourceKey<LootTable> key = block.getLootTable();
			if (key == net.minecraft.world.level.storage.loot.BuiltInLootTables.EMPTY) {
				continue;
			}
			helper.assertTrue(lootTables.getLootTable(key) != LootTable.EMPTY, "There is no loot table for the block " + key.location());
			checked++;
		}
		helper.assertTrue(checked > 80, "Too few blocks were examined: " + checked);

		for (String boss : List.of("gaia_3rd", "ego", "void_herrscher")) {
			ResourceKey<LootTable> key = ResourceKey.create(Registries.LOOT_TABLE,
					ResourceLocation.fromNamespaceAndPath(LibMisc.MOD_ID, "entities/" + boss));
			helper.assertTrue(lootTables.getLootTable(key) != LootTable.EMPTY, "There is no loot table for the boss " + boss);
		}
		helper.succeed();
	}

	/**
	 * 三张唱片都有曲目，曲目的长度和音频文件的长度相符。律者的曲目以前写成 201 秒，实际的音频是 47.5 秒。
	 * The three records all have a song, and the length of each song agrees with the length of the audio file. The song of the Herrscher was 201 seconds before; the audio is 47.5 seconds.
	 */
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void recordsHaveSongsOfTheCorrectLength(GameTestHelper helper) {
		var songs = helper.getLevel().registryAccess().registryOrThrow(Registries.JUKEBOX_SONG);
		record Expected(Item item, ResourceKey<JukeboxSong> song, net.minecraft.sounds.SoundEvent sound, float seconds) {}
		for (Expected expected : List.of(
				new Expected(ExtraBotanyItems.recordGaia3, ExtraBotanyJukeboxSongs.GAIA_3, ExtraBotanySounds.MUSIC_GAIA3, 201F),
				new Expected(ExtraBotanyItems.recordEgo, ExtraBotanyJukeboxSongs.EGO, ExtraBotanySounds.MUSIC_EGO, 198F),
				new Expected(ExtraBotanyItems.recordHerrscherOfTheVoid, ExtraBotanyJukeboxSongs.HERRSCHER_OF_THE_VOID, ExtraBotanySounds.MUSIC_HERRSCHER, 48F))) {
			var playable = new ItemStack(expected.item()).get(DataComponents.JUKEBOX_PLAYABLE);
			helper.assertTrue(playable != null, BuiltInRegistries.ITEM.getKey(expected.item()) + " cannot be played in a jukebox");
			Holder<JukeboxSong> holder = playable.song().unwrap(helper.getLevel().registryAccess()).orElse(null);
			helper.assertTrue(holder != null && holder.is(expected.song()), "The record points to an incorrect song: " + expected.song().location());
			JukeboxSong song = songs.getOrThrow(expected.song());
			helper.assertTrue(song.soundEvent().value() == expected.sound(), "The song has an incorrect sound: " + expected.song().location());
			helper.assertTrue(song.lengthInSeconds() == expected.seconds(),
					"The length of " + expected.song().location() + " is " + song.lengthInSeconds() + " seconds, expected " + expected.seconds());
		}
		helper.succeed();
	}

	/**
	 * 本模组的合成配方都有合法的分类。游戏对不认识的分类不报错，只是悄悄当作"杂项"，所以这里直接读原始的 JSON。
	 * All crafting recipes of this mod have a valid category. The game gives no error for a category that it does not know and silently uses "misc", thus this test reads the raw JSON.
	 */
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void craftingRecipesHaveValidCategories(GameTestHelper helper) {
		var resources = helper.getLevel().getServer().getResourceManager();
		var valid = java.util.Arrays.stream(CraftingBookCategory.values()).map(CraftingBookCategory::getSerializedName).toList();
		int checked = 0;
		for (var entry : resources.listResources("recipe", path -> path.getNamespace().equals(LibMisc.MOD_ID) && path.getPath().endsWith(".json")).entrySet()) {
			com.google.gson.JsonObject json;
			try (var reader = entry.getValue().openAsReader()) {
				json = com.google.gson.JsonParser.parseReader(reader).getAsJsonObject();
			} catch (java.io.IOException e) {
				throw new IllegalStateException(e);
			}
			String type = json.has("type") ? json.get("type").getAsString() : "";
			if (!type.startsWith("minecraft:crafting_") || !json.has("category")) {
				continue;
			}
			String category = json.get("category").getAsString();
			helper.assertTrue(valid.contains(category), entry.getKey() + " has the unknown crafting category \"" + category + "\"");
			checked++;
		}
		helper.assertTrue(checked > 50, "Too few crafting recipes were examined: " + checked);

		// 所有的配方都能加载：数据包里的配方文件数量和游戏里的配方数量相同。
		// All recipes load: the number of recipe files in the data pack is the same as the number of recipes in the game.
		long files = resources.listResources("recipe", path -> path.getNamespace().equals(LibMisc.MOD_ID) && path.getPath().endsWith(".json")).size();
		long loaded = helper.getLevel().getRecipeManager().getRecipes().stream()
				.map(RecipeHolder::id).filter(id -> id.getNamespace().equals(LibMisc.MOD_ID)).count();
		helper.assertTrue(files == loaded, "The data pack has " + files + " recipe files of this mod, but the game loaded " + loaded);
		long crafting = helper.getLevel().getRecipeManager().getRecipes().stream()
				.filter(holder -> holder.id().getNamespace().equals(LibMisc.MOD_ID) && holder.value() instanceof CraftingRecipe).count();
		helper.assertTrue(crafting > 50, "Too few crafting recipes were loaded: " + crafting);
		helper.succeed();
	}
}
