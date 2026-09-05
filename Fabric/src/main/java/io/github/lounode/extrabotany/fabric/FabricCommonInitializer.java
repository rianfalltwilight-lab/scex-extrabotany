package io.github.lounode.extrabotany.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.impl.ModContainerImpl;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.BotaniaFabricCapabilities;
import vazkii.botania.api.brew.Brew;
import vazkii.botania.common.handler.EquipmentHandler;
import vazkii.botania.common.item.CustomCreativeTabContents;

import io.github.lounode.eventwrapper.fabric.AutoEventSubscriberRegistryFabric;
import io.github.lounode.extrabotany.api.ExtraBotaniaRegistries;
import io.github.lounode.extrabotany.api.ExtrabotanyFabricCapabilities;
import io.github.lounode.extrabotany.common.advancements.ExtrabotanyCriteriaTriggers;
import io.github.lounode.extrabotany.common.block.ExtraBotanyBlocks;
import io.github.lounode.extrabotany.common.block.block_entity.ExtraBotanyBlockEntities;
import io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks;
import io.github.lounode.extrabotany.common.brew.ExtraBotanyBrews;
import io.github.lounode.extrabotany.common.brew.ExtraBotanyMobEffects;
import io.github.lounode.extrabotany.common.crafting.ExtraBotanyRecipeTypes;
import io.github.lounode.extrabotany.common.entity.ExtraBotanyEntityType;
import io.github.lounode.extrabotany.common.entity.ExtraBotanyMemoryType;
import io.github.lounode.extrabotany.common.impl.WindImpl;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.item.brew.InfiniteWineItem;
import io.github.lounode.extrabotany.common.item.equipment.bauble.NatureOrbItem;
import io.github.lounode.extrabotany.common.item.equipment.tool.hammer.RheinHammerItem;
import io.github.lounode.extrabotany.common.item.relic.*;
import io.github.lounode.extrabotany.common.item.relic.void_archives.VoidArchivesItem;
import io.github.lounode.extrabotany.common.item.relic.voidcore.CoreOfTheVoidItem;
import io.github.lounode.extrabotany.common.lib.LibMisc;
import io.github.lounode.extrabotany.common.loot.RewardBagManager;
import io.github.lounode.extrabotany.common.sounds.ExtraBotanySounds;
import io.github.lounode.extrabotany.fabric.network.FabricPacketHandler;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.BiConsumer;

public class FabricCommonInitializer implements ModInitializer {
	//private static final Registry<Brew> BREW_REGISTRY = FabricRegistryBuilder.createDefaulted(BotaniaRegistries.BREWS, prefix("fallback")).buildAndRegister();

	@Override
	public void onInitialize() {
		coreInit();
		registryInit();

		registerCapabilities();
		registerEvents();
		registerFuels();
	}

