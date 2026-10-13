package io.github.lounode.extrabotany.forge.data;

import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import io.github.lounode.extrabotany.common.ExtraBotanyDamageTypes;
import io.github.lounode.extrabotany.common.lib.LibMisc;
import io.github.lounode.extrabotany.data.AdvancementProvider;
import io.github.lounode.extrabotany.data.BlockstateProvider;
import io.github.lounode.extrabotany.data.FloatingFlowerModelProvider;
import io.github.lounode.extrabotany.data.HandWrittenFilter;
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

/**
 * 数据生成的入口（{@code gradlew runData}）。它登记所有的生成器，输出到 {@code src/generated/resources}。
 * The entry point of the data generation ({@code gradlew runData}). It records all providers, and the output goes to {@code src/generated/resources}.
 *
 * <p>输出目录完全属于生成器：运行结束时，目录里不是这次生成的文件都会被删除。所以不要把手写的文件放进那个目录，手写的文件放在 {@code src/main/resources}。
 * <p>The output directory belongs to the generators completely: at the end of a run, each file in the directory that this run did not make is deleted. Thus do not put hand-written files into that directory; they go into {@code src/main/resources}.
 *
 * <p>每个生成器都包了一层 {@link HandWrittenFilter}，所以同一个路径不会既有手写的文件又有生成的文件。
 * <p>Each provider has a {@link HandWrittenFilter} around it, thus one path never has a hand-written file and a generated file together.
 */
@EventBusSubscriber(modid = LibMisc.MOD_ID)
public final class ForgeDatagenInitializer {
	private ForgeDatagenInitializer() {}

	@SubscribeEvent
	public static void configureForgeDatagen(GatherDataEvent evt) {
		DataGenerator generator = evt.getGenerator();
		PackOutput output = generator.getPackOutput();
		boolean server = evt.includeServer();
		boolean client = evt.includeClient();

		evt.createDatapackRegistryObjects(new RegistrySetBuilder().add(Registries.DAMAGE_TYPE, context -> {
			context.register(ExtraBotanyDamageTypes.LINK_DAMAGE, ExtraBotanyDamageTypes.LINK);
			context.register(ExtraBotanyDamageTypes.EXCALIBUR_BEAM_DAMAGE, ExtraBotanyDamageTypes.EXCALIBUR);
			context.register(ExtraBotanyDamageTypes.JINGWEI_PUNCH_DAMAGE, ExtraBotanyDamageTypes.JINGWEI);
			context.register(ExtraBotanyDamageTypes.REVERSE_HEAL_DAMAGE, ExtraBotanyDamageTypes.REVERSE_HEAL);
			context.register(ExtraBotanyDamageTypes.BACKFIRE_DAMAGE, ExtraBotanyDamageTypes.BACKFIRE);
			context.register(ExtraBotanyDamageTypes.PIERCING_DAMAGE, ExtraBotanyDamageTypes.PIERCING);
			context.register(ExtraBotanyDamageTypes.MAGIC_PIERCING_DAMAGE, ExtraBotanyDamageTypes.MAGIC_PIERCING);
			context.register(ExtraBotanyDamageTypes.HERRSCHER_ICE_DAMAGE, ExtraBotanyDamageTypes.HERRSCHER_ICE);
			context.register(ExtraBotanyDamageTypes.HERRSCHER_THUNDER_DAMAGE, ExtraBotanyDamageTypes.HERRSCHER_THUNDER);
			context.register(ExtraBotanyDamageTypes.FLAMESCION_FLAME_DAMAGE, ExtraBotanyDamageTypes.FLAMESCION_FLAME);
			context.register(ExtraBotanyDamageTypes.PIERCING_FIRE_DAMAGE, ExtraBotanyDamageTypes.PIERCING_FIRE);
		}));
		var lookupProvider = evt.getLookupProvider();

		// 标签。 / Tags.
		BlockTagProvider commonBlockTags = new BlockTagProvider(output, lookupProvider);
		add(generator, server, commonBlockTags);
		add(generator, server, new ItemTagProvider(output, lookupProvider, commonBlockTags.contentsGetter()));
		add(generator, server, new EntityTypeTagProvider(output, lookupProvider));
		add(generator, server, new DamageTypeTagProvider(output, lookupProvider));


		// 战利品表。 / Loot tables.
		add(generator, server, new LootTableProvider(output, Set.of(), List.of(
				new LootTableProvider.SubProviderEntry(BlockLootProvider::new, LootContextParamSets.BLOCK),
				new LootTableProvider.SubProviderEntry(EntityLootProvider::new, LootContextParamSets.ENTITY),
				new LootTableProvider.SubProviderEntry(ignored -> new RewardBagLootProvider(), LootContextParamSets.EMPTY)
		), lookupProvider));

		// 配方和进度。 / Recipes and advancements.
		add(generator, server, new PedestalRecipeProvider(output, lookupProvider));
		add(generator, server, new CraftingRecipeProvider(output, lookupProvider));
		add(generator, server, new SmeltingProvider(output, lookupProvider));
		add(generator, server, new SmithingRecipeProvider(output, lookupProvider));
		add(generator, server, new ElvenTradeProvider(output, lookupProvider));
		add(generator, server, new ManaInfusionProvider(output, lookupProvider));
		add(generator, server, new BrewProvider(output, lookupProvider));
		add(generator, server, new PetalApothecaryProvider(output, lookupProvider));
		add(generator, server, new RunicAltarProvider(output, lookupProvider));
		add(generator, server, new TerrestrialAgglomerationProvider(output, lookupProvider));
		add(generator, server, new StonesiaProvider(output, lookupProvider));
		add(generator, server, new EdelweissRecipeProvider(output, lookupProvider));
		add(generator, server, new OmnivioletProvider(output, lookupProvider));
		add(generator, server, AdvancementProvider.create(output, lookupProvider, evt.getExistingFileHelper()));

		// 客户端资源。 / Client assets.
		add(generator, client, new BlockstateProvider(output));
		add(generator, client, new FloatingFlowerModelProvider(output));
		add(generator, client, new ItemModelProvider(output));
		add(generator, client, new PottedPlantModelProvider(output));
		add(generator, client, new SoundProvider(output, evt.getExistingFileHelper()));
		add(generator, client, new PatchouliBookProvider(output));
	}

	private static void add(DataGenerator generator, boolean run, DataProvider provider) {
		generator.addProvider(run, new HandWrittenFilter(provider, generator.getPackOutput().getOutputFolder()));
	}
}
