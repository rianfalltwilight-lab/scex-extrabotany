package io.github.lounode.extrabotany.common.handler;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;

import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.lib.ExtraBotanyTags;

import java.util.Map;

/**
 * 原版 1.12 的两处掉落：箱子战利品注入，以及拿着万圣节糖果袋生成的怪物。
 * Two drops of the original 1.12: the chest loot injection, and the monsters that spawn with a Halloween candy bag.
 *
 * <p>来源：ExtraMeteorP 1.12 {@code LootHandler} 和 {@code loot_tables/inject/*.json}，以及 {@code ItemHalloweenCandy.onSpawn}（默认开启，2%）。
 * <p>Source: {@code LootHandler} and {@code loot_tables/inject/*.json} of ExtraMeteorP 1.12, and {@code ItemHalloweenCandy.onSpawn} (on by default, 2%).
 */
public final class OriginalLootHandler {
	/**
	 * 注入的箱子。1.12 的村庄铁匠箱在现在的版本里是武器匠的箱子。值表示这个箱子是否也有灵魂碎片（只有地牢有）。
	 * The chests that get the injection. The village blacksmith chest of 1.12 is the weaponsmith chest in the current version. The value tells whether this chest also has spirit fragments (only the dungeon has them).
	 */
	private static final Map<ResourceLocation, Boolean> CHESTS = Map.of(
			chest("abandoned_mineshaft"), false,
			chest("desert_pyramid"), false,
			chest("jungle_temple"), false,
			chest("simple_dungeon"), true,
			chest("spawn_bonus_chest"), false,
			chest("stronghold_corridor"), false,
			chest("village/village_weaponsmith"), false);
	public static final float CANDY_BAG_CHANCE = 0.02F;

	private OriginalLootHandler() {}

	private static ResourceLocation chest(String path) {
		return ResourceLocation.withDefaultNamespace("chests/" + path);
	}

	public static void onLootTableLoad(LootTableLoadEvent event) {
		Boolean dungeon = CHESTS.get(event.getName());
		if (dungeon == null) {
			return;
		}
		// 1.12：1 次，幸运时最多再 1 次；瓶装火焰 16、瓶装星光 16 中的 12、瓶装妖精 8，其余为空。地牢另有灵魂碎片 12。
		// 1.12: 1 roll, up to 1 more with luck; bottled flame 16, bottled star 12, bottled pixie 8, the rest empty. The dungeon also has spirit fragments 12.
		LootPool.Builder pool = LootPool.lootPool()
				.name("extrabotany_inject_pool")
				.setRolls(ConstantValue.exactly(1))
				.setBonusRolls(UniformGenerator.between(0, 1))
				.add(LootItem.lootTableItem(ExtraBotanyItems.bottledFlame).setWeight(16))
				.add(LootItem.lootTableItem(ExtraBotanyItems.bottledStar).setWeight(12))
				.add(LootItem.lootTableItem(ExtraBotanyItems.bottledPixie).setWeight(8));
		if (dungeon) {
			pool.add(LootItem.lootTableItem(ExtraBotanyItems.spiritFragment).setWeight(12))
					.add(EmptyLootItem.emptyItem().setWeight(52));
		} else {
			pool.add(EmptyLootItem.emptyItem().setWeight(64));
		}
		event.getTable().addPool(pool.build());
	}

	public static void onFinalizeSpawn(FinalizeSpawnEvent event) {
		MobSpawnType type = event.getSpawnType();
		if (type != MobSpawnType.NATURAL && type != MobSpawnType.CHUNK_GENERATION && type != MobSpawnType.SPAWNER) {
			return;
		}
		Mob mob = event.getEntity();
		if (!mob.getType().is(ExtraBotanyTags.Entities.CANDY_BAG_HOLDERS) || !mob.getOffhandItem().isEmpty()) {
			return;
		}
		if (mob.getRandom().nextFloat() < CANDY_BAG_CHANCE) {
			giveCandyBag(mob);
		}
	}

	public static void giveCandyBag(Mob mob) {
		mob.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(ExtraBotanyItems.candyBag));
		mob.setDropChance(EquipmentSlot.OFFHAND, 1F);
	}
}
