package io.github.lounode.extrabotany.common.entity.gaia.behavior;

import com.google.common.collect.ImmutableMap;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import top.theillusivec4.curios.api.CuriosApi;

import io.github.lounode.extrabotany.common.entity.gaia.Gaia;
import io.github.lounode.extrabotany.xplat.ExtraBotanyConfig;

import java.util.ArrayList;
import java.util.List;

import static io.github.lounode.extrabotany.api.gaia.GaiaArena.checkFeasibility;

/**
 * 缴械。玩家拿在手里、穿在身上或者戴在饰品栏里的不允许的物品会掉到地上。背包里的物品不受影响。
 * Disarm. The items that are not permitted and that a player holds, wears or has in the accessory slots drop to the ground. The items in the backpack are not changed.
 *
 * <p>哪些物品被允许，见 {@code GaiaArena#GUARDIAN_ALLOWED_NAMESPACES} 和配置里的免检名单。创造模式的玩家、带着 Boss 指定的豁免物品的玩家不受影响。配置可以关闭缴械。
 * <p>For the permitted items, see {@code GaiaArena#GUARDIAN_ALLOWED_NAMESPACES} and the bypass list of the configuration. A player in creative mode, or a player with the bypass item of the boss, is not changed. The configuration can disable the disarm.
 */
public class GaiaDisarm<E extends Gaia> extends Behavior<E> {

	public GaiaDisarm() {
		super(ImmutableMap.of(
				MemoryModuleType.NEAREST_PLAYERS, MemoryStatus.VALUE_PRESENT
		));
	}

	@Override
	protected boolean canStillUse(ServerLevel level, E entity, long gameTime) {
		return true;
	}

	@Override
	protected void tick(ServerLevel level, E gaia, long gameTime) {
		List<Player> players = getPlayers(gaia);
		for (Player player : players) {
			if (gaia.getGuardianBypassItem() == null || !io.github.lounode.extrabotany.api.gaia.GaiaArena.playerHasItem(player, gaia.getGuardianBypassItem())) disArm(player);
		}
	}

	protected void disArm(Player player) {
		if (player.level().isClientSide() || player.isCreative() || player.isSpectator() || ExtraBotanyConfig.common().disableGaiaDisArm() || !ExtraBotanyConfig.common().guardianItemCheck()) {
			return;
		}
		// Only wielded/equipped items: do not empty the backpack or unselected hotbar.
		for (EquipmentSlot slot : new EquipmentSlot[] {EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND,
				EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
			if (dropForbidden(player, player.getItemBySlot(slot))) player.setItemSlot(slot, ItemStack.EMPTY);
		}
		CuriosApi.getCuriosInventory(player).ifPresent(inventory -> {
			for (var handler : inventory.getCurios().values()) {
				var stacks = handler.getStacks();
				for (int slot = 0; slot < stacks.getSlots(); slot++) {
					// Forced combat removal intentionally bypasses manual canUnequip restrictions.
					// Curios observes the changed stack and performs its normal unequip/sync lifecycle.
					if (dropForbidden(player, stacks.getStackInSlot(slot))) stacks.setStackInSlot(slot, ItemStack.EMPTY);
				}
			}
		});
	}

	private static boolean dropForbidden(Player player, ItemStack stack) {
		if (stack.isEmpty() || checkFeasibility(stack) || io.github.lounode.extrabotany.api.gaia.GaiaArena.GUARDIAN_ALLOWED_NAMESPACES
				.contains(BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace())) return false;
		var drop = new ItemEntity(player.level(), player.getX(), player.getEyeY() - 0.3, player.getZ(), stack.copy());
		drop.setDeltaMovement(player.getLookAngle().scale(0.3).add(0, 0.15, 0));
		drop.setPickUpDelay(90);
		// Keep the original if another mod cancels spawning the dropped entity.
		return player.level().addFreshEntity(drop);
	}

	protected List<Player> getPlayers(Gaia gaia) {
		return gaia.getBrain().getMemory(MemoryModuleType.NEAREST_PLAYERS).orElse(new ArrayList<>());
	}
}
