package io.github.lounode.extrabotany.client.model;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;

import io.github.lounode.extrabotany.client.model.armor.GoblinSlayerArmorModel;
import io.github.lounode.extrabotany.client.model.armor.PleiadesCombatMaidArmorModel;
import io.github.lounode.extrabotany.client.model.armor.ShadowWarriorArmorModel;
import io.github.lounode.extrabotany.client.model.armor.StarryIdolArmorModel;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class ExtrabotanyLayerDefinitions {

	public static void init(BiConsumer<ModelLayerLocation, Supplier<LayerDefinition>> consumer) {
		consumer.accept(ExtrabotanyModelLayers.STARRY_IDOL_ARMOR_NORMAL, () -> LayerDefinition.create(StarryIdolArmorModel.createNormalMesh(), 128, 128));
		consumer.accept(ExtrabotanyModelLayers.STARRY_IDOL_ARMOR_DRESS, () -> LayerDefinition.create(StarryIdolArmorModel.createDressMesh(), 128, 128));
		consumer.accept(ExtrabotanyModelLayers.PLEIADES_COMBAT_MAID_ARMOR_NORMAL, () -> LayerDefinition.create(PleiadesCombatMaidArmorModel.createNormalMesh(), 128, 128));
		consumer.accept(ExtrabotanyModelLayers.PLEIADES_COMBAT_MAID_ARMOR_DRESS, () -> LayerDefinition.create(PleiadesCombatMaidArmorModel.createDressMesh(), 128, 128));
		consumer.accept(ExtrabotanyModelLayers.SHADOW_WARRIOR_ARMOR_NORMAL, () -> LayerDefinition.create(ShadowWarriorArmorModel.createNormalMesh(), 128, 128));
		consumer.accept(ExtrabotanyModelLayers.SHADOW_WARRIOR_ARMOR_LEGGINGS, () -> LayerDefinition.create(ShadowWarriorArmorModel.createLeggingsMesh(), 128, 128));
		consumer.accept(ExtrabotanyModelLayers.GOBLIN_SLAYER_ARMOR_NORMAL, () -> LayerDefinition.create(GoblinSlayerArmorModel.createNormalMesh(), 128, 128));
		consumer.accept(ExtrabotanyModelLayers.GOBLIN_SLAYER_ARMOR_LEGGINGS, () -> LayerDefinition.create(GoblinSlayerArmorModel.createLeggingsMesh(), 128, 128));
	}
}
