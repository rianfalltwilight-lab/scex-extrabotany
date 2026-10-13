package io.github.lounode.extrabotany.forge.gametest;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.lounode.extrabotany.common.entity.*;
import io.github.lounode.extrabotany.common.entity.gaia.VoidHerrscher;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import java.nio.file.Files;
import java.util.*;
import java.util.function.BiConsumer;

/** Actual loaded models/renderers, plus screenshots at useful inspection sizes. Never distributed. */
public final class UpstreamRenderProbe extends Screen {
    private final Map<String,Object> checks;
    private final BiConsumer<Minecraft,Throwable> finish;
    private final List<ItemStack> items;
    private final List<Entity> entities = new ArrayList<>();
    private final List<net.minecraft.world.entity.decoration.ArmorStand> armors = new ArrayList<>();
    private int page,frames;
    public UpstreamRenderProbe(Map<String,Object> checks, BiConsumer<Minecraft,Throwable> finish) {
        super(Component.literal("ExtraBotany upstream parity audit")); this.checks=checks;this.finish=finish;
        var mc=Minecraft.getInstance();
        items=BuiltInRegistries.ITEM.keySet().stream().filter(id->id.getNamespace().equals("extrabotany")).sorted()
                .map(id->new ItemStack(BuiltInRegistries.ITEM.get(id))).toList();
        for(var id:BuiltInRegistries.ENTITY_TYPE.keySet().stream().filter(id->id.getNamespace().equals("extrabotany")).sorted().toList()) {
            var entity=BuiltInRegistries.ENTITY_TYPE.get(id).create(mc.level);
            check(entity!=null,"entity_constructs_"+id.getPath());entity.setPos(mc.player.position());entity.tickCount=30;
            if(entity instanceof io.github.lounode.extrabotany.common.entity.subspace.SubspaceEntity) {var tag=entity.saveWithoutId(new CompoundTag());tag.putInt("LiveTicks",120);tag.putInt("Delay",15);tag.putInt("Interval",10);tag.putFloat("Size",1.5F);entity.load(tag);}
            if(entity instanceof io.github.lounode.extrabotany.common.entity.projectile.FlowerWeaponEntity) {var tag=entity.saveWithoutId(new CompoundTag());tag.putInt("ChargeTicks",10);entity.load(tag);}
            if(entity instanceof VoidHerrscher) {var tag=entity.saveWithoutId(new CompoundTag());tag.putBoolean("RankII",true);tag.putInt("RotatingShields",3);entity.load(tag);}
            entities.add(entity);
        }
        for (String set : new String[]{"miku", "shootingguardian", "silentsages"}) {
            var stand = new net.minecraft.world.entity.decoration.ArmorStand(mc.level,0,0,0);
            stand.setPos(mc.player.position());stand.tickCount=30;stand.setShowArms(true);stand.setNoBasePlate(true);
            for (String suffix : new String[]{"helm","chest","legs","boots"}) {
                var armor=(net.minecraft.world.item.ArmorItem)BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("extrabotany",set+"_"+suffix));
                stand.setItemSlot(armor.getEquipmentSlot(),new ItemStack(armor));
            }
            armors.add(stand);
        }
    }
    private void check(boolean ok,String name) {if(!ok)throw new AssertionError(name);checks.put(name,true);}
    @Override public boolean isPauseScreen(){return false;}
    @Override protected void init(){
        try { inspectModels(); }catch(Throwable error){finish.accept(minecraft,error);}
    }
    @SuppressWarnings("deprecation") private void inspectModels(){
        var missing=minecraft.getModelManager().getMissingModel();
        for(var stack:items){
            var model=minecraft.getItemRenderer().getModel(stack,minecraft.level,minecraft.player,0);
            check(model!=missing,"item_model_loaded_"+BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath());
        }
        var flame=minecraft.getItemRenderer().getModel(new ItemStack(io.github.lounode.extrabotany.common.item.ExtraBotanyItems.flamescionWeapon),minecraft.level,minecraft.player,0);
        int quads=0;var random=RandomSource.create(42);
        for(int i=0;i<=6;i++)quads+=flame.getQuads(null,i==6?null:Direction.values()[i],random).size();
        check(quads==106*6,"flamescion_106_original_cuboids_baked");
        var rows=new ArrayList<Map<String,Object>>();
        for(var entity:entities){
            var counter=new Counter();var renderer=minecraft.getEntityRenderDispatcher().getRenderer(entity);
            renderer.render(entity,0,0,new PoseStack(),type->counter,0xF000F0);
            var name=BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getPath();
            boolean particleOnly=entity.getType()==io.github.lounode.extrabotany.common.entity.ExtraBotanyEntityType.AURA_FIRE || entity instanceof io.github.lounode.extrabotany.common.entity.judah.JudahSwordEntity;
            check(counter.vertices>0||particleOnly,"entity_has_original_geometry_or_particles_"+name);
            rows.add(Map.of("id",name,"renderer",renderer.getClass().getSimpleName(),"vertices",counter.vertices,"bounds",counter.bounds(),"particle_only",particleOnly));
            if(entity instanceof io.github.lounode.extrabotany.common.entity.subspace.SubspaceSpearEntity spear){
                check(counter.maxExtent()>3,"spear_uses_original_world_scale");
                float centerX=(counter.minX+counter.maxX)*.5F,centerY=(counter.minY+counter.maxY)*.5F,centerZ=(counter.minZ+counter.maxZ)*.5F;
                check(Math.abs(centerX)<.02F&&Math.abs(centerY-spear.getBbHeight()*.5F)<.02F&&Math.abs(centerZ)<.02F,
                        "spear_visual_center_matches_collision_box");
                check(counter.maxX-counter.minX<=spear.getBbWidth()+.01F&&counter.maxY-counter.minY<=spear.getBbHeight()+.01F,
                        "spear_visual_cross_section_fits_collision_box");
            }
            if(entity instanceof VoidHerrscher)check(renderer.getTextureLocation(entity).toString().equals("extrabotany:textures/entity/wing.png"),"herrscher_binds_original_128_atlas");
            if(entity.getType()==ExtraBotanyEntityType.MOTOR)check(counter.vertices==66*24,"motor_66_original_cuboids_rendered");
        }
        for(var stand:armors) for(var stack:stand.getArmorSlots()) {
            var model=io.github.lounode.extrabotany.client.model.ArmorModels.get(stack);var counter=new Counter();
            var name=BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
            check(model!=null,"armor_model_registered_"+name);
            model.setupAnim(stand,0,0,0,0,0);model.renderToBuffer(new PoseStack(),counter,0xF000F0,0,-1);
            check(counter.vertices>0,"armor_model_has_geometry_"+name);
            rows.add(Map.of("id",name,"renderer",model.getClass().getSimpleName(),"vertices",counter.vertices,"bounds",counter.bounds()));
        }
        var quantum=quantum();var tileRenderer=minecraft.getBlockEntityRenderDispatcher().getRenderer(quantum);var counter=new Counter();
        check(tileRenderer!=null,"quantum_original_tile_renderer_registered");
        tileRenderer.render(quantum,0,new PoseStack(),type->counter,0xF000F0,0);
        check(counter.vertices==36*24,"quantum_36_original_cuboids_rendered");
        try{var dir=minecraft.gameDirectory.toPath().resolve("verification");Files.createDirectories(dir);
            Files.writeString(dir.resolve("renderer-inventory.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(rows));
        }catch(Exception e){throw new RuntimeException(e);}
    }
    @Override public void render(GuiGraphics gui,int mouseX,int mouseY,float partial){
        try{
            gui.fill(0,0,width,height,0xFF20262E);
            gui.drawString(font,"ExtraBotany scex.8 | upstream geometry audit | page "+(page+1),12,10,-1);
            if(page==0 || page==8){
                int columns=Math.max(8,(width-24)/24);
                int capacity=columns*((height-40)/23),offset=page==8?capacity:0;
                for(int i=0;i<Math.min(capacity,items.size()-offset);i++)gui.renderItem(items.get(i+offset),12+i%columns*24,32+i/columns*23);
            }else if(page==1){
                int columns=8,cellW=(width-24)/columns,cellH=(height-45)/5;
                for(int i=0;i<entities.size();i++){
                    var entity=entities.get(i);float extent=entity instanceof io.github.lounode.extrabotany.common.entity.subspace.SubspaceSpearEntity?4:Math.max(1.8F,entity.getBbHeight());
                    draw(gui,entity,12+i%columns*cellW+cellW/2,35+i/columns*cellH+cellH*.7F,Math.min(cellW,cellH)*.45F/extent,25);
                    gui.drawString(font,Integer.toString(i+1),12+i%columns*cellW+4,35+i/columns*cellH+cellH-8,0xFFBBBBBB);
                }
            }else if(page==2){
                var boss=(VoidHerrscher)entities.stream().filter(e->e instanceof VoidHerrscher).findFirst().orElseThrow();
                for(int i=0;i<3;i++){
                    var tag=boss.saveWithoutId(new CompoundTag());tag.putBoolean("RankII",i>0);tag.putInt("RotatingShields",i==2?3:0);boss.load(tag);
                    draw(gui,boss,(i+.5F)*width/3,height*.8F,Math.min(height*.23F,width*.09F),i==1?0:180);
                    gui.drawCenteredString(font,new String[]{"Rank I / front","Rank II / rear","Rank III / shields"}[i],(int)((i+.5F)*width/3),height-22,-1);
                }
            }else if(page==3){
                var stack=new ItemStack(io.github.lounode.extrabotany.common.item.ExtraBotanyItems.flamescionWeapon);var pose=gui.pose();
                for(int i=0;i<3;i++){
                    pose.pushPose();pose.translate((i+.5)*width/3,height*.53,120);float size=Math.min(width/(i==0?5F:10F),height/(i==0?2F:4F));pose.scale(size,-size,size);if(i>0)pose.mulPose(Axis.YP.rotationDegrees(-35));
                    minecraft.getItemRenderer().renderStatic(stack,new ItemDisplayContext[]{ItemDisplayContext.GUI,ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,ItemDisplayContext.FIRST_PERSON_RIGHT_HAND}[i],
                            0xF000F0,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,pose,gui.bufferSource(),minecraft.level,0);gui.flush();pose.popPose();
                    gui.drawCenteredString(font,new String[]{"Inventory model","Third person hand","First person hand"}[i],(int)((i+.5)*width/3),height-30,-1);
                }
            }else if(page==4){
                String[] names={"subspace","subspace_spear","subspace_lance","void_field","flamescion_slash","strengthen_slash"};
                for(int i=0;i<names.length;i++){
                    String name=names[i];var entity=entities.stream().filter(e->BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).getPath().equals(name)).findFirst().orElseThrow();
                    draw(gui,entity,(i%3+.5F)*width/3,(i/3+.7F)*(height-40)/2+30,Math.min(width/22F,height/20F),45);
                    gui.drawCenteredString(font,name,(int)((i%3+.5F)*width/3),(int)((i/3+1F)*(height-40)/2)+20,-1);
                }
            }else if(page==5){
                var motor=entities.stream().filter(e->e.getType()==ExtraBotanyEntityType.MOTOR).findFirst().orElseThrow();
                draw(gui,motor,width*.25F,height*.72F,Math.min(width/8F,height/5F),45);
                var tile=quantum();var pose=gui.pose();pose.pushPose();pose.translate(width*.75,height*.55,100);
                float scale=Math.min(width/4F,height/2F);pose.scale(scale,-scale,scale);pose.mulPose(Axis.XP.rotationDegrees(20));pose.mulPose(Axis.YP.rotationDegrees(40));pose.translate(-.5,-.5,-.5);
                minecraft.getBlockEntityRenderDispatcher().getRenderer(tile).render(tile,0,pose,gui.bufferSource(),0xF000F0,0);gui.flush();pose.popPose();
                gui.drawCenteredString(font,"Original motor / 66 cuboids",(int)(width*.25F),height-24,-1);
                gui.drawCenteredString(font,"Original quantum buffer / 36 cuboids",(int)(width*.75F),height-24,-1);
            }
            if(page>=6 && page<8){
                for(int i=0;i<armors.size();i++){
                    draw(gui,armors.get(i),(i+.5F)*width/3,height*.84F,Math.min(width*.13F,height*.34F),page==7?0:180);
                    gui.drawCenteredString(font,new String[]{"Miku","Shooting Guardian","Silent Sages"}[i],(int)((i+.5F)*width/3),height-22,-1);
                }
            }
            gui.flush();
            if(++frames==30){var dir=minecraft.gameDirectory.toPath().resolve("verification");Files.createDirectories(dir);
                Screenshot.grab(dir.toFile(),"upstream-page-"+page+".png",minecraft.getMainRenderTarget(),message->{});
                check(true,"render_page_"+page+"_captured");}
            if(frames>45){frames=0;if(++page>8)finish.accept(minecraft,null);}
        }catch(Throwable error){finish.accept(minecraft,error);}
    }
    private io.github.lounode.extrabotany.common.block.block_entity.ManaBufferBlockEntity quantum(){
        return new io.github.lounode.extrabotany.common.block.block_entity.ManaBufferBlockEntity(
                io.github.lounode.extrabotany.common.block.block_entity.ManaBufferBlockEntity.Tier.QUANTUM,net.minecraft.core.BlockPos.ZERO,
                io.github.lounode.extrabotany.common.block.ExtraBotanyBlocks.quantumManaBuffer.defaultBlockState());
    }
    private void draw(GuiGraphics gui,Entity entity,float x,float y,float scale,float yaw){
        var pose=gui.pose();pose.pushPose();pose.translate(x,y,100);pose.scale(scale,scale,-scale);
        pose.mulPose(Axis.ZP.rotationDegrees(180));pose.mulPose(Axis.YP.rotationDegrees(yaw));
        var renderer=minecraft.getEntityRenderDispatcher().getRenderer(entity);renderer.render(entity,0,0,pose,gui.bufferSource(),0xF000F0);
        gui.flush();pose.popPose();
    }
    private static final class Counter implements VertexConsumer {
        int vertices;float minX=Float.POSITIVE_INFINITY,minY=minX,minZ=minX,maxX=Float.NEGATIVE_INFINITY,maxY=maxX,maxZ=maxX;
        public VertexConsumer addVertex(float x,float y,float z){if(!Float.isFinite(x+y+z))throw new AssertionError("Nonfinite render vertex");vertices++;minX=Math.min(minX,x);minY=Math.min(minY,y);minZ=Math.min(minZ,z);maxX=Math.max(maxX,x);maxY=Math.max(maxY,y);maxZ=Math.max(maxZ,z);return this;}
        public VertexConsumer setColor(int r,int g,int b,int a){return this;}public VertexConsumer setUv(float u,float v){if(!Float.isFinite(u+v))throw new AssertionError("Nonfinite UV");return this;}
        public VertexConsumer setUv1(int u,int v){return this;}public VertexConsumer setUv2(int u,int v){return this;}public VertexConsumer setNormal(float x,float y,float z){return this;}
        float maxExtent(){return Math.max(maxX-minX,Math.max(maxY-minY,maxZ-minZ));}
        Object bounds(){return vertices==0?List.of():List.of(minX,minY,minZ,maxX,maxY,maxZ);}
    }
}
