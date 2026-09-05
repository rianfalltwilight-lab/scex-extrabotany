package io.github.lounode.extrabotany.client.model.armor;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class ShadowWarriorArmorModel {

	public static MeshDefinition createNormalMesh() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		partdefinition.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);

		PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition helmet = head.addOrReplaceChild("helmet", CubeListBuilder.create().texOffs(0, 33).addBox(-5.5F, -9.0F, -5.0F, 9.0F, 3.0F, 9.0F, new CubeDeformation(0.1F))
				.texOffs(97, 33).addBox(-4.0F, -8.5F, -6.0F, 6.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(7, 50).addBox(-2.0F, -12.0F, -5.5F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, 0.0F, 0.0F));

		PartDefinition Ear2_r1 = helmet.addOrReplaceChild("Ear2_r1", CubeListBuilder.create().texOffs(44, 50).addBox(-2.0F, -2.0F, -1.0F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.4356F, -5.194F, -4.0F, 0.0F, 0.0F, -0.3491F));

		PartDefinition Ear1_r1 = helmet.addOrReplaceChild("Ear1_r1", CubeListBuilder.create().texOffs(14, 50).addBox(-2.0F, -2.0F, -1.0F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.4356F, -5.194F, -4.0F, 0.0F, 0.0F, 0.3491F));

		PartDefinition Hair3_r1 = helmet.addOrReplaceChild("Hair3_r1", CubeListBuilder.create().texOffs(0, 50).addBox(-2.0F, -2.0F, 1.0F, 2.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.5F, -11.0F, -6.0F, 0.0F, 0.2618F, -0.4363F));

		PartDefinition Hair1_r1 = helmet.addOrReplaceChild("Hair1_r1", CubeListBuilder.create().texOffs(53, 50).addBox(-2.0F, -2.0F, 0.5F, 2.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, -11.0F, -5.0F, -0.1745F, 0.0F, 0.3927F));

		PartDefinition Head4_r1 = helmet.addOrReplaceChild("Head4_r1", CubeListBuilder.create().texOffs(23, 50).addBox(-0.5F, 0.0F, -5.0F, 1.0F, 6.0F, 9.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(-5.0F, -7.5F, 0.0F, 0.0F, 0.0F, 0.3491F));

		PartDefinition Head3_r1 = helmet.addOrReplaceChild("Head3_r1", CubeListBuilder.create().texOffs(76, 33).addBox(-1.5F, 0.0F, -5.0F, 1.0F, 6.0F, 9.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(4.0F, -7.5F, 0.0F, 0.0F, 0.0F, -0.3491F));

		PartDefinition Mouse2_r1 = helmet.addOrReplaceChild("Mouse2_r1", CubeListBuilder.create().texOffs(67, 33).addBox(-2.0F, -2.0F, -1.0F, 3.0F, 5.0F, 1.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(2.5F, -4.0F, -1.0F, 0.0F, -0.7854F, 0.0F));

		PartDefinition Mouse1_r1 = helmet.addOrReplaceChild("Mouse1_r1", CubeListBuilder.create().texOffs(58, 33).addBox(-2.0F, -2.0F, -1.0F, 3.0F, 5.0F, 1.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(-4.5F, -4.0F, -1.5F, 0.0F, 0.7854F, 0.0F));

		PartDefinition Head2_r1 = helmet.addOrReplaceChild("Head2_r1", CubeListBuilder.create().texOffs(37, 33).addBox(-4.5F, -4.5F, -0.9879F, 9.0F, 7.0F, 1.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(-1.0F, -4.9122F, 5.8379F, 0.3491F, 0.0F, 0.0F));

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition chestplate = body.addOrReplaceChild("chestplate", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.5F))
				.texOffs(0, 84).addBox(-3.0F, 1.0F, -3.0F, 6.0F, 7.0F, 1.0F, new CubeDeformation(0.5F))
				.texOffs(0, 102).addBox(-3.0F, 1.0F, 2.0F, 6.0F, 10.0F, 1.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition Lexicon_r1 = chestplate.addOrReplaceChild("Lexicon_r1", CubeListBuilder.create().texOffs(0, 93).addBox(-2.0F, -2.25F, -2.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.5F)), PartPose.offsetAndRotation(0.5F, 4.0F, 5.0F, 0.0F, 0.0F, 0.7854F));

		PartDefinition right_arm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));

		PartDefinition right_gardebras = right_arm.addOrReplaceChild("right_gardebras", CubeListBuilder.create().texOffs(15, 84).addBox(-4.0F, -3.5F, -2.5F, 5.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
				.texOffs(36, 84).addBox(-4.0F, 4.0F, -2.0F, 2.0F, 5.0F, 4.0F, new CubeDeformation(0.3F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition ArmArmor2_r1 = right_gardebras.addOrReplaceChild("ArmArmor2_r1", CubeListBuilder.create().texOffs(49, 84).addBox(-0.5F, 2.0F, 0.0F, 1.0F, 6.0F, 4.0F, new CubeDeformation(0.3F)), PartPose.offsetAndRotation(-2.0F, -0.5F, -2.0F, 0.0F, 0.0F, 0.3491F));

		PartDefinition ArmArmor1_r1 = right_gardebras.addOrReplaceChild("ArmArmor1_r1", CubeListBuilder.create().texOffs(60, 84).addBox(-0.5F, 2.0F, 0.0F, 1.0F, 5.0F, 4.0F, new CubeDeformation(0.3F)), PartPose.offsetAndRotation(-2.0F, -3.5F, -2.0F, 0.0F, 0.0F, 0.3491F));

		PartDefinition left_arm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));

		PartDefinition left_gardebars = left_arm.addOrReplaceChild("left_gardebars", CubeListBuilder.create().texOffs(15, 96).addBox(-1.0F, -3.5F, -2.5F, 5.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
				.texOffs(58, 96).addBox(2.0F, 4.0F, -2.0F, 2.0F, 5.0F, 4.0F, new CubeDeformation(0.3F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition ArmArmor2Left_r1 = left_gardebars.addOrReplaceChild("ArmArmor2Left_r1", CubeListBuilder.create().texOffs(49, 84).addBox(-0.5F, 2.0F, 0.0F, 1.0F, 6.0F, 4.0F, new CubeDeformation(0.3F)), PartPose.offsetAndRotation(2.0F, -0.5F, -2.0F, 0.0F, 0.0F, -0.3491F));

		PartDefinition ArmArmor1Left_r1 = left_gardebars.addOrReplaceChild("ArmArmor1Left_r1", CubeListBuilder.create().texOffs(36, 96).mirror().addBox(-0.5F, 2.0F, 0.0F, 1.0F, 5.0F, 4.0F, new CubeDeformation(0.3F)).mirror(false), PartPose.offsetAndRotation(2.0F, -3.5F, -2.0F, 0.0F, 0.0F, -0.3491F));

		PartDefinition right_leg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12.0F, 0.0F));

		PartDefinition right_leggings = right_leg.addOrReplaceChild("right_leggings", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition RightLeg6_r1 = right_leggings.addOrReplaceChild("RightLeg6_r1", CubeListBuilder.create().texOffs(0, 74).addBox(-1.0F, -2.0F, -3.0F, 1.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, 6.0F, 1.0F, 0.0F, 0.0F, 0.3491F));

		PartDefinition RightLeg5_r1 = right_leggings.addOrReplaceChild("RightLeg5_r1", CubeListBuilder.create().texOffs(0, 74).addBox(-1.0F, -2.0F, -3.0F, 1.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, 4.0F, 1.0F, 0.0F, 0.0F, 0.3491F));

		PartDefinition RightLeg4_r1 = right_leggings.addOrReplaceChild("RightLeg4_r1", CubeListBuilder.create().texOffs(0, 74).addBox(-1.0F, -2.0F, -3.0F, 1.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, 2.0F, 1.0F, 0.0F, 0.0F, 0.3491F));

		PartDefinition RightLeg3_r1 = right_leggings.addOrReplaceChild("RightLeg3_r1", CubeListBuilder.create().texOffs(13, 67).addBox(-2.5F, -2.0F, 0.0F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.1F, 5.0F, -3.0F, -0.4363F, 0.0F, 0.0F));

		PartDefinition RightLeg2_r1 = right_leggings.addOrReplaceChild("RightLeg2_r1", CubeListBuilder.create().texOffs(13, 67).addBox(-2.5F, -2.0F, 0.0F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.1F, 3.0F, -3.0F, -0.4363F, 0.0F, 0.0F));

		PartDefinition RightLeg1_r1 = right_leggings.addOrReplaceChild("RightLeg1_r1", CubeListBuilder.create().texOffs(13, 67).addBox(-2.5F, -2.0F, 0.0F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.1F, 1.0F, -3.0F, -0.4363F, 0.0F, 0.0F));

		PartDefinition right_boot = right_leg.addOrReplaceChild("right_boot", CubeListBuilder.create().texOffs(0, 114).addBox(-4.0F, -4.0F, -2.5F, 4.0F, 4.0F, 5.0F, new CubeDeformation(0.3F))
				.texOffs(19, 114).addBox(-3.0F, -3.0F, -3.5F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(1.9F, 12.0F, 0.0F));

		PartDefinition left_leg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12.0F, 0.0F));

		PartDefinition left_leggings = left_leg.addOrReplaceChild("left_leggings", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition LeftLeg6_r1 = left_leggings.addOrReplaceChild("LeftLeg6_r1", CubeListBuilder.create().texOffs(0, 74).addBox(0.0F, -2.0F, -3.0F, 1.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, 6.0F, 1.0F, 0.0F, 0.0F, -0.3491F));

		PartDefinition LeftLeg5_r1 = left_leggings.addOrReplaceChild("LeftLeg5_r1", CubeListBuilder.create().texOffs(0, 74).addBox(0.0F, -2.0F, -3.0F, 1.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, 4.0F, 1.0F, 0.0F, 0.0F, -0.3491F));

		PartDefinition LeftLeg4_r1 = left_leggings.addOrReplaceChild("LeftLeg4_r1", CubeListBuilder.create().texOffs(0, 74).addBox(0.0F, -2.0F, -3.0F, 1.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, 2.0F, 1.0F, 0.0F, 0.0F, -0.3491F));

		PartDefinition LeftLeg3_r1 = left_leggings.addOrReplaceChild("LeftLeg3_r1", CubeListBuilder.create().texOffs(22, 67).addBox(-2.5F, -2.0F, 0.0F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.1F, 5.0F, -3.0F, -0.4363F, 0.0F, 0.0F));

		PartDefinition LeftLeg2_r1 = left_leggings.addOrReplaceChild("LeftLeg2_r1", CubeListBuilder.create().texOffs(22, 67).addBox(-2.5F, -2.0F, 0.0F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.1F, 3.0F, -3.0F, -0.4363F, 0.0F, 0.0F));

		PartDefinition LeftLeg1_r1 = left_leggings.addOrReplaceChild("LeftLeg1_r1", CubeListBuilder.create().texOffs(22, 67).addBox(-2.5F, -2.0F, 0.0F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.1F, 1.0F, -3.0F, -0.4363F, 0.0F, 0.0F));

		PartDefinition left_boot = left_leg.addOrReplaceChild("left_boot", CubeListBuilder.create().texOffs(0, 114).addBox(0.0F, -4.0F, -3.5F, 4.0F, 4.0F, 5.0F, new CubeDeformation(0.3F))
				.texOffs(19, 114).addBox(0.0F, -3.0F, -4.5F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.9F, 12.0F, 1.0F));

		return meshdefinition;
	}

	public static MeshDefinition createLeggingsMesh() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		partdefinition.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
		partdefinition.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
		partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.ZERO);
		partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.ZERO);
		partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.ZERO);
		partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.ZERO);

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);

		PartDefinition leggings = body.addOrReplaceChild("leggings", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition leggings_front = leggings.addOrReplaceChild("leggings_front", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition LegsFront3_r1 = leggings_front.addOrReplaceChild("LegsFront3_r1", CubeListBuilder.create().texOffs(0, 67).addBox(-2.5F, -2.0F, 0.0F, 5.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.0F, -3.0F, -0.4363F, 0.0F, 0.0F));

		PartDefinition LegsFront2_r1 = leggings_front.addOrReplaceChild("LegsFront2_r1", CubeListBuilder.create().texOffs(0, 67).addBox(-2.5F, -2.0F, 0.0F, 5.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -8.0F, -3.0F, -0.4363F, 0.0F, 0.0F));

		PartDefinition LegsFront1_r1 = leggings_front.addOrReplaceChild("LegsFront1_r1", CubeListBuilder.create().texOffs(0, 67).addBox(-2.5F, -2.0F, 0.0F, 5.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -10.0F, -3.0F, -0.4363F, 0.0F, 0.0F));

		PartDefinition leggings_back = leggings.addOrReplaceChild("leggings_back", CubeListBuilder.create().texOffs(11, 74).addBox(-4.5F, -13.0F, 2.0F, 9.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(11, 74).addBox(-4.5F, -11.0F, 2.0F, 9.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(11, 74).addBox(-4.5F, -9.0F, 2.0F, 9.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		return meshdefinition;
	}
}
