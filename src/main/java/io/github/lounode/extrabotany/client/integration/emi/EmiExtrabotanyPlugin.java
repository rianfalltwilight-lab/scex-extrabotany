package io.github.lounode.extrabotany.client.integration.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.render.EmiRenderable;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiIngredient;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;

import org.apache.commons.lang3.StringUtils;

import io.github.lounode.extrabotany.api.recipe.ManaOutputRecipe;
import io.github.lounode.extrabotany.api.recipe.PedestalRecipe;
import io.github.lounode.extrabotany.client.integration.emi.recipe.PedestalEmiRecipe;
import io.github.lounode.extrabotany.client.integration.emi.recipe.StonesiaEmiRecipe;
import io.github.lounode.extrabotany.common.block.ExtraBotanyBlocks;
import io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks;
import io.github.lounode.extrabotany.common.crafting.ExtraBotanyRecipeTypes;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.item.brew.ManaCocktailItem;
import io.github.lounode.extrabotany.common.brew.BrewUtil;
import io.github.lounode.extrabotany.common.brew.ExtraBotanyBrews;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.common.brew.BotaniaBrews;
import vazkii.botania.common.item.BotaniaItems;

import java.util.Comparator;
import java.util.List;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * EMI 集成：祭坛、石中姬及按实际酿造组件展示的合成配方。
 * EMI integration for the pedestal, Stonesia and component-aware brew crafting recipes.
 */
@EmiEntrypoint
public class EmiExtrabotanyPlugin implements EmiPlugin {

	private static final Comparator<EmiRecipe> BY_ID = Comparator.comparing(
			EmiRecipe::getId,
			Comparator.nullsLast(Comparator.naturalOrder())
	);
	private static final Comparator<EmiRecipe> BY_GROUP =
			Comparator.comparing(emiRecipe -> emiRecipe instanceof ExtrabotanyEmiRecipe ber ? ber.getGroup() : "");
	private static final Comparator<EmiRecipe> BY_CATALYST =
			Comparator.comparing(emiRecipe -> emiRecipe.getCatalysts()
					.stream()
					.flatMap(emiIngredient -> emiIngredient.getEmiStacks().stream())
					.map(emiStack -> emiStack.getId().toString())
					.filter(StringUtils::isNotEmpty)
					.findFirst()
					.orElse(""));

	private static final Comparator<EmiRecipe> BY_SMASH_TOOLS = (l, r) -> {
		if (l instanceof PedestalEmiRecipe leftRecipe && r instanceof PedestalEmiRecipe rightRecipe) {
			PedestalRecipe lp = leftRecipe.getRecipe();
			PedestalRecipe rp = rightRecipe.getRecipe();
			Ingredient left = lp.getSmashTools();
			Ingredient right = rp.getSmashTools();
			return left.toString().compareTo(right.toString());
		}

		return 0;
	};

	private static final Comparator<EmiRecipe> BY_MANA_OUTPUT = Comparator.<EmiRecipe, Integer>comparing(
			emiRecipe -> emiRecipe instanceof ManaOutputRecipe recipe ? recipe.getManaOutput() : 0).reversed();

	//Category
	public static final EmiRecipeCategory PEDESTAL_SMASH = createCategory("pedestal_smash",
			EmiStack.of(ExtraBotanyBlocks.livingrockPedestal), BY_SMASH_TOOLS.thenComparing(BY_GROUP).thenComparing(BY_ID));
	public static final EmiRecipeCategory STONESIA = createCategory("stonesia",
			EmiStack.of(ExtrabotanyFlowerBlocks.stonesia), BY_MANA_OUTPUT.thenComparing(BY_GROUP).thenComparing(BY_ID));

