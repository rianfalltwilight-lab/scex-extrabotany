package io.github.lounode.extrabotany.common.loot;

import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import vazkii.botania.common.loot.BindUuid;

import java.util.List;

public class ExtendBindUUID extends BindUuid {
	public ExtendBindUUID(List<LootItemCondition> conditionsIn) {
		super(conditionsIn);
	}

	public static LootItemConditionalFunction.Builder<?> bindUUID() {
		return simpleBuilder(ExtendBindUUID::new);
	}
}
