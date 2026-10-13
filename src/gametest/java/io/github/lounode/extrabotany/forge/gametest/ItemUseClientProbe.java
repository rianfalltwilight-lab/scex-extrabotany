package io.github.lounode.extrabotany.forge.gametest;

import com.google.gson.GsonBuilder;
import io.github.lounode.extrabotany.common.entity.subspace.SubspaceEntity;
import io.github.lounode.extrabotany.common.entity.flamescion.FlamescionUltEntity;
import io.github.lounode.extrabotany.common.brew.ExtraBotanyMobEffects;
import io.github.lounode.extrabotany.forge.client.ExtraBotanyKeyMappings;
import io.github.lounode.extrabotany.xplat.EXplatAbstractions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import top.theillusivec4.curios.api.CuriosApi;
import vazkii.botania.api.mana.ManaItem;
import vazkii.botania.common.item.BotaniaItems;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/** Opt-in disposable GUI probe. Excluded from the released JAR. */
@EventBusSubscriber(modid="extrabotany", value=Dist.CLIENT)
public final class ItemUseClientProbe {
    private static volatile int stage;
    private static int ticks,total,targetId;
    private static final List<String> flameTrace=Collections.synchronizedList(new ArrayList<>());
    private static void trace(net.minecraft.world.entity.player.Player p,String side) {
        if((stage>=11 && stage<=14) || stage==110)flameTrace.add(side+" tick="+p.tickCount+" stage="+stage+" ground="+p.onGround()+" shift="+p.isShiftKeyDown()+" effect="+p.getEffect(ExtraBotanyMobEffects.INCANDESCENCE)+" overload="+io.github.lounode.extrabotany.common.handler.FlamescionHandler.isOverloaded(p)+" pos="+p.position()+" velocity="+p.getDeltaMovement()+" held="+p.getMainHandItem());
    }
    @SubscribeEvent public static void serverTrace(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post event) {
        if(Boolean.getBoolean("extrabotany.itemProbe") && !event.getEntity().level().isClientSide())trace(event.getEntity(),"SERVER");
    }
    private static float healthAfterGlove;
    private static final Map<String,Object> checks=new LinkedHashMap<>();
    private static CompletableFuture<Void> pending;
    private static void check(boolean ok,String label) { if(!ok)throw new AssertionError(label);checks.put(label,true);System.out.println("EXTRA_ITEM_PASS "+label); }
    private static top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler ring(net.minecraft.world.entity.player.Player p) {
        return CuriosApi.getCuriosInventory(p).orElseThrow().getStacksHandler("ring").orElseThrow().getStacks();
    }
    private static void server(Minecraft mc,java.util.function.Consumer<ServerPlayer> action,int next) {
        var uuid=mc.player.getUUID();pending=new CompletableFuture<>();stage=next;ticks=0;
        mc.getSingleplayerServer().execute(()->{try{action.accept(mc.getSingleplayerServer().getPlayerList().getPlayer(uuid));pending.complete(null);}catch(Throwable e){pending.completeExceptionally(e);}});
    }
    private static void finish(Minecraft mc,Throwable error) {
        stage=99;mc.options.keyAttack.setDown(false);ExtraBotanyKeyMappings.SKILL.setDown(false);
        try {
            var out=mc.gameDirectory.toPath().resolve("verification");Files.createDirectories(out);
            Files.write(out.resolve("flame-trace.txt"),flameTrace);
            Files.writeString(out.resolve("result.json"),new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("status",error==null?"captured":"failed","checks",checks,"error",error==null?"":error.toString())));
        } catch(Exception e){e.printStackTrace();}
        if(error!=null)error.printStackTrace();
        if(mc.getSingleplayerServer()!=null)mc.getSingleplayerServer().halt(true);
        mc.disconnect();mc.stop();
    }
    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        if(!Boolean.getBoolean("extrabotany.itemProbe") || stage==99)return;
        var mc=Minecraft.getInstance();
        try {
            if(++total>4200)throw new AssertionError("Timeout stage="+stage);
            mc.options.pauseOnLostFocus=false;mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
            if(stage==0 && mc.screen instanceof net.minecraft.client.gui.screens.AccessibilityOnboardingScreen){mc.options.onboardingAccessibilityFinished();mc.setScreen(new TitleScreen());}
            if(stage==0 && mc.screen instanceof TitleScreen && mc.getOverlay()==null) {
                stage=1;mc.createWorldOpenFlows().createFreshLevel("extra-items-probe",new LevelSettings("ExtraBotany item regression",GameType.SURVIVAL,false,Difficulty.NORMAL,true,new GameRules(),WorldDataConfiguration.DEFAULT),
                    new WorldOptions(20260912L,false,false),r->r.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),new TitleScreen());return;
            }
            if(mc.player==null || mc.level==null || mc.getSingleplayerServer()==null)return;
            trace(mc.player,"CLIENT");
            ticks++;
            if(pending!=null){if(!pending.isDone())return;pending.join();pending=null;ticks=0;}
            switch(stage) {
                case 1 -> {if(ticks>40)server(mc,p->{
                    var l=p.serverLevel();l.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false,l.getServer());
                    for(int x=-5;x<=5;x++)for(int z=-5;z<=5;z++)l.setBlockAndUpdate(new BlockPos(x,70,z),Blocks.STONE.defaultBlockState());
                    p.teleportTo(0.5,71,0.5);p.setYRot(0);p.setXRot(0);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.IRON_SWORD));
                    ring(p).setStackInSlot(0,new ItemStack(io.github.lounode.extrabotany.common.item.ExtraBotanyItems.powerGlove));
                    var target=EntityType.HUSK.create(l);target.setNoAi(true);target.setPersistenceRequired();target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);target.setHealth(1000);
                    target.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);target.setPos(.5,71,2.5);l.addFreshEntity(target);targetId=target.getId();
                },2);}
                case 2 -> {if(ticks>30){check(!vazkii.botania.common.handler.EquipmentHandler.findOrEmpty(io.github.lounode.extrabotany.common.item.ExtraBotanyItems.powerGlove,mc.player).isEmpty(),"glove_syncs_to_client");mc.player.setYRot(0);mc.player.setXRot(0);mc.mouseHandler.grabMouse();org.lwjgl.glfw.GLFW.glfwFocusWindow(mc.getWindow().getWindow());stage=3;ticks=0;}}
                case 3 -> {
                    mc.hitResult=new EntityHitResult(Objects.requireNonNull(mc.level.getEntity(targetId)));mc.options.keyAttack.setDown(true);
                    if(ticks>80){mc.options.keyAttack.setDown(false);server(mc,p->{var target=(net.minecraft.world.entity.LivingEntity)p.serverLevel().getEntity(targetId);healthAfterGlove=target.getHealth();
                        check(healthAfterGlove<970,"held_attack_deals_repeated_server_damage");ring(p).setStackInSlot(0,ItemStack.EMPTY);},4);}
                }
                case 4 -> {if(ticks>20){check(!!vazkii.botania.common.handler.EquipmentHandler.findOrEmpty(io.github.lounode.extrabotany.common.item.ExtraBotanyItems.powerGlove,mc.player).isEmpty(),"glove_removal_syncs");stage=5;ticks=0;}}
                case 5 -> {
                    mc.hitResult=new EntityHitResult(Objects.requireNonNull(mc.level.getEntity(targetId)));mc.options.keyAttack.setDown(true);
                    if(ticks>50){mc.options.keyAttack.setDown(false);server(mc,p->{check(((net.minecraft.world.entity.LivingEntity)p.serverLevel().getEntity(targetId)).getHealth()==healthAfterGlove,"held_attack_stops_after_unequip");
                        ring(p).setStackInSlot(0,new ItemStack(io.github.lounode.extrabotany.common.item.ExtraBotanyItems.gemOfConquest));},6);}
                }
                case 6 -> {if(ticks>30){check(!vazkii.botania.common.handler.EquipmentHandler.findOrEmpty(io.github.lounode.extrabotany.common.item.ExtraBotanyItems.gemOfConquest,mc.player).isEmpty(),"conquest_syncs_to_client");server(mc,p->{
                    check(p.getAttributeValue(Attributes.MOVEMENT_SPEED)>.104,"conquest_real_movement_modifier");
                    check(p.getAttributeValue(Attributes.ATTACK_DAMAGE)>6.8,"conquest_real_damage_modifier");
                    p.setLastHurtMob((net.minecraft.world.entity.LivingEntity)p.serverLevel().getEntity(targetId));
                },7);}}
                case 7 -> {if(ticks>45)server(mc,p->{
                    check(p.hasEffect(net.minecraft.world.effect.MobEffects.DAMAGE_BOOST),"conquest_strength_after_attack");
                    ring(p).setStackInSlot(0,ItemStack.EMPTY);
                    var stack=new ItemStack(io.github.lounode.extrabotany.common.item.ExtraBotanyItems.spearOfSubspace);p.setItemInHand(InteractionHand.MAIN_HAND,stack);EXplatAbstractions.INSTANCE.findRelic(stack).bindToUUID(p.getUUID());
                    var tablet=new ItemStack(BotaniaItems.MANA_TABLET);ManaItem.LOOKUP.find(tablet).addMana(30000);p.getInventory().setItem(2,tablet);
                },8);}
                case 8 -> {if(ticks>25){mc.gameMode.useItem(mc.player,InteractionHand.MAIN_HAND);stage=9;ticks=0;}}
                case 9 -> {if(ticks>12){mc.gameMode.releaseUsingItem(mc.player);stage=10;ticks=0;}}
                case 10 -> {if(ticks>8){
                    var portals=mc.level.getEntitiesOfClass(SubspaceEntity.class,mc.player.getBoundingBox().inflate(30));
                    check(portals.size()==24 && portals.stream().allMatch(e->e.getSize()>0 && e.getDelay()>0),"spear_portal_visual_parameters_sync_to_client");
                    server(mc,p->{
                    check(ManaItem.LOOKUP.find(p.getInventory().getItem(2)).getMana()==20000,"spear_client_release_pays_once");
                    check(p.serverLevel().getEntitiesOfClass(SubspaceEntity.class,p.getBoundingBox().inflate(30),e->e.getOwner()==p).size()==24,"spear_client_release_spawns_domain");
                    p.removeEffect(ExtraBotanyMobEffects.ETERNITY);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(io.github.lounode.extrabotany.common.item.ExtraBotanyItems.flamescionWeapon));p.teleportTo(.5,71,.5);p.setDeltaMovement(0,0,0);
                },11);}}
                case 11 -> {if(ticks>20){mc.options.keyShift.setDown(true);stage=110;ticks=0;}}
                case 110 -> {if(ticks>2){check(mc.player.isShiftKeyDown(),"flamescion_real_shift_input_ready");mc.gameMode.useItem(mc.player,InteractionHand.MAIN_HAND);stage=12;ticks=0;}}
                case 12 -> {if(ticks>8){mc.options.keyShift.setDown(false);server(mc,p->{
                    check(p.hasEffect(ExtraBotanyMobEffects.INCANDESCENCE),"flamescion_client_sneak_use_ignites");
                    p.setShiftKeyDown(false);
                },13);}}
                case 13 -> {if(ticks>8){check(io.github.lounode.extrabotany.common.handler.FlamescionHandler.isFlamescionMode(mc.player),"flamescion_mode_syncs_to_client");ExtraBotanyKeyMappings.SKILL.setDown(true);stage=14;ticks=0;}}
                case 14 -> {if(ticks>5){ExtraBotanyKeyMappings.SKILL.setDown(false);server(mc,p->{
                    check(io.github.lounode.extrabotany.common.handler.FlamescionHandler.isOverloaded(p),"flamescion_configurable_key_reaches_server");
                    check(!p.serverLevel().getEntitiesOfClass(FlamescionUltEntity.class,p.getBoundingBox().inflate(20),e->e.getOwner()==p).isEmpty(),"flamescion_key_spawns_ultimate");
                    p.setNoGravity(false);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(io.github.lounode.extrabotany.common.item.ExtraBotanyItems.gemOfConquest));
                },15);}}
                case 15 -> {if(ticks>10){var out=mc.gameDirectory.toPath().resolve("verification");Files.createDirectories(out);
                    net.minecraft.client.Screenshot.grab(out.toFile(),"items.png",mc.getMainRenderTarget(),c->{});stage=16;ticks=0;}}
                case 16 -> {if(ticks>10){stage=99;mc.setScreen(new UpstreamRenderProbe(checks,ItemUseClientProbe::finish));}}
            }
        }catch(Throwable e){
            e.printStackTrace();stage=99;
            if(mc.level!=null && mc.player!=null) {
                mc.setScreen(new UpstreamRenderProbe(checks,(client,renderError)->finish(client,renderError==null?e:renderError)));
            }else finish(mc,e);
        }
    }
}
