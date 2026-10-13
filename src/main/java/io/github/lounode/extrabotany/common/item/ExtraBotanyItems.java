package io.github.lounode.extrabotany.common.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.crafting.RecipeSerializer;

import vazkii.botania.common.item.lens.LensItem;
import vazkii.botania.xplat.XplatAbstractions;

import io.github.lounode.extrabotany.common.crafting.recipe.*;
import io.github.lounode.extrabotany.common.item.brew.HolyWaterGrenadeItem;
import io.github.lounode.extrabotany.common.item.brew.InfiniteWineItem;
import io.github.lounode.extrabotany.common.item.brew.ManaCocktailItem;
import io.github.lounode.extrabotany.common.item.brew.ManaGlassBottleItem;
import io.github.lounode.extrabotany.common.item.equipment.armor.goblin_slayer.GoblinSlayerArmorItem;
import io.github.lounode.extrabotany.common.item.equipment.armor.goblin_slayer.GoblinSlayerHelmetItem;
import io.github.lounode.extrabotany.common.item.equipment.armor.miku.MikuArmorItem;
import io.github.lounode.extrabotany.common.item.equipment.armor.pleiades_combat_maid.PleiadesCombatMaidArmorItem;
import io.github.lounode.extrabotany.common.item.equipment.armor.pleiades_combat_maid.PleiadesCombatMaidSuitItem;
import io.github.lounode.extrabotany.common.item.equipment.armor.pleiades_combat_maid.SanguinePleiadesCombatMaidSuitItem;
import io.github.lounode.extrabotany.common.item.equipment.armor.shadow_warrior.ShadowWarriorArmorItem;
import io.github.lounode.extrabotany.common.item.equipment.armor.shadow_warrior.ShadowWarriorHelmetItem;
import io.github.lounode.extrabotany.common.item.equipment.armor.shooting_guardian.ShootingGuardianArmorItem;
import io.github.lounode.extrabotany.common.item.equipment.armor.shooting_guardian.ShootingGuardianHelmetItem;
import io.github.lounode.extrabotany.common.item.equipment.armor.silent_sages.SilentSagesArmorItem;
import io.github.lounode.extrabotany.common.item.equipment.armor.starry_idol.StarryIdolArmorItem;
import io.github.lounode.extrabotany.common.item.equipment.armor.starry_idol.StarryIdolHeadgearItem;
import io.github.lounode.extrabotany.common.item.equipment.bauble.*;
import io.github.lounode.extrabotany.common.item.equipment.shield.ElementiumShieldItem;
import io.github.lounode.extrabotany.common.item.equipment.shield.ManasteelShieldItem;
import io.github.lounode.extrabotany.common.item.equipment.shield.TerrasteelShieldItem;
import io.github.lounode.extrabotany.common.item.equipment.tool.BinderItem;
import io.github.lounode.extrabotany.common.item.equipment.tool.BottledPixieItem;
import io.github.lounode.extrabotany.common.item.equipment.tool.BottledStarItem;
import io.github.lounode.extrabotany.common.item.equipment.tool.MagicFingerItem;
import io.github.lounode.extrabotany.common.item.equipment.tool.PhotonShotgunItem;
import io.github.lounode.extrabotany.common.item.equipment.tool.RodOfDiscordItem;
import io.github.lounode.extrabotany.common.item.equipment.tool.FlamescionWeaponItem;
import io.github.lounode.extrabotany.common.item.equipment.tool.ShadowKatanaItem;
import io.github.lounode.extrabotany.common.item.equipment.tool.SilverBulletItem;
import io.github.lounode.extrabotany.common.item.equipment.tool.UuzFanItem;
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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 本模组的全部物品（方块物品除外，它们在 {@code ExtraBotanyBlocks} 和 {@code ExtrabotanyFlowerBlocks} 里）。
 * All items of this mod (but not the block items, which are in {@code ExtraBotanyBlocks} and {@code ExtrabotanyFlowerBlocks}).
 *
 * <p>注册 ID 不能改。旧存档里的物品靠 ID 找到自己，ID 变了物品就会消失。
 * <p>The registry IDs must not change. An item in an old save finds itself by its ID, and the item goes away when the ID changes.
 *
 * <p>物品按声明的顺序注册，创造模式物品栏也按这个顺序显示。
 * <p>The items are registered in the sequence of their declarations, and the creative tab shows them in this sequence.
 */
public final class ExtraBotanyItems {
	public static final Map<ResourceLocation, Item> ALL = new LinkedHashMap<>();

