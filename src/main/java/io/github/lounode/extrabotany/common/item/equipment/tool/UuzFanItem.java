package io.github.lounode.extrabotany.common.item.equipment.tool;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import io.github.lounode.extrabotany.common.entity.projectile.ButterflyProjectileEntity;

/**
 * 西行寺幽幽子的折扇。右键向前方扇形发射三只蝴蝶。
 * Saigyouji Yuyuko's Fan. A use shoots three butterflies in a fan shape.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code ItemUUZFan}。
 * <p>Source: ExtraMeteorP 1.16 {@code ItemUUZFan}.
 */
public class UuzFanItem extends Item {
	private static final int COOLDOWN_TICKS = 10;
	private static final float SPREAD_DEGREES = 25F;
	private static final float SPEED = 0.5F;

	public UuzFanItem(Properties props) {
		super(props);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
		if (!level.isClientSide()) {
			for (int i = -1; i <= 1; i++) {
				ButterflyProjectileEntity butterfly = new ButterflyProjectileEntity(level, player);
				butterfly.setPos(player.getX(), player.getY(), player.getZ());
				butterfly.shootFromRotation(player, player.getXRot(), player.getYRot() + SPREAD_DEGREES * i, 0F, SPEED, 1F);
				level.addFreshEntity(butterfly);
			}
		}
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
	}
}
