package io.github.lounode.extrabotany.data.tags;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

import io.github.lounode.extrabotany.common.entity.ExtraBotanyEntityType;
import io.github.lounode.extrabotany.common.lib.ExtraBotanyTags;
import io.github.lounode.extrabotany.common.lib.LibMisc;

import java.util.concurrent.CompletableFuture;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.location;

public class EntityTypeTagProvider extends IntrinsicHolderTagsProvider<EntityType<?>> {

	public EntityTypeTagProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
		super(packOutput, Registries.ENTITY_TYPE, lookupProvider,
				entityType -> BuiltInRegistries.ENTITY_TYPE.getResourceKey(entityType).orElseThrow(), LibMisc.MOD_ID, null);
	}

	@Override
	protected void addTags(HolderLookup.Provider provider) {
		tag(common("bosses")).add(ExtraBotanyEntityType.GAIA_LEGACY, ExtraBotanyEntityType.GAIA_III);

		tag(ExtraBotanyTags.Entities.GOBLINS)
				.addOptional(location("goblins_tyranny", "goblin_huntsman"))
				.addOptional(location("goblins_tyranny", "goblin_hunter"))
				.addOptional(location("goblins_tyranny", "engineeress_goblin"))
				.addOptional(location("goblins_tyranny", "engineer_goblin"))
				.addOptional(location("goblins_tyranny", "shaman_goblin"))
				.addOptional(location("goblins_tyranny", "champion_goblin"))
				.addOptional(location("goblins_tyranny", "leader_goblin"))
				.addOptional(location("goblins_tyranny", "knight_goblin"))
				.addOptional(location("goblins_tyranny", "mini_goblin_3"))
				.addOptional(location("goblins_tyranny", "mini_goblin_2"))
				.addOptional(location("goblins_tyranny", "mini_goblin_1"))
				.addOptional(location("goblins_tyranny", "bard"))
				.addOptional(location("goblins_tyranny", "gold_cave_goblin"))
				.addOptional(location("goblins_tyranny", "iron_cave_goblin"))
				.addOptional(location("goblins_tyranny", "copper_cave_goblin"))
				.addOptional(location("goblins_tyranny", "coal_cave_goblin"))
				.addOptional(location("goblins_tyranny", "mini_drunk_gob_3"))
				.addOptional(location("goblins_tyranny", "mini_drunk_gob_2"))
				.addOptional(location("goblins_tyranny", "mini_drunk_gob_1"))
				.addOptional(location("goblins_tyranny", "wanderer_goblin"))
				.addOptional(location("goblins_tyranny", "merchant"))
				.addOptional(location("goblins_tyranny", "bartender_goblin"))
				.addOptional(location("goblins_tyranny", "blacksmith_goblin"))
				.addOptional(location("goblins_tyranny", "grobot"))
				.addOptional(location("goblins_tyranny", "droblin"))
				.addOptional(location("aoa3", "goblin"))
				.addOptional(location("goblintraders", "vein_goblin_trader"))
				.addOptional(location("goblintraders", "goblin_trader"))
				.addOptional(location("apotheosis", "twilight_treasure_goblin"))
				.addOptional(location("twilightforest", "block_and_chain_goblin"))
				.addOptional(location("twilightforest", "upper_goblin_knight"))
				.addOptional(location("twilightforest", "lower_goblin_knight"))
				.addOptional(location("twilightforest", "kobold"))
				.addOptional(location("twilightforest", "redcap"));

	}

	private static TagKey<EntityType<?>> common(String name) {
		return TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath("c", name));
	}

}
