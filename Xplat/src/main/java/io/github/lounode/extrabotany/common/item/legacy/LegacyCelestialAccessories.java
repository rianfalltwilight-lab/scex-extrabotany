package io.github.lounode.extrabotany.common.item.legacy;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import vazkii.botania.api.item.BlockChangedListenerBauble;
import vazkii.botania.api.item.Relic;
import vazkii.botania.common.handler.EquipmentHandler;
import vazkii.botania.common.handler.PixieHandler;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.item.equipment.bauble.BaubleItem;
import vazkii.botania.common.item.equipment.bauble.NimbusAmuletItem;
import vazkii.botania.common.item.equipment.bauble.SnowflakePendantItem;
import vazkii.botania.common.item.relic.RelicBaubleItem;
import vazkii.botania.common.item.relic.RelicImpl;
import java.util.List;

public final class LegacyCelestialAccessories {
    public static final Sun SUN = new Sun();
    public static final Moon MOON = new Moon();
    private static Item.Properties properties() { return new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant(); }
    private LegacyCelestialAccessories() {}
    public static final class Sun extends RelicBaubleItem {
        private Sun() { super(properties()); }
        private List<Item> delegates() { return List.of(BotaniaItems.RING_OF_THE_MANTLE, BotaniaItems.GREATER_BAND_OF_AURA,
                BotaniaItems.RING_OF_CORRECTION, BotaniaItems.RING_OF_CHORDATA, LegacyAccessories.ITEMS.get("death_ring"),
                LegacyAccessories.ITEMS.get("frost_star"), ExtraBotanyItems.dispersiveRing); }
        @Override public void onWornTick(ItemStack stack, LivingEntity user) {
            super.onWornTick(stack, user);
            if (user instanceof Player player && LegacyEternityItem.owned(stack, player))
                for (var item : delegates()) ((BaubleItem) item).onWornTick(stack, user);
        }
        @Override public void onUnequipped(ItemStack stack, LivingEntity user) {
            super.onUnequipped(stack, user);
            ((BaubleItem) BotaniaItems.RING_OF_THE_MANTLE).onUnequipped(stack, user);
            ((BaubleItem) BotaniaItems.RING_OF_CHORDATA).onUnequipped(stack, user);
        }
        @Override public boolean canEquip(ItemStack stack, LivingEntity user) { return super.canEquip(stack, user) && EquipmentHandler.findOrEmpty(this, user).isEmpty(); }
        @Override public Multimap<Holder<Attribute>, AttributeModifier> getEquippedAttributeModifiers(ItemStack stack, ResourceLocation slot) {
            Multimap<Holder<Attribute>, AttributeModifier> result = HashMultimap.create();
            var reach = new AttributeModifier(ResourceLocation.parse("extrabotany:sun_ring_reach"), 3.5, AttributeModifier.Operation.ADD_VALUE);
            result.put(Attributes.BLOCK_INTERACTION_RANGE, reach); result.put(Attributes.ENTITY_INTERACTION_RANGE, reach);
            result.put(PixieHandler.PIXIE_SPAWN_CHANCE, PixieHandler.makeModifier(ResourceLocation.parse("extrabotany:sun_ring_pixie"), .25));
            return result;
        }
    }
    public static final class Moon extends NimbusAmuletItem implements BlockChangedListenerBauble {
        private Moon() { super(properties()); }
        @Override public void inventoryTick(ItemStack stack, Level level, Entity user, int slot, boolean selected) {
            super.inventoryTick(stack, level, user, slot, selected);
            if (!level.isClientSide() && user instanceof Player player) { var relic = Relic.LOOKUP.find(stack); if (relic != null) relic.tickBinding(player); }
        }
        @Override public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flags) {
            super.appendHoverText(stack, context, tooltip, flags); RelicImpl.addDefaultTooltip(stack, tooltip);
        }
        @Override public void onWornTick(ItemStack stack, LivingEntity user) {
            if (!(user instanceof Player player) || !LegacyEternityItem.owned(stack, player)) return;
            super.onWornTick(stack, user);
            for (var item : List.of(BotaniaItems.CRIMSON_PENDANT, BotaniaItems.SNOWFLAKE_PENDANT, BotaniaItems.THIRD_EYE)) ((BaubleItem) item).onWornTick(stack, user);
        }
        @Override public void onChangedBlock(ItemStack stack, LivingEntity user, ServerLevel level, BlockPos pos) {
            ((SnowflakePendantItem) BotaniaItems.SNOWFLAKE_PENDANT).onChangedBlock(stack, user, level, pos);
        }
        @Override public boolean canWalkOnPowderedSnow(ItemStack stack, LivingEntity user) { return true; }
        @Override public boolean canEquip(ItemStack stack, LivingEntity user) { return user instanceof Player player
                && LegacyEternityItem.owned(stack, player) && EquipmentHandler.findOrEmpty(this, user).isEmpty(); }
        @Override public Multimap<Holder<Attribute>, AttributeModifier> getEquippedAttributeModifiers(ItemStack stack, ResourceLocation slot) {
            Multimap<Holder<Attribute>, AttributeModifier> result = HashMultimap.create();
            result.put(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(ResourceLocation.parse("extrabotany:moon_pendant_knockback_resistance"), 1, AttributeModifier.Operation.ADD_VALUE));
            return result;
        }
    }
}
