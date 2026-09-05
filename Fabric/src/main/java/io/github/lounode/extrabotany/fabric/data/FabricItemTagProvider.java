package io.github.lounode.extrabotany.fabric.data;

import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.data.tags.ItemTagProvider;

import java.util.concurrent.CompletableFuture;

import static io.github.lounode.extrabotany.common.item.ExtraBotanyItems.*;

public class FabricItemTagProvider extends ItemTagProvider {
	public FabricItemTagProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagsProvider.TagLookup<Block>> blockTagProvider) {
		super(packOutput, lookupProvider, blockTagProvider);
	}

	private static TagKey<Item> itemTag(ResourceLocation location) {
		return TagKey.create(Registries.ITEM, location);
	}

	@Override
	protected void addTags(HolderLookup.Provider provider) {
		generateMaterialTags();
		generateToolTags();
		generateAccessoryTags();
		generateCompatTags();
	}

	private void generateMaterialTags() {
		//Material
		this.tag(c("ingots/orichalcos")).add(orichalcos);
		this.tag(c("ingots/photonium")).add(ExtraBotanyItems.photonium);
		this.tag(c("ingots/shadowium")).add(ExtraBotanyItems.shadowium);
		this.tag(c("ingots/aerialite")).add(ExtraBotanyItems.aerialite);
		this.tag(ConventionalItemTags.INGOTS)
				.addTag(c("ingots/orichalcos"))
				.addTag(c("ingots/photonium"))
				.addTag(c("ingots/shadowium"))
				.addTag(c("ingots/aerialite"));

		this.tag(c("nuggets/orichalcos")).add(ExtraBotanyItems.orichalcosNugget);
		this.tag(c("nuggets/photonium")).add(ExtraBotanyItems.photoniumNugget);
		this.tag(c("nuggets/shadowium")).add(ExtraBotanyItems.shadowiumNugget);
		this.tag(c("nuggets/aerialite")).add(ExtraBotanyItems.aerialiteNugget);
		this.tag(ConventionalItemTags.NUGGETS)
				.addTag(c("nuggets/orichalcos"))
				.addTag(c("nuggets/photonium"))
				.addTag(c("nuggets/shadowium"))
				.addTag(c("nuggets/aerialite"));

		this.tag(ConventionalItemTags.QUARTZ).add(gaiaQuartz, elementiumQuartz);
	}

	private void generateToolTags() {
		this.tag(ConventionalItemTags.BOWS).add(BOWS);
		this.tag(ConventionalItemTags.SHIELDS).add(SHIELDS);
	}

	private void generateAccessoryTags() {
		this.tag(accessory("hand/ring")).add(RINGS);
		this.tag(accessory("offhand/ring")).add(RINGS);
		this.tag(accessory("chest/necklace")).add(pureDaisyPendant);
		this.tag(accessory("all")).add(ALL_SLOT);
		this.tag(accessory("chest/cape")).add(BODY);
	}

	private void generateCompatTags() {
		this.tag(itemTag(new ResourceLocation("modern_industrialization", "replicator_blacklist")))
				.add(REPLICATOR_BLACKLIST);
	}

	private static TagKey<Item> accessory(String name) {
		return itemTag(new ResourceLocation("trinkets", name));
	}

	private static TagKey<Item> c(String name) {
		return itemTag(ResourceLocation.tryBuild("c", name));
	}
}
