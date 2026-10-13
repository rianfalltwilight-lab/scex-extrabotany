package io.github.lounode.extrabotany.data.loot;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
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

/**
 * 生成本模组所有方块的战利品表。大多数方块掉落它自己；台阶按数量掉落；花盆掉落花盆和花；六种有冷却或者寿命的花把这些数据留在掉落的物品上。
 * Makes the loot tables of all blocks of this mod. Most blocks drop themselves; slabs drop by their count; flower pots drop the pot and the flower; six flowers that have a cooldown or a lifetime keep that data on the dropped item.
 */
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
		// 没有战利品表的方块（例如液态魔力）不在这里：基类不允许给它们生成战利品表。
		// Blocks with no loot table (for example Fluided Mana) are not included: the base class does not permit a loot table for them.
		return BuiltInRegistries.BLOCK.stream()
				.filter(block -> LibMisc.MOD_ID.equals(BuiltInRegistries.BLOCK.getKey(block).getNamespace()))
				.filter(block -> block.getLootTable() != BuiltInLootTables.EMPTY)
				.toList();
	}
}
