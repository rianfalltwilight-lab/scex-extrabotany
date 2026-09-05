package io.github.lounode.extrabotany.client.integration.jei;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import io.github.lounode.extrabotany.api.recipe.StonesiaRecipe;
import io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks;
import io.github.lounode.extrabotany.common.lib.LibMisc;

public class StonesiaRecipeCategory implements IRecipeCategory<StonesiaRecipe> {

	public static final RecipeType<StonesiaRecipe> TYPE =
			RecipeType.create(LibMisc.MOD_ID, "stonesia", StonesiaRecipe.class);

	private final IDrawableStatic background;
	private final Component localizedName;
	private final IDrawable icon;

	public StonesiaRecipeCategory(IGuiHelper guiHelper) {
		this.localizedName = Component.translatable("extrabotany.jei.stonesia");
		this.background = guiHelper.createBlankDrawable(96, 44);
		this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ExtrabotanyFlowerBlocks.stonesia));
	}

	@NotNull
	@Override
	public Component getTitle() {
		return localizedName;
	}

	@NotNull
	@Override
	@SuppressWarnings("removal")
	public IDrawable getBackground() {
		return background;
	}

	@NotNull
	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Override
	public RecipeType<StonesiaRecipe> getRecipeType() {
		return TYPE;
	}

	@Override
	public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull StonesiaRecipe recipe, @NotNull IFocusGroup focusGroup) {
		builder.addSlot(RecipeIngredientRole.INPUT, 9, 12)
				.setStandardSlotBackground()
				.addItemStacks(recipe.getInput().getDisplayedStacks());
	}

	@Override
	public void createRecipeExtras(IRecipeExtrasBuilder builder, StonesiaRecipe recipe, IFocusGroup focuses) {
		int mana = recipe.getManaOutput();
		Component manaOutText = Component.translatable("extrabotany.jei.stonesia.tip_mana_output", mana);

		builder.addText(manaOutText, getWidth() - 25, getHeight())
				.setPosition(25, 0)
				.setTextAlignment(HorizontalAlignment.CENTER)
				.setTextAlignment(VerticalAlignment.CENTER)
				.setColor(0xFF808080);
	}
}
