package io.github.lounode.extrabotany.common.item;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import vazkii.botania.api.block_entity.FunctionalFlowerBlockEntity;
import vazkii.botania.api.block_entity.GeneratingFlowerBlockEntity;
import vazkii.botania.api.mana.ManaReceiver;
import vazkii.botania.common.handler.BotaniaSounds;
import io.github.lounode.extrabotany.common.helper.ItemNBTHelper;
import vazkii.botania.common.helper.PlayerHelper;

import io.github.lounode.extrabotany.common.lib.LibAdvancementNames;
import io.github.lounode.extrabotany.network.clientbound.ManaReaderPacket;
import io.github.lounode.extrabotany.xplat.EXplatAbstractions;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class ManaReaderItem extends Item {
	private static final int ADVANCE_REQUIRE = 1000;

	public ManaReaderItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext ctx) {
		Player player = ctx.getPlayer();
		if (player == null) {
			return InteractionResult.PASS;
		}
		Level world = ctx.getLevel();
		BlockPos pos = ctx.getClickedPos();
		BlockEntity tile = world.getBlockEntity(pos);
		if (!canReadMana(tile)) {
			return InteractionResult.PASS;
		}
		if (world.isClientSide) {
			player.playSound(BotaniaSounds.DING, 0.6F, 0.1F + world.random.nextFloat() * 0.5F);
			return InteractionResult.SUCCESS;
		}

		int manaValue = tryReadMana(tile);
		if (manaValue < 0) {
			return InteractionResult.SUCCESS;
		}
		EXplatAbstractions.INSTANCE.sendToPlayer((ServerPlayer) player, new ManaReaderPacket(manaValue));

		ItemStack stack = ctx.getItemInHand();
		int currentCount = ItemNBTHelper.getInt(stack, "count", 0) + 1;
		ItemNBTHelper.setInt(stack, "count", currentCount);

		if (currentCount >= ADVANCE_REQUIRE) {
			PlayerHelper.grantCriterion((ServerPlayer) player, prefix("main/" + LibAdvancementNames.SENBON_ZAKURA), "code_triggered");
		}

		return InteractionResult.SUCCESS;
	}

	public static boolean canReadMana(BlockEntity tile) {
		return tile instanceof ManaReceiver ||
				tile instanceof GeneratingFlowerBlockEntity ||
				tile instanceof FunctionalFlowerBlockEntity;
	}

	public static int tryReadMana(BlockEntity tile) {
		int mana = -1;
		if (tile instanceof ManaReceiver receiver) {
			mana = receiver.getCurrentMana();
		} else if (tile instanceof GeneratingFlowerBlockEntity genFlower) {
			mana = genFlower.getMana();
		} else if (tile instanceof FunctionalFlowerBlockEntity funcFlower) {
			mana = funcFlower.getMana();
		}

		return mana;
	}
}
