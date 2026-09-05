package io.github.lounode.extrabotany.data.loot;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyCustomDataFunction;
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks;
import io.github.lounode.extrabotany.common.block.flower.functional.WoodieniaBlockEntity;
import io.github.lounode.extrabotany.common.block.flower.generating.BellflowerBlockEntity;
import io.github.lounode.extrabotany.common.block.flower.generating.MoonlightLilyBlockEntity;
import io.github.lounode.extrabotany.common.block.flower.generating.ReikarlilyBlockEntity;
import io.github.lounode.extrabotany.common.block.flower.generating.ResoncundBlockEntity;
import io.github.lounode.extrabotany.common.block.flower.generating.SunshineLilyBlockEntity;
import io.github.lounode.extrabotany.common.lib.LibMisc;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/** Generates all common ExtraBotany block loot tables using the 1.21 registry-aware pipeline. */
public class BlockLootProvider extends BlockLootSubProvider {
	private final Map<Block, Function<Block, LootTable.Builder>> specialCases = new HashMap<>();

	public BlockLootProvider(HolderLookup.Provider registries) {
		super(Set.<Item>of(), FeatureFlags.REGISTRY.allFlags(), registries);

		copyFlowerData(ExtrabotanyFlowerBlocks.woodienia, WoodieniaBlockEntity.TAG_COOLDOWN);
		copyFlowerData(ExtrabotanyFlowerBlocks.woodieniaFloating, WoodieniaBlockEntity.TAG_COOLDOWN);
		copyFlowerData(ExtrabotanyFlowerBlocks.reikarlily, ReikarlilyBlockEntity.TAG_COOLDOWN);
		copyFlowerData(ExtrabotanyFlowerBlocks.reikarlilyFloating, ReikarlilyBlockEntity.TAG_COOLDOWN);
		copyFlowerData(ExtrabotanyFlowerBlocks.bellflower, BellflowerBlockEntity.TAG_PASSIVE_DECAY_TICKS);
		copyFlowerData(ExtrabotanyFlowerBlocks.bellflowerFloating, BellflowerBlockEntity.TAG_PASSIVE_DECAY_TICKS);
		copyFlowerData(ExtrabotanyFlowerBlocks.resoncund, ResoncundBlockEntity.TAG_SOUND_HEARD);
		copyFlowerData(ExtrabotanyFlowerBlocks.resoncundFloating, ResoncundBlockEntity.TAG_SOUND_HEARD);
		copyFlowerData(ExtrabotanyFlowerBlocks.sunshineLily, SunshineLilyBlockEntity.TAG_PASSIVE_DECAY_TICKS);
		copyFlowerData(ExtrabotanyFlowerBlocks.sunshineLilyFloating, SunshineLilyBlockEntity.TAG_PASSIVE_DECAY_TICKS);
		copyFlowerData(ExtrabotanyFlowerBlocks.moonlightLily, MoonlightLilyBlockEntity.TAG_PASSIVE_DECAY_TICKS);
		copyFlowerData(ExtrabotanyFlowerBlocks.moonlightLilyFloating, MoonlightLilyBlockEntity.TAG_PASSIVE_DECAY_TICKS);
	}

	private void copyFlowerData(Block block, String... tags) {
		specialCases.put(block, ignored -> createFlowerDataTable(block, tags));
	}

	@Override
	protected void generate() {
		for (Block block : getKnownBlocks()) {
			Function<Block, LootTable.Builder> specialCase = specialCases.get(block);
			if (specialCase != null) {
				add(block, specialCase.apply(block));
			} else if (block instanceof SlabBlock) {
				add(block, createSlabItemTable(block));
			} else if (block instanceof FlowerPotBlock flowerPot) {
				add(block, createPotFlowerItemTable(flowerPot.getPotted()));
			} else {
				dropSelf(block);
			}
		}
	}

	@SuppressWarnings("deprecation")
	private LootTable.Builder createFlowerDataTable(Block block, String... tags) {
		CopyCustomDataFunction.Builder copyData = CopyCustomDataFunction.copyData(ContextNbtProvider.BLOCK_ENTITY);
		for (String tag : tags) {
			copyData.copy(tag, tag);
		}
		return LootTable.lootTable().withPool(applyExplosionCondition(block,
				LootPool.lootPool().setRolls(ConstantValue.exactly(1)).add(LootItem.lootTableItem(block).apply(copyData))));
	}

	@Override
	protected Iterable<Block> getKnownBlocks() {
		return BuiltInRegistries.BLOCK.stream()
				.filter(block -> {
					ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
					return LibMisc.MOD_ID.equals(id.getNamespace());
				})
				.toList();
	}
}
