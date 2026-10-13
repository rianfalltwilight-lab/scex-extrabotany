package io.github.lounode.extrabotany.client.renderer.blockentity;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
/**
 * 量子魔力缓存器的模型。
 * The model of the Quantum Mana Buffer.
 *
 * <p>下面的数值逐项来自 ExtraMeteorP 1.12 {@code ModelQuantumManaBuffer}（提交 4ad2cc9b）。部件的名字和原版相同。
 * <p>The values below come item by item from ExtraMeteorP 1.12 {@code ModelQuantumManaBuffer} (commit 4ad2cc9b). The names of the parts are the same as in the original.
 */
final class QuantumManaBufferModel {
	static ModelPart create() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		root.addOrReplaceChild("Shape1", CubeListBuilder.create().texOffs(0, 0).addBox(0F, 0F, 0F, 4, 14, 4, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(3F, 10F, -7F, 0F, 0F, 0F));
		root.addOrReplaceChild("Shape2", CubeListBuilder.create().texOffs(0, 0).addBox(0F, 0F, 0F, 4, 14, 4, new CubeDeformation(0F)), PartPose.offsetAndRotation(3F, 10F, -2F, 0F, 0F, 0F));
		root.addOrReplaceChild("Shape3", CubeListBuilder.create().texOffs(0, 0).addBox(0F, 0F, 0F, 4, 14, 4, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(3F, 10F, 3F, 0F, 0F, 0F));
		root.addOrReplaceChild("Shape4", CubeListBuilder.create().texOffs(0, 0).addBox(0F, 0F, 0F, 4, 14, 4, new CubeDeformation(0F)), PartPose.offsetAndRotation(-7F, 10F, -7F, 0F, 0F, 0F));
		root.addOrReplaceChild("Shape5", CubeListBuilder.create().texOffs(0, 0).addBox(0F, 0F, 0F, 4, 14, 4, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(-2F, 10F, 3F, 0F, 0F, 0F));
		root.addOrReplaceChild("Shape6", CubeListBuilder.create().texOffs(0, 0).addBox(0F, 0F, 0F, 4, 14, 4, new CubeDeformation(0F)), PartPose.offsetAndRotation(-7F, 10F, 3F, 0F, 0F, 0F));
		root.addOrReplaceChild("Shape7", CubeListBuilder.create().texOffs(0, 0).addBox(0F, 0F, 0F, 4, 14, 4, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(-7F, 10F, -2F, 0F, 0F, 0F));
		root.addOrReplaceChild("Shape8", CubeListBuilder.create().texOffs(0, 0).addBox(0F, 0F, 0F, 4, 14, 4, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(-2F, 10F, -7F, 0F, 0F, 0F));
		root.addOrReplaceChild("Shape9", CubeListBuilder.create().texOffs(0, 0).addBox(0F, 0F, 0F, 4, 14, 4, new CubeDeformation(0F)), PartPose.offsetAndRotation(-2F, 10F, -2F, 0F, 0F, 0F));
		root.addOrReplaceChild("Shape10", CubeListBuilder.create().texOffs(17, 5).addBox(0F, 0F, 0F, 1, 2, 1, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(0F, 8.5F, -0.5F, 0F, 0F, 0F));
		root.addOrReplaceChild("Shape11", CubeListBuilder.create().texOffs(18, 5).addBox(0F, 0F, 0F, 1, 2, 1, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(-5F, 8.5F, -0.5F, 0F, 0F, 0F));
		root.addOrReplaceChild("Shape12", CubeListBuilder.create().texOffs(17, 5).addBox(0F, 0F, 0F, 1, 2, 1, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, 8.5F, 4.5F, 0F, 0F, 0F));
		root.addOrReplaceChild("Shape13", CubeListBuilder.create().texOffs(17, 5).addBox(0F, 0F, 0F, 1, 2, 1, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(5F, 8.5F, -0.5F, 0F, 0F, 0F));
		root.addOrReplaceChild("Shape14", CubeListBuilder.create().texOffs(17, 5).addBox(0F, 0F, 0F, 1, 2, 1, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, 8.5F, -5.5F, 0F, 0F, 0F));
		root.addOrReplaceChild("af", CubeListBuilder.create().texOffs(17, 5).addBox(0F, 0F, 0F, 1, 2, 1, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(5F, 8.5F, 4.5F, 0F, 0F, 0F));
		root.addOrReplaceChild("ag", CubeListBuilder.create().texOffs(17, 5).addBox(0F, 0F, 0F, 1, 2, 1, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(5F, 8.5F, -5.5F, 0F, 0F, 0F));
		root.addOrReplaceChild("at", CubeListBuilder.create().texOffs(17, 5).addBox(0F, 0F, 0F, 1, 2, 1, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(-5F, 8.5F, 4.5F, 0F, 0F, 0F));
		root.addOrReplaceChild("sd", CubeListBuilder.create().texOffs(17, 5).addBox(0F, 0F, 0F, 1, 2, 1, new CubeDeformation(0F)), PartPose.offsetAndRotation(-5F, 8.5F, -5.5F, 0F, 0F, 0F));
		root.addOrReplaceChild("sf", CubeListBuilder.create().texOffs(0, 25).addBox(0F, 0F, 0F, 11, 1, 1, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(-5F, 8F, -0.5F, 0F, 0F, 0F));
		root.addOrReplaceChild("sg", CubeListBuilder.create().texOffs(0, 25).addBox(0F, 0F, 0F, 11, 1, 1, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(-5F, 8F, 4.5F, 0F, 0F, 0F));
		root.addOrReplaceChild("a", CubeListBuilder.create().texOffs(0, 25).addBox(0F, 0F, 0F, 11, 1, 1, new CubeDeformation(0F)), PartPose.offsetAndRotation(-5F, 8F, -5.5F, 0F, 0F, 0F));
		root.addOrReplaceChild("b", CubeListBuilder.create().texOffs(0, 25).addBox(0F, 0F, 0F, 6, 1, 1, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(5F, 8F, 0.5F, 0F, 1.570796F, 0F));
		root.addOrReplaceChild("c", CubeListBuilder.create().texOffs(0, 25).addBox(0F, 0F, 0F, 6, 1, 1, new CubeDeformation(0F)), PartPose.offsetAndRotation(-5F, 8F, 5.5F, 0F, 1.570796F, 0F));
		root.addOrReplaceChild("d", CubeListBuilder.create().texOffs(17, 0).addBox(0F, 0F, 0F, 2, 1, 2, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(-1F, 7.5F, -1F, 0F, 0F, 0F));
		root.addOrReplaceChild("e", CubeListBuilder.create().texOffs(17, 0).addBox(-9F, 14F, -1F, 1, 2, 2, new CubeDeformation(0F)), PartPose.offsetAndRotation(1F, 6F, 0F, 0F, 0F, 0F));
		root.addOrReplaceChild("f", CubeListBuilder.create().texOffs(17, 0).addBox(-9F, 14F, -1F, 1, 2, 2, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(0F, 6F, -1F, 0F, 1.570796F, 0F));
		root.addOrReplaceChild("g", CubeListBuilder.create().texOffs(17, 0).addBox(-9F, 14F, 0F, 1, 2, 2, new CubeDeformation(0F)), PartPose.offsetAndRotation(1F, 6F, 1F, 0F, -1.570796F, 0F));
		root.addOrReplaceChild("w", CubeListBuilder.create().texOffs(17, 0).addBox(-7F, 14F, -1F, 1, 2, 2, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(1F, 6F, 0F, 0F, -3.141593F, 0F));
		root.addOrReplaceChild("y", CubeListBuilder.create().texOffs(0, 19).addBox(0F, -0.5F, 0F, 16, 1, 1, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(-8F, 13F, -8F, 0F, 0F, 0F));
		root.addOrReplaceChild("u", CubeListBuilder.create().texOffs(0, 19).addBox(0F, -0.5F, 0F, 16, 1, 1, new CubeDeformation(0F)), PartPose.offsetAndRotation(7F, 13F, 8F, 0F, 1.570796F, 0F));
		root.addOrReplaceChild("sh", CubeListBuilder.create().texOffs(0, 19).addBox(0F, -0.5F, 0F, 16, 1, 1, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(-8F, 13F, 8F, 0F, 1.570796F, 0F));
		root.addOrReplaceChild("df", CubeListBuilder.create().texOffs(0, 19).addBox(0F, -0.5F, 0F, 16, 1, 1, new CubeDeformation(0F)), PartPose.offsetAndRotation(-8F, 13F, 7F, 0F, 0F, 0F));
		root.addOrReplaceChild("fg", CubeListBuilder.create().texOffs(0, 22).addBox(0F, -0.5F, 0F, 16, 1, 1, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(-8F, 21F, -7.9F, 0F, 0F, 0F));
		root.addOrReplaceChild("ac", CubeListBuilder.create().texOffs(0, 22).addBox(0F, -0.5F, 0F, 16, 1, 1, new CubeDeformation(0F)), PartPose.offsetAndRotation(-7.9F, 21F, 8F, 0F, 1.570796F, 0F));
		root.addOrReplaceChild("ad", CubeListBuilder.create().texOffs(0, 22).addBox(0F, -0.5F, 0F, 16, 1, 1, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(-8F, 21F, 6.9F, 0F, 0F, 0F));
		root.addOrReplaceChild("ae", CubeListBuilder.create().texOffs(0, 22).addBox(0F, -0.5F, 0F, 16, 1, 1, new CubeDeformation(0F)), PartPose.offsetAndRotation(6.9F, 21F, 8F, 0F, 1.570796F, 0F));
		return LayerDefinition.create(mesh, 64, 64).bakeRoot();
	}
}
