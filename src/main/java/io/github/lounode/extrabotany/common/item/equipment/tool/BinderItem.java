package io.github.lounode.extrabotany.common.item.equipment.tool;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import io.github.lounode.extrabotany.common.block.flower.functional.StardustLotusBlockEntity;
import io.github.lounode.extrabotany.common.helper.ItemNBTHelper;

import java.util.List;

/**
 * 坐标读取器（代码里的名字是 Binder）。潜行时右键方块，它记录这个方块的坐标和维度。不潜行时右键星尘莲，它把记录的坐标设为星尘莲的传送目标。
 * Position Reader (the name in the code is Binder). A sneak-use on a block records the position and the dimension of the block. A use on a Stardust Lotus without sneak sets the recorded position as the teleport target of the lotus.
 *
 * <p>数据存在物品的自定义数据里，键名是 {@code posx}、{@code posy}、{@code posz} 和 {@code dim}。旧存档使用相同的键名。
 * <p>The data is in the custom data of the item. The keys are {@code posx}, {@code posy}, {@code posz} and {@code dim}. Old saves use the same keys.
 *
 * <p>来源：ExtraMeteorP 1.12 {@code ItemBinder}。
 * <p>Source: ExtraMeteorP 1.12 {@code ItemBinder}.
 */
public class BinderItem extends Item {
	private static final String TAG_X = "posx";
	private static final String TAG_Y = "posy";
	private static final String TAG_Z = "posz";
	private static final String TAG_DIMENSION = "dim";
	private static final String DEFAULT_DIMENSION = "minecraft:overworld";
	// 旧存档用 Y = -1 表示"没有绑定"。
	// Old saves use Y = -1 for "not bound".
	private static final int UNBOUND_Y = -1;

	public BinderItem(Properties props) {
		super(props);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Player player = context.getPlayer();
		if (player == null) {
			return InteractionResult.PASS;
		}
		if (!player.isShiftKeyDown()) {
			return context.getLevel().getBlockEntity(context.getClickedPos()) instanceof StardustLotusBlockEntity lotus
					? bindLotus(context.getLevel(), player, context.getItemInHand(), lotus)
					: InteractionResult.PASS;
		}
		BlockPos pos = context.getClickedPos();
		String dimension = context.getLevel().dimension().location().toString();
		// 一次写入全部四个键。这样物品数据只更新一次。
		// Write all four keys in one operation. Thus the item data changes only one time.
		CustomData.update(DataComponents.CUSTOM_DATA, context.getItemInHand(), tag -> {
			tag.putInt(TAG_X, pos.getX());
			tag.putInt(TAG_Y, pos.getY());
			tag.putInt(TAG_Z, pos.getZ());
			tag.putString(TAG_DIMENSION, dimension);
		});
		return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
	}

	/**
	 * 把绑定器记录的位置设为星尘莲的传送目标。目标必须在星尘莲所在的维度。
	 * Sets the position that the binder recorded as the teleport target of the Stardust Lotus. The target must be in the dimension of the lotus.
	 */
	private static InteractionResult bindLotus(Level level, Player player, ItemStack stack, StardustLotusBlockEntity lotus) {
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		GlobalPos bound = getBoundPos(stack);
		if (bound == null || bound.pos().getY() == UNBOUND_Y || bound.dimension() != level.dimension()) {
			player.displayClientMessage(Component.translatable("message.extrabotany.stardust_lotus.invalid_bind")
					.withStyle(ChatFormatting.DARK_RED), true);
		} else {
			BlockPos target = bound.pos();
			lotus.setTarget(target);
			player.displayClientMessage(Component.translatable("message.extrabotany.stardust_lotus.bind_to_pos",
					target.getX(), target.getY(), target.getZ()), true);
		}
		return InteractionResult.CONSUME;
	}

	/**
	 * 读取绑定的位置。绑定器还没有绑定时返回 {@code null}。
	 * Reads the bound position. Returns {@code null} when the binder is not bound.
	 */
	@Nullable
	public static GlobalPos getBoundPos(ItemStack stack) {
		if (!ItemNBTHelper.verifyExistance(stack, TAG_Y)) {
			return null;
		}
		ResourceLocation dimensionId = ResourceLocation.tryParse(ItemNBTHelper.getString(stack, TAG_DIMENSION, DEFAULT_DIMENSION));
		if (dimensionId == null) {
			return null;
		}
		ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, dimensionId);
		return GlobalPos.of(dimension, new BlockPos(
				ItemNBTHelper.getInt(stack, TAG_X, 0),
				ItemNBTHelper.getInt(stack, TAG_Y, UNBOUND_Y),
				ItemNBTHelper.getInt(stack, TAG_Z, 0)));
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flags) {
		super.appendHoverText(stack, context, tooltip, flags);
		tooltip.add(Component.translatable("item.extrabotany.binder.bindx", ItemNBTHelper.getInt(stack, TAG_X, 0))
				.withStyle(ChatFormatting.GRAY));
		tooltip.add(Component.translatable("item.extrabotany.binder.bindy", ItemNBTHelper.getInt(stack, TAG_Y, UNBOUND_Y))
				.withStyle(ChatFormatting.GRAY));
		tooltip.add(Component.translatable("item.extrabotany.binder.bindz", ItemNBTHelper.getInt(stack, TAG_Z, 0))
				.withStyle(ChatFormatting.GRAY));
		tooltip.add(Component.translatable("item.extrabotany.binder.binddim",
				ItemNBTHelper.getString(stack, TAG_DIMENSION, DEFAULT_DIMENSION)).withStyle(ChatFormatting.GRAY));
	}
}
