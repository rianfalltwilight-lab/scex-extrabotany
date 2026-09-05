package io.github.lounode.extrabotany.client.model.armor;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class PleiadesCombatMaidArmorModel {

	public static MeshDefinition createNormalMesh() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		partdefinition.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
		PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition headgear = head.addOrReplaceChild("headgear", CubeListBuilder.create().texOffs(1, 61).addBox(-5.0F, -9.0F, -1.0F, 10.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition right_ear_r1 = headgear.addOrReplaceChild("right_ear_r1", CubeListBuilder.create().texOffs(12, 55).addBox(-2.0F, -2.0F, -1.0F, 3.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, -9.0F, -2.0F, 0.0F, 0.0F, -0.8727F));

		PartDefinition left_ear_r1 = headgear.addOrReplaceChild("left_ear_r1", CubeListBuilder.create().texOffs(1, 55).addBox(-1.0F, -2.0F, -1.0F, 3.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, -9.0F, -2.0F, 0.0F, 0.0F, 0.8727F));

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition suit = body.addOrReplaceChild("suit", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition chest_r1 = suit.addOrReplaceChild("chest_r1", CubeListBuilder.create().texOffs(1, 67).addBox(-1.0F, -4.5F, -2.0F, 6.0F, 4.0F, 3.0F, new CubeDeformation(0.5F)), PartPose.offsetAndRotation(-2.0F, 5.0F, -1.0F, 0.192F, 0.0F, 0.0F));

		PartDefinition right_arm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));

		PartDefinition right_sleeve = right_arm.addOrReplaceChild("right_sleeve", CubeListBuilder.create().texOffs(24, 75).addBox(-4.0F, -2.0F, -3.0F, 5.0F, 4.0F, 6.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition left_arm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));

		PartDefinition left_sleeve = left_arm.addOrReplaceChild("left_sleeve", CubeListBuilder.create().texOffs(1, 75).addBox(-1.0F, -2.0F, -3.0F, 5.0F, 4.0F, 6.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition right_leg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12.0F, 0.0F));

		PartDefinition right_sock = right_leg.addOrReplaceChild("right_sock", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition left_leg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12.0F, 0.0F));

		PartDefinition left_sock = left_leg.addOrReplaceChild("left_sock", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 0.0F, 0.0F));

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

		PartDefinition front = dress.addOrReplaceChild("front", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -12.0F, -5.0F, -0.3054F, 0.0F, 0.0F));

		PartDefinition DressFront_r1 = front.addOrReplaceChild("DressFront_r1", CubeListBuilder.create().texOffs(20, 48).addBox(-4.0F, -5.0F, -0.5F, 8.0F, 5.0F, 1.0F, new CubeDeformation(0.35F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363F, 0.0F, 0.0F));

		PartDefinition DressFrontPlus_r1 = front.addOrReplaceChild("DressFrontPlus_r1", CubeListBuilder.create().texOffs(1, 32).addBox(-2.0F, -5.0F, -0.7F, 6.0F, 4.0F, 1.0F, new CubeDeformation(0.35F)), PartPose.offsetAndRotation(-1.0F, 0.0F, 0.0F, -0.48F, 0.0F, 0.0F));

		PartDefinition backend = dress.addOrReplaceChild("backend", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -12.0298F, 5.0203F, 0.2618F, 0.0F, 0.0F));

		PartDefinition DressBack_r1 = backend.addOrReplaceChild("DressBack_r1", CubeListBuilder.create().texOffs(1, 48).addBox(-4.0F, -3.038F, -0.5216F, 8.0F, 5.0F, 1.0F, new CubeDeformation(0.35F)), PartPose.offsetAndRotation(0.0F, -1.9702F, -1.0203F, 0.5236F, 0.0F, 0.0F));

		PartDefinition DressBackPlus_r1 = backend.addOrReplaceChild("DressBackPlus_r1", CubeListBuilder.create().texOffs(16, 32).addBox(-3.0F, -5.0F, 0.1F, 6.0F, 4.0F, 1.0F, new CubeDeformation(0.35F)), PartPose.offsetAndRotation(0.0F, 0.0298F, -0.5203F, 0.5236F, 0.0F, 0.0F));

		PartDefinition left_cloth = dress.addOrReplaceChild("left_cloth", CubeListBuilder.create(), PartPose.offset(2.0F, -1.0F, 0.0F));

		PartDefinition cloth_r1 = left_cloth.addOrReplaceChild("cloth_r1", CubeListBuilder.create().texOffs(1, 38).addBox(-0.5088F, -4.0341F, -2.0F, 1.0F, 5.0F, 4.0F, new CubeDeformation(0.35F)), PartPose.offsetAndRotation(3.0F, -11.0F, 0.0F, 0.0F, 0.0F, -0.3491F));

		PartDefinition right_cloth = dress.addOrReplaceChild("right_cloth", CubeListBuilder.create(), PartPose.offset(-2.0F, -1.0F, 0.0F));

		PartDefinition cloth_r2 = right_cloth.addOrReplaceChild("cloth_r2", CubeListBuilder.create().texOffs(1, 38).addBox(-0.4912F, -4.0341F, -2.0F, 1.0F, 5.0F, 4.0F, new CubeDeformation(0.35F)), PartPose.offsetAndRotation(-3.0F, -11.0F, 0.0F, 0.0F, 0.0F, 0.3491F));

		return meshdefinition;
	}
}
