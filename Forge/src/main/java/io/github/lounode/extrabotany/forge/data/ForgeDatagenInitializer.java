package io.github.lounode.extrabotany.forge.data;

import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.structures.SnbtToNbt;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import io.github.lounode.extrabotany.common.lib.LibMisc;
import io.github.lounode.extrabotany.data.AdvancementProvider;
import io.github.lounode.extrabotany.data.BlockstateProvider;
import io.github.lounode.extrabotany.data.FloatingFlowerModelProvider;
import io.github.lounode.extrabotany.data.ItemModelProvider;
import io.github.lounode.extrabotany.data.PatchouliBookProvider;
import io.github.lounode.extrabotany.data.PottedPlantModelProvider;
import io.github.lounode.extrabotany.data.SoundProvider;
import io.github.lounode.extrabotany.data.loot.BlockLootProvider;
import io.github.lounode.extrabotany.data.loot.EntityLootProvider;
import io.github.lounode.extrabotany.data.loot.RewardBagLootProvider;
import io.github.lounode.extrabotany.data.recipes.BrewProvider;
import io.github.lounode.extrabotany.data.recipes.CraftingRecipeProvider;
import io.github.lounode.extrabotany.data.recipes.EdelweissRecipeProvider;
import io.github.lounode.extrabotany.data.recipes.ElvenTradeProvider;
import io.github.lounode.extrabotany.data.recipes.ManaInfusionProvider;
import io.github.lounode.extrabotany.data.recipes.OmnivioletProvider;
import io.github.lounode.extrabotany.data.recipes.PedestalRecipeProvider;
import io.github.lounode.extrabotany.data.recipes.PetalApothecaryProvider;
import io.github.lounode.extrabotany.data.recipes.RunicAltarProvider;
import io.github.lounode.extrabotany.data.recipes.SmeltingProvider;
import io.github.lounode.extrabotany.data.recipes.SmithingRecipeProvider;
import io.github.lounode.extrabotany.data.recipes.StonesiaProvider;
import io.github.lounode.extrabotany.data.recipes.TerrestrialAgglomerationProvider;
import io.github.lounode.extrabotany.data.tags.BlockTagProvider;
import io.github.lounode.extrabotany.data.tags.DamageTypeTagProvider;
import io.github.lounode.extrabotany.data.tags.EntityTypeTagProvider;
import io.github.lounode.extrabotany.data.tags.ItemTagProvider;

import java.util.List;
import java.util.Set;

import static io.github.lounode.extrabotany.common.ExtraBotanyDamageTypes.BACKFIRE;
import static io.github.lounode.extrabotany.common.ExtraBotanyDamageTypes.BACKFIRE_DAMAGE;
import static io.github.lounode.extrabotany.common.ExtraBotanyDamageTypes.EXCALIBUR;
import static io.github.lounode.extrabotany.common.ExtraBotanyDamageTypes.EXCALIBUR_BEAM_DAMAGE;
import static io.github.lounode.extrabotany.common.ExtraBotanyDamageTypes.JINGWEI;
import static io.github.lounode.extrabotany.common.ExtraBotanyDamageTypes.JINGWEI_PUNCH_DAMAGE;
import static io.github.lounode.extrabotany.common.ExtraBotanyDamageTypes.LINK;
import static io.github.lounode.extrabotany.common.ExtraBotanyDamageTypes.LINK_DAMAGE;
import static io.github.lounode.extrabotany.common.ExtraBotanyDamageTypes.REVERSE_HEAL;
import static io.github.lounode.extrabotany.common.ExtraBotanyDamageTypes.REVERSE_HEAL_DAMAGE;

@EventBusSubscriber(modid = LibMisc.MOD_ID)
public final class ForgeDatagenInitializer {
	private ForgeDatagenInitializer() {}

