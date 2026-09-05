package io.github.lounode.extrabotany.common.item.legacy;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.Enchantments;
import vazkii.botania.common.item.equipment.bauble.BaubleItem;
import vazkii.botania.common.handler.EquipmentHandler;
import vazkii.botania.api.mana.ManaItemHandler;
import io.github.lounode.extrabotany.common.handler.DamageHandler;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/** Shared implementation for the legacy elemental and aura accessories. */
public final class LegacyAccessories extends BaubleItem {
    public static final Map<String, LegacyAccessories> ITEMS = new LinkedHashMap<>();
    private static final Set<String> ELEMENTS = Set.of("aero_stone", "aqua_stone", "earth_stone", "ignis_stone");
    private final String id;

    static {
        for (String id : new String[] {"aero_stone", "aqua_stone", "earth_stone", "ignis_stone",
                "the_community", "power_glove", "peace_amulet", "potato_chips", "death_ring", "frost_star", "gem_of_conquest"}) {
            ITEMS.put(id, new LegacyAccessories(id));
        }
    }
    private LegacyAccessories(String id) { super(new Item.Properties().stacksTo(1)); this.id = id; }

    public static boolean worn(String id, LivingEntity user) {
        Item item = ITEMS.get(id);
        return item != null && !EquipmentHandler.findOrEmpty(item, user).isEmpty();
    }

    @Override public boolean canEquip(ItemStack stack, LivingEntity user) {
        if (worn(id, user)) return false;
        if (ELEMENTS.contains(id)) return !worn("the_community", user);
        return !id.equals("the_community") || ELEMENTS.stream().noneMatch(element -> worn(element, user));
    }

    @Override public Multimap<Holder<Attribute>, AttributeModifier> getEquippedAttributeModifiers(ItemStack stack, ResourceLocation slot) {
        Multimap<Holder<Attribute>, AttributeModifier> result = HashMultimap.create();
        if (id.equals("aero_stone") || id.equals("the_community")) {
            add(result, Attributes.MOVEMENT_SPEED, "movement_speed", .15, false);
            add(result, Attributes.FLYING_SPEED, "flying_speed", .15, false);
        }
        if (id.equals("earth_stone") || id.equals("the_community")) add(result, Attributes.ARMOR, "armor", 4, true);
        if (id.equals("ignis_stone") || id.equals("the_community")) add(result, Attributes.ATTACK_DAMAGE, "attack_damage", .1, false);
        if (id.equals("power_glove")) add(result, Attributes.ATTACK_SPEED, "attack_speed", .12, false);
        if (id.equals("gem_of_conquest")) {
            add(result, Attributes.ATTACK_DAMAGE, "attack_damage", .15, false);
            add(result, Attributes.ATTACK_SPEED, "attack_speed", .1, false);
            add(result, Attributes.MOVEMENT_SPEED, "movement_speed", .05, false);
        }
        return result;
    }

    private void add(Multimap<Holder<Attribute>, AttributeModifier> attributes, Holder<Attribute> attribute,
                     String name, double amount, boolean flat) {
        attributes.put(attribute, new AttributeModifier(ResourceLocation.fromNamespaceAndPath("extrabotany", id + "_" + name),
                amount, flat ? AttributeModifier.Operation.ADD_VALUE : AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }

    public static void frostWalker(ServerPlayer user, ItemStack stack, int strength) {
        boolean grounded = user.onGround();
        user.setOnGround(true);
        try {
            var enchantment = user.serverLevel().holderLookup(Registries.ENCHANTMENT).getOrThrow(Enchantments.FROST_WALKER).value();
            var inUse = new EnchantedItemInUse(stack, EquipmentSlot.FEET, user);
            for (var effect : enchantment.getEffects(EnchantmentEffectComponents.LOCATION_CHANGED)) {
                effect.effect().onChangedBlock(user.serverLevel(), strength, inUse, user, user.position(), false);
            }
        } finally { user.setOnGround(grounded); }
    }

    @Override public void onWornTick(ItemStack stack, LivingEntity user) {
        super.onWornTick(stack, user);
        if (!(user instanceof ServerPlayer player)) return;
        if (id.equals("gem_of_conquest")) {
            if (player.tickCount % 40 == 0 && player.getLastHurtMob() != null && player.tickCount - player.getLastHurtMobTimestamp() <= 100)
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 60, 0, true, false, true));
            return;
        }
        boolean frost = id.equals("frost_star");
        if (frost) frostWalker(player, stack, 8);
        if ((!frost && !id.equals("death_ring")) || player.tickCount % (frost ? 20 : 30) != 0) return;
        for (var target : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(6))) {
            if (target == player || !player.hasLineOfSight(target) || !DamageHandler.INSTANCE.checkPassable(target, player)) continue;
            if (!ManaItemHandler.instance().requestManaExactForTool(stack, player, frost ? 30 : 80, true)) break;
            if (frost) target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 4));
            else {
                target.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 1));
                target.addEffect(new MobEffectInstance(MobEffects.UNLUCK, 60, 1));
                target.setHealth(Math.max(1, target.getHealth() - .5F));
                target.hurt(player.damageSources().indirectMagic(player, player), .01F);
            }
        }
    }
}
