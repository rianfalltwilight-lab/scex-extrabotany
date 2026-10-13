package io.github.lounode.extrabotany.common.loot;

import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import vazkii.botania.common.loot.BindUuid;

import java.util.List;

/**
 * 战利品函数：把遗物绑定到得到它的玩家。它直接使用植物魔法的实现。
 * A loot function: binds a relic to the player that gets it. It uses the implementation of Botania directly.
 */
public class ExtendBindUUID extends BindUuid {
	public ExtendBindUUID(List<LootItemCondition> conditionsIn) {
		super(conditionsIn);
	}

	public static LootItemConditionalFunction.Builder<?> bindUUID() {
		return simpleBuilder(ExtendBindUUID::new);
	}
}
