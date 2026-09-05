package io.github.lounode.extrabotany.client.model;

import net.minecraft.client.model.geom.ModelLayerLocation;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class ExtrabotanyModelLayers {
	public static final ModelLayerLocation STARRY_IDOL_ARMOR_NORMAL = make("starry_idol_armor_normal");
	public static final ModelLayerLocation STARRY_IDOL_ARMOR_DRESS = make("starry_idol_armor_dress");
	public static final ModelLayerLocation PLEIADES_COMBAT_MAID_ARMOR_NORMAL = make("pleiades_combat_maid_armor_normal");
	public static final ModelLayerLocation PLEIADES_COMBAT_MAID_ARMOR_DRESS = make("pleiades_combat_maid_armor_dress");
	public static final ModelLayerLocation SHADOW_WARRIOR_ARMOR_NORMAL = make("shadow_warrior_armor_normal");
	public static final ModelLayerLocation SHADOW_WARRIOR_ARMOR_LEGGINGS = make("shadow_warrior_leggings");
	public static final ModelLayerLocation GOBLIN_SLAYER_ARMOR_NORMAL = make("goblin_slayer_armor_normal");
	public static final ModelLayerLocation GOBLIN_SLAYER_ARMOR_LEGGINGS = make("goblin_slayer_armor_leggings");

	private static ModelLayerLocation make(String name) {
		return make(name, "main");
	}

	private static ModelLayerLocation make(String name, String layer) {
		// Don't add to vanilla's ModelLayers. It seems to only be used for error checking
		// And would be annoying to do under Forge's parallel mod loading
		return new ModelLayerLocation(prefix(name), layer);
	}

	public static void init() {}
}
