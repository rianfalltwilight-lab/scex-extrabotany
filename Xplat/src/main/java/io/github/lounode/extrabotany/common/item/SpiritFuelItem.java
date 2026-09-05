package io.github.lounode.extrabotany.common.item;

import net.minecraft.world.item.Item;

import net.neoforged.neoforge.event.furnace.FurnaceFuelBurnTimeEvent;

public class SpiritFuelItem extends Item {
	public SpiritFuelItem(Properties properties) {
		super(properties);
	}

	public static void makeFuel(FurnaceFuelBurnTimeEvent wrapper) {
		if (wrapper.getItemStack().is(ExtraBotanyItems.spiritFuel)) {
			wrapper.setBurnTime(12800);
		}
	}
}
