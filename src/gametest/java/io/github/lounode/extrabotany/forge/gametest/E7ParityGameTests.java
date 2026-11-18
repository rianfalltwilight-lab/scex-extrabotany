package io.github.lounode.extrabotany.forge.gametest;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.lounode.extrabotany.common.item.material.ArmorsMaterial;
import io.github.lounode.extrabotany.common.lib.LibMisc;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * 对照原版审计第 E7 批：美术、音效和中文名回到原版。
 * Batch E7 of the parity audit with the original: art, sounds and Chinese names go back to the original.
 */
@GameTestHolder("extrabotany")
@PrefixGameTestTemplate(false)
public final class E7ParityGameTests {

	private E7ParityGameTests() {}

	private static InputStream resource(String path) {
		return E7ParityGameTests.class.getResourceAsStream("/" + path);
	}

	// 1.12 烦人花音效加回，重燃没人用的风铃草铃声删除；装备音效按 1.16 ExtraBotanyAPI.ArmorMaterial。
	// The annoying flower sound of 1.12 is back and the unused bellflower ring of Reburn is gone; the equip sounds follow ArmorMaterial of 1.16 ExtraBotanyAPI.
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void soundsFollowTheOriginal(GameTestHelper h) {
		h.assertTrue(BuiltInRegistries.SOUND_EVENT.containsKey(ResourceLocation.fromNamespaceAndPath(LibMisc.MOD_ID, "entity.flower_weapon.annoying_flower")),
				"The annoying flower sound is missing");
		for (String gone : List.of("block.bellflower.ring", "item.armor.equip_maid", "item.armor.equip_goblin", "item.armor.equip_warrior")) {
			h.assertTrue(!BuiltInRegistries.SOUND_EVENT.containsKey(ResourceLocation.fromNamespaceAndPath(LibMisc.MOD_ID, gone)), "The Reburn sound " + gone + " is still there");
		}
		h.assertTrue(ArmorsMaterial.PLEIADS_MAID_COMBAT.value().equipSound().is(SoundEvents.ARMOR_EQUIP_DIAMOND)
				&& ArmorsMaterial.GOBLIN_SLAYER.value().equipSound().is(SoundEvents.ARMOR_EQUIP_IRON)
				&& ArmorsMaterial.SHADOW_WARRIOR.value().equipSound().is(SoundEvents.ARMOR_EQUIP_IRON),
				"The equip sounds must be diamond, iron and iron");
		h.assertTrue(resource("assets/extrabotany/sounds/entity/flower_weapon/annoying_flower.ogg") != null
				&& resource("assets/extrabotany/sounds/block/bellflower/ring.ogg") == null, "The sound files do not match");
		h.succeed();
	}

	// 原版中文名：两个版本都有时用 1.16 的；"诅咒指环"还给 death_ring，重燃的 curse_ring 叫"凋零指环"。
	// The original Chinese names, 1.16 when both versions have the item; "诅咒指环" goes back to death_ring, and the curse_ring of Reburn is "凋零指环".
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void chineseNamesFollowTheOriginal(GameTestHelper h) throws Exception {
		JsonObject zh;
		try (var reader = new InputStreamReader(resource("assets/extrabotany/lang/zh_cn.json"), StandardCharsets.UTF_8)) {
			zh = JsonParser.parseReader(reader).getAsJsonObject();
		}
		Map<String, String> expected = Map.of(
				"item.extrabotany.death_ring", "诅咒指环",
				"item.extrabotany.curse_ring", "凋零指环",
				"block.extrabotany.sunshine_lily", "日耀百合",
				"block.extrabotany.moonlight_lily", "月光百合",
				"block.extrabotany.stardustlotus", "星移莲",
				"item.extrabotany.gilded_potato", "镀金服务器",
				"item.extrabotany.orichalcos_ingot", "奥利哈刚锭",
				"item.extrabotany.goblin_slayer_helmet", "哥布林杀手头盔",
				"item.extrabotany.mana_drink", "魔力鸡尾酒",
				"item.extrabotany.terrasteel_hammer", "泰拉钢锤");
		expected.forEach((key, name) -> h.assertTrue(name.equals(zh.get(key).getAsString()), key + " must be " + name + ", got " + zh.get(key)));
		h.succeed();
	}

	// 换回原版的贴图和 1.12 的节日、手册素材都在；重燃的次元催化器叠加图删除。
	// The original textures and the festive and lexicon art of 1.12 are present; the dimension catalyst overlay of Reburn is gone.
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void artFiles(GameTestHelper h) {
		for (String path : List.of("item/eins_reward_bag_christmas", "item/nine_and_three_quarters_reward_bag_christmas",
				"entity/dark_pixie_pumpkin", "gui/lexicon/geminiorchid", "gui/lexicon/manafluid")) {
			h.assertTrue(resource("assets/extrabotany/textures/" + path + ".png") != null, "Missing texture " + path);
		}
		h.assertTrue(resource("assets/extrabotany/textures/block/dimension_catalyst_overlay.png") == null
				&& resource("assets/extrabotany/textures/block/dimension_catalyst.png.mcmeta") == null, "The Reburn dimension catalyst art is still there");
		h.succeed();
	}
}
