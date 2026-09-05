package io.github.lounode.extrabotany.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.AABB;

import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.core.helper.RenderHelper;

import io.github.lounode.extrabotany.common.entity.MagicLandMineEntity;

public class MagicLandMineRenderer extends EntityRenderer<MagicLandMineEntity> {
	public static final int FADE_TICKS = 8;

	public MagicLandMineRenderer(EntityRendererProvider.Context ctx) {
		super(ctx);
	}

	@Override
	public void render(MagicLandMineEntity e, float entityYaw, float partialTicks, PoseStack ms, MultiBufferSource buffers, int light) {
		super.render(e, entityYaw, partialTicks, ms, buffers, light);

		ms.pushPose();
		AABB aabb = e.getBoundingBox().move(e.position().scale(-1));

		float gs = (float) (Math.sin((e.tickCount + partialTicks) / 20) + 1) * 0.2F + 0.6F;
		int r = (int) (105 * gs);
		int g = (int) (25 * gs);
		int b = (int) (145 * gs);

		int alpha = 32;

		if (e.tickCount < FADE_TICKS) {
			alpha *= Math.min((e.tickCount + partialTicks) / 8F, 1F);
		} else if (e.getExplodeCountDown() < FADE_TICKS) {
			float fadeProgress = 1 - (FADE_TICKS - 1 - e.getExplodeCountDown() + partialTicks) / 8F;
			alpha *= Math.min(fadeProgress, 1f);
		}

		ms.translate(aabb.minX, aabb.minY + RenderHelper.getOffY(), aabb.minZ);

		float f = 1F / 16F;
		float x = (float) (aabb.getXsize() - f);
		float z = (float) (aabb.getZsize() - f);

		VertexConsumer buffer = buffers.getBuffer(RenderHelper.RECTANGLE);
		Matrix4f mat = ms.last().pose();
		RenderHelper.flatRectangle(buffer, mat, f, x, 0, f, z, r, g, b, alpha);
		RenderHelper.incrementOffY();
		ms.popPose();
	}

	@NotNull
	@Override
	public ResourceLocation getTextureLocation(@NotNull MagicLandMineEntity entity) {
		return InventoryMenu.BLOCK_ATLAS;
	}
}
