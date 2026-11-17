package io.github.lounode.extrabotany.common.util;

import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;

/** Drops a copy; callers remove the equipped original only after success. */
public final class PlayerDropHelper {

	private PlayerDropHelper() {}

	public static boolean tryDrop(Player player, ItemStack stack) {
		return tryDrop(player, stack, false);
	}

	public static boolean tryToss(Player player, ItemStack stack) {
		return tryDrop(player, stack, true);
	}

	private static boolean tryDrop(Player player, ItemStack stack, boolean fireTossEvent) {
		if (player.level().isClientSide() || stack.isEmpty()) {
			return false;
		}
		ItemEntity drop = new ItemEntity(player.level(), player.getX(), player.getEyeY() - 0.3F, player.getZ(), stack.copy());
		drop.setPickUpDelay(40);
		// Keep the trajectory of Player.drop(stack, false, ...).
		float pitch = player.getXRot() * Mth.DEG_TO_RAD;
		float yaw = player.getYRot() * Mth.DEG_TO_RAD;
		var random = player.getRandom();
		float angle = random.nextFloat() * Mth.TWO_PI;
		float spread = 0.02F * random.nextFloat();
		drop.setDeltaMovement(-Mth.sin(yaw) * Mth.cos(pitch) * 0.3F + Math.cos(angle) * spread,
				-Mth.sin(pitch) * 0.3F + 0.1F + (random.nextFloat() - random.nextFloat()) * 0.1F,
				Mth.cos(yaw) * Mth.cos(pitch) * 0.3F + Math.sin(angle) * spread);
		if (fireTossEvent) {
			if (NeoForge.EVENT_BUS.post(new ItemTossEvent(drop, player)).isCanceled()) {
				return false;
			}
		} else {
			drop.setThrower(player);
		}
		Entity entityToAdd = drop;
		ItemStack droppedStack = drop.getItem();
		if (droppedStack.getItem().hasCustomEntity(droppedStack)) {
			Entity custom = droppedStack.getItem().createEntity(player.level(), drop, droppedStack);
			if (custom != null) {
				entityToAdd = custom;
			}
			if (entityToAdd.getClass() == ItemEntity.class) {
				// NeoForge replaces exact ItemEntity instances through a deferred join.
				// A null (or ordinary ItemEntity) factory result has already been resolved.
				ItemEntity resolved = new ResolvedItemEntity(player.level());
				resolved.restoreFrom(entityToAdd);
				entityToAdd = resolved;
			}
		}
		// Commit against the final entity, not NeoForge's cancelled replacement placeholder.
		if (!player.level().addFreshEntity(entityToAdd)) {
			return false;
		}
		if (!fireTossEvent) {
			player.awardStat(Stats.ITEM_DROPPED.get(stack.getItem()), stack.getCount());
			player.awardStat(Stats.DROP);
		}
		return true;
	}

	private static final class ResolvedItemEntity extends ItemEntity {
		private ResolvedItemEntity(Level level) {
			super(EntityType.ITEM, level);
		}
	}
}
