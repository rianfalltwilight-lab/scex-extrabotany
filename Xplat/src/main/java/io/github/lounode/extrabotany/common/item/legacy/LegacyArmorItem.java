package io.github.lounode.extrabotany.common.item.legacy;

import io.github.lounode.extrabotany.common.item.equipment.armor.starry_idol.StarryIdolArmorItem;
import io.github.lounode.extrabotany.common.item.material.ArmorsMaterial;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import vazkii.botania.api.mana.ManaDiscountArmor;
import vazkii.botania.api.mana.ManaItemHandler;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class LegacyArmorItem extends StarryIdolArmorItem implements ManaDiscountArmor {
    public static final Map<String, LegacyArmorItem> ITEMS = new LinkedHashMap<>();
    private static final Type[] SLOTS = {Type.HELMET, Type.CHESTPLATE, Type.LEGGINGS, Type.BOOTS};
    private static final String[] SUFFIXES = {"helm", "chest", "legs", "boots"};
    private final String set;
    static {
        addSet("miku", ArmorsMaterial.MIKU, 5);
        addSet("shootingguardian", ArmorsMaterial.SHOOTING_GUARDIAN, 34);
        addSet("silentsages", ArmorsMaterial.SILENT_SAGES, 50);
    }
    private static void addSet(String name, Holder<ArmorMaterial> material, int durability) {
        for (int i = 0; i < SLOTS.length; i++) ITEMS.put(name + "_" + SUFFIXES[i], new LegacyArmorItem(name, material, SLOTS[i], durability));
    }
    private LegacyArmorItem(String set, Holder<ArmorMaterial> material, Type type, int durability) {
        super(material, type, new Properties().durability(type.getDurability(durability))); this.set = set;
    }
    @Override public int getManaPerDamage() { return 70; }
    @Override public void inventoryTick(ItemStack stack, Level level, Entity owner, int slot, boolean selected) {
        triggerAdvancement(owner);
        if (!level.isClientSide() && owner instanceof Player player && stack.getDamageValue() > 0
                && ManaItemHandler.instance().requestManaExact(stack, player, 140, true)) stack.setDamageValue(stack.getDamageValue() - 1);
    }
    @Override public float getDiscount(ItemStack stack, int slot, Player player, ItemStack tool) { return hasArmorSet(player) ? .15F : 0; }
    @Override public boolean hasArmorSetItem(Player player, EquipmentSlot slot) {
        return player != null && player.getItemBySlot(slot).getItem() instanceof LegacyArmorItem armor
                && armor.set.equals(set) && armor.getEquipmentSlot() == slot;
    }
    @Override public ItemStack[] getArmorSetStacks() {
        return java.util.Arrays.stream(SUFFIXES).map(suffix -> new ItemStack(ITEMS.get(set + "_" + suffix))).toArray(ItemStack[]::new);
    }
    @Override public MutableComponent getArmorSetName() { return Component.translatable("extrabotany.armorset." + set + ".name"); }
    @Override public void addArmorSetDescription(ItemStack stack, List<Component> lines, boolean complete) {
        for (int i = 0; i < (set.equals("shootingguardian") ? 4 : 1); i++) {
            lines.add(Component.translatable("extrabotany.armorset." + set + ".desc" + (set.equals("shootingguardian") ? i : ""))
                    .withStyle(complete ? ChatFormatting.AQUA : ChatFormatting.GRAY));
        }
    }
    @Override public String getArmorTextureAfterInk(ItemStack stack, EquipmentSlot slot) {
        return "extrabotany:textures/models/armor/" + (set.equals("silentsages") ? "shadow_warrior" : set)
                + (set.equals("shootingguardian") && slot == EquipmentSlot.HEAD ? "_helmet" : "") + ".png";
    }
    @Override public ItemAttributeModifiers getDefaultAttributeModifiers() {
        var attributes = super.getDefaultAttributeModifiers();
        if (!set.equals("shootingguardian")) return attributes;
        var slot = EquipmentSlotGroup.bySlot(getEquipmentSlot());
        var values = List.of(Attributes.FLYING_SPEED, Attributes.MOVEMENT_SPEED, Attributes.ATTACK_DAMAGE, Attributes.ATTACK_SPEED);
        var names = List.of("flying_speed", "movement_speed", "attack_damage", "attack_speed");
        for (int i = 0; i < values.size(); i++) attributes = attributes.withModifierAdded(values.get(i),
                new AttributeModifier(ResourceLocation.fromNamespaceAndPath("extrabotany", set + "_" + names.get(i) + "." + type.getName()),
                        i == 3 ? .03 : .1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), slot);
        return attributes;
    }
}
