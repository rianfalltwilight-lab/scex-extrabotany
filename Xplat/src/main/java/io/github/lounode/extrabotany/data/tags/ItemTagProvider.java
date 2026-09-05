package io.github.lounode.extrabotany.data.tags;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import vazkii.botania.common.item.lens.LensItem;
import vazkii.botania.common.lib.BotaniaTags;

import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.lib.ExtraBotanyTags;
import io.github.lounode.extrabotany.common.lib.LibMisc;

import java.util.concurrent.CompletableFuture;

import static io.github.lounode.extrabotany.common.item.ExtraBotanyItems.*;

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
		this.tag(ExtraBotanyTags.Items.MUSIC_DISCS).add(recordGaia3, recordHerrscherOfTheVoid);
		this.tag(ItemTags.CLUSTER_MAX_HARVESTABLES).add(HAMMERS);

		this.tag(BotaniaTags.Items.RUNES);
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
	}
}
