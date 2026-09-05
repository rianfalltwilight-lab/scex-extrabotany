package io.github.lounode.extrabotany.common.block;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import vazkii.botania.common.block.BotaniaBlocks;

import io.github.lounode.extrabotany.common.block.mana.DimensionCatalystBlock;
import io.github.lounode.extrabotany.common.block.mana.ManaChargerBlock;
import io.github.lounode.extrabotany.common.block.mana.PowerFrameBlock;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.lib.LibBlockNames;

import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;
import static vazkii.botania.common.lib.LibBlockNames.SLAB_SUFFIX;
import static vazkii.botania.common.lib.LibBlockNames.STAIRS_SUFFIX;

public final class ExtraBotanyBlocks {
	public static final Block cocoonOfDesire = new io.github.lounode.extrabotany.common.block.legacy.LegacyCocoonBlock(
			BlockBehaviour.Properties.of().strength(3, 50).sound(SoundType.WOOL).mapColor(MapColor.WOOL).noOcclusion());
	public static final Block trophy = new io.github.lounode.extrabotany.common.block.legacy.LegacyTrophyBlock(
			BlockBehaviour.Properties.ofFullCopy(Blocks.ANVIL).strength(2, 10).sound(SoundType.STONE).noOcclusion());
	public static final Block livingrockBarrel = new io.github.lounode.extrabotany.common.block.legacy.LegacyBarrelBlock(
			BlockBehaviour.Properties.ofFullCopy(BotaniaBlocks.LIVINGROCK).strength(1.5F, 10).sound(SoundType.GLASS).noOcclusion());

