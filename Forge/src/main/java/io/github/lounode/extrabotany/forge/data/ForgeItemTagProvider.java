package io.github.lounode.extrabotany.forge.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import io.github.lounode.extrabotany.common.lib.LibMisc;

import java.util.concurrent.CompletableFuture;

import static io.github.lounode.extrabotany.common.item.ExtraBotanyItems.*;

public class ForgeItemTagProvider extends net.minecraft.data.tags.ItemTagsProvider {
	public ForgeItemTagProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagsProvider.TagLookup<Block>> blockTagProvider, ExistingFileHelper helper) {
		super(packOutput, lookupProvider, blockTagProvider, LibMisc.MOD_ID, helper);
	}

	@Override
	public String getName() {
		return "ExtraBotany item tags (Forge-specific)";
	}

	@Override
	protected void addTags(HolderLookup.Provider provider) {
		this.generateMaterialTags();
		this.generateToolTags();
		this.generateAccessoryTags();
	}

	private void generateMaterialTags() {
		//Material
		this.tag(common("ingots/orichalcos")).add(orichalcos);
		this.tag(common("ingots/photonium")).add(photonium);
		this.tag(common("ingots/shadowium")).add(shadowium);
		this.tag(common("ingots/aerialite")).add(aerialite);
		this.tag(Tags.Items.INGOTS)
				.addTag(common("ingots/orichalcos"))
				.addTag(common("ingots/photonium"))
				.addTag(common("ingots/shadowium"))
				.addTag(common("ingots/aerialite"));

		this.tag(common("nuggets/orichalcos")).add(orichalcosNugget);
		this.tag(common("nuggets/photonium")).add(photoniumNugget);
		this.tag(common("nuggets/shadowium")).add(shadowiumNugget);
		this.tag(common("nuggets/aerialite")).add(aerialiteNugget);
		this.tag(Tags.Items.NUGGETS)
				.addTag(common("nuggets/orichalcos"))
				.addTag(common("nuggets/photonium"))
				.addTag(common("nuggets/shadowium"))
				.addTag(common("nuggets/aerialite"));

		this.copyToSameName(ForgeBlockTagProvider.ORICHALCOS);
		this.copyToSameName(ForgeBlockTagProvider.PHOTONIUM);
		this.copyToSameName(ForgeBlockTagProvider.SHADOWIUM);
		this.copyToSameName(ForgeBlockTagProvider.AERIALITE);

		this.tag(Tags.Items.GEMS_QUARTZ).add(gaiaQuartz, elementiumQuartz);
	}

	private void generateAccessoryTags() {
		tag(accessory("ring")).add(RINGS);
		tag(accessory("necklace")).add(pureDaisyPendant);
		tag(accessory(("body"))).add(BODY);
		tag(accessory("curio")).add(ALL_SLOT);
	}

	private void generateToolTags() {
		this.tag(ItemTags.HEAD_ARMOR).add(starryIdolHeadgear, pleiadesCombatMaidHeadgear, shadowWarriorHelmet, goblinSlayerHelmet);
		this.tag(ItemTags.CHEST_ARMOR).add(starryIdolSuit, pleiadesCombatMaidSuit, sanguinePleiadesCombatMaidSuit, shadowWarriorChestplate, goblinSlayerChestplate);
		this.tag(ItemTags.LEG_ARMOR).add(starryIdolSkirt, pleiadesCombatMaidSkirt, shadowWarriorLeggings, goblinSlayerLeggings);
		this.tag(ItemTags.FOOT_ARMOR).add(starryIdolBoots, pleiadesCombatMaidBoots, shadowWarriorBoots, goblinSlayerBoots);
		this.tag(Tags.Items.TOOLS_SHIELD).add(SHIELDS);
		this.tag(Tags.Items.TOOLS_BOW).add(BOWS);
	}

	private static TagKey<Item> accessory(String name) {
		return ItemTags.create(ResourceLocation.tryBuild("curios", name));
	}

	private static TagKey<Item> common(String name) {
		return ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", name));
	}

	private void copyToSameName(TagKey<Block> source) {
		this.copy(source, ItemTags.create(source.location()));
	}
}
