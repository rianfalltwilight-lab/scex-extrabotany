package io.github.lounode.extrabotany.data.loot;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import vazkii.botania.common.loot.EnableRelics;
import vazkii.botania.common.loot.RealPlayerCondition;

import io.github.lounode.extrabotany.common.entity.ExtraBotanyEntityType;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.lib.LibMisc;
import io.github.lounode.extrabotany.common.loot.ExtendBindUUID;

import java.util.stream.Stream;

/**
 * 生成本模组生物的战利品表。只有 Boss 有掉落物，而且只有被真实的玩家击杀时才掉落。
 * Makes the loot tables of the creatures of this mod. Only the bosses have drops, and they only drop when a real player kills them.
 */
public class EntityLootProvider extends EntityLootSubProviderFix {
	public EntityLootProvider(HolderLookup.Provider registries) {
		super(registries);
	}

	@Override
	protected Stream<EntityType<?>> getKnownEntityTypes() {
		return BuiltInRegistries.ENTITY_TYPE.stream()
				.filter(e -> LibMisc.MOD_ID.equals(BuiltInRegistries.ENTITY_TYPE.getKey(e).getNamespace()));
	}

	@Override
	public void generate() {
		this.add(ExtraBotanyEntityType.GAIA_LEGACY, LootTable.lootTable());
		// 本我仆从什么也不掉。
		// The Ego Minion drops nothing.
		this.add(ExtraBotanyEntityType.EGO_MINION, LootTable.lootTable());

		// 对照原版审计 E5：1.12 loot_tables/gaia_guardian_3.json 只有潘多拉魔盒。两张唱片在 1.12 是"终局"进度的奖励（见 AllAdvancementsHandler）。
		// Parity audit E5: loot_tables/gaia_guardian_3.json of 1.12 only has the Pandora's Box. The two music discs are the reward of the "End Day" advancement in 1.12 (see AllAdvancementsHandler).
		this.add(ExtraBotanyEntityType.GAIA_III, LootTable.lootTable()
				.withPool(boundPandorasBoxPool())
		);

		// 本我：潘多拉魔盒，以及 20% 的几率掉落它的唱片。原版只掉落潘多拉魔盒，唱片在原版里没有来源。
		// Ego: a Pandora's Box, and a 20% chance for its music disc. The original only drops the Pandora's Box, and the disc has no source in the original.
		// 本我只在最后一个阶段被击败时才使用这张表，见 Ego#getDefaultLootTable。
		// The Ego only uses this table when it is defeated in its last stage, see Ego#getDefaultLootTable.
		this.add(ExtraBotanyEntityType.EGO, LootTable.lootTable()
				.withPool(boundPandorasBoxPool())
				.withPool(playerKillPool(ExtraBotanyItems.recordEgo, 1)
						.when(LootItemRandomChanceCondition.randomChance(0.2F)))
		);

		// 空之律者：潘多拉魔盒、三个限量补给袋，以及 2% 的几率掉落空之律者核心（1.12 loot_tables/void_herrscher.json，没有唱片）。
		// Herrscher of the Void: a Pandora's Box, three bags of the type Supply Bag Limited Edition, and a 2% chance for a Core of The Void (loot_tables/void_herrscher.json of 1.12, no music disc).
		// 1.12 的骷髅仆从不掉落东西。 / The skull minion of 1.12 drops nothing.
		this.add(ExtraBotanyEntityType.SKULL_MINION, LootTable.lootTable());
		this.add(ExtraBotanyEntityType.VOID_HERRSCHER, LootTable.lootTable()
				.withPool(playerKillPool(ExtraBotanyItems.pandorasBox, 1))
				.withPool(playerKillPool(ExtraBotanyItems.limitedEditionSupplyBag, 3))
				// 1.12 loot_tables/void_herrscher.json：2% 的彩虹外观律者核心。 / 1.12 loot_tables/void_herrscher.json: a 2% Core of the Void with the rainbow appearance.
				.withPool(LootPool.lootPool()
						.when(() -> RealPlayerCondition.INSTANCE)
						.when(LootItemRandomChanceCondition.randomChance(0.02F))
						.setRolls(ConstantValue.exactly(1))
						.add(LootItem.lootTableItem(ExtraBotanyItems.coreOfTheVoid)
								.apply(net.minecraft.world.level.storage.loot.functions.SetCustomDataFunction.setCustomData(rainbowVariant()))))
		);
	}

	/**
	 * 一个潘多拉魔盒，绑定在得到它的玩家身上。每个攻击过 Boss 的玩家各得到一个，所以别人拿不走它。
	 * One Pandora's Box, bound to the player that gets it. Each player that attacked the boss gets one, thus a different player cannot take it.
	 */
	private static LootPool.Builder boundPandorasBoxPool() {
		return LootPool.lootPool()
				.when(EnableRelics.builder())
				.when(() -> RealPlayerCondition.INSTANCE)
				.setRolls(ConstantValue.exactly(1))
				.add(LootItem.lootTableItem(ExtraBotanyItems.pandorasBox)
						.apply(ExtendBindUUID.bindUUID()));
	}

	/**
	 * 一个只在真实玩家击杀时掉落的奖池：固定掉落 {@code count} 个物品。
	 * A pool that only drops when a real player makes the kill: it always drops {@code count} of the item.
	 */
	private static net.minecraft.nbt.CompoundTag rainbowVariant() {
		var tag = new net.minecraft.nbt.CompoundTag();
		tag.putString("variant", io.github.lounode.extrabotany.common.item.relic.voidcore.CoreOfTheVoidItem.VARIANT_RAINBOW);
		return tag;
	}

	private static LootPool.Builder playerKillPool(ItemLike item, int count) {
		var entry = LootItem.lootTableItem(item);
		if (count != 1) {
			entry.apply(SetItemCountFunction.setCount(ConstantValue.exactly(count)));
		}
		return LootPool.lootPool()
				.when(() -> RealPlayerCondition.INSTANCE)
				.setRolls(ConstantValue.exactly(1))
				.add(entry);
	}
}
