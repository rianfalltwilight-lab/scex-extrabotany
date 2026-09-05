package io.github.lounode.extrabotany.common.item.material;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import vazkii.botania.common.helper.PlayerHelper;

import io.github.lounode.extrabotany.common.lib.LibAdvancementNames;

import java.util.Locale;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class GildedPotatoItem extends Item {

	private static final String ADVANCEMENT_NAME = "ubisoft";

	public GildedPotatoItem(Properties properties) {
		super(properties);
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
		super.inventoryTick(stack, level, entity, slotId, isSelected);
		if (!(entity instanceof ServerPlayer serverPlayer)) {
			return;
		}
		if (PlayerHelper.hasAdvancement(serverPlayer, prefix(LibAdvancementNames.POTATO_SERVER).withPrefix("main/"))) {
			return;
		}

		if (ADVANCEMENT_NAME.equals(stack.getHoverName().getString().toLowerCase(Locale.ROOT))) {
			PlayerHelper.grantCriterion(serverPlayer, prefix(LibAdvancementNames.POTATO_SERVER).withPrefix("main/"), "code_triggered");
		}
	}
}
