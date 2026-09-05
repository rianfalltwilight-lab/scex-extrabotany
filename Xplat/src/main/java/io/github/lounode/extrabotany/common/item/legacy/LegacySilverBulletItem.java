package io.github.lounode.extrabotany.common.item.legacy;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import vazkii.botania.api.mana.BasicLensItem;
import vazkii.botania.api.mana.BurstProperties;
import vazkii.botania.common.advancements.ManaBlasterTrigger;
import vazkii.botania.common.entity.ManaBurstEntity;
import vazkii.botania.common.handler.BotaniaSounds;
import vazkii.botania.common.item.ManaBlasterItem;

public final class LegacySilverBulletItem extends ManaBlasterItem {
    public static final LegacySilverBulletItem INSTANCE = new LegacySilverBulletItem();
    private LegacySilverBulletItem() { super(new Properties().stacksTo(1)); }
    @Override public BurstProperties getBurstProps(Player player, ItemStack stack, boolean request, InteractionHand hand) {
        var burst = new BurstProperties(240, 80, 3, 0, 7.5F, 8900346);
        var lens = getLens(stack);
        if (!lens.isEmpty() && lens.getItem() instanceof BasicLensItem lensItem) lensItem.apply(lens, burst, player.level());
        return burst;
    }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (player.isSecondaryUseActive() || !LegacyArmorItem.ITEMS.get("shootingguardian_helm").hasArmorSet(player)) return super.use(level, player, hand);
        if (player.getCooldowns().isOnCooldown(this)) return InteractionResultHolder.pass(stack);
        if (!level.isClientSide()) {
            var burst = new ManaBurstEntity(player);
            var properties = getBurstProps(player, stack, false, hand);
            burst.setSourceLens(getLens(stack)); burst.setColor(properties.color);
            burst.setMana(properties.maxMana); burst.setStartingMana(properties.maxMana);
            burst.setMinManaLoss(properties.ticksBeforeManaLoss); burst.setManaLossPerTick(properties.manaLossPerTick);
            burst.setGravity(properties.gravity); burst.setDeltaMovement(burst.getDeltaMovement().scale(properties.motionModifier));
            level.playSound(null, player.blockPosition(), BotaniaSounds.MANA_BLASTER, SoundSource.PLAYERS, 1, 1);
            level.addFreshEntity(burst);
            if (player instanceof ServerPlayer serverPlayer) ManaBlasterTrigger.INSTANCE.trigger(serverPlayer, stack);
        }
        player.getCooldowns().addCooldown(this, 30);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
