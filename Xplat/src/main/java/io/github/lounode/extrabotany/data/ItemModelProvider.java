package io.github.lounode.extrabotany.data;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.models.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.common.block.flower.FloatingFlowerBaseBlock;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.data.util.ModelWithOverrides;
import vazkii.botania.data.util.OverrideHolder;
import vazkii.botania.mixin.TextureSlotAccessor;

import io.github.lounode.extrabotany.api.ExtraBotanyAPI;
import io.github.lounode.extrabotany.api.item.VoidArchivesVariant;
import io.github.lounode.extrabotany.common.item.relic.void_archives.variants.*;
import io.github.lounode.extrabotany.common.lib.LibMisc;
import io.github.lounode.extrabotany.xplat.EXplatAbstractions;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static io.github.lounode.extrabotany.common.item.ExtraBotanyItems.*;
import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;
import static vazkii.botania.data.ItemModelProvider.takeAll;

public class ItemModelProvider implements DataProvider {
	private static final TextureSlot LAYER1 = TextureSlotAccessor.botania_create("layer1");
	private static final TextureSlot LAYER2 = TextureSlotAccessor.botania_create("layer2");
	private static final TextureSlot LAYER3 = TextureSlotAccessor.botania_create("layer3");
	private static final ModelTemplate GENERATED_0 = new ModelTemplate(Optional.of(ResourceLocation.tryParse("item/generated")), Optional.empty(), TextureSlot.LAYER0);
	private static final ModelTemplate GENERATED_1 = new ModelTemplate(Optional.of(ResourceLocation.tryParse("item/generated")), Optional.empty(), TextureSlot.LAYER0, LAYER1);
	private static final ModelTemplate GENERATED_2 = new ModelTemplate(Optional.of(ResourceLocation.tryParse("item/generated")), Optional.empty(), TextureSlot.LAYER0, LAYER1, LAYER2);
	private static final ModelTemplate HANDHELD_0 = new ModelTemplate(Optional.of(ResourceLocation.tryParse("item/handheld")), Optional.empty(), TextureSlot.LAYER0);
	private static final ModelTemplate HANDHELD_1 = new ModelTemplate(Optional.of(ResourceLocation.tryParse("item/handheld")), Optional.empty(), TextureSlot.LAYER0, LAYER1);
	private static final ModelTemplate HANDHELD_3 = new ModelTemplate(Optional.of(ResourceLocation.tryParse("item/handheld")), Optional.empty(), TextureSlot.LAYER0, LAYER1, LAYER2, LAYER3);
	private static final ModelTemplate WALL_INVENTORY = new ModelTemplate(Optional.of(prefix("block/shapes/wall_inventory")), Optional.empty(), TextureSlot.TOP, TextureSlot.BOTTOM, TextureSlot.WALL);
	private static final ModelTemplate WALL_INVENTORY_CHECKERED = new ModelTemplate(Optional.of(prefix("block/shapes/wall_inventory_checkered")), Optional.empty(), TextureSlot.NORTH, TextureSlot.SIDE);
	private static final TextureSlot OUTSIDE = TextureSlotAccessor.botania_create("outside");
	private static final TextureSlot CORE = TextureSlotAccessor.botania_create("core");
	private static final ModelTemplate SPREADER = new ModelTemplate(Optional.of(prefix("block/shapes/spreader_item")), Optional.empty(), TextureSlot.SIDE, TextureSlot.BACK, TextureSlot.INSIDE, OUTSIDE, CORE);
	private static final ModelWithOverrides GENERATED_OVERRIDES = new ModelWithOverrides(ResourceLocation.tryParse("item/generated"), TextureSlot.LAYER0);
	private static final ModelWithOverrides GENERATED_OVERRIDES_1 = new ModelWithOverrides(ResourceLocation.tryParse("item/generated"), TextureSlot.LAYER0, LAYER1);
	private static final ModelWithOverrides HANDHELD_OVERRIDES = new ModelWithOverrides(ResourceLocation.tryParse("item/handheld"), TextureSlot.LAYER0);
	private static final ModelWithOverrides HANDHELD_OVERRIDES_2 = new ModelWithOverrides(ResourceLocation.tryParse("item/handheld"), TextureSlot.LAYER0, LAYER1, LAYER2);

	private final PackOutput packOutput;

	public ItemModelProvider(PackOutput packOutput) {
		this.packOutput = packOutput;
	}

