package io.github.lounode.extrabotany.fabric.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;

import vazkii.botania.xplat.XplatAbstractions;

import io.github.lounode.extrabotany.data.tags.BlockTagProvider;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class FabricBlockTagProvider extends BlockTagProvider {

	public FabricBlockTagProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
		super(packOutput, lookupProvider);
	}

	@Override
	protected void addTags(HolderLookup.Provider provider) {

		var vanillaTags = List.of(
				BlockTags.COAL_ORES,
				BlockTags.IRON_ORES,
				BlockTags.GOLD_ORES,
				BlockTags.LAPIS_ORES,
				BlockTags.REDSTONE_ORES,
				BlockTags.DIAMOND_ORES,
				BlockTags.COPPER_ORES,
				BlockTags.EMERALD_ORES
		);
		// We aren't calling vanilla's generation, so need to add dummy calls so that using them below doesn't error out.
		vanillaTags.forEach(this::tag);

		var oreTag = tag(XplatAbstractions.INSTANCE.getOreTag());
		vanillaTags.forEach(oreTag::addTag);

	}
}
