package io.github.lounode.extrabotany.data;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.brew.Brew;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.item.BotaniaItems;

import io.github.lounode.extrabotany.common.block.ExtraBotanyBlocks;
import io.github.lounode.extrabotany.common.block.block_entity.PedestalBlockEntity;
import io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks;
import io.github.lounode.extrabotany.common.block.flower.functional.ManalinkBlockEntity;
import io.github.lounode.extrabotany.common.brew.ExtraBotanyBrews;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.lib.LibAdvancementNames;
import io.github.lounode.extrabotany.common.lib.RegistryHelper;
import io.github.lounode.extrabotany.data.patchouli.PatchouliBuilder;
import io.github.lounode.extrabotany.data.patchouli.PatchouliEntry;
import io.github.lounode.extrabotany.data.patchouli.PatchouliProvider;
import io.github.lounode.extrabotany.data.patchouli.page.botania.*;
import io.github.lounode.extrabotany.data.patchouli.page.extrabotany.EatPage;
import io.github.lounode.extrabotany.data.patchouli.page.extrabotany.PedestalPage;
import io.github.lounode.extrabotany.data.patchouli.page.extrabotany.SmithingPage;
import io.github.lounode.extrabotany.data.patchouli.page.extrabotany.WandExtensionPage;

import java.nio.file.Path;
import java.util.function.Consumer;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;
import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefixBotania;

/**
 * 生成本模组在植物魔法图鉴里的条目。
 * Makes the entries of this mod in the lexicon of Botania.
 *
 * <p>从原版恢复的内容的条目，以及"额外植物学"这个分类本身，是 {@code src/main/resources} 里的手写文件。
 * <p>The entries for the content that was restored from the original, and the category "Extra Botany" itself, are hand-written files in {@code src/main/resources}.
 */
public final class PatchouliBookProvider extends PatchouliProvider {
	private static final ResourceLocation CATEGORY = prefixBotania("extrabotanies");
	private static final ResourceLocation GENERATING_FLOWERS = prefixBotania("generating_flowers");
	private static final ResourceLocation FUNCTIONAL_FLOWERS = prefixBotania("functional_flowers");
	private static final ResourceLocation BAUBLES = prefixBotania("baubles");
	private static final ResourceLocation MANA = prefixBotania("mana");

	public PatchouliBookProvider(PackOutput packOutput) {
		super(packOutput);
	}

