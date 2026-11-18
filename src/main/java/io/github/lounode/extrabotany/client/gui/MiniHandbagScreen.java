package io.github.lounode.extrabotany.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import io.github.lounode.extrabotany.common.item.handbag.MiniHandbagMenu;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 旅行手袋的界面。贴图来自 1.12 的 {@code textures/gui/handbag.png}。和 1.12 一样不画标题。
 * The screen of the Mini Handbag. The texture comes from {@code textures/gui/handbag.png} of 1.12. As in 1.12, no title is drawn.
 */
public class MiniHandbagScreen extends AbstractContainerScreen<MiniHandbagMenu> {
	private static final ResourceLocation TEXTURE = prefix("textures/gui/handbag.png");

	public MiniHandbagScreen(MiniHandbagMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		super.render(graphics, mouseX, mouseY, partialTicks);
		renderTooltip(graphics, mouseX, mouseY);
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
		graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
	}
}
