package io.github.lounode.extrabotany.forge.gametest;

import com.google.gson.GsonBuilder;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiStack;
import io.github.lounode.extrabotany.client.integration.emi.EmiExtrabotanyPlugin;
import io.github.lounode.extrabotany.client.integration.emi.recipe.StonesiaEmiRecipe;
import io.github.lounode.extrabotany.common.brew.BrewUtil;
import io.github.lounode.extrabotany.common.brew.ExtraBotanyBrews;
import io.github.lounode.extrabotany.common.crafting.ExtraBotanyRecipeTypes;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.item.brew.ManaCocktailItem;
import io.github.lounode.extrabotany.common.item.relic.void_archives.VoidArchivesItem;
import io.github.lounode.extrabotany.common.item.relic.void_archives.variants.InfiniteWine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.common.brew.BotaniaBrews;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.item.brew.BaseBrewItem;
import vazkii.botania.common.item.lens.LensItem;
import vazkii.botania.api.item.Relic;
import vazkii.botania.api.mana.ManaItem;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/** Opt-in real EMI registration and rendering regression; only compiled into the GameTest source set. */
@EventBusSubscriber(modid = "extrabotany", value = Dist.CLIENT)
public final class P2ClientProbe {

	private static int stage, ticks, total;
	private static final Map<String, Object> checks = new LinkedHashMap<>();
	private static final List<EmiRecipe> pages = new ArrayList<>();
	private static CompletableFuture<Void> serverSetup;
	private static int repairBefore;
	private static final List<TintSample> tintSamples = new ArrayList<>();
	private record TintSample(String label, ItemStack stack, int tint) {}

	@SubscribeEvent
	public static void tick(ClientTickEvent.Post event) {
		if (!Boolean.getBoolean("extrabotany.p2ClientProbe") || stage == 99) return;
		Minecraft mc = Minecraft.getInstance();
		try {
			if (++total > 4800) throw new AssertionError("Client probe timeout at stage " + stage);
			mc.options.pauseOnLostFocus = false;
			mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
			if (stage == 0 && mc.screen instanceof net.minecraft.client.gui.screens.AccessibilityOnboardingScreen) {
				mc.options.onboardingAccessibilityFinished();
				mc.setScreen(new TitleScreen());
			}
			if (stage == 0 && mc.screen instanceof TitleScreen && mc.getOverlay() == null) {
				stage = 1;
				mc.createWorldOpenFlows().createFreshLevel("extrabotany-p2-probe",
						new LevelSettings("ExtraBotany P2 regression", GameType.CREATIVE, false, Difficulty.NORMAL, true, new GameRules(), WorldDataConfiguration.DEFAULT),
						new WorldOptions(20261025L, false, false),
						r -> r.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(), new TitleScreen());
				return;
			}
			if (mc.level == null || mc.player == null) return;
			if (stage == 1) {
				if (mc.screen != null || mc.getOverlay() != null) return;
				// EMI reload is asynchronous. Read its actual manager only after our entries arrive.
				if (EmiApi.getRecipeManager().getRecipes(EmiExtrabotanyPlugin.STONESIA).isEmpty()) return;
				verifyRecipes(mc);
				checks.put("renderer", org.lwjgl.opengl.GL11.glGetString(org.lwjgl.opengl.GL11.GL_RENDERER));
				var uuid = mc.player.getUUID();
				serverSetup = mc.getSingleplayerServer().submit(() -> {
					var player = mc.getSingleplayerServer().getPlayerList().getPlayer(uuid);
					player.setGameMode(GameType.SURVIVAL);
					var sword = new ItemStack(ExtraBotanyItems.trueTerrablade);
					Relic.LOOKUP.find(sword).bindToUUID(uuid);
					sword.setDamageValue(200);
					player.getInventory().setItem(0, sword);
					var tablet = new ItemStack(BotaniaItems.MANA_TABLET);
					ManaItem.LOOKUP.find(tablet).addMana(100000);
					player.getInventory().setItem(1, tablet);
					player.getInventory().selected = 0;
				});
				stage = 10;
				ticks = 0;
				return;
			}
			if (stage == 10) {
				if (!serverSetup.isDone()) return;
				serverSetup.join();
				if (!mc.player.getMainHandItem().is(ExtraBotanyItems.trueTerrablade)) return;
				if (++ticks < 30) return;
				repairBefore = mc.player.getMainHandItem().getDamageValue();
				check(repairBefore > 50 && repairBefore < 200, "real_server_repair_reached_client");
				stage = 11;
				ticks = 0;
				return;
			}
			if (stage == 11) {
				var height = net.minecraft.client.renderer.ItemInHandRenderer.class.getDeclaredField("mainHandHeight");
				height.setAccessible(true);
				check(height.getFloat(mc.gameRenderer.itemInHandRenderer) > 0.99F, "stable_hand_height_during_repair_tick_" + ticks);
				if (++ticks < 30) return;
				check(mc.player.getMainHandItem().getDamageValue() <= repairBefore - 20, "repair_continued_during_30_render_ticks");
				capture(mc, "held-repair.png");
				stage = 2;
				ticks = 0;
				EmiApi.displayRecipe(pages.getFirst());
				return;
			}
			if (stage >= 2 && stage < 10 && ++ticks >= 30) {
				int page = stage - 2;
				capture(mc, "emi-p2-" + page + ".png");
				if (page + 1 == pages.size()) {
					stage = 12;
					ticks = 0;
				} else {
					stage++;
					ticks = 0;
					EmiApi.displayRecipe(pages.get(page + 1));
				}
				return;
			}
			if (stage == 12) {
				prepareTintSamples(mc);
				mc.setScreen(new TintPreviewScreen());
				stage = 13;
				ticks = 0;
				return;
			}
			if (stage == 13 && ++ticks >= 30) {
				// Capture the real renderer before checking alpha, including on the failing baseline.
				capture(mc, "item-tints.png");
				verifyItemTints(mc);
				finish(mc, null);
			}
		} catch (Throwable error) {
			finish(mc, error);
		}
	}

