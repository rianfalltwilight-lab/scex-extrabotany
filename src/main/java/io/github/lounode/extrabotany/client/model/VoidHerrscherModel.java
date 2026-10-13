package io.github.lounode.extrabotany.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

import io.github.lounode.extrabotany.common.entity.gaia.VoidHerrscher;

/**
 * 空之律者的模型：一个玩家体型的人形，带一层外衣、一对翅膀和三面旋转的护盾。
 * The model of the Herrscher of the Void: a humanoid with the shape of a player, with an outer layer, a pair of wings and three shields that turn.
 *
 * <p>全部部件使用同一张 128x128 的贴图。下面的数值逐项来自 ExtraMeteorP 1.12 {@code ModelVoidHerrscher}。
 * <p>All parts use the same 128x128 texture. The values below come item by item from ExtraMeteorP 1.12 {@code ModelVoidHerrscher}.
 */
public class VoidHerrscherModel extends HumanoidModel<VoidHerrscher> {
	private static final int SHIELD_COUNT = 3;

	private final ModelPart[] outerLayer;
	private final ModelPart[] bodyParts;
	private final ModelPart[] wings;
	private final ModelPart[] shields = new ModelPart[SHIELD_COUNT];

	public VoidHerrscherModel() {
		this(createLayer().bakeRoot());
	}

	private VoidHerrscherModel(ModelPart root) {
		super(root);
		outerLayer = new ModelPart[] {
				root.getChild("body_wear"), root.getChild("left_arm_wear"), root.getChild("right_arm_wear"),
				root.getChild("left_leg_wear"), root.getChild("right_leg_wear") };
		bodyParts = new ModelPart[] { body, leftArm, rightArm, leftLeg, rightLeg };
		wings = new ModelPart[] { root.getChild("left_wing"), root.getChild("right_wing") };
		for (int i = 0; i < SHIELD_COUNT; i++) {
			shields[i] = root.getChild("shield" + (i + 1));
		}
	}

	private static LayerDefinition createLayer() {
		MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0F);
		PartDefinition root = mesh.getRoot();
		CubeDeformation outer = new CubeDeformation(0.25F);
		root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(32, 48).addBox(-1F, -2F, -2F, 4, 12, 4), PartPose.offset(5F, 2F, 0F));
		root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 48).addBox(-2F, 0F, -2F, 4, 12, 4), PartPose.offset(1.9F, 12F, 0F));
		root.addOrReplaceChild("left_arm_wear", CubeListBuilder.create().texOffs(48, 48).addBox(-1F, -2F, -2F, 4, 12, 4, outer), PartPose.offset(5F, 2F, 0F));
		root.addOrReplaceChild("right_arm_wear", CubeListBuilder.create().texOffs(40, 32).addBox(-3F, -2F, -2F, 4, 12, 4, outer), PartPose.offset(-5F, 2F, 0F));
		root.addOrReplaceChild("left_leg_wear", CubeListBuilder.create().texOffs(0, 48).addBox(-2F, 0F, -2F, 4, 12, 4, outer), PartPose.offset(1.9F, 12F, 0F));
		root.addOrReplaceChild("right_leg_wear", CubeListBuilder.create().texOffs(0, 32).addBox(-2F, 0F, -2F, 4, 12, 4, outer), PartPose.offset(-1.9F, 12F, 0F));
		root.addOrReplaceChild("body_wear", CubeListBuilder.create().texOffs(16, 32).addBox(-4F, 0F, -2F, 8, 12, 4, outer), PartPose.ZERO);
		root.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(0, 64).addBox(0F, 0F, 0F, 26, 32, 0),
				PartPose.offsetAndRotation(-30F, -11F, 5F, 0F, 0F, -0.1745329F));
		root.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(52, 64).addBox(0F, 0F, 0F, 26, 32, 0),
				PartPose.offsetAndRotation(4F, -15.8F, 5F, 0F, 0F, 0.1745329F));
		for (int i = 1; i <= SHIELD_COUNT; i++) {
			root.addOrReplaceChild("shield" + i, CubeListBuilder.create().texOffs(0, 113).addBox(-6F, -1F, 9F, 12, 11, 0), PartPose.ZERO);
		}
		return LayerDefinition.create(mesh, 128, 128);
	}

	@Override
	public void setupAnim(VoidHerrscher boss, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		super.setupAnim(boss, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		for (int i = 0; i < outerLayer.length; i++) {
			outerLayer[i].copyFrom(bodyParts[i]);
		}
		// 护盾均匀地排在身体周围，并且来回摆动。
		// The shields are at equal angles around the body, and they swing back and forth.
		int count = boss.getShields();
		for (int i = 0; i < SHIELD_COUNT; i++) {
			shields[i].visible = i < count;
			if (count > 0) {
				shields[i].yRot = (float) (Math.sin(ageInTicks * 0.1F) + Math.PI * 2 / count * (i + 1));
			}
		}
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
		super.renderToBuffer(poseStack, buffer, packedLight, packedOverlay, color);
		for (ModelPart part : outerLayer) {
			part.render(poseStack, buffer, packedLight, packedOverlay, color);
		}
	}

	public void renderWings(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay) {
		for (ModelPart wing : wings) {
			wing.render(poseStack, buffer, packedLight, packedOverlay);
		}
	}

	public void renderShields(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
		for (ModelPart shield : shields) {
			shield.render(poseStack, buffer, packedLight, packedOverlay, color);
		}
	}
}
