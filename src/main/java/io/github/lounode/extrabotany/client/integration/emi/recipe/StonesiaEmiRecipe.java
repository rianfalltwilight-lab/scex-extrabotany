package io.github.lounode.extrabotany.client.integration.emi.recipe;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import io.github.lounode.extrabotany.api.recipe.ManaOutputRecipe;
import io.github.lounode.extrabotany.api.recipe.StonesiaRecipe;
import io.github.lounode.extrabotany.client.integration.emi.EmiExtrabotanyPlugin;
import io.github.lounode.extrabotany.client.integration.emi.ExtrabotanyEmiRecipe;
import io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

public final class StonesiaEmiRecipe extends ExtrabotanyEmiRecipe implements ManaOutputRecipe {

	private final StonesiaRecipe recipe;

	public StonesiaEmiRecipe(RecipeHolder<StonesiaRecipe> holder) {
		super(EmiExtrabotanyPlugin.STONESIA, holder);
		recipe = holder.value();
		input = List.of(EmiIngredient.of(recipe.getInput().getDisplayedStacks().stream().map(EmiStack::of).toList()));
	}

	@Override
	public int getManaOutput() { return recipe.getManaOutput(); }

	@Override
	public int getDisplayWidth() { return 142; }

	@Override
	public int getDisplayHeight() { return 44; }

	@Override
	public boolean supportsRecipeTree() { return false; }

	@Override
	public void addWidgets(WidgetHolder widgets) {
		widgets.addSlot(input.getFirst(), 8, 13);
		widgets.addSlot(EmiStack.of(ExtrabotanyFlowerBlocks.stonesia), 34, 13).catalyst(true);
		widgets.addText(Component.translatable("extrabotany.jei.stonesia.tip_mana_output", getManaOutput()), 58, 18, 0xFF808080, false);
	}
}
