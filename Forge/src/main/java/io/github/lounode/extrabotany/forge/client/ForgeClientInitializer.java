package io.github.lounode.extrabotany.forge.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
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
import io.github.lounode.extrabotany.client.core.ExtraBotanyModels;
import io.github.lounode.extrabotany.client.gui.HUD;
import io.github.lounode.extrabotany.client.model.ArmorModels;
import io.github.lounode.extrabotany.client.model.ExtrabotanyLayerDefinitions;
import io.github.lounode.extrabotany.client.renderer.BlockRenderLayers;
import io.github.lounode.extrabotany.client.renderer.ColorHandler;
import io.github.lounode.extrabotany.client.renderer.entity.EntityRenderers;
import io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.lib.LibMisc;
import io.github.lounode.extrabotany.xplat.ExtraBotanyConfig;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

@EventBusSubscriber(modid = LibMisc.MOD_ID, value = Dist.CLIENT)
public class ForgeClientInitializer {
	public static HUD hud;

	@SubscribeEvent
	public static void clientInit(FMLClientSetupEvent evt) {
		evt.enqueueWork(io.github.lounode.extrabotany.client.renderer.LegacyAccessoryRenderers::register);
		BlockRenderLayers.skipPlatformBlocks = true; // platforms can use standard rendering on Forge
		registerBlockRenderLayers();

		var bus = NeoForge.EVENT_BUS;
		hud = new HUD(Minecraft.getInstance());

		PatchouliAPI.get().setConfigFlag("otaku_mode", ExtraBotanyConfig.client().otakuMode());
		/*
		bus.addListener((CustomizeGuiOverlayEvent.BossEventProgress e) -> {
			var result = BossBarHandler.onBarRender(e.getGuiGraphics(), e.getX(), e.getY(),
					e.getBossEvent(), true);
			result.ifPresent(increment -> {
				e.setCanceled(true);
				e.setIncrement(increment);
			});
		});
		
		//MasterRingToolTip TODO
		
		bus.addListener(EventPriority.LOWEST, (RenderTooltipEvent.Color e) -> {
			var manaItem = vazkii.botania.api.mana.ManaItem.LOOKUP.find(e.getItemStack());
			if (manaItem == null) {
				return;
			}
			// Forge does not pass the tooltip width to any tooltip event.
			// To avoid a mixin here, we just duplicate the width checking part.
			int width = 0;
			MasterBandOfManaTooltipComponent manaBar = null;
			for (ClientTooltipComponent component : e.getComponents()) {
				width = Math.max(width, component.getWidth(e.getFont()));
				if (component instanceof MasterBandOfManaTooltipComponent c) {
					manaBar = c;
				}
			}
			if (manaBar != null) {
				manaBar.setContext(e.getX(), e.getY(), width);
			}
		});
		
		*/
		bus.addListener((ClientPlayerNetworkEvent.LoggingOut event) -> HUD.onDisconnected());
	}

	@SuppressWarnings("deprecation") // Generated flower models do not yet carry NeoForge render_type metadata.
	private static void registerBlockRenderLayers() {
		BlockRenderLayers.init(ItemBlockRenderTypes::setRenderLayer);
		ItemBlockRenderTypes.setRenderLayer(io.github.lounode.extrabotany.forge.fluid.LegacyManaFluid.SOURCE, net.minecraft.client.renderer.RenderType.translucent());
		ItemBlockRenderTypes.setRenderLayer(io.github.lounode.extrabotany.forge.fluid.LegacyManaFluid.FLOWING, net.minecraft.client.renderer.RenderType.translucent());
	}

	@SubscribeEvent
	private static void registerCapabilities(RegisterCapabilitiesEvent event) {
		var capability = BotaniaNeoForgeCapabilities.getBlockApiLookupById(WandHUD.BLOCK_LOOKUP);
		ExtrabotanyFlowerBlocks.registerWandHudCaps((factory, types) -> {
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
		}, io.github.lounode.extrabotany.forge.fluid.LegacyManaFluid.TYPE);
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
	}

	@SubscribeEvent
	public static void registerEntityLayers(EntityRenderersEvent.RegisterLayerDefinitions evt) {
		ExtrabotanyLayerDefinitions.init(evt::registerLayerDefinition);
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
		var resourceManager = Minecraft.getInstance().getResourceManager();
		ExtraBotanyModels.INSTANCE.onModelRegister(resourceManager,
				id -> evt.register(ModelResourceLocation.standalone(id)));
		ExtraBotanyItemProperties.init((item, id, prop) -> ItemProperties.register(item.asItem(), id, prop));
	}

	@SubscribeEvent
	public static void onModelBake(ModelEvent.ModifyBakingResult evt) {
		ExtraBotanyModels.INSTANCE.onModelBake(evt.getModelBakery(), evt.getModels());
	}
}
