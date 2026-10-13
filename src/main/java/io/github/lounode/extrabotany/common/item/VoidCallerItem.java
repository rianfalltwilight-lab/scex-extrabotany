package io.github.lounode.extrabotany.common.item;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

import io.github.lounode.extrabotany.common.entity.gaia.VoidHerrscher;

/**
 * 来自虚空的呼唤。玩家潜行时用它右键盖亚仪式场地中间的信标，召唤空之律者。召唤成功时消耗一个。
 * Call of the Void. A player uses it, with sneak, on the beacon in the center of a Gaia ritual arena to summon the Herrscher of the Void. A successful summon uses one item.
 *
 * <p>来源：ExtraMeteorP 1.12 {@code ItemMaterial}（元数据 9）。
 * <p>Source: ExtraMeteorP 1.12 {@code ItemMaterial} (metadata 9).
 */
public class VoidCallerItem extends Item {
	public VoidCallerItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Player player = context.getPlayer();
		if (player == null || !player.isShiftKeyDown()) {
			return InteractionResult.PASS;
		}
		if (!VoidHerrscher.spawn(player, context.getLevel(), context.getClickedPos())) {
			return InteractionResult.FAIL;
		}
		if (!context.getLevel().isClientSide()) {
			context.getItemInHand().consume(1, player);
		}
		return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
	}
}
