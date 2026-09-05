package io.github.lounode.extrabotany.common.item.legacy;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import vazkii.botania.api.item.Relic;
import vazkii.botania.common.handler.EquipmentHandler;
import vazkii.botania.common.item.BaubleBoxItem;
import vazkii.botania.common.item.equipment.bauble.BaubleItem;
import vazkii.botania.common.item.relic.RelicBaubleItem;
import java.util.*;
import java.util.function.Predicate;

/** Simulate unique, unequipped accessories from the first inventory bauble box. */
public final class LegacyBoxSimulator extends RelicBaubleItem {
    public static final LegacyBoxSimulator ELVEN_KING = new LegacyBoxSimulator(3);
    public static final LegacyBoxSimulator ALL_FOR_ONE = new LegacyBoxSimulator(24);
    private static final Map<Player, State> STATES = Collections.synchronizedMap(new WeakHashMap<>());
    private final int slots;
    private LegacyBoxSimulator(int slots) { super(new Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant()); this.slots = slots; }
    @Override public void appendHoverText(ItemStack stack, TooltipContext context, List<net.minecraft.network.chat.Component> tooltip, net.minecraft.world.item.TooltipFlag flags) {
        tooltip.add(net.minecraft.network.chat.Component.translatable(getDescriptionId() + ".tooltip").withStyle(net.minecraft.ChatFormatting.GRAY));
        super.appendHoverText(stack, context, tooltip, flags);
    }
    private static final class State {
        long last = Long.MIN_VALUE;
        Map<Item, ItemStack> items = Map.of();
        Map<ResourceLocation, Holder<Attribute>> attributes = Map.of();
    }
    private record Box(SimpleContainer inventory, int size) {}
    private static Box box(Player player, int limit) {
        var inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            var candidate = inventory.getItem(i);
            if (candidate.getItem() instanceof BaubleBoxItem) {
                var contents = BaubleBoxItem.getInventory(candidate);
                return new Box(contents, Math.min(limit, contents.getContainerSize()));
            }
        }
        return null;
    }
    private static boolean allowed(ItemStack stack, Player player) {
        if (!(stack.getItem() instanceof BaubleItem) || stack.getItem() instanceof LegacyBoxSimulator) return false;
        var relic = Relic.LOOKUP.find(stack);
        return relic == null || relic.isRightPlayer(player);
    }
    public static ItemStack find(Predicate<ItemStack> predicate, LivingEntity living) {
        if (!(living instanceof Player player)) return ItemStack.EMPTY;
        var worn = EquipmentHandler.getAllWorn(living);
        if (worn == null) return ItemStack.EMPTY;
        int limit = 0;
        for (int i = 0; i < worn.getContainerSize(); i++) {
            var stack = worn.getItem(i);
            if (stack.getItem() instanceof LegacyBoxSimulator simulator && LegacyEternityItem.owned(stack, player)) limit = Math.max(limit, simulator.slots);
        }
        var box = limit == 0 ? null : box(player, limit);
        if (box != null) for (int i = 0; i < box.size; i++) {
            var stack = box.inventory.getItem(i);
            if (allowed(stack, player) && predicate.test(stack)) return stack;
        }
        return ItemStack.EMPTY;
    }
    @Override public boolean canEquip(ItemStack stack, LivingEntity living) {
        if (!super.canEquip(stack, living)) return false;
        var worn = EquipmentHandler.getAllWorn(living);
        if (worn != null) for (int i = 0; i < worn.getContainerSize(); i++) if (worn.getItem(i).getItem() instanceof LegacyBoxSimulator) return false;
        return true;
    }
    @Override public void onWornTick(ItemStack stack, LivingEntity living) {
        super.onWornTick(stack, living);
        if (!(living instanceof Player player) || !LegacyEternityItem.owned(stack, player)) return;
        var state = STATES.computeIfAbsent(player, ignored -> new State());
        long now = player.level().getGameTime();
        if (state.last == now) return;
        state.last = now;
        var box = box(player, slots);
        Map<Item, ItemStack> simulated = new LinkedHashMap<>();
        var worn = EquipmentHandler.getAllWorn(player);
        Set<Item> actual = new HashSet<>();
        if (worn != null) for (int i = 0; i < worn.getContainerSize(); i++) actual.add(worn.getItem(i).getItem());
        if (box != null) for (int i = 0; i < box.size; i++) {
            var candidate = box.inventory.getItem(i);
            if (allowed(candidate, player) && !actual.contains(candidate.getItem())) simulated.putIfAbsent(candidate.getItem(), candidate);
        }
        if (!player.level().isClientSide()) {
            state.items.forEach((item, previous) -> { if (!simulated.containsKey(item)) ((BaubleItem) item).onUnequipped(previous, player); });
            simulated.forEach((item, current) -> { if (!state.items.containsKey(item)) ((BaubleItem) item).onEquipped(current, player); });
            state.items = simulated;
            // Remove our previous modifiers before rebuilding; unrelated attributes are untouched.
            state.attributes.forEach((id, attribute) -> { var instance = player.getAttribute(attribute); if (instance != null) instance.removeModifier(id); });
            Map<ResourceLocation, Holder<Attribute>> applied = new HashMap<>();
            simulated.forEach((item, current) -> ((BaubleItem) item).getEquippedAttributeModifiers(current, ResourceLocation.parse("extrabotany:bauble_box")).forEach((attribute, modifier) -> {
                var id = ResourceLocation.fromNamespaceAndPath("extrabotany", ("box_sim/" + BuiltInRegistries.ITEM.getKey(item) + "/" + modifier.id()).replace(':', '.'));
                var instance = player.getAttribute(attribute);
                if (instance != null && !applied.containsKey(id)) {
                    if (!instance.hasModifier(id)) instance.addTransientModifier(new AttributeModifier(id, modifier.amount(), modifier.operation()));
                    applied.put(id, attribute);
                }
            }));
            state.attributes = applied;
        }
        simulated.forEach((item, current) -> ((BaubleItem) item).onWornTick(current, player));
        if (!player.level().isClientSide() && box != null && !simulated.isEmpty()) box.inventory.setChanged();
    }
    @Override public void onUnequipped(ItemStack stack, LivingEntity living) {
        super.onUnequipped(stack, living);
        if (!(living instanceof Player player) || player.level().isClientSide()) return;
        var state = STATES.remove(player);
        if (state == null) return;
        state.items.forEach((item, previous) -> ((BaubleItem) item).onUnequipped(previous, player));
        state.attributes.forEach((id, attribute) -> { var instance = player.getAttribute(attribute); if (instance != null) instance.removeModifier(id); });
    }
}
