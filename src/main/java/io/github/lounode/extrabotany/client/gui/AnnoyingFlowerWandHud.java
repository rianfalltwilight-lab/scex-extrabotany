package io.github.lounode.extrabotany.client.gui;

import com.mojang.blaze3d.platform.Window;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import vazkii.botania.api.block_entity.BindableSpecialFlowerBlockEntity;

import io.github.lounode.extrabotany.common.block.flower.functional.AnnoyingFlowerBlockEntity;

/**
 * 玩家拿着森林法杖看神烦花时显示的界面：魔力条，以及还剩几次使用宝藏战利品表的钓鱼。
 * The display that a player sees when the player looks at an Annoyingflower with the Wand of the Forest: the mana bar, and the number of catches that still use the treasure loot table.
 */
public class AnnoyingFlowerWandHud extends BindableSpecialFlowerBlockEntity.BindableFlowerWandHud<AnnoyingFlowerBlockEntity> {
	public AnnoyingFlowerWandHud(AnnoyingFlowerBlockEntity flower) {
		super(flower);
	}

	@Override
	public void renderHUD(GuiGraphics gui, Window window, Font font, float partialTick) {
		Component component = Component.translatable("message.extrabotany.gui.food_level", flower.getBoostLeft());
		int halfWidth = (font.width(component) + 24) / 2;
		int centerX = window.getGuiScaledWidth() / 2;
		int centerY = window.getGuiScaledHeight() / 2;

		super.renderHUD(gui, window, font, halfWidth + 2, halfWidth + 2, 48);
		gui.drawString(font, component, centerX - halfWidth + 12, centerY + 34, 0xFFFFFF);
	}
}
