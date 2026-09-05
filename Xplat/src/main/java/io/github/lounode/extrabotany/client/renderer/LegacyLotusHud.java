package io.github.lounode.extrabotany.client.renderer;

import com.mojang.blaze3d.platform.Window;
import io.github.lounode.extrabotany.common.block.flower.functional.StardustLotusBlockEntity;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import vazkii.botania.api.BotaniaAPIClient;
import vazkii.botania.api.block_entity.BindableSpecialFlowerBlockEntity;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.client.gui.HUDHandler;

public final class LegacyLotusHud extends BindableSpecialFlowerBlockEntity.BindableFlowerWandHud<StardustLotusBlockEntity> {
    public LegacyLotusHud(StardustLotusBlockEntity flower) { super(flower); }
    @Override public void renderHUD(GuiGraphics gui, Window window, Font font, int minLeft, int minRight, int minDown) {
        String name = I18n.get(flower.getBlockState().getBlock().getDescriptionId());
        int x = window.getGuiScaledWidth() / 2, y = window.getGuiScaledHeight() / 2;
        int half = (Math.max(102, font.width(name)) + 4) / 2;
        RenderHelper.renderHUDBox(gui, x - Math.max(half, minLeft), y + 8,
                x + Math.max(half + 20, minRight), y + Math.max(30, minDown + 52));
        BotaniaAPIClient.instance().drawComplexManaHUD(gui, window, font, flower.getColor(),
                flower.getConsumedMana(), flower.getTeleportCost(), name, flower.getHudIcon(), flower.isValidBinding());
        RenderHelper.drawTexturedModalRect(gui, HUDHandler.manaBar, x - 11, y + 34, 0, 38, 22, 15);
        var target = flower.getTarget();
        var text = target == null ? Component.translatable("message.extrabotany.stardust_lotus.no_target")
                : Component.translatable("message.extrabotany.stardust_lotus.target", target.getX(), target.getY(), target.getZ());
        gui.drawString(font, text, x - font.width(text) / 2, y + 51, 0xFFFFFF);
    }
}
