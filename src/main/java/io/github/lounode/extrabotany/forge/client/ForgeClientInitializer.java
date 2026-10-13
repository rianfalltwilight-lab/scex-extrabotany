package io.github.lounode.extrabotany.forge.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;

import vazkii.botania.api.block.WandHUD;
import vazkii.botania.api.neoforge.BotaniaNeoForgeCapabilities;
import vazkii.patchouli.api.PatchouliAPI;

import io.github.lounode.extrabotany.client.ExtraBotanyItemProperties;
import io.github.lounode.extrabotany.client.gui.EnergyBarsHud;
import io.github.lounode.extrabotany.client.gui.HUD;
import io.github.lounode.extrabotany.client.gui.WandHuds;
import io.github.lounode.extrabotany.client.model.ArmorModels;
import io.github.lounode.extrabotany.client.model.ExtrabotanyLayerDefinitions;
import io.github.lounode.extrabotany.client.renderer.BlockRenderLayers;
import io.github.lounode.extrabotany.client.renderer.ColorHandler;
import io.github.lounode.extrabotany.client.renderer.entity.EntityRenderers;
import io.github.lounode.extrabotany.client.renderer.entity.SwordDomainRenderer;
import io.github.lounode.extrabotany.client.renderer.wing.CoreOfTheVoidRenderer;
import io.github.lounode.extrabotany.common.entity.SwordDomainEntity;
import io.github.lounode.extrabotany.common.fluid.ExtraBotanyFluids;
import io.github.lounode.extrabotany.common.lib.LibMisc;
import io.github.lounode.extrabotany.xplat.ExtraBotanyConfig;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import io.github.lounode.extrabotany.common.handler.EgoRequirementHandler;
import io.github.lounode.extrabotany.common.proxy.Proxy;

/**
 * 客户端的入口。它登记渲染器、模型、界面层、物品属性、颜色和法杖界面。
 * The entry point of the client. It records the renderers, the models, the HUD layers, the item properties, the colors and the wand HUDs.
 */
@EventBusSubscriber(modid = LibMisc.MOD_ID, value = Dist.CLIENT)
public class ForgeClientInitializer {
	public static HUD hud;

	@SubscribeEvent
	public static void clientInit(FMLClientSetupEvent evt) {
		evt.enqueueWork(io.github.lounode.extrabotany.client.renderer.AccessoryRenderers::register);
		// 物品属性的表不是线程安全的，所以要在主线程上登记，并且只登记一次。以前它在每次模型重载时、在工作线程上登记。
		// The map of the item properties is not thread-safe, thus the registration must be on the main thread, and only one time. Before, it was done at each model reload, on a worker thread.
		evt.enqueueWork(() -> ExtraBotanyItemProperties.init((item, id, prop) -> ItemProperties.register(item.asItem(), id, prop)));
		BlockRenderLayers.skipPlatformBlocks = true; // platforms can use standard rendering on Forge
		registerBlockRenderLayers();

		var bus = NeoForge.EVENT_BUS;
		hud = new HUD(Minecraft.getInstance());

		PatchouliAPI.get().setConfigFlag("otaku_mode", ExtraBotanyConfig.client().otakuMode());
		bus.addListener((ClientPlayerNetworkEvent.LoggingOut event) -> HUD.onDisconnected());
		bus.addListener(EventPriority.LOWEST, ForgeClientInitializer::addRequirementTooltip);
	}

	/**
	 * 规则"没有击败本我的玩家不能使用"打开时，给受限的遗物加一行红色的提示。
	 * When the rule "a player that did not defeat the Ego cannot use them" is on, this adds a red line to the tooltip of a restricted relic.
	 */
	private static void addRequirementTooltip(ItemTooltipEvent event) {
		if (event.getEntity() != null && EgoRequirementHandler.isRestricted(event.getItemStack())
				&& !Proxy.INSTANCE.hasAdvancementOnClient(EgoRequirementHandler.ADVANCEMENT)) {
			event.getToolTip().add(EgoRequirementHandler.message());
		}
	}

	@SuppressWarnings("deprecation") // Generated flower models do not yet carry NeoForge render_type metadata.
	private static void registerBlockRenderLayers() {
		BlockRenderLayers.init(ItemBlockRenderTypes::setRenderLayer);
		ItemBlockRenderTypes.setRenderLayer(ExtraBotanyFluids.MANA, RenderType.translucent());
		ItemBlockRenderTypes.setRenderLayer(ExtraBotanyFluids.FLOWING_MANA, RenderType.translucent());
	}

	@SubscribeEvent
	private static void registerCapabilities(RegisterCapabilitiesEvent event) {
		var capability = BotaniaNeoForgeCapabilities.getBlockApiLookupById(WandHUD.BLOCK_LOOKUP);
		WandHuds.register((factory, types) -> {
			for (var type : types) {
				event.registerBlockEntity(capability, type, (blockEntity, context) -> factory.apply(blockEntity));
			}
		});
	}

