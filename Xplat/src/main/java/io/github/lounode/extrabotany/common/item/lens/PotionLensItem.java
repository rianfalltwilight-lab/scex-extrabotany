package io.github.lounode.extrabotany.common.item.lens;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import vazkii.botania.common.item.lens.LensItem;

import io.github.lounode.extrabotany.common.brew.BrewUtil;

import java.util.List;

public class PotionLensItem extends LensItem {
	public PotionLensItem(Properties builder, PotionLens lens, int props) {
		super(builder, lens, props);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> stacks, TooltipFlag flags) {
		super.appendHoverText(stack, context, stacks, flags);
		if (BrewUtil.getBrew(stack) != BrewUtil.EMPTY) {
			BrewUtil.addPotionTooltip(BrewUtil.getBrew(stack), stacks, 1.0F, 0, context.tickRate());
		}
	}
}
