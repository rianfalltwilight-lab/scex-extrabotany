package io.github.lounode.extrabotany.client.gui;

import com.google.common.collect.Iterables;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.mana.ManaItem;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.common.lib.BotaniaTags;
import vazkii.botania.xplat.BotaniaConfig;

import io.github.lounode.extrabotany.common.item.relic.CameraItem;
import io.github.lounode.extrabotany.common.item.relic.void_archives.VoidArchivesItem;
import io.github.lounode.extrabotany.common.item.relic.void_archives.variants.Camera;

import java.util.List;

/**
 * 本模组的界面层：相机的取景框、Boss 血条和物品栏上方的魔力条。
 * The HUD layer of this mod: the frame of the camera, the boss bars, and the mana bar above the hotbar.
 */
public final class HUD {
	public static HUD INSTANCE;
	private static final ResourceLocation manaBar = ResourceLocation.tryParse("botania:textures/gui/mana_hud.png");

	private final Minecraft minecraft;
	private final ColorfulBossBarOverlay bossOverlay;
	private float scopeScale;

	public HUD(Minecraft minecraft) {
		INSTANCE = this;
		this.minecraft = minecraft;
		this.bossOverlay = new ColorfulBossBarOverlay(minecraft);
	}

	public void onDrawScreenPost(GuiGraphics gui, float partialTicks) {
		PoseStack ms = gui.pose();
		if (minecraft.options.hideGui) {
			return;
		}
		ProfilerFiller profiler = minecraft.getProfiler();
		profiler.push("extrabotany-hud");

		float f = minecraft.getTimer().getGameTimeDeltaPartialTick(true);
		scopeScale = Mth.lerp(0.5F * f, scopeScale, 1.125F);
		if (minecraft.options.getCameraType().isFirstPerson()) {
			if (shouldRenderCameraGUI()) {
				profiler.push("camera-hud");
				CameraOverlay.render(gui, scopeScale);
				profiler.pop();
			} else {
				scopeScale = 0.5F;
			}
		}

		profiler.push("boss-bar");
		bossOverlay.render(gui);
		profiler.pop();

		profiler.push("manaBar");
		renderManaBar(gui, partialTicks);
		profiler.pop();

		profiler.pop();
	}

	/**
	 * 画出物品栏上方的魔力条。它代替植物魔法自己的魔力条（见 {@code HUDHandlerMixin}）。
	 * Draws the mana bar above the hotbar. It replaces the mana bar of Botania (see {@code HUDHandlerMixin}).
	 *
	 * <p>植物魔法用 int 累加所有物品的魔力。大师魔力之戒报告的数值接近 int 的最大值，所以累加会溢出。这里用 long 累加。
	 * <p>Botania adds the mana of all items in an int. The Master Band of Mana reports a value near the maximum of an int, thus the sum overflows. This method adds in a long.
	 */
	private void renderManaBar(GuiGraphics gui, float partialTicks) {
		Player player = minecraft.player;
		if (player == null || player.isSpectator()) {
			return;
		}
		boolean anyRequest = false;
		Container mainInv = player.getInventory();
		Container accInv = BotaniaAPI.instance().getAccessoriesInventory(player);
		int invSize = mainInv.getContainerSize();
		int size = invSize + accInv.getContainerSize();

		for (int i = 0; i < size && !anyRequest; ++i) {
			boolean useAccessories = i >= invSize;
			Container inv = useAccessories ? accInv : mainInv;
			ItemStack stack = inv.getItem(i - (useAccessories ? invSize : 0));
			anyRequest = !stack.isEmpty() && stack.is(BotaniaTags.Items.MANA_USING_ITEMS);
		}
		// 玩家身上没有使用魔力的物品时不显示魔力条，所以也不需要查找魔力物品。
		// The bar is not shown when the player has no item that uses mana, thus the search for mana items is not necessary.
		if (!anyRequest) {
			return;
		}

		long totalMana = 0;
		long totalMaxMana = 0;
		List<ItemStack> items = ManaItemHandler.instance().getManaItems(player);
		List<ItemStack> acc = ManaItemHandler.instance().getManaAccesories(player);

		for (ItemStack stack : Iterables.concat(items, acc)) {
			ManaItem manaItem = io.github.lounode.extrabotany.xplat.EXplatAbstractions.INSTANCE.findManaItem(stack);
			if (manaItem != null && !manaItem.isNoExport()) {
				totalMana += manaItem.getMana();
				totalMaxMana += manaItem.getMaxMana();
			}
		}

		if (totalMaxMana > 0) {
			renderManaInvBar(gui, (float) ((double) totalMana / (double) totalMaxMana));
		}
	}
	private static void renderManaInvBar(GuiGraphics gui, float progress) {
		Minecraft mc = Minecraft.getInstance();
		int width = 182;
		int x = mc.getWindow().getGuiScaledWidth() / 2 - width / 2;
		int y = mc.getWindow().getGuiScaledHeight() - BotaniaConfig.client().manaBarHeight();

		width = (int) ((double) width * progress);

		if (width == 0) {
			width = 1;
		}

		int color = Mth.hsvToRgb(0.55F, (float) Math.min((double) 1.0F, Math.sin((double) Util.getMillis() / (double) 200.0F) * (double) 0.5F + (double) 1.0F), 1.0F);
		int r = color >> 16 & 255;
		int g = color >> 8 & 255;
		int b = color & 255;
		RenderSystem.setShaderColor((float) r / 255.0F, (float) g / 255.0F, (float) b / 255.0F, 1.0F - (float) r / 255.0F);
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(770, 771);
		RenderHelper.drawTexturedModalRect(gui, manaBar, x, y, 0, 251, width, 5);
		RenderSystem.disableBlend();
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
	}

	private boolean shouldRenderCameraGUI() {
		ItemStack useItem = minecraft.player.getUseItem();
		if (useItem.getItem() instanceof CameraItem) {
			return true;
		}
		if (useItem.getItem() instanceof VoidArchivesItem item) {
			if (VoidArchivesItem.getVariant(useItem).getId().equals(Camera.INSTANCE.getId())) {
				return true;
			}
		}

		return false;
	}

	public static HUD getInstance() {
		return INSTANCE;
	}

	public ColorfulBossBarOverlay getBossOverlay() {
		return bossOverlay;
	}

	//TODO ClientPlayerNetworkEvent
	public static void onDisconnected() {
		HUD.getInstance().getBossOverlay().reset();
	}

}
