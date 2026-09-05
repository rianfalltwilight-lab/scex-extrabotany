package io.github.lounode.extrabotany.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.lounode.extrabotany.common.entity.LegacyMount;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Numeric geometry mapping of the MIT scex.1 MotorModel/UfoModel; no invented mesh. */
public final class LegacyMountRenderer extends EntityRenderer<LegacyMount> {
    private final boolean flying;
    private final ModelPart model;
    private final ResourceLocation texture;
    public LegacyMountRenderer(EntityRendererProvider.Context context, boolean flying) {
        super(context); this.flying = flying;
        model = (flying ? ufo() : motor()).bakeRoot();
        texture = ResourceLocation.parse("extrabotany:textures/entity/" + (flying ? "ufo" : "motor") + ".png");
    }
    // Rows: texture u/v, box x/y/z, box width/height/depth, optional mirror flag.
    private static CubeListBuilder cubes(float[][] boxes) {
        var builder = CubeListBuilder.create();
        for (var box : boxes) builder.texOffs((int)box[0],(int)box[1]).mirror(box.length>8 && box[8]!=0).addBox(box[2],box[3],box[4],box[5],box[6],box[7]);
        return builder;
    }
    private static LayerDefinition motor() {
        var mesh = new MeshDefinition(); var root = mesh.getRoot();
        root.addOrReplaceChild("body", cubes(new float[][] {
                {0,23,-2.5F,-9,-11,5,4,16},{0,0,-3,-5,-11,6,2,21},{0,32,-3,-11.5F,-17,6,1,2},
                {36,45,-3,-11.5F,13,6,1,6},{81,11,-3,-14,-7.5F,6,3,7},{85,77,-3,-11,2,6,5,6},
                {0,63,-2.5F,-7,8,5,3,11},{46,38,3,-7.5F,-16,1,2,16},{46,38,-4,-7.5F,-16,1,2,16,1},
                {18,45,3,-4.5F,-16,1,2,16,1},{18,45,-4,-4.5F,-16,1,2,16,1},
                {58,59,-6.5F,-4,9,3,3,13,1},{58,59,3.5F,-4,9,3,3,13,1}}), PartPose.offset(0,24,0));
        float[][] wheel = {{68,96,-3,-5,-2,6,10,4},{48,92,-3,-2,-5,6,4,10},{18,51,2.01F,-2,-2,1,4,4},{0,51,-3.01F,-2,-2,1,4,4}};
        root.addOrReplaceChild("front_wheel",cubes(wheel),PartPose.offset(0,24,-21));
        root.addOrReplaceChild("back_wheel",cubes(wheel),PartPose.offset(0,24,12));
        return LayerDefinition.create(mesh,128,128);
    }
    private static LayerDefinition ufo() {
        var mesh=new MeshDefinition(); var root=mesh.getRoot();
        var body=root.addOrReplaceChild("body",cubes(new float[][] {
                {112,0,-24,0,-8,32,2,32},{80,38,-19,-7,-3,4,5,22},{0,86,-15,-9,15,14,7,4},
                {80,65,-15,-7,-3,14,5,4},{132,38,-1,-7,-3,4,5,22},{0,62,-12,-5,-5,8,3,2},
                {1,82,-18,-8,6,2,1,2},{1,83,0,-8,6,2,1,2},{184,53,-13,-4,3,10,2,10}}),PartPose.offset(6,24,-6));
        body.addOrReplaceChild("cube_r1",cubes(new float[][] {{0,0,-19,6,-17,36,2,36}}),PartPose.offsetAndRotation(-7,-8,7,0,-.7854F,0));
        root.addOrReplaceChild("glow",cubes(new float[][] {
                {0,67,-2,-6,-4,6,4,6},{0,77,-1,-13,5,4,5,4},{16,77,-19,-13,5,4,5,4},
                {24,67,-20,-6,-4,6,4,6},{0,38,-18,2,-2,20,4,20},{36,89,3,2,5,3,2,6},{54,89,-22,2,5,3,2,6},
                {0,0,-9,-4,-6,2,2,1},{6,12,4,-10,6,1,8,1},{4,9,3,-10,6,1,1,1},{0,14,-20,-10,6,1,1,1},{4,12,-21,-10,6,1,8,1}}),PartPose.offset(6,24,-6));
        return LayerDefinition.create(mesh,256,256);
    }
    @Override public void render(LegacyMount mount,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light) {
        pose.pushPose(); pose.translate(0,flying?2.5:1.5,0); pose.mulPose(Axis.YP.rotationDegrees(180-yaw));
        if(!flying) { pose.mulPose(Axis.ZP.rotationDegrees(mount.lean())); pose.mulPose(Axis.XP.rotationDegrees(mount.pitch())); }
        pose.scale(flying?-1.35F:-1,flying?-1.35F:-1,flying?1.35F:1);
        var vertex=buffers.getBuffer(RenderType.entityCutoutNoCull(texture));
        model.getChild("body").render(pose,vertex,light,OverlayTexture.NO_OVERLAY,-1);
        if(flying) model.getChild("glow").render(pose,vertex,0xF000F0,OverlayTexture.NO_OVERLAY,-1);
        else for(String name:new String[]{"front_wheel","back_wheel"}) {
            var wheel=model.getChild(name); wheel.xRot=mount.getDeltaMovement().horizontalDistanceSqr()>.0001?mount.ridingTicks()*.7F:Mth.lerp(.15F,wheel.xRot,0);
            wheel.render(pose,vertex,light,OverlayTexture.NO_OVERLAY,-1);
        }
        pose.popPose(); super.render(mount,yaw,partial,pose,buffers,light);
    }
    @Override public ResourceLocation getTextureLocation(LegacyMount mount) { return texture; }
}
