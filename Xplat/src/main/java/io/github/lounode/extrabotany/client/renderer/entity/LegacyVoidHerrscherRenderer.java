package io.github.lounode.extrabotany.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.lounode.extrabotany.common.entity.gaia.LegacyVoidHerrscher;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public final class LegacyVoidHerrscherRenderer extends HumanoidMobRenderer<LegacyVoidHerrscher, LegacyVoidHerrscherRenderer.Model> {
    private static final ResourceLocation BODY = ResourceLocation.parse("extrabotany:textures/entity/voidherrscher.png");
    private static final ResourceLocation WING = ResourceLocation.parse("extrabotany:textures/entity/wing.png");
    public LegacyVoidHerrscherRenderer(EntityRendererProvider.Context context) {
        super(context, new Model(), 0); addLayer(new ExtraLayer(this, false)); addLayer(new ExtraLayer(this, true));
    }
    @Override public ResourceLocation getTextureLocation(LegacyVoidHerrscher entity) { return BODY; }
    @Override protected RenderType getRenderType(LegacyVoidHerrscher entity, boolean visible, boolean translucent, boolean glowing) { return RenderType.entityTranslucent(BODY); }

    private static final class ExtraLayer extends RenderLayer<LegacyVoidHerrscher, Model> {
        private final boolean shields;
        ExtraLayer(LegacyVoidHerrscherRenderer renderer, boolean shields) { super(renderer); this.shields = shields; }
        @Override public void render(PoseStack pose, MultiBufferSource buffers, int light, LegacyVoidHerrscher entity,
                float limb, float amount, float partial, float age, float yaw, float pitch) {
            if (shields ? entity.getRotatingShieldsRenderState() <= 0 : !entity.isRankIIRenderState()) return;
            var vertices = buffers.getBuffer(shields ? RenderType.entityTranslucent(WING) : RenderType.entityCutoutNoCull(WING));
            for (var part : shields ? getParentModel().shields : getParentModel().wings)
                part.render(pose, vertices, shields ? 0xF000F0 : light, OverlayTexture.NO_OVERLAY, shields ? 0x99FFFFFF : -1);
        }
    }
    /** Numeric mesh/UV mapping from the archived model; original textures are preserved verbatim. */
    public static final class Model extends HumanoidModel<LegacyVoidHerrscher> {
        final ModelPart[] wear, wings, shields;
        Model() { this(layer()); }
        private Model(ModelPart root) {
            super(root);
            wear = new ModelPart[]{root.getChild("body_wear"), root.getChild("left_arm_wear"), root.getChild("right_arm_wear"), root.getChild("left_leg_wear"), root.getChild("right_leg_wear")};
            wings = new ModelPart[]{root.getChild("left_wing"), root.getChild("right_wing")};
            shields = new ModelPart[]{root.getChild("shield1"), root.getChild("shield2"), root.getChild("shield3")};
        }
        private static ModelPart layer() {
            var mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0); var root = mesh.getRoot(); var inflation = new CubeDeformation(.25F);
            root.addOrReplaceChild("left_arm_wear", CubeListBuilder.create().texOffs(48, 48).mirror().addBox(-1, -2, -2, 4, 12, 4, inflation), PartPose.offset(5, 2, 0));
            root.addOrReplaceChild("right_arm_wear", CubeListBuilder.create().texOffs(40, 32).addBox(-3, -2, -2, 4, 12, 4, inflation), PartPose.offset(-5, 2, 0));
            root.addOrReplaceChild("left_leg_wear", CubeListBuilder.create().texOffs(0, 48).mirror().addBox(-2, 0, -2, 4, 12, 4, inflation), PartPose.offset(1.9F, 12, 0));
            root.addOrReplaceChild("right_leg_wear", CubeListBuilder.create().texOffs(0, 32).addBox(-2, 0, -2, 4, 12, 4, inflation), PartPose.offset(-1.9F, 12, 0));
            root.addOrReplaceChild("body_wear", CubeListBuilder.create().texOffs(16, 32).addBox(-4, 0, -2, 8, 12, 4, inflation), PartPose.ZERO);
            root.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(0, 64).addBox(0, 0, 0, 26, 32, 0), PartPose.offsetAndRotation(-30, -11, 5, 0, 0, -.1745329F));
            root.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(52, 64).addBox(0, 0, 0, 26, 32, 0), PartPose.offsetAndRotation(4, -15.8F, 5, 0, 0, .1745329F));
            for (int index = 1; index <= 3; index++) root.addOrReplaceChild("shield" + index, CubeListBuilder.create().texOffs(0, 113).addBox(-6, -1, 9, 12, 11, 0), PartPose.ZERO);
            return LayerDefinition.create(mesh, 128, 128).bakeRoot();
        }
        @Override public void setupAnim(LegacyVoidHerrscher entity, float limb, float amount, float age, float yaw, float pitch) {
            super.setupAnim(entity, limb, amount, age, yaw, pitch);
            var originals = new ModelPart[]{body, leftArm, rightArm, leftLeg, rightLeg};
            for (int index = 0; index < wear.length; index++) wear[index].copyFrom(originals[index]);
            for (var wing : wings) wing.visible = entity.isRankIIRenderState();
            int count = entity.getRotatingShieldsRenderState();
            for (int index = 0; index < shields.length; index++) {
                shields[index].visible = count > index;
                if (count > 0) shields[index].yRot = (float) Math.sin(age * .1F) + (float) (2 * Math.PI / count * (index + 1));
            }
        }
        @Override public void renderToBuffer(PoseStack pose, VertexConsumer vertices, int light, int overlay, int color) {
            super.renderToBuffer(pose, vertices, light, overlay, color);
            for (var part : wear) part.render(pose, vertices, light, overlay, color);
        }
    }
}
