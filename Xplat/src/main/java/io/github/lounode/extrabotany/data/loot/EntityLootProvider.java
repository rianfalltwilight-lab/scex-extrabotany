package io.github.lounode.extrabotany.data.loot;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import vazkii.botania.common.loot.EnableRelics;
import vazkii.botania.common.loot.RealPlayerCondition;

import io.github.lounode.extrabotany.common.entity.ExtraBotanyEntityType;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.lib.LibMisc;
import io.github.lounode.extrabotany.common.loot.ExtendBindUUID;

import java.util.stream.Stream;

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
		this.add(ExtraBotanyEntityType.GAIA_III, LootTable.lootTable()
				.withPool(LootPool.lootPool()
						//F**K there's no static method in these conditions
						.when(EnableRelics.builder())
						.when(() -> RealPlayerCondition.INSTANCE)
						.setRolls(ConstantValue.exactly(1))
						.add(LootItem.lootTableItem(ExtraBotanyItems.pandorasBox)
								.apply(ExtendBindUUID.bindUUID()))
				)
				.withPool(LootPool.lootPool()
						.when(() -> RealPlayerCondition.INSTANCE)
						.when(LootItemRandomChanceCondition.randomChance(0.2F))
						.setRolls(ConstantValue.exactly(1))
						.add(LootItem.lootTableItem(ExtraBotanyItems.recordGaia3))
						.add(LootItem.lootTableItem(ExtraBotanyItems.recordHerrscherOfTheVoid))
				)
		);
	}
}
