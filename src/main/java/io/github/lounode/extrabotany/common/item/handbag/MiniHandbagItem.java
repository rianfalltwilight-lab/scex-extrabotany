package io.github.lounode.extrabotany.common.item.handbag;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.Level;

import vazkii.botania.common.item.ItemBackedInventory;

/**
 * 旅行手袋。右键打开一个 24 格的随身容器，界面里同时显示四个盔甲栏。
 * Mini Handbag. A use opens a portable container with 24 slots; the screen also shows the four armor slots.
 *
 * <p>来源：ExtraMeteorP 1.12 {@code ItemMasterHandbag}、{@code ContainerHandbag}、{@code GuiHandbag}（对照原版审计 E6）。手袋不能放进手袋里。
 * <p>Source: {@code ItemMasterHandbag}, {@code ContainerHandbag} and {@code GuiHandbag} of ExtraMeteorP 1.12 (parity audit E6). A handbag cannot go into a handbag.
 */
public class MiniHandbagItem extends Item {
	public static final int SIZE = 24;

	public MiniHandbagItem(Properties properties) {
		super(properties);
	}

	public static ItemBackedInventory getInventory(ItemStack stack) {
		return new ItemBackedInventory(stack, SIZE);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!level.isClientSide()) {
			boolean mainHand = hand == InteractionHand.MAIN_HAND;
			player.openMenu(new SimpleMenuProvider((id, inventory, p) -> new MiniHandbagMenu(id, inventory, mainHand),
					stack.getHoverName()), buf -> buf.writeBoolean(mainHand));
		}
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
	}

	@Override
	public void onDestroyed(ItemEntity entity) {
		ItemContainerContents contents = entity.getItem().getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
		ItemUtils.onContainerDestroyed(entity, contents.nonEmptyItemsCopy());
	}

	@Override
	public boolean canFitInsideContainerItems() {
		return false;
	}
}
