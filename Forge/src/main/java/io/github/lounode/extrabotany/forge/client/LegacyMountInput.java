package io.github.lounode.extrabotany.forge.client;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.lounode.extrabotany.common.entity.LegacyMount;
import io.github.lounode.extrabotany.forge.network.LegacyMountPacket;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = "extrabotany", value = Dist.CLIENT)
public final class LegacyMountInput {
    private static boolean mountDown;
    private static boolean shiftDown;
    private static int previous = -1;
    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        var minecraft = Minecraft.getInstance();
        if (minecraft.player == null) { mountDown = false; shiftDown = false; previous = -1; return; }
        boolean summon = minecraft.screen == null && InputConstants.isKeyDown(minecraft.getWindow().getWindow(), 82);
        boolean flameMode = io.github.lounode.extrabotany.common.item.legacy.LegacyFlamescionItem.mode(minecraft.player);
        if (summon && !mountDown) {
            if (flameMode) PacketDistributor.sendToServer(new io.github.lounode.extrabotany.forge.network.LegacyFlameSkillPacket(true));
            else if (minecraft.player.getVehicle() == null) PacketDistributor.sendToServer(new LegacyMountPacket(0, false, true));
        }
        boolean shift = minecraft.screen == null && minecraft.options.keyShift.isDown();
        if (shift && !shiftDown && flameMode) PacketDistributor.sendToServer(new io.github.lounode.extrabotany.forge.network.LegacyFlameSkillPacket(false));
        shiftDown = shift;
        mountDown = summon;
        if (!(minecraft.player.getVehicle() instanceof LegacyMount mount)) { previous = -1; return; }
        var options = minecraft.options;
        int controls = minecraft.screen != null ? 0 : (options.keyUp.isDown()?1:0) | (options.keyDown.isDown()?2:0)
                | (options.keyLeft.isDown()?4:0) | (options.keyRight.isDown()?8:0) | (options.keyJump.isDown()?16:0);
        boolean special = minecraft.screen == null && options.keySprint.consumeClick();
        mount.input(controls, false);
        if (controls != previous || special) { PacketDistributor.sendToServer(new LegacyMountPacket(controls, special, false)); previous = controls; }
    }
}
