package io.github.lounode.extrabotany.common.item.legacy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;
import vazkii.botania.common.handler.EquipmentHandler;
import vazkii.botania.common.item.equipment.bauble.BaubleItem;

/** Place an inventory torch through its normal use handler, preserving placement events. */
public final class LegacyBottledFlameItem extends BaubleItem {
    public static final LegacyBottledFlameItem INSTANCE = new LegacyBottledFlameItem();
    private LegacyBottledFlameItem() { super(new Properties().stacksTo(1)); }
    @Override public boolean canEquip(ItemStack stack, LivingEntity user) { return EquipmentHandler.findOrEmpty(this, user).isEmpty(); }
    @SuppressWarnings("deprecation") // Same legacy material-fluid placement exclusion.
    @Override public void onWornTick(ItemStack stack, LivingEntity user) {
        super.onWornTick(stack, user);
        if (!(user instanceof ServerPlayer player) || player.tickCount % 10 != 0) return;
        BlockPos position = player.blockPosition();
        var level = player.level();
        var below = level.getBlockState(position.below());
        if (level.getMaxLocalRawBrightness(position) > 3 || !level.getBlockState(position).isAir()
                || below.isAir() || below.liquid()) return;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            var torch = player.getInventory().getItem(slot);
            if (torch.isEmpty() || !(torch.getItem() instanceof BlockItem blockItem)
                    || !BuiltInRegistries.ITEM.getKey(blockItem).getPath().contains("torch")
                    || !blockItem.getBlock().defaultBlockState().canSurvive(level, position)) continue;
            var previous = player.getOffhandItem();
            try {
                player.setItemInHand(InteractionHand.OFF_HAND, torch);
                torch.useOn(new UseOnContext(player, InteractionHand.OFF_HAND,
                        new BlockHitResult(position.getCenter(), Direction.UP, position.below(), false)));
            } finally { player.setItemInHand(InteractionHand.OFF_HAND, previous); }
            return;
        }
    }
}
