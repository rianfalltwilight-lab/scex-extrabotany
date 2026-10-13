package io.github.lounode.extrabotany.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import io.github.lounode.extrabotany.common.helper.ItemNBTHelper;

import java.util.List;
import java.util.UUID;

/**
 * 莱茵河的黄金。对着一个玩家使用时，它记下这个玩家。然后在工作台里把它和一件遗物放在一起，遗物就改成绑定这个玩家（见 {@code DasRheingoldChangeSoulBoundRecipe}）。潜行并使用可以清除记下的玩家。
 * Das Rheingold. When it is used on a player, it records that player. Then, when it is put together with a relic in a crafting table, the relic gets the binding of that player (see {@code DasRheingoldChangeSoulBoundRecipe}). A sneak-use removes the recorded player.
 */
public class DasRheingoldItem extends Item {

	private static final String TAG_SOULBIND_UUID = "soulbindUUID";

	public DasRheingoldItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {
		if (!(interactionTarget instanceof Player bindPlayer)) {
			return InteractionResult.PASS;
		}

		if (bindPlayer.isShiftKeyDown()) {
			return InteractionResult.PASS;
		}

		ItemStack out = stack.copyWithCount(1);
		bindToUUID(out, bindPlayer.getUUID());
		getSoulbindUUID(out);
		out.set(DataComponents.CUSTOM_NAME, Component.translatable("item.extrabotany.das_rheingold.bind"));

		if (!player.addItem(out)) {
			return InteractionResult.PASS;
		}

		stack.shrink(1);

		return InteractionResult.SUCCESS;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
		ItemStack stack = player.getItemInHand(usedHand);
		if (getSoulbindUUID(stack) == null) {
			return InteractionResultHolder.pass(stack);
		}
		if (player.isSecondaryUseActive()) {
			stack.remove(DataComponents.CUSTOM_NAME);
			ItemNBTHelper.removeEntry(stack, TAG_SOULBIND_UUID);
			return InteractionResultHolder.success(stack);
		}
		return InteractionResultHolder.pass(stack);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
		super.appendHoverText(stack, context, tooltipComponents, isAdvanced);
		if (getSoulbindUUID(stack) != null) {
			tooltipComponents.add(Component.translatable("tooltip.extrabotany.bind", getSoulbindUUID(stack)).withStyle(ChatFormatting.GRAY));
		}
	}

	public void bindToUUID(ItemStack stack, UUID uuid) {
		ItemNBTHelper.setString(stack, TAG_SOULBIND_UUID, uuid.toString());
	}

	@Nullable
	public UUID getSoulbindUUID(ItemStack stack) {
		if (ItemNBTHelper.verifyExistance(stack, TAG_SOULBIND_UUID)) {
			try {
				return UUID.fromString(ItemNBTHelper.getString(stack, TAG_SOULBIND_UUID, ""));
			} catch (IllegalArgumentException ex) { // Bad UUID in tag
				ItemNBTHelper.removeEntry(stack, TAG_SOULBIND_UUID);
			}
		}

		return null;
	}
}