	public static final Block shadowiumBlock = new ExtraBotanyBlock(BlockBehaviour.Properties.of()
			.strength(3, 10)
			.sound(SoundType.METAL)
			.instrument(NoteBlockInstrument.IRON_XYLOPHONE)
			.mapColor(MapColor.COLOR_BLACK)
			.requiresCorrectToolForDrops()
	);
	public static final Block photoniumBlock = new ExtraBotanyBlock(BlockBehaviour.Properties.ofFullCopy(shadowiumBlock)
			.mapColor(MapColor.TERRACOTTA_WHITE)
			.lightLevel((state) -> 15)
	);
	public static final Block aerialiteBlock = new ExtraBotanyBlock(BlockBehaviour.Properties.ofFullCopy(shadowiumBlock)
			.mapColor(MapColor.LAPIS)
	);
	public static final Block orichalcosBlock = new ExtraBotanyBlock(BlockBehaviour.Properties.of()
			.strength(10, 1200.0F)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.mapColor(MapColor.TERRACOTTA_PURPLE)
	);
	public static final Block dimensionCatalyst = new DimensionCatalystBlock(BlockBehaviour.Properties.ofFullCopy(BotaniaBlocks.LIVINGROCK));
	public static final Block livingrockPedestal = new PedestalBlock(PedestalBlock.Variant.LIVINGROCK, BlockBehaviour.Properties.of()
			.strength(3.5F)
			.sound(SoundType.STONE)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.mapColor(MapColor.TERRACOTTA_WHITE));
	public static final Block calcitePedestal = new PedestalBlock(PedestalBlock.Variant.CALCITE, BlockBehaviour.Properties.ofFullCopy(livingrockPedestal)
			.sound(SoundType.CALCITE));
	public static final Block[] ALL_PEDESTALS = new Block[] { livingrockPedestal, calcitePedestal };
	public static final Block powerFrame = new PowerFrameBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SPAWNER));
	public static final Block manaCharger = new ManaChargerBlock(BlockBehaviour.Properties.ofFullCopy(BotaniaBlocks.LIVINGWOOD)
			.explosionResistance(1.0F)
			.mapColor(MapColor.TERRACOTTA_WHITE));
	public static final Block enchantedSoil = new EnchantedSoilBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.MOSS_BLOCK));

	//Quartz
	public static final Block gaiaQuartzBlock = new ExtraBotanyBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.QUARTZ_BLOCK).mapColor(MapColor.COLOR_LIGHT_GREEN));
	public static final Block gaiaQuartzStairs = new StairBlock(gaiaQuartzBlock.defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(gaiaQuartzBlock));
	public static final Block gaiaQuartzSlab = new SlabBlock(BlockBehaviour.Properties.ofFullCopy(gaiaQuartzBlock));
	public static final Block chiseledGaiaQuartzBlock = new ExtraBotanyBlock(BlockBehaviour.Properties.ofFullCopy(gaiaQuartzBlock));
	public static final Block gaiaQuartzBricks = new ExtraBotanyBlock(BlockBehaviour.Properties.ofFullCopy(gaiaQuartzBlock));
	public static final Block gaiaQuartzPillar = new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(gaiaQuartzBlock));
	public static final Block smoothGaiaQuartz = new ExtraBotanyBlock(BlockBehaviour.Properties.ofFullCopy(gaiaQuartzBlock));
	public static final Block smoothGaiaQuartzStairs = new StairBlock(smoothGaiaQuartz.defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(smoothGaiaQuartz));
	public static final Block smoothGaiaQuartzSlab = new SlabBlock(BlockBehaviour.Properties.ofFullCopy(smoothGaiaQuartz));

	public static final Block elementiumQuartzBlock = new ExtraBotanyBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.QUARTZ_BLOCK).mapColor(MapColor.COLOR_PINK));
	public static final Block elementiumQuartzStairs = new StairBlock(elementiumQuartzBlock.defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(elementiumQuartzBlock));
	public static final Block elementiumQuartzSlab = new SlabBlock(BlockBehaviour.Properties.ofFullCopy(elementiumQuartzBlock));
	public static final Block chiseledElementiumQuartzBlock = new ExtraBotanyBlock(BlockBehaviour.Properties.ofFullCopy(elementiumQuartzBlock));
	public static final Block elementiumQuartzBricks = new ExtraBotanyBlock(BlockBehaviour.Properties.ofFullCopy(elementiumQuartzBlock));
	public static final Block elementiumQuartzPillar = new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(elementiumQuartzBlock));
	public static final Block smoothElementiumQuartz = new ExtraBotanyBlock(BlockBehaviour.Properties.ofFullCopy(elementiumQuartzBlock));
	public static final Block smoothElementiumQuartzStairs = new StairBlock(smoothElementiumQuartz.defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(smoothElementiumQuartz));
	public static final Block smoothElementiumQuartzSlab = new SlabBlock(BlockBehaviour.Properties.ofFullCopy(smoothElementiumQuartz));

	public static final Block[] ALL_QUARTZ = new Block[] {
			gaiaQuartzBlock, gaiaQuartzStairs, gaiaQuartzSlab,
			chiseledGaiaQuartzBlock, gaiaQuartzBricks, gaiaQuartzPillar,
			smoothGaiaQuartz, smoothGaiaQuartzStairs, smoothGaiaQuartzSlab,
			elementiumQuartzBlock, elementiumQuartzStairs, elementiumQuartzSlab,
			chiseledElementiumQuartzBlock, elementiumQuartzBricks, elementiumQuartzPillar,
			smoothElementiumQuartz, smoothElementiumQuartzStairs, smoothElementiumQuartzSlab
	};

	public static FlowerPotBlock flowerPot(Block block, int lightLevel) {
		BlockBehaviour.Properties properties = BlockBehaviour.Properties.of().instabreak().noOcclusion().pushReaction(PushReaction.DESTROY);
		return new FlowerPotBlock(() -> (FlowerPotBlock) Blocks.FLOWER_POT, () -> block,
				lightLevel > 0 ? properties.lightLevel(blockState -> lightLevel) : properties);
	}

	public static void registerBlocks(BiConsumer<Block, ResourceLocation> r) {
		r.accept(cocoonOfDesire, prefix("cocoondesire"));
		r.accept(trophy, prefix("trophy"));
		r.accept(livingrockBarrel, prefix("elfjar"));
		io.github.lounode.extrabotany.common.block.legacy.LegacyMachineBlock.BLOCKS.forEach((kind,block)->r.accept(block,prefix(kind.id)));
		r.accept(orichalcosBlock, prefix(LibBlockNames.ORICHALCOS_BLOCK));
		r.accept(photoniumBlock, prefix(LibBlockNames.PHOTONIUM_BLOCK));
		r.accept(shadowiumBlock, prefix(LibBlockNames.SHADOWIUM_BLOCK));
		r.accept(aerialiteBlock, prefix(LibBlockNames.AERIALITE_BLOCK));

		//Pedestal
		r.accept(livingrockPedestal, prefix(LibBlockNames.PEDESTAL_PREFIX + PedestalBlock.Variant.LIVINGROCK.name().toLowerCase(Locale.ROOT)));
		r.accept(calcitePedestal, prefix(LibBlockNames.PEDESTAL_PREFIX + PedestalBlock.Variant.CALCITE.name().toLowerCase(Locale.ROOT)));
		r.accept(powerFrame, prefix(LibBlockNames.POWER_FRAME));
		r.accept(manaCharger, prefix(LibBlockNames.MANA_CHARGER));
		r.accept(dimensionCatalyst, prefix(LibBlockNames.DIMENSION_CATALYST));
		r.accept(enchantedSoil, prefix(LibBlockNames.ENCHANTED_SOIL));

		r.accept(gaiaQuartzBlock, prefix(LibBlockNames.GAIA_QUARTZ + "_block"));
		r.accept(chiseledGaiaQuartzBlock, prefix("chiseled_" + LibBlockNames.GAIA_QUARTZ + "_block"));
		r.accept(gaiaQuartzBricks, prefix(LibBlockNames.GAIA_QUARTZ + "_bricks"));
		r.accept(gaiaQuartzPillar, prefix(LibBlockNames.GAIA_QUARTZ + "_pillar"));
		r.accept(gaiaQuartzSlab, prefix(LibBlockNames.GAIA_QUARTZ + SLAB_SUFFIX));
		r.accept(gaiaQuartzStairs, prefix(LibBlockNames.GAIA_QUARTZ + STAIRS_SUFFIX));
		r.accept(smoothGaiaQuartz, prefix("smooth_" + LibBlockNames.GAIA_QUARTZ));
		r.accept(smoothGaiaQuartzStairs, prefix("smooth_" + LibBlockNames.GAIA_QUARTZ + STAIRS_SUFFIX));
		r.accept(smoothGaiaQuartzSlab, prefix("smooth_" + LibBlockNames.GAIA_QUARTZ + SLAB_SUFFIX));

		r.accept(elementiumQuartzBlock, prefix(LibBlockNames.ELEMENTIUM_QUARTZ + "_block"));
		r.accept(chiseledElementiumQuartzBlock, prefix("chiseled_" + LibBlockNames.ELEMENTIUM_QUARTZ + "_block"));
		r.accept(elementiumQuartzBricks, prefix(LibBlockNames.ELEMENTIUM_QUARTZ + "_bricks"));
		r.accept(elementiumQuartzPillar, prefix(LibBlockNames.ELEMENTIUM_QUARTZ + "_pillar"));
		r.accept(elementiumQuartzSlab, prefix(LibBlockNames.ELEMENTIUM_QUARTZ + SLAB_SUFFIX));
		r.accept(elementiumQuartzStairs, prefix(LibBlockNames.ELEMENTIUM_QUARTZ + STAIRS_SUFFIX));
		r.accept(smoothElementiumQuartz, prefix("smooth_" + LibBlockNames.ELEMENTIUM_QUARTZ));
		r.accept(smoothElementiumQuartzStairs, prefix("smooth_" + LibBlockNames.ELEMENTIUM_QUARTZ + STAIRS_SUFFIX));
		r.accept(smoothElementiumQuartzSlab, prefix("smooth_" + LibBlockNames.ELEMENTIUM_QUARTZ + SLAB_SUFFIX));

	}

	public static void registerItemBlocks(BiConsumer<Item, ResourceLocation> r) {
		r.accept(new BlockItem(cocoonOfDesire, ExtraBotanyItems.defaultBuilder()), prefix("cocoondesire"));
		r.accept(new BlockItem(trophy, ExtraBotanyItems.defaultBuilder()), prefix("trophy"));
		r.accept(new BlockItem(livingrockBarrel, ExtraBotanyItems.defaultBuilder()), prefix("elfjar"));
		io.github.lounode.extrabotany.common.block.legacy.LegacyMachineBlock.BLOCKS.forEach((kind,block)->r.accept(new BlockItem(block,new Item.Properties()),prefix(kind.id)));
		Item.Properties props = ExtraBotanyItems.defaultBuilder();

		r.accept(new BlockItem(gaiaQuartzBlock, props), BuiltInRegistries.BLOCK.getKey(gaiaQuartzBlock));
		r.accept(new BlockItem(gaiaQuartzStairs, props), BuiltInRegistries.BLOCK.getKey(gaiaQuartzStairs));
		r.accept(new BlockItem(gaiaQuartzSlab, props), BuiltInRegistries.BLOCK.getKey(gaiaQuartzSlab));
		r.accept(new BlockItem(chiseledGaiaQuartzBlock, props), BuiltInRegistries.BLOCK.getKey(chiseledGaiaQuartzBlock));
		r.accept(new BlockItem(gaiaQuartzBricks, props), BuiltInRegistries.BLOCK.getKey(gaiaQuartzBricks));
		r.accept(new BlockItem(gaiaQuartzPillar, props), BuiltInRegistries.BLOCK.getKey(gaiaQuartzPillar));
		r.accept(new BlockItem(smoothGaiaQuartz, props), BuiltInRegistries.BLOCK.getKey(smoothGaiaQuartz));
		r.accept(new BlockItem(smoothGaiaQuartzStairs, props), BuiltInRegistries.BLOCK.getKey(smoothGaiaQuartzStairs));
		r.accept(new BlockItem(smoothGaiaQuartzSlab, props), BuiltInRegistries.BLOCK.getKey(smoothGaiaQuartzSlab));

		r.accept(new BlockItem(elementiumQuartzBlock, props), BuiltInRegistries.BLOCK.getKey(elementiumQuartzBlock));
		r.accept(new BlockItem(elementiumQuartzStairs, props), BuiltInRegistries.BLOCK.getKey(elementiumQuartzStairs));
		r.accept(new BlockItem(elementiumQuartzSlab, props), BuiltInRegistries.BLOCK.getKey(elementiumQuartzSlab));
		r.accept(new BlockItem(chiseledElementiumQuartzBlock, props), BuiltInRegistries.BLOCK.getKey(chiseledElementiumQuartzBlock));
		r.accept(new BlockItem(elementiumQuartzBricks, props), BuiltInRegistries.BLOCK.getKey(elementiumQuartzBricks));
		r.accept(new BlockItem(elementiumQuartzPillar, props), BuiltInRegistries.BLOCK.getKey(elementiumQuartzPillar));
		r.accept(new BlockItem(smoothElementiumQuartz, props), BuiltInRegistries.BLOCK.getKey(smoothElementiumQuartz));
		r.accept(new BlockItem(smoothElementiumQuartzStairs, props), BuiltInRegistries.BLOCK.getKey(smoothElementiumQuartzStairs));
		r.accept(new BlockItem(smoothElementiumQuartzSlab, props), BuiltInRegistries.BLOCK.getKey(smoothElementiumQuartzSlab));

		r.accept(new BlockItem(orichalcosBlock, ExtraBotanyItems.defaultBuilder().rarity(Rarity.EPIC)), BuiltInRegistries.BLOCK.getKey(orichalcosBlock));
		r.accept(new BlockItem(photoniumBlock, props), BuiltInRegistries.BLOCK.getKey(photoniumBlock));
		r.accept(new BlockItem(shadowiumBlock, props), BuiltInRegistries.BLOCK.getKey(shadowiumBlock));
		r.accept(new BlockItem(aerialiteBlock, props), BuiltInRegistries.BLOCK.getKey(aerialiteBlock));
		r.accept(new BlockItem(dimensionCatalyst, props), BuiltInRegistries.BLOCK.getKey(dimensionCatalyst));
		r.accept(new BlockItem(powerFrame, props), BuiltInRegistries.BLOCK.getKey(powerFrame));
		r.accept(new BlockItem(livingrockPedestal, props), BuiltInRegistries.BLOCK.getKey(livingrockPedestal));
		r.accept(new BlockItem(calcitePedestal, props), BuiltInRegistries.BLOCK.getKey(calcitePedestal));
		r.accept(new BlockItem(manaCharger, props), BuiltInRegistries.BLOCK.getKey(manaCharger));
		r.accept(new BlockItem(enchantedSoil, props), BuiltInRegistries.BLOCK.getKey(enchantedSoil));

	}

	public static void registerFlowerPotPlants(BiConsumer<ResourceLocation, Supplier<? extends Block>> consumer) {

	}
}
