package io.github.lounode.extrabotany.data.recipes;

import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.block.flower.FloatingSpecialFlowerBlock;
import vazkii.botania.common.crafting.recipe.ManaUpgradeRecipe;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.lib.BotaniaTags;
import vazkii.botania.common.lib.ConventionalBotaniaTags;
import vazkii.botania.common.lib.LibBlockNames;
import vazkii.botania.data.recipes.builder.BotaniaSpecialRecipeBuilder;
import vazkii.botania.data.recipes.builder.WrapperRecipeBuilder;
import vazkii.botania.mixin.RecipeProviderAccessor;

import io.github.lounode.extrabotany.common.block.ExtraBotanyBlocks;
import io.github.lounode.extrabotany.common.crafting.recipe.*;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.lib.ExtraBotanyTags;
import io.github.lounode.extrabotany.common.lib.LibItemNames;
import io.github.lounode.extrabotany.common.lib.ResourceLocationHelper;

import java.util.Comparator;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static io.github.lounode.extrabotany.data.tags.BlockTagProvider.EXTRABOTANY_BLOCK;

public class CraftingRecipeProvider extends ExtraBotanyRecipeProvider {
	public CraftingRecipeProvider(PackOutput packOutput,
			CompletableFuture<HolderLookup.Provider> lookupProvider) {
		super(packOutput, lookupProvider);
	}

	@Override
	public void buildRecipes(RecipeOutput consumer) {

		specialRecipe(consumer, PotionLensChangePotionRecipe::new, CraftingBookCategory.MISC);
		specialRecipe(consumer, DasRheingoldChangeSoulBoundRecipe::new, CraftingBookCategory.EQUIPMENT);
		specialRecipe(consumer, WandOfTheForestExtendRecipe::new, CraftingBookCategory.EQUIPMENT);

		registerMain(consumer);
		registerMisc(consumer);
		registerTools(consumer);
		registerArmors(consumer);
		registerTrinkets(consumer);
		registerLenses(consumer);
		registerConversions(consumer);
		registerDecor(consumer);
		registerFloatingFlowers(consumer);
	}