	private static void capture(Minecraft mc, String name) throws java.io.IOException {
		check(name.equals("held-repair.png")
				? mc.screen == null && !mc.options.hideGui && mc.options.getCameraType().isFirstPerson()
				: name.equals("item-tints.png") ? mc.screen instanceof TintPreviewScreen
				: mc.screen != null && mc.screen.getClass().getName().startsWith("dev.emi.emi."), "visible_scene_" + name);
		var output = mc.gameDirectory.toPath().resolve("verification");
		Files.createDirectories(output);
		var file = output.resolve(name);
		// Synchronous save on the render thread; a queued request alone never counts as a capture.
		try (var pixels = Screenshot.takeScreenshot(mc.getMainRenderTarget())) {
			pixels.writeToFile(file);
		}
		check(Files.size(file) > 0, "captured_" + name);
	}

	private static ItemStack craft(Minecraft mc, String path, ItemStack first, ItemStack second) {
		var recipe = (CraftingRecipe) mc.level.getRecipeManager().byKey(prefix(path)).orElseThrow().value();
		var input = CraftingInput.of(2, 1, List.of(first, second));
		check(recipe.matches(input, mc.level), "tint_preview_matches_" + path);
		ItemStack output = recipe.assemble(input, mc.level.registryAccess());
		check(!output.isEmpty(), "tint_preview_real_output_" + path);
		return output;
	}

	private static void prepareTintSamples(Minecraft mc) {
		ItemStack flask = new ItemStack(BotaniaItems.BREW_FLASK);
		BrewUtil.setBrew(flask, ExtraBotanyBrews.revolution);
		ItemStack cocktail = craft(mc, "mana_cocktail_change_brew", new ItemStack(ExtraBotanyItems.manaDrink), flask);
		ItemStack wine = craft(mc, "infinite_wine", cocktail, new ItemStack(ExtraBotanyItems.heroMedal));
		ItemStack grenade = craft(mc, "holy_water_grenade", cocktail, new ItemStack(Items.POPPED_CHORUS_FRUIT));
		ItemStack emptyWine = wine.copy();
		((BaseBrewItem) emptyWine.getItem()).setSwigsLeft(emptyWine, 0);
		ItemStack potionLens = craft(mc, "potion_lens_change_potion", new ItemStack(ExtraBotanyItems.lensPotion), cocktail);
		ItemStack rainbow = new ItemStack(ExtraBotanyItems.lensTrace);
		LensItem.setLensRainbow(rainbow);
		ItemStack archives = new ItemStack(ExtraBotanyItems.voidArchives);
		VoidArchivesItem.setVariant(archives, InfiniteWine.INSTANCE);
		tintSamples.addAll(List.of(
				new TintSample("Cocktail / brew", cocktail, 1),
				new TintSample("Wine / full", wine, 1),
				new TintSample("Grenade / brew", grenade, 1),
				new TintSample("Wine / empty", emptyWine, 1),
				new TintSample("Push lens", new ItemStack(ExtraBotanyItems.lensPush), 0),
				new TintSample("Smelt lens", new ItemStack(ExtraBotanyItems.lensSmelt), 0),
				new TintSample("Mana lens", new ItemStack(ExtraBotanyItems.lensMana), 0),
				new TintSample("Potion lens / brew", potionLens, 0),
				new TintSample("Trace lens", new ItemStack(ExtraBotanyItems.lensTrace), 0),
				new TintSample("Rainbow lens", rainbow, 0),
				new TintSample("Gaia hammer", new ItemStack(ExtraBotanyItems.gaiaHammer), 0),
				new TintSample("Archives / wine", archives, 1)));
	}

