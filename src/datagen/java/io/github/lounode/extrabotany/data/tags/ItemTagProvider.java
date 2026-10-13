package io.github.lounode.extrabotany.data.tags;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;

import vazkii.botania.common.item.lens.LensItem;
import vazkii.botania.common.lib.BotaniaTags;

import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.lib.ExtraBotanyTags;
import io.github.lounode.extrabotany.common.lib.LibMisc;

import java.util.concurrent.CompletableFuture;

import static io.github.lounode.extrabotany.common.item.ExtraBotanyItems.*;

/**
 * 生成物品标签：原版的标签（剑、盔甲等）、植物魔法的标签（使用魔力的物品、符文、透镜、花）、本模组自己的标签、NeoForge 的通用标签，以及 Curios 的栏位标签。
 * Makes the item tags: vanilla tags (swords, armor and others), tags of Botania (items that use mana, runes, lenses, flowers), the tags of this mod, the common tags of NeoForge, and the slot tags of Curios.
 *
 * <p>标签的内容来自 {@code ExtraBotanyItems} 里的物品分组。给模组加新物品时，把它加进对应的分组。
 * <p>The content of the tags comes from the item groups in {@code ExtraBotanyItems}. When a new item is added to the mod, put it into the related group.
 */
public class ItemTagProvider extends ItemTagsProvider {
	public ItemTagProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagsProvider.TagLookup<Block>> blockTagProvider) {
		super(packOutput, lookupProvider, blockTagProvider);
	}

	@Override
	protected void addTags(HolderLookup.Provider provider) {
		this.tag(ExtraBotanyTags.Items.INGOTS_ORICHALCOS).add(ExtraBotanyItems.orichalcos);
		this.tag(ExtraBotanyTags.Items.INGOTS_PHOTONIUM).add(ExtraBotanyItems.photonium);
		this.tag(ExtraBotanyTags.Items.INGOTS_SHADOWIUM).add(ExtraBotanyItems.shadowium);
		this.tag(ExtraBotanyTags.Items.INGOTS_AERIALITE).add(ExtraBotanyItems.aerialite);

		this.tag(ExtraBotanyTags.Items.NUGGETS_ORICHALCOS).add(ExtraBotanyItems.orichalcosNugget);
		this.tag(ExtraBotanyTags.Items.NUGGETS_PHOTONIUM).add(ExtraBotanyItems.photoniumNugget);
		this.tag(ExtraBotanyTags.Items.NUGGETS_SHADOWIUM).add(ExtraBotanyItems.shadowiumNugget);
		this.tag(ExtraBotanyTags.Items.NUGGETS_AERIALITE).add(ExtraBotanyItems.aerialiteNugget);

		this.copy(ExtraBotanyTags.Blocks.BLOCKS_ORICHALCOS, ExtraBotanyTags.Items.BLOCKS_ORICHALCOS);
		this.copy(ExtraBotanyTags.Blocks.BLOCKS_PHOTONIUM, ExtraBotanyTags.Items.BLOCKS_PHOTONIUM);
		this.copy(ExtraBotanyTags.Blocks.BLOCKS_SHADOWIUM, ExtraBotanyTags.Items.BLOCKS_SHADOWIUM);
		this.copy(ExtraBotanyTags.Blocks.BLOCKS_AERIALITE, ExtraBotanyTags.Items.BLOCKS_AERIALITE);

		//Tools
		this.tag(ItemTags.PICKAXES).add(HAMMERS);
		this.tag(ItemTags.SWORDS).add(SWORDS);
		this.tag(ExtraBotanyTags.Items.HAMMERS).add(HAMMERS);

		this.copy(BlockTags.RAILS, ItemTags.RAILS);
		this.copy(BlockTags.SLABS, ItemTags.SLABS);
		this.copy(BlockTags.PLANKS, ItemTags.PLANKS);
		this.copy(BlockTags.WOODEN_SLABS, ItemTags.WOODEN_SLABS);
		this.copy(BlockTags.STAIRS, ItemTags.STAIRS);
		this.copy(BlockTags.WOODEN_STAIRS, ItemTags.WOODEN_STAIRS);
		this.copy(BlockTags.WALLS, ItemTags.WALLS);
		this.copy(BlockTags.FENCES, ItemTags.FENCES);
		this.copy(BlockTags.WOODEN_FENCES, ItemTags.WOODEN_FENCES);

		this.copy(BotaniaTags.Blocks.MUNDANE_FLOATING_FLOWERS, BotaniaTags.Items.MUNDANE_FLOATING_FLOWERS);
		this.copy(BotaniaTags.Blocks.SPECIAL_FLOATING_FLOWERS, BotaniaTags.Items.SPECIAL_FLOATING_FLOWERS);
		this.copy(BotaniaTags.Blocks.FLOATING_FLOWERS, BotaniaTags.Items.FLOATING_FLOWERS);
		this.copy(BotaniaTags.Blocks.TALL_MYSTICAL_FLOWERS, BotaniaTags.Items.TALL_MYSTICAL_FLOWERS);
		this.copy(BotaniaTags.Blocks.SMALL_MYSTICAL_FLOWERS, BotaniaTags.Items.SMALL_MYSTICAL_FLOWERS);
		this.copy(BotaniaTags.Blocks.SHIMMERING_MUSHROOMS, BotaniaTags.Items.SHIMMERING_MUSHROOMS);

		this.copy(BotaniaTags.Blocks.MISC_SPECIAL_FLOWERS, BotaniaTags.Items.MISC_SPECIAL_FLOWERS);
		this.copy(BotaniaTags.Blocks.GENERATING_SPECIAL_FLOWERS, BotaniaTags.Items.GENERATING_SPECIAL_FLOWERS);
		this.copy(BotaniaTags.Blocks.FUNCTIONAL_SPECIAL_FLOWERS, BotaniaTags.Items.FUNCTIONAL_SPECIAL_FLOWERS);
		this.copy(BotaniaTags.Blocks.SPECIAL_FLOWERS, BotaniaTags.Items.SPECIAL_FLOWERS);
		this.copy(BotaniaTags.Blocks.MINI_FLOWERS, BotaniaTags.Items.MINI_FLOWERS);

		this.tag(ItemTags.TALL_FLOWERS).addTag(BotaniaTags.Items.TALL_MYSTICAL_FLOWERS);
		this.tag(ItemTags.SMALL_FLOWERS).addTag(BotaniaTags.Items.SMALL_MYSTICAL_FLOWERS).addTag(BotaniaTags.Items.SPECIAL_FLOWERS);

		TagsProvider.TagAppender<Item> builder = this.tag(BotaniaTags.Items.LENS);
		BuiltInRegistries.ITEM.stream().filter(i -> i instanceof LensItem && BuiltInRegistries.ITEM.getKey(i).getNamespace().equals(LibMisc.MOD_ID))
				.map(BuiltInRegistries.ITEM::getKey)
				.sorted()
				.forEach(item -> builder.add(ResourceKey.create(Registries.ITEM, item)));

		this.tag(ItemTags.PIGLIN_LOVED).add(gildedPotato, gildedPotatoMashed, dasRheingold);
		this.tag(ExtraBotanyTags.Items.MUSIC_DISCS).add(recordGaia3, recordEgo, recordHerrscherOfTheVoid);
		this.tag(ItemTags.CLUSTER_MAX_HARVESTABLES).add(HAMMERS);

		this.tag(BotaniaTags.Items.RUNES).add(elementRune, sinRune);
		// 原版 1.16 里要求"击败本我"的九件遗物。只有打开配置项 relicsNeedEgoDefeat 之后，这个标签才有作用。
		// The nine relics that need "defeat the Ego" in the original 1.16. The tag only has an effect when the configuration option relicsNeedEgoDefeat is on.
		this.tag(ExtraBotanyTags.Items.NEEDS_EGO_DEFEAT).add(coreOfTheVoid, moonPendant, sunRing, voidArchives, camera,
				excalibur, failnaught, firstFractal, manaRingMaster);
		//ManaUsingItem
		this.tag(BotaniaTags.Items.MANA_USING_ITEMS).add(ExtraBotanyItems.MANA_USING_ITEM);
		this.tag(BotaniaTags.Items.MANA_USING_ITEMS).add(HAMMERS);
		this.tag(BotaniaTags.Items.MANA_USING_ITEMS).add(SHIELDS);

		this.tag(ExtraBotanyTags.Items.REWARD_BAGS).add(REWARD_BAGS);
		this.copy(ExtraBotanyTags.Blocks.PEDESTALS, ExtraBotanyTags.Items.PEDESTALS);
		this.copy(ExtraBotanyTags.Blocks.MANA_POOLS, ExtraBotanyTags.Items.MANA_POOLS);
		this.copy(ExtraBotanyTags.Blocks.CHARGERS, ExtraBotanyTags.Items.CHARGERS);
		this.tag(ExtraBotanyTags.Items.ANNOYING_FLOWER_EATABLE).add(friedChicken);
		this.tag(ExtraBotanyTags.Items.SHIELDS).add(manasteelShield, elementiumShield, terrasteelShield, achillesShield);

		addCommonMaterialTags();
		addEquipmentTags();
		addAccessorySlotTags();
	}

	/**
	 * NeoForge 的通用标签（{@code c} 命名空间）：锭、粒和材料块。
	 * The common tags of NeoForge (the {@code c} namespace): ingots, nuggets and storage blocks.
	 */
	private void addCommonMaterialTags() {
		this.tag(common("ingots/orichalcos")).add(orichalcos).addTag(ExtraBotanyTags.Items.INGOTS_ORICHALCOS);
		this.tag(common("ingots/photonium")).add(photonium).addTag(ExtraBotanyTags.Items.INGOTS_PHOTONIUM);
		this.tag(common("ingots/shadowium")).add(shadowium).addTag(ExtraBotanyTags.Items.INGOTS_SHADOWIUM);
		this.tag(common("ingots/aerialite")).add(aerialite).addTag(ExtraBotanyTags.Items.INGOTS_AERIALITE);
		this.tag(Tags.Items.INGOTS)
				.addTag(common("ingots/orichalcos"))
				.addTag(common("ingots/photonium"))
				.addTag(common("ingots/shadowium"))
				.addTag(common("ingots/aerialite"));

		this.tag(common("nuggets/orichalcos")).add(orichalcosNugget).addTag(ExtraBotanyTags.Items.NUGGETS_ORICHALCOS);
		this.tag(common("nuggets/photonium")).add(photoniumNugget).addTag(ExtraBotanyTags.Items.NUGGETS_PHOTONIUM);
		this.tag(common("nuggets/shadowium")).add(shadowiumNugget).addTag(ExtraBotanyTags.Items.NUGGETS_SHADOWIUM);
		this.tag(common("nuggets/aerialite")).add(aerialiteNugget).addTag(ExtraBotanyTags.Items.NUGGETS_AERIALITE);
		this.tag(Tags.Items.NUGGETS)
				.addTag(common("nuggets/orichalcos"))
				.addTag(common("nuggets/photonium"))
				.addTag(common("nuggets/shadowium"))
				.addTag(common("nuggets/aerialite"));

		this.copy(BlockTagProvider.STORAGE_ORICHALCOS, ItemTags.create(BlockTagProvider.STORAGE_ORICHALCOS.location()));
		this.copy(BlockTagProvider.STORAGE_PHOTONIUM, ItemTags.create(BlockTagProvider.STORAGE_PHOTONIUM.location()));
		this.copy(BlockTagProvider.STORAGE_SHADOWIUM, ItemTags.create(BlockTagProvider.STORAGE_SHADOWIUM.location()));
		this.copy(BlockTagProvider.STORAGE_AERIALITE, ItemTags.create(BlockTagProvider.STORAGE_AERIALITE.location()));

		this.tag(Tags.Items.GEMS_QUARTZ).add(gaiaQuartz, elementiumQuartz);
	}

	private void addEquipmentTags() {
		this.tag(ItemTags.HEAD_ARMOR).add(starryIdolHeadgear, pleiadesCombatMaidHeadgear, shadowWarriorHelmet, goblinSlayerHelmet,
				mikuHelm, shootingGuardianHelm, silentSagesHelm);
		this.tag(ItemTags.CHEST_ARMOR).add(starryIdolSuit, pleiadesCombatMaidSuit, sanguinePleiadesCombatMaidSuit, shadowWarriorChestplate, goblinSlayerChestplate,
				mikuChest, shootingGuardianChest, silentSagesChest);
		this.tag(ItemTags.LEG_ARMOR).add(starryIdolSkirt, pleiadesCombatMaidSkirt, shadowWarriorLeggings, goblinSlayerLeggings,
				mikuLegs, shootingGuardianLegs, silentSagesLegs);
		this.tag(ItemTags.FOOT_ARMOR).add(starryIdolBoots, pleiadesCombatMaidBoots, shadowWarriorBoots, goblinSlayerBoots,
				mikuBoots, shootingGuardianBoots, silentSagesBoots);
		this.tag(Tags.Items.TOOLS_SHIELD).add(SHIELDS);
		this.tag(Tags.Items.TOOLS_BOW).add(BOWS);
	}

	/**
	 * Curios 的栏位标签：决定饰品能戴在哪个栏位。这里只有由物品分组决定的栏位；头部、护符和坐骑栏位的标签是手写的文件。
	 * The slot tags of Curios: they set the slot that a bauble can go into. Only the slots that come from the item groups are here; the tags of the head, charm and mount slots are hand-written files.
	 */
	private void addAccessorySlotTags() {
		this.tag(accessory("ring")).add(RINGS);
		this.tag(accessory("necklace")).add(NECKLACES);
		this.tag(accessory("body")).add(BODY);
		this.tag(accessory("curio")).add(ALL_SLOT);
	}

	private static TagKey<Item> accessory(String name) {
		return ItemTags.create(ResourceLocation.fromNamespaceAndPath("curios", name));
	}

	private static TagKey<Item> common(String name) {
		return ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", name));
	}
}
