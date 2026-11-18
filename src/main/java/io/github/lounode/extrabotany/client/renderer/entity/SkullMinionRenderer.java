package io.github.lounode.extrabotany.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import io.github.lounode.extrabotany.common.entity.SkullMinionEntity;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 骷髅仆从：一个会转向玩家的头颅，四种贴图。来源：ExtraMeteorP 1.12 {@code RenderSkullMinion}。
 * The skull minion: a head that turns to the player, with four textures. Source: {@code RenderSkullMinion} of ExtraMeteorP 1.12.
 */
public class SkullMinionRenderer extends EntityRenderer<SkullMinionEntity> {
	private static final ResourceLocation[] TEXTURES = {
			prefix("textures/entity/skull_minion/1.png"), prefix("textures/entity/skull_minion/2.png"),
			prefix("textures/entity/skull_minion/3.png"), prefix("textures/entity/skull_minion/4.png")
	};
	private final SkullModel model;

	public SkullMinionRenderer(EntityRendererProvider.Context ctx) {
		super(ctx);
		this.model = new SkullModel(ctx.bakeLayer(ModelLayers.PLAYER_HEAD));
	}

	@Override
	public void render(SkullMinionEntity entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int light) {
		poseStack.pushPose();
		poseStack.scale(-1.0F, -1.0F, 1.0F);
		float headYaw = Mth.rotLerp(partialTicks, entity.yHeadRotO, entity.yHeadRot);
		float pitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
		this.model.setupAnim(0.0F, headYaw, pitch);
		this.model.renderToBuffer(poseStack, buffer.getBuffer(this.model.renderType(getTextureLocation(entity))), light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
		poseStack.popPose();
		super.render(entity, yaw, partialTicks, poseStack, buffer, light);
	}

	@Override
	public ResourceLocation getTextureLocation(SkullMinionEntity entity) {
		int type = entity.getMinionType();
		return TEXTURES[type >= 0 && type < TEXTURES.length ? type : 0];
	}
}
