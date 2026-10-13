package io.github.lounode.extrabotany.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.AABB;

import org.joml.Matrix4f;

import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.core.helper.RenderHelper;

import io.github.lounode.extrabotany.common.entity.EgoLandmineEntity;

/**
 * 本我的地雷的渲染器。它在地面上绘制一块半透明的方形色块，颜色表示地雷的种类。色块出现时淡入，爆炸前淡出。
 * The renderer of the landmine of the Ego. It draws a translucent colored square on the ground, and the color shows the type of the landmine. The square fades in when it appears and fades out before the explosion.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code RenderEGOLandmine}。它和植物魔法的魔法地雷用同一种画法。
 * <p>Source: ExtraMeteorP 1.16 {@code RenderEGOLandmine}. It uses the same drawing method as the magic landmine of Botania.
 */
public class EgoLandmineRenderer extends EntityRenderer<EgoLandmineEntity> {
	private static final int BASE_ALPHA = 32;
	private static final int BASE_BRIGHTNESS = 240;
	private static final float FADE_TICKS = 8F;
	/** 地雷从这个时刻开始淡出。 / The landmine starts to fade out at this time. */
	private static final int FADE_OUT_START = 47;

	public EgoLandmineRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public void render(EgoLandmineEntity landmine, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int packedLight) {
		super.render(landmine, yaw, partialTick, poseStack, buffers, packedLight);
		// 颜色的亮度慢慢地起伏。
		// The brightness of the color slowly goes up and down.
		float pulse = (float) (Math.sin((ClientTickHandler.getEntityTicksInGame() + partialTick) / 20) + 1) * 0.2F + 0.6F;
		int brightness = (int) (BASE_BRIGHTNESS * pulse);
		int type = landmine.getLandmineType();
		int red = type == EgoLandmineEntity.TYPE_RED ? brightness : 0;
		int green = type == EgoLandmineEntity.TYPE_GREEN ? brightness : 0;
		int blue = type == EgoLandmineEntity.TYPE_BLUE ? brightness : 0;

		float age = landmine.tickCount + partialTick;
		float fade = 1F;
		if (landmine.tickCount < FADE_TICKS) {
			fade = Math.min(age / FADE_TICKS, 1F);
		} else if (landmine.tickCount > FADE_OUT_START) {
			fade = Math.clamp(1F - (age - FADE_OUT_START) / FADE_TICKS, 0F, 1F);
		}

		AABB box = landmine.getBoundingBox().move(landmine.position().scale(-1));
		float inset = 1F / 16F;
		poseStack.pushPose();
		// 每块色块的高度略有不同，这样重叠的地雷不会闪烁。
		// Each square has a slightly different height, thus landmines that overlap do not flicker.
		poseStack.translate(box.minX, box.minY + RenderHelper.getOffY(), box.minZ);
		VertexConsumer buffer = buffers.getBuffer(RenderHelper.RECTANGLE);
		Matrix4f matrix = poseStack.last().pose();
		RenderHelper.flatRectangle(buffer, matrix, inset, (float) box.getXsize() - inset, 0, inset, (float) box.getZsize() - inset,
				red, green, blue, (int) (BASE_ALPHA * fade));
		RenderHelper.incrementOffY();
		poseStack.popPose();
	}

	@Override
	public ResourceLocation getTextureLocation(EgoLandmineEntity landmine) {
		return InventoryMenu.BLOCK_ATLAS;
	}
}
