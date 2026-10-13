package io.github.lounode.extrabotany.common.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;

import io.github.lounode.extrabotany.common.entity.mount.UfoEntity;

/**
 * 宇宙车钥匙。对着方块使用时放下一辆飞碟。
 * Cosmic Car Key. A use on a block puts down a Cosmic Car.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code ItemCosmicCarKey}。
 * <p>Source: ExtraMeteorP 1.16 {@code ItemCosmicCarKey}.
 */
public class CosmicCarKeyItem extends Item {
	public CosmicCarKeyItem(Properties props) {
		super(props);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		return MountItems.place(level, player, hand, getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY),
				(mountLevel, pos, placer, stack) -> new UfoEntity(mountLevel, pos));
	}
}
