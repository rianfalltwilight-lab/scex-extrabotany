package io.github.lounode.extrabotany.client.gui;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;

import io.github.lounode.extrabotany.common.attachment.FlamescionState;
import io.github.lounode.extrabotany.common.entity.mount.MotorEntity;
import io.github.lounode.extrabotany.common.handler.FlamescionHandler;
import io.github.lounode.extrabotany.common.handler.HerrscherHandler;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 快捷栏上方的能量条。有多条时，它们从下往上依次排列。
 * The energy bars above the hotbar. When there is more than one bar, they are put one above the other from the bottom up.
 *
 * <ul>
 * <li>雷之律者的能量：佩戴征服宝石时显示。充满时显示高亮的条。
 * The energy of the Herrscher of Thunder: shown while the Gem of Conquest is worn. A full bar is highlighted.</li>
 * <li>机车的构造能量：骑着机车时显示。
 * The tectonic energy of the motor: shown while the player rides a motor.</li>
 * <li>薪炎的能量：主手拿着薪炎之钥并且没有骑乘时显示。过载时填充的颜色不同。
 * The energy of the Flamescion: shown while the Key of Flamescion is in the main hand and the player rides nothing. The fill has a different color during an overload.</li>
 * </ul>
 *
 * <p>来源：ExtraMeteorP 1.16 {@code HUDHandler}、{@code HerrscherGUI}、{@code MotorGUI} 和 {@code FlamescionGUI}。
 * <p>Source: ExtraMeteorP 1.16 {@code HUDHandler}, {@code HerrscherGUI}, {@code MotorGUI} and {@code FlamescionGUI}.
 */
public final class EnergyBarsHud {
	private static final ResourceLocation HERRSCHER_TEXTURE = prefix("textures/gui/herrscherhud.png");
	private static final ResourceLocation MOTOR_TEXTURE = prefix("textures/gui/motorhud.png");
	private static final ResourceLocation FLAMESCION_TEXTURE = prefix("textures/gui/flamescionhud.png");
	private static final int BAR_WIDTH = 64;
	private static final int BAR_HEIGHT = 6;
	private static final int BOTTOM_OFFSET = 56;
	private static final int BAR_SPACING = 7;
	// 贴图里三行的纵坐标：底板、填充、充满时的高亮条。
	// The vertical coordinates of the three rows in the texture: the background, the fill, and the highlight for a full bar.
	private static final int V_BACKGROUND = 0;
	private static final int V_FILL = 6;
	private static final int V_FULL = 11;
	// 薪炎贴图里过载时的填充行。
	// The fill row for an overload in the Flamescion texture.
	private static final int V_OVERLOADED = 12;

	private EnergyBarsHud() {}

	public static void render(GuiGraphics gui, DeltaTracker deltaTracker) {
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		if (player == null || minecraft.options.hideGui) {
			return;
		}
		int x = gui.guiWidth() / 2 - BAR_WIDTH / 2;
		int y = gui.guiHeight() - BOTTOM_OFFSET;

		if (HerrscherHandler.isHerrscherOfThunder(player)) {
			int energy = HerrscherHandler.getEnergy(player);
			gui.blit(HERRSCHER_TEXTURE, x, y, 0, V_BACKGROUND, BAR_WIDTH, BAR_HEIGHT);
			if (energy >= HerrscherHandler.MAX_ENERGY) {
				gui.blit(HERRSCHER_TEXTURE, x, y, 0, V_FULL, BAR_WIDTH, BAR_HEIGHT);
			} else {
				gui.blit(HERRSCHER_TEXTURE, x, y, 0, V_FILL, BAR_WIDTH * energy / HerrscherHandler.MAX_ENERGY, BAR_HEIGHT);
			}
			y -= BAR_SPACING;
		}

		if (player.getVehicle() instanceof MotorEntity motor) {
			gui.blit(MOTOR_TEXTURE, x, y, 0, V_BACKGROUND, BAR_WIDTH, BAR_HEIGHT);
			gui.blit(MOTOR_TEXTURE, x, y, 0, V_FILL, BAR_WIDTH * motor.getEnergy() / MotorEntity.MAX_ENERGY, BAR_HEIGHT);
			y -= BAR_SPACING;
		}

		if (!player.isPassenger() && !player.isSpectator() && player.getMainHandItem().is(ExtraBotanyItems.flamescionWeapon)) {
			FlamescionState state = FlamescionHandler.getState(player);
			int v = state.isOverloaded() ? V_OVERLOADED : V_FILL;
			gui.blit(FLAMESCION_TEXTURE, x, y, 0, V_BACKGROUND, BAR_WIDTH, BAR_HEIGHT);
			gui.blit(FLAMESCION_TEXTURE, x, y, 0, v, BAR_WIDTH * state.getEnergy() / FlamescionHandler.MAX_ENERGY, BAR_HEIGHT);
		}
	}
}
