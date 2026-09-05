package io.github.lounode.extrabotany.common.lib;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class ExtraBotanyTags {
	public static class Items {
		public static final TagKey<Item> INGOTS_ORICHALCOS = tag("orichalcos_ingots");
		public static final TagKey<Item> INGOTS_PHOTONIUM = tag("photonium_ingots");
		public static final TagKey<Item> INGOTS_SHADOWIUM = tag("shadowium_ingots");
		public static final TagKey<Item> INGOTS_AERIALITE = tag("aerialite_ingots");

		public static final TagKey<Item> NUGGETS_ORICHALCOS = tag("orichalcos_nuggets");
		public static final TagKey<Item> NUGGETS_PHOTONIUM = tag("photonium_nuggets");
		public static final TagKey<Item> NUGGETS_SHADOWIUM = tag("shadowium_nuggets");
		public static final TagKey<Item> NUGGETS_AERIALITE = tag("aerialite_nuggets");

		public static final TagKey<Item> BLOCKS_ORICHALCOS = tag("orichalcos_blocks");
		public static final TagKey<Item> BLOCKS_PHOTONIUM = tag("photonium_blocks");
		public static final TagKey<Item> BLOCKS_SHADOWIUM = tag("shadowium_blocks");
		public static final TagKey<Item> BLOCKS_AERIALITE = tag("aerialite_blocks");

		public static final TagKey<Item> HAMMERS = tag("hammers");
		public static final TagKey<Item> PEDESTALS = tag("pedestals");
		public static final TagKey<Item> REWARD_BAGS = tag("reward_bags");
		public static final TagKey<Item> MANA_POOLS = tag("botania", "mana_pools");
		public static final TagKey<Item> CHARGERS = tag("chargers");
		public static final TagKey<Item> ANNOYING_FLOWER_EATABLE = tag("annoying_flower_eatable");
		public static final TagKey<Item> SHIELDS = tag("shields");
		public static final TagKey<Item> MUSIC_DISCS = tag("c", "music_discs");

		private static TagKey<Item> tag(String name) {
			return TagKey.create(Registries.ITEM, prefix(name));
		}

		private static TagKey<Item> tag(String namespace, String name) {
			return TagKey.create(Registries.ITEM, ResourceLocation.tryBuild(namespace, name));
		}
	}

	public static class Blocks {
		public static final TagKey<Block> BLOCKS_ORICHALCOS = tag("orichalcos_blocks");
		public static final TagKey<Block> BLOCKS_PHOTONIUM = tag("photonium_blocks");
		public static final TagKey<Block> BLOCKS_SHADOWIUM = tag("shadowium_blocks");
		public static final TagKey<Block> BLOCKS_AERIALITE = tag("aerialite_blocks");
		public static final TagKey<Block> PEDESTALS = tag("pedestals");
		public static final TagKey<Block> MANA_POOLS = tag("botania", "mana_pools");
		public static final TagKey<Block> CHARGERS = tag("chargers");
		public static final TagKey<Block> BELLFLOWER_IGNORE = tag("bellflower_ignore");
		public static final TagKey<Block> MANA_SPREADERS = tag("botania", "mana_spreaders");

		private static TagKey<Block> tag(String name) {
			return TagKey.create(Registries.BLOCK, prefix(name));
		}

		private static TagKey<Block> tag(String namespace, String name) {
			return TagKey.create(Registries.BLOCK, ResourceLocation.tryBuild(namespace, name));
		}
	}

	public static class DamageTypes {
		public static final TagKey<DamageType> PEACE_AMULET_AVAILABLE = tag("peace_amulet_available");
		public static final TagKey<DamageType> MAID_PROTECTION = tag("maid_protection");
		public static final TagKey<DamageType> SHADOW_WARRIOR_PROTECTION = tag("shadow_warrior_protection");

		private static TagKey<DamageType> tag(String name) {
			return TagKey.create(Registries.DAMAGE_TYPE, prefix(name));
		}
	}

	public static class Entities {
		public static final TagKey<EntityType<?>> GOBLINS = tag("goblins");

		private static TagKey<EntityType<?>> tag(String name) {
			return TagKey.create(Registries.ENTITY_TYPE, prefix(name));
		}
	}
}
