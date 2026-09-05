package io.github.lounode.extrabotany.common.item.legacy;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import java.util.List;

/** Keeps the scex.1 coordinate contract in CUSTOM_DATA, including unknown keys. */
public final class LegacyBinderItem extends Item {
    public LegacyBinderItem(Properties properties) { super(properties); }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getPlayer() == null || !context.getPlayer().isShiftKeyDown()) return InteractionResult.PASS;
        var position = context.getClickedPos();
        CustomData.update(DataComponents.CUSTOM_DATA, context.getItemInHand(), data -> {
            data.putInt("posx", position.getX());
            data.putInt("posy", position.getY());
            data.putInt("posz", position.getZ());
            data.putString("dim", context.getLevel().dimension().location().toString());
        });
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flags) {
        super.appendHoverText(stack, context, tooltip, flags);
        var data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        for (String axis : List.of("x", "y", "z")) {
            int value = data.contains("pos" + axis) ? data.getInt("pos" + axis) : (axis.equals("y") ? -1 : 0);
            tooltip.add(Component.translatable("item.extrabotany.binder.bind" + axis, value).withStyle(ChatFormatting.GRAY));
        }
        tooltip.add(Component.translatable("item.extrabotany.binder.binddim",
                data.contains("dim") ? data.getString("dim") : "minecraft:overworld").withStyle(ChatFormatting.GRAY));
    }
}