	// 这些注册 ID 来自旧版本。它们保持不变，这样旧存档里的物品不会消失。
	// These registry IDs come from old versions. They stay as they are, so that the items in old saves do not go away.
	public static final Item binder = make(prefix("binder"), new BinderItem(unstackable()));
	public static final Item universalPetal = make(prefix("universal_petal"), new Item(defaultBuilder()));
	public static final Item emptyCoreOfTheVoid = make(prefix("empty_core_of_the_void"), new Item(unstackable().rarity(Rarity.EPIC)));
	public static final Item manaDrink = make(prefix("mana_drink"), new ManaDrinkItem(defaultBuilder()
			.food(new FoodProperties.Builder().nutrition(1).saturationModifier(0.1F).alwaysEdible().build())));
	public static final Item lensSuperconductor = make(prefix("lens_superconductor"), new LensItem(stackTo16(), new SuperconductorLens(), LensItem.PROP_POWER));

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

	// 来自原版（ExtraMeteorP）的饰品。注册 ID 和旧存档保持一致。
	// Baubles from the original (ExtraMeteorP). The registry IDs agree with old saves.
	public static final Item aeroStone = make(prefix("aero_stone"), new AeroStoneItem(unstackable()));
	public static final Item aquaStone = make(prefix("aqua_stone"), new AquaStoneItem(unstackable()));
	public static final Item earthStone = make(prefix("earth_stone"), new EarthStoneItem(unstackable()));
	public static final Item ignisStone = make(prefix("ignis_stone"), new IgnisStoneItem(unstackable()));
	public static final Item theCommunity = make(prefix("the_community"), new TheCommunityItem(unstackable()));
	public static final Item peaceAmulet = make(prefix("peace_amulet"), new PeaceAmuletItem(unstackable()));
	public static final Item frostStar = make(prefix("frost_star"), new FrostStarItem(unstackable()));
	public static final Item deathRing = make(prefix("death_ring"), new DeathRingItem(unstackable()));
	public static final Item powerGlove = make(prefix("power_glove"), new PowerGloveItem(unstackable()));
	public static final Item potatoChips = make(prefix("potato_chips"), new PotatoChipsItem(unstackable()));
	public static final Item gemOfConquest = make(prefix("gem_of_conquest"), new GemOfConquestItem(unstackable().rarity(Rarity.EPIC)));
	public static final Item bottledFlame = make(prefix("bottled_flame"), new BottledFlameItem(unstackable()));
	public static final Item sunRing = make(prefix("sun_ring"), new SunRingItem(unstackable().rarity(Rarity.EPIC).fireResistant()));
	public static final Item moonPendant = make(prefix("moon_pendant"), new MoonPendantItem(unstackable().rarity(Rarity.EPIC).fireResistant()));

	// 装饰饰品。
	// Cosmetic baubles.
	public static final Item foxEar = make(prefix("fox_ear"), new CosmeticBaubleItem(CosmeticBaubleItem.Variant.FOX_EAR, unstackable()));
	public static final Item foxMask = make(prefix("fox_mask"), new CosmeticBaubleItem(CosmeticBaubleItem.Variant.FOX_MASK, unstackable()));
	public static final Item pylon = make(prefix("pylon"), new CosmeticBaubleItem(CosmeticBaubleItem.Variant.PYLON, unstackable()));
	public static final Item goggleJungle = make(prefix("goggle_jungle"), new CosmeticBaubleItem(CosmeticBaubleItem.Variant.GOGGLE_JUNGLE, unstackable()));
	public static final Item goggleOcean = make(prefix("goggle_ocean"), new CosmeticBaubleItem(CosmeticBaubleItem.Variant.GOGGLE_OCEAN, unstackable()));
	public static final Item goggleSnowfield = make(prefix("goggle_snowfield"), new CosmeticBaubleItem(CosmeticBaubleItem.Variant.GOGGLE_SNOWFIELD, unstackable()));
	public static final Item goggleStandard = make(prefix("goggle_standard"), new CosmeticBaubleItem(CosmeticBaubleItem.Variant.GOGGLE_STANDARD, unstackable()));
	public static final Item blackGlasses = make(prefix("black_glasses"), new CosmeticBaubleItem(CosmeticBaubleItem.Variant.BLACK_GLASSES, unstackable()));
	public static final Item thugLife = make(prefix("thug_life"), new CosmeticBaubleItem(CosmeticBaubleItem.Variant.THUG_LIFE, unstackable()));
	public static final Item superCrown = make(prefix("super_crown"), new CosmeticBaubleItem(CosmeticBaubleItem.Variant.SUPER_CROWN, unstackable()));
	public static final Item mask = make(prefix("mask"), new CosmeticBaubleItem(CosmeticBaubleItem.Variant.MASK, unstackable()));
	public static final Item redScarf = make(prefix("red_scarf"), new CosmeticBaubleItem(CosmeticBaubleItem.Variant.RED_SCARF, unstackable()));

