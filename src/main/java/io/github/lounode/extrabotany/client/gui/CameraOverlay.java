package io.github.lounode.extrabotany.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 文文的相机的取景框。玩家用第一人称视角举着相机时，它盖在画面上。
 * The frame of the Camera. It covers the view while the player holds up the Camera in the first-person view.
 *
 * <p>画法和原版游戏的望远镜相同：一张正方形的贴图，放在画面中间，按 {@code scale} 缩放。
 * <p>The method is the same as for the vanilla spyglass: one square texture at the center of the view, with the size multiplied by {@code scale}.
 */
public final class CameraOverlay {
	private static final ResourceLocation TEXTURE = prefix("textures/gui/spyglass_scope.png");

	private CameraOverlay() {}

	public static void render(GuiGraphics gui, float scale) {
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();

		float shortSide = (float) Math.min(gui.guiWidth(), gui.guiHeight());
		float factor = Math.min((float) gui.guiWidth() / shortSide, (float) gui.guiHeight() / shortSide) * scale;
		int size = Mth.floor(shortSide * factor);
		int left = (gui.guiWidth() - size) / 2;
		int top = (gui.guiHeight() - size) / 2;
		gui.blit(TEXTURE, left, top, -90, 0.0F, 0.0F, size, size, size, size);

		RenderSystem.disableBlend();
	}
}
