package io.github.lounode.extrabotany.api.item;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.xplat.XplatAbstractions;

import io.github.lounode.extrabotany.common.item.relic.void_archives.VoidArchivesItem;

import java.util.List;

/**
 * 虚空万藏的一种形态。虚空万藏把物品的各种行为（使用、攻击、每 tick 的更新等）转交给当前的形态，每个方法的含义和 {@code Item} 里的同名方法相同。
 * One form of the Void Archives. The Void Archives sends the behavior of the item (use, attack, the update on each tick and others) to the current form, and each method has the same meaning as the method of {@code Item} with the same name.
 */
public interface VoidArchivesVariant {
	VoidArchivesVariant DEFAULT = () -> "default";

	String getId();

	default void onActive(ItemStack stack) {}

	default void onInactive(ItemStack stack) {}

	default boolean isActive(ItemStack stack) {
		return VoidArchivesItem.getVariant(stack).getId().equals(getId());
	}

	default boolean isMaster(ItemStack stack, Player player) {
		var relic = io.github.lounode.extrabotany.xplat.EXplatAbstractions.INSTANCE.findRelic(stack);
		return relic != null && relic.isRightPlayer(player);
	}

	default InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		return InteractionResultHolder.fail(stack);
	}

	default void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {}

	default void appendHoverText(ItemStack stack, Item.TooltipContext context,
			List<Component> tooltipComponents, TooltipFlag isAdvanced) {}

	default void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {}

	default void onDestroyed(ItemEntity itemEntity) {}

	default InteractionResult useOn(UseOnContext context) {
		return InteractionResult.PASS;
	}

	default float getDestroySpeed(ItemStack stack, BlockState state) {
		return 1.0F;
	}

	default ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
		return stack;
	}

	default boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction action, Player player) {
		return false;
	}

	default boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access) {
		return false;
	}

	default boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		return false;
	}

	default boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miningEntity) {
		return false;
	}

	default InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {
		return InteractionResult.PASS;
	}

	default Component getName(ItemStack stack) {
		return Component.translatable("item.extrabotany.void_archives");
	}

	default UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.NONE;
	}

	default int getUseDuration(ItemStack stack) {
		return 0;
	}

	default void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeCharged) {}

	default boolean useOnRelease(ItemStack stack) {
		return false;
	}

}
