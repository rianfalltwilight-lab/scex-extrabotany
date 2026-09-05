package io.github.lounode.extrabotany.data.loot;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import vazkii.botania.common.item.BotaniaItems;

import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.block.ExtraBotanyBlocks;
import io.github.lounode.extrabotany.common.lib.LibMisc;

public class RewardBagLootProvider extends RewardBagLootSubProvider {
	@Override
	public void generate() {
		this.add("eins", petalPoll());
		this.add("zwei", runeTable());
		this.add("drei", usefulTable());
		this.add("vier", miscTable());
		this.add("nine_and_three_quarters", harryPotterTable());
		this.add("pandoras_box", pandoraTable());
		this.add("limited_edition_supply_bag", LootTable.lootTable());
	}

	@Override
	public String getNameSpace() {
		return LibMisc.MOD_ID;
	}

	public static LootTable.Builder pandoraTable() {
		return LootTable.lootTable()
				.withPool(LootPool.lootPool()
						.setRolls(ConstantValue.exactly(1))
						.add(LootItem.lootTableItem(ExtraBotanyItems.einsRewardBag)
								.apply(SetItemCountFunction.setCount(ConstantValue.exactly(32)))))
				.withPool(LootPool.lootPool()
						.setRolls(ConstantValue.exactly(1))
						.add(LootItem.lootTableItem(ExtraBotanyItems.zweiRewardBag)
								.apply(SetItemCountFunction.setCount(ConstantValue.exactly(16)))))
				.withPool(LootPool.lootPool()
						.setRolls(ConstantValue.exactly(1))
						.add(LootItem.lootTableItem(ExtraBotanyItems.dreiRewardBag)
								.apply(SetItemCountFunction.setCount(ConstantValue.exactly(10)))))
				.withPool(LootPool.lootPool()
						.setRolls(ConstantValue.exactly(1))
						.add(LootItem.lootTableItem(ExtraBotanyItems.vierRewardBag)
								.apply(SetItemCountFunction.setCount(ConstantValue.exactly(10)))))
				.withPool(LootPool.lootPool()
						.setRolls(ConstantValue.exactly(1))
						.add(LootItem.lootTableItem(ExtraBotanyItems.heroMedal)
								.apply(SetItemCountFunction.setCount(ConstantValue.exactly(1)))));
	}

	public static LootTable.Builder harryPotterTable() {
		return LootTable.lootTable()
				.withPool(LootPool.lootPool()
						.setRolls(ConstantValue.exactly(1))
						.add(LootItem.lootTableItem(ExtraBotanyItems.heroMedal)
								.setWeight(1)
								.apply(SetItemCountFunction.setCount(ConstantValue.exactly(1))))
						.add(LootItem.lootTableItem(ExtraBotanyItems.einsRewardBag)
								.setWeight(30)
								.apply(SetItemCountFunction.setCount(ConstantValue.exactly(16))))
						.add(LootItem.lootTableItem(ExtraBotanyItems.zweiRewardBag)
								.setWeight(20)
								.apply(SetItemCountFunction.setCount(ConstantValue.exactly(10))))
						.add(LootItem.lootTableItem(ExtraBotanyItems.dreiRewardBag)
								.setWeight(10)
								.apply(SetItemCountFunction.setCount(ConstantValue.exactly(6))))
						.add(LootItem.lootTableItem(ExtraBotanyItems.vierRewardBag)
								.setWeight(10)
								.apply(SetItemCountFunction.setCount(ConstantValue.exactly(6))))
						.add(LootItem.lootTableItem(BotaniaItems.GAIA_INGOT)
								.setWeight(14)
								.apply(SetItemCountFunction.setCount(ConstantValue.exactly(1))))
						.add(LootItem.lootTableItem(BotaniaItems.GAIA_SPIRIT)
								.setWeight(20)
								.apply(SetItemCountFunction.setCount(ConstantValue.exactly(4))))
						.add(LootItem.lootTableItem(ExtraBotanyItems.challengeTicket)
								.setWeight(45)
								.apply(SetItemCountFunction.setCount(ConstantValue.exactly(1))))
				);
	}