	@Override
	protected void buildEntries(Consumer<PatchouliEntry> consumer) {
		addOriginalEntries(consumer);
		addFunctionEntries(consumer);
		PatchouliBuilder.entry(CATEGORY)
				.withName("extrabotany.entry.mana_reader")
				.withIcon(ExtraBotanyItems.manaReader)
				.pages(
						text("extrabotany.page.mana_reader0"),
						crafting(ExtraBotanyItems.manaReader)
								.withText("extrabotany.page.mana_reader1"),
						text("extrabotany.page.mana_reader2"),
						new WandExtensionPage(BotaniaItems.WAND_OF_THE_FOREST),
						new WandExtensionPage(BotaniaItems.WAND_OF_THE_ELVEN_FOREST)
				)
				.save(consumer, id("mana_reader"));
		PatchouliBuilder.entry(CATEGORY)
				.withName("extrabotany.entry.hammers")
				.withIcon(ExtraBotanyItems.manasteelHammer)
				.pages(
						text("extrabotany.page.hammers0"),
						crafting(ExtraBotanyItems.manasteelHammer).withText("extrabotany.page.hammers1"),
						text("extrabotany.page.hammers2"),
						crafting(ExtraBotanyItems.elementiumHammer).withText("extrabotany.page.hammers3"),
						text("extrabotany.page.hammers4"),
						crafting(ExtraBotanyItems.terrasteelHammer).withText("extrabotany.page.hammers5"),
						text("extrabotany.page.hammers6"),
						new SmithingPage("extrabotany:smithing/gaia_hammer").withText("extrabotany.page.hammers7"),
						text("extrabotany.page.hammers8"),
						crafting(ExtraBotanyItems.photoniumHammer).withText("extrabotany.page.hammers9"),
						text("extrabotany.page.hammers10"),
						crafting(ExtraBotanyItems.shadowiumHammer).withText("extrabotany.page.hammers11"),
						text("extrabotany.page.hammers12"),
						crafting(ExtraBotanyItems.aerialiteHammer).withText("extrabotany.page.hammers13"),
						text("extrabotany.page.hammers14"),
						runicAlter(ExtraBotanyItems.orichalcosHammer).withText("extrabotany.page.hammers15"),
						text("extrabotany.page.hammers16"),
						text("extrabotany.page.hammers17"),
						terraPlate(ExtraBotanyItems.rheinHammer).withText("extrabotany.page.hammers18")
				)
				.save(consumer, id("hammers"));
		PatchouliBuilder.entry(CATEGORY)
				.withName("extrabotany.entry.camera")
				.withIcon(ExtraBotanyItems.camera)
				.withAdvancement(mainAdvancement(LibAdvancementNames.STYGIAN_TWINS))
				.pages(
						text("extrabotany.page.camera0"),
						text("extrabotany.page.camera1"),
						crafting(ExtraBotanyItems.camera)
								.withText("extrabotany.page.camera2")
				)
				.save(consumer, id("camera"));
		PatchouliBuilder.entry(CATEGORY)
				.withName("extrabotany.entry.mana_ring_master")
				.withIcon(ExtraBotanyItems.manaRingMaster)
				.withAdvancement(mainAdvancement(LibAdvancementNames.THE_SOURCE_OF_HONKAI))
				.pages(
						text("extrabotany.page.mana_ring_master0"),
						crafting(ExtraBotanyItems.manaRingMaster)
								.withText("extrabotany.page.mana_ring_master1")
				)
				.save(consumer, id("mana_ring_master"));
		PatchouliBuilder.entry(CATEGORY)
				.withName("extrabotany.entry.failnaught")
				.withIcon(ExtraBotanyItems.failnaught)
				.withAdvancement(mainAdvancement(LibAdvancementNames.THE_SOURCE_OF_HONKAI))
				.pages(
						text("extrabotany.page.failnaught0"),
						crafting(ExtraBotanyItems.failnaught)
								.withText("extrabotany.page.failnaught1"),
						text("extrabotany.page.failnaught2"),
						text("extrabotany.page.failnaught3")
				)
				.save(consumer, id("failnaught"));
		PatchouliBuilder.entry(CATEGORY)
				.withName("extrabotany.entry.pedestals")
				.withIcon(ExtraBotanyBlocks.livingrockPedestal)
				.pages(
						text("extrabotany.page.pedestals0"),
						crafting(ExtraBotanyBlocks.livingrockPedestal)
								.withText("extrabotany.page.pedestals1"),
						text("extrabotany.page.pedestals2"),
						pedestal(ExtraBotanyItems.gildedPotatoMashed)
								.withText("extrabotany.page.pedestals3"),
						pedestal(ExtraBotanyItems.spiritFragment)
								.withText("extrabotany.page.pedestals5"),
						text("extrabotany.page.pedestals4")
				)
				.extraRecipeMapping(ExtraBotanyBlocks.calcitePedestal, 1)
				.save(consumer, id("pedestals"));
		PatchouliBuilder.entry(CATEGORY)
				.withName("extrabotany.entry.fuels")
				.withIcon(ExtraBotanyItems.nightmareFuel)
				.pages(
						text("extrabotany.page.fuels0"),
						manaInfusion(ExtraBotanyItems.nightmareFuel)
								.withText("extrabotany.page.fuels1"),
						text("extrabotany.page.fuels2"),
						new EatPage(ExtraBotanyItems.nightmareFuel, ExtraBotanyItems.spiritFuel)
								.withText("extrabotany.page.fuels3"),
						text("extrabotany.page.fuels4")
				)
				.save(consumer, id("fuels"));
		PatchouliBuilder.entry(CATEGORY)
				.withName("extrabotany.entry.zadkiel")
				.withIcon(ExtraBotanyItems.zadkiel)
				.pages(
						text("extrabotany.page.zadkiel0"),
						runicAlter(ExtraBotanyItems.zadkiel)
								.withText("extrabotany.page.zadkiel1")
				)
				.save(consumer, id("zadkiel"));
		PatchouliBuilder.entry(CATEGORY)
				.withName("extrabotany.entry.excalibur")
				.withIcon(ExtraBotanyItems.excalibur)
				.withAdvancement(mainAdvancement(LibAdvancementNames.THE_SOURCE_OF_HONKAI))
				.pages(
						text("extrabotany.page.excalibur0"),
						smithing(ExtraBotanyItems.excalibur)
								.withText("extrabotany.page.excalibur1")
				)
				.save(consumer, id("excalibur"));
		PatchouliBuilder.entry(CATEGORY)
				.withName("extrabotany.entry.jingwei")
				.withIcon(ExtraBotanyItems.featherOfJingwei)
				.withAdvancement(mainAdvancement(LibAdvancementNames.THE_SOURCE_OF_HONKAI))
				.pages(
						text("extrabotany.page.jingwei0"),
						crafting(ExtraBotanyItems.featherOfJingwei)
								.withText("extrabotany.page.jingwei1")
				)
				.save(consumer, id("jingwei"));
		PatchouliBuilder.entry(CATEGORY)
				.withName("extrabotany.entry.magic_finger")
				.withIcon(ExtraBotanyItems.magicFinger)
				.withAdvancement(mainAdvancement(LibAdvancementNames.THE_SOURCE_OF_HONKAI))
				.pages(
						text("extrabotany.page.magic_finger0"),
						crafting(ExtraBotanyItems.magicFinger)
								.withText("extrabotany.page.magic_finger1")

				)
				.save(consumer, id("magic_finger"));
		PatchouliBuilder.entry(CATEGORY)
				.withName("extrabotany.entry.walking_cane")
				.withIcon(ExtraBotanyItems.walkingCane)
				.pages(
						text("extrabotany.page.walking_cane0"),
						crafting(ExtraBotanyItems.walkingCane)
								.withText("extrabotany.page.walking_cane1")
				)
				.save(consumer, id("walking_cane"));
		PatchouliBuilder.entry(CATEGORY)
				.withName("extrabotany.entry.core_of_the_void")
				.withIcon(ExtraBotanyItems.coreOfTheVoid)
				.withAdvancement(mainAdvancement(LibAdvancementNames.THE_SOURCE_OF_HONKAI))
				.pages(
						text("extrabotany.page.core_of_the_void0"),
						text("extrabotany.page.core_of_the_void1")

				)
				.extraRecipeMapping(ExtraBotanyItems.coreOfTheVoid, 0)
				.save(consumer, id("core_of_the_void"));
		PatchouliBuilder.entry(CATEGORY)
				.withName("extrabotany.entry.gaia_3rd")
				.withIcon(ExtraBotanyItems.challengeTicket)
				.withAdvancement(mainAdvancement(LibAdvancementNames.STYGIAN_TWINS))
				.pages(
						text("extrabotany.page.gaia_3rd0"),
						crafting(ExtraBotanyItems.challengeTicket)
								.withText("extrabotany.page.gaia_3rd1"),
						text("extrabotany.page.gaia_3rd2"),
						text("extrabotany.page.gaia_3rd3"),
						text("extrabotany.page.gaia_3rd4")
				)
				.save(consumer, id("gaia_3rd"));
		// 本我。文字来自原版 1.16 的条目，按现在的召唤方式和阶段数值改写。原版的图标是一个这里没有的物品，所以用玩家的头：本我就是玩家自己。
		// The Ego. The text comes from the entry of the original 1.16, written again for the summon and the stage values of today. The icon of the original is an item that is not here, thus a player head is used: the Ego is the player.
		PatchouliBuilder.entry(CATEGORY)
				.withName("entity.extrabotany.ego")
				.withIcon(Items.PLAYER_HEAD)
				.withAdvancement(botaniaAdvancement("gaia_guardian_kill"))
				.pages(
						text("extrabotany.page.ego0"),
						text("extrabotany.page.ego1"),
						text("extrabotany.page.ego2"),
						text("extrabotany.page.ego3")
				)
				.save(consumer, id("ego"));
		PatchouliBuilder.entry(CATEGORY)
				.withName("extrabotany.entry.yin_and_yang")
				.withIcon(ExtraBotanyItems.shadowium)
				//.withAdvancement(ResourceLocation.tryParse("botania:main/elf_portal_open"))
				.pages(
						text("extrabotany.page.yin_and_yang0"),
						text("extrabotany.page.yin_and_yang1"),
						runicAlter(ExtraBotanyItems.photonium)
								.withText("extrabotany.page.yin_and_yang2"),
						runicAlter(ExtraBotanyItems.shadowium)
								.withText("extrabotany.page.yin_and_yang3")
				)
				.extraRecipeMapping(ExtraBotanyItems.photoniumNugget, 2)
				.extraRecipeMapping(ExtraBotanyBlocks.photoniumBlock, 2)
				.extraRecipeMapping(ExtraBotanyItems.shadowiumNugget, 3)
				.extraRecipeMapping(ExtraBotanyBlocks.shadowiumBlock, 3)
				.save(consumer, id("yin_and_yang"));
		PatchouliBuilder.entry(CATEGORY)
				.withName("extrabotany.entry.pandoras_box")
				.withIcon(ExtraBotanyItems.pandorasBox)
				.withAdvancement(mainAdvancement(LibAdvancementNames.GAIA_TRIAL))
				.pages(
						spotlight(ExtraBotanyItems.pandorasBox)
								.withText("extrabotany.page.pandoras_box0"),

						empty(),
						text("extrabotany.page.pandoras_box1")
								.withAdvancement(mainAdvancement(LibAdvancementNames.THE_SOURCE_OF_HONKAI)),
						spotlight(ExtraBotanyItems.heroMedal)
								.withText("extrabotany.page.pandoras_box2")
								.withAdvancement(mainAdvancement(LibAdvancementNames.THE_SOURCE_OF_HONKAI))
				)
				.save(consumer, id("pandoras_box"));
		PatchouliBuilder.entry(CATEGORY)
				.withName("extrabotany.entry.reward_bags")
				.withIcon(ExtraBotanyItems.zweiRewardBag)
				.withAdvancement(mainAdvancement(LibAdvancementNames.THE_SOURCE_OF_HONKAI))
				.pages(
						text("extrabotany.page.reward_bags0"),
						spotlight(ExtraBotanyItems.einsRewardBag)
								.withText("extrabotany.page.reward_bags1"),
						spotlight(ExtraBotanyItems.zweiRewardBag)
								.withText("extrabotany.page.reward_bags2"),
						spotlight(ExtraBotanyItems.dreiRewardBag)
								.withText("extrabotany.page.reward_bags3"),
						spotlight(ExtraBotanyItems.vierRewardBag)
								.withText("extrabotany.page.reward_bags4"),
						crafting(ExtraBotanyItems.nineAndThreeQuartersRewardBag)
								.withText("extrabotany.page.reward_bags5")

				)
				.save(consumer, id("reward_bags"));
		PatchouliBuilder.entry(MANA)
				.withName("extrabotany.entry.mana_charger")
				.withIcon(ExtraBotanyBlocks.manaCharger)
				.pages(
						text("extrabotany.page.mana_charger0"),
						crafting(ExtraBotanyBlocks.manaCharger)
								.withText("extrabotany.page.mana_charger1")
				)
				.save(consumer, id("mana_charger"));
		PatchouliBuilder.entry(CATEGORY)
				.withName("extrabotany.entry.dimension_catalyst")
				.withIcon(ExtraBotanyBlocks.dimensionCatalyst)
				.pages(
						text("extrabotany.page.dimension_catalyst0"),
						crafting(ExtraBotanyBlocks.dimensionCatalyst).withText("extrabotany.page.dimension_catalyst11"),
						manaInfusion("apple_to_chorus_fruit").withText("extrabotany.page.dimension_catalyst1"),
						manaInfusion("blaze_rod_dupe").withText("extrabotany.page.dimension_catalyst2"),
						manaInfusion("cobblestone_to_nether_rack").withText("extrabotany.page.dimension_catalyst3"),
						manaInfusion("diamond_horse_armor_to_shulker_shell").withText("extrabotany.page.dimension_catalyst4"),
						manaInfusion("snowball_to_ender_pearl").withText("extrabotany.page.dimension_catalyst5"),
						manaInfusion("iron_ore_to_quartz_ore").withText("extrabotany.page.dimension_catalyst6"),
						manaInfusion("nether_star_to_totem_of_undying").withText("extrabotany.page.dimension_catalyst7"),
						manaInfusion("sand_to_soul_sand").withText("extrabotany.page.dimension_catalyst8"),
						manaInfusion("stone_to_end_stone").withText("extrabotany.page.dimension_catalyst9"),
						manaInfusion("the_origin_to_elytra").withText("extrabotany.page.dimension_catalyst10")
				)
				.save(consumer, id("dimension_catalyst"));
		PatchouliBuilder.entry(CATEGORY)
				.withName("extrabotany.entry.quartzs")
				.withIcon(ExtraBotanyItems.gaiaQuartz)
				.pages(
						text("extrabotany.page.quartzs0"),
						crafting(ExtraBotanyItems.gaiaQuartz).withText("extrabotany.page.quartzs1"),
						elvenTrade(ExtraBotanyItems.elementiumQuartz).withText("extrabotany.page.quartzs2")
				)
				.extraRecipeMapping(ExtraBotanyBlocks.gaiaQuartzBlock, 1)
				.extraRecipeMapping(ExtraBotanyBlocks.gaiaQuartzStairs, 1)
				.extraRecipeMapping(ExtraBotanyBlocks.gaiaQuartzSlab, 1)
				.extraRecipeMapping(ExtraBotanyBlocks.chiseledGaiaQuartzBlock, 1)
				.extraRecipeMapping(ExtraBotanyBlocks.gaiaQuartzBricks, 1)
				.extraRecipeMapping(ExtraBotanyBlocks.gaiaQuartzPillar, 1)
				.extraRecipeMapping(ExtraBotanyBlocks.smoothGaiaQuartz, 1)
				.extraRecipeMapping(ExtraBotanyBlocks.smoothGaiaQuartzStairs, 1)
				.extraRecipeMapping(ExtraBotanyBlocks.smoothGaiaQuartzSlab, 1)

				.extraRecipeMapping(ExtraBotanyBlocks.elementiumQuartzBlock, 2)
				.extraRecipeMapping(ExtraBotanyBlocks.elementiumQuartzStairs, 2)
				.extraRecipeMapping(ExtraBotanyBlocks.elementiumQuartzSlab, 2)
				.extraRecipeMapping(ExtraBotanyBlocks.chiseledElementiumQuartzBlock, 2)
				.extraRecipeMapping(ExtraBotanyBlocks.elementiumQuartzBricks, 2)
				.extraRecipeMapping(ExtraBotanyBlocks.elementiumQuartzPillar, 2)
				.extraRecipeMapping(ExtraBotanyBlocks.smoothElementiumQuartz, 2)
				.extraRecipeMapping(ExtraBotanyBlocks.smoothElementiumQuartzStairs, 2)
				.extraRecipeMapping(ExtraBotanyBlocks.smoothElementiumQuartzSlab, 2)
				.save(consumer, id("quartzs"));
		PatchouliBuilder.entry(FUNCTIONAL_FLOWERS)
				.withName("extrabotany.entry.trade_orchid")
				.withIcon(ExtrabotanyFlowerBlocks.tradeOrchid)
				.pages(
						text("extrabotany.page.trade_orchid0"),
						petal(ExtrabotanyFlowerBlocks.tradeOrchid)
								.withText("extrabotany.page.trade_orchid1")
				)
				.extraRecipeMapping(ExtrabotanyFlowerBlocks.tradeOrchidFloating, 1)
				.save(consumer, id("trade_orchid"));
		PatchouliBuilder.entry(CATEGORY)
				.withName("extrabotany.entry.nature_orb")
				.withIcon(ExtraBotanyItems.natureOrb)
				.pages(
						text("extrabotany.page.nature_orb0"),
						text("extrabotany.page.nature_orb1"),
						crafting(ExtraBotanyItems.natureOrb).withText("extrabotany.page.nature_orb2"),
						text("extrabotany.page.nature_orb3"),
						multiBlock("extrabotany.multiblock.nature_pedestal_tier1", PedestalBlockEntity.TIER1_PATTERN)
								.withMapping("P", RegistryHelper.getRegistryName(BotaniaBlocks.NATURA_PYLON).toString())
								.withMapping("0", RegistryHelper.getRegistryName(ExtraBotanyBlocks.livingrockPedestal).toString()),
						multiBlock("extrabotany.multiblock.nature_pedestal_tier2", PedestalBlockEntity.TIER2_PATTERN)
								.withMapping("P", RegistryHelper.getRegistryName(BotaniaBlocks.NATURA_PYLON).toString())
								.withMapping("0", RegistryHelper.getRegistryName(ExtraBotanyBlocks.livingrockPedestal).toString())
								.withMapping("S", RegistryHelper.getRegistryName(BotaniaBlocks.SHIMMERROCK).toString())
								.withMapping("M", RegistryHelper.getRegistryName(BotaniaBlocks.MANA_POOL).toString())
				)
				.withAdvancement(botaniaAdvancement("terrasteel_pickup"))
				.save(consumer, id("nature_orb"));
		PatchouliBuilder.entry(CATEGORY)
				.withName("extrabotany.entry.void_archives")
				.withIcon(ExtraBotanyItems.voidArchives)
				.pages(
						spotlight(ExtraBotanyItems.voidArchives).withText("extrabotany.page.void_archives0"),
						text("extrabotany.page.void_archives1")
				)
				.withAdvancement(mainAdvancement(LibAdvancementNames.THE_ORIGINAL_DIVINE_KEY))
				.save(consumer, id("void_archives"));
		PatchouliBuilder.entry(CATEGORY)
				.withName("extrabotany.entry.glided_potato")
				.withIcon(ExtraBotanyItems.gildedPotato)
				.pages(
						text("extrabotany.page.glided_potato0"),
						runicAlter(ExtraBotanyItems.gildedPotato).withText("extrabotany.page.glided_potato1")

				)
				.save(consumer, id("glided_potato"));
		PatchouliBuilder.entry(CATEGORY)
				.withName("extrabotany.entry.universe_medium")
				.withIcon(ExtraBotanyItems.theUniverse)
				.pages(
						text("extrabotany.page.universe_medium0"),
						text("extrabotany.page.universe_medium1"),
						crafting(ExtraBotanyItems.theOrigin).withText("extrabotany.page.universe_medium2"),
						crafting(ExtraBotanyItems.theEnd).withText("extrabotany.page.universe_medium3"),
						crafting(ExtraBotanyItems.theChaos).withText("extrabotany.page.universe_medium4"),
						terraPlate(ExtraBotanyItems.theUniverse).withText("extrabotany.page.universe_medium5")

				)
				.save(consumer, id("universe_medium"));
		PatchouliBuilder.entry(CATEGORY)
				.withName("extrabotany.entry.orichalcos")
				.withIcon(ExtraBotanyItems.orichalcos)
				.pages(
						text("extrabotany.page.orichalcos0"),
						runicAlter(ExtraBotanyItems.orichalcos).withText("extrabotany.page.orichalcos1")
				)
				.extraRecipeMapping(ExtraBotanyBlocks.orichalcosBlock, 0)
				.extraRecipeMapping(ExtraBotanyItems.orichalcosNugget, 0)
				.save(consumer, id("orichalcos"));
		PatchouliBuilder.entry(CATEGORY)
				.withName("extrabotany.entry.aerialite")
				.withIcon(ExtraBotanyItems.aerialite)
				.pages(
						text("extrabotany.page.aerialite0"),
						terraPlate(ExtraBotanyItems.aerialite).withText("extrabotany.page.aerialite1")
				)
				.extraRecipeMapping(ExtraBotanyBlocks.aerialiteBlock, 0)
				.extraRecipeMapping(ExtraBotanyItems.aerialiteNugget, 0)
				.save(consumer, id("aerialite"));
		PatchouliBuilder.entry(CATEGORY)
				.withName("extrabotany.entry.das_rheingold")
				.withIcon(ExtraBotanyItems.dasRheingold)
				.pages(
						text("extrabotany.page.das_rheingold0"),
						text("extrabotany.page.das_rheingold1"),
						crafting(ExtraBotanyItems.dasRheingold).withText("extrabotany.page.das_rheingold2")
				)
				.save(consumer, id("das_rheingold"));
		PatchouliBuilder.entry(CATEGORY)
				.withName("extrabotany.entry.starry_idol")
				.withIcon(ExtraBotanyItems.starryIdolHeadgear)
				.pages(
						text("extrabotany.page.starry_idol0"),
						crafting(ExtraBotanyItems.starryIdolHeadgear).withText("extrabotany.page.starry_idol1"),
						crafting(ExtraBotanyItems.starryIdolSuit).withText("extrabotany.page.starry_idol2"),
						crafting(ExtraBotanyItems.starryIdolSkirt).withText("extrabotany.page.starry_idol3"),
						crafting(ExtraBotanyItems.starryIdolBoots).withText("extrabotany.page.starry_idol4"),
						text("extrabotany.page.starry_idol5")
								.withFlags("otaku_mode")
				)
				.save(consumer, id("starry_idol"));
		PatchouliBuilder.entry(CATEGORY)
				.withName("extrabotany.entry.pleiades_combat_maid")
				.withIcon(ExtraBotanyItems.pleiadesCombatMaidSuit)
				.pages(
						text("extrabotany.page.pleiades_combat_maid0"),
						crafting(ExtraBotanyItems.pleiadesCombatMaidHeadgear).withText("extrabotany.page.pleiades_combat_maid1"),
						crafting(ExtraBotanyItems.pleiadesCombatMaidSuit).withText("extrabotany.page.pleiades_combat_maid2"),
						crafting(ExtraBotanyItems.pleiadesCombatMaidSkirt).withText("extrabotany.page.pleiades_combat_maid3"),
						crafting(ExtraBotanyItems.pleiadesCombatMaidBoots).withText("extrabotany.page.pleiades_combat_maid4"),
						text("extrabotany.page.pleiades_combat_maid5"),
						text("extrabotany.page.pleiades_combat_maid6")
								.withAdvancement(mainAdvancement(LibAdvancementNames.CORRUPTION)),
						spotlight(ExtraBotanyItems.sanguinePleiadesCombatMaidSuit).withText("extrabotany.page.pleiades_combat_maid7")
								.withAdvancement(mainAdvancement(LibAdvancementNames.CORRUPTION)),
						text("extrabotany.page.pleiades_combat_maid8")
								.withFlags("otaku_mode")

				)
				.withAdvancement(mainAdvancement(LibAdvancementNames.STYGIAN_TWINS))
				.save(consumer, id("pleiades_combat_maid"));
		PatchouliBuilder.entry(CATEGORY)
				.withName("extrabotany.entry.shadow_warrior")
				.withIcon(ExtraBotanyItems.shadowWarriorHelmet)
				.pages(
						text("extrabotany.page.shadow_warrior0"),
						crafting(ExtraBotanyItems.shadowWarriorHelmet).withText("extrabotany.page.shadow_warrior1"),
						crafting(ExtraBotanyItems.shadowWarriorChestplate).withText("extrabotany.page.shadow_warrior2"),
						crafting(ExtraBotanyItems.shadowWarriorLeggings).withText("extrabotany.page.shadow_warrior3"),
						crafting(ExtraBotanyItems.shadowWarriorBoots).withText("extrabotany.page.shadow_warrior4")
				)
				.withAdvancement(mainAdvancement(LibAdvancementNames.STYGIAN_TWINS))
				.save(consumer, id("shadow_warrior"));
		PatchouliBuilder.entry(CATEGORY)
				.withName("extrabotany.entry.goblin_slayer")
				.withIcon(ExtraBotanyItems.goblinSlayerHelmet)
				.pages(
						text("extrabotany.page.goblin_slayer0"),
						crafting(ExtraBotanyItems.goblinSlayerHelmet).withText("extrabotany.page.goblin_slayer1"),
						crafting(ExtraBotanyItems.goblinSlayerChestplate).withText("extrabotany.page.goblin_slayer2"),
						crafting(ExtraBotanyItems.goblinSlayerLeggings).withText("extrabotany.page.goblin_slayer3"),
						crafting(ExtraBotanyItems.goblinSlayerBoots).withText("extrabotany.page.goblin_slayer4")
				)
				.withAdvancement(mainAdvancement(LibAdvancementNames.STYGIAN_TWINS))
				.save(consumer, id("goblin_slayer"));
		PatchouliBuilder.entry(FUNCTIONAL_FLOWERS)
				.withName("extrabotany.entry.woodienia")
				.withIcon(ExtrabotanyFlowerBlocks.woodienia)
				.pages(
						text("extrabotany.page.woodienia0"),
						petal(ExtrabotanyFlowerBlocks.woodienia).withText("extrabotany.page.woodienia1")
				)
				.extraRecipeMapping(ExtrabotanyFlowerBlocks.woodieniaFloating, 1)
				.save(consumer, id("woodienia"));
		PatchouliBuilder.entry(GENERATING_FLOWERS)
				.withName("extrabotany.entry.reikarlily")
				.withIcon(ExtrabotanyFlowerBlocks.reikarlily)
				.pages(
						text("extrabotany.page.reikarlily0"),
						text("extrabotany.page.reikarlily1"),
						petal(ExtrabotanyFlowerBlocks.reikarlily).withText("extrabotany.page.reikarlily2")
				)
				.extraRecipeMapping(ExtrabotanyFlowerBlocks.reikarlilyFloating, 2)
				.save(consumer, id("reikarlily"));
		PatchouliBuilder.entry(GENERATING_FLOWERS)
				.withName("extrabotany.entry.bellflower")
				.withIcon(ExtrabotanyFlowerBlocks.bellflower)
				.pages(
						text("extrabotany.page.bellflower0"),
						text("extrabotany.page.bellflower1"),
						petal(ExtrabotanyFlowerBlocks.bellflower).withText("extrabotany.page.bellflower2")
				)
				.extraRecipeMapping(ExtrabotanyFlowerBlocks.bellflowerFloating, 2)
				.save(consumer, id("bellflower"));
		PatchouliBuilder.entry(FUNCTIONAL_FLOWERS)
				.withName("extrabotany.entry.annoyingflower")
				.withIcon(ExtrabotanyFlowerBlocks.annoyingflower)
				.pages(
						text("extrabotany.page.annoyingflower0"),
						petal(ExtrabotanyFlowerBlocks.annoyingflower).withText("extrabotany.page.annoyingflower1")
				)
				.extraRecipeMapping(ExtrabotanyFlowerBlocks.annoyingflowerFloating, 1)
				.save(consumer, id("annoyingflower"));
		PatchouliBuilder.entry(CATEGORY)
				.withName("extrabotany.entry.fried_chicken")
				.withIcon(ExtraBotanyItems.friedChicken)
				.pages(
						text("extrabotany.page.fried_chicken0"),
						manaInfusion(ExtraBotanyItems.friedChicken).withText("extrabotany.page.fried_chicken1")
				)
				.save(consumer, id("fried_chicken"));
		PatchouliBuilder.entry(CATEGORY)
				.withName("extrabotany.entry.more_brews")
				.withIcon(Items.AIR)
				.pages(
						text("extrabotany.page.more_brews0"),
						brew(ExtraBotanyBrews.revolution)
								.withText("extrabotany.page.more_brews1a")
								.withFlavor("extrabotany.page.more_brews1b"),
						brew(ExtraBotanyBrews.shield)
								.withText("extrabotany.page.more_brews2a")
								.withFlavor("extrabotany.page.more_brews2b"),
						brew(ExtraBotanyBrews.allInOne)
								.withText("extrabotany.page.more_brews3a")
								.withFlavor("extrabotany.page.more_brews3b"),
						brew(ExtraBotanyBrews.deadpool)
								.withText("extrabotany.page.more_brews4a")
								.withFlavor("extrabotany.page.more_brews4b"),
						brew(ExtraBotanyBrews.floating)
								.withText("extrabotany.page.more_brews5a")
								.withFlavor("extrabotany.page.more_brews5b")
				)
				.save(consumer, id("more_brews"));
		PatchouliBuilder.entry(GENERATING_FLOWERS)
				.withName("extrabotany.entry.stonesia")
				.withIcon(ExtrabotanyFlowerBlocks.stonesia)
				.pages(
						text("extrabotany.page.stonesia0"),
						petal(ExtrabotanyFlowerBlocks.stonesia).withText("extrabotany.page.stonesia1")
				)
				.extraRecipeMapping(ExtrabotanyFlowerBlocks.stonesiaFloating, 1)
				.save(consumer, id("stonesia"));
		PatchouliBuilder.entry(GENERATING_FLOWERS)
				.withName("extrabotany.entry.edelweiss")
				.withIcon(ExtrabotanyFlowerBlocks.edelweiss)
				.pages(
						text("extrabotany.page.edelweiss0"),
						petal(ExtrabotanyFlowerBlocks.edelweiss).withText("extrabotany.page.edelweiss1")
				)
				.extraRecipeMapping(ExtrabotanyFlowerBlocks.edelweissFloating, 1)
				.save(consumer, id("edelweiss"));
		PatchouliBuilder.entry(GENERATING_FLOWERS)
				.withName("extrabotany.entry.resoncund")
				.withIcon(ExtrabotanyFlowerBlocks.resoncund)
				.pages(
						text("extrabotany.page.resoncund0"),
						petal(ExtrabotanyFlowerBlocks.resoncund).withText("extrabotany.page.resoncund1")
				)
				.extraRecipeMapping(ExtrabotanyFlowerBlocks.resoncundFloating, 1)
				.save(consumer, id("resoncund"));
		PatchouliBuilder.entry(BAUBLES)
				.withName("extrabotany.entry.frost_ring")
				.withIcon(ExtraBotanyItems.frostRing)
				.pages(
						text("extrabotany.page.frost_ring0"),
						crafting(ExtraBotanyItems.frostRing).withText("extrabotany.page.frost_ring1")
				)
				.save(consumer, id("frost_ring"));
		PatchouliBuilder.entry(BAUBLES)
				.withName("extrabotany.entry.dispersive_ring")
				.withIcon(ExtraBotanyItems.dispersiveRing)
				.pages(
						text("extrabotany.page.dispersive_ring0"),
						crafting(ExtraBotanyItems.dispersiveRing).withText("extrabotany.page.dispersive_ring1")
				)
				.save(consumer, id("dispersive_ring"));
		PatchouliBuilder.entry(BAUBLES)
				.withName("extrabotany.entry.curse_ring")
				.withIcon(ExtraBotanyItems.curseRing)
				.pages(
						text("extrabotany.page.curse_ring0"),
						crafting(ExtraBotanyItems.curseRing).withText("extrabotany.page.curse_ring1")
				)
				.save(consumer, id("curse_ring"));
		PatchouliBuilder.entry(BAUBLES)
				.withName("extrabotany.entry.pure_daisy_pendant")
				.withIcon(ExtraBotanyItems.pureDaisyPendant)
				.pages(
						text("extrabotany.page.pure_daisy_pendant0"),
						crafting(ExtraBotanyItems.pureDaisyPendant).withText("extrabotany.page.pure_daisy_pendant1")
				)
				.save(consumer, id("pure_daisy_pendant"));
		PatchouliBuilder.entry(BAUBLES)
				.withName("extrabotany.entry.spider_ring")
				.withIcon(ExtraBotanyItems.spiderRing)
				.pages(
						text("extrabotany.page.spider_ring0"),
						crafting(ExtraBotanyItems.spiderRing).withText("extrabotany.page.spider_ring1")
				)
				.save(consumer, id("spider_ring"));
		PatchouliBuilder.entry(BAUBLES)
				.withName("extrabotany.entry.jackie_chan_ring")
				.withIcon(ExtraBotanyItems.jackieChanRing)
				.pages(
						text("extrabotany.page.jackie_chan_ring0"),
						crafting(ExtraBotanyItems.jackieChanRing).withText("extrabotany.page.jackie_chan_ring1")
				)
				.save(consumer, id("jackie_chan_ring"));
		PatchouliBuilder.entry(BAUBLES)
				.withName("extrabotany.entry.parkour_ring")
				.withIcon(ExtraBotanyItems.parkourRing)
				.pages(
						text("extrabotany.page.parkour_ring0"),
						crafting(ExtraBotanyItems.parkourRing).withText("extrabotany.page.parkour_ring1")
				)
				.save(consumer, id("parkour_ring"));
		PatchouliBuilder.entry(GENERATING_FLOWERS)
				.withName("extrabotany.entry.sunshine_lily")
				.withIcon(ExtrabotanyFlowerBlocks.sunshineLily)
				.pages(
						text("extrabotany.page.sunshine_lily0"),
						petal(ExtrabotanyFlowerBlocks.sunshineLily).withText("extrabotany.page.sunshine_lily1")
				)
				.extraRecipeMapping(ExtrabotanyFlowerBlocks.sunshineLilyFloating, 1)
				.save(consumer, id("sunshine_lily"));
		PatchouliBuilder.entry(GENERATING_FLOWERS)
				.withName("extrabotany.entry.moonlight_lily")
				.withIcon(ExtrabotanyFlowerBlocks.moonlightLily)
				.pages(
						text("extrabotany.page.moonlight_lily0"),
						petal(ExtrabotanyFlowerBlocks.moonlightLily).withText("extrabotany.page.moonlight_lily1")
				)
				.extraRecipeMapping(ExtrabotanyFlowerBlocks.moonlightLilyFloating, 1)
				.save(consumer, id("moonlight_lily"));
		PatchouliBuilder.entry(FUNCTIONAL_FLOWERS)
				.withName("extrabotany.entry.serenitian")
				.withIcon(ExtrabotanyFlowerBlocks.serenitian)
				.pages(
						text("extrabotany.page.serenitian0"),
						petal(ExtrabotanyFlowerBlocks.serenitian).withText("extrabotany.page.serenitian1")
				)
				.extraRecipeMapping(ExtrabotanyFlowerBlocks.serenitianFloating, 1)
				.save(consumer, id("serenitian"));
		PatchouliBuilder.entry(GENERATING_FLOWERS)
				.withName("extrabotany.entry.twinstar")
				.withIcon(ExtrabotanyFlowerBlocks.twinstar)
				.pages(
						text("extrabotany.page.twinstar0"),
						petal(ExtrabotanyFlowerBlocks.twinstar).withText("extrabotany.page.twinstar1")
				)
				.extraRecipeMapping(ExtrabotanyFlowerBlocks.twinstarFloating, 1)
				.save(consumer, id("twinstar"));
		PatchouliBuilder.entry(GENERATING_FLOWERS)
				.withName("extrabotany.entry.omniviolet")
				.withIcon(ExtrabotanyFlowerBlocks.omniviolet)
				.pages(
						text("extrabotany.page.omniviolet0"),
						petal(ExtrabotanyFlowerBlocks.omniviolet).withText("extrabotany.page.omniviolet1")
				)
				.extraRecipeMapping(ExtrabotanyFlowerBlocks.omnivioletFloating, 1)
				.save(consumer, id("omniviolet"));
		PatchouliBuilder.entry(GENERATING_FLOWERS)
				.withName("extrabotany.entry.tinkle")
				.withIcon(ExtrabotanyFlowerBlocks.tinkle)
				.pages(
						text("extrabotany.page.tinkle0"),
						petal(ExtrabotanyFlowerBlocks.tinkle).withText("extrabotany.page.tinkle1")
				)
				.extraRecipeMapping(ExtrabotanyFlowerBlocks.tinkleFloating, 1)
				.save(consumer, id("tinkle"));
		PatchouliBuilder.entry(GENERATING_FLOWERS)
				.withName("extrabotany.entry.blood_enchantress")
				.withIcon(ExtrabotanyFlowerBlocks.bloodEnchantress)
				.pages(
						text("extrabotany.page.blood_enchantress0"),
						petal(ExtrabotanyFlowerBlocks.bloodEnchantress).withText("extrabotany.page.blood_enchantress1")
				)
				.extraRecipeMapping(ExtrabotanyFlowerBlocks.bloodEnchantressFloating, 1)
				.save(consumer, id("blood_enchantress"));
		PatchouliBuilder.entry(FUNCTIONAL_FLOWERS)
				.withName("extrabotany.entry.mirrowtunia")
				.withIcon(ExtrabotanyFlowerBlocks.mirrowtunia)
				.pages(
						text("extrabotany.page.mirrowtunia0"),
						petal(ExtrabotanyFlowerBlocks.mirrowtunia).withText("extrabotany.page.mirrowtunia1")
				)
				.extraRecipeMapping(ExtrabotanyFlowerBlocks.mirrowtuniaFloating, 1)
				.save(consumer, id("mirrowtunia"));
		PatchouliBuilder.entry(FUNCTIONAL_FLOWERS)
				.withName("extrabotany.entry.necrofleur")
				.withIcon(ExtrabotanyFlowerBlocks.necrofleur)
				.pages(
						text("extrabotany.page.necrofleur0"),
						petal(ExtrabotanyFlowerBlocks.necrofleur).withText("extrabotany.page.necrofleur1"),
						text("extrabotany.page.necrofleur2"),
						manaInfusion(ExtrabotanyFlowerBlocks.necrofleurChibi).withText("extrabotany.page.necrofleur3")
				)
				.extraRecipeMapping(ExtrabotanyFlowerBlocks.necrofleurFloating, 1)
				.extraRecipeMapping(ExtrabotanyFlowerBlocks.necrofleurChibiFloating, 3)
				.save(consumer, id("necrofleur"));
		PatchouliBuilder.entry(MANA)
				.withName("extrabotany.entry.manalink")
				.withIcon(ExtrabotanyFlowerBlocks.manalink)
				.pages(
						text("extrabotany.page.manalink0"),
						multiBlock("extrabotany.multiblock.manalink", ManalinkBlockEntity.PATCHOULI_PATTERN)
								.withMapping("D", RegistryHelper.getRegistryName(Blocks.GRASS_BLOCK).toString())
								.withMapping("Q", RegistryHelper.getRegistryName(Blocks.QUARTZ_BLOCK).toString())
								.withMapping("L", RegistryHelper.getRegistryName(Blocks.LAPIS_BLOCK).toString())
								.withMapping("O", RegistryHelper.getRegistryName(Blocks.OBSIDIAN).toString())
								.withMapping("0", RegistryHelper.getRegistryName(ExtrabotanyFlowerBlocks.manalink).toString())
								.withMapping("A", RegistryHelper.getRegistryName(Blocks.AIR).toString()),
						text("extrabotany.page.manalink1"),
						petal(ExtrabotanyFlowerBlocks.manalink).withText("extrabotany.page.manalink2")
				)
				.extraRecipeMapping(ExtrabotanyFlowerBlocks.manalinkFloating, 1)
				.priority()
				.save(consumer, id("manalink"));
		PatchouliBuilder.entry(FUNCTIONAL_FLOWERS)
				.withName("extrabotany.entry.enchanter")
				.withIcon(ExtrabotanyFlowerBlocks.enchanter)
				.pages(
						text("extrabotany.page.enchanter0"),
						petal(ExtrabotanyFlowerBlocks.enchanter).withText("extrabotany.page.enchanter1")
				)
				.extraRecipeMapping(ExtrabotanyFlowerBlocks.enchanterFloating, 1)
				.save(consumer, id("enchanter"));
		PatchouliBuilder.entry(CATEGORY)
				.withName("extrabotany.entry.shields")
				.withIcon(ExtraBotanyItems.manasteelShield)
				.pages(
						text("extrabotany.page.shields0"),
						crafting(ExtraBotanyItems.manasteelShield).withText("extrabotany.page.shields1"),
						text("extrabotany.page.shields2"),
						crafting(ExtraBotanyItems.elementiumShield).withText("extrabotany.page.shields3"),
						text("extrabotany.page.shields4"),
						crafting(ExtraBotanyItems.terrasteelShield).withText("extrabotany.page.shields5")
				)
				.save(consumer, id("shields"));
		PatchouliBuilder.entry(CATEGORY)
				.withName("extrabotany.entry.achille_shield")
				.withIcon(ExtraBotanyItems.achillesShield)
				.pages(
						text("extrabotany.page.achille_shield0"),
						text("extrabotany.page.achille_shield1"),
						smithing(ExtraBotanyItems.achillesShield).withText("extrabotany.page.achille_shield2")
				)
				.save(consumer, id("achille_shield"));
		PatchouliBuilder.entry(CATEGORY)
				.withName("extrabotany.entry.records")
				.withIcon(ExtraBotanyItems.recordGaia3)
				.pages(
						text("extrabotany.page.records0"),
						spotlight(ExtraBotanyItems.recordGaia3).withText("extrabotany.page.records1"),
						spotlight(ExtraBotanyItems.recordEgo).withText("extrabotany.page.records2")
				)
				.save(consumer, id("records"));
		PatchouliBuilder.entry(CATEGORY)
				.withName("extrabotany.entry.more_lens")
				.withIcon(ExtraBotanyItems.lensMana)
				.pages(
						text("extrabotany.page.more_lens0"),
						crafting(ExtraBotanyItems.lensPush).withText("extrabotany.page.more_lens1"),
						text("extrabotany.page.more_lens2"),
						crafting(ExtraBotanyItems.lensSmelt).withText("extrabotany.page.more_lens3"),
						text("extrabotany.page.more_lens4"),
						crafting(ExtraBotanyItems.lensMana).withText("extrabotany.page.more_lens5"),
						text("extrabotany.page.more_lens6"),
						crafting(ExtraBotanyItems.lensPotion).withText("extrabotany.page.more_lens7"),
						text("extrabotany.page.more_lens8"),
						crafting(ExtraBotanyItems.lensTrace).withText("extrabotany.page.more_lens9")
				)
				.save(consumer, id("more_lens"));

	}

