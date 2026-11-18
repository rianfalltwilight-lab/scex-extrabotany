package io.github.lounode.extrabotany.common.item;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

import vazkii.botania.common.item.CustomCreativeTabContents;

/**
 * 一个不在创造物品栏里出现的物品。注册名保留，旧存档里的物品不会丢失。
 * An item that is not in the creative tab. The registry name stays, thus the items in old saves are not lost.
 *
 * <p>对照原版审计 E5：空的律者核心是重燃加的物品，原版的律者核心直接用工作台做，所以它没有用处了。
 * <p>Parity audit E5: the Empty Herrscher Core is an item that Reburn added; the Core of the Void of the original is made directly on the crafting table, thus it has no use now.
 */
public class HiddenItem extends Item implements CustomCreativeTabContents {
	public HiddenItem(Properties properties) {
		super(properties);
	}

	@Override
	public void addToCreativeTab(Item me, CreativeModeTab.Output output) {}
}
