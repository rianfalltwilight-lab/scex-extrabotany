package io.github.lounode.extrabotany.data.loot;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

public abstract class RewardBagLootSubProvider implements LootTableSubProvider {
	private final Map<ResourceKey<LootTable>, LootTable.Builder> map;

	protected RewardBagLootSubProvider() {
		this.map = Maps.newHashMap();
	}

	public abstract void generate();
	public abstract String getNameSpace();

	@Override
	public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
		this.generate();
		Set<ResourceKey<LootTable>> duplicates = Sets.newHashSet();
		Set<ResourceKey<LootTable>> processed = Sets.newHashSet();

		for (var entry : this.map.entrySet()) {
			ResourceKey<LootTable> location = entry.getKey();
			if (!processed.add(location)) {
				duplicates.add(location);
			}
		}

		if (!duplicates.isEmpty()) {
			throw new IllegalStateException("Duplicate loot tables: " + duplicates);
		}

		this.map.forEach((location, builder) -> {
			try {
				output.accept(location, builder);
			} catch (Exception e) {
				throw new IllegalStateException("Failed to create loot table '" + location + "'", e);
			}
		});

	}

	protected void add(ResourceKey<LootTable> lootTableLocation, LootTable.Builder builder) {
		this.map.put(lootTableLocation, builder);
	}

	public void add(String key, LootTable.Builder builder) {
		ResourceLocation lootTableLocation = ResourceLocation.tryBuild(getNameSpace(), key).withPrefix("reward_bags/");
		this.add(ResourceKey.create(Registries.LOOT_TABLE, lootTableLocation), builder);
	}
}
