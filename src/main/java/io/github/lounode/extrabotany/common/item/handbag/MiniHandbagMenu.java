package io.github.lounode.extrabotany.common.item.handbag;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import vazkii.botania.client.gui.SlotLocked;

import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

/**
 * 旅行手袋的容器：四个盔甲栏、24 个手袋格子和玩家物品栏。格子位置和 1.12 的 {@code ContainerHandbag} 相同。
 * The menu of the Mini Handbag: four armor slots, 24 handbag slots and the player inventory. The slot positions are those of {@code ContainerHandbag} of 1.12.
 */
public class MiniHandbagMenu extends AbstractContainerMenu {
	private static final ResourceLocation[] ARMOR_ICONS = {
			InventoryMenu.EMPTY_ARMOR_SLOT_HELMET, InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE,
			InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS, InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS };
	private static final EquipmentSlot[] ARMOR = { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET };
	private static final int ARMOR_END = 4;
	private static final int BAG_END = ARMOR_END + MiniHandbagItem.SIZE;
	private static final int INV_END = BAG_END + 36;

	private final ItemStack handbag;

	public MiniHandbagMenu(int windowId, Inventory playerInv, boolean mainHand) {
		super(ExtraBotanyItems.MINI_HANDBAG_MENU, windowId);
		Player player = playerInv.player;
		this.handbag = player.getItemInHand(mainHand ? net.minecraft.world.InteractionHand.MAIN_HAND : net.minecraft.world.InteractionHand.OFF_HAND);
		Container bag = player.level().isClientSide() ? new net.minecraft.world.SimpleContainer(MiniHandbagItem.SIZE) : MiniHandbagItem.getInventory(handbag);

		for (int i = 0; i < 4; i++) {
			EquipmentSlot slot = ARMOR[i];
			ResourceLocation icon = ARMOR_ICONS[i];
			addSlot(new Slot(playerInv, 39 - i, 8, 8 + i * 18) {
				@Override
				public int getMaxStackSize() {
					return 1;
				}

				@Override
				public boolean mayPlace(ItemStack stack) {
					return player.getEquipmentSlotForItem(stack) == slot;
				}

				@Override
				public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
					return Pair.of(InventoryMenu.BLOCK_ATLAS, icon);
				}
			});
		}
		for (int row = 0; row < 4; row++) {
			for (int col = 0; col < 6; col++) {
				addSlot(new Slot(bag, col + row * 6, 62 + col * 18, 8 + row * 18) {
					@Override
					public boolean mayPlace(ItemStack stack) {
						return stack.getItem().canFitInsideContainerItems();
					}
				});
			}
		}
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) {
				addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
			}
		}
		for (int col = 0; col < 9; col++) {
			if (mainHand && playerInv.selected == col) {
				addSlot(new SlotLocked(playerInv, col, 8 + col * 18, 142));
			} else {
				addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
			}
		}
	}

	public static MiniHandbagMenu fromNetwork(int windowId, Inventory inv, net.minecraft.network.RegistryFriendlyByteBuf buf) {
		return new MiniHandbagMenu(windowId, inv, buf.readBoolean());
	}

	@Override
	public boolean stillValid(Player player) {
		return player.getMainHandItem() == handbag || player.getOffhandItem() == handbag;
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		ItemStack result = ItemStack.EMPTY;
		Slot slot = slots.get(index);
		if (slot.hasItem()) {
			ItemStack stack = slot.getItem();
			result = stack.copy();
			if (index < BAG_END) {
				if (!moveItemStackTo(stack, BAG_END, INV_END, true)) {
					return ItemStack.EMPTY;
				}
			} else if (!stack.getItem().canFitInsideContainerItems() || !moveItemStackTo(stack, ARMOR_END, BAG_END, false)) {
				return ItemStack.EMPTY;
			}
			if (stack.isEmpty()) {
				slot.set(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}
			if (stack.getCount() == result.getCount()) {
				return ItemStack.EMPTY;
			}
			slot.onTake(player, stack);
		}
		return result;
	}
}
