package io.github.lounode.extrabotany.data.tags;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.*;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.block.BotaniaGrassBlock;
import vazkii.botania.common.block.flower.FloatingSpecialFlowerBlock;
import vazkii.botania.common.block.flower.FloatingFlowerBaseBlock;
import vazkii.botania.common.lib.BotaniaTags;

import io.github.lounode.extrabotany.common.lib.ExtraBotanyTags;
import io.github.lounode.extrabotany.common.lib.LibMisc;
import io.github.lounode.extrabotany.xplat.EXplatAbstractions;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import static io.github.lounode.extrabotany.common.block.ExtraBotanyBlocks.*;
import static io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks.*;

public class BlockTagProvider extends IntrinsicHolderTagsProvider<Block> {
	public static final Predicate<Block> EXTRABOTANY_BLOCK = b -> LibMisc.MOD_ID.equals(BuiltInRegistries.BLOCK.getKey(b).getNamespace());

	public BlockTagProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
		super(packOutput, Registries.BLOCK, lookupProvider,
				block -> BuiltInRegistries.BLOCK.getResourceKey(block).orElseThrow(), LibMisc.MOD_ID, null);
	}

	@Override
	protected void addTags(HolderLookup.Provider provider) {
		tag(BlockTags.RAILS);
		tag(BlockTags.SLABS).add(getModBlocks(b -> b instanceof SlabBlock));
		tag(BlockTags.WOODEN_SLABS);
		tag(BlockTags.STAIRS).add(getModBlocks(b -> b instanceof StairBlock));
		tag(BlockTags.WOODEN_STAIRS);
		tag(BlockTags.WALLS).add(getModBlocks(b -> b instanceof WallBlock));
		tag(BlockTags.FENCES).add(getModBlocks(b -> b instanceof FenceBlock));
		tag(BlockTags.WOODEN_FENCES);
		tag(BlockTags.FENCE_GATES).add(getModBlocks(b -> b instanceof FenceGateBlock));
		tag(BlockTags.DRAGON_IMMUNE);
		tag(BlockTags.WITHER_IMMUNE);
		tag(BlockTags.PLANKS);
		tag(BlockTags.DIRT).add(enchantedSoil);

		tag(BlockTags.BEACON_BASE_BLOCKS).add(
				aerialiteBlock,
				orichalcosBlock,
				photoniumBlock,
				shadowiumBlock
		);
		tag(ExtraBotanyTags.Blocks.BLOCKS_AERIALITE).add(aerialiteBlock);
		tag(ExtraBotanyTags.Blocks.BLOCKS_ORICHALCOS).add(orichalcosBlock);
		tag(ExtraBotanyTags.Blocks.BLOCKS_PHOTONIUM).add(photoniumBlock);
		tag(ExtraBotanyTags.Blocks.BLOCKS_SHADOWIUM).add(shadowiumBlock);

		tag(ExtraBotanyTags.Blocks.PEDESTALS).add(ALL_PEDESTALS);
		tag(ExtraBotanyTags.Blocks.MANA_POOLS).add(
				BotaniaBlocks.MANA_POOL,
				BotaniaBlocks.CREATIVE_MANA_POOL,
				BotaniaBlocks.DILUTED_MANA_POOL,
				BotaniaBlocks.FABULOUS_MANA_POOL
		);
		tag(ExtraBotanyTags.Blocks.MANA_SPREADERS).add(
				BotaniaBlocks.MANA_SPREADER,
				BotaniaBlocks.PULSE_MANA_SPREADER,
				BotaniaBlocks.ELVEN_MANA_SPREADER,
				BotaniaBlocks.GAIA_MANA_SPREADER
		);
		tag(ExtraBotanyTags.Blocks.CHARGERS).add(powerFrame, manaCharger);
		tag(ExtraBotanyTags.Blocks.BELLFLOWER_IGNORE)
				.addTag(ExtraBotanyTags.Blocks.MANA_POOLS)
				.addTag(ExtraBotanyTags.Blocks.MANA_SPREADERS);

		tag(BotaniaTags.Blocks.MUNDANE_FLOATING_FLOWERS);
		tag(BotaniaTags.Blocks.SMALL_MYSTICAL_FLOWERS);
		tag(BotaniaTags.Blocks.SHIMMERING_MUSHROOMS);
		tag(BotaniaTags.Blocks.GLIMMERING_FLOWERS);
		tag(BotaniaTags.Blocks.TALL_MYSTICAL_FLOWERS);
		tag(BotaniaTags.Blocks.MISC_SPECIAL_FLOWERS);
		tag(BotaniaTags.Blocks.SPECIAL_FLOATING_FLOWERS).add(BuiltInRegistries.BLOCK.stream().filter(EXTRABOTANY_BLOCK)
				.filter(b -> b instanceof FloatingSpecialFlowerBlock)
				.sorted(Comparator.comparing(BuiltInRegistries.BLOCK::getKey))
				.toArray(Block[]::new)
		);
		tag(BotaniaTags.Blocks.FLOATING_FLOWERS).add(BuiltInRegistries.BLOCK.stream().filter(EXTRABOTANY_BLOCK)
				.filter(b -> b instanceof FloatingSpecialFlowerBlock)
				.sorted(Comparator.comparing(BuiltInRegistries.BLOCK::getKey))
				.toArray(Block[]::new)
		);

		Block[] generatingSpecialFlowers = {
				reikarlily, bellflower, stonesia, edelweiss, resoncund,
				sunshineLily, moonlightLily, twinstar, omniviolet,
				tinkle, bloodEnchantress
		};
		tag(BotaniaTags.Blocks.GENERATING_SPECIAL_FLOWERS).add(generatingSpecialFlowers);

		Block[] functionalSpecialFlowers = {
				tradeOrchid, woodienia, annoyingflower, serenitian,
				mirrowtunia, necrofleur, necrofleurChibi, manalink,
				enchanter
		};
		tag(BotaniaTags.Blocks.FUNCTIONAL_SPECIAL_FLOWERS).add(functionalSpecialFlowers);

		tag(BotaniaTags.Blocks.SPECIAL_FLOWERS)
				.add(generatingSpecialFlowers)
				.add(functionalSpecialFlowers);

		tag(BotaniaTags.Blocks.MINI_FLOWERS).add(
				getModBlocks(b -> EXplatAbstractions.INSTANCE.isSpecialFlowerBlock(b)
						&& BuiltInRegistries.BLOCK.getKey(b).getPath().endsWith("_chibi"))
		);

		tag(BotaniaTags.Blocks.ENCHANTER_FLOWERS);

		tag(BlockTags.FLOWER_POTS)

				.add(
						//Generating
						reikarlilyPotted, bellflowerPotted, stonesiaPotted, edelweissPotted, resoncundPotted,
						sunshineLilyPotted, moonlightLilyPotted, twinstarPotted, omnivioletPotted,
						tinklePotted, bloodEnchantressPotted,
						//Func
						tradeOrchidPotted, woodieniaPotted, annoyingflowerPotted, serenitianPotted, mirrowtuniaPotted,
						necrofleurPotted, necrofleurChibiPotted, manalinkPotted, enchanterPotted
				);

		registerMiningTags();
	}

	private void registerMiningTags() {
		tag(BlockTags.MINEABLE_WITH_SHOVEL).add(enchantedSoil).add(
				getModBlocks(b -> b instanceof FloatingFlowerBaseBlock || b instanceof BotaniaGrassBlock)
		);
		//Pickaxe
		Set<Block> pickaxe = new HashSet<>(Set.of(
				aerialiteBlock, orichalcosBlock, photoniumBlock, shadowiumBlock,
				dimensionCatalyst, powerFrame
		));
		pickaxe.addAll(List.of(ALL_PEDESTALS));
		pickaxe.addAll(List.of(ALL_QUARTZ));

		tag(BlockTags.MINEABLE_WITH_PICKAXE).add(getModBlocks(pickaxe::contains));
		tag(BlockTags.NEEDS_STONE_TOOL).add(photoniumBlock, shadowiumBlock);
		tag(BlockTags.NEEDS_IRON_TOOL).add(aerialiteBlock);
		tag(BlockTags.NEEDS_DIAMOND_TOOL).add(orichalcosBlock);
		//Axe
		tag(BlockTags.MINEABLE_WITH_AXE).add(manaCharger);

	}

	@NotNull
	private Block[] getModBlocks(Predicate<Block> predicate) {
		return BuiltInRegistries.BLOCK.stream().filter(EXTRABOTANY_BLOCK.and(predicate))
				.sorted(Comparator.comparing(BuiltInRegistries.BLOCK::getKey))
				.toArray(Block[]::new);
	}
}
