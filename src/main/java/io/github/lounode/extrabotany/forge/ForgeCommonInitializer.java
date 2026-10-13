package io.github.lounode.extrabotany.forge;

import com.google.common.base.Suppliers;
import com.mojang.logging.LogUtils;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;

import org.slf4j.Logger;

import vazkii.botania.api.BotaniaRegistries;
import vazkii.botania.api.block.Wandable;
import vazkii.botania.api.item.Relic;
import vazkii.botania.api.mana.ManaItem;
import vazkii.botania.api.mana.ManaReceiver;
import vazkii.botania.api.mana.spark.ManaSparkAttachable;
import vazkii.botania.api.neoforge.BotaniaNeoForgeCapabilities;
import vazkii.botania.common.handler.EquipmentHandler;
import vazkii.botania.common.item.CustomCreativeTabContents;
import vazkii.botania.common.item.equipment.bauble.BaubleItem;
import vazkii.botania.neoforge.integration.curios.CurioIntegration;

import io.github.lounode.extrabotany.api.ExtraBotaniaRegistries;
import io.github.lounode.extrabotany.api.ExtrabotanyForgeCapabilities;
import io.github.lounode.extrabotany.api.item.NatureEnergyItem;
import io.github.lounode.extrabotany.common.attachment.ExtraBotanyAttachments;
import io.github.lounode.extrabotany.common.advancements.ExtrabotanyCriteriaTriggers;
import io.github.lounode.extrabotany.common.block.ExtraBotanyBlocks;
import io.github.lounode.extrabotany.common.block.block_entity.ExtraBotanyBlockEntities;
import io.github.lounode.extrabotany.common.block.block_entity.ManaMachineBlockEntity;
import io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks;
import io.github.lounode.extrabotany.common.brew.ExtraBotanyBrews;
import io.github.lounode.extrabotany.common.brew.ExtraBotanyMobEffects;
import io.github.lounode.extrabotany.common.crafting.ExtraBotanyRecipeTypes;
import io.github.lounode.extrabotany.common.data.ExtraBotanyDataMaps;
import io.github.lounode.extrabotany.common.entity.ExtraBotanyEntityType;
import io.github.lounode.extrabotany.common.entity.ExtraBotanyMemoryType;
import io.github.lounode.extrabotany.common.fluid.ExtraBotanyFluids;
import io.github.lounode.extrabotany.common.impl.WindImpl;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.item.UniversalRunes;
import io.github.lounode.extrabotany.common.item.material.ArmorsMaterial;
import io.github.lounode.extrabotany.common.item.brew.InfiniteWineItem;
import io.github.lounode.extrabotany.common.item.equipment.bauble.NatureOrbItem;
import io.github.lounode.extrabotany.common.item.equipment.tool.hammer.RheinHammerItem;
import io.github.lounode.extrabotany.common.item.equipment.bauble.AllForOneRingItem;
import io.github.lounode.extrabotany.common.item.equipment.bauble.MotorAccessoryItem;
import io.github.lounode.extrabotany.common.item.equipment.bauble.SilentEternityItem;
import io.github.lounode.extrabotany.common.item.relic.*;
import io.github.lounode.extrabotany.common.item.relic.void_archives.VoidArchivesItem;
import io.github.lounode.extrabotany.common.item.relic.voidcore.CoreOfTheVoidItem;
import io.github.lounode.extrabotany.common.lib.LibMisc;
import io.github.lounode.extrabotany.common.loot.RewardBagManager;
import io.github.lounode.extrabotany.common.sounds.ExtraBotanySounds;
import io.github.lounode.extrabotany.forge.event.NeoForgeEventBridge;
import io.github.lounode.extrabotany.forge.network.ForgePacketHandler;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 模组的入口。它登记所有注册表内容、能力、数据包和配置，并把 NeoForge 的事件接到各个处理方法上。
 * The entry point of the mod. It records all registry content, the capabilities, the packets and the configuration, and it connects the NeoForge events to the handler methods.
 */
@Mod(LibMisc.MOD_ID)
public class ForgeCommonInitializer {
	private static final Logger LOGGER = LogUtils.getLogger();

	public ForgeCommonInitializer(IEventBus modBus, ModContainer modContainer) {
		coreInit(modContainer);
		registryInit(modBus);
		modBus.addListener(this::commonSetup);
		modBus.addListener(this::attachCapabilities);
		modBus.addListener(ForgePacketHandler::registerPayloadHandlers);
		modBus.addListener((ModConfigEvent.Loading event) -> ForgeExtrabotanyConfig.onConfigLoad(event));
		modBus.addListener((ModConfigEvent.Reloading event) -> ForgeExtrabotanyConfig.onConfigReload(event));
	}

