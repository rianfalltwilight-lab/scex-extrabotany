package io.github.lounode.extrabotany.common.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import io.github.lounode.extrabotany.common.entity.mount.MountEntity;

/**
 * 放置坐骑的公共逻辑。规则和原版的船物品相同。
 * The common logic to place a mount. The rules are the same as for the vanilla boat item.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code ItemMotor} 和 {@code ItemCosmicCarKey}。两个类的放置代码完全相同。
 * <p>Source: ExtraMeteorP 1.16 {@code ItemMotor} and {@code ItemCosmicCarKey}. The placement code of the two classes is identical.
 */
public final class MountItems {
	private static final double REACH = 5.0;

	private MountItems() {}

	/**
	 * 创建坐骑实体的工厂。
	 * A factory that makes the mount entity.
	 */
	@FunctionalInterface
	public interface MountFactory {
		MountEntity create(Level level, Vec3 pos, Player player, ItemStack stack);
	}

	/**
	 * 把坐骑放在玩家准星指向的方块上，并且消耗物品。
	 * Puts a mount on the block that the player aims at, and uses up the item.
	 *
	 * @param hit 玩家视线的射线检测结果。射线必须把流体算作命中。 The ray trace result of the player view. The ray must count fluids as a hit.
	 */
	public static InteractionResultHolder<ItemStack> place(Level level, Player player, InteractionHand hand, BlockHitResult hit, MountFactory factory) {
		ItemStack stack = player.getItemInHand(hand);
		if (hit.getType() != HitResult.Type.BLOCK) {
			return InteractionResultHolder.pass(stack);
		}

		// 玩家的眼睛在另一个实体的碰撞箱里时不放置。
		// Do not place the mount when the eyes of the player are in the collision box of a different entity.
		Vec3 view = player.getViewVector(1F);
		Vec3 eye = player.getEyePosition();
		for (Entity entity : level.getEntities(player, player.getBoundingBox().expandTowards(view.scale(REACH)).inflate(1.0),
				EntitySelector.NO_SPECTATORS.and(Entity::isPickable))) {
			if (entity.getBoundingBox().inflate(entity.getPickRadius()).contains(eye)) {
				return InteractionResultHolder.pass(stack);
			}
		}

		MountEntity mount = factory.create(level, hit.getLocation(), player, stack);
		mount.setYRot(player.getYRot());
		if (!level.noCollision(mount, mount.getBoundingBox().inflate(-0.1))) {
			return InteractionResultHolder.fail(stack);
		}
		if (!level.isClientSide()) {
			level.addFreshEntity(mount);
			stack.consume(1, player);
		}
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
	}
}
