package io.github.lounode.extrabotany.client.gui;

import com.mojang.blaze3d.platform.Window;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import vazkii.botania.api.BotaniaAPIClient;
import vazkii.botania.api.block.WandHUD;
import vazkii.botania.client.core.helper.RenderHelper;

import io.github.lounode.extrabotany.common.block.block_entity.ManaMachineBlockEntity;

/**
 * 玩家拿着森林法杖看魔力机器时显示的界面：机器的名字、魔力条，以及机器自己的一行说明（如果有）。
 * The display that a player sees when the player looks at a mana machine with the Wand of the Forest: the name of the machine, the mana bar, and one line of detail from the machine (if there is one).
 *
 * <p>原版用 WAILA 和 The One Probe 显示这些数值。这里用植物魔法自己的法杖界面。
 * <p>The original shows these values with WAILA and The One Probe. This port uses the wand display of Botania itself.
 */
public class ManaMachineWandHud implements WandHUD {
	private static final int MANA_COLOR = 0x0095FF;

	private final ManaMachineBlockEntity machine;

	public ManaMachineWandHud(ManaMachineBlockEntity machine) {
		this.machine = machine;
	}

	@Override
	public void renderHUD(GuiGraphics gui, Window window, Font font, float partialTick) {
		String name = machine.getBlockState().getBlock().getName().getString();
		Component detail = machine.getHudDetail();
		int centerX = window.getGuiScaledWidth() / 2;
		int centerY = window.getGuiScaledHeight() / 2;
		int detailWidth = detail == null ? 0 : font.width(detail);
		int halfWidth = (Math.max(102, Math.max(font.width(name), detailWidth)) + 4) / 2;

		RenderHelper.renderHUDBox(gui, centerX - halfWidth, centerY + 8, centerX + halfWidth, centerY + (detail == null ? 30 : 42));
		BotaniaAPIClient.instance().drawSimpleManaHUD(gui, window, font, MANA_COLOR, machine.getCurrentMana(), machine.getMaxMana(), name);
		if (detail != null) {
			gui.drawString(font, detail, centerX - detailWidth / 2, centerY + 30, 0xFFFFFF);
		}
	}
}
