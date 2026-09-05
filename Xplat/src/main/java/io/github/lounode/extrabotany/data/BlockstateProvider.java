package io.github.lounode.extrabotany.data;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.models.model.ModelTemplate;
import net.minecraft.data.models.model.ModelTemplates;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.data.models.model.TextureSlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.common.block.flower.FloatingFlowerBaseBlock;
import vazkii.botania.mixin.BlockModelGeneratorsAccessor;

import io.github.lounode.extrabotany.common.block.ExtraBotanyBlocks;
import io.github.lounode.extrabotany.common.block.PedestalBlock;
import io.github.lounode.extrabotany.common.lib.LibMisc;
import io.github.lounode.extrabotany.xplat.EXplatAbstractions;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;
import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefixBotania;
import static net.minecraft.data.models.model.ModelLocationUtils.getModelLocation;
import static net.minecraft.data.models.model.TextureMapping.getBlockTexture;

public class BlockstateProvider extends vazkii.botania.data.BlockstateProvider {
	public BlockstateProvider(PackOutput packOutput) {
		super(packOutput);
	}

	@NotNull
	@Override
	public String getName() {
		return "ExtraBotany Blockstates and Models";
	}

	@Override
	protected void registerStatesAndModels() {
		Set<Block> remainingBlocks = BuiltInRegistries.BLOCK.stream()
				.filter(b -> LibMisc.MOD_ID.equals(BuiltInRegistries.BLOCK.getKey(b).getNamespace()))
				.collect(Collectors.toSet());

		//ManaCharger
		manualModel(remainingBlocks, ExtraBotanyBlocks.manaCharger);
		manualModel(remainingBlocks, ExtraBotanyBlocks.powerFrame);

		// Botania removed enchanted soil in 1.21. Reuse a vanilla texture instead of fabricating its old asset.
		singleVariantBlockState(ExtraBotanyBlocks.enchantedSoil,
				ModelTemplates.CUBE_ALL.create(ExtraBotanyBlocks.enchantedSoil,
						TextureMapping.cube(Blocks.MOSS_BLOCK), this.modelOutput));
		remainingBlocks.remove(ExtraBotanyBlocks.enchantedSoil);

		//Pedestal
		var pedestalTemplate = new ModelTemplate(Optional.of(prefix("block/shapes/pedestal")), Optional.empty(),
				TextureSlot.TOP, TextureSlot.UP, TextureSlot.DOWN, TextureSlot.SIDE, TextureSlot.PARTICLE);
		takeAll(remainingBlocks, b -> b instanceof PedestalBlock).forEach(b -> singleVariantBlockState(b,
				pedestalTemplate.create(b, new TextureMapping()
						.put(TextureSlot.TOP, getBlockTexture(b, "_top"))
						.put(TextureSlot.UP, getBlockTexture(b, "_up"))
						.put(TextureSlot.DOWN, getBlockTexture(b, "_down"))
						.put(TextureSlot.SIDE, getBlockTexture(b, "_side"))
						.put(TextureSlot.PARTICLE, getBlockTexture(b, "_down")), this.modelOutput))
		);
		//Quartz
		for (String variant : new String[] { "gaia", "elementium" }) {
			ResourceLocation quartzId = prefix(variant + "_quartz_block");
			Block quartz = BuiltInRegistries.BLOCK.get(quartzId);
			singleVariantBlockState(quartz,
					ModelTemplates.CUBE_BOTTOM_TOP.create(quartz, TextureMapping.cubeBottomTop(quartz), this.modelOutput));

			ResourceLocation pillarId = prefix(variant + "_quartz_pillar");
			Block pillar = BuiltInRegistries.BLOCK.get(pillarId);
			var pillarModel = ModelTemplates.CUBE_COLUMN.create(pillar,
					TextureMapping.column(getBlockTexture(pillar, ""), getBlockTexture(pillar, "_top")),
					this.modelOutput);
			this.blockStateGenerators.add(BlockModelGeneratorsAccessor.botania_createAxisAlignedPillarBlock(pillar, pillarModel));

			ResourceLocation chiseledId = prefix("chiseled_" + variant + "_quartz_block");
			Block chiseled = BuiltInRegistries.BLOCK.get(chiseledId);
			singleVariantBlockState(chiseled,
					ModelTemplates.CUBE_COLUMN.create(chiseled, new TextureMapping()
							.put(TextureSlot.SIDE, getBlockTexture(chiseled, ""))
							.put(TextureSlot.END, getBlockTexture(chiseled, "_top")), this.modelOutput));

			ResourceLocation bricksId = prefix(variant + "_quartz_bricks");
			Block bricks = BuiltInRegistries.BLOCK.get(bricksId);
			singleVariantBlockState(bricks,
					ModelTemplates.CUBE_ALL.create(bricks, TextureMapping.cube(bricks), this.modelOutput));

			ResourceLocation smoothId = prefix("smooth_" + variant + "_quartz");
			Block smooth = BuiltInRegistries.BLOCK.get(smoothId);
			singleVariantBlockState(smooth,
					ModelTemplates.CUBE_ALL.create(smooth, TextureMapping.cube(prefix("block/" + variant + "_quartz_block_bottom")), this.modelOutput));

			remainingBlocks.remove(quartz);
			remainingBlocks.remove(pillar);
			remainingBlocks.remove(chiseled);
			remainingBlocks.remove(bricks);
			remainingBlocks.remove(smooth);
		}

		takeAll(remainingBlocks, b -> b instanceof StairBlock).forEach(b -> {
			String name = BuiltInRegistries.BLOCK.getKey(b).getPath();
			String baseName = name.substring(0, name.length() - vazkii.botania.common.lib.LibBlockNames.STAIRS_SUFFIX.length());
			boolean quartz = name.contains("quartz");
			if (quartz) {
				baseName = baseName + "_block";
				ResourceLocation side = prefix("block/" + baseName + "_side");
				//ResourceLocation bottom = prefix("block/" + baseName + "_bottom");
				ResourceLocation top = prefix("block/" + baseName + "_top");
				boolean smooth = name.contains("smooth");
				if (!smooth) {
					stairsBlock(new HashSet<>(), b, side, top, top);
				} else {
					ResourceLocation bottom = prefix("block/" + baseName.replace("smooth_", "") + "_bottom");
					stairsBlock(new HashSet<>(), b, bottom, bottom, bottom);
				}
			} else {
				var tex = prefix("block/" + baseName);
				stairsBlock(new HashSet<>(), b, tex, tex, tex);
			}
		});

		takeAll(remainingBlocks, b -> b instanceof SlabBlock).forEach(slabBlock -> {
			String name = BuiltInRegistries.BLOCK.getKey(slabBlock).getPath();
			String baseName = name.substring(0, name.length() - vazkii.botania.common.lib.LibBlockNames.SLAB_SUFFIX.length());
			Block base = BuiltInRegistries.BLOCK.get(prefix(baseName));
			boolean quartz = name.contains("quartz");
			if (quartz) {
				base = BuiltInRegistries.BLOCK.get(prefix(baseName.replace("smooth_", "") + "_block"));
				var side = getBlockTexture(base, "_side");
				var bottom = getBlockTexture(base, "_bottom");
				var top = getBlockTexture(base, "_top");
				var doubleModel = getModelLocation(base);
				boolean smooth = name.contains("smooth");
				if (!smooth) {
					slabBlock(new HashSet<>(), slabBlock, doubleModel, side, top, top);
				} else {
					slabBlock(new HashSet<>(), slabBlock, doubleModel, bottom, bottom, bottom);
				}
			} else {
				var baseTex = getBlockTexture(base);
				var doubleModel = getModelLocation(base);
				slabBlock(new HashSet<>(), slabBlock, doubleModel, baseTex, baseTex, baseTex);
			}
		});
		/*
		takeAll(remainingBlocks, b -> b instanceof WallBlock).forEach(wallBlock -> {
			String name = BuiltInRegistries.BLOCK.getKey(wallBlock).getPath();
			String baseName = name.substring(0, name.length() - vazkii.botania.common.lib.LibBlockNames.WALL_SUFFIX.length());
			Block base = BuiltInRegistries.BLOCK.get(prefix(baseName));
			var baseTexture = getBlockTexture(base);
			wallBlock(new HashSet<>(), wallBlock, baseTexture);
		});
		
		*/
		Predicate<Block> flowers = EXplatAbstractions.INSTANCE::isSpecialFlowerBlock;
		ModelTemplate crossTemplate = new ModelTemplate(Optional.of(prefixBotania("block/shapes/cross")), Optional.empty(), TextureSlot.CROSS);
		takeAll(remainingBlocks, flowers).forEach(b -> {
			singleVariantBlockState(b, crossTemplate.create(b, TextureMapping.cross(b), this.modelOutput));
		});

		takeAll(remainingBlocks, b -> b instanceof FloatingFlowerBaseBlock).forEach(b -> {
			// Models generated by FloatingFlowerModelProvider
			singleVariantBlockState(b, getModelLocation(b));
		});

		remainingBlocks.forEach(this::cubeAllNoRemove);
	}
}