	private static boolean modelHasTint(Minecraft mc, ItemStack stack, int tint) {
		var model = mc.getItemRenderer().getModel(stack, mc.level, mc.player, 0);
		check(model != mc.getModelManager().getMissingModel(), "tint_preview_model_exists_" + stack.getItem());
		for (var pass : model.getRenderPasses(stack, false)) {
			if (pass.getQuads(null, null, RandomSource.create(0)).stream().anyMatch(quad -> quad.getTintIndex() == tint)) return true;
			for (Direction face : Direction.values()) {
				if (pass.getQuads(null, face, RandomSource.create(0)).stream().anyMatch(quad -> quad.getTintIndex() == tint)) return true;
			}
		}
		return false;
	}

	private static void verifyItemTints(Minecraft mc) {
		check(modelHasTint(mc, tintSamples.get(1).stack(), 1), "full_wine_model_has_liquid_layer");
		check(!modelHasTint(mc, tintSamples.get(3).stack(), 1), "empty_wine_model_has_no_liquid_layer");
		List<TintSample> samples = new ArrayList<>(tintSamples);
		ItemStack defaultCocktail = ManaCocktailItem.getDefaultCocktail();
		samples.add(new TintSample("Default cocktail", defaultCocktail, 1));
		samples.add(new TintSample("Default wine", craft(mc, "infinite_wine", defaultCocktail, new ItemStack(ExtraBotanyItems.heroMedal)), 1));
		samples.add(new TintSample("Fallback wine", new ItemStack(ExtraBotanyItems.infiniteWine), 1));
		ItemStack dyed = new ItemStack(ExtraBotanyItems.lensPotion);
		LensItem.setLensColor(dyed, DyeColor.RED);
		samples.add(new TintSample("Red lens", dyed, 0));
		List<String> transparent = new ArrayList<>();
		for (TintSample sample : samples) {
			int color = mc.getItemColors().getColor(sample.stack(), sample.tint());
			boolean opaque = FastColor.ARGB32.alpha(color) == 255;
			checks.put("item_tint_argb_" + sample.label(), String.format("%08X", color));
			checks.put("item_tint_opaque_" + sample.label(), opaque);
			if (!opaque) transparent.add(sample.label());
			check(mc.getItemColors().getColor(sample.stack(), sample.tint() == 0 ? 1 : 0) == -1,
					"untinted_layer_unchanged_" + sample.label());
		}
		check(transparent.isEmpty(), "registered_item_tints_must_be_opaque: " + transparent);
	}

	private static final class TintPreviewScreen extends Screen {
		private TintPreviewScreen() {
			super(Component.literal("ExtraBotany item tint regression"));
		}

		@Override
		public boolean isPauseScreen() {
			return false;
		}

