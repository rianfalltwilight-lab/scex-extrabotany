package io.github.lounode.extrabotany.client.gui;

import com.mojang.blaze3d.platform.Window;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;

import vazkii.botania.api.block_entity.BindableSpecialFlowerBlockEntity;

import io.github.lounode.extrabotany.common.block.flower.functional.ManalinkBlockEntity;

/**
 * 玩家拿着森林法杖看魔链星时显示的界面：魔力条，以及已绑定的魔力池的维度和坐标。
 * The display that a player sees when the player looks at a Manalink with the Wand of the Forest: the mana bar, and the dimension and the position of the bound mana pool.
 */
public class ManalinkWandHud extends BindableSpecialFlowerBlockEntity.BindableFlowerWandHud<ManalinkBlockEntity> {
	private static final float TEXT_SCALE = 0.8F;

	public ManalinkWandHud(ManalinkBlockEntity flower) {
		super(flower);
	}

	@Override
	public void renderHUD(GuiGraphics gui, Window window, Font font, int minLeft, int minRight, int minDown) {
		GlobalPos pos = flower.getLinkPos().orElse(null);
		if (pos == null) {
			super.renderHUD(gui, window, font, minLeft, minRight, minDown);
			return;
		}

		Component levelComponent = Component.translatable("message.extrabotany.actionbar.bind_to_pos",
				pos.dimension().location() + String.format(" %d, %d, %d", pos.pos().getX(), pos.pos().getY(), pos.pos().getZ()));

		int halfWidth = (int) (((float) (font.width(levelComponent) + 24) / 2) * TEXT_SCALE);
		int centerX = window.getGuiScaledWidth() / 2;
		int centerY = window.getGuiScaledHeight() / 2;

		super.renderHUD(gui, window, font, halfWidth + 2, halfWidth + 2, 48);

		gui.pose().pushPose();
		gui.pose().scale(TEXT_SCALE, TEXT_SCALE, 1.0F);
		gui.drawString(font, levelComponent, (int) ((centerX - halfWidth + 12) / TEXT_SCALE), (int) ((centerY + 34) / TEXT_SCALE), ChatFormatting.GREEN.getColor());
		gui.pose().popPose();
	}
}