	// 来自原版（ExtraMeteorP）的工具。
	// Tools from the original (ExtraMeteorP).
	public static final Item rodOfDiscord = make(prefix("rod_of_discord"), new RodOfDiscordItem(unstackable()));
	public static final Item shadowKatana = make(prefix("shadow_katana"), new ShadowKatanaItem(unstackable()));
	public static final Item uuzFan = make(prefix("uuz_fan"), new UuzFanItem(unstackable()));
	public static final Item bottledPixie = make(prefix("bottled_pixie"), new BottledPixieItem(unstackable()));
	public static final Item bottledStar = make(prefix("bottled_star"), new BottledStarItem(unstackable()));
	public static final Item photonShotgun = make(prefix("photon_shotgun"), new PhotonShotgunItem(unstackable()));

	// 来自原版（ExtraMeteorP）的遗物剑。
	// Relic swords from the original (ExtraMeteorP).
	public static final Item trueTerrablade = make(prefix("true_terrablade"), new TrueTerrabladeItem(relic()));
	public static final Item trueShadowKatana = make(prefix("true_shadow_katana"), new TrueShadowKatanaItem(relic()));
	public static final Item influxWaver = make(prefix("influx_waver"), new InfluxWaverItem(relic()));
	public static final Item starWrath = make(prefix("star_wrath"), new StarWrathItem(relic()));
	public static final Item firstFractal = make(prefix("first_fractal"), new FirstFractalItem(relic()));
	public static final Item spearOfSubspace = make(prefix("spear_of_subspace"), new SpearOfSubspaceItem(relic()));
	public static final Item judahOath = make(prefix("judah_oath"), new JudahOathItem(JudahOathItem.Variant.JUDAH, relic()));
	public static final Item judahOathKira = make(prefix("judah_oath_kira"), new JudahOathItem(JudahOathItem.Variant.KIRA, relic()));
	public static final Item judahOathSakura = make(prefix("judah_oath_sakura"), new JudahOathItem(JudahOathItem.Variant.SAKURA, relic()));

	// 来自原版 1.12 的遗物和物品。
	// Relics and items from the original 1.12.
	public static final Item kingGarden = make(prefix("king_garden"), new KingGardenItem(relic()));
	public static final Item silentEternity = make(prefix("silent_eternity"), new SilentEternityItem(relic()));
	public static final Item elvenKing = make(prefix("elven_king"), new AllForOneRingItem(AllForOneRingItem.ELVEN_KING_SLOTS, relic()));
	public static final Item allForOne = make(prefix("all_for_one"), new AllForOneRingItem(AllForOneRingItem.ALL_SLOTS, relic()));

	// 来自原版（ExtraMeteorP）的三套盔甲和银翼射手。
	// The three armor sets and the Silver Bullet from the original (ExtraMeteorP).
	public static final Item mikuHelm = make(prefix("miku_helm"),
			new MikuArmorItem(ArmorItem.Type.HELMET, armorProperties(ArmorItem.Type.HELMET, ArmorsMaterial.MIKU_DURABILITY_FACTOR)));
	public static final Item mikuChest = make(prefix("miku_chest"),
			new MikuArmorItem(ArmorItem.Type.CHESTPLATE, armorProperties(ArmorItem.Type.CHESTPLATE, ArmorsMaterial.MIKU_DURABILITY_FACTOR)));
	public static final Item mikuLegs = make(prefix("miku_legs"),
			new MikuArmorItem(ArmorItem.Type.LEGGINGS, armorProperties(ArmorItem.Type.LEGGINGS, ArmorsMaterial.MIKU_DURABILITY_FACTOR)));
	public static final Item mikuBoots = make(prefix("miku_boots"),
			new MikuArmorItem(ArmorItem.Type.BOOTS, armorProperties(ArmorItem.Type.BOOTS, ArmorsMaterial.MIKU_DURABILITY_FACTOR)));

