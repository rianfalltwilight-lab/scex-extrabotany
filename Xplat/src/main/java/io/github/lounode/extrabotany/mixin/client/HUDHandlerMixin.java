package io.github.lounode.extrabotany.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.platform.Window;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import vazkii.botania.client.gui.HUDHandler;

@Mixin(HUDHandler.class)
public class HUDHandlerMixin {
	//ManaReader
	@Inject(method = "drawSimpleManaHUD", at = @At("TAIL"), remap = false)
	private static void drawManaNumber(GuiGraphics gui, Window window, Font font, int color, int mana, int maxMana,
			String name, CallbackInfo ci) {
		Minecraft mc = Minecraft.getInstance();
		Player player = mc.player;
		if (player == null) {
			return;
		}

		boolean display = false;
		for (ItemStack stack : player.getHandSlots()) {
			if (stack.isEmpty()) {
				continue;
			}

			CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
			if (tag != null && tag.contains("extrabotany", Tag.TAG_COMPOUND)) {
				CompoundTag extraBotany = tag.getCompound("extrabotany");
				if (extraBotany.contains("mana_reader", Tag.TAG_COMPOUND)) {
					CompoundTag manaReader = extraBotany.getCompound("mana_reader");
					if (manaReader.getBoolean("enable")) {
						display = true;
						break;
					}
				}
			}
		}

		if (!display) {
			return;
		}

		RenderSystem.enableBlend();
		String manaString = mana + "/" + maxMana;

		int screenWidth = window.getGuiScaledWidth();
		int x = window.getGuiScaledWidth() / 2 - font.width(name) / 2;
		int y = window.getGuiScaledHeight() / 2 + 10;
		x = (int) (((float) screenWidth / 2 - font.width(manaString) * 0.75f / 2));
		y += 15;

		gui.pose().pushPose();
		gui.pose().scale(0.75f, 0.75f, 1f);
		gui.drawString(font, manaString, (int) (x / 0.75f), (int) (y / 0.75f), color);
		gui.pose().popPose();

		RenderSystem.disableBlend();
	}

	//Takeover original render
	@Inject(
		method = "renderManaInvBar",
		at = @At("HEAD"),
		cancellable = true,
		remap = false
	)
	private static void onRenderManaBar(GuiGraphics gui, Window window, int totalMana, int totalMaxMana,
			CallbackInfo ci) {
		ci.cancel();
	}
}
