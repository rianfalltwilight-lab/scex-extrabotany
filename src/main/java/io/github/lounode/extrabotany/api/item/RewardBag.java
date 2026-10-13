package io.github.lounode.extrabotany.api.item;

import net.minecraft.resources.ResourceLocation;

/**
 * 奖励袋：打开时按一张战利品表给出物品。
 * A reward bag: when it is opened, it gives items from a loot table.
 */
public interface RewardBag {
	ResourceLocation getLootTable();
}