	private BrewPage brew(Brew brew) {
		ResourceLocation location = BotaniaAPI.instance().getBrewRegistry().getKey(brew);
		ResourceLocation recipeLocation = location.withPrefix("brew/");
		return new BrewPage(recipeLocation.toString());
	}

	private RunicAltarPage runicAlter(ItemLike item) {
		ResourceLocation location = BuiltInRegistries.ITEM.getKey(item.asItem());
		ResourceLocation recipeLocation = location.withPrefix("runic_altar/");
		return new RunicAltarPage(recipeLocation.toString());
	}

	private PetalApothecaryPage petal(ItemLike item) {
		ResourceLocation location = BuiltInRegistries.ITEM.getKey(item.asItem());
		ResourceLocation recipeLocation = location.withPrefix("petal_apothecary/");
		return new PetalApothecaryPage(recipeLocation.toString());
	}

	private PedestalPage pedestal(ItemLike item) {
		ResourceLocation location = BuiltInRegistries.ITEM.getKey(item.asItem());
		ResourceLocation recipeLocation = location.withPrefix("pedestal_smash/");
		return new PedestalPage(recipeLocation.toString());
	}

	private ManaInfusionPage manaInfusion(ItemLike item) {
		ResourceLocation location = BuiltInRegistries.ITEM.getKey(item.asItem());
		ResourceLocation recipeLocation = location.withPrefix("mana_infusion/");
		return new ManaInfusionPage(recipeLocation.toString());
	}

