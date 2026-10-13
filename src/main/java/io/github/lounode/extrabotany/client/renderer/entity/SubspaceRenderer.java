package io.github.lounode.extrabotany.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

import org.joml.Matrix4f;

import io.github.lounode.extrabotany.common.entity.subspace.SubspaceEntity;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 亚空间裂隙的渲染器。它画一张竖直的方形贴图，贴图每 tick 换一帧，一共 6 帧。
 * The renderer for the Subspace rift. It draws a vertical square picture. The texture changes one frame on each tick, with 6 frames in total.
 *
 * <p>来源：ExtraMeteorP 1.12 {@code RenderSubspace}。
 * <p>Source: ExtraMeteorP 1.12 {@code RenderSubspace}.
 */
public class SubspaceRenderer extends EntityRenderer<SubspaceEntity> {
	private static final int FRAME_COUNT = 6;
	private static final ResourceLocation[] FRAMES = new ResourceLocation[FRAME_COUNT];
	private static final int WHITE = 0xFFFFFFFF;

	static {
		for (int i = 0; i < FRAME_COUNT; i++) {
			FRAMES[i] = prefix("textures/misc/subspace_" + i + ".png");
		}
	}

	public SubspaceRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public boolean shouldRender(SubspaceEntity rift, Frustum frustum, double x, double y, double z) {
		return rift.shouldRender(x, y, z) && frustum.isVisible(rift.getBoundingBox().inflate(rift.getSize()));
	}

	@Override
	public void render(SubspaceEntity rift, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
		float scale = rift.getRenderScale(partialTick);
		if (scale <= 0F) {
			return;
		}
		pose.pushPose();
		pose.mulPose(Axis.YP.rotationDegrees(rift.getRotation()));
		pose.scale(scale, scale, scale);
		VertexConsumer buffer = buffers.getBuffer(PortalRenderType.get(getTextureLocation(rift)));
		Matrix4f matrix = pose.last().pose();
		buffer.addVertex(matrix, -1F, -1F, 0F).setColor(WHITE).setUv(0F, 0F);
		buffer.addVertex(matrix, -1F, 1F, 0F).setColor(WHITE).setUv(0F, 1F);
		buffer.addVertex(matrix, 1F, 1F, 0F).setColor(WHITE).setUv(1F, 1F);
		buffer.addVertex(matrix, 1F, -1F, 0F).setColor(WHITE).setUv(1F, 0F);
		pose.popPose();
		super.render(rift, yaw, partialTick, pose, buffers, light);
	}

	@Override
	public ResourceLocation getTextureLocation(SubspaceEntity rift) {
		return FRAMES[Math.floorMod(rift.tickCount, FRAME_COUNT)];
	}
}
