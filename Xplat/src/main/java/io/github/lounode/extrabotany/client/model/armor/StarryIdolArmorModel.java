package io.github.lounode.extrabotany.client.model.armor;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class StarryIdolArmorModel {
	public static MeshDefinition createNormalMesh() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		partdefinition.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
		partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.ZERO);
		partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.ZERO);

		PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition headgear = head.addOrReplaceChild("headgear", CubeListBuilder.create().texOffs(36, 33).addBox(-5.0F, -9.0F, -1.0F, 10.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition hairB_r1 = headgear.addOrReplaceChild("hairB_r1", CubeListBuilder.create().texOffs(9, 33).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 12.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, -8.0F, 1.0F, 0.1745F, 0.0F, -0.2618F));

		PartDefinition hairA_r1 = headgear.addOrReplaceChild("hairA_r1", CubeListBuilder.create().texOffs(0, 33).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 12.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, -8.0F, 3.0F, 0.1745F, 0.0F, 0.2793F));

		PartDefinition flowerB_r1 = headgear.addOrReplaceChild("flowerB_r1", CubeListBuilder.create().texOffs(27, 33).addBox(0.0F, -3.0F, -1.5F, 1.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, -7.0F, 1.0F, 0.0F, 0.0F, -0.1745F));

		PartDefinition flowerA_r1 = headgear.addOrReplaceChild("flowerA_r1", CubeListBuilder.create().texOffs(18, 33).addBox(-1.0F, -3.0F, -0.5F, 1.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, -7.0F, 0.0F, 0.0F, 0.0F, 0.1745F));

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition suit = body.addOrReplaceChild("suit", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition chest1_r1 = suit.addOrReplaceChild("chest1_r1", CubeListBuilder.create().texOffs(0, 48).addBox(-4.0F, -2.5F, -2.0F, 6.0F, 3.0F, 3.0F, new CubeDeformation(0.5F)), PartPose.offsetAndRotation(1.0F, 3.0F, -1.0F, 0.6109F, 0.0F, 0.0F));

		PartDefinition cape_r1 = suit.addOrReplaceChild("cape_r1", CubeListBuilder.create().texOffs(65, 48).addBox(-8.5F, -1.5F, 0.0F, 8.0F, 12.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.5F, 1.5F, 2.85F, 0.1745F, 0.0F, 0.0F));

		PartDefinition right_leg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12.0F, 0.0F));

		PartDefinition right_boot = right_leg.addOrReplaceChild("right_boot", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition left_leg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12.0F, 0.0F));

		PartDefinition left_boot = left_leg.addOrReplaceChild("left_boot", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		return meshdefinition;
	}

	public static MeshDefinition createDressMesh() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		partdefinition.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
		partdefinition.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
		partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.ZERO);
		partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.ZERO);
		partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.ZERO);
		partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.ZERO);
		var body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);

		PartDefinition dress = body.addOrReplaceChild("dress", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition DressSideB_r1 = dress.addOrReplaceChild("DressSideB_r1", CubeListBuilder.create().texOffs(42, 65).addBox(0.0F, 0.0F, -2.0F, 1.0F, 6.0F, 3.0F, new CubeDeformation(0.35F)), PartPose.offsetAndRotation(3.0F, -12.0F, 0.0F, 0.0F, 0.0F, -0.3927F));

		PartDefinition DressSideA_r1 = dress.addOrReplaceChild("DressSideA_r1", CubeListBuilder.create().texOffs(33, 65).addBox(-0.5F, 1.0F, -1.5F, 1.0F, 6.0F, 3.0F, new CubeDeformation(0.35F)), PartPose.offsetAndRotation(-4.0F, -13.0F, 0.0F, 0.0F, 0.0F, 0.3927F));

		PartDefinition DressSide4_r1 = dress.addOrReplaceChild("DressSide4_r1", CubeListBuilder.create().texOffs(75, 65).addBox(-2.0F, -1.0F, 0.0F, 3.0F, 7.0F, 1.0F, new CubeDeformation(0.35F)), PartPose.offsetAndRotation(4.0F, -11.0F, 1.0F, 0.3054F, 0.5236F, 0.0F));

		PartDefinition DressSide3_r1 = dress.addOrReplaceChild("DressSide3_r1", CubeListBuilder.create().texOffs(66, 65).addBox(-1.5F, -1.0F, 0.0F, 3.0F, 7.0F, 1.0F, new CubeDeformation(0.35F)), PartPose.offsetAndRotation(4.0F, -11.0F, -2.0F, -0.3054F, -0.6981F, 0.0F));

		PartDefinition DressSide2_r1 = dress.addOrReplaceChild("DressSide2_r1", CubeListBuilder.create().texOffs(24, 65).addBox(-1.5F, -1.0F, -0.5F, 3.0F, 7.0F, 1.0F, new CubeDeformation(0.35F)), PartPose.offsetAndRotation(-4.0F, -11.0F, 2.0F, 0.3054F, -0.5236F, 0.0F));

		PartDefinition DressSide1_r1 = dress.addOrReplaceChild("DressSide1_r1", CubeListBuilder.create().texOffs(15, 65).addBox(-1.5F, -1.0F, 0.0F, 3.0F, 7.0F, 1.0F, new CubeDeformation(0.35F)), PartPose.offsetAndRotation(-4.0F, -11.0F, -2.0F, -0.3054F, 0.5236F, 0.0F));

		PartDefinition DressBack_r1 = dress.addOrReplaceChild("DressBack_r1", CubeListBuilder.create().texOffs(51, 65).addBox(-0.1F, -12.0F, -4.0F, 6.0F, 7.0F, 1.0F, new CubeDeformation(0.35F)), PartPose.offsetAndRotation(-2.9F, -0.88F, 8.0F, 0.1745F, 0.0F, 0.0F));

		PartDefinition DressFront_r1 = dress.addOrReplaceChild("DressFront_r1", CubeListBuilder.create().texOffs(0, 65).addBox(-0.1F, -12.0F, -3.9907F, 6.0F, 7.0F, 1.0F, new CubeDeformation(0.35F)), PartPose.offsetAndRotation(-2.9F, 0.34F, -1.1294F, -0.1745F, 0.0F, 0.0F));

		return meshdefinition;
	}
}
