package io.github.lounode.extrabotany.common.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.crafting.RecipeSerializer;

import vazkii.botania.common.item.CustomCreativeTabContents;
import vazkii.botania.common.item.lens.Lens;
import vazkii.botania.common.item.lens.LensItem;
import vazkii.botania.xplat.XplatAbstractions;

import io.github.lounode.extrabotany.common.crafting.recipe.*;
import io.github.lounode.extrabotany.common.item.brew.HolyWaterGrenadeItem;
import io.github.lounode.extrabotany.common.item.brew.InfiniteWineItem;
import io.github.lounode.extrabotany.common.item.brew.ManaCocktailItem;
import io.github.lounode.extrabotany.common.item.brew.ManaGlassBottleItem;
import io.github.lounode.extrabotany.common.item.equipment.armor.goblin_slayer.GoblinSlayerArmorItem;
import io.github.lounode.extrabotany.common.item.equipment.armor.goblin_slayer.GoblinSlayerHelmetItem;
import io.github.lounode.extrabotany.common.item.equipment.armor.pleiades_combat_maid.PleiadesCombatMaidArmorItem;
import io.github.lounode.extrabotany.common.item.equipment.armor.pleiades_combat_maid.PleiadesCombatMaidSuitItem;
import io.github.lounode.extrabotany.common.item.equipment.armor.pleiades_combat_maid.SanguinePleiadesCombatMaidSuitItem;
import io.github.lounode.extrabotany.common.item.equipment.armor.shadow_warrior.ShadowWarriorArmorItem;
import io.github.lounode.extrabotany.common.item.equipment.armor.shadow_warrior.ShadowWarriorHelmetItem;
import io.github.lounode.extrabotany.common.item.equipment.armor.starry_idol.StarryIdolArmorItem;
import io.github.lounode.extrabotany.common.item.equipment.armor.starry_idol.StarryIdolHeadgearItem;
import io.github.lounode.extrabotany.common.item.equipment.bauble.*;
import io.github.lounode.extrabotany.common.item.equipment.shield.ElementiumShieldItem;
import io.github.lounode.extrabotany.common.item.equipment.shield.ManasteelShieldItem;
import io.github.lounode.extrabotany.common.item.equipment.shield.TerrasteelShieldItem;
import io.github.lounode.extrabotany.common.item.equipment.tool.MagicFingerItem;
import io.github.lounode.extrabotany.common.item.equipment.tool.hammer.*;
import io.github.lounode.extrabotany.common.item.lens.*;
import io.github.lounode.extrabotany.common.item.material.ChallangeTicketItem;
import io.github.lounode.extrabotany.common.item.material.GildedPotatoItem;
import io.github.lounode.extrabotany.common.item.material.HammerTiers;
import io.github.lounode.extrabotany.common.item.material.ArmorsMaterial;
import io.github.lounode.extrabotany.common.item.relic.*;
import io.github.lounode.extrabotany.common.item.relic.void_archives.VoidArchivesItem;
import io.github.lounode.extrabotany.common.item.relic.voidcore.CoreOfTheVoidItem;
import io.github.lounode.extrabotany.common.item.record.ExtraBotanyJukeboxSongs;
import io.github.lounode.extrabotany.common.lib.LibItemNames;
import io.github.lounode.extrabotany.common.sounds.ExtraBotanySounds;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public final class ExtraBotanyItems {
	public static final Map<ResourceLocation, Item> ALL = new LinkedHashMap<>();

	// Legacy IDs keep their original components and behavior; no remapping to air.
	public static final Item binder = make(prefix("binder"), new io.github.lounode.extrabotany.common.item.legacy.LegacyBinderItem(unstackable()));
	public static final Item universalPetal = make(prefix("universal_petal"), new Item(defaultBuilder()));
	public static final Item emptyCoreOfTheVoid = make(prefix("empty_core_of_the_void"), new Item(unstackable().rarity(Rarity.EPIC)));
	public static final Item manaDrink = make(prefix("mana_drink"), new io.github.lounode.extrabotany.common.item.legacy.LegacyManaDrinkItem(defaultBuilder()
			.food(new FoodProperties.Builder().nutrition(0).saturationModifier(0).alwaysEdible().build())));
	public static final Item lensSuperconductor = make(prefix("lens_superconductor"), new LensItem(stackTo16(), new SuperconductorLens(), 12));

	//Mana Item
	public static final Item zadkiel = make(prefix(LibItemNames.ZADKIEL), new ZadkielItem(unstackable()));
	public static final Item manaReader = make(prefix(LibItemNames.MANA_READER), new ManaReaderItem(unstackable()));
	public static final Item natureOrb = make(prefix(LibItemNames.NATURE_ORB), new NatureOrbItem(unstackable().rarity(Rarity.UNCOMMON)));
	public static final Item featherOfJingwei = make(prefix(LibItemNames.FEATHER_OF_JINGWEI), new FeatherOfJingweiItem(unstackable()));
	public static final Item magicFinger = make(prefix(LibItemNames.MAGIC_FINGER), new MagicFingerItem(unstackable()));
	public static final Item walkingCane = make(prefix(LibItemNames.WALKING_CANE), new WalkingCaneItem(unstackable()));
	public static final Item manaRingMaster = make(prefix(LibItemNames.MANA_RING_MASTER), new MasterBandOfManaItem(unstackable().rarity(Rarity.UNCOMMON).fireResistant()));
	public static final Item frostRing = make(prefix(LibItemNames.FROST_RING), new FrostRingItem(unstackable()));
	public static final Item curseRing = make(prefix(LibItemNames.CURSE_RING), new CurseRingItem(unstackable()));
	public static final Item spiderRing = make(prefix(LibItemNames.SPIDER_RING), new SpiderRingItem(unstackable()));
	public static final Item jackieChanRing = make(prefix(LibItemNames.JACKIE_CHAN_RING), new JackieChanRingItem(unstackable()));
	public static final Item parkourRing = make(prefix(LibItemNames.PARKOUR_RING), new ParkourRingItem(unstackable()));
	public static final Item dispersiveRing = make(prefix(LibItemNames.DISPERSIVE_RING), new DispersiveRingItem(unstackable()));
	public static final Item pureDaisyPendant = make(prefix(LibItemNames.PURE_DAISY_PENDANT), new PureDaisyPendantItem(unstackable()));
	public static final Item coreOfTheVoid = make(prefix(LibItemNames.CORE_OF_THE_VOID), new CoreOfTheVoidItem(unstackable().rarity(Rarity.UNCOMMON)));
	public static final Item voidArchives = make(prefix(LibItemNames.VOID_ARCHIVES), new VoidArchivesItem(unstackable().rarity(Rarity.EPIC)));

	//Hammer
	public static final Item manasteelHammer = make(prefix(LibItemNames.MANASTEEL_HAMMER),
			new ManasteelHammerItem(HammerTiers.MANASTEEL, 6, -3.1F, unstackableCustomDamage()));
	public static final Item elementiumHammer = make(prefix(LibItemNames.ELEMENTIUM_HAMMER),
			new ElementiumHammerItem(HammerTiers.ELEMENTIUM, 6, -3.1F, unstackableCustomDamage()));
	public static final Item terrasteelHammer = make(prefix(LibItemNames.TERRASTEEL_HAMMER),
			new TerrasteelHammerItem(HammerTiers.TERRASTEEL, 5, -3.0F, unstackableCustomDamage().rarity(Rarity.UNCOMMON)));
	public static final Item gaiaHammer = make(prefix(LibItemNames.GAIA_HAMMER),
			new GaiaHammerItem(HammerTiers.GAIA, 5, -3.0F, unstackableCustomDamage().rarity(Rarity.UNCOMMON).fireResistant()));
	public static final Item photoniumHammer = make(prefix(LibItemNames.PHOTONIUM_HAMMER),
			new PhotoniumHammerItem(HammerTiers.PHOTONIUM, 6, -3.1F, unstackableCustomDamage()));
	public static final Item shadowiumHammer = make(prefix(LibItemNames.SHADOWIUM_HAMMER),
			new ShadowiumHammerItem(HammerTiers.SHADOWIUM, 6, -3.1F, unstackableCustomDamage()));
	public static final Item aerialiteHammer = make(prefix(LibItemNames.AERIALITE_HAMMER),
			new AerialiteHammerItem(HammerTiers.AERIALITE, 5, -3.0F, unstackableCustomDamage().rarity(Rarity.UNCOMMON)));
	public static final Item orichalcosHammer = make(prefix(LibItemNames.ORICHALCOS_HAMMER),
			new OrichalcosHammer(HammerTiers.ORICHALCOS, 5, -3.0F, unstackableCustomDamage().rarity(Rarity.EPIC).fireResistant()));
	public static final Item rheinHammer = make(prefix(LibItemNames.RHEIN_HAMMER),
			new RheinHammerItem(unstackableCustomDamage().rarity(Rarity.EPIC).fireResistant()));
	//Shield
	public static final Item manasteelShield = make(prefix(LibItemNames.MANASTEEL_SHIELD),
			new ManasteelShieldItem(unstackableCustomDamage()));
	public static final Item elementiumShield = make(prefix(LibItemNames.ELEMENTIUM_SHIELD),
			new ElementiumShieldItem(unstackableCustomDamage()));
	public static final Item terrasteelShield = make(prefix(LibItemNames.TERRASTEEL_SHIELD),
			new TerrasteelShieldItem(unstackableCustomDamage().rarity(Rarity.UNCOMMON)));
	public static final Item achillesShield = make(prefix(LibItemNames.ACHILLES_SHIELD),
			new AchillesShieldItem(unstackableCustomDamage().rarity(Rarity.EPIC).fireResistant()));

	//Armor
	public static final Item starryIdolHeadgear = make(prefix(LibItemNames.STARRY_IDOL_HEADGEAR),
			new StarryIdolHeadgearItem(armorProperties(ArmorItem.Type.HELMET, ArmorsMaterial.STARRY_IDOL_DURABILITY_FACTOR)));
	public static final Item starryIdolSuit = make(prefix(LibItemNames.STARRY_IDOL_SUIT),
			new StarryIdolArmorItem(ArmorItem.Type.CHESTPLATE, armorProperties(ArmorItem.Type.CHESTPLATE, ArmorsMaterial.STARRY_IDOL_DURABILITY_FACTOR)));
	public static final Item starryIdolSkirt = make(prefix(LibItemNames.STARRY_IDOL_SKIRT),
			new StarryIdolArmorItem(ArmorItem.Type.LEGGINGS, armorProperties(ArmorItem.Type.LEGGINGS, ArmorsMaterial.STARRY_IDOL_DURABILITY_FACTOR)));
	public static final Item starryIdolBoots = make(prefix(LibItemNames.STARRY_IDOL_BOOTS),
			new StarryIdolArmorItem(ArmorItem.Type.BOOTS, armorProperties(ArmorItem.Type.BOOTS, ArmorsMaterial.STARRY_IDOL_DURABILITY_FACTOR)));

	public static final Item pleiadesCombatMaidHeadgear = make(prefix(LibItemNames.PLEIADES_COMBAT_MAID_HEADGEAR),
			new PleiadesCombatMaidArmorItem(ArmorItem.Type.HELMET, armorProperties(ArmorItem.Type.HELMET, ArmorsMaterial.PLEIADS_MAID_COMBAT_DURABILITY_FACTOR)));
	public static final Item pleiadesCombatMaidSuit = make(prefix(LibItemNames.PLEIADES_COMBAT_MAID_SUIT),
			new PleiadesCombatMaidSuitItem(armorProperties(ArmorItem.Type.CHESTPLATE, ArmorsMaterial.PLEIADS_MAID_COMBAT_DURABILITY_FACTOR)));
	public static final Item pleiadesCombatMaidSkirt = make(prefix(LibItemNames.PLEIADES_COMBAT_MAID_SKIRT),
			new PleiadesCombatMaidArmorItem(ArmorItem.Type.LEGGINGS, armorProperties(ArmorItem.Type.LEGGINGS, ArmorsMaterial.PLEIADS_MAID_COMBAT_DURABILITY_FACTOR)));
	public static final Item pleiadesCombatMaidBoots = make(prefix(LibItemNames.PLEIADES_COMBAT_MAID_BOOTS),
			new PleiadesCombatMaidArmorItem(ArmorItem.Type.BOOTS, armorProperties(ArmorItem.Type.BOOTS, ArmorsMaterial.PLEIADS_MAID_COMBAT_DURABILITY_FACTOR)));
	public static final Item sanguinePleiadesCombatMaidSuit = make(prefix(LibItemNames.SANGUINE_PLEIADES_COMBAT_MAID_SUIT),
			new SanguinePleiadesCombatMaidSuitItem(armorProperties(ArmorItem.Type.CHESTPLATE, ArmorsMaterial.PLEIADS_MAID_COMBAT_DURABILITY_FACTOR)));

	public static final Item goblinSlayerHelmet = make(prefix(LibItemNames.GOBLIN_SLAYER_HELMET),
			new GoblinSlayerHelmetItem(armorProperties(ArmorItem.Type.HELMET, ArmorsMaterial.GOBLIN_SLAYER_DURABILITY_FACTOR)));
	public static final Item goblinSlayerChestplate = make(prefix(LibItemNames.GOBLIN_SLAYER_CHESTPLATE),
			new GoblinSlayerArmorItem(ArmorItem.Type.CHESTPLATE, armorProperties(ArmorItem.Type.CHESTPLATE, ArmorsMaterial.GOBLIN_SLAYER_DURABILITY_FACTOR)));
	public static final Item goblinSlayerLeggings = make(prefix(LibItemNames.GOBLIN_SLAYER_LEGGINGS),
			new GoblinSlayerArmorItem(ArmorItem.Type.LEGGINGS, armorProperties(ArmorItem.Type.LEGGINGS, ArmorsMaterial.GOBLIN_SLAYER_DURABILITY_FACTOR)));
	public static final Item goblinSlayerBoots = make(prefix(LibItemNames.GOBLIN_SLAYER_BOOTS),
			new GoblinSlayerArmorItem(ArmorItem.Type.BOOTS, armorProperties(ArmorItem.Type.BOOTS, ArmorsMaterial.GOBLIN_SLAYER_DURABILITY_FACTOR)));

	public static final Item shadowWarriorHelmet = make(prefix(LibItemNames.SHADOW_WARRIOR_HELMET),
			new ShadowWarriorHelmetItem(armorProperties(ArmorItem.Type.HELMET, ArmorsMaterial.SHADOW_WARRIOR_DURABILITY_FACTOR)));
	public static final Item shadowWarriorChestplate = make(prefix(LibItemNames.SHADOW_WARRIOR_CHESTPLATE),
			new ShadowWarriorArmorItem(ArmorItem.Type.CHESTPLATE, armorProperties(ArmorItem.Type.CHESTPLATE, ArmorsMaterial.SHADOW_WARRIOR_DURABILITY_FACTOR)));
	public static final Item shadowWarriorLeggings = make(prefix(LibItemNames.SHADOW_WARRIOR_LEGGINGS),
			new ShadowWarriorArmorItem(ArmorItem.Type.LEGGINGS, armorProperties(ArmorItem.Type.LEGGINGS, ArmorsMaterial.SHADOW_WARRIOR_DURABILITY_FACTOR)));
	public static final Item shadowWarriorBoots = make(prefix(LibItemNames.SHADOW_WARRIOR_BOOTS),
			new ShadowWarriorArmorItem(ArmorItem.Type.BOOTS, armorProperties(ArmorItem.Type.BOOTS, ArmorsMaterial.SHADOW_WARRIOR_DURABILITY_FACTOR)));

	//Relic
	public static final Item camera = make(prefix(LibItemNames.CAMERA), new CameraItem(unstackable().rarity(Rarity.UNCOMMON).fireResistant()));
	public static final Item failnaught = make(prefix(LibItemNames.FAILNAUGHT), new FailnaughtItem(unstackable().rarity(Rarity.UNCOMMON).fireResistant()));
	public static final Item excalibur = make(prefix(LibItemNames.EXCALIBUR), new ExcaliburItem(unstackable().rarity(Rarity.UNCOMMON).fireResistant()));
	//Armor

	//Material
	public static final Item orichalcos = make(prefix(LibItemNames.ORICHALCOS), new Item(defaultBuilder().rarity(Rarity.EPIC).fireResistant()));
	public static final Item photonium = make(prefix(LibItemNames.PHOTONIUM), new Item(defaultBuilder()));
	public static final Item shadowium = make(prefix(LibItemNames.SHADOWIUM), new Item(defaultBuilder()));
	public static final Item aerialite = make(prefix(LibItemNames.AERIALITE_INGOT), new Item(defaultBuilder()));
	public static final Item orichalcosNugget = make(prefix(LibItemNames.ORICHALCOS_NUGGET), new Item(defaultBuilder().fireResistant()));
	public static final Item photoniumNugget = make(prefix(LibItemNames.PHOTONIUM_NUGGET), new Item(defaultBuilder()));
	public static final Item shadowiumNugget = make(prefix(LibItemNames.SHADOWIUM_NUGGET), new Item(defaultBuilder()));
	public static final Item aerialiteNugget = make(prefix(LibItemNames.AERIALITE_NUGGET), new Item(defaultBuilder()));
	public static final Item gaiaQuartz = make(prefix(LibItemNames.GAIA_QUARTZ), new Item(defaultBuilder()));
	public static final Item elementiumQuartz = make(prefix(LibItemNames.ELEMENTIUM_QUARTZ), new Item(defaultBuilder()));

	public static final Item lensPush = make(prefix(LibItemNames.LENS_PUSH), new LensItem(stackTo16(), new PushLens(), LensItem.PROP_INTERACTION | LensItem.PROP_TOUCH));
	public static final Item lensSmelt = make(prefix(LibItemNames.LENS_SMELT), new LensItem(stackTo16(), new SmeltLens(), LensItem.PROP_TOUCH | LensItem.PROP_INTERACTION));
	public static final Item lensMana = make(prefix(LibItemNames.LENS_MANA), new LensItem(stackTo16(), new ManaLens(), LensItem.PROP_TOUCH | LensItem.PROP_INTERACTION));
	public static final Item lensPotion = make(prefix(LibItemNames.LENS_POTION), new PotionLensItem(stackTo16(), new PotionLens(), LensItem.PROP_INTERACTION));
	public static final Item lensTrace = make(prefix(LibItemNames.LENS_TRACE), new LensItem(stackTo16(), new TraceLens(), LensItem.PROP_TOUCH | LensItem.PROP_INTERACTION));
	public static final Item dasRheingold = make(prefix(LibItemNames.DAS_RHEINGOLD), new DasRheingoldItem(defaultBuilder()));
	public static final Item gildedPotato = make(prefix(LibItemNames.GILDED_POTATO), new GildedPotatoItem(defaultBuilder()));
	public static final Item gildedPotatoMashed = make(prefix(LibItemNames.GILDED_POTATO_MASHED), new Item(defaultBuilder()));
	public static final Item friedChicken = make(prefix(LibItemNames.FRIED_CHICKEN), new Item(defaultBuilder()
			.food(new FoodProperties.Builder()
					.nutrition(8)
					.saturationModifier(0.5F)
					.alwaysEdible()
					.build()
			)
	));
	public static final Item heroMedal = make(prefix(LibItemNames.HERO_MEDAL), new Item(defaultBuilder().rarity(Rarity.UNCOMMON)));//OT
	public static final Item challengeTicket = make(prefix(LibItemNames.CHALLENGE_TICKET), new ChallangeTicketItem(defaultBuilder()));
	public static final Item nightmareFuel = make(prefix(LibItemNames.NIGHTMARE_FUEL), new NightmareFuelItem(defaultBuilder()
			.food(new FoodProperties.Builder().nutrition(0).saturationModifier(0.3F).alwaysEdible()
					.effect(() -> new MobEffectInstance(MobEffects.HARM, 1, 1), 1.0F)
					.effect(() -> new MobEffectInstance(MobEffects.BLINDNESS, 500, 1), 1.0F)
					.effect(() -> new MobEffectInstance(MobEffects.WEAKNESS, 500, 1), 1.0F)
					.effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 500, 1), 1.0F)
					.effect(() -> new MobEffectInstance(MobEffects.UNLUCK, 500, 1), 1.0F)
					.build()
			)
	));
	public static final Item spiritFuel = make(prefix(LibItemNames.SPIRIT_FUEL), new SpiritFuelItem(defaultBuilder()
			.food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.3F).alwaysEdible()
					.effect(() -> new MobEffectInstance(MobEffects.HEAL, 1, 1), 1.0F)
					.effect(() -> new MobEffectInstance(MobEffects.NIGHT_VISION, 500, 1), 1.0F)
					.effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, 500, 1), 1.0F)
					.effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 500, 1), 1.0F)
					.effect(() -> new MobEffectInstance(MobEffects.LUCK, 500, 1), 1.0F)
					.build()
			)
	));
	public static final Item spiritFragment = make(prefix(LibItemNames.SPIRIT_FRAGMENT), new Item(defaultBuilder()));
	public static final Item manaGlassBottle = make(prefix(LibItemNames.MANA_GLASS_BOTTLE), new ManaGlassBottleItem(defaultBuilder()));

	public static final Item theChaos = make(prefix(LibItemNames.THE_CHAOS), new Item(defaultBuilder()));
	public static final Item theOrigin = make(prefix(LibItemNames.THE_ORIGIN), new Item(defaultBuilder()));
	public static final Item theEnd = make(prefix(LibItemNames.THE_END), new Item(defaultBuilder()));
	public static final Item theUniverse = make(prefix(LibItemNames.THE_UNIVERSE), new Item(defaultBuilder()));

	public static final Item einsRewardBag = make(prefix(LibItemNames.EINS_REWARD_BAG), new RewardBagItem(defaultBuilder(), prefix("eins")));
	// Preserve the exact IDs used by deployed scex.1 inventories and storage keys.
	public static final Item candyBag = make(prefix("candy_bag"), new CandyBagItem(defaultBuilder()));
	public static final Item elementRune = make(prefix("element_rune"), new Item(defaultBuilder()));
	public static final Item sinRune = make(prefix("sin_rune"), new Item(defaultBuilder()));
	public static final Item recordHerrscherOfTheVoid = make(prefix("music_disc_herrscher_of_the_void"),
			new Item(unstackable().rarity(Rarity.EPIC).jukeboxPlayable(ExtraBotanyJukeboxSongs.HERRSCHER_OF_THE_VOID)));
	public static final Item candyEins = make(prefix("candy_eins"), new CandyItem(defaultBuilder(), MobEffects.MOVEMENT_SPEED));
	public static final Item candyZwei = make(prefix("candy_zwei"), new CandyItem(defaultBuilder(), MobEffects.JUMP));
	public static final Item candyDrei = make(prefix("candy_drei"), new CandyItem(defaultBuilder(), MobEffects.DIG_SPEED));
	public static final Item zweiRewardBag = make(prefix(LibItemNames.ZWEI_REWARD_BAG), new RewardBagItem(defaultBuilder(), prefix("zwei")));
	public static final Item dreiRewardBag = make(prefix(LibItemNames.DREI_REWARD_BAG), new RewardBagItem(defaultBuilder(), prefix("drei")));
	public static final Item vierRewardBag = make(prefix(LibItemNames.VIER_REWARD_BAG), new RewardBagItem(defaultBuilder(), prefix("vier")));
	public static final Item nineAndThreeQuartersRewardBag = make(prefix(LibItemNames.NINE_AND_THREE_QUARTERS_REWARD_BAG), new RewardBagItem(defaultBuilder(), prefix("nine_and_three_quarters")));
	public static final Item pandorasBox = make(prefix(LibItemNames.PANDORAS_BOX), new PandorasBoxItem(unstackable().rarity(Rarity.UNCOMMON), prefix("pandoras_box")));

	public static final Item recordGaia3 = make(prefix(LibItemNames.RECORD_GAIA3),
			new Item(unstackable().rarity(Rarity.EPIC).jukeboxPlayable(ExtraBotanyJukeboxSongs.GAIA_3)));
	//public static final Item recordHerrscherOfTheVoid = make(prefix(LibItemNames.RECORD_HERRSCHER_OF_THE_VOID), new BotaniaRecordItem(1, ExtraBotanySounds.MUSIC_GAIA3, unstackable().rarity(Rarity.EPIC), 201));

	//Brews
	public static final Item manaCocktail = make(prefix(LibItemNames.MANA_COCKTAIL), new ManaCocktailItem(unstackable().craftRemainder(manaGlassBottle), 8, 32, () -> manaGlassBottle));
	public static final Item infiniteWine = make(prefix(LibItemNames.INFINITE_WINE), new InfiniteWineItem(unstackable().rarity(Rarity.RARE).craftRemainder(manaGlassBottle), 12, 18, () -> manaGlassBottle));
	public static final Item holyWaterGrenade = make(prefix(LibItemNames.HOLY_WATER_GRENADE), new HolyWaterGrenadeItem(stackTo4()));

	//For tags
	public static final Item[] REWARD_BAGS = {
			einsRewardBag, zweiRewardBag, dreiRewardBag, vierRewardBag,
			nineAndThreeQuartersRewardBag, pandorasBox, candyBag,
	};
	public static final Item[] HAMMERS = {
			manasteelHammer, elementiumHammer, terrasteelHammer, gaiaHammer,
			photoniumHammer, shadowiumHammer, aerialiteHammer, orichalcosHammer,
			rheinHammer
	};
	public static final Item[] PICKAXES = {

	};

	public static final Item[] SWORDS = {
			excalibur,
	};
	public static final Item[] BOWS = {
			failnaught
	};
	public static final Item[] SHIELDS = {
			manasteelShield, elementiumShield, terrasteelShield, achillesShield
	};
	public static final Item[] MANA_USING_ITEM = {
			camera, failnaught, excalibur, featherOfJingwei, magicFinger, coreOfTheVoid, voidArchives,
			dispersiveRing, curseRing, pureDaisyPendant,
			starryIdolHeadgear, starryIdolSuit, starryIdolSkirt, starryIdolBoots,
			pleiadesCombatMaidHeadgear, pleiadesCombatMaidSuit, pleiadesCombatMaidSkirt, pleiadesCombatMaidBoots, sanguinePleiadesCombatMaidSuit,
			shadowWarriorHelmet, shadowWarriorChestplate, shadowWarriorLeggings, shadowWarriorBoots,
			goblinSlayerHelmet, goblinSlayerChestplate, goblinSlayerLeggings, goblinSlayerBoots
	};

	public static final Item[] REPLICATOR_BLACKLIST = {
			failnaught, manaRingMaster, excalibur
	};
	//Bauble
	public static final Item[] RINGS = {
			manaRingMaster, frostRing, dispersiveRing, curseRing,
			spiderRing, jackieChanRing, parkourRing
	};

	public static final Item[] BODY = {
			coreOfTheVoid
	};

	public static final Item[] ALL_SLOT = {
			featherOfJingwei, natureOrb
	};

	private static <T extends Item> T make(ResourceLocation id, T item) {
		var old = ALL.put(id, item);
		if (old != null) {
			throw new IllegalArgumentException("Typo? Duplicate id " + id);
		}
		return item;
	}

	public static Item.Properties defaultBuilderCustomDamage() {
		return XplatAbstractions.instance().defaultItemBuilderWithCustomDamageOnFabric();
	}

	public static Item.Properties unstackableCustomDamage() {
		return defaultBuilderCustomDamage().stacksTo(1);
	}

	private static Item.Properties armorProperties(ArmorItem.Type type, int durabilityFactor) {
		return unstackableCustomDamage().durability(type.getDurability(durabilityFactor));
	}

	public static Item.Properties defaultBuilder() {
		return XplatAbstractions.instance().defaultItemBuilder();
	}

	private static Item.Properties stackTo16() {
		return defaultBuilder().stacksTo(16);
	}

	private static Item.Properties stackTo4() {
		return defaultBuilder().stacksTo(4);
	}

	private static Item.Properties unstackable() {
		return defaultBuilder().stacksTo(1);
	}

	public static void registerRecipeSerializers(BiConsumer<RecipeSerializer<?>, ResourceLocation> r) {
		r.accept(WandOfTheForestExtendRecipe.SERIALIZER, prefix("wand_of_the_forest_extension"));
		r.accept(CopyBrewFormFlaskRecipe.SERIALIZER, prefix("copy_brew_from_flask"));
		r.accept(CopyBrewFromManaCocktailRecipe.SERIALIZER, prefix("copy_brew_from_mana_cocktail"));
		r.accept(DasRheingoldChangeSoulBoundRecipe.SERIALIZER, prefix("das_rheingold_change_bind"));
		r.accept(PotionLensChangePotionRecipe.SERIALIZER, prefix("potion_lens_change_potion"));
	}

	public static void registerItems(BiConsumer<Item, ResourceLocation> r) {
		io.github.lounode.extrabotany.common.item.legacy.LegacyJudahItem.ITEMS.forEach((id, item) -> ALL.putIfAbsent(prefix(id), item));
		io.github.lounode.extrabotany.common.item.legacy.LegacyRelicSword.ITEMS.forEach((id, item) -> ALL.putIfAbsent(prefix(id), item));
		ALL.putIfAbsent(prefix("flamescion_weapon"), io.github.lounode.extrabotany.common.item.legacy.LegacyFlamescionItem.INSTANCE);
		ALL.putIfAbsent(prefix("voidcaller"), io.github.lounode.extrabotany.common.item.legacy.LegacyVoidCallerItem.INSTANCE);
		ALL.putIfAbsent(prefix("king_garden"), io.github.lounode.extrabotany.common.item.legacy.LegacyKingGardenItem.INSTANCE);
		io.github.lounode.extrabotany.common.item.legacy.LegacyMountItems.ITEMS.forEach((id, item) -> ALL.putIfAbsent(prefix(id), item));
		ALL.putIfAbsent(prefix("silver_bullet"), io.github.lounode.extrabotany.common.item.legacy.LegacySilverBulletItem.INSTANCE);
		ALL.putIfAbsent(prefix("sun_ring"), io.github.lounode.extrabotany.common.item.legacy.LegacyCelestialAccessories.SUN);
		ALL.putIfAbsent(prefix("moon_pendant"), io.github.lounode.extrabotany.common.item.legacy.LegacyCelestialAccessories.MOON);
		ALL.putIfAbsent(prefix("elven_king"), io.github.lounode.extrabotany.common.item.legacy.LegacyBoxSimulator.ELVEN_KING);
		ALL.putIfAbsent(prefix("all_for_one"), io.github.lounode.extrabotany.common.item.legacy.LegacyBoxSimulator.ALL_FOR_ONE);
		ALL.putIfAbsent(prefix("limited_edition_supply_bag"), io.github.lounode.extrabotany.common.item.legacy.LegacySupplyBagItem.INSTANCE);
		ALL.putIfAbsent(prefix("silent_eternity"), io.github.lounode.extrabotany.common.item.legacy.LegacyEternityItem.INSTANCE);
		ALL.putIfAbsent(prefix("bottled_flame"), io.github.lounode.extrabotany.common.item.legacy.LegacyBottledFlameItem.INSTANCE);
		io.github.lounode.extrabotany.common.item.legacy.LegacyAccessories.ITEMS.forEach((id, item) -> ALL.putIfAbsent(prefix(id), item));
		io.github.lounode.extrabotany.common.item.legacy.LegacyCosmetics.ITEMS.forEach((id, item) -> ALL.putIfAbsent(prefix(id), item));
		io.github.lounode.extrabotany.common.item.legacy.LegacyArmorItem.ITEMS.forEach((id, item) -> ALL.putIfAbsent(prefix(id), item));
		io.github.lounode.extrabotany.common.item.legacy.LegacyTools.ITEMS.forEach((id, item) -> ALL.putIfAbsent(prefix(id), item));
		for (var e : ALL.entrySet()) {
			r.accept(e.getValue(), e.getKey());
		}
	}

	private static class HiddenLensItem extends LensItem implements CustomCreativeTabContents {

		public HiddenLensItem(Properties builder, Lens lens, int props) {
			super(builder, lens, props);
		}

		@Override
		public void addToCreativeTab(Item me, CreativeModeTab.Output output) {

		}
	}
}
