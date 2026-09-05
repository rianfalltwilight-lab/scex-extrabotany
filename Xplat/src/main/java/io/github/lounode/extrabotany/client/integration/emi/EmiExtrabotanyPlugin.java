package io.github.lounode.extrabotany.client.integration.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiRenderable;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiStack;

import net.minecraft.world.item.crafting.Ingredient;

import org.apache.commons.lang3.StringUtils;

import io.github.lounode.extrabotany.api.recipe.ManaOutputRecipe;
import io.github.lounode.extrabotany.api.recipe.PedestalRecipe;
import io.github.lounode.extrabotany.client.integration.emi.recipe.PedestalEmiRecipe;
import io.github.lounode.extrabotany.common.block.ExtraBotanyBlocks;
import io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks;
import io.github.lounode.extrabotany.common.crafting.ExtraBotanyRecipeTypes;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

import java.util.Comparator;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

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
	}

	private static EmiRecipeCategory createCategory(String idPath, EmiRenderable icon, Comparator<EmiRecipe> comp) {
		return new EmiRecipeCategory(prefix(idPath), icon, icon, comp);
	}
}
