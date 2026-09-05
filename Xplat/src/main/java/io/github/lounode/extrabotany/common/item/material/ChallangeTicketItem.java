package io.github.lounode.extrabotany.common.item.material;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

import org.jetbrains.annotations.NotNull;

import io.github.lounode.extrabotany.common.entity.gaia.GaiaIII;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

public class ChallangeTicketItem extends Item {

	public ChallangeTicketItem(Properties properties) {
		super(properties);
	}

	@NotNull
	@Override
	public InteractionResult useOn(UseOnContext ctx) {
		ItemStack stack = ctx.getItemInHand();
		Player player = ctx.getPlayer();

		if (stack.is(ExtraBotanyItems.challengeTicket)) {
			if (GaiaIII.spawn(ctx.getPlayer(), stack, ctx.getLevel(), ctx.getClickedPos())) {
				stack.shrink(player.isCreative() ? 0 : 1);
				return InteractionResult.sidedSuccess(ctx.getLevel().isClientSide());
			}
			return InteractionResult.CONSUME;
		}

		return super.useOn(ctx);
	}
}
