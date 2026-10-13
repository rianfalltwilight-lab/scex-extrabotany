package io.github.lounode.extrabotany.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

import io.github.lounode.extrabotany.common.entity.subspace.SubspaceSpearEntity;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 亚空之矛的渲染器。
 * The renderer for the Subspace Spear.
 *
 * <p>来源：ExtraMeteorP 1.12 {@code RenderSubspaceSpear} 和 {@code ModelSubspaceSpear}。
 * <p>Source: ExtraMeteorP 1.12 {@code RenderSubspaceSpear} and {@code ModelSubspaceSpear}.
 *
 * <p>和原版的差异：原版的渲染器把模型平移了 (0.5, 1.5, 0.5)，这是方块实体渲染器的写法。在 1.21.1 上这会让整个模型偏离实体的碰撞箱。这里把模型的中心对准碰撞箱的中心。
 * <p>Difference from the original: the renderer of the original moves the model by (0.5, 1.5, 0.5), which is how a block entity renderer is written. On 1.21.1 this puts the full model away from the collision box of the entity. This port puts the center of the model at the center of the collision box.
 */
public class SubspaceSpearRenderer extends EntityRenderer<SubspaceSpearEntity> {
	private static final ResourceLocation TEXTURE = prefix("textures/entity/spearsubspace.png");
	// 原版用 0.07 的缩放渲染模型，而模型部件自己按 1/16 缩放。0.07 × 16 = 1.12。
	// The original draws the model with a scale of 0.07, and a model part has its own scale of 1/16. 0.07 × 16 = 1.12.
	private static final float SCALE = 1.12F;
	// 模型的中心不在原点，而在 (1, -0.5, 1) 像素处。
	// The center of the model is not at the origin. It is at (1, -0.5, 1) pixels.
	private static final float MODEL_CENTER_X = 1F / 16F;
	private static final float MODEL_CENTER_Y = -0.5F / 16F;
	private static final float MODEL_CENTER_Z = 1F / 16F;

	// 每一行是一个方块：贴图 U、贴图 V、X、Y、Z、宽、高、深。
	// Each row is one box: texture U, texture V, X, Y, Z, width, height, depth.
	private static final float[][] SHAFT_BOXES = {
			{ 10, 0, 0, -0.5F, 0, 2, 3, 2 }, { 10, 0, 0, 0, -0.5F, 2, 2, 3 }, { 10, 0, -0.5F, 0, 0, 3, 2, 2 },
			{ 26, 0, 0.5F, 7, 0.5F, 1, 16, 1 }, { 26, 0, 0, 8, 0, 1, 10, 1 }, { 26, 0, 1, 6, 1, 1, 9, 1 },
			{ 26, 0, 1, 5, 0, 1, 9, 1 }, { 26, 0, 0, 7, 1, 1, 7, 1 },
			{ 0, 0, 0, -12, 0, 1, 10, 1 }, { 0, 0, 1, -14, 0, 1, 10, 1 }, { 0, 0, 0, -16, 1, 1, 13, 1 },
			{ 0, 0, 1, -20, 1, 1, 14, 1 }, { 0, 0, 0.5F, -24, 0.5F, 1, 13, 1 }
	};
	// 每一行是一片斜置的矛翼：贴图 U、X、Y、Z、高、绕 X 轴的角度。
	// Each row is one angled fin: texture U, X, Y, Z, height, angle around the X axis.
	private static final float[][] FINS = {
			{ 5, 0, -0.5F, -2, 2, -0.7853982F }, { 5, 0, 0, -2.5F, 4, 0.1919862F },
			{ 21, 0, -1.5F, 3, 4, 0.1919862F }, { 21, 0, -1.5F, 4, 2, -0.7853982F }
	};
	private static final float FIN_Y_ROTATION = 0.2617994F;

	private final ModelPart model;

	public SubspaceSpearRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.model = bakeModel();
	}

	/**
	 * 生成矛的模型。空之律者的长枪也使用这个模型。
	 * Makes the model of the spear. The lance of the Herrscher of the Void also uses this model.
	 */
	static ModelPart bakeModel() {
		return createLayer().bakeRoot().getChild("spear");
	}

	private static LayerDefinition createLayer() {
		MeshDefinition mesh = new MeshDefinition();
		CubeListBuilder shaft = CubeListBuilder.create();
		for (float[] box : SHAFT_BOXES) {
			shaft.texOffs((int) box[0], (int) box[1]).addBox(box[2], box[3], box[4], box[5], box[6], box[7]);
		}
		PartDefinition spear = mesh.getRoot().addOrReplaceChild("spear", shaft, PartPose.ZERO);
		for (int i = 0; i < FINS.length; i++) {
			float[] fin = FINS[i];
			spear.addOrReplaceChild("fin" + i,
					CubeListBuilder.create().texOffs((int) fin[0], 0).addBox(fin[1], fin[2], fin[3], 1F, fin[4], 1F),
					PartPose.rotation(fin[5], FIN_Y_ROTATION, 0F));
		}
		return LayerDefinition.create(mesh, 64, 32);
	}

	@Override
	public void render(SubspaceSpearEntity spear, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
		pose.pushPose();
		pose.translate(0F, spear.getBbHeight() * 0.5F, 0F);
		pose.mulPose(Axis.YP.rotationDegrees(180F + spear.getYRot()));
		pose.mulPose(Axis.XP.rotationDegrees(90F));
		pose.mulPose(Axis.XP.rotationDegrees(spear.getXRot()));
		pose.scale(SCALE, -SCALE, -SCALE);
		pose.translate(-MODEL_CENTER_X, -MODEL_CENTER_Y, -MODEL_CENTER_Z);
		model.render(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)), light, OverlayTexture.NO_OVERLAY);
		pose.popPose();
		super.render(spear, yaw, partialTick, pose, buffers, light);
	}

	@Override
	public ResourceLocation getTextureLocation(SubspaceSpearEntity spear) {
		return TEXTURE;
	}
}
