package io.github.lounode.extrabotany.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.lounode.extrabotany.common.entity.LegacySubspaceSpear;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public final class LegacySubspaceSpearRenderer extends EntityRenderer<LegacySubspaceSpear> {
    private static final ResourceLocation TEXTURE = ResourceLocation.parse("extrabotany:textures/entity/spearsubspace.png");
    private final ModelPart model;
    public LegacySubspaceSpearRenderer(EntityRendererProvider.Context context) { super(context); model = mesh(); }
    private static ModelPart mesh() {
        // Exact archived model UVs and cubes, mapped into the maintained model builder.
        float[][] boxes = {{10,0,0,-.5F,0,2,3,2},{10,0,0,0,-.5F,2,2,3},{10,0,-.5F,0,0,3,2,2},
                {26,0,.5F,7,.5F,1,16,1},{26,0,0,8,0,1,10,1},{26,0,1,6,1,1,9,1},{26,0,1,5,0,1,9,1},{26,0,0,7,1,1,7,1},
                {0,0,0,-12,0,1,10,1},{0,0,1,-14,0,1,10,1},{0,0,0,-16,1,1,13,1},{0,0,1,-20,1,1,14,1},{0,0,.5F,-24,.5F,1,13,1}};
        var mesh = new MeshDefinition(); var cubes = CubeListBuilder.create();
        for (var b : boxes) cubes.texOffs((int) b[0], (int) b[1]).addBox(b[2], b[3], b[4], b[5], b[6], b[7]);
        var root = mesh.getRoot().addOrReplaceChild("spear", cubes, PartPose.ZERO);
        float[][] fins = {{5,0,-.5F,-2,2,-.7853982F},{5,0,0,-2.5F,4,.1919862F},{21,0,-1.5F,3,4,.1919862F},{21,0,-1.5F,4,2,-.7853982F}};
        for (int i = 0; i < fins.length; i++) { var f = fins[i]; root.addOrReplaceChild("shape" + (i + 4),
                CubeListBuilder.create().texOffs((int) f[0], 0).addBox(f[1], f[2], f[3], 1, f[4], 1), PartPose.rotation(f[5], .2617994F, 0)); }
        return LayerDefinition.create(mesh, 64, 32).bakeRoot().getChild("spear");
    }
    @Override public void render(LegacySubspaceSpear entity, float yaw, float partial, PoseStack pose, MultiBufferSource buffers, int light) {
        pose.pushPose(); pose.translate(.5F, 1.5F, .5F); pose.mulPose(Axis.YP.rotationDegrees(entity.getYRot()));
        pose.mulPose(Axis.XP.rotationDegrees(90)); pose.mulPose(Axis.XP.rotationDegrees(entity.getXRot())); pose.scale(.07F, -.07F, -.07F);
        model.render(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)), light, OverlayTexture.NO_OVERLAY, -1);
        pose.popPose(); super.render(entity, yaw, partial, pose, buffers, light);
    }
    @Override public ResourceLocation getTextureLocation(LegacySubspaceSpear entity) { return TEXTURE; }
}
