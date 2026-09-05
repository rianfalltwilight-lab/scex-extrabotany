package io.github.lounode.extrabotany.fabric.data;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import io.github.lounode.extrabotany.common.lib.LibMisc;
import io.github.lounode.extrabotany.data.*;
import io.github.lounode.extrabotany.data.loot.BlockLootProvider;
import io.github.lounode.extrabotany.data.loot.EntityLootProvider;
import io.github.lounode.extrabotany.data.loot.RewardBagLootProvider;
import io.github.lounode.extrabotany.data.recipes.*;
import io.github.lounode.extrabotany.data.tags.BlockTagProvider;
import io.github.lounode.extrabotany.data.tags.DamageTypeTagProvider;
import io.github.lounode.extrabotany.data.tags.EntityTypeTagProvider;
import io.github.lounode.extrabotany.data.tags.ItemTagProvider;

import java.util.List;
import java.util.Set;

import static io.github.lounode.extrabotany.common.ExtraBotanyDamageTypes.*;

public class FabricDatagenInitializer implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator generator) {

		if (System.getProperty("extrabotany.xplat_datagen") != null) {
			configureXplatDatagen(generator.createPack());
		} else {
			configureFabricDatagen(generator.createPack());
		}
	}

	private void configureFabricDatagen(FabricDataGenerator.Pack pack) {
		pack.addProvider((PackOutput output) -> new FabricBlockLootProvider(output));
		var blockTagProvider = pack.addProvider(FabricBlockTagProvider::new);
		pack.addProvider((output, registriesFuture) -> new FabricItemTagProvider(output, registriesFuture, blockTagProvider.contentsGetter()));
	}

	private void configureXplatDatagen(FabricDataGenerator.Pack pack) {
		pack.addProvider((PackOutput output) -> new BlockLootProvider(output));

		pack.addProvider((PackOutput output) -> new LootTableProvider(output, Set.of(), List.of(
				new LootTableProvider.SubProviderEntry(EntityLootProvider::new, LootContextParamSets.ENTITY),
				new LootTableProvider.SubProviderEntry(RewardBagLootProvider::new, LootContextParamSets.EMPTY)
		)));
		//pack.addProvider((PackOutput output) -> new LooniumStructureLootProvider(output));
		//pack.addProvider((PackOutput output) -> new LooniumStructureConfigurationProvider(output));
		BlockTagProvider blockTagProvider = pack.addProvider(BlockTagProvider::new);
		pack.addProvider((output, registriesFuture) -> new ItemTagProvider(output, registriesFuture, blockTagProvider.contentsGetter()));
		pack.addProvider(EntityTypeTagProvider::new);
		pack.addProvider(ExtraBotanyDynamicRegistryProvider::new);
		pack.addProvider(DamageTypeTagProvider::new);

		//Recipes
		pack.addProvider((PackOutput output) -> new PedestalRecipeProvider(output));
		//pack.addProvider((PackOutput output) -> new StonecuttingProvider(output));
		pack.addProvider((PackOutput output) -> new CraftingRecipeProvider(output));
		pack.addProvider((PackOutput output) -> new SmeltingProvider(output));
		pack.addProvider((PackOutput output) -> new SmithingRecipeProvider(output));
		pack.addProvider((PackOutput output) -> new ElvenTradeProvider(output));
		pack.addProvider((PackOutput output) -> new ManaInfusionProvider(output));
		//pack.addProvider((PackOutput output) -> new PureDaisyProvider(output));
		pack.addProvider((PackOutput output) -> new BrewProvider(output));
		pack.addProvider((PackOutput output) -> new PetalApothecaryProvider(output));
		pack.addProvider((PackOutput output) -> new RunicAltarProvider(output));
		pack.addProvider((PackOutput output) -> new TerrestrialAgglomerationProvider(output));
		//pack.addProvider((PackOutput output) -> new OrechidProvider(output));
		pack.addProvider((PackOutput output) -> new StonesiaProvider(output));
		pack.addProvider((PackOutput output) -> new EdelweissRecipeProvider(output));
		pack.addProvider((PackOutput output) -> new OmnivioletProvider(output));
		pack.addProvider((PackOutput output) -> new BlockstateProvider(output));
		pack.addProvider((PackOutput output) -> new FloatingFlowerModelProvider(output));
		pack.addProvider((PackOutput output) -> new ItemModelProvider(output));
		pack.addProvider((PackOutput output) -> new PottedPlantModelProvider(output));
		pack.addProvider(AdvancementProvider::create);
		pack.addProvider((PackOutput output) -> new SoundProvider(output, LibMisc.MOD_ID));
		pack.addProvider((PackOutput output) -> new PatchouliBookProvider(output));
	}

	@Override
	public void buildRegistry(RegistrySetBuilder builder) {
		builder.add(Registries.DAMAGE_TYPE, FabricDatagenInitializer::damageTypeBC);
	}

	protected static void damageTypeBC(BootstapContext<DamageType> context) {
		context.register(LINK_DAMAGE, LINK);
		context.register(EXCALIBUR_BEAM_DAMAGE, EXCALIBUR);
		context.register(JINGWEI_PUNCH_DAMAGE, JINGWEI);
		context.register(REVERSE_HEAL_DAMAGE, REVERSE_HEAL);
		context.register(BACKFIRE_DAMAGE, BACKFIRE);
	}
}
