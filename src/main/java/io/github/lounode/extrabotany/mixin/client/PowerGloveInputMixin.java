package io.github.lounode.extrabotany.mixin.client;

import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import vazkii.botania.common.handler.EquipmentHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 佩戴强力手套的玩家按住攻击键时，在攻击冷却结束后自动再次攻击。
 * When a player that wears the Power Glove holds the attack key, this attacks again automatically after the attack cooldown is complete.
 */
@Mixin(Minecraft.class)
public abstract class PowerGloveInputMixin {
    @Shadow private boolean startAttack() { throw new AssertionError(); }

    @Inject(method = "handleKeybinds", at = @At("TAIL"))
    private void repeatAttack(CallbackInfo ci) {
        var mc = (Minecraft) (Object) this;
        var player = mc.player;
        if (player == null || mc.gameMode == null || mc.screen != null || mc.isPaused()
                || !mc.isWindowActive() || player.isSpectator() || player.isUsingItem()
                || !mc.options.keyAttack.isDown() || player.getAttackStrengthScale(0) < 1
                || mc.gameMode.isDestroying() || mc.hitResult == null
                || mc.hitResult.getType() == HitResult.Type.BLOCK) return;
        if (!EquipmentHandler.findOrEmpty(ExtraBotanyItems.powerGlove, player).isEmpty()) startAttack();
    }
}
