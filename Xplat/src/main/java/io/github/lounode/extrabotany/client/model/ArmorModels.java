package io.github.lounode.extrabotany.client.model;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.client.model.armor.ArmorModel;

import io.github.lounode.extrabotany.common.item.equipment.armor.goblin_slayer.GoblinSlayerArmorItem;
import io.github.lounode.extrabotany.common.item.equipment.armor.pleiades_combat_maid.PleiadesCombatMaidArmorItem;
import io.github.lounode.extrabotany.common.item.equipment.armor.shadow_warrior.ShadowWarriorArmorItem;
import io.github.lounode.extrabotany.common.item.equipment.armor.starry_idol.StarryIdolArmorItem;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class ArmorModels {

	private static Map<EquipmentSlot, ArmorModel> starryIdol = Collections.emptyMap();
	private static Map<EquipmentSlot, ArmorModel> pleiadesCombatMaid = Collections.emptyMap();
	private static Map<EquipmentSlot, ArmorModel> shadowWarrior = Collections.emptyMap();
	private static Map<EquipmentSlot, ArmorModel> goblinSlayer = Collections.emptyMap();
	private static Map<EquipmentSlot, ArmorModel> legacy = Collections.emptyMap();

	private static Map<EquipmentSlot, ArmorModel> make(EntityRendererProvider.Context ctx, ModelLayerLocation inner, ModelLayerLocation outer) {
		Map<EquipmentSlot, ArmorModel> ret = new EnumMap<>(EquipmentSlot.class);
		for (var slot : EquipmentSlot.values()) {
			var mesh = ctx.bakeLayer(slot == EquipmentSlot.LEGS ? inner : outer);
			ret.put(slot, new ArmorModel(mesh, slot));
		}
		return ret;
	}

	private static Map<EquipmentSlot, ArmorModel> make(EntityRendererProvider.Context ctx, ModelLayerLocation inner) {
		Map<EquipmentSlot, ArmorModel> ret = new EnumMap<>(EquipmentSlot.class);
		for (var slot : EquipmentSlot.values()) {
			var mesh = ctx.bakeLayer(inner);
			ret.put(slot, new ArmorModel(mesh, slot));
		}
		return ret;
	}

	private static Map<EquipmentSlot, ArmorModel> makeIdol(EntityRendererProvider.Context ctx, ModelLayerLocation layer, ModelLayerLocation dressLayer) {
		Map<EquipmentSlot, ArmorModel> ret = new EnumMap<>(EquipmentSlot.class);
		for (var slot : EquipmentSlot.values()) {
			var mesh = ctx.bakeLayer(slot != EquipmentSlot.LEGS ? layer : dressLayer);
			ret.put(slot, new ArmorModel(mesh, slot));
		}
		return ret;
	}

	public static void init(EntityRendererProvider.Context ctx) {
		legacy = make(ctx, ExtrabotanyModelLayers.SHADOW_WARRIOR_ARMOR_LEGGINGS, ExtrabotanyModelLayers.SHADOW_WARRIOR_ARMOR_NORMAL);
		starryIdol = makeIdol(ctx, ExtrabotanyModelLayers.STARRY_IDOL_ARMOR_NORMAL, ExtrabotanyModelLayers.STARRY_IDOL_ARMOR_DRESS);
		pleiadesCombatMaid = makeIdol(ctx, ExtrabotanyModelLayers.PLEIADES_COMBAT_MAID_ARMOR_NORMAL, ExtrabotanyModelLayers.PLEIADES_COMBAT_MAID_ARMOR_DRESS);
		shadowWarrior = makeIdol(ctx, ExtrabotanyModelLayers.SHADOW_WARRIOR_ARMOR_NORMAL, ExtrabotanyModelLayers.SHADOW_WARRIOR_ARMOR_LEGGINGS);
		goblinSlayer = makeIdol(ctx, ExtrabotanyModelLayers.GOBLIN_SLAYER_ARMOR_NORMAL, ExtrabotanyModelLayers.GOBLIN_SLAYER_ARMOR_LEGGINGS);
	}

	@Nullable
	public static ArmorModel get(ItemStack stack) {
		Item item = stack.getItem();
		if (item instanceof PleiadesCombatMaidArmorItem maid) {
			return pleiadesCombatMaid.get(maid.getEquipmentSlot());
		}

		if (item instanceof ShadowWarriorArmorItem warrior) {
			return shadowWarrior.get(warrior.getEquipmentSlot());
		}

		if (item instanceof GoblinSlayerArmorItem goblin) {
			return goblinSlayer.get(goblin.getEquipmentSlot());
		}

		if (item instanceof StarryIdolArmorItem idol) {
			if (item instanceof io.github.lounode.extrabotany.common.item.legacy.LegacyArmorItem) return legacy.get(idol.getEquipmentSlot());
			return starryIdol.get(idol.getEquipmentSlot());
		}

		return null;
	}
}