	@SubscribeEvent
	public static void configureForgeDatagen(GatherDataEvent evt) {
		var generator = evt.getGenerator();
		var output = generator.getPackOutput();
		generator.addProvider(evt.includeServer(), new SnbtToNbt(output, evt.getInputs()));
		evt.createDatapackRegistryObjects(new RegistrySetBuilder().add(Registries.DAMAGE_TYPE, context -> {
			context.register(LINK_DAMAGE, LINK);
			context.register(EXCALIBUR_BEAM_DAMAGE, EXCALIBUR);
			context.register(JINGWEI_PUNCH_DAMAGE, JINGWEI);
			context.register(REVERSE_HEAL_DAMAGE, REVERSE_HEAL);
			context.register(BACKFIRE_DAMAGE, BACKFIRE);
		}));
		var lookupProvider = evt.getLookupProvider();

		BlockTagProvider commonBlockTags = new BlockTagProvider(output, lookupProvider);
		generator.addProvider(evt.includeServer(), commonBlockTags);
		generator.addProvider(evt.includeServer(),
				new ItemTagProvider(output, lookupProvider, commonBlockTags.contentsGetter()));
		generator.addProvider(evt.includeServer(), new EntityTypeTagProvider(output, lookupProvider));
		generator.addProvider(evt.includeServer(), new DamageTypeTagProvider(output, lookupProvider));

		ForgeBlockTagProvider neoForgeBlockTags = new ForgeBlockTagProvider(
				output, lookupProvider, evt.getExistingFileHelper());
		generator.addProvider(evt.includeServer(), neoForgeBlockTags);
		generator.addProvider(evt.includeServer(), new ForgeItemTagProvider(
				output, lookupProvider, neoForgeBlockTags.contentsGetter(), evt.getExistingFileHelper()));

		generator.addProvider(evt.includeServer(), new LootTableProvider(output, Set.of(), List.of(
				new LootTableProvider.SubProviderEntry(BlockLootProvider::new, LootContextParamSets.BLOCK),
				new LootTableProvider.SubProviderEntry(EntityLootProvider::new, LootContextParamSets.ENTITY),
				new LootTableProvider.SubProviderEntry(ignored -> new RewardBagLootProvider(), LootContextParamSets.EMPTY)
		), lookupProvider));

		generator.addProvider(evt.includeServer(), new PedestalRecipeProvider(output, lookupProvider));
		generator.addProvider(evt.includeServer(), new CraftingRecipeProvider(output, lookupProvider));
		generator.addProvider(evt.includeServer(), new SmeltingProvider(output, lookupProvider));
		generator.addProvider(evt.includeServer(), new SmithingRecipeProvider(output, lookupProvider));
		generator.addProvider(evt.includeServer(), new ElvenTradeProvider(output, lookupProvider));
		generator.addProvider(evt.includeServer(), new ManaInfusionProvider(output, lookupProvider));
		generator.addProvider(evt.includeServer(), new BrewProvider(output, lookupProvider));
		generator.addProvider(evt.includeServer(), new PetalApothecaryProvider(output, lookupProvider));
		generator.addProvider(evt.includeServer(), new RunicAltarProvider(output, lookupProvider));
		generator.addProvider(evt.includeServer(), new TerrestrialAgglomerationProvider(output, lookupProvider));
		generator.addProvider(evt.includeServer(), new StonesiaProvider(output, lookupProvider));
		generator.addProvider(evt.includeServer(), new EdelweissRecipeProvider(output, lookupProvider));
		generator.addProvider(evt.includeServer(), new OmnivioletProvider(output, lookupProvider));
		generator.addProvider(evt.includeServer(), AdvancementProvider.create(
				output, lookupProvider, evt.getExistingFileHelper()));

		generator.addProvider(evt.includeClient(), new BlockstateProvider(output));
		generator.addProvider(evt.includeClient(), new FloatingFlowerModelProvider(output));
		generator.addProvider(evt.includeClient(), new ItemModelProvider(output));
		generator.addProvider(evt.includeClient(), new PottedPlantModelProvider(output));
		generator.addProvider(evt.includeClient(), new SoundProvider(output, LibMisc.MOD_ID));
		generator.addProvider(evt.includeClient(), new PatchouliBookProvider(output));
	}
}