	public static LootTable.Builder usefulTable() {
		return LootTable.lootTable()
				.withPool(LootPool.lootPool()
						.setRolls(ConstantValue.exactly(1))
						.add(LootItem.lootTableItem(BotaniaItems.MANASTEEL_INGOT)
								.setWeight(15)
								.apply(SetItemCountFunction.setCount(ConstantValue.exactly(4))))
						.add(LootItem.lootTableItem(BotaniaItems.MANA_PEARL)
								.setWeight(15)
								.apply(SetItemCountFunction.setCount(ConstantValue.exactly(4))))
						.add(LootItem.lootTableItem(BotaniaItems.MANA_DIAMOND)
								.setWeight(15)
								.apply(SetItemCountFunction.setCount(ConstantValue.exactly(4))))
						.add(LootItem.lootTableItem(BotaniaItems.ELEMENTIUM_INGOT)
								.setWeight(11)
								.apply(SetItemCountFunction.setCount(ConstantValue.exactly(3))))
						.add(LootItem.lootTableItem(BotaniaItems.PIXIE_DUST)
								.setWeight(11)
								.apply(SetItemCountFunction.setCount(ConstantValue.exactly(3))))
						.add(LootItem.lootTableItem(BotaniaItems.DRAGONSTONE)
								.setWeight(11)
								.apply(SetItemCountFunction.setCount(ConstantValue.exactly(3))))
						.add(LootItem.lootTableItem(BotaniaItems.MANA_POWDER)
								.setWeight(10)
								.apply(SetItemCountFunction.setCount(ConstantValue.exactly(8))))
						.add(LootItem.lootTableItem(BotaniaItems.TERRASTEEL_INGOT)
								.setWeight(9)
								.apply(SetItemCountFunction.setCount(ConstantValue.exactly(1))))
						.add(LootItem.lootTableItem(BotaniaItems.GAIA_SPIRIT)
								.setWeight(8)
								.apply(SetItemCountFunction.setCount(ConstantValue.exactly(4))))
						.add(LootItem.lootTableItem(BotaniaItems.GAIA_INGOT)
								.setWeight(7)
								.apply(SetItemCountFunction.setCount(ConstantValue.exactly(1))))
						.add(LootItem.lootTableItem(ExtraBotanyItems.heroMedal)
								.setWeight(1)
								.apply(SetItemCountFunction.setCount(ConstantValue.exactly(1))))
				);
	}

	public static LootTable.Builder miscTable() {
		LootPool.Builder pool = LootPool.lootPool()
				.add(LootItem.lootTableItem(Items.COAL).apply(SetItemCountFunction.setCount(ConstantValue.exactly(6))).setWeight(40))
				.add(LootItem.lootTableItem(Items.IRON_INGOT).apply(SetItemCountFunction.setCount(ConstantValue.exactly(4))).setWeight(36))
				.add(LootItem.lootTableItem(Items.GOLD_INGOT).apply(SetItemCountFunction.setCount(ConstantValue.exactly(4))).setWeight(24))
				.add(LootItem.lootTableItem(Items.REDSTONE).apply(SetItemCountFunction.setCount(ConstantValue.exactly(8))).setWeight(22))
				.add(LootItem.lootTableItem(Items.ENDER_PEARL).apply(SetItemCountFunction.setCount(ConstantValue.exactly(4))).setWeight(20))
				.add(LootItem.lootTableItem(Items.DIAMOND).apply(SetItemCountFunction.setCount(ConstantValue.exactly(1))).setWeight(18))
				.add(LootItem.lootTableItem(BotaniaItems.BLACKER_LOTUS).apply(SetItemCountFunction.setCount(ConstantValue.exactly(2))).setWeight(16))
				.add(LootItem.lootTableItem(ExtraBotanyBlocks.enchantedSoil).apply(SetItemCountFunction.setCount(ConstantValue.exactly(1))).setWeight(12))
				.add(LootItem.lootTableItem(ExtraBotanyItems.voidArchives).apply(SetItemCountFunction.setCount(ConstantValue.exactly(1))).setWeight(1))
				.setRolls(ConstantValue.exactly(1));

		return LootTable.lootTable().withPool(pool);
	}

