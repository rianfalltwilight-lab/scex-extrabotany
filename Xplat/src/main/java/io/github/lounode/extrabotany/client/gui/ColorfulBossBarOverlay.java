package io.github.lounode.extrabotany.client.gui;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.BossEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import vazkii.botania.client.core.helper.CoreShaders;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.client.lib.ResourcesLib;
import vazkii.botania.client.core.helper.GuiGraphicsAddon;

import io.github.lounode.extrabotany.common.bossevents.GaiaBossEvent;
import io.github.lounode.extrabotany.network.clientbound.ColorfulBossEventPacket;
import io.github.lounode.extrabotany.network.clientbound.GaiaBossEventPacket;

import java.util.Map;
import java.util.UUID;

public class ColorfulBossBarOverlay {

	public static final ResourceLocation BAR_TEXTURE = ResourceLocation.tryParse(ResourcesLib.GUI_BOSS_BAR);
	private static final ResourceLocation GUI_BARS_LOCATION = ResourceLocation.tryParse("textures/gui/bars.png");
	private static final int BAR_WIDTH = 182;
	private static final int BAR_HEIGHT = 5;
	private static final int OVERLAY_OFFSET = 80;
	private final Minecraft minecraft;
	final Map<UUID, GaiaLerpingBossEvent> events = Maps.newLinkedHashMap();

	public ColorfulBossBarOverlay(Minecraft minecraft) {
		this.minecraft = minecraft;
	}

	public void render(GuiGraphics gui) {
		if (this.events.isEmpty()) {
			return;
		}

		int guiWidth = gui.guiWidth();
		int x = guiWidth / 2 - 91;
		int y = 12;

		for (var event : this.events.values()) {
			/*
			
			drawBar(guiGraphics, x, y, event);
			Component component = event.getName();
			int l = this.minecraft.font.width(component);
			int textX = guiWidth / 2 - l / 2;
			int textY = y - 9;
			guiGraphics.drawString(this.minecraft.font, component, textX, textY, 16777215);
			//guiGraphics.drawString(this.minecraft.font, component, textX, textY, component.getStyle() == Style.EMPTY ? event.getColor().getFormatting().getColor() : component.getStyle().getColor().getValue());
			int px = x + 160;
			int py = y + 12;
			drawPlayerCount(event.getPlayerCount(), guiGraphics, px, py);
			y += 19;
			if (y >= guiGraphics.guiHeight() / 3) {
				break;
			}
			
			*/

			// todo boss_bar.png has textures for different colors, respect bossEvent's getColor()?
			int frameU = 0, frameV = 0;
			int frameWidth = 185, frameHeight = 15;

			RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

			//Name
			Component name = event.getName();
			int centerX = minecraft.getWindow().getGuiScaledWidth() / 2;
			gui.drawCenteredString(minecraft.font, name, centerX, y - 10, 0xA2018C);

			int healthU = 0, healthV = frameV + frameHeight;
			int healthWidth = 181, healthHeight = 7;
			int healthX = x + (frameWidth - healthWidth) / 2;
			int healthY = y + (frameHeight - healthHeight) / 2;

			//BossBar
			RenderHelper.drawTexturedModalRect(gui, BAR_TEXTURE, x, y, frameU, frameV,
					frameWidth, frameHeight);
			drawHealthBar(gui, event, healthX, healthY, healthU, healthV,
					(int) (healthWidth * event.getProgress()), healthHeight);

			//y += frameHeight;

			//PlayerCount
			int px = x + 160;
			int py = y + 12;
			if (event.displayPlayerCount()) {
				drawPlayerCount(event.getPlayerCount(), gui, px, py);

			}

			y += (frameHeight * 2);

			if (y >= gui.guiHeight() / 3) {
				break;
			}
		}
	}

	public static void drawPlayerCount(int playerCount, GuiGraphics gui, int x, int y) {
		PoseStack ps = gui.pose();
		ps.pushPose();

		Minecraft mc = Minecraft.getInstance();
		ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
		gui.renderItem(stack, x, y);

		gui.drawString(mc.font, Integer.toString(playerCount), x + 15, y + 4, 0xFFFFFF);
		ps.popPose();
	}

	public static void drawBar(GuiGraphics guiGraphics, int x, int y, BossEvent bossEvent) {
		drawBar(guiGraphics, x, y, bossEvent, BAR_WIDTH, 0);
		int i = (int) (bossEvent.getProgress() * 183.0F);
		if (i > 0) {
			drawBar(guiGraphics, x, y, bossEvent, i, BAR_HEIGHT);
		}

	}