	public static final Item shootingGuardianHelm = make(prefix("shootingguardian_helm"),
			new ShootingGuardianHelmetItem(armorProperties(ArmorItem.Type.HELMET, ArmorsMaterial.SHOOTING_GUARDIAN_DURABILITY_FACTOR)));
	public static final Item shootingGuardianChest = make(prefix("shootingguardian_chest"),
			new ShootingGuardianArmorItem(ArmorItem.Type.CHESTPLATE, armorProperties(ArmorItem.Type.CHESTPLATE, ArmorsMaterial.SHOOTING_GUARDIAN_DURABILITY_FACTOR)));
	public static final Item shootingGuardianLegs = make(prefix("shootingguardian_legs"),
			new ShootingGuardianArmorItem(ArmorItem.Type.LEGGINGS, armorProperties(ArmorItem.Type.LEGGINGS, ArmorsMaterial.SHOOTING_GUARDIAN_DURABILITY_FACTOR)));
	public static final Item shootingGuardianBoots = make(prefix("shootingguardian_boots"),
			new ShootingGuardianArmorItem(ArmorItem.Type.BOOTS, armorProperties(ArmorItem.Type.BOOTS, ArmorsMaterial.SHOOTING_GUARDIAN_DURABILITY_FACTOR)));

	public static final Item silentSagesHelm = make(prefix("silentsages_helm"),
			new SilentSagesArmorItem(ArmorItem.Type.HELMET, armorProperties(ArmorItem.Type.HELMET, ArmorsMaterial.SILENT_SAGES_DURABILITY_FACTOR)));
	public static final Item silentSagesChest = make(prefix("silentsages_chest"),
			new SilentSagesArmorItem(ArmorItem.Type.CHESTPLATE, armorProperties(ArmorItem.Type.CHESTPLATE, ArmorsMaterial.SILENT_SAGES_DURABILITY_FACTOR)));
	public static final Item silentSagesLegs = make(prefix("silentsages_legs"),
			new SilentSagesArmorItem(ArmorItem.Type.LEGGINGS, armorProperties(ArmorItem.Type.LEGGINGS, ArmorsMaterial.SILENT_SAGES_DURABILITY_FACTOR)));
	public static final Item silentSagesBoots = make(prefix("silentsages_boots"),
			new SilentSagesArmorItem(ArmorItem.Type.BOOTS, armorProperties(ArmorItem.Type.BOOTS, ArmorsMaterial.SILENT_SAGES_DURABILITY_FACTOR)));

	public static final Item silverBullet = make(prefix("silver_bullet"), new SilverBulletItem(unstackable()));
	public static final Item flamescionWeapon = make(prefix("flamescion_weapon"),
			new FlamescionWeaponItem(unstackable().rarity(Rarity.EPIC).fireResistant()));

	// 坐骑物品和坐骑饰品。
	// The mount items and the mount accessories.
	public static final Item motor = make(prefix("motor"), new MotorItem(unstackable().rarity(Rarity.EPIC)));
	public static final Item cosmicCarKey = make(prefix("cosmic_car_key"), new CosmicCarKeyItem(unstackable().rarity(Rarity.EPIC)));
	public static final Item motorAccessory = make(prefix("motor_accessory"), new MotorAccessoryItem(unstackable().rarity(Rarity.EPIC)));
	public static final Item cosmicCarKeyAccessory = make(prefix("cosmic_car_key_accessory"),
			new CosmicCarKeyAccessoryItem(unstackable().rarity(Rarity.EPIC)));

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
	// 这几个 ID 和 scex.1 版本相同，那个版本的存档里已经有这些物品。
	// These IDs are the same as in the scex.1 version; saves of that version already have these items.
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
	public static final Item limitedEditionSupplyBag = make(prefix("limited_edition_supply_bag"), new SupplyBagItem(defaultBuilder().rarity(Rarity.RARE)));
	public static final Item pandorasBox = make(prefix(LibItemNames.PANDORAS_BOX), new PandorasBoxItem(unstackable().rarity(Rarity.UNCOMMON), prefix("pandoras_box")));

	public static final Item recordGaia3 = make(prefix(LibItemNames.RECORD_GAIA3),
			new Item(unstackable().rarity(Rarity.EPIC).jukeboxPlayable(ExtraBotanyJukeboxSongs.GAIA_3)));
	// 本我的战斗音乐的唱片。来源：ExtraMeteorP 1.16 的 recordego。
	// The record of the fight music of the Ego. Source: recordego of ExtraMeteorP 1.16.
	public static final Item recordEgo = make(prefix(LibItemNames.RECORD_EGO),
			new Item(unstackable().rarity(Rarity.EPIC).jukeboxPlayable(ExtraBotanyJukeboxSongs.EGO)));