	public void commonSetup(FMLCommonSetupEvent evt) {
		registerEvents();

		evt.enqueueWork(() -> {
			BiConsumer<ResourceLocation, Supplier<? extends Block>> consumer = (resourceLocation, blockSupplier) -> ((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(resourceLocation, blockSupplier);
			ExtraBotanyBlocks.registerFlowerPotPlants(consumer);
			ExtrabotanyFlowerBlocks.registerFlowerPotPlants(consumer);
			UniversalRunes.init();
		});
	}

	private void coreInit(ModContainer modContainer) {
		ForgeExtrabotanyConfig.setup(modContainer);
	}

	private void registryInit(IEventBus modEventBus) {
		bind(modEventBus, NeoForgeRegistries.Keys.FLUID_TYPES,
				consumer -> consumer.accept(ExtraBotanyFluids.MANA_TYPE, prefix("fluidedmana")));
		bind(modEventBus, Registries.FLUID, consumer -> {
			consumer.accept(ExtraBotanyFluids.MANA, prefix("fluidedmana"));
			consumer.accept(ExtraBotanyFluids.FLOWING_MANA, prefix("flowing_fluidedmana"));
		});
		bind(modEventBus, Registries.BLOCK,
				consumer -> consumer.accept(ExtraBotanyFluids.MANA_BLOCK, prefix("fluidedmana")));
		bindForItems(modEventBus,
				consumer -> consumer.accept(ExtraBotanyFluids.MANA_BUCKET, prefix("fluidedmana_bucket")));
		modEventBus.addListener(ExtraBotanyDataMaps::register);
		modEventBus.addListener((RegisterEvent event) -> runRegistration(
				event, Registries.SOUND_EVENT, ExtraBotanySounds::init));
		modEventBus.addListener((RegisterEvent event) -> runRegistration(
				event, Registries.ARMOR_MATERIAL, ArmorsMaterial::registerArmorMaterials));
		//Block&ItemBlock&Items
		bind(modEventBus, Registries.BLOCK, ExtraBotanyBlocks::registerBlocks);
		bindForItems(modEventBus, ExtraBotanyBlocks::registerItemBlocks);
		bind(modEventBus, Registries.BLOCK_ENTITY_TYPE, ExtraBotanyBlockEntities::registerTiles);
		bindForItems(modEventBus, ExtraBotanyItems::registerItems);

		bind(modEventBus, Registries.BLOCK, ExtrabotanyFlowerBlocks::registerBlocks);
		bindForItems(modEventBus, ExtrabotanyFlowerBlocks::registerItemBlocks);
		bind(modEventBus, Registries.BLOCK_ENTITY_TYPE, ExtrabotanyFlowerBlocks::registerTEs);

		//GUI & Recipe
		bind(modEventBus, Registries.RECIPE_SERIALIZER, ExtraBotanyItems::registerRecipeSerializers);
		bind(modEventBus, Registries.RECIPE_TYPE, ExtraBotanyRecipeTypes::submitRecipeTypes);
		bind(modEventBus, Registries.RECIPE_SERIALIZER, ExtraBotanyRecipeTypes::submitRecipeSerializers);

		// Entities
		bind(modEventBus, Registries.ENTITY_TYPE, ExtraBotanyEntityType::registerEntities);
		modEventBus.addListener((EntityAttributeCreationEvent e) -> ExtraBotanyEntityType.registerAttributes((type, builder) -> e.put(type, builder.build())));
		bind(modEventBus, Registries.MEMORY_MODULE_TYPE, ExtraBotanyMemoryType::registerMemories);

		// Potions
		modEventBus.addListener((RegisterEvent event) -> runRegistration(
				event, Registries.MOB_EFFECT, ExtraBotanyMobEffects::registerPotions));
		bind(modEventBus, BotaniaRegistries.BREWS, ExtraBotanyBrews::submitRegistrations);

		// Rest
		//registerDatas(modEventBus);

		bind(modEventBus, Registries.TRIGGER_TYPE, ExtrabotanyCriteriaTriggers::init);
		bind(modEventBus, net.neoforged.neoforge.registries.NeoForgeRegistries.Keys.ATTACHMENT_TYPES, ExtraBotanyAttachments::register);

		//Creative tab
		bind(modEventBus, Registries.CREATIVE_MODE_TAB, consumer -> {
			consumer.accept(CreativeModeTab.builder()
					.title(Component.translatable("itemGroup.extrabotany").withStyle(style -> style.withColor(ChatFormatting.WHITE)))
					.hideTitle()
					.icon(() -> new ItemStack(ExtraBotanyItems.zadkiel))
					//.withTabsBefore(CreativeModeTabs.NATURAL_BLOCKS)
					.backgroundTexture(prefix("textures/gui/container/creative_inventory/tab_extrabotany.png"))
					//.withSearchBar()
					.build(),
					ExtraBotaniaRegistries.EXTRA_BOTANIA_TAB_KEY.location());
		});

		modEventBus.addListener((BuildCreativeModeTabContentsEvent e) -> {
			if (e.getTabKey() == ExtraBotaniaRegistries.EXTRA_BOTANIA_TAB_KEY) {
				for (Item item : this.itemsToAddToCreativeTab) {
					if (item instanceof CustomCreativeTabContents cc) {
						cc.addToCreativeTab(item, e);
					} else if (item instanceof BlockItem bi && bi.getBlock() instanceof CustomCreativeTabContents cc) {
						cc.addToCreativeTab(item, e);
					} else {
						e.accept(item);
					}
				}
			}
		});

	}

	private void registerEvents() {
		IEventBus bus = NeoForge.EVENT_BUS;
		bus.addListener((LevelTickEvent.Post event) -> WindImpl.EventHandler.onLevelTick(event.getLevel()));
		NeoForgeEventBridge.register(bus);

		RewardBagManager.registerListener();
	}

	private static <T extends net.minecraft.world.level.block.entity.BlockEntity> void registerWandBindable(
			RegisterCapabilitiesEvent event,
			net.neoforged.neoforge.capabilities.BlockCapability<vazkii.botania.api.block.WandBindable, net.minecraft.core.Direction> capability,
			net.minecraft.world.level.block.entity.BlockEntityType<T> type) {
		event.registerBlockEntity(capability, type,
				(blockEntity, side) -> blockEntity instanceof vazkii.botania.api.block.WandBindable bindable ? bindable : null);
	}

	private void attachCapabilities(RegisterCapabilitiesEvent event) {

		// Merged from the former SCEX compat JAR: Botania 456 only exposes WandBindable for its own
		// block entity types, so ExtraBotany flowers could not be selected with the Forest Wand.
		var wandBindable = BotaniaNeoForgeCapabilities.getBlockApiLookupById(vazkii.botania.api.block.WandBindable.LOOKUP);
		for (var entry : BuiltInRegistries.BLOCK_ENTITY_TYPE.entrySet()) {
			if (LibMisc.MOD_ID.equals(entry.getKey().location().getNamespace())) {
				registerWandBindable(event, wandBindable, entry.getValue());
			}
		}
		event.registerBlockEntity(Capabilities.FluidHandler.BLOCK,
				ExtraBotanyBlockEntities.LIVINGROCK_BARREL, (barrel, side) -> barrel);
		event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK,
				ExtraBotanyBlockEntities.MANA_GENERATOR, (generator, side) -> generator.getEnergyStorage());
		for (BlockEntityType<? extends ManaMachineBlockEntity> machine : List.of(
				ExtraBotanyBlockEntities.MANA_BUFFER, ExtraBotanyBlockEntities.QUANTUM_MANA_BUFFER,
				ExtraBotanyBlockEntities.MANA_GENERATOR, ExtraBotanyBlockEntities.MANA_LIQUEFACTION)) {
			event.registerBlockEntity(BotaniaNeoForgeCapabilities.getBlockApiLookupById(Wandable.LOOKUP), machine, (entity, side) -> entity);
			event.registerBlockEntity(BotaniaNeoForgeCapabilities.getBlockApiLookupById(ManaReceiver.LOOKUP), machine, (entity, side) -> entity);
			event.registerBlockEntity(BotaniaNeoForgeCapabilities.getBlockApiLookupById(ManaSparkAttachable.LOOKUP), machine, (entity, context) -> entity);
		}

		if (EquipmentHandler.instance instanceof CurioIntegration curios) {
			Item[] baubleItems = BuiltInRegistries.ITEM.stream()
					.filter(item -> item instanceof BaubleItem)
					.toArray(Item[]::new);
			curios.initCapability(event, baubleItems);
		}

		attachMappedItemCaps(event,
				BotaniaNeoForgeCapabilities.getItemApiLookupById(ManaItem.LOOKUP), MANA_ITEM.get());
		attachMappedItemCaps(event, ExtrabotanyForgeCapabilities.NATURE_ENERGY_ITEM, NATURE_ENERGY_ITEM.get());
		attachMappedItemCaps(event,
				BotaniaNeoForgeCapabilities.getItemApiLookupById(Relic.LOOKUP), RELIC.get());
	}

	private static <T> void attachMappedItemCaps(RegisterCapabilitiesEvent event,
			ItemCapability<T, Void> capability, Map<Item, Function<ItemStack, T>> providers) {
		providers.forEach((item, provider) ->
				event.registerItem(capability, (stack, context) -> provider.apply(stack), item));
	}

	private static final Supplier<Map<Item, Function<ItemStack, NatureEnergyItem>>> NATURE_ENERGY_ITEM = Suppliers.memoize(() -> Map.of(
			ExtraBotanyItems.natureOrb, NatureOrbItem.NatureEnergyImpl::new
	));

	private static final Supplier<Map<Item, Function<ItemStack, ManaItem>>> MANA_ITEM = Suppliers.memoize(() -> Map.of(
			ExtraBotanyItems.manaRingMaster, MasterBandOfManaItem.ExtendManaItemImpl::new,
			ExtraBotanyItems.silentEternity, SilentEternityItem.Reservoir::new
	));
	private static final Supplier<Map<Item, Function<ItemStack, Relic>>> RELIC = Suppliers.memoize(() -> Map.ofEntries(
			Map.entry(ExtraBotanyItems.manaRingMaster, MasterBandOfManaItem::makeRelic),
			Map.entry(ExtraBotanyItems.camera, CameraItem::makeRelic),
			Map.entry(ExtraBotanyItems.failnaught, FailnaughtItem::makeRelic),
			Map.entry(ExtraBotanyItems.excalibur, ExcaliburItem::makeRelic),
			Map.entry(ExtraBotanyItems.coreOfTheVoid, CoreOfTheVoidItem::makeRelic),
			Map.entry(ExtraBotanyItems.pandorasBox, PandorasBoxItem::makeRelic),
			Map.entry(ExtraBotanyItems.infiniteWine, InfiniteWineItem::makeRelic),
			Map.entry(ExtraBotanyItems.voidArchives, VoidArchivesItem::makeRelic),
			Map.entry(ExtraBotanyItems.rheinHammer, RheinHammerItem::makeRelic),
			Map.entry(ExtraBotanyItems.achillesShield, AchillesShieldItem::makeRelic),
			Map.entry(ExtraBotanyItems.sunRing, SunRingItem::makeRelic),
			Map.entry(ExtraBotanyItems.moonPendant, MoonPendantItem::makeRelic),
			Map.entry(ExtraBotanyItems.trueTerrablade, RelicSwordItem::makeRelic),
			Map.entry(ExtraBotanyItems.trueShadowKatana, RelicSwordItem::makeRelic),
			Map.entry(ExtraBotanyItems.influxWaver, RelicSwordItem::makeRelic),
			Map.entry(ExtraBotanyItems.starWrath, RelicSwordItem::makeRelic),
			Map.entry(ExtraBotanyItems.firstFractal, FirstFractalItem::makeRelic),
			Map.entry(ExtraBotanyItems.spearOfSubspace, SpearOfSubspaceItem::makeRelic),
			Map.entry(ExtraBotanyItems.judahOath, JudahOathItem::makeRelic),
			Map.entry(ExtraBotanyItems.judahOathKira, JudahOathItem::makeRelic),
			Map.entry(ExtraBotanyItems.judahOathSakura, JudahOathItem::makeRelic),
			Map.entry(ExtraBotanyItems.silentEternity, SilentEternityItem::makeRelic),
			Map.entry(ExtraBotanyItems.elvenKing, AllForOneRingItem::makeRelic),
			Map.entry(ExtraBotanyItems.allForOne, AllForOneRingItem::makeRelic),
			Map.entry(ExtraBotanyItems.motor, MotorItem::makeRelic),
			Map.entry(ExtraBotanyItems.motorAccessory, MotorAccessoryItem::makeRelic)
	));

	private static <T> void bind(IEventBus modEventBus, ResourceKey<Registry<T>> registry, Consumer<BiConsumer<T, ResourceLocation>> source) {
		modEventBus.addListener((RegisterEvent event) -> {
			if (registry.equals(event.getRegistryKey())) {
				source.accept((t, rl) -> event.register(registry, rl, () -> t));
			}
		});
	}

	private static <T> void runRegistration(RegisterEvent event, ResourceKey<Registry<T>> registryKey,
			Consumer<Registry<T>> source) {
		Registry<T> registry = event.getRegistry(registryKey);
		if (registry != null) {
			source.accept(registry);
		}
	}

	private final Set<Item> itemsToAddToCreativeTab = new LinkedHashSet<>();

	private void bindForItems(IEventBus modEventBus, Consumer<BiConsumer<Item, ResourceLocation>> source) {
		modEventBus.addListener((RegisterEvent event) -> {
			if (event.getRegistryKey().equals(Registries.ITEM)) {
				source.accept((t, rl) -> {
					itemsToAddToCreativeTab.add(t);
					event.register(Registries.ITEM, rl, () -> t);
				});
			}
		});
	}
}