	private void registerMain(RecipeOutput consumer) {
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ExtraBotanyItems.theChaos)
				.define('S', ExtraBotanyTags.Items.INGOTS_SHADOWIUM)
				.define('P', ExtraBotanyTags.Items.INGOTS_PHOTONIUM)
				.define('F', ExtraBotanyItems.spiritFragment)
				.pattern(" S ")
				.pattern("SFP")
				.pattern(" P ")
				.unlockedBy("has_item", conditionsFromItem(ExtraBotanyItems.nightmareFuel))
				.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ExtraBotanyItems.theOrigin)
				.define('S', ConventionalBotaniaTags.Items.TERRASTEEL_INGOTS)
				.define('P', ExtraBotanyTags.Items.INGOTS_AERIALITE)
				.define('F', ExtraBotanyItems.spiritFragment)
				.pattern(" S ")
				.pattern("SFP")
				.pattern(" P ")
				.unlockedBy("has_item", conditionsFromItem(ExtraBotanyItems.nightmareFuel))
				.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ExtraBotanyItems.theEnd)
				.define('S', ExtraBotanyTags.Items.INGOTS_ORICHALCOS)
				.define('P', BotaniaItems.GAIA_INGOT)
				.define('F', ExtraBotanyItems.spiritFragment)
				.pattern(" S ")
				.pattern("SFP")
				.pattern(" P ")
				.unlockedBy("has_item", conditionsFromItem(ExtraBotanyItems.nightmareFuel))
				.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ExtraBotanyItems.challengeTicket)
				.define('S', ExtraBotanyTags.Items.INGOTS_SHADOWIUM)
				.define('P', BotaniaItems.GAIA_INGOT)
				.define('F', ExtraBotanyTags.Items.INGOTS_PHOTONIUM)
				.define('A', BotaniaItems.GAIA_SPIRIT)
				.pattern("ASA")
				.pattern("FPF")
				.pattern("ASA")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.GAIA_INGOT))
				.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ExtraBotanyItems.dasRheingold)
				.define('S', BotaniaItems.MANAWEAVE_CLOTH)
				.define('P', Items.GOLD_INGOT)
				.define('A', BotaniaItems.GAIA_SPIRIT)
				.pattern("ASA")
				.pattern("SPS")
				.pattern("ASA")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.GAIA_SPIRIT))
				.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ExtraBotanyItems.natureOrb)
				.define('S', ConventionalBotaniaTags.Items.DRAGONSTONE_GEMS)
				.define('P', BotaniaItems.MANA_PEARL)
				.define('A', ConventionalBotaniaTags.Items.TERRASTEEL_INGOTS)
				.pattern("ASA")
				.pattern("SPS")
				.pattern("ASA")
				.unlockedBy("has_item", conditionsFromTag(ConventionalBotaniaTags.Items.TERRASTEEL_INGOTS))
				.save(consumer);

	}

	private void registerTrinkets(RecipeOutput consumer) {
		WrapperRecipeBuilder.wrap(ManaUpgradeRecipe.SERIALIZER,
				ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ExtraBotanyItems.manaRingMaster)
				.define('R', BotaniaItems.GREATER_BAND_OF_MANA)
				.define('O', ExtraBotanyTags.Items.INGOTS_ORICHALCOS)
				.define('H', ExtraBotanyItems.heroMedal)
				.define('Y', ExtraBotanyItems.theOrigin)
				.define('C', ExtraBotanyItems.theChaos)
				.define('E', ExtraBotanyItems.theEnd)
				.pattern("OHO")
				.pattern("YRE")
				.pattern("OCO")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.GREATER_BAND_OF_MANA))
		).save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ExtraBotanyItems.camera)
				.define('B', ExtraBotanyTags.Items.INGOTS_SHADOWIUM)
				.define('G', BotaniaItems.GAIA_INGOT)
				.define('S', Items.SPYGLASS)
				.pattern("BBB")
				.pattern("BSB")
				.pattern("GGG")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.GAIA_INGOT))
				.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ExtraBotanyItems.featherOfJingwei)
				.define('L', Items.LAVA_BUCKET)
				.define('F', Items.FEATHER)
				.define('H', ExtraBotanyItems.heroMedal)
				.define('B', Items.BLAZE_POWDER)
				.pattern("BLB")
				.pattern("BFB")
				.pattern("BHB")
				.unlockedBy("has_item", conditionsFromItem(ExtraBotanyItems.heroMedal))
				.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ExtraBotanyItems.manaGlassBottle, 3)
				.define('G', BotaniaBlocks.MANAGLASS)
				.pattern("G G")
				.pattern("G G")
				.pattern(" G ")
				.unlockedBy("has_item", conditionsFromItem(BotaniaBlocks.MANAGLASS))
				.save(consumer);
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ExtraBotanyItems.nineAndThreeQuartersRewardBag, 3)
				.requires(BotaniaItems.DICE_OF_FATE)
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.DICE_OF_FATE))
				.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ExtraBotanyItems.pureDaisyPendant)
				.define('P', BotaniaBlocks.PURE_DAISY)
				.define('E', ConventionalBotaniaTags.Items.ELEMENTIUM_NUGGETS)
				.pattern("PEP")
				.pattern(" P ")
				.pattern("   ")
				.unlockedBy("has_item", conditionsFromItem(BotaniaBlocks.PURE_DAISY))
				.save(consumer);
		ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, ExtraBotanyItems.dispersiveRing)
				.requires(BotaniaItems.BAND_OF_MANA)
				.requires(BotaniaItems.RUNE_OF_MANA)
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.BAND_OF_MANA))
				.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ExtraBotanyItems.frostRing)
				.define('M', ConventionalBotaniaTags.Items.MANASTEEL_INGOTS)
				.define('I', Items.ICE)
				.pattern("IM ")
				.pattern("M M")
				.pattern(" M ")
				.unlockedBy("has_item", conditionsFromTag(ConventionalBotaniaTags.Items.MANASTEEL_INGOTS))
				.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ExtraBotanyItems.curseRing)
				.define('M', ConventionalBotaniaTags.Items.MANASTEEL_INGOTS)
				.define('S', Items.WITHER_SKELETON_SKULL)
				.pattern("SM ")
				.pattern("M M")
				.pattern(" M ")
				.unlockedBy("has_item", conditionsFromTag(ConventionalBotaniaTags.Items.MANASTEEL_INGOTS))
				.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ExtraBotanyItems.spiderRing)
				.define('M', ConventionalBotaniaTags.Items.MANASTEEL_INGOTS)
				.define('S', Items.SPIDER_EYE)
				.pattern("SM ")
				.pattern("M M")
				.pattern(" M ")
				.unlockedBy("has_item", conditionsFromTag(ConventionalBotaniaTags.Items.MANASTEEL_INGOTS))
				.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ExtraBotanyItems.jackieChanRing)
				.define('M', ConventionalBotaniaTags.Items.MANASTEEL_INGOTS)
				.define('S', Items.STICKY_PISTON)
				.pattern("SM ")
				.pattern("M M")
				.pattern(" M ")
				.unlockedBy("has_item", conditionsFromTag(ConventionalBotaniaTags.Items.MANASTEEL_INGOTS))
				.save(consumer);
		ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, ExtraBotanyItems.parkourRing)
				.requires(ExtraBotanyItems.spiderRing)
				.requires(ExtraBotanyItems.jackieChanRing)
				.unlockedBy("has_item", conditionsFromTag(ConventionalBotaniaTags.Items.MANASTEEL_INGOTS))
				.save(consumer);

	}

	private void registerLenses(RecipeOutput consumer) {
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ExtraBotanyItems.lensPush)
				.requires(BotaniaItems.MANA_LENS)
				.requires(BotaniaItems.RUNE_OF_EARTH)
				.requires(ConventionalBotaniaTags.Items.MANA_DUSTS)
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.MANA_LENS))
				.save(consumer);
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ExtraBotanyItems.lensSmelt)
				.requires(BotaniaItems.MANA_LENS)
				.requires(BotaniaItems.RUNE_OF_FIRE)
				.requires(ConventionalBotaniaTags.Items.MANA_DUSTS)
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.MANA_LENS))
				.save(consumer);
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ExtraBotanyItems.lensMana)
				.requires(BotaniaItems.MANA_LENS)
				.requires(BotaniaItems.RUNE_OF_MANA)
				.requires(ConventionalBotaniaTags.Items.MANA_DUSTS)
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.MANA_LENS))
				.save(consumer);
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ExtraBotanyItems.lensPotion)
				.requires(BotaniaItems.MANA_LENS)
				.requires(BotaniaItems.RUNE_OF_SPRING)
				.requires(ConventionalBotaniaTags.Items.MANA_DUSTS)
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.MANA_LENS))
				.save(consumer);
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ExtraBotanyItems.lensTrace)
				.requires(BotaniaItems.MANA_LENS)
				.requires(BotaniaItems.RUNE_OF_GREED)
				.requires(ConventionalBotaniaTags.Items.MANA_DUSTS)
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.MANA_LENS))
				.save(consumer);

	}

	private void registerTools(RecipeOutput consumer) {
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ExtraBotanyItems.failnaught)
				.define('O', ExtraBotanyTags.Items.INGOTS_ORICHALCOS)
				.define('S', BotaniaItems.MANA_INFUSED_STRING)
				.define('G', BotaniaItems.GAIA_INGOT)
				.pattern(" GS")
				.pattern("GOS")
				.pattern(" GS")
				.unlockedBy("has_item", conditionsFromTag(ExtraBotanyTags.Items.INGOTS_ORICHALCOS))
				.save(consumer);
		//ManaReader&WandExtend
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ExtraBotanyItems.manaReader)
				.define('D', ConventionalBotaniaTags.Items.MANA_DIAMOND_GEMS)
				.define('P', ConventionalBotaniaTags.Items.MANA_DUSTS)
				.define('S', BotaniaItems.LIVINGWOOD_TWIG)
				.pattern(" PD")
				.pattern(" SP")
				.pattern("S  ")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.WAND_OF_THE_FOREST))
				.save(consumer);
		/*
		ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, BotaniaItems.WAND_OF_THE_FOREST)
				.requires(BotaniaItems.WAND_OF_THE_FOREST)
				.requires(ExtraBotanyItems.manaReader)
				.unlockedBy("has_item", conditionsFromItem(ExtraBotanyItems.manaReader))
				.save(WrapperResult.ofType(WandOfTheForestExtendRecipe.SERIALIZER, consumer), prefix("twig_wand_extension"));
		ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, BotaniaItems.WAND_OF_THE_ELVEN_FOREST)
				.requires(BotaniaItems.WAND_OF_THE_ELVEN_FOREST)
				.requires(ExtraBotanyItems.manaReader)
				.unlockedBy("has_item", conditionsFromItem(ExtraBotanyItems.manaReader))
				.save(WrapperResult.ofType(WandOfTheForestExtendRecipe.SERIALIZER, consumer), prefix("dreamwood_wand_extension"));
		*/

		//Hammer
		hammer(ConventionalBotaniaTags.Items.MANASTEEL_INGOTS, BotaniaItems.LIVINGWOOD_TWIG, ExtraBotanyItems.manasteelHammer).save(consumer);
		hammer(ConventionalBotaniaTags.Items.ELEMENTIUM_INGOTS, BotaniaItems.DREAMWOOD_TWIG, ExtraBotanyItems.elementiumHammer).save(consumer);
		hammer(ConventionalBotaniaTags.Items.TERRASTEEL_INGOTS, BotaniaItems.LIVINGWOOD_TWIG, ExtraBotanyItems.terrasteelHammer).save(consumer);
		//Gaia hammer see smithing recipe
		hammer(ExtraBotanyTags.Items.INGOTS_PHOTONIUM, BotaniaItems.LIVINGWOOD_TWIG, ExtraBotanyItems.photoniumHammer).save(consumer);
		hammer(ExtraBotanyTags.Items.INGOTS_SHADOWIUM, BotaniaItems.LIVINGWOOD_TWIG, ExtraBotanyItems.shadowiumHammer).save(consumer);
		hammer(ExtraBotanyTags.Items.INGOTS_AERIALITE, BotaniaItems.DREAMWOOD_TWIG, ExtraBotanyItems.aerialiteHammer).save(consumer);
		//Orichalcos & Rhin see RunicAltar

		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ExtraBotanyItems.walkingCane)
				.define('T', BotaniaItems.LIVINGWOOD_TWIG)
				.define('G', Items.GOLD_BLOCK)
				.define('P', Items.PRISMARINE_CRYSTALS)
				.pattern("TGP")
				.pattern(" TG")
				.pattern("T T")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.LIVINGWOOD_TWIG))
				.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ExtraBotanyItems.magicFinger)
				.define('C', Items.CARROT)
				.define('H', ExtraBotanyItems.heroMedal)
				.define('P', BotaniaItems.MANA_POWDER)
				.pattern(" P ")
				.pattern("PCP")
				.pattern(" H ")
				.unlockedBy("has_item", conditionsFromItem(ExtraBotanyItems.heroMedal))
				.save(consumer);
	}

	private void registerArmors(RecipeOutput consumer) {
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ExtraBotanyItems.starryIdolHeadgear)
				.define('W', BotaniaItems.MANAWEAVE_CLOTH)
				.define('A', BotaniaItems.MANASTEEL_HELMET)
				.pattern("WWW")
				.pattern("WAW")
				.pattern("WWW")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.MANASTEEL_HELMET))
				.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ExtraBotanyItems.starryIdolSuit)
				.define('W', BotaniaItems.MANAWEAVE_CLOTH)
				.define('A', BotaniaItems.MANASTEEL_CHESTPLATE)
				.pattern("WWW")
				.pattern("WAW")
				.pattern("WWW")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.MANASTEEL_CHESTPLATE))
				.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ExtraBotanyItems.starryIdolSkirt)
				.define('W', BotaniaItems.MANAWEAVE_CLOTH)
				.define('A', BotaniaItems.MANASTEEL_LEGGINGS)
				.pattern("WWW")
				.pattern("WAW")
				.pattern("WWW")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.MANASTEEL_LEGGINGS))
				.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ExtraBotanyItems.starryIdolBoots)
				.define('W', BotaniaItems.MANAWEAVE_CLOTH)
				.define('A', BotaniaItems.MANASTEEL_BOOTS)
				.pattern("WWW")
				.pattern("WAW")
				.pattern("WWW")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.MANASTEEL_BOOTS))
				.save(consumer);
		registerSimpleArmorSet(consumer,
				Ingredient.of(ExtraBotanyItems.shadowium), "shadow_warrior", conditionsFromTag(ExtraBotanyTags.Items.INGOTS_SHADOWIUM));
		registerSimpleArmorSet(consumer,
				Ingredient.of(ExtraBotanyItems.photonium), "goblin_slayer", conditionsFromTag(ExtraBotanyTags.Items.INGOTS_PHOTONIUM));
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ExtraBotanyItems.pleiadesCombatMaidHeadgear)
				.define('I', BotaniaItems.GAIA_INGOT)
				.define('G', ExtraBotanyItems.dasRheingold)
				.define('T', BotaniaItems.TERRASTEEL_HELMET)
				.pattern("III")
				.pattern("GTG")
				.pattern("   ")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.GAIA_SPIRIT))
				.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ExtraBotanyItems.pleiadesCombatMaidSuit)
				.define('I', BotaniaItems.GAIA_INGOT)
				.define('G', ExtraBotanyItems.dasRheingold)
				.define('T', BotaniaItems.TERRASTEEL_CHESTPLATE)
				.pattern("G G")
				.pattern("GTG")
				.pattern("III")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.GAIA_SPIRIT))
				.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ExtraBotanyItems.pleiadesCombatMaidSkirt)
				.define('I', BotaniaItems.GAIA_INGOT)
				.define('G', ExtraBotanyItems.dasRheingold)
				.define('T', BotaniaItems.TERRASTEEL_LEGGINGS)
				.pattern("III")
				.pattern("GTG")
				.pattern("G G")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.GAIA_SPIRIT))
				.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ExtraBotanyItems.pleiadesCombatMaidBoots)
				.define('I', BotaniaItems.GAIA_INGOT)
				.define('G', ExtraBotanyItems.dasRheingold)
				.define('T', BotaniaItems.TERRASTEEL_BOOTS)
				.pattern("GTG")
				.pattern("III")
				.pattern("   ")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.GAIA_SPIRIT))
				.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ExtraBotanyItems.manasteelShield)
				.define('I', ConventionalBotaniaTags.Items.MANASTEEL_INGOTS)
				.define('S', Items.SHIELD)
				.pattern("I I")
				.pattern("ISI")
				.pattern(" I ")
				.unlockedBy("has_item", conditionsFromTag(ConventionalBotaniaTags.Items.MANASTEEL_INGOTS))
				.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ExtraBotanyItems.elementiumShield)
				.define('I', ConventionalBotaniaTags.Items.ELEMENTIUM_INGOTS)
				.define('S', Items.SHIELD)
				.pattern("I I")
				.pattern("ISI")
				.pattern(" I ")
				.unlockedBy("has_item", conditionsFromTag(ConventionalBotaniaTags.Items.ELEMENTIUM_INGOTS))
				.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ExtraBotanyItems.terrasteelShield)
				.define('I', ConventionalBotaniaTags.Items.TERRASTEEL_INGOTS)
				.define('S', Items.SHIELD)
				.pattern("I I")
				.pattern("ISI")
				.pattern(" I ")
				.unlockedBy("has_item", conditionsFromTag(ConventionalBotaniaTags.Items.TERRASTEEL_INGOTS))
				.save(consumer);

	}

	private void registerConversions(RecipeOutput consumer) {
		compression(ExtraBotanyItems.orichalcos, ExtraBotanyTags.Items.NUGGETS_ORICHALCOS)
				.save(consumer, prefix("conversions/orichalcos_from_nuggets"));

		compression(ExtraBotanyItems.photonium, ExtraBotanyTags.Items.NUGGETS_PHOTONIUM)
				.save(consumer, prefix("conversions/photonium_from_nuggets"));

		compression(ExtraBotanyItems.shadowium, ExtraBotanyTags.Items.NUGGETS_SHADOWIUM)
				.save(consumer, prefix("conversions/shadowium_from_nuggets"));

		compression(ExtraBotanyItems.aerialite, ExtraBotanyTags.Items.NUGGETS_AERIALITE)
				.save(consumer, prefix("conversions/aerialite_from_nuggets"));

		compression(ExtraBotanyBlocks.orichalcosBlock, ExtraBotanyTags.Items.INGOTS_ORICHALCOS).save(consumer);
		compression(ExtraBotanyBlocks.photoniumBlock, ExtraBotanyTags.Items.INGOTS_PHOTONIUM).save(consumer);
		compression(ExtraBotanyBlocks.shadowiumBlock, ExtraBotanyTags.Items.INGOTS_SHADOWIUM).save(consumer);
		compression(ExtraBotanyBlocks.aerialiteBlock, ExtraBotanyTags.Items.INGOTS_AERIALITE).save(consumer);

		deconstruct(consumer, ExtraBotanyItems.orichalcos, ExtraBotanyTags.Items.BLOCKS_ORICHALCOS, "orichalcos_block_deconstruct");
		deconstruct(consumer, ExtraBotanyItems.photonium, ExtraBotanyTags.Items.BLOCKS_PHOTONIUM, "photonium_block_deconstruct");
		deconstruct(consumer, ExtraBotanyItems.shadowium, ExtraBotanyTags.Items.BLOCKS_SHADOWIUM, "shadowium_block_deconstruct");
		deconstruct(consumer, ExtraBotanyItems.aerialite, ExtraBotanyTags.Items.BLOCKS_AERIALITE, "aerialite_block_deconstruct");

		deconstruct(consumer, ExtraBotanyItems.orichalcosNugget, ExtraBotanyTags.Items.INGOTS_ORICHALCOS, "orichalcos_to_nuggets");
		deconstruct(consumer, ExtraBotanyItems.photoniumNugget, ExtraBotanyTags.Items.INGOTS_PHOTONIUM, "photonium_to_nuggets");
		deconstruct(consumer, ExtraBotanyItems.shadowiumNugget, ExtraBotanyTags.Items.INGOTS_SHADOWIUM, "shadowium_to_nuggets");
		deconstruct(consumer, ExtraBotanyItems.aerialiteNugget, ExtraBotanyTags.Items.INGOTS_AERIALITE, "aerialite_to_nuggets");

	}

	private void registerDecor(RecipeOutput consumer) {
		registerForQuartz(consumer, LibItemNames.GAIA_QUARTZ, ExtraBotanyItems.gaiaQuartz);
		registerForQuartz(consumer, LibItemNames.ELEMENTIUM_QUARTZ, ExtraBotanyItems.elementiumQuartz);
	}

	protected void registerForQuartz(RecipeOutput consumer, String variant, ItemLike baseItem) {
		Block block = getBlockOrThrow(prefix(variant + "_block"));
		Block stairs = getBlockOrThrow(prefix(variant + LibBlockNames.STAIRS_SUFFIX));
		Block slab = getBlockOrThrow(prefix(variant + LibBlockNames.SLAB_SUFFIX));
		Block chiseled = getBlockOrThrow(prefix("chiseled_" + variant + "_block"));
		Block bricks = getBlockOrThrow(prefix(variant + "_bricks"));
		Block pillar = getBlockOrThrow(prefix(variant + "_pillar"));
		Block smooth = getBlockOrThrow(prefix("smooth_" + variant));
		Block smoothStairs = getBlockOrThrow(prefix("smooth_" + variant + LibBlockNames.STAIRS_SUFFIX));
		Block smoothSlab = getBlockOrThrow(prefix("smooth_" + variant + LibBlockNames.SLAB_SUFFIX));

		ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, block)
				.define('Q', baseItem)
				.pattern("QQ")
				.pattern("QQ")
				.group("extrabotany:quartz_block")
				.unlockedBy("has_item", conditionsFromItem(baseItem))
				.save(consumer);
		stairs(stairs, block).group("extrabotany:quartz_stairs").save(consumer);
		slabShape(slab, block).group("extrabotany:quartz_slab").save(consumer);
		chiseled(chiseled, slab).group("extrabotany:quartz_chiseled")
				.unlockedBy("has_base_item", conditionsFromItem(block)).save(consumer);
		brick(bricks, block).group("extrabotany:quartz_bricks").save(consumer);
		pillar(pillar, block).group("extrabotany:quartz_pillar").save(consumer);
		//smooth: see SmeltingProvider
		stairs(smoothStairs, smooth).group("extrabotany:quartz_stairs").save(consumer);
		slabShape(smoothSlab, smooth).group("extrabotany:quartz_slab").save(consumer);
	}

	private void registerMisc(RecipeOutput consumer) {

		//Pedestal
		pedestal(BotaniaBlocks.LIVINGROCK, ExtraBotanyBlocks.livingrockPedestal)
				.unlockedBy("has_item", conditionsFromItem(BotaniaBlocks.LIVINGROCK))
				.save(consumer);
		pedestal(Items.CALCITE, ExtraBotanyBlocks.calcitePedestal)
				.unlockedBy("has_item", conditionsFromItem(BotaniaBlocks.LIVINGROCK))
				.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ExtraBotanyBlocks.manaCharger)
				.define('R', BotaniaBlocks.LIVINGROCK_SLAB)
				.define('S', BotaniaItems.LIVINGWOOD_TWIG)
				.pattern("   ")
				.pattern(" R ")
				.pattern("S S")
				.unlockedBy("has_item", conditionsFromItem(BotaniaBlocks.LIVINGROCK))
				.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ExtraBotanyBlocks.dimensionCatalyst)
				.define('R', BotaniaBlocks.LIVINGROCK)
				.define('P', Items.ENDER_PEARL)
				.define('C', BotaniaBlocks.ALCHEMY_CATALYST)
				.define('S', ExtraBotanyItems.theChaos)
				.define('Q', ExtraBotanyItems.elementiumQuartz)
				.pattern("RPR")
				.pattern("QCQ")
				.pattern("RSR")
				.unlockedBy("has_item", conditionsFromItem(BotaniaBlocks.ALCHEMY_CATALYST))
				.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ExtraBotanyItems.gaiaQuartz)
				.define('T', BotaniaItems.TERRASTEEL_NUGGET)
				.define('Q', Items.QUARTZ)
				.pattern("QQQ")
				.pattern("QTQ")
				.pattern("QQQ")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.TERRASTEEL_INGOT))
				.save(consumer);
		WrapperRecipeBuilder.wrap(CopyBrewFormFlaskRecipe.SERIALIZER,
				ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ExtraBotanyItems.manaCocktail)
				.requires(ExtraBotanyItems.manaCocktail)
				.requires(BotaniaItems.BREW_FLASK)
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.BREW_FLASK))
		).save(consumer, prefix("mana_cocktail_change_brew"));
		WrapperRecipeBuilder.wrap(CopyBrewFromManaCocktailRecipe.SERIALIZER,
				ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ExtraBotanyItems.infiniteWine)
				.requires(ExtraBotanyItems.manaCocktail)
				.requires(ExtraBotanyItems.infiniteWine)
				.unlockedBy("has_item", conditionsFromItem(ExtraBotanyItems.heroMedal))
		).save(consumer, prefix("infinite_wine_change_brew"));
		WrapperRecipeBuilder.wrap(CopyBrewFromManaCocktailRecipe.SERIALIZER,
				ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ExtraBotanyItems.infiniteWine)
				.requires(ExtraBotanyItems.manaCocktail)
				.requires(ExtraBotanyItems.heroMedal)
				.unlockedBy("has_item", conditionsFromItem(ExtraBotanyItems.heroMedal))
		).save(consumer, prefix("infinite_wine"));
		WrapperRecipeBuilder.wrap(CopyBrewFromManaCocktailRecipe.SERIALIZER,
				ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ExtraBotanyItems.holyWaterGrenade)
				.requires(ExtraBotanyItems.manaCocktail)
				.requires(Items.POPPED_CHORUS_FRUIT)
				.unlockedBy("has_item", conditionsFromItem(ExtraBotanyItems.manaCocktail))
		).save(consumer, prefix("holy_water_grenade"));

	}

	private void registerFloatingFlowers(RecipeOutput consumer) {
		var floatings = BuiltInRegistries.BLOCK.stream().filter(EXTRABOTANY_BLOCK.and(b -> b instanceof FloatingSpecialFlowerBlock))
				.sorted(Comparator.comparing(BuiltInRegistries.BLOCK::getKey))
				.toArray(Block[]::new);

		for (var block : floatings) {
			ResourceLocation inputName = BuiltInRegistries.ITEM.getKey(block.asItem());
			Item origin = getItemOrThrow(ResourceLocation.fromNamespaceAndPath(inputName.getNamespace(), inputName.getPath().replace("floating_", "")));
			createFloatingFlowerRecipe(consumer, origin);
		}
	}

	private static void specialRecipe(RecipeOutput recipeOutput,
			Function<CraftingBookCategory, Recipe<?>> factory, CraftingBookCategory category) {
		BotaniaSpecialRecipeBuilder.special(factory, category).save(recipeOutput);
	}

	public static Criterion<InventoryChangeTrigger.TriggerInstance> conditionsFromItem(ItemLike item) {
		return RecipeProviderAccessor.botania_inventoryTrigger(ItemPredicate.Builder.item().of(item));
	}

	public static Criterion<InventoryChangeTrigger.TriggerInstance> conditionsFromTag(TagKey<Item> tag) {
		return RecipeProviderAccessor.botania_inventoryTrigger(ItemPredicate.Builder.item().of(tag));
	}

	private static Block getBlockOrThrow(ResourceLocation id) {
		return BuiltInRegistries.BLOCK.getOptional(id)
				.orElseThrow(() -> new IllegalStateException("Missing block " + id));
	}

	private static Item getItemOrThrow(ResourceLocation id) {
		return BuiltInRegistries.ITEM.getOptional(id)
				.orElseThrow(() -> new IllegalStateException("Missing item " + id));
	}

	private void createFloatingFlowerRecipe(RecipeOutput recipeOutput, ItemLike input) {
		ResourceLocation inputName = BuiltInRegistries.ITEM.getKey(input.asItem());
		Item output = getItemOrThrow(ResourceLocation.fromNamespaceAndPath(
				inputName.getNamespace(), "floating_" + inputName.getPath()));
		ShapelessRecipeBuilder.shapeless(RecipeCategory.DECORATIONS, output)
				.requires(BotaniaTags.Items.FLOATING_FLOWERS)
				.requires(input)
				.group("extrabotany:floating_flower")
				.unlockedBy("has_item", conditionsFromItem(input))
				.save(recipeOutput);
	}

	private void registerSimpleArmorSet(RecipeOutput recipeOutput, Ingredient material, String variant,
			Criterion<InventoryChangeTrigger.TriggerInstance> criterion) {
		Item helmet = getItemOrThrow(prefix(variant + "_helmet"));
		Item chestplate = getItemOrThrow(prefix(variant + "_chestplate"));
		Item leggings = getItemOrThrow(prefix(variant + "_leggings"));
		Item boots = getItemOrThrow(prefix(variant + "_boots"));
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, helmet)
				.define('S', material).pattern("SSS").pattern("S S")
				.unlockedBy("has_item", criterion).save(recipeOutput);
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, chestplate)
				.define('S', material).pattern("S S").pattern("SSS").pattern("SSS")
				.unlockedBy("has_item", criterion).save(recipeOutput);
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, leggings)
				.define('S', material).pattern("SSS").pattern("S S").pattern("S S")
				.unlockedBy("has_item", criterion).save(recipeOutput);
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, boots)
				.define('S', material).pattern("S S").pattern("S S")
				.unlockedBy("has_item", criterion).save(recipeOutput);
	}

	private void deconstruct(RecipeOutput recipeOutput, ItemLike output, TagKey<Item> input, String id) {
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, output, 9)
				.requires(input)
				.unlockedBy("has_item", conditionsFromTag(input))
				.save(recipeOutput, prefix("conversions/" + id));
	}

	private ShapedRecipeBuilder stairs(ItemLike output, ItemLike input) {
		return ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, output, 4)
				.define('Q', input).pattern("Q  ").pattern("QQ ").pattern("QQQ")
				.unlockedBy("has_item", conditionsFromItem(input));
	}

	private ShapedRecipeBuilder slabShape(ItemLike output, ItemLike input) {
		return ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, output, 6)
				.define('Q', input).pattern("QQQ")
				.unlockedBy("has_item", conditionsFromItem(input));
	}

	private ShapedRecipeBuilder chiseled(ItemLike output, ItemLike input) {
		return ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, output)
				.define('Q', input).pattern("Q").pattern("Q")
				.unlockedBy("has_item", conditionsFromItem(input));
	}

	private ShapedRecipeBuilder brick(ItemLike output, ItemLike input) {
		return ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, output, 4)
				.define('Q', input).pattern("QQ").pattern("QQ")
				.unlockedBy("has_item", conditionsFromItem(input));
	}

	private ShapedRecipeBuilder pillar(ItemLike output, ItemLike input) {
		return ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, output, 2)
				.define('Q', input).pattern("Q").pattern("Q")
				.unlockedBy("has_item", conditionsFromItem(input));
	}

	protected ShapedRecipeBuilder hammer(ItemLike head, ItemLike handle, ItemLike hammer) {
		return ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, hammer)
				.define('H', head)
				.define('S', handle)
				.pattern("HHH")
				.pattern("HHH")
				.pattern(" S ")
				.unlockedBy("has_item", conditionsFromItem(head));
	}

	protected ShapedRecipeBuilder hammer(TagKey<Item> item, ItemLike handle, ItemLike hammer) {
		return ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, hammer)
				.define('H', item)
				.define('S', handle)
				.pattern("HHH")
				.pattern("HHH")
				.pattern(" S ")
				.unlockedBy("has_item", conditionsFromTag(item));
	}

	protected ShapedRecipeBuilder pedestal(ItemLike block, ItemLike pedestal) {
		return ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, pedestal)
				.define('P', Items.GOLD_NUGGET)
				.define('C', block)
				.pattern("CPC")
				.pattern(" C ")
				.pattern("CCC");
	}

	private ShapedRecipeBuilder compression(ItemLike output, TagKey<Item> input) {
		return ShapedRecipeBuilder.shaped(output instanceof Block ? RecipeCategory.BUILDING_BLOCKS : RecipeCategory.MISC, output)
				.define('I', input)
				.pattern("III")
				.pattern("III")
				.pattern("III")
				.unlockedBy("has_item", conditionsFromTag(input));
	}

	protected ResourceLocation prefix(String path) {
		return ResourceLocationHelper.prefix(path);
	}

	@Override
	public String getName() {
		return "ExtraBotany crafting recipes";
	}
}