	//Brews
	public static final Item manaCocktail = make(prefix(LibItemNames.MANA_COCKTAIL), new ManaCocktailItem(unstackable().craftRemainder(manaGlassBottle), 8, 32, () -> manaGlassBottle));
	public static final Item infiniteWine = make(prefix(LibItemNames.INFINITE_WINE), new InfiniteWineItem(unstackable().rarity(Rarity.RARE).craftRemainder(manaGlassBottle), 12, 18, () -> manaGlassBottle));
	public static final Item holyWaterGrenade = make(prefix(LibItemNames.HOLY_WATER_GRENADE), new HolyWaterGrenadeItem(stackTo4()));
	public static final Item voidCaller = make(prefix("voidcaller"), new VoidCallerItem(defaultBuilder().rarity(Rarity.EPIC)));

	// 下面的分组只给标签生成器使用。加新物品时，把它放进对应的分组，然后重新运行 runData。
	// The groups below are only for the tag generators. When a new item is added, put it into the related group, and then run runData again.
	public static final Item[] REWARD_BAGS = {
			einsRewardBag, zweiRewardBag, dreiRewardBag, vierRewardBag,
			nineAndThreeQuartersRewardBag, pandorasBox, candyBag, limitedEditionSupplyBag
	};
	public static final Item[] HAMMERS = {
			manasteelHammer, elementiumHammer, terrasteelHammer, gaiaHammer,
			photoniumHammer, shadowiumHammer, aerialiteHammer, orichalcosHammer,
			rheinHammer
	};

	public static final Item[] SWORDS = {
			excalibur,
			shadowKatana, flamescionWeapon, trueTerrablade, trueShadowKatana, influxWaver, starWrath,
			firstFractal, spearOfSubspace, judahOath, judahOathKira, judahOathSakura
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
			goblinSlayerHelmet, goblinSlayerChestplate, goblinSlayerLeggings, goblinSlayerBoots,
			// 原版（ExtraMeteorP）的内容。 / Content of the original (ExtraMeteorP).
			trueTerrablade, trueShadowKatana, influxWaver, starWrath, firstFractal, spearOfSubspace,
			judahOath, judahOathKira, judahOathSakura, rodOfDiscord, bottledStar, bottledPixie,
			photonShotgun, silverBullet, kingGarden, flamescionWeapon, cosmicCarKey, motor,
			silentEternity, frostStar, deathRing, potatoChips, sunRing, moonPendant,
			mikuHelm, mikuChest, mikuLegs, mikuBoots,
			shootingGuardianHelm, shootingGuardianChest, shootingGuardianLegs, shootingGuardianBoots,
			silentSagesHelm, silentSagesChest, silentSagesLegs, silentSagesBoots
	};


	//Bauble
	public static final Item[] RINGS = {
			manaRingMaster, frostRing, dispersiveRing, curseRing,
			spiderRing, jackieChanRing, parkourRing,
			sunRing, deathRing, frostStar, elvenKing, allForOne
	};

	public static final Item[] NECKLACES = {
			pureDaisyPendant, moonPendant
	};

	public static final Item[] BODY = {
			coreOfTheVoid, redScarf
	};

	public static final Item[] ALL_SLOT = {
			featherOfJingwei, natureOrb, gemOfConquest,
			peaceAmulet, aeroStone, aquaStone, earthStone, ignisStone, theCommunity, powerGlove, silentEternity
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

	/**
	 * 遗物的物品属性：不可堆叠、史诗稀有度、防火。
	 * The item properties of a relic: not stackable, epic rarity, fire resistant.
	 */
	private static Item.Properties relic() {
		return unstackable().rarity(Rarity.EPIC).fireResistant();
	}

	public static void registerRecipeSerializers(BiConsumer<RecipeSerializer<?>, ResourceLocation> r) {
		r.accept(WandOfTheForestExtendRecipe.SERIALIZER, prefix("wand_of_the_forest_extension"));
		r.accept(CopyBrewFormFlaskRecipe.SERIALIZER, prefix("copy_brew_from_flask"));
		r.accept(CopyBrewFromManaCocktailRecipe.SERIALIZER, prefix("copy_brew_from_mana_cocktail"));
		r.accept(DasRheingoldChangeSoulBoundRecipe.SERIALIZER, prefix("das_rheingold_change_bind"));
		r.accept(PotionLensChangePotionRecipe.SERIALIZER, prefix("potion_lens_change_potion"));
	}

	public static void registerItems(BiConsumer<Item, ResourceLocation> r) {
		for (var e : ALL.entrySet()) {
			r.accept(e.getValue(), e.getKey());
		}
	}
}
