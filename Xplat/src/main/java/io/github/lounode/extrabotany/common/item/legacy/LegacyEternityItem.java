package io.github.lounode.extrabotany.common.item.legacy;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import io.github.lounode.extrabotany.common.brew.ExtraBotanyMobEffects;
import io.github.lounode.extrabotany.common.item.equipment.bauble.FrostRingItem;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.entity.BlockEntity;
import vazkii.botania.api.item.Relic;
import vazkii.botania.api.mana.ManaItem;
import vazkii.botania.common.handler.EquipmentHandler;
import vazkii.botania.common.item.relic.RelicBaubleItem;

public final class LegacyEternityItem extends RelicBaubleItem {
    public static final LegacyEternityItem INSTANCE = new LegacyEternityItem();
    private LegacyEternityItem() { super(new Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant()); }
    public static boolean owned(ItemStack stack, Player user) {
        var relic = Relic.LOOKUP.find(stack);
        return relic != null && relic.isRightPlayer(user);
    }
    @Override public boolean canEquip(ItemStack stack, LivingEntity user) {
        return super.canEquip(stack, user) && EquipmentHandler.findOrEmpty(this, user).isEmpty();
    }
    @Override public Multimap<Holder<Attribute>, AttributeModifier> getEquippedAttributeModifiers(ItemStack stack, ResourceLocation slot) {
        Multimap<Holder<Attribute>, AttributeModifier> modifiers = HashMultimap.create();
        modifiers.put(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(ResourceLocation.parse("extrabotany:silent_eternity_knockback_resistance"), 1, AttributeModifier.Operation.ADD_VALUE));
        return modifiers;
    }
    @Override public void onWornTick(ItemStack stack, LivingEntity living) {
        super.onWornTick(stack, living);
        if (!(living instanceof Player player) || !owned(stack, player)) return;
        new Reservoir(stack).addMana(666);
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        LegacyAccessories.frostWalker(serverPlayer, stack, 4);
        boolean grounded = player.onGround();
        player.setOnGround(true);
        try { FrostRingItem.freezeLava(player, player.level(), player.blockPosition(), 4); }
        finally { player.setOnGround(grounded); }
        CustomData.update(DataComponents.CUSTOM_DATA, stack, data -> {
            double dx = player.getX() - (data.contains("posx") ? Double.longBitsToDouble(data.getLong("posx")) : player.getX());
            double dy = player.getY() - (data.contains("posy") ? Double.longBitsToDouble(data.getLong("posy")) : player.getY());
            double dz = player.getZ() - (data.contains("posz") ? Double.longBitsToDouble(data.getLong("posz")) : player.getZ());
            int ticks = dx * dx + dy * dy + dz * dz <= .0001 ? data.getInt("stopticks") + 1 : 0;
            data.putInt("stopticks", ticks);
            data.putLong("posx", Double.doubleToLongBits(player.getX()));
            data.putLong("posy", Double.doubleToLongBits(player.getY()));
            data.putLong("posz", Double.doubleToLongBits(player.getZ()));
            if (ticks > 15) {
                player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + .4F));
                player.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.ETERNITY, 10));
            }
        });
    }
    public record Reservoir(ItemStack stack) implements ManaItem {
        @Override public int getMana() { return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt("mana"); }
        @Override public int getMaxMana() { return 666; }
        @Override public void addMana(int amount) {
            int mana = Math.max(0, Math.min(666, getMana() + amount));
            CustomData.update(DataComponents.CUSTOM_DATA, stack, data -> { if (mana == 0) data.remove("mana"); else data.putInt("mana", mana); });
        }
        @Override public boolean canReceiveManaFromPool(BlockEntity pool) { return false; }
        @Override public boolean acceptDispatchedManaFromItem(ItemStack other) { return false; }
        @Override public boolean refuseRequestedManaFromItem(ItemStack other) { return true; }
        @Override public boolean canDrainManaToPool(BlockEntity pool) { return false; }
        @Override public boolean canSendRequestedManaToItem(ItemStack other) { return true; }
        @Override public boolean isNoExport() { return false; }
    }
}
