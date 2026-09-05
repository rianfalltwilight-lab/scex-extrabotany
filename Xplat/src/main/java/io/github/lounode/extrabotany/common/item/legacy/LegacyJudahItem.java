package io.github.lounode.extrabotany.common.item.legacy;

import io.github.lounode.extrabotany.common.entity.LegacyJudahEntity;
import io.github.lounode.extrabotany.xplat.EXplatAbstractions;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.item.relic.RelicImpl;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class LegacyJudahItem extends Item {
    public static final Map<String, LegacyJudahItem> ITEMS = new LinkedHashMap<>();
    static { ITEMS.put("judah_oath", new LegacyJudahItem(0)); ITEMS.put("judah_oath_kira", new LegacyJudahItem(1)); ITEMS.put("judah_oath_sakura", new LegacyJudahItem(2)); }
    public final int variant;
    private LegacyJudahItem(int variant) { super(new Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant()); this.variant = variant; }
    public static LegacyJudahItem itemFor(int variant) { return ITEMS.get(variant == 1 ? "judah_oath_kira" : variant == 2 ? "judah_oath_sakura" : "judah_oath"); }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (level.isClientSide()) return InteractionResultHolder.sidedSuccess(stack, true);
        var relic = EXplatAbstractions.INSTANCE.findRelic(stack); if (relic != null) relic.tickBinding(player);
        if (relic == null || !relic.isRightPlayer(player) || !ManaItemHandler.instance().requestManaExactForTool(stack, player, 2500, true)) return InteractionResultHolder.fail(stack);
        player.getCooldowns().addCooldown(this, 80);
        level.playSound(null, player.blockPosition(), SoundEvents.TRIDENT_THROW.value(), SoundSource.PLAYERS, .8F, .8F);
        var oath = LegacyJudahEntity.create(false, player, variant, false); oath.setPos(player.position().add(0, 1, 0)); oath.setYRot(player.getYRot());
        oath.shootFromRotation(player, player.getXRot(), player.getYRot(), 0, .5F, 0); level.addFreshEntity(oath);
        return InteractionResultHolder.sidedSuccess(stack, false);
    }
    @Override public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!level.isClientSide() && entity instanceof Player player) { var relic = EXplatAbstractions.INSTANCE.findRelic(stack); if (relic != null) relic.tickBinding(player); }
        super.inventoryTick(stack, level, entity, slot, selected);
    }
    @Override public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flags) {
        tooltip.add(Component.translatable("tooltip.extrabotany.judah_oath").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.empty()); RelicImpl.addDefaultTooltip(stack, tooltip);
    }
}