	private ManaInfusionPage manaInfusion(String recipe) {
		ResourceLocation resourceLocation = prefix("mana_infusion/" + recipe);
		return new ManaInfusionPage(resourceLocation.toString());
	}

	private ElvenTradePage elvenTrade(ItemLike item) {
		ResourceLocation location = BuiltInRegistries.ITEM.getKey(item.asItem());
		ResourceLocation recipeLocation = location.withPrefix("elven_trade/");
		return new ElvenTradePage(recipeLocation.toString());
	}

	private TerraPlatePage terraPlate(ItemLike item) {
		ResourceLocation location = BuiltInRegistries.ITEM.getKey(item.asItem());
		ResourceLocation recipeLocation = location.withPrefix("terra_plate/");
		return new TerraPlatePage(recipeLocation.toString());
	}

	private SmithingPage smithing(ItemLike item) {
		ResourceLocation location = BuiltInRegistries.ITEM.getKey(item.asItem());
		ResourceLocation recipeLocation = location.withPrefix("smithing/");
		return new SmithingPage(recipeLocation.toString());
	}

	/**
	 * 原版 1.12 图鉴里有、这个移植版以前没有的条目。文字来自原版的语言文件，只改了名字和已经不同的行为。
	 * The entries that the lexicon of the original 1.12 has and that this port did not have before. The text comes from the language files of the original; only the names and the behavior that is different now were changed.
	 */
	private void addOriginalEntries(Consumer<PatchouliEntry> consumer) {
		// 花在第三层的中间。四个角是两格青金石块加一个海晶灯，最下面一层是石英块的外圈。
		// The flower is in the middle of the third layer. Each corner has two lapis blocks and one sea lantern, and the lowest layer is the outer ring of quartz blocks.
		String[][] lotusPattern = {
				{ "S___S", "_____", "_____", "_____", "S___S" },
				{ "L___L", "_____", "_____", "_____", "L___L" },
				{ "L___L", "_____", "__0__", "_____", "L___L" },
				{ "QQQQQ", "Q___Q", "Q___Q", "Q___Q", "QQQQQ" }
		};
		PatchouliBuilder.entry(FUNCTIONAL_FLOWERS)
				.withName("block.extrabotany.stardustlotus")
				.withIcon(ExtrabotanyFlowerBlocks.stardustLotus)
				.pages(
						text("extrabotany.page.stardustlotus0"),
						petal(ExtrabotanyFlowerBlocks.stardustLotus).withText("extrabotany.page.stardustlotus1"),
						multiBlock("block.extrabotany.stardustlotus", lotusPattern)
								.withMapping("Q", RegistryHelper.getRegistryName(Blocks.QUARTZ_BLOCK).toString())
								.withMapping("L", RegistryHelper.getRegistryName(Blocks.LAPIS_BLOCK).toString())
								.withMapping("S", RegistryHelper.getRegistryName(Blocks.SEA_LANTERN).toString())
								.withMapping("0", RegistryHelper.getRegistryName(ExtrabotanyFlowerBlocks.stardustLotus).toString())
								.withText("extrabotany.page.stardustlotus2")
				)
				.extraRecipeMapping(ExtrabotanyFlowerBlocks.stardustLotusFloating, 1)
				.save(consumer, id("stardustlotus"));
		PatchouliBuilder.entry(CATEGORY)
				.withName("item.extrabotany.binder")
				.withIcon(ExtraBotanyItems.binder)
				.pages(
						text("extrabotany.page.binder0"),
						crafting(ExtraBotanyItems.binder).withText("extrabotany.page.binder1")
				)
				.save(consumer, id("binder"));
		PatchouliBuilder.entry(CATEGORY)
				.withName("extrabotany.entry.king_garden")
				.withIcon(ExtraBotanyItems.kingGarden)
				.pages(
						text("extrabotany.page.king_garden0"),
						text("extrabotany.page.king_garden1"),
						crafting(ExtraBotanyItems.kingGarden).withText("extrabotany.page.king_garden2"),
						crafting("extrabotany:king_garden_clear").withText("extrabotany.page.king_garden3")
				)
				.save(consumer, id("king_garden"));
		PatchouliBuilder.entry(CATEGORY)
				.withName("block.extrabotany.cocoondesire")
				.withIcon(ExtraBotanyBlocks.cocoonOfDesire)
				.pages(
						text("extrabotany.page.cocoondesire0"),
						text("extrabotany.page.cocoondesire1"),
						crafting(ExtraBotanyBlocks.cocoonOfDesire).withText("extrabotany.page.cocoondesire2")
				)
				.save(consumer, id("cocoondesire"));
		PatchouliBuilder.entry(MANA)
				.withName("block.extrabotany.managenerator")
				.withIcon(ExtraBotanyBlocks.manaGenerator)
				.pages(
						text("extrabotany.page.managenerator0"),
						crafting(ExtraBotanyBlocks.manaGenerator).withText("extrabotany.page.managenerator1")
				)
				.save(consumer, id("managenerator"));
		PatchouliBuilder.entry(MANA)
				.withName("block.extrabotany.manaliquefaction")
				.withIcon(ExtraBotanyBlocks.manaLiquefaction)
				.pages(
						text("extrabotany.page.manaliquefaction0"),
						crafting(ExtraBotanyBlocks.manaLiquefaction).withText("extrabotany.page.manaliquefaction1")
				)
				.save(consumer, id("manaliquefaction"));
		PatchouliBuilder.entry(CATEGORY)
				.withName("block.extrabotany.elfjar")
				.withIcon(ExtraBotanyBlocks.livingrockBarrel)
				.pages(
						text("extrabotany.page.elfjar0"),
						crafting(ExtraBotanyBlocks.livingrockBarrel).withText("extrabotany.page.elfjar1")
				)
				.save(consumer, id("elfjar"));
		PatchouliBuilder.entry(CATEGORY)
				.withName("item.extrabotany.spear_of_subspace")
				.withIcon(ExtraBotanyItems.spearOfSubspace)
				.pages(
						text("extrabotany.page.spear_of_subspace0"),
						crafting(ExtraBotanyItems.spearOfSubspace)
				)
				.save(consumer, id("spear_of_subspace"));
		PatchouliBuilder.entry(CATEGORY)
				.withName("item.extrabotany.judah_oath")
				.withIcon(ExtraBotanyItems.judahOath)
				.pages(
						text("extrabotany.page.judah_oath0"),
						crafting(ExtraBotanyItems.judahOath),
						crafting(ExtraBotanyItems.judahOathKira).withRecipe2(ExtraBotanyItems.judahOathSakura)
				)
				.save(consumer, id("judah_oath"));
		PatchouliBuilder.entry(BAUBLES)
				.withName("item.extrabotany.bottled_flame")
				.withIcon(ExtraBotanyItems.bottledFlame)
				.pages(
						text("extrabotany.page.bottled_flame0"),
						crafting(ExtraBotanyItems.bottledFlame)
				)
				.save(consumer, id("bottled_flame"));
		PatchouliBuilder.entry(CATEGORY)
				.withName("item.extrabotany.bottled_star")
				.withIcon(ExtraBotanyItems.bottledStar)
				.pages(
						text("extrabotany.page.bottled_star0"),
						crafting(ExtraBotanyItems.bottledStar)
				)
				.save(consumer, id("bottled_star"));
		PatchouliBuilder.entry(CATEGORY)
				.withName("item.extrabotany.bottled_pixie")
				.withIcon(ExtraBotanyItems.bottledPixie)
				.pages(
						text("extrabotany.page.bottled_pixie0"),
						crafting(ExtraBotanyItems.bottledPixie)
				)
				.save(consumer, id("bottled_pixie"));
		PatchouliBuilder.entry(CATEGORY)
				.withName("item.extrabotany.limited_edition_supply_bag")
				.withIcon(ExtraBotanyItems.limitedEditionSupplyBag)
				.pages(
						text("extrabotany.page.supply_bag0"),
						spotlight(ExtraBotanyItems.silentEternity).withText("extrabotany.page.supply_bag1"),
						spotlight(ExtraBotanyItems.lensSuperconductor).withText("extrabotany.page.supply_bag2")
				)
				.save(consumer, id("supply_bag"));
		PatchouliBuilder.entry(BAUBLES)
				.withName("extrabotany.entry.cosmetics")
				.withIcon(ExtraBotanyItems.foxEar)
				.pages(
						text("extrabotany.page.cosmetics0"),
						crafting(ExtraBotanyItems.foxEar).withRecipe2(ExtraBotanyItems.foxMask),
						crafting(ExtraBotanyItems.pylon).withRecipe2(ExtraBotanyItems.redScarf),
						crafting(ExtraBotanyItems.blackGlasses).withRecipe2(ExtraBotanyItems.thugLife),
						crafting(ExtraBotanyItems.mask).withRecipe2(ExtraBotanyItems.goggleStandard),
						crafting(ExtraBotanyItems.goggleJungle).withRecipe2(ExtraBotanyItems.goggleOcean),
						crafting(ExtraBotanyItems.goggleSnowfield),
						text("extrabotany.page.cosmetics1"),
						crafting(ExtraBotanyItems.superCrown).withText("extrabotany.page.cosmetics2")
				)
				.save(consumer, id("cosmetics"));
	}

