package io.github.lounode.extrabotany.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import vazkii.botania.client.lib.ResourcesLib;
import vazkii.botania.client.model.BotaniaModelLayers;

import io.github.lounode.extrabotany.common.entity.PetPixieEntity;

/**
 * 宠物精灵的渲染器。它使用植物魔法的精灵模型和贴图。
 * The renderer for the pet pixie. It uses the pixie model and texture of Botania.
 */
public class PetPixieRenderer extends MobRenderer<PetPixieEntity, PetPixieRenderer.Model> {
	private static final ResourceLocation TEXTURE = ResourceLocation.parse(ResourcesLib.MODEL_PIXIE);

	public PetPixieRenderer(EntityRendererProvider.Context context) {
		super(context, new Model(context.bakeLayer(BotaniaModelLayers.PIXIE)), 0F);
	}

	@Override
	public ResourceLocation getTextureLocation(PetPixieEntity entity) {
		return TEXTURE;
	}

	/**
	 * 精灵模型。部件来自植物魔法注册的模型层，所以外形和植物魔法的精灵一致。
	 * The pixie model. The parts come from the model layer that Botania registers, so the shape agrees with the pixie of Botania.
	 */
	public static class Model extends EntityModel<PetPixieEntity> {
		private static final float WING_SPEED = 1.7F;

		private final ModelPart body;
		private final ModelPart leftWingTop;
		private final ModelPart leftWingBottom;
		private final ModelPart rightWingTop;
		private final ModelPart rightWingBottom;

		public Model(ModelPart root) {
			super(RenderType::entityCutoutNoCull);
			body = root.getChild("body");
			leftWingTop = root.getChild("leftWingT");
			leftWingBottom = root.getChild("leftWingB");
			rightWingTop = root.getChild("rightWingT");
			rightWingBottom = root.getChild("rightWingB");
		}

		@Override
		public void setupAnim(PetPixieEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
				float netHeadYaw, float headPitch) {
			float flap = Mth.cos(ageInTicks * WING_SPEED) * Mth.PI;
			rightWingTop.yRot = -flap * 0.5F;
			leftWingTop.yRot = flap * 0.5F;
			rightWingBottom.yRot = -flap * 0.25F;
			leftWingBottom.yRot = flap * 0.25F;
		}

		@Override
		public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int light, int overlay, int color) {
			body.render(pose, buffer, light, overlay, color);
			leftWingTop.render(pose, buffer, light, overlay, color);
			leftWingBottom.render(pose, buffer, light, overlay, color);
			rightWingTop.render(pose, buffer, light, overlay, color);
			rightWingBottom.render(pose, buffer, light, overlay, color);
		}
	}
}