	private void coreInit() {
		//Fix register null bug
		EquipmentHandler.init();

		FabricExtraBotanyConfig.setup();
		FabricPacketHandler.init();

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void registryInit() {
		// Core item/block/BE
		ExtraBotanySounds.init(BuiltInRegistries.SOUND_EVENT);
		ExtraBotanyBlocks.registerBlocks(bind(BuiltInRegistries.BLOCK));
		ExtraBotanyBlocks.registerItemBlocks(boundForItem);
		ExtraBotanyBlockEntities.registerTiles(bind(BuiltInRegistries.BLOCK_ENTITY_TYPE));
		ExtraBotanyItems.registerItems(boundForItem);

		ExtrabotanyFlowerBlocks.registerBlocks(bind(BuiltInRegistries.BLOCK));
		ExtrabotanyFlowerBlocks.registerItemBlocks(boundForItem);
		ExtrabotanyFlowerBlocks.registerTEs(bind(BuiltInRegistries.BLOCK_ENTITY_TYPE));

		// GUI and Recipe
		ExtraBotanyItems.registerRecipeSerializers(bind(BuiltInRegistries.RECIPE_SERIALIZER));
		ExtraBotanyRecipeTypes.submitRecipeTypes(bind(BuiltInRegistries.RECIPE_TYPE));
		ExtraBotanyRecipeTypes.submitRecipeSerializers(bind(BuiltInRegistries.RECIPE_SERIALIZER));

		// Entities
		ExtraBotanyEntityType.registerEntities(bind(BuiltInRegistries.ENTITY_TYPE));
		ExtraBotanyEntityType.registerAttributes(FabricDefaultAttributeRegistry::register);
		ExtraBotanyMemoryType.registerMemories(bind(BuiltInRegistries.MEMORY_MODULE_TYPE));

		// Potions
		ExtraBotanyMobEffects.registerPotions(bind(BuiltInRegistries.MOB_EFFECT));

		//Use force class load because sometimes registry not init
		Registry<Brew> BREW_REGISTRY = BotaniaAPI.instance().getBrewRegistry();
		if (BREW_REGISTRY == null) {
			try {
				Class.forName("vazkii.botania.fabric.FabricCommonInitializer");
			} catch (Exception ignored) {}

			BREW_REGISTRY = BotaniaAPI.instance().getBrewRegistry();
			assert BREW_REGISTRY != null;
		}
		ExtraBotanyBrews.submitRegistrations(bind(BREW_REGISTRY));

		// Rest
		ExtrabotanyCriteriaTriggers.init();

		// CreativeTab
		Registry.register(
				BuiltInRegistries.CREATIVE_MODE_TAB,
				ExtraBotaniaRegistries.EXTRA_BOTANIA_TAB_KEY,
				FabricItemGroup.builder()
						.title(Component.translatable("itemGroup.extrabotany").withStyle(style -> style.withColor(ChatFormatting.WHITE)))
						.hideTitle()
						.icon(() -> new ItemStack(ExtraBotanyItems.zadkiel))
						.backgroundSuffix("extrabotany.png")
						.build()
		);
		ItemGroupEvents.modifyEntriesEvent(ExtraBotaniaRegistries.EXTRA_BOTANIA_TAB_KEY)
				.register(entries -> {
					for (Item item : this.itemsToAddToCreativeTab) {
						if (item instanceof CustomCreativeTabContents cc) {
							cc.addToCreativeTab(item, entries);
						} else if (item instanceof BlockItem bi && bi.getBlock() instanceof CustomCreativeTabContents cc) {
							cc.addToCreativeTab(item, entries);
						} else {
							entries.accept(item);
						}
					}
				});
	}

	private void registerEvents() {
		FabricLoader.getInstance().getModContainer(LibMisc.MOD_ID).ifPresent(mod -> {
			if (mod instanceof ModContainerImpl impl) {
				AutoEventSubscriberRegistryFabric.register(impl);
			}
		});

		ServerTickEvents.START_WORLD_TICK.register(WindImpl.EventHandler::onLevelTick);

		RewardBagManager.registerListener();
	}

	private void registerCapabilities() {
		ExtrabotanyFabricCapabilities.NATURE_ENERGY_ITEM.registerForItems((st, c) -> new NatureOrbItem.NatureEnergyImpl(st), ExtraBotanyItems.natureOrb);
		BotaniaFabricCapabilities.MANA_ITEM.registerForItems((itemStack, unit) -> new MasterBandOfManaItem.ExtendManaItemImpl(itemStack), ExtraBotanyItems.manaRingMaster);
		BotaniaFabricCapabilities.RELIC.registerForItems((st, c) -> MasterBandOfManaItem.makeRelic(st), ExtraBotanyItems.manaRingMaster);
		BotaniaFabricCapabilities.RELIC.registerForItems((st, c) -> CameraItem.makeRelic(st), ExtraBotanyItems.camera);
		BotaniaFabricCapabilities.RELIC.registerForItems((st, c) -> FailnaughtItem.makeRelic(st), ExtraBotanyItems.failnaught);
		BotaniaFabricCapabilities.RELIC.registerForItems((st, c) -> ExcaliburItem.makeRelic(st), ExtraBotanyItems.excalibur);
		BotaniaFabricCapabilities.RELIC.registerForItems((st, c) -> CoreOfTheVoidItem.makeRelic(st), ExtraBotanyItems.coreOfTheVoid);
		BotaniaFabricCapabilities.RELIC.registerForItems((st, c) -> PandorasBoxItem.makeRelic(st), ExtraBotanyItems.pandorasBox);
		BotaniaFabricCapabilities.RELIC.registerForItems((st, c) -> InfiniteWineItem.makeRelic(st), ExtraBotanyItems.infiniteWine);
		BotaniaFabricCapabilities.RELIC.registerForItems((st, c) -> VoidArchivesItem.makeRelic(st), ExtraBotanyItems.voidArchives);
		BotaniaFabricCapabilities.RELIC.registerForItems((st, c) -> RheinHammerItem.makeRelic(st), ExtraBotanyItems.rheinHammer);
		BotaniaFabricCapabilities.RELIC.registerForItems((st, c) -> AchillesShieldItem.makeRelic(st), ExtraBotanyItems.achillesShield);
	}

	private void registerFuels() {
		//FuelRegistry.INSTANCE.add(ExtraBotanyItems.nightmareFuel, 3200);
		//FuelRegistry.INSTANCE.add(ExtraBotanyItems.spiritFuel, 12800);
	}

	private static <T> BiConsumer<T, ResourceLocation> bind(Registry<? super T> registry) {
		return (t, id) -> Registry.register(registry, id, t);
	}

	private final Set<Item> itemsToAddToCreativeTab = new LinkedHashSet<>();
	private final BiConsumer<Item, ResourceLocation> boundForItem =
			(t, id) -> {
				this.itemsToAddToCreativeTab.add(t);
				Registry.register(BuiltInRegistries.ITEM, id, t);
			};
}