	/**
	 * 原版和重制版都没有图鉴条目的内容。文字是按现在的功能新写的，不是从原版翻译的。
	 * The content that has a lexicon entry in neither the original nor Reburn. The text was written new from the function of today, and is not a translation of the original.
	 */
	private void addFunctionEntries(Consumer<PatchouliEntry> consumer) {
		PatchouliBuilder.entry(CATEGORY)
				.withName("item.extrabotany.flamescion_weapon")
				.withIcon(ExtraBotanyItems.flamescionWeapon)
				.pages(
						text("extrabotany.page.flamescion_weapon0"),
						text("extrabotany.page.flamescion_weapon1"),
						text("extrabotany.page.flamescion_weapon2"),
						crafting(ExtraBotanyItems.flamescionWeapon).withText("extrabotany.page.flamescion_weapon3")
				)
				.save(consumer, id("flamescion_weapon"));
		PatchouliBuilder.entry(CATEGORY)
				.withName("item.extrabotany.gem_of_conquest")
				.withIcon(ExtraBotanyItems.gemOfConquest)
				.pages(
						text("extrabotany.page.gem_of_conquest0"),
						text("extrabotany.page.gem_of_conquest1"),
						crafting(ExtraBotanyItems.gemOfConquest).withText("extrabotany.page.gem_of_conquest2")
				)
				.save(consumer, id("gem_of_conquest"));
		PatchouliBuilder.entry(CATEGORY)
				.withName("item.extrabotany.shadow_katana")
				.withIcon(ExtraBotanyItems.shadowKatana)
				.pages(
						text("extrabotany.page.shadow_katana0"),
						crafting(ExtraBotanyItems.shadowKatana).withText("extrabotany.page.shadow_katana1")
				)
				.save(consumer, id("shadow_katana"));
		PatchouliBuilder.entry(CATEGORY)
				.withName("item.extrabotany.photon_shotgun")
				.withIcon(ExtraBotanyItems.photonShotgun)
				.pages(
						text("extrabotany.page.photon_shotgun0"),
						crafting(ExtraBotanyItems.photonShotgun).withText("extrabotany.page.photon_shotgun1")
				)
				.save(consumer, id("photon_shotgun"));
		PatchouliBuilder.entry(CATEGORY)
				.withName("item.extrabotany.uuz_fan")
				.withIcon(ExtraBotanyItems.uuzFan)
				.pages(
						text("extrabotany.page.uuz_fan0"),
						crafting(ExtraBotanyItems.uuzFan).withText("extrabotany.page.uuz_fan1")
				)
				.save(consumer, id("uuz_fan"));
		PatchouliBuilder.entry(CATEGORY)
				.withName("extrabotany.entry.shooting_guardian")
				.withIcon(ExtraBotanyItems.shootingGuardianHelm)
				.pages(
						text("extrabotany.page.shooting_guardian0"),
						text("extrabotany.page.shooting_guardian1"),
						crafting(ExtraBotanyItems.shootingGuardianHelm).withRecipe2(ExtraBotanyItems.shootingGuardianChest),
						crafting(ExtraBotanyItems.shootingGuardianLegs).withRecipe2(ExtraBotanyItems.shootingGuardianBoots)
				)
				.save(consumer, id("shooting_guardian"));
		PatchouliBuilder.entry(CATEGORY)
				.withName("extrabotany.entry.silent_sages")
				.withIcon(ExtraBotanyItems.silentSagesHelm)
				.pages(
						text("extrabotany.page.silent_sages0"),
						crafting(ExtraBotanyItems.silentSagesHelm).withRecipe2(ExtraBotanyItems.silentSagesChest),
						crafting(ExtraBotanyItems.silentSagesLegs).withRecipe2(ExtraBotanyItems.silentSagesBoots)
				)
				.save(consumer, id("silent_sages"));
		PatchouliBuilder.entry(CATEGORY)
				.withName("block.extrabotany.trophy")
				.withIcon(ExtraBotanyBlocks.trophy)
				.pages(
						spotlight(ExtraBotanyBlocks.trophy).withText("extrabotany.page.trophy0")
				)
				.save(consumer, id("trophy"));
		PatchouliBuilder.entry(CATEGORY)
				.withName("block.extrabotany.quantummanabuffer")
				.withIcon(ExtraBotanyBlocks.quantumManaBuffer)
				.pages(
						text("extrabotany.page.quantummanabuffer0"),
						crafting(ExtraBotanyBlocks.quantumManaBuffer).withText("extrabotany.page.quantummanabuffer1")
				)
				.save(consumer, id("quantummanabuffer"));
		PatchouliBuilder.entry(CATEGORY)
				.withName("extrabotany.entry.candies")
				.withIcon(ExtraBotanyItems.candyEins)
				.pages(
						text("extrabotany.page.candies0"),
						crafting(ExtraBotanyItems.candyEins).withRecipe2(ExtraBotanyItems.candyZwei),
						crafting(ExtraBotanyItems.candyDrei),
						crafting(ExtraBotanyItems.candyBag).withText("extrabotany.page.candies1")
				)
				.save(consumer, id("candies"));
		PatchouliBuilder.entry(CATEGORY)
				.withName("extrabotany.entry.universal_runes")
				.withIcon(ExtraBotanyItems.elementRune)
				.pages(
						text("extrabotany.page.universal_runes0"),
						crafting(ExtraBotanyItems.elementRune).withText("extrabotany.page.universal_runes1"),
						crafting(ExtraBotanyItems.sinRune).withText("extrabotany.page.universal_runes2")
				)
				.save(consumer, id("universal_runes"));
		PatchouliBuilder.entry(CATEGORY)
				.withName("item.extrabotany.universal_petal")
				.withIcon(ExtraBotanyItems.universalPetal)
				.pages(
						text("extrabotany.page.universal_petal0"),
						crafting(ExtraBotanyItems.universalPetal).withText("extrabotany.page.universal_petal1")
				)
				.save(consumer, id("universal_petal"));
	}

