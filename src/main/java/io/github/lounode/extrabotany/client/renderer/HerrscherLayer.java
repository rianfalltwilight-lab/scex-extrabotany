package io.github.lounode.extrabotany.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

import io.github.lounode.extrabotany.common.handler.HerrscherHandler;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 雷之律者的玩家渲染层。它在玩家的两条手臂上画出装甲，并在背后画出圣痕。
 * The player render layer of the Herrscher of Thunder. It draws armor on the two arms of the player and a stigma behind the back.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code LayerHerrscher} 和 {@code ModelHerrscher}。
 * <p>Source: ExtraMeteorP 1.16 {@code LayerHerrscher} and {@code ModelHerrscher}.
 */
public class HerrscherLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
	private static final ResourceLocation TEXTURE = prefix("textures/entity/herrscher.png");
	private static final int FULL_BRIGHT = 0xF000F0;
	private static final float ARM_OFFSET_Y = -0.4F;
	private static final float STIGMA_SCALE = 1.4F;
	private static final float STIGMA_OFFSET_Y = -0.2F;

	// 模型里的名称是从佩戴者的正面看的，所以 leftArmArmor 画在玩家模型的左臂上。
	// The names in the model are from a view of the front of the wearer, so leftArmArmor is drawn on the left arm of the player model.
	private final ModelPart leftArmArmor;
	private final ModelPart rightArmArmor;
	private final ModelPart stigma;

	public HerrscherLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer) {
		super(renderer);
		ModelPart root = createLayer().bakeRoot();
		this.leftArmArmor = root.getChild("rightArm");
		this.rightArmArmor = root.getChild("leftArm");
		this.stigma = root.getChild("back");
	}

	private static LayerDefinition createLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();

		PartDefinition rightArm = root.addOrReplaceChild("rightArm", CubeListBuilder.create()
				.texOffs(0, 13).addBox(0.6859F, 1.3201F, -2.6295F, 3.0F, 9.0F, 5.0F)
				.texOffs(11, 0).addBox(0.6859F, 2.3201F, -3.6295F, 2.0F, 7.0F, 1.0F)
				.texOffs(0, 0).addBox(0.6859F, 2.3201F, 2.3705F, 2.0F, 7.0F, 1.0F)
				.texOffs(0, 0).addBox(1.1859F, 3.3201F, -4.6295F, 1.0F, 4.0F, 9.0F)
				.texOffs(12, 5).addBox(2.1859F, 3.3201F, -4.1295F, 1.0F, 2.0F, 8.0F)
				.texOffs(17, 0).addBox(3.1859F, 1.3201F, -1.6295F, 1.0F, 2.0F, 3.0F)
				.texOffs(22, 0).addBox(3.1859F, 3.3201F, -0.6295F, 1.0F, 1.0F, 1.0F)
				.texOffs(16, 16).addBox(0.1859F, 10.3201F, -1.1295F, 3.0F, 3.0F, 3.0F)
				.texOffs(11, 15).addBox(0.1859F, 10.3201F, -2.1295F, 3.0F, 2.0F, 1.0F)
				.texOffs(16, 7).addBox(1.1859F, 4.3201F, 4.3705F, 1.0F, 1.0F, 1.0F)
				.texOffs(5, 7).addBox(1.1859F, 4.3201F, -5.6295F, 1.0F, 1.0F, 1.0F),
				PartPose.offsetAndRotation(5.75F, 1.0F, 2.75F, -0.4363F, 0.0F, -0.3491F));
		rightArm.addOrReplaceChild("bone2", CubeListBuilder.create()
				.texOffs(0, 13).addBox(-0.2835F, -2.991F, -0.5F, 1.0F, 4.0F, 1.0F),
				PartPose.offsetAndRotation(3.6859F, 1.3201F, -0.1295F, 0.0F, 0.0F, 0.7854F));
		PartDefinition katanaSheath = rightArm.addOrReplaceChild("katanaSheath", CubeListBuilder.create()
				.texOffs(31, 49).addBox(-0.1555F, -2.0137F, -1.1343F, 1.0F, 2.0F, 13.0F),
				PartPose.offsetAndRotation(1.2474F, 13.2096F, 0.6816F, -0.0873F, 0.0F, -0.0873F));
		katanaSheath.addOrReplaceChild("bone4", CubeListBuilder.create()
				.texOffs(46, 52).addBox(-0.1555F, -2.2105F, -1.1147F, 1.0F, 2.0F, 8.0F),
				PartPose.offsetAndRotation(0.0F, 0.0F, 13.0F, 0.1745F, 0.0F, 0.0F));
		rightArm.addOrReplaceChild("bone3", CubeListBuilder.create()
				.texOffs(16, 22).addBox(-2.0F, -2.0F, -2.0F, 2.0F, 3.0F, 3.0F)
				.texOffs(22, 2).addBox(0.0F, -1.0F, -2.0F, 1.0F, 2.0F, 3.0F),
				PartPose.offsetAndRotation(2.1859F, 0.5701F, 0.3705F, 0.0F, 0.0F, -0.2618F));

		PartDefinition leftArm = root.addOrReplaceChild("leftArm", CubeListBuilder.create()
				.texOffs(30, 0).addBox(-1.0F, -1.0F, -3.0F, 3.0F, 10.0F, 4.0F)
				.texOffs(44, 0).addBox(-2.0F, 1.0F, -2.0F, 1.0F, 4.0F, 2.0F)
				.texOffs(30, 20).addBox(-1.0F, -3.0F, -3.0F, 3.0F, 2.0F, 1.0F)
				.texOffs(30, 23).addBox(-1.0F, -3.0F, 0.0F, 3.0F, 2.0F, 1.0F)
				.texOffs(44, 6).addBox(-1.0F, -4.0F, -2.0F, 3.0F, 1.0F, 2.0F)
				.texOffs(54, 0).addBox(-0.6381F, -0.7174F, -4.039F, 2.0F, 5.0F, 1.0F)
				.texOffs(54, 6).addBox(-0.4238F, -0.7607F, 0.9059F, 2.0F, 5.0F, 1.0F),
				PartPose.offsetAndRotation(-8.0F, 3.0F, 2.5F, -0.4363F, 0.0F, 0.3491F));
		PartDefinition bone5 = leftArm.addOrReplaceChild("bone5", CubeListBuilder.create()
				.texOffs(30, 14).addBox(-3.1094F, -0.3639F, -1.0F, 3.0F, 2.0F, 2.0F),
				PartPose.offsetAndRotation(0.0F, 0.0F, -1.0F, 0.0F, 0.0F, 0.5236F));
		bone5.addOrReplaceChild("bone6", CubeListBuilder.create()
				.texOffs(30, 18).addBox(-2.7531F, -1.3651F, -0.5F, 4.0F, 1.0F, 1.0F),
				PartPose.offsetAndRotation(-3.9575F, 1.2207F, 0.0F, 0.0F, 0.0F, 0.3491F));
		PartDefinition katana = leftArm.addOrReplaceChild("katana", CubeListBuilder.create()
				.texOffs(31, 13).addBox(-0.1555F, -2.0137F, -1.1343F, 1.0F, 2.0F, 13.0F)
				.texOffs(46, 14).addBox(-0.1596F, -1.9492F, -7.5283F, 1.0F, 2.0F, 3.0F)
				.texOffs(46, 9).addBox(-1.6025F, -3.4558F, -0.4588F, 4.0F, 5.0F, 0.0F),
				PartPose.offsetAndRotation(1.0F, 8.0F, -4.0F, -0.0873F, 3.0543F, 0.0F));
		katana.addOrReplaceChild("bone7", CubeListBuilder.create()
				.texOffs(46, 16).addBox(-0.1555F, -2.2105F, -1.1147F, 1.0F, 2.0F, 8.0F),
				PartPose.offsetAndRotation(0.0F, 0.0F, 13.0F, 0.1745F, 0.0F, 0.0F));

		root.addOrReplaceChild("back", CubeListBuilder.create()
				.texOffs(0, 28).addBox(-16.0F, -34.0F, 10.0F, 32.0F, 21.0F, 0.0F),
				PartPose.offset(0.0F, 24.0F, 0.0F));
		return LayerDefinition.create(mesh, 64, 64);
	}

	@Override
	public void render(PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer player,
			float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
		if (player.isInvisible() || !HerrscherHandler.isHerrscherOfThunder(player)) {
			return;
		}
		VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
		PlayerModel<AbstractClientPlayer> model = getParentModel();

		pose.pushPose();
		pose.translate(0F, ARM_OFFSET_Y, 0F);
		model.leftArm.translateAndRotate(pose);
		leftArmArmor.render(pose, buffer, FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
		pose.popPose();

		pose.pushPose();
		pose.translate(0F, ARM_OFFSET_Y, 0F);
		model.rightArm.translateAndRotate(pose);
		rightArmArmor.render(pose, buffer, FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
		pose.popPose();

		pose.pushPose();
		pose.scale(STIGMA_SCALE, STIGMA_SCALE, STIGMA_SCALE);
		pose.translate(0F, STIGMA_OFFSET_Y, 0F);
		model.body.translateAndRotate(pose);
		stigma.render(pose, buffer, FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
		pose.popPose();
	}
}