		@Override
		public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
			graphics.fill(0, 0, width, height, 0xFF18212B);
			graphics.drawCenteredString(font, title, width / 2, 10, 0xFFFFFFFF);
			int cellWidth = (width - 24) / 4;
			int cellHeight = (height - 42) / 3;
			for (int i = 0; i < tintSamples.size(); i++) {
				TintSample sample = tintSamples.get(i);
				int x = 12 + (i % 4) * cellWidth;
				int y = 30 + (i / 4) * cellHeight;
				graphics.fill(x + 2, y + 2, x + cellWidth - 2, y + cellHeight - 2, 0xFF2A3643);
				int scale = Math.max(1, Math.min(3, (cellHeight - 20) / 16));
				graphics.pose().pushPose();
				graphics.pose().translate(x + (cellWidth - 16 * scale) / 2F, y + 5, 0);
				graphics.pose().scale(scale, scale, 1);
				graphics.renderItem(sample.stack(), 0, 0);
				graphics.pose().popPose();
				graphics.drawCenteredString(font, sample.label(), x + cellWidth / 2, y + cellHeight - 14, 0xFFFFFFFF);
			}
		}
	}

	private static void verifyRecipes(Minecraft mc) {
		var manager = EmiApi.getRecipeManager();
		var sources = mc.level.getRecipeManager().getAllRecipesFor(ExtraBotanyRecipeTypes.STONESIA_RECIPE_TYPE).stream()
				.filter(r -> !r.value().getInput().getDisplayedStacks().isEmpty()).toList();
		var shown = manager.getRecipes(EmiExtrabotanyPlugin.STONESIA);
		check(net.minecraft.client.resources.language.I18n.exists("emi.category.extrabotany.stonesia"), "stonesia_category_name_is_translated");
		check(!sources.isEmpty() && shown.size() == sources.size(), "stonesia_category_has_every_displayable_recipe");
		for (var source : sources) {
			var shownRecipe = manager.getRecipe(source.id());
			check(shownRecipe instanceof StonesiaEmiRecipe stone && stone.getManaOutput() == source.value().getManaOutput()
					&& !stone.getInputs().getFirst().getEmiStacks().isEmpty(), "stonesia_" + source.id());
		}
		pages.add(shown.getFirst());
		int brewRecipes = 0;
		for (var brew : BotaniaAPI.instance().getBrewRegistry()) {
			if (brew == BotaniaBrews.FALLBACK) continue;
			var id = BotaniaAPI.instance().getBrewRegistry().getKey(brew);
			for (String path : List.of("mana_cocktail_change_brew", "infinite_wine", "infinite_wine_change_brew", "holy_water_grenade")) {
				if (path.equals("mana_cocktail_change_brew") && brew == ExtraBotanyBrews.manaCocktail) continue;
				var display = manager.getRecipe(prefix("/emi/" + path + "/" + id.getNamespace() + "/" + id.getPath()));
				check(display != null, "registered_" + path + "_" + id);
				var inputs = display.getInputs().stream().map(i -> i.getEmiStacks().getFirst().getItemStack()).toList();
				var input = CraftingInput.of(2, 1, inputs);
				var source = (CraftingRecipe) mc.level.getRecipeManager().byKey(prefix(path)).orElseThrow().value();
				var actual = display.getOutputs().getFirst().getItemStack();
				check(source.matches(input, mc.level) && ItemStack.matches(source.assemble(input, mc.level.registryAccess()), actual)
						&& BrewUtil.getBrew(actual) == brew, "components_match_real_recipe_" + path + "_" + id);
				var remainders = source.getRemainingItems(input);
				for (int slot = 0; slot < input.size(); slot++) {
					check(ItemStack.matches(remainders.get(slot), display.getInputs().get(slot).getEmiStacks().getFirst().getRemainder().getItemStack()),
							"remainder_" + slot + "_" + path + "_" + id);
				}
				check(manager.getRecipesByOutput(EmiStack.of(actual)).stream().anyMatch(r -> display.getId().equals(r.getId())),
						"output_lookup_" + path + "_" + id);
				if (pages.size() < 4 && !path.equals("infinite_wine_change_brew")) pages.add(display);
				brewRecipes++;
			}
		}
		check(brewRecipes > 8, "multiple_brews_have_distinct_component_outputs");
		checks.put("brew_recipe_count", brewRecipes);
		checks.put("stonesia_recipe_count", shown.size());
	}

	private static void check(boolean success, String label) {
		if (!success) throw new AssertionError(label);
		checks.put(label, true);
	}

	private static void finish(Minecraft mc, Throwable error) {
		stage = 99;
		try {
			var output = mc.gameDirectory.toPath().resolve("verification");
			Files.createDirectories(output);
			Files.writeString(output.resolve("result.json"), new GsonBuilder().setPrettyPrinting().create().toJson(
					Map.of("status", error == null ? "passed" : "failed", "checks", checks, "error", error == null ? "" : error.toString())));
		} catch (Exception saveError) {
			saveError.printStackTrace();
		}
		if (error != null) error.printStackTrace();
		if (mc.getSingleplayerServer() != null) mc.getSingleplayerServer().halt(true);
		mc.disconnect();
		mc.stop();
	}
}