	@SubscribeEvent
	public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
		event.registerFluidType(new net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions() {
			@Override
			public net.minecraft.resources.ResourceLocation getStillTexture() {
				return prefix("block/fluid/fluidedmana_still");
			}
			@Override
			public net.minecraft.resources.ResourceLocation getFlowingTexture() {
				return prefix("block/fluid/fluidedmana_flow");
			}
		}, ExtraBotanyFluids.MANA_TYPE);
		event.registerItem(new IClientItemExtensions() {
			@Override
			public HumanoidModel<?> getHumanoidArmorModel(LivingEntity living, ItemStack stack,
					EquipmentSlot slot, HumanoidModel<?> defaultModel) {
				var model = ArmorModels.get(stack);
				return model != null ? model : defaultModel;
			}
		}, net.minecraft.core.registries.BuiltInRegistries.ITEM.stream()
				.filter(item -> item instanceof io.github.lounode.extrabotany.common.item.equipment.armor.starry_idol.StarryIdolArmorItem)
				.toArray(net.minecraft.world.item.Item[]::new));
	}

	@SubscribeEvent
	public static void registerGuiOverlays(RegisterGuiLayersEvent e) {
		e.registerAbove(VanillaGuiLayers.EXPERIENCE_BAR, prefix("hud"),
				(gui, deltaTracker) -> hud.onDrawScreenPost(gui, deltaTracker.getGameTimeDeltaPartialTick(true)));
		e.registerAbove(VanillaGuiLayers.EXPERIENCE_BAR, prefix("energy_bars"), EnergyBarsHud::render);
	}

	@SubscribeEvent
	public static void registerEntityLayers(EntityRenderersEvent.RegisterLayerDefinitions evt) {
		ExtrabotanyLayerDefinitions.init(evt::registerLayerDefinition);
	}

	@SubscribeEvent
	public static void addPlayerLayers(EntityRenderersEvent.AddLayers evt) {
		for (var skin : evt.getSkins()) {
			net.minecraft.client.renderer.entity.player.PlayerRenderer renderer = evt.getSkin(skin);
			if (renderer != null) {
				renderer.addLayer(new io.github.lounode.extrabotany.client.renderer.FlamescionLayer(renderer));
				renderer.addLayer(new io.github.lounode.extrabotany.client.renderer.HerrscherLayer(renderer));
			}
		}
	}

	@SubscribeEvent
	public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers evt) {
		EntityRenderers.registerBlockEntityRenderers(evt::registerBlockEntityRenderer);
		EntityRenderers.registerEntityRenderers(evt::registerEntityRenderer);
	}

	@SubscribeEvent
	public static void registerBlockColors(RegisterColorHandlersEvent.Block evt) {
		ColorHandler.submitBlocks(evt::register);
	}

	@SubscribeEvent
	public static void registerItemColors(RegisterColorHandlersEvent.Item evt) {
		ColorHandler.submitItems(evt::register);
	}

	@SubscribeEvent
	public static void onModelRegister(ModelEvent.RegisterAdditional evt) {
		evt.register(ModelResourceLocation.standalone(io.github.lounode.extrabotany.client.renderer.entity.ButterflyProjectileRenderer.BUTTERFLY_MODEL));
		for (int i = 0; i < io.github.lounode.extrabotany.common.item.KingGardenItem.WEAPON_TYPES; i++) {
			evt.register(ModelResourceLocation.standalone(io.github.lounode.extrabotany.client.renderer.entity.FlowerWeaponRenderer.model(i)));
		}
		for (int i = 0; i < io.github.lounode.extrabotany.client.renderer.entity.JudahSpearRenderer.MODEL_COUNT; i++) {
			evt.register(ModelResourceLocation.standalone(io.github.lounode.extrabotany.client.renderer.entity.JudahSpearRenderer.model(i)));
		}
		for (var variant : io.github.lounode.extrabotany.common.item.relic.JudahOathItem.Variant.values()) {
			evt.register(ModelResourceLocation.standalone(io.github.lounode.extrabotany.client.renderer.entity.JudahOathRenderer.model(variant)));
		}
		evt.register(ModelResourceLocation.standalone(io.github.lounode.extrabotany.client.renderer.entity.StrengthenSlashRenderer.SLASH_MODEL));
		evt.register(ModelResourceLocation.standalone(io.github.lounode.extrabotany.client.renderer.FlamescionLayer.RING_MODEL));
		for (int i = 0; i < SwordDomainEntity.TYPES; i++) {
			evt.register(ModelResourceLocation.standalone(SwordDomainRenderer.model(i)));
		}
		CoreOfTheVoidRenderer.collectModels(id -> evt.register(ModelResourceLocation.standalone(id)));
	}
}
