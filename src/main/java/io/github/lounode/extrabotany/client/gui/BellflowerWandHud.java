package io.github.lounode.extrabotany.client.gui;

import com.mojang.blaze3d.platform.Window;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import vazkii.botania.api.block_entity.BindableSpecialFlowerBlockEntity;

import io.github.lounode.extrabotany.common.block.flower.generating.BellflowerBlockEntity;

/**
 * 玩家拿着森林法杖看风铃草时显示的界面：魔力条和风力。
 * The display that a player sees when the player looks at a Bellflower with the Wand of the Forest: the mana bar and the wind level.
 *
 * <p>风力来自服务端同步的数值。花快要被扇水仙吹死时，风力显示为 999，文字变成深红色。
 * <p>The wind level comes from the value that the server synchronized. When a Daffomill is about to kill the flower, the wind level shows as 999 and the text becomes dark red.
 */
public class BellflowerWandHud extends BindableSpecialFlowerBlockEntity.BindableFlowerWandHud<BellflowerBlockEntity> {
	private static final double DYING_WIND_LEVEL = 999;

	public BellflowerWandHud(BellflowerBlockEntity flower) {
		super(flower);
	}

	@Override
	public void renderHUD(GuiGraphics gui, Window window, Font font, float partialTick) {
		boolean dying = flower.isDying();
		double windLevel = dying ? DYING_WIND_LEVEL : flower.getWindLevel();

		Component component = Component.translatable(
				"message.extrabotany.chat.wind_level",
				String.format("%.2f", windLevel)
		);
		int halfWidth = (font.width(component) + 24) / 2;
		int centerX = window.getGuiScaledWidth() / 2;
		int centerY = window.getGuiScaledHeight() / 2;

		super.renderHUD(gui, window, font, halfWidth + 2, halfWidth + 2, 48);
		gui.drawString(font, component, centerX - halfWidth + 12, centerY + 34, dying ? ChatFormatting.DARK_RED.getColor() : 0xFFFFFF);
	}
}