	@Override
	public CompletableFuture<?> run(CachedOutput cache) {
		Set<Item> items = BuiltInRegistries.ITEM.stream().filter(i -> LibMisc.MOD_ID.equals(BuiltInRegistries.ITEM.getKey(i).getNamespace()))
				.collect(Collectors.toSet());
		Map<ResourceLocation, Supplier<JsonElement>> map = new HashMap<>();
		// Exact deployed seasonal models are maintained in the existing-resource root.
		items.removeAll(List.of(candyBag, candyEins, candyZwei, candyDrei));
		items.removeAll(List.of(elementRune, sinRune, recordHerrscherOfTheVoid));

		registerItemBlocks(takeAll(items, i -> i instanceof BlockItem).stream().map(i -> (BlockItem) i).collect(Collectors.toSet()), map::put);
		registerItemOverrides(items, map::put);
		registerItems(items, map::put);

		PackOutput.PathProvider modelPathProvider = packOutput.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models");
		List<CompletableFuture<?>> output = new ArrayList<>();

		for (Map.Entry<ResourceLocation, Supplier<JsonElement>> e : map.entrySet()) {
			ResourceLocation id = e.getKey();
			output.add(DataProvider.saveStable(cache, e.getValue().get(), modelPathProvider.json(id)));
		}

		return CompletableFuture.allOf(output.toArray(CompletableFuture[]::new));
	}

	private void registerItemBlocks(Set<BlockItem> itemBlocks, BiConsumer<ResourceLocation, Supplier<JsonElement>> consumer) {
		// Generated by FloatingFlowerModelProvider
		itemBlocks.removeIf(i -> {
			var id = BuiltInRegistries.BLOCK.getKey(i.getBlock());
			return id.getNamespace().equals(LibMisc.MOD_ID) && i.getBlock() instanceof FloatingFlowerBaseBlock;
		});

		Predicate<BlockItem> defaultGenerated = i -> {
			Block b = i.getBlock();
			return EXplatAbstractions.INSTANCE.isSpecialFlowerBlock(b);
		};
		takeAll(itemBlocks, defaultGenerated).forEach(i -> {
			ModelTemplates.FLAT_ITEM.create(ModelLocationUtils.getModelLocation(i), TextureMapping.layer0(i.getBlock()), consumer);
		});

		itemBlocks.forEach(i -> consumer.accept(ModelLocationUtils.getModelLocation(i), new DelegatedModel(ModelLocationUtils.getModelLocation(i.getBlock()))));
	}

	private static void registerItems(Set<Item> items, BiConsumer<ResourceLocation, Supplier<JsonElement>> consumer) {
		//Manual items
		items.remove(failnaught);

		takeAll(items, manasteelHammer, elementiumHammer, gaiaHammer,
				photoniumHammer, shadowiumHammer, aerialiteHammer,
				walkingCane, magicFinger, manaReader
		).forEach(i -> ModelTemplates.FLAT_HANDHELD_ITEM.create(ModelLocationUtils.getModelLocation(i), TextureMapping.layer0(i), consumer));

		takeAll(items, i -> true).forEach(i -> ModelTemplates.FLAT_ITEM.create(ModelLocationUtils.getModelLocation(i), TextureMapping.layer0(i), consumer));
	}

