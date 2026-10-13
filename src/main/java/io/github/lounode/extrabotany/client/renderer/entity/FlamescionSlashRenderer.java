package io.github.lounode.extrabotany.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

import io.github.lounode.extrabotany.common.entity.flamescion.FlamescionSlashEntity;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 薪炎斩击的渲染器。它画一张朝向镜头的平面，贴图每 tick 换一帧，一共 6 帧。每道斩击有自己的随机朝向。
 * The renderer for the Flamescion Slash. It draws a flat quad that faces the camera. The texture changes one frame on each tick, with 6 frames in total. Each slash has its own random orientation.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code RenderFlamescionSlash} 和 {@code ModelSlash}。
 * <p>Source: ExtraMeteorP 1.16 {@code RenderFlamescionSlash} and {@code ModelSlash}.
 */
public class FlamescionSlashRenderer extends EntityRenderer<FlamescionSlashEntity> {
	private static final int FRAME_COUNT = 6;
	private static final ResourceLocation[] FRAMES = new ResourceLocation[FRAME_COUNT];
	private static final float SCALE = 3F;
	private static final int FULL_BRIGHT = 0xF000F0;
	private static final double VISIBLE_RANGE = 6.0;

	static {
		for (int i = 0; i < FRAME_COUNT; i++) {
			FRAMES[i] = prefix("textures/entity/flamescionslash_" + i + ".png");
		}
	}

	private final ModelPart quad;

	public FlamescionSlashRenderer(EntityRendererProvider.Context context) {
		super(context);
		MeshDefinition mesh = new MeshDefinition();
		mesh.getRoot().addOrReplaceChild("bone",
				CubeListBuilder.create().texOffs(0, 0).addBox(-16.0F, -32.0F, 0.0F, 32.0F, 32.0F, 0.0F),
				PartPose.offset(0.0F, 24.0F, 0.0F));
		this.quad = LayerDefinition.create(mesh, 64, 64).bakeRoot().getChild("bone");
	}

	@Override
	public boolean shouldRender(FlamescionSlashEntity slash, Frustum frustum, double x, double y, double z) {
		// 画面比实体的碰撞箱大得多，所以用放大的范围做视锥剔除。
		// The picture is much larger than the collision box of the entity, so the frustum check uses a larger box.
		return slash.shouldRender(x, y, z) && frustum.isVisible(slash.getBoundingBox().inflate(VISIBLE_RANGE));
	}

	@Override
	public void render(FlamescionSlashEntity slash, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
		pose.pushPose();
		pose.mulPose(entityRenderDispatcher.cameraOrientation());
		pose.scale(SCALE, -SCALE, -SCALE);
		pose.mulPose(Axis.XP.rotationDegrees(slash.getPitch()));
		pose.mulPose(Axis.YP.rotationDegrees(slash.getRotation()));
		quad.render(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(slash))), FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
		pose.popPose();
		super.render(slash, yaw, partialTick, pose, buffers, light);
	}

	@Override
	public ResourceLocation getTextureLocation(FlamescionSlashEntity slash) {
		return FRAMES[slash.tickCount % FRAME_COUNT];
	}
}
