package io.github.lounode.extrabotany.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

import io.github.lounode.extrabotany.common.entity.ThunderSlashEntity;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 心斩的渲染器。它画一张始终朝向镜头的平面，贴图每 tick 换一帧，一共 35 帧。
 * The renderer for the Thunder Slash. It draws a flat quad that always faces the camera. The texture changes one frame on each tick, with 35 frames in total.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code RenderSlash} 和 {@code ModelSlash}。
 * <p>Source: ExtraMeteorP 1.16 {@code RenderSlash} and {@code ModelSlash}.
 */
public class ThunderSlashRenderer extends EntityRenderer<ThunderSlashEntity> {
	private static final int FRAME_COUNT = 35;
	private static final ResourceLocation[] FRAMES = new ResourceLocation[FRAME_COUNT];
	private static final float SCALE = 1.75F;
	private static final int FULL_BRIGHT = 0xF000F0;

	static {
		for (int i = 0; i < FRAME_COUNT; i++) {
			FRAMES[i] = prefix("textures/entity/thunder_slash/slash_" + i + ".png");
		}
	}

	private final ModelPart quad;

	public ThunderSlashRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.quad = createLayer().bakeRoot().getChild("bone");
	}

	private static LayerDefinition createLayer() {
		MeshDefinition mesh = new MeshDefinition();
		mesh.getRoot().addOrReplaceChild("bone",
				CubeListBuilder.create().texOffs(0, 0).addBox(-16.0F, -32.0F, 0.0F, 32.0F, 32.0F, 0.0F),
				PartPose.offset(0.0F, 24.0F, 0.0F));
		return LayerDefinition.create(mesh, 64, 64);
	}

	@Override
	public void render(ThunderSlashEntity slash, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
		pose.pushPose();
		pose.mulPose(entityRenderDispatcher.cameraOrientation());
		pose.scale(SCALE, -SCALE, -SCALE);
		VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(slash)));
		quad.render(pose, buffer, FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
		pose.popPose();
		super.render(slash, yaw, partialTick, pose, buffers, light);
	}

	@Override
	public ResourceLocation getTextureLocation(ThunderSlashEntity slash) {
		return FRAMES[slash.tickCount % FRAME_COUNT];
	}
}
