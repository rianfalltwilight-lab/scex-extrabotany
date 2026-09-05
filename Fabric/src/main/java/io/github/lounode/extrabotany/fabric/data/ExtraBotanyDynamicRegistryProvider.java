package io.github.lounode.extrabotany.fabric.data;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.core.HolderLookup;

import java.util.concurrent.CompletableFuture;

import static io.github.lounode.extrabotany.common.ExtraBotanyDamageTypes.*;

public class ExtraBotanyDynamicRegistryProvider extends FabricDynamicRegistryProvider {

	public ExtraBotanyDynamicRegistryProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
		super(output, registriesFuture);
	}

	@Override
	protected void configure(HolderLookup.Provider provider, Entries entries) {
		entries.add(LINK_DAMAGE, LINK);
		entries.add(EXCALIBUR_BEAM_DAMAGE, EXCALIBUR);
		entries.add(JINGWEI_PUNCH_DAMAGE, JINGWEI);
		entries.add(REVERSE_HEAL_DAMAGE, REVERSE_HEAL);
		entries.add(BACKFIRE_DAMAGE, BACKFIRE);
	}

	@Override
	public String getName() {
		return "ExtraBotanyDynamicRegistryProvider";
	}
}
