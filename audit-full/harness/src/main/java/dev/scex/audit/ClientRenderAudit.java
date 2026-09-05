package dev.scex.audit;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Isolated physical-client render probe. This audit mod is never included in the deliverable. */
@EventBusSubscriber(modid = "scex_registry_audit", value = Dist.CLIENT)
public final class ClientRenderAudit {
    private static int ticks, stage;
    private static final List<String> completed = new ArrayList<>();
    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("scex.audit.client")) return;
        var game = Minecraft.getInstance();
        try {
            if (stage == 0 && game.screen != null && ++ticks > 60) {
                stage = 1; game.createWorldOpenFlows().openWorld("legacy-fixture", () -> fail(new IllegalStateException("Cannot open isolated fixture")));
            } else if (stage == 1 && game.level != null && game.player != null && game.getSingleplayerServer() != null) {
                stage = 2; game.setScreen(new AuditScreen());
            }
        } catch (Throwable failure) { fail(failure); }
    }
    private static void fail(Throwable failure) {
        failure.printStackTrace();
        try { Files.writeString(Path.of("client-render-failure.txt"), failure.toString()); } catch (Exception ignored) {}
        Minecraft.getInstance().stop(); stage = 99;
    }
    private static final class AuditScreen extends Screen {
        private int page, frames;
        private final List<ItemStack> items;
        private final List<Entity> entities = new ArrayList<>();
        private final List<ArmorStand> armor = new ArrayList<>();
        AuditScreen() {
            super(Component.literal("SCEX full legacy render audit"));
            var game = Minecraft.getInstance();
            items = BuiltInRegistries.ITEM.keySet().stream().filter(id -> id.getNamespace().equals("extrabotany")).sorted()
                    .map(id -> new ItemStack(BuiltInRegistries.ITEM.get(id))).toList();
            for (var id : BuiltInRegistries.ENTITY_TYPE.keySet().stream().filter(id -> id.getNamespace().equals("extrabotany")).sorted().toList()) {
                var entity = BuiltInRegistries.ENTITY_TYPE.get(id).create(game.level);
                if (entity == null) throw new IllegalStateException("Null client entity " + id);
                entity.setPos(game.player.position());
                entity.tickCount = 20; // Bypass the vanilla thrown-item near-camera spawn grace period.
                if (id.getPath().equals("void_herrscher")) { var tag = entity.saveWithoutId(new CompoundTag()); tag.putBoolean("RankII", true); tag.putInt("RotatingShields", 3); entity.load(tag); }
                entities.add(entity);
            }
            for (String set : List.of("miku", "shootingguardian", "silentsages")) {
                var stand = new ArmorStand(game.level, game.player.getX(), game.player.getY(), game.player.getZ());
                String[] names = {"helm", "chest", "legs", "boots"};
                EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
                for (int i = 0; i < slots.length; i++) {
                    var id = net.minecraft.resources.ResourceLocation.parse("extrabotany:" + set + "_" + names[i]);
                    if (!BuiltInRegistries.ITEM.containsKey(id)) throw new IllegalStateException("Missing armor " + id);
                    stand.setItemSlot(slots[i], new ItemStack(BuiltInRegistries.ITEM.get(id)));
                }
                armor.add(stand);
            }
        }
        @Override public boolean isPauseScreen() { return false; }
        @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partial) {
            try {
                graphics.fill(0, 0, width, height, 0xFF20262E);
                graphics.drawString(font, "SCEX .5 full restore | " + (page == 0 ? "243 registered items" : page == 1 ? "34 entity renderers" : "restored armor sets"), 12, 12, 0xFFFFFFFF);
                if (page == 0) {
                    for (int index = 0; index < items.size(); index++) graphics.renderItem(items.get(index), 14 + index % 16 * 24, 36 + index / 16 * 23);
                } else if (page == 1) {
                    for (int index = 0; index < entities.size(); index++) {
                        var entity = entities.get(index); int x = 40 + index % 8 * 70, y = 100 + index / 8 * 75;
                        drawEntity(graphics, entity, x, y, 38 / Math.max(.5F, Math.max(entity.getBbHeight(), entity.getBbWidth())));
                        graphics.drawString(font, Integer.toString(index + 1), x - 4, y + 2, 0xFFCCCCCC);
                    }
                } else for (int index = 0; index < armor.size(); index++) drawEntity(graphics, armor.get(index), 100 + index * 145, 300, 100);
                graphics.flush();
                if (++frames == 40) {
                    String name = "full-legacy-" + (page == 0 ? "items" : page == 1 ? "entities" : "armor") + ".png";
                    Screenshot.grab(minecraft.gameDirectory, name, minecraft.getMainRenderTarget(), message -> System.out.println("SCEX_RENDER_SCREENSHOT " + message.getString()));
                    completed.add(name); System.out.println("SCEX_RENDER_PAGE_PASS " + page);
                }
                if (frames > 60) {
                    frames = 0;
                    if (++page > 2) {
                        Files.writeString(Path.of("client-render-success.json"), new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(completed));
                        stage = 99; minecraft.stop();
                    }
                }
            } catch (Throwable failure) { fail(failure); }
        }
        private void drawEntity(GuiGraphics graphics, Entity entity, int x, int y, float scale) {
            var pose = graphics.pose(); pose.pushPose(); pose.translate(x, y, 100); pose.scale(scale, scale, -scale);
            pose.mulPose(Axis.ZP.rotationDegrees(180)); pose.mulPose(Axis.YP.rotationDegrees(25));
            var dispatcher = minecraft.getEntityRenderDispatcher(); dispatcher.setRenderShadow(false);
            dispatcher.render(entity, 0, 0, 0, 0, 0, pose, graphics.bufferSource(), 0xF000F0);
            graphics.flush(); dispatcher.setRenderShadow(true); pose.popPose();
        }
    }
}
