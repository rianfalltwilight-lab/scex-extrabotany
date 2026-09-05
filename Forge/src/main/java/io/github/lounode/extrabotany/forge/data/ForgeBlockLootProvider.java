package io.github.lounode.extrabotany.forge.data;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;

import java.util.concurrent.CompletableFuture;

public class ForgeBlockLootProvider implements DataProvider {
	private final DataGenerator generator;

	public ForgeBlockLootProvider(DataGenerator generator) {
		this.generator = generator;
	}

	@Override
	public CompletableFuture<?> run(CachedOutput cache) {
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public String getName() {
		return "ExtraBotany block loot (Forge-specific)";
	}
}
