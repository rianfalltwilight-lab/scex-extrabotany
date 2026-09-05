package io.github.lounode.extrabotany.forge.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import io.github.lounode.extrabotany.common.lib.LibMisc;

import java.util.concurrent.CompletableFuture;

import static io.github.lounode.extrabotany.common.block.ExtraBotanyBlocks.aerialiteBlock;
import static io.github.lounode.extrabotany.common.block.ExtraBotanyBlocks.orichalcosBlock;
import static io.github.lounode.extrabotany.common.block.ExtraBotanyBlocks.photoniumBlock;
import static io.github.lounode.extrabotany.common.block.ExtraBotanyBlocks.shadowiumBlock;

public class ForgeBlockTagProvider extends IntrinsicHolderTagsProvider<Block> {
	public static final TagKey<Block> MANASTEEL = common("storage_blocks/manasteel");
	public static final TagKey<Block> ORICHALCOS = common("storage_blocks/orichalcos");
	public static final TagKey<Block> PHOTONIUM = common("storage_blocks/photonium");
	public static final TagKey<Block> SHADOWIUM = common("storage_blocks/shadowium");
	public static final TagKey<Block> AERIALITE = common("storage_blocks/aerialite");

	public ForgeBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider,
			ExistingFileHelper existingFileHelper) {
		super(output, Registries.BLOCK, provider,
				block -> BuiltInRegistries.BLOCK.getResourceKey(block).orElseThrow(), LibMisc.MOD_ID, existingFileHelper);
	}

	@Override
	protected void addTags(HolderLookup.Provider provider) {
		IntrinsicTagAppender<Block> storageBlocks = tag(Tags.Blocks.STORAGE_BLOCKS);

		tag(ORICHALCOS).add(orichalcosBlock);
		tag(PHOTONIUM).add(photoniumBlock);
		tag(SHADOWIUM).add(shadowiumBlock);
		tag(AERIALITE).add(aerialiteBlock);

		storageBlocks
				.addTag(ORICHALCOS)
				.addTag(PHOTONIUM)
				.addTag(SHADOWIUM)
				.addTag(AERIALITE);
	}

	@Override
	public String getName() {
		return "ExtraBotany block tags (Forge-specific)";
	}

	private static TagKey<Block> common(String name) {
		return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", name));
	}
}
