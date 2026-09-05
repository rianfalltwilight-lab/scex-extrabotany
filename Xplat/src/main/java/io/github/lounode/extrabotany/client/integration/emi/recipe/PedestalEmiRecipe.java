package io.github.lounode.extrabotany.client.integration.emi.recipe;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;

import vazkii.botania.client.integration.emi.BlendTextureWidget;

import io.github.lounode.extrabotany.api.recipe.PedestalRecipe;
import io.github.lounode.extrabotany.client.integration.emi.EmiExtrabotanyPlugin;
import io.github.lounode.extrabotany.client.integration.emi.ExtrabotanyEmiRecipe;
import io.github.lounode.extrabotany.common.lib.ExtraBotanyTags;

import java.util.List;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefixBotania;

public class PedestalEmiRecipe extends ExtrabotanyEmiRecipe {

	private static final ResourceLocation TEXTURE = prefixBotania("textures/gui/pure_daisy_overlay.png");
	private static final EmiIngredient PEDESTAL = EmiIngredient.of(ExtraBotanyTags.Blocks.PEDESTALS);

	protected EmiIngredient hammers;
	private final PedestalRecipe recipe;

	public PedestalEmiRecipe(RecipeHolder<PedestalRecipe> holder) {
		super(EmiExtrabotanyPlugin.PEDESTAL_SMASH, holder);
		this.recipe = holder.value();

		this.input = List.of(EmiIngredient.of(recipe.getInput()));
		this.output = List.of(EmiStack.of(recipe.getResultItem(getRegistryAccess())));
		this.hammers = EmiIngredient.of(recipe.getSmashTools());
	}

	public PedestalRecipe getRecipe() {
		return recipe;
	}

	@Override
	public int getDisplayWidth() {
		return 142;
	}

	@Override
	public int getDisplayHeight() {
		return 58;
	}

	@Override
	public void addWidgets(WidgetHolder widgets) {
		addPedestalWidgets(widgets, this, input.get(0), PEDESTAL, output.get(0), hammers);
	}

	public static void addPedestalWidgets(WidgetHolder widgets, EmiRecipe recipe,
			EmiIngredient input, EmiIngredient pedestal, EmiStack output, EmiIngredient hammers) {
		widgets.add(new BlendTextureWidget(TEXTURE, 40, -2, 64, 46, 0, 0));
		widgets.addSlot(input, 32, 13).drawBack(false);
		widgets.addSlot(output, 93, 13).drawBack(false).recipeContext(recipe);
		widgets.addSlot(pedestal, 62, 13).catalyst(true).drawBack(false);
		widgets.addSlot(hammers, 62, 43).catalyst(true).drawBack(false);
	}
}
