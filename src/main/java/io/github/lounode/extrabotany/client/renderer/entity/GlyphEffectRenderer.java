package io.github.lounode.extrabotany.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.InventoryMenu;

/**
 * 用文字画出来的效果实体的渲染器。原版用汉字拼出薪炎的剑气、黑洞和终结技，这里保持一样的画法。
 * The renderer for effect entities that are drawn with text. The original makes the sword, the void and the ultimate of the Flamescion from Chinese characters, and this port draws them in the same way.
 *
 * <p>文字来自语言文件的 {@code mri.*} 键。这些字是图案，不是说明文字，所以在全部语言里都相同。
 * <p>The text comes from the {@code mri.*} keys of the language file. These characters are a picture and not a description, so they are the same in all languages.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code RenderFlamescionSword}、{@code RenderFlamescionVoid} 和 {@code RenderFlamescionUlt}。
 * <p>Source: ExtraMeteorP 1.16 {@code RenderFlamescionSword}, {@code RenderFlamescionVoid} and {@code RenderFlamescionUlt}.
 */
public class GlyphEffectRenderer<T extends Entity> extends EntityRenderer<T> {
	// 原版文字渲染的基础缩放：1/60 格对应 1 个像素。
	// The base scale of vanilla text rendering: 1 pixel is 1/60 of a block.
	private static final float PIXEL = 1F / 60F;
	private static final int LINE_HEIGHT = 12;
	private static final double VISIBLE_RANGE = 6.0;

	private final Component[] lines;
	private final float scale;
	private final float spinDegreesPerTick;

	/**
	 * @param scale              文字的放大倍数。 The magnification of the text.
	 * @param spinDegreesPerTick 文字绕视线旋转的速度。0 表示不旋转。 The speed that the text turns around the view axis. 0 for no turn.
	 * @param color              文字的颜色。 The color of the text.
	 * @param keys               每一行文字的语言键。 The language key for each line of text.
	 */
	public GlyphEffectRenderer(EntityRendererProvider.Context context, float scale, float spinDegreesPerTick, ChatFormatting color, String... keys) {
		super(context);
		this.scale = scale;
		this.spinDegreesPerTick = spinDegreesPerTick;
		this.lines = new Component[keys.length];
		for (int i = 0; i < keys.length; i++) {
			lines[i] = Component.translatable(keys[i]).withStyle(color);
		}
	}

	/**
	 * 剑气：红色的"剑气"两个字。
	 * The sword: the two characters for "sword energy" in red.
	 */
	public static <T extends Entity> GlyphEffectRenderer<T> sword(EntityRendererProvider.Context context) {
		return new GlyphEffectRenderer<>(context, 7F, 0F, ChatFormatting.RED, "mri.sword");
	}

	/**
	 * 黑洞：金色的"黑洞"两个字，每 tick 旋转 10 度。
	 * The void: the two characters for "black hole" in gold, with a turn of 10 degrees on each tick.
	 */
	public static <T extends Entity> GlyphEffectRenderer<T> flameVoid(EntityRendererProvider.Context context) {
		return new GlyphEffectRenderer<>(context, 5F, 10F, ChatFormatting.GOLD, "mri.void");
	}

	/**
	 * 终结技：八行深红色的字组成的图案。
	 * The ultimate: a picture made from eight lines of dark red characters.
	 */
	public static <T extends Entity> GlyphEffectRenderer<T> ultimate(EntityRendererProvider.Context context) {
		return new GlyphEffectRenderer<>(context, 4F, 0F, ChatFormatting.DARK_RED,
				"mri.ult0", "mri.ult1", "mri.ult2", "mri.ult3", "mri.ult4", "mri.ult5", "mri.ult6", "mri.ult7");
	}

	@Override
	public boolean shouldRender(T entity, Frustum frustum, double x, double y, double z) {
		return entity.shouldRender(x, y, z) && frustum.isVisible(entity.getBoundingBox().inflate(VISIBLE_RANGE));
	}

	@Override
	public void render(T entity, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
		Font font = getFont();
		pose.pushPose();
		pose.mulPose(entityRenderDispatcher.cameraOrientation());
		if (spinDegreesPerTick != 0F) {
			pose.mulPose(Axis.ZN.rotationDegrees((entity.tickCount + partialTick) * spinDegreesPerTick));
		}
		float size = PIXEL * scale;
		pose.scale(-size, -size, size);
		if (lines.length > 1) {
			// 多行图案整体上移，让它的中心大致在实体的位置。
			// A picture with more than one line is moved up, so that its center is approximately at the position of the entity.
			pose.translate(0F, -60F, 0F);
		}
		for (Component line : lines) {
			if (lines.length > 1) {
				pose.translate(0F, LINE_HEIGHT, 0F);
			}
			font.drawInBatch(line, -font.width(line) / 2F, 0F, 0xFFFFFFFF, false, pose.last().pose(), buffers,
					Font.DisplayMode.NORMAL, 0, light);
		}
		pose.popPose();
		super.render(entity, yaw, partialTick, pose, buffers, light);
	}

	@Override
	public ResourceLocation getTextureLocation(T entity) {
		return InventoryMenu.BLOCK_ATLAS;
	}
}
