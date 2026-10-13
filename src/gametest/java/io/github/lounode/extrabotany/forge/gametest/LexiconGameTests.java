package io.github.lounode.extrabotany.forge.gametest;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import io.github.lounode.extrabotany.common.lib.LibMisc;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * 图鉴条目的回归测试。条目是客户端的资源，游戏在加载时不检查它们引用的东西，写错的配方 ID 只会在翻到那一页时显示一个空白的格子。
 * Regression tests for the lexicon entries. The entries are client resources, and the game does not examine their references at load; an incorrect recipe ID only shows as an empty grid when the player opens that page.
 */
@GameTestHolder("extrabotany")
@PrefixGameTestTemplate(false)
public final class LexiconGameTests {
	private static final String ENTRIES = "assets/botania/patchouli_books/lexica_botania/en_us/entries";

	private LexiconGameTests() {}

	private static Path modResource(String path) {
		return ModList.get().getModFileById(LibMisc.MOD_ID).getFile().findResource(path.split("/"));
	}

	private static JsonObject readJson(Path path) {
		try {
			return JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8)).getAsJsonObject();
		} catch (IOException e) {
			throw new IllegalStateException("Cannot read " + path, e);
		}
	}

	/**
	 * 每个条目的图标、展示的物品、配方和多方块结构里的方块都存在；每个翻译键在英文和中文的语言文件里都有。
	 * The icon, the shown items, the recipes and the blocks of the multiblock structures of each entry exist; each translation key is in the English and in the Chinese language file.
	 */
	@GameTest(template = "empty", timeoutTicks = 40)
	public static void lexiconEntriesReferToThingsThatExist(GameTestHelper helper) {
		Path root = modResource(ENTRIES);
		helper.assertTrue(Files.isDirectory(root), "The lexicon entries are not in the mod file: " + ENTRIES);
		JsonObject english = readJson(modResource("assets/extrabotany/lang/en_us.json"));
		JsonObject chinese = readJson(modResource("assets/extrabotany/lang/zh_cn.json"));
		Set<ResourceLocation> recipes = helper.getLevel().getRecipeManager().getRecipeIds().collect(java.util.stream.Collectors.toSet());

		List<Path> files;
		try (Stream<Path> walk = Files.walk(root)) {
			files = walk.filter(path -> path.toString().endsWith(".json")).toList();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		helper.assertTrue(files.size() >= 100, "Too few lexicon entries were found: " + files.size());

		List<String> problems = new ArrayList<>();
		for (Path file : files) {
			String name = root.relativize(file).toString().replace('\\', '/');
			JsonObject entry = readJson(file);
			checkItem(problems, name, "icon", entry.get("icon"));
			checkKey(problems, name, entry.get("name"), english, chinese);
			if (entry.has("extra_recipe_mappings")) {
				for (String item : entry.getAsJsonObject("extra_recipe_mappings").keySet()) {
					checkItemId(problems, name, "extra_recipe_mappings", item);
				}
			}
			for (JsonElement element : entry.getAsJsonArray("pages")) {
				JsonObject page = element.getAsJsonObject();
				for (String key : List.of("text", "title", "heading", "name")) {
					checkKey(problems, name, page.get(key), english, chinese);
				}
				for (String key : List.of("recipe", "recipe2")) {
					if (page.has(key)) {
						ResourceLocation id = ResourceLocation.tryParse(page.get(key).getAsString());
						if (id == null || !recipes.contains(id)) {
							problems.add(name + ": the recipe " + page.get(key).getAsString() + " does not exist");
						}
					}
				}
				for (String key : List.of("item", "input", "output", "input1", "input2")) {
					checkItem(problems, name, key, page.get(key));
				}
				if (page.has("multiblock")) {
					JsonObject mapping = page.getAsJsonObject("multiblock").getAsJsonObject("mapping");
					for (String letter : mapping.keySet()) {
						String block = mapping.get(letter).getAsString();
						int state = block.indexOf('[');
						ResourceLocation id = ResourceLocation.tryParse(state < 0 ? block : block.substring(0, state));
						if (id == null || !BuiltInRegistries.BLOCK.containsKey(id)) {
							problems.add(name + ": the multiblock block " + block + " does not exist");
						}
					}
				}
			}
		}
		helper.assertTrue(problems.isEmpty(), problems.size() + " problem(s) in the lexicon: " + String.join("; ", problems));
		helper.succeed();
	}

	/**
	 * 物品字段可以带有组件（{@code id[...]}），也可以是用逗号隔开的几个物品。
	 * An item field can have components ({@code id[...]}), and can be some items with commas between them.
	 */
	private static void checkItem(List<String> problems, String entry, String field, JsonElement value) {
		if (value == null || !value.isJsonPrimitive()) {
			return;
		}
		String text = value.getAsString();
		int components = text.indexOf('[');
		if (components >= 0) {
			text = text.substring(0, components);
		}
		for (String item : text.split(",")) {
			checkItemId(problems, entry, field, item.trim());
		}
	}

	private static void checkItemId(List<String> problems, String entry, String field, String item) {
		ResourceLocation id = ResourceLocation.tryParse(item);
		if (id == null || !BuiltInRegistries.ITEM.containsKey(id)) {
			problems.add(entry + ": the item " + item + " in \"" + field + "\" does not exist");
		}
	}

	/**
	 * 只检查本模组的翻译键。空字符串和别的模组的键不检查。
	 * Only the translation keys of this mod are examined. Empty strings and the keys of other mods are not.
	 */
	private static void checkKey(List<String> problems, String entry, JsonElement value, JsonObject english, JsonObject chinese) {
		if (value == null || !value.isJsonPrimitive()) {
			return;
		}
		String key = value.getAsString();
		if (!key.contains("extrabotany")) {
			return;
		}
		if (!english.has(key)) {
			problems.add(entry + ": the key " + key + " is not in en_us.json");
		}
		if (!chinese.has(key)) {
			problems.add(entry + ": the key " + key + " is not in zh_cn.json");
		}
	}
}
