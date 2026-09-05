package io.github.lounode.extrabotany.data.loot;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.EntityLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;

/** Entity loot base restricted by subclasses to ExtraBotany entity types. */
public abstract class EntityLootSubProviderFix extends EntityLootSubProvider {
	protected EntityLootSubProviderFix(HolderLookup.Provider registries) {
		super(FeatureFlags.REGISTRY.allFlags(), registries);
	}
}