	public static void drawBar(GuiGraphics guiGraphics, int x, int y, BossEvent bossEvent, int width, int height) {
		guiGraphics.blit(GUI_BARS_LOCATION, x, y, 0, bossEvent.getColor().ordinal() * 5 * 2 + height, width, 5);
		if (bossEvent.getOverlay() != BossEvent.BossBarOverlay.PROGRESS) {
			RenderSystem.enableBlend();
			guiGraphics.blit(GUI_BARS_LOCATION, x, y, 0, OVERLAY_OFFSET + (bossEvent.getOverlay().ordinal() - 1) * 5 * 2 + height, width, 5);
			RenderSystem.disableBlend();
		}
	}

	public static void drawHealthBar(GuiGraphics gui, GaiaBossEvent bossEvent, int x, int y, int u, int v, int w, int h) {
		BossEvent event = (BossEvent) bossEvent;
		var shader = CoreShaders.gaiaBossBar();
		if (shader != null) {
			float grainIntensity = bossEvent.getGrainIntensity();
			shader.safeGetUniform("BotaniaGrainIntensity").set(grainIntensity);
			shader.safeGetUniform("BotaniaHpFract").set(event.getProgress());
		}

		RenderSystem.setShader(CoreShaders::gaiaBossBar);
		RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
		((GuiGraphicsAddon) gui).botania_blitWithoutShader(BAR_TEXTURE, x, y, u, v, w, h, 256, 256);
	}

	public void update(ColorfulBossEventPacket packet) {
		packet.dispatch(new GaiaBossEventPacket.Handler() {
			@Override
			public void updatePlayerCount(UUID uuid, int playerCount) {
				ColorfulBossBarOverlay.this.events.get(uuid).setPlayerCount(playerCount);
			}

			@Override
			public void updateGrainTime(UUID uuid, int time) {
				ColorfulBossBarOverlay.this.events.get(uuid).setGrainTime(time);
			}

			@Override
			public void add(UUID uuid, Component name, float progress, BossEvent.BossBarColor bossBarColor, BossEvent.BossBarOverlay overlay, boolean darkenScreen, boolean playMusic, boolean createWorldFog) {
				ColorfulBossBarOverlay.this.events.put(uuid, new GaiaLerpingBossEvent(uuid, name, progress, bossBarColor, overlay, darkenScreen, playMusic, createWorldFog));
			}

			@Override
			public void remove(UUID uuid) {
				ColorfulBossBarOverlay.this.events.remove(uuid);
			}

			@Override
			public void updateProgress(UUID uuid, float progress) {
				ColorfulBossBarOverlay.this.events.get(uuid).setProgress(progress);
			}

			@Override
			public void updateName(UUID uuid, Component name) {
				ColorfulBossBarOverlay.this.events.get(uuid).setName(name);
			}

			@Override
			public void updateStyle(UUID uuid, BossEvent.BossBarColor bossBarColor, BossEvent.BossBarOverlay overlay) {
				LerpingBossEvent lerpingbossevent = ColorfulBossBarOverlay.this.events.get(uuid);
				lerpingbossevent.setColor(bossBarColor);
				lerpingbossevent.setOverlay(overlay);
			}

			@Override
			public void updateProperties(UUID uuid, boolean darkenScreen, boolean playMusic, boolean createWorldFog) {
				LerpingBossEvent lerpingbossevent = ColorfulBossBarOverlay.this.events.get(uuid);
				lerpingbossevent.setDarkenScreen(darkenScreen);
				lerpingbossevent.setPlayBossMusic(playMusic);
				lerpingbossevent.setCreateWorldFog(createWorldFog);
			}
		});
	}

	public void reset() {
		this.events.clear();
	}

	public boolean shouldPlayMusic() {
		if (!this.events.isEmpty()) {
			for (BossEvent bossevent : this.events.values()) {
				if (bossevent.shouldPlayBossMusic()) {
					return true;
				}
			}
		}

		return false;
	}

	public boolean shouldDarkenScreen() {
		if (!this.events.isEmpty()) {
			for (BossEvent bossevent : this.events.values()) {
				if (bossevent.shouldDarkenScreen()) {
					return true;
				}
			}
		}

		return false;
	}

	public boolean shouldCreateWorldFog() {
		if (!this.events.isEmpty()) {
			for (BossEvent bossevent : this.events.values()) {
				if (bossevent.shouldCreateWorldFog()) {
					return true;
				}
			}
		}

		return false;
	}
}
