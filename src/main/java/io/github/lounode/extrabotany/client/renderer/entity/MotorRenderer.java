package io.github.lounode.extrabotany.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

import io.github.lounode.extrabotany.common.entity.mount.MotorEntity;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 机车的渲染器。
 * The renderer for the Armored Motor.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code RenderMotor} 和 {@code ModelMotor}。
 * <p>Source: ExtraMeteorP 1.16 {@code RenderMotor} and {@code ModelMotor}.
 *
 * <p>和原版的差异：原版把"40 × 骑乘 tick 数"的角度值直接当作弧度使用，所以车轮每 tick 跳到一个不连续的角度。这里按机车实际移动的距离转动车轮。
 * <p>Difference from the original: the original uses the degree value "40 × riding ticks" as radians, so the wheels jump to a different angle on each tick. This port turns the wheels by the distance that the motor actually moves.
 */
public class MotorRenderer extends EntityRenderer<MotorEntity> {
	private static final ResourceLocation TEXTURE = prefix("textures/entity/motor.png");

	private final ModelPart body;
	private final ModelPart frontWheel;
	private final ModelPart backWheel;

	public MotorRenderer(EntityRendererProvider.Context context) {
		super(context);
		body = MountModels.motor().bakeRoot().getChild("all");
		frontWheel = body.getChild("tirefront3");
		backWheel = body.getChild("tireback");
	}

	@Override
	public void render(MotorEntity motor, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
		pose.pushPose();
		pose.translate(0.0, 1.5, 0.0);
		pose.mulPose(Axis.YP.rotationDegrees(180F - yaw));
		pose.mulPose(Axis.ZP.rotationDegrees(motor.getRoll()));
		pose.mulPose(Axis.XP.rotationDegrees(motor.getPitch()));
		pose.scale(-1F, -1F, 1F);

		float wheelAngle = motor.getWheelAngle(partialTick);
		frontWheel.xRot = wheelAngle;
		backWheel.xRot = wheelAngle;

		VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
		body.render(pose, buffer, light, OverlayTexture.NO_OVERLAY);
		pose.popPose();
		super.render(motor, yaw, partialTick, pose, buffers, light);
	}

	@Override
	public ResourceLocation getTextureLocation(MotorEntity motor) {
		return TEXTURE;
	}
}
