package io.github.lounode.extrabotany.data.loot;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import vazkii.botania.common.item.BotaniaItems;

import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.block.ExtraBotanyBlocks;
import io.github.lounode.extrabotany.common.lib.LibMisc;

/**
 * 生成奖励袋的战利品表（{@code reward_bags/*}）：四种奖励袋、九又四分之三奖励袋和潘多拉魔盒。
 * Makes the loot tables of the reward bags ({@code reward_bags/*}): the four reward bags, the Nine and Three-Quarters Reward Bag and the Pandora's Box.
 *
 * <p>万圣节糖果袋的表是手写的文件。限量补给袋的表是空的：它的奖池在代码里（见 {@code SupplyBagItem}）。
 * <p>The table of the Halloween candy bag is a hand-written file. The table of the Supply Bag Limited Edition is empty: its pool is in the code (see {@code SupplyBagItem}).
 */
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

	/**
	 * 九又四分之三奖励袋只有 1.12 有：ConfigHandler.REWARDBAG5 的默认值。
	 * Only 1.12 has the Nine and Three-Quarters bag: the default value of ConfigHandler.REWARDBAG5.
	 */
	public static LootTable.Builder harryPotterTable() {
		return LootTable.lootTable()
				.withPool(LootPool.lootPool()
						.setRolls(ConstantValue.exactly(1))
						.add(weighted(ExtraBotanyItems.einsRewardBag, 16, 33))
						.add(weighted(ExtraBotanyItems.zweiRewardBag, 10, 23))
						.add(weighted(ExtraBotanyItems.dreiRewardBag, 6, 13))
						.add(weighted(ExtraBotanyItems.vierRewardBag, 6, 13))
						.add(weighted(BotaniaItems.GAIA_INGOT, 1, 23))
						.add(weighted(BotaniaItems.TERRASTEEL_INGOT, 5, 20))
						.add(weighted(ExtraBotanyItems.challengeTicket, 1, 45))
						.add(weighted(ExtraBotanyItems.heroMedal, 1, 1))
				);
	}

	private static LootPoolSingletonContainer.Builder<?> weighted(ItemLike item, int count, int weight) {
		var entry = LootItem.lootTableItem(item).setWeight(weight);
		if (count != 1) {
			entry.apply(SetItemCountFunction.setCount(ConstantValue.exactly(count)));
		}
		return entry;
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

	/**
	 * Vier 奖励袋：1.16 ModItems.categoryListD。现在的植物魔法没有过度生长种子，用附魔土代替；"佛舍利"就是虚空档案。
	 * The Vier bag: categoryListD of 1.16 ModItems. The current Botania has no overgrowth seed, thus the enchanted soil takes its place; the "Buddhist relics" are the Void Archives.
	 */
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

	/**
	 * Zwei 奖励袋：1.16 ModItems.categoryListB。两个版本都有奖励袋，所以用 1.16。
	 * The Zwei bag: categoryListB of 1.16 ModItems. Both versions have the reward bags, thus 1.16 is used.
	 */
	public static LootTable.Builder runeTable() {
		return LootTable.lootTable()
				.withPool(LootPool.lootPool()
						.setRolls(ConstantValue.exactly(1))
						.add(weighted(ExtraBotanyItems.elementRune, 2, 80))
						.add(weighted(ExtraBotanyItems.sinRune, 1, 20))
				);
	}

	/**
	 * Eins 奖励袋：1.16 ModItems.categoryListA，万能花瓣 4、8 或 6 个（权重 10、10、30）。
	 * The Eins bag: categoryListA of 1.16 ModItems, 4, 8 or 6 universal petals (weights 10, 10, 30).
	 */
	public static LootTable.Builder petalPoll() {
		return LootTable.lootTable()
				.withPool(LootPool.lootPool()
						.setRolls(ConstantValue.exactly(1))
						.add(weighted(ExtraBotanyItems.universalPetal, 4, 10))
						.add(weighted(ExtraBotanyItems.universalPetal, 8, 10))
						.add(weighted(ExtraBotanyItems.universalPetal, 6, 30))
				);
	}
}
