package io.github.lounode.extrabotany.common.item.legacy;

import io.github.lounode.extrabotany.common.entity.gaia.LegacyVoidHerrscher;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.context.UseOnContext;

public final class LegacyVoidCallerItem extends Item {
    public static final LegacyVoidCallerItem INSTANCE = new LegacyVoidCallerItem();
    private LegacyVoidCallerItem() { super(new Properties().rarity(Rarity.EPIC)); }
    @Override public InteractionResult useOn(UseOnContext context) {
        var player = context.getPlayer();
        if (player == null || !player.isShiftKeyDown() || !context.getItemInHand().is(this)) return super.useOn(context);
        if (!LegacyVoidHerrscher.spawn(player, context.getItemInHand(), context.getLevel(), context.getClickedPos())) return InteractionResult.CONSUME;
        if (!context.getLevel().isClientSide() && !player.isCreative()) context.getItemInHand().shrink(1);
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
    }
}