	private static void registerItemOverrides(Set<Item> items, BiConsumer<ResourceLocation, Supplier<JsonElement>> consumer) {

		OverrideHolder manaCocktailOverrides = new OverrideHolder();
		for (int i = 1; i <= 7; i++) {
			ResourceLocation overrideModel = ModelLocationUtils.getModelLocation(manaCocktail, "_" + i);
			GENERATED_1.create(overrideModel,
					TextureMapping.layer0(manaCocktail).put(LAYER1, overrideModel),
					consumer);

			manaCocktailOverrides.add(overrideModel, Pair.of(prefix("swigs_taken"), (double) i * 0.01D));
		}

		GENERATED_OVERRIDES_1.create(ModelLocationUtils.getModelLocation(manaCocktail),
				TextureMapping.layer0(manaCocktail).put(LAYER1, TextureMapping.getItemTexture(manaCocktail, "_0")),
				manaCocktailOverrides,
				consumer);

		items.remove(manaCocktail);

		OverrideHolder infiniteWineOverrides = new OverrideHolder();
		for (int i = 1; i <= 12; i++) {
			int modelSuffix = i;
			if (i == 1 || i == 2 || i == 3) {
				modelSuffix = 3;
			} else if (i == 4 || i == 5) {
				modelSuffix = 5;
			} else if (i == 6 || i == 7) {
				modelSuffix = 7;
			} else if (i == 8 || i == 9) {
				modelSuffix = 9;
			} else if (i == 10 || i == 11) {
				modelSuffix = 11;
			}

			ResourceLocation overrideModel = ModelLocationUtils.getModelLocation(infiniteWine, "_" + modelSuffix);
			if (i == modelSuffix) {
				if (i != 12) {
					GENERATED_1.create(overrideModel,
							TextureMapping.layer0(infiniteWine).put(LAYER1, overrideModel),
							consumer);
				} else {
					GENERATED_0.create(overrideModel,
							TextureMapping.layer0(infiniteWine),
							consumer);
				}

				infiniteWineOverrides.add(overrideModel, Pair.of(prefix("swigs_taken"), (double) i * 0.01D));
			}
		}

		GENERATED_OVERRIDES_1.create(ModelLocationUtils.getModelLocation(infiniteWine),
				TextureMapping.layer0(infiniteWine).put(LAYER1, TextureMapping.getItemTexture(infiniteWine, "_0")),
				infiniteWineOverrides,
				consumer);

		items.remove(infiniteWine);

		OverrideHolder holyWaterGrenadeOverrides = new OverrideHolder();

		GENERATED_OVERRIDES_1.create(ModelLocationUtils.getModelLocation(holyWaterGrenade),
				TextureMapping.layer0(holyWaterGrenade).put(LAYER1, TextureMapping.getItemTexture(holyWaterGrenade, "_cover")),
				holyWaterGrenadeOverrides,
				consumer);

		items.remove(holyWaterGrenade);

		OverrideHolder voidArchivesOverrides = new OverrideHolder();
		int index = 1;
		for (var variant : ExtraBotanyAPI.instance().getVoidArchivesVariants().entrySet()) {
			String key = variant.getKey();
			ResourceLocation overrideModel = ModelLocationUtils.getModelLocation(voidArchives, "_" + key);

			if (key.equals(VoidArchivesVariant.DEFAULT.getId())) {
				continue;
			}
			if (key.equals(Excalibur.INSTANCE.getId())) {
				HANDHELD_0.create(overrideModel,
						TextureMapping.layer0(excalibur),
						consumer);
			}
			if (key.equals(FruitOfGrisaia.INSTANCE.getId())) {
				GENERATED_0.create(overrideModel, TextureMapping.layer0(BotaniaItems.FRUIT_OF_GRISAIA), consumer);
			}
			if (key.equals(Camera.INSTANCE.getId())) {
				GENERATED_0.create(overrideModel, TextureMapping.layer0(camera), consumer);
			}
			if (key.equals(Failnaught.INSTANCE.getId())) {
				overrideModel = ModelLocationUtils.getModelLocation(failnaught);
			}
			if (key.equals(InfiniteWine.INSTANCE.getId())) {
				GENERATED_1.create(overrideModel,
						TextureMapping.layer0(infiniteWine).put(LAYER1, ModelLocationUtils.getModelLocation(infiniteWine, "_" + 0)),
						consumer);
			}

			voidArchivesOverrides.add(overrideModel, Pair.of(prefix("variant"), (double) index * 0.01D));
			index++;
		}

		GENERATED_OVERRIDES.create(ModelLocationUtils.getModelLocation(voidArchives),
				TextureMapping.layer0(voidArchives),
				voidArchivesOverrides,
				consumer);

		items.remove(voidArchives);

		ResourceLocation terrHammerEnabledModel = ModelLocationUtils.getModelLocation(terrasteelHammer, "_active");
		ModelTemplates.FLAT_HANDHELD_ITEM.create(terrHammerEnabledModel, TextureMapping.layer0(terrHammerEnabledModel), consumer);

		HANDHELD_OVERRIDES.create(ModelLocationUtils.getModelLocation(terrasteelHammer),
				TextureMapping.layer0(terrasteelHammer),
				new OverrideHolder().add(terrHammerEnabledModel, Pair.of(prefix("active"), 1.0)),
				consumer);
		items.remove(terrasteelHammer);

		ResourceLocation orichalcosHammerEnabledModel = ModelLocationUtils.getModelLocation(orichalcosHammer, "_active");
		ModelTemplates.FLAT_HANDHELD_ITEM.create(orichalcosHammerEnabledModel, TextureMapping.layer0(orichalcosHammerEnabledModel), consumer);

		HANDHELD_OVERRIDES.create(ModelLocationUtils.getModelLocation(orichalcosHammer),
				TextureMapping.layer0(orichalcosHammer),
				new OverrideHolder().add(orichalcosHammerEnabledModel, Pair.of(prefix("active"), 1.0)),
				consumer);
		items.remove(orichalcosHammer);

		ResourceLocation rheinHammerEnabledModel = ModelLocationUtils.getModelLocation(rheinHammer, "_active");
		ModelTemplates.FLAT_HANDHELD_ITEM.create(rheinHammerEnabledModel, TextureMapping.layer0(rheinHammerEnabledModel), consumer);

		HANDHELD_OVERRIDES.create(ModelLocationUtils.getModelLocation(rheinHammer),
				TextureMapping.layer0(rheinHammer),
				new OverrideHolder().add(rheinHammerEnabledModel, Pair.of(prefix("active"), 1.0)),
				consumer);
		items.remove(rheinHammer);

		GENERATED_OVERRIDES_1.create(ModelLocationUtils.getModelLocation(holyWaterGrenade),
				TextureMapping.layer0(holyWaterGrenade).put(LAYER1, TextureMapping.getItemTexture(holyWaterGrenade, "_cover")),
				holyWaterGrenadeOverrides,
				consumer);

		items.remove(holyWaterGrenade);

		ResourceLocation senketsuModel = ModelLocationUtils.getModelLocation(sanguinePleiadesCombatMaidSuit, "_senketsu");
		GENERATED_0.create(senketsuModel, TextureMapping.layer0(senketsuModel), consumer);
		GENERATED_OVERRIDES.create(ModelLocationUtils.getModelLocation(sanguinePleiadesCombatMaidSuit),
				TextureMapping.layer0(sanguinePleiadesCombatMaidSuit),
				new OverrideHolder().add(senketsuModel, Pair.of(prefix("senketsu"), 1.0)),
				consumer);
		items.remove(sanguinePleiadesCombatMaidSuit);

		ResourceLocation saberModel = ModelLocationUtils.getModelLocation(excalibur, "_saber");
		ModelTemplates.FLAT_HANDHELD_ITEM.create(saberModel, TextureMapping.layer0(saberModel), consumer);

		HANDHELD_OVERRIDES.create(ModelLocationUtils.getModelLocation(excalibur),
				TextureMapping.layer0(excalibur),
				new OverrideHolder().add(saberModel, Pair.of(prefix("saber"), 1.0)),
				consumer);

		items.remove(excalibur);

		Item[] shields = new Item[] { manasteelShield, elementiumShield, terrasteelShield, achillesShield };

		for (var shield : shields) {
			ResourceLocation shieldModel = ModelLocationUtils.getModelLocation(shield);

			var shieldModelTemplate = new ModelWithOverrides(prefix("item/template/shield"), TextureSlot.LAYER0);
			var blockingModelTemplate = new ModelTemplate(Optional.of(prefix("item/template/shield_blocking")), Optional.empty(), TextureSlot.LAYER0);

			var overrides = new OverrideHolder();
			if (shield == achillesShield) {
				HANDHELD_0.create(shieldModel.withSuffix("_released"),
						TextureMapping.layer0(shieldModel.withSuffix("_released")),
						consumer);
				overrides.add(shieldModel.withSuffix("_blocking"),
						Pair.of(prefix("blocking"), 1.0),
						Pair.of(prefix("released"), 0.0));
				overrides.add(shieldModel.withSuffix("_released"),
						Pair.of(prefix("blocking"), 0.0),
						Pair.of(prefix("released"), 1.0));
			} else {
				overrides.add(shieldModel.withSuffix("_blocking"), Pair.of(prefix("blocking"), 1.0));
			}
			shieldModelTemplate.create(shieldModel,
					TextureMapping.layer0(shield),
					overrides,
					consumer);
			blockingModelTemplate.create(shieldModel.withSuffix("_blocking"),
					TextureMapping.layer0(shield),
					consumer);

			items.remove(shield);
		}
	}

	@NotNull
	@Override
	public String getName() {
		return "ExtraBotany item models";
	}
}
