package io.github.lounode.extrabotany.client.gui;

import com.mojang.blaze3d.platform.Window;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import vazkii.botania.api.BotaniaAPIClient;
import vazkii.botania.api.block_entity.BindableSpecialFlowerBlockEntity;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.client.gui.HUDHandler;

import io.github.lounode.extrabotany.common.block.flower.functional.StardustLotusBlockEntity;

/**
 * 玩家拿着森林法杖看星尘莲时显示的界面。
 * The display that a player sees when the player looks at a Stardust Lotus with the Wand of the Forest.
 *
 * <p>魔力条显示传送门的进度：已经消耗的魔力和总花费。下面一行显示传送目标。
 * <p>The mana bar shows the progress of the portal: the mana that the flower used and the full cost. The line below shows the teleport target.
 */
public class StardustLotusWandHud extends BindableSpecialFlowerBlockEntity.BindableFlowerWandHud<StardustLotusBlockEntity> {
	public StardustLotusWandHud(StardustLotusBlockEntity flower) {
		super(flower);
	}

	@Override
	public void renderHUD(GuiGraphics gui, Window window, Font font, int minLeft, int minRight, int minDown) {
		String name = I18n.get(flower.getBlockState().getBlock().getDescriptionId());
		int centerX = window.getGuiScaledWidth() / 2;
		int centerY = window.getGuiScaledHeight() / 2;
		int halfWidth = (Math.max(102, font.width(name)) + 4) / 2;
		RenderHelper.renderHUDBox(gui, centerX - Math.max(halfWidth, minLeft), centerY + 8,
				centerX + Math.max(halfWidth + 20, minRight), centerY + Math.max(30, minDown + 52));

		BotaniaAPIClient.instance().drawComplexManaHUD(gui, window, font, flower.getColor(),
				flower.getConsumedMana(), flower.getTeleportCost(), name, flower.getHudIcon(), flower.isValidBinding());
		RenderHelper.drawTexturedModalRect(gui, HUDHandler.manaBar, centerX - 11, centerY + 34, 0, 38, 22, 15);

		BlockPos target = flower.getTarget();
		Component text = target == null
				? Component.translatable("message.extrabotany.stardust_lotus.no_target")
				: Component.translatable("message.extrabotany.stardust_lotus.target", target.getX(), target.getY(), target.getZ());
		gui.drawString(font, text, centerX - font.width(text) / 2, centerY + 51, 0xFFFFFF);
	}
}
