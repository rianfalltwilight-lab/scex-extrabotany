package io.github.lounode.extrabotany.client.integration.jei;

import com.mojang.blaze3d.systems.RenderSystem;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import io.github.lounode.extrabotany.api.recipe.PedestalRecipe;
import io.github.lounode.extrabotany.common.block.ExtraBotanyBlocks;
import io.github.lounode.extrabotany.common.lib.LibMisc;

public class PedestalRecipeCategory implements IRecipeCategory<PedestalRecipe> {
	public static final RecipeType<PedestalRecipe> TYPE =
			RecipeType.create(LibMisc.MOD_ID, "pedestal_smash", PedestalRecipe.class);
	private final Component localizedName;
	private final IDrawable background;
	private final IDrawable overlay;
	private final IDrawable icon;
	private final ItemStack renderStack = new ItemStack(ExtraBotanyBlocks.livingrockPedestal);

	public PedestalRecipeCategory(IGuiHelper guiHelper) {
		this.localizedName = Component.translatable("extrabotany.jei.pedestal");
		this.background = guiHelper.createBlankDrawable(142, 60);
		this.overlay = guiHelper.createDrawable(ResourceLocation.tryBuild("botania", "textures/gui/pure_daisy_overlay.png"),
				0, 0, 64, 46);
		this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, renderStack.copy());
	}

	@Override
	public RecipeType<PedestalRecipe> getRecipeType() {
		return TYPE;
	}

	@Override
	public Component getTitle() {
		return localizedName;
	}

	@Override
	@SuppressWarnings("removal")
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public int getWidth() {
		return 142;
	}

	@Override
	public int getHeight() {
		return 60;
	}

	@Override
	public @Nullable IDrawable getIcon() {
		return icon;
	}

	@Override
	public void draw(PedestalRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics gui, double mouseX, double mouseY) {
		background.draw(gui, 0, 0);
		final int strikes = recipe.getStrike();
		final int exp = recipe.getExp();
		Component strikesComp = Component.translatable("extrabotany.jei.pedestal.tip_strikes", strikes);
		Component expComp = Component.translatable("extrabotany.jei.pedestal.tip_exp", exp);

		Font font = Minecraft.getInstance().font;
		gui.drawString(font, strikesComp, 90, 50, 0x888888, false);
		gui.drawString(font, expComp, 90, 3, 0x888888, false);

		RenderSystem.enableBlend();

		overlay.draw(gui, 40, 0);

		RenderSystem.disableBlend();
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, PedestalRecipe recipe, IFocusGroup focusGroup) {
		builder.addSlot(RecipeIngredientRole.INPUT, 32, 12)
				.addIngredients(recipe.getInput());
		builder.addSlot(RecipeIngredientRole.OUTPUT, 93, 12)
				.addItemStack(recipe.getResultItem(RegistryAccess.EMPTY));
		builder.addSlot(RecipeIngredientRole.CATALYST, 62, 12)
				.addItemStack(renderStack);
		builder.addSlot(RecipeIngredientRole.CATALYST, 62, 45)
				.addIngredients(recipe.getSmashTools());

	}
}
