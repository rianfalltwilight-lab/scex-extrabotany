package io.github.lounode.extrabotany.common.item.equipment.tool;

import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import io.github.lounode.extrabotany.common.entity.projectile.PhotonProjectileEntity;

/**
 * 光子霰弹枪。右键发射 12 颗弹丸。每颗弹丸的方向在 16 度的范围内随机偏移。
 * Photon Shotgun. A use shoots 12 pellets. The direction of each pellet has a random offset in a range of 16 degrees.
 *
 * <p>来源：ExtraMeteorP 1.12 {@code ItemPhotonShotgun}。
 * <p>Source: ExtraMeteorP 1.12 {@code ItemPhotonShotgun}.
 */
public class PhotonShotgunItem extends Item {
	private static final int PELLET_COUNT = 12;
	private static final float SPREAD_DEGREES = 16F;
	private static final double PELLET_SPEED = 3.2;

	public PhotonShotgunItem(Properties props) {
		super(props);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!level.isClientSide()) {
			for (int i = 0; i < PELLET_COUNT; i++) {
				float pitch = player.getXRot() - SPREAD_DEGREES / 2 + level.random.nextFloat() * SPREAD_DEGREES;
				float yaw = player.getYRot() - SPREAD_DEGREES / 2 + level.random.nextFloat() * SPREAD_DEGREES;
				PhotonProjectileEntity pellet = new PhotonProjectileEntity(level, player);
				pellet.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
				pellet.setDeltaMovement(Vec3.directionFromRotation(pitch, yaw).scale(PELLET_SPEED));
				pellet.setXRot(pitch);
				pellet.setYRot(yaw);
				level.addFreshEntity(pellet);
			}
		}
		player.awardStat(Stats.ITEM_USED.get(this));
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
	}
}
