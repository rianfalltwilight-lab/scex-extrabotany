package io.github.lounode.extrabotany.data.loot;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.EntityLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;

/**
 * 生物战利品表生成器的基类。它只负责把"启用全部特性"传给原版的基类；子类决定处理哪些实体类型。
 * The base class of the generators for entity loot tables. It only gives "all features enabled" to the vanilla base class; the subclass selects the entity types.
 */
public abstract class EntityLootSubProviderFix extends EntityLootSubProvider {
	protected EntityLootSubProviderFix(HolderLookup.Provider registries) {
		super(FeatureFlags.REGISTRY.allFlags(), registries);
	}
}