	private static ResourceLocation mainAdvancement(String name) {
		return prefix("main/" + name);
	}

	private static ResourceLocation botaniaAdvancement(String name) {
		return ResourceLocation.tryBuild("botania", name).withPrefix("main/");
	}

	private ResourceLocation id(String fileName) {
		return ResourceLocation.tryBuild(CATEGORY.getPath(), fileName);
	}

	/**
	 * 条目写进植物魔法自己的图鉴（{@code botania:lexica_botania}）。Patchouli 允许别的模组用同样的路径往一本书里加条目。
	 * The entries go into the lexicon of Botania itself ({@code botania:lexica_botania}). Patchouli lets other mods add entries to a book with the same path.
	 *
	 * <p>目录名是 {@code en_us}，但是条目里的文字都是翻译键，所以所有语言共用这些文件。
	 * <p>The directory name is {@code en_us}, but all text in the entries is translation keys, thus all languages use these files.
	 */
	@Override
	protected Path getOutputPath(PatchouliEntry entry) {
		return packOutput.getOutputFolder(PackOutput.Target.RESOURCE_PACK)
				.resolve("botania/patchouli_books/lexica_botania/en_us/entries")
				.resolve(entry.getID().getNamespace())
				.resolve(entry.getID().getPath() + ".json");
	}

	@Override
	public String getName() {
		return "ExtraBotany Patchouli Entries";
	}
}
