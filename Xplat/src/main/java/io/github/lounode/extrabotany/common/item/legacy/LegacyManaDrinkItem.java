package io.github.lounode.extrabotany.common.item.legacy;

import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import vazkii.botania.api.mana.ManaItemHandler;
import java.util.List;

public final class LegacyManaDrinkItem extends Item {
    public LegacyManaDrinkItem(Properties properties) { super(properties); }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity user) {
        if (user instanceof ServerPlayer player) {
            CriteriaTriggers.CONSUME_ITEM.trigger(player, stack);
            player.awardStat(Stats.ITEM_USED.get(this));
        }
        if (level.isClientSide() || !(user instanceof Player player)) return stack;
        player.heal(5);
        for (var effect : List.of(MobEffects.DAMAGE_RESISTANCE, MobEffects.DAMAGE_BOOST,
                MobEffects.MOVEMENT_SPEED, MobEffects.JUMP)) {
            player.addEffect(new MobEffectInstance(effect, 1200, 0));
        }
        ManaItemHandler.instance().dispatchManaExact(stack, player, 10000, true);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
            var bottle = new ItemStack(ExtraBotanyItems.manaGlassBottle);
            if (stack.isEmpty()) return bottle;
            if (!player.getInventory().add(bottle)) player.drop(bottle, false);
        }
        return stack;
    }

    @Override public int getUseDuration(ItemStack stack, LivingEntity user) { return 32; }
    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.DRINK; }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player user, InteractionHand hand) {
        user.startUsingItem(hand);
        return InteractionResultHolder.consume(user.getItemInHand(hand));
    }
}