	@Override
	public void register(EmiRegistry registry) {
		registry.addCategory(PEDESTAL_SMASH);
		registry.addCategory(STONESIA);

		for (var pedestal : ExtraBotanyBlocks.ALL_PEDESTALS) {
			registry.addWorkstation(PEDESTAL_SMASH, EmiStack.of(pedestal));
		}

		registry.addWorkstation(STONESIA, EmiStack.of(ExtrabotanyFlowerBlocks.stonesia));
		registry.addWorkstation(STONESIA, EmiStack.of(ExtrabotanyFlowerBlocks.stonesiaFloating));

		registry.setDefaultComparison(ExtraBotanyItems.manaCocktail, Comparison.compareComponents());
		registry.setDefaultComparison(ExtraBotanyItems.infiniteWine, Comparison.compareComponents());
		registry.setDefaultComparison(ExtraBotanyItems.holyWaterGrenade, Comparison.compareComponents());

		//Add Recipe
		for (var recipe : registry.getRecipeManager().getAllRecipesFor(ExtraBotanyRecipeTypes.PEDESTAL_SMASH_TYPE)) {
			registry.addRecipe(new PedestalEmiRecipe(recipe));
		}
		for (var recipe : registry.getRecipeManager().getAllRecipesFor(ExtraBotanyRecipeTypes.STONESIA_RECIPE_TYPE)) {
			if (!recipe.value().getInput().getDisplayedStacks().isEmpty()) {
				registry.addRecipe(new StonesiaEmiRecipe(recipe));
			}
		}
		registerBrewRecipes(registry);
	}

	private static void registerBrewRecipes(EmiRegistry registry) {
		for (var brew : BotaniaAPI.instance().getBrewRegistry()) {
			if (brew == BotaniaBrews.FALLBACK) {
				continue;
			}
			ResourceLocation brewId = BotaniaAPI.instance().getBrewRegistry().getKey(brew);
			ItemStack cocktail = brew == ExtraBotanyBrews.manaCocktail
					? ManaCocktailItem.getDefaultCocktail() : ExtraBotanyItems.manaCocktail.getDefaultInstance();
			BrewUtil.setBrew(cocktail, brew);
			if (brew != ExtraBotanyBrews.manaCocktail) {
				ItemStack flask = BotaniaItems.BREW_FLASK.getDefaultInstance();
				BrewUtil.setBrew(flask, brew);
				addBrewRecipe(registry, "mana_cocktail_change_brew", brewId, ManaCocktailItem.getDefaultCocktail(), flask);
			}
			addBrewRecipe(registry, "infinite_wine", brewId, cocktail, ExtraBotanyItems.heroMedal.getDefaultInstance());
			addBrewRecipe(registry, "infinite_wine_change_brew", brewId, cocktail, ExtraBotanyItems.infiniteWine.getDefaultInstance());
			addBrewRecipe(registry, "holy_water_grenade", brewId, cocktail, Items.POPPED_CHORUS_FRUIT.getDefaultInstance());
		}
	}

	private static void addBrewRecipe(EmiRegistry registry, String recipePath, ResourceLocation brewId, ItemStack first, ItemStack second) {
		var level = Minecraft.getInstance().level;
		var holder = registry.getRecipeManager().byKey(prefix(recipePath)).orElse(null);
		if (level == null || holder == null || !(holder.value() instanceof CraftingRecipe recipe)) {
			return;
		}
		CraftingInput input = CraftingInput.of(2, 1, List.of(first, second));
		if (!recipe.matches(input, level)) {
			return;
		}
		// Use the real recipe for outputs/components so data reloads and brew rules remain authoritative.
		ItemStack output = recipe.assemble(input, level.registryAccess());
		if (!output.isEmpty()) {
			var remainders = recipe.getRemainingItems(input);
			registry.addRecipe(new EmiCraftingRecipe(List.<EmiIngredient>of(
					EmiStack.of(first).setRemainder(EmiStack.of(remainders.get(0))),
					EmiStack.of(second).setRemainder(EmiStack.of(remainders.get(1)))),
					EmiStack.of(output), prefix("/emi/" + recipePath + "/" + brewId.getNamespace() + "/" + brewId.getPath()), true));
		}
	}

	private static EmiRecipeCategory createCategory(String idPath, EmiRenderable icon, Comparator<EmiRecipe> comp) {
		return new EmiRecipeCategory(prefix(idPath), icon, icon, comp);
	}
}