	public static LootTable.Builder runeTable() {
		return LootTable.lootTable()
				.withPool(LootPool.lootPool()
						.setRolls(ConstantValue.exactly(1))
						.add(LootItem.lootTableItem(BotaniaItems.RUNE_OF_WATER)
								.setWeight(50)
								.apply(SetItemCountFunction.setCount(ConstantValue.exactly(2))))
						.add(LootItem.lootTableItem(BotaniaItems.RUNE_OF_FIRE)
								.setWeight(50)
								.apply(SetItemCountFunction.setCount(ConstantValue.exactly(2))))
						.add(LootItem.lootTableItem(BotaniaItems.RUNE_OF_EARTH)
								.setWeight(50)
								.apply(SetItemCountFunction.setCount(ConstantValue.exactly(2))))
						.add(LootItem.lootTableItem(BotaniaItems.RUNE_OF_AIR)
								.setWeight(50)
								.apply(SetItemCountFunction.setCount(ConstantValue.exactly(2))))
						.add(LootItem.lootTableItem(BotaniaItems.RUNE_OF_SPRING)
								.setWeight(30)
								.apply(SetItemCountFunction.setCount(ConstantValue.exactly(1))))
						.add(LootItem.lootTableItem(BotaniaItems.RUNE_OF_SUMMER)
								.setWeight(30)
								.apply(SetItemCountFunction.setCount(ConstantValue.exactly(1))))
						.add(LootItem.lootTableItem(BotaniaItems.RUNE_OF_AUTUMN)
								.setWeight(30)
								.apply(SetItemCountFunction.setCount(ConstantValue.exactly(1))))
						.add(LootItem.lootTableItem(BotaniaItems.RUNE_OF_WINTER)
								.setWeight(30)
								.apply(SetItemCountFunction.setCount(ConstantValue.exactly(1))))
						.add(LootItem.lootTableItem(BotaniaItems.RUNE_OF_MANA)
								.setWeight(10)
								.apply(SetItemCountFunction.setCount(ConstantValue.exactly(1))))
						.add(LootItem.lootTableItem(BotaniaItems.RUNE_OF_LUST)
								.setWeight(10)
								.apply(SetItemCountFunction.setCount(ConstantValue.exactly(1))))
						.add(LootItem.lootTableItem(BotaniaItems.RUNE_OF_GLUTTONY)
								.setWeight(10)
								.apply(SetItemCountFunction.setCount(ConstantValue.exactly(1))))
						.add(LootItem.lootTableItem(BotaniaItems.RUNE_OF_GREED)
								.setWeight(10)
								.apply(SetItemCountFunction.setCount(ConstantValue.exactly(1))))
						.add(LootItem.lootTableItem(BotaniaItems.RUNE_OF_SLOTH)
								.setWeight(10)
								.apply(SetItemCountFunction.setCount(ConstantValue.exactly(1))))
						.add(LootItem.lootTableItem(BotaniaItems.RUNE_OF_WRATH)
								.setWeight(10)
								.apply(SetItemCountFunction.setCount(ConstantValue.exactly(1))))
						.add(LootItem.lootTableItem(BotaniaItems.RUNE_OF_ENVY)
								.setWeight(10)
								.apply(SetItemCountFunction.setCount(ConstantValue.exactly(1))))
						.add(LootItem.lootTableItem(BotaniaItems.RUNE_OF_PRIDE)
								.setWeight(10)
								.apply(SetItemCountFunction.setCount(ConstantValue.exactly(1))))
				);
	}

	public static LootTable.Builder petalPoll() {
		Item[] petals = new Item[] {
				BotaniaItems.WHITE_MYSTICAL_PETAL,
				BotaniaItems.ORANGE_MYSTICAL_PETAL,
				BotaniaItems.MAGENTA_MYSTICAL_PETAL,
				BotaniaItems.LIGHT_BLUE_MYSTICAL_PETAL,
				BotaniaItems.YELLOW_MYSTICAL_PETAL,
				BotaniaItems.LIME_MYSTICAL_PETAL,
				BotaniaItems.PINK_MYSTICAL_PETAL,
				BotaniaItems.GRAY_MYSTICAL_PETAL,
				BotaniaItems.LIGHT_GRAY_MYSTICAL_PETAL,
				BotaniaItems.CYAN_MYSTICAL_PETAL,
				BotaniaItems.PURPLE_MYSTICAL_PETAL,
				BotaniaItems.BLUE_MYSTICAL_PETAL,
				BotaniaItems.BROWN_MYSTICAL_PETAL,
				BotaniaItems.GREEN_MYSTICAL_PETAL,
				BotaniaItems.RED_MYSTICAL_PETAL,
				BotaniaItems.BLACK_MYSTICAL_PETAL
		};
		LootTable.Builder builder = LootTable.lootTable();

		LootPool.Builder pool = LootPool.lootPool();

		for (var petal : petals) {
			pool.add(LootItem.lootTableItem(petal));
		}

		pool.apply(SetItemCountFunction.setCount(ConstantValue.exactly(6)));
		pool.setRolls(ConstantValue.exactly(1));

		builder.withPool(pool);

		return builder;
	}
}
