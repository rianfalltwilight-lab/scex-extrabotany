package io.github.lounode.extrabotany.common.entity.vehicle;

import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Predicate;

/**
 * 飞艇的物品。用法和船相同：对着方块或水面使用就放下飞艇。来源：ExtraMeteorP 1.12 {@code ItemFlyingBoat}。
 * The item of a flying boat. It works like a boat: a use at a block or at water puts the flying boat there. Source: {@code ItemFlyingBoat} of ExtraMeteorP 1.12.
 */
public class FlyingBoatItem extends Item {
	private static final Predicate<Entity> ENTITY_PREDICATE = EntitySelector.NO_SPECTATORS.and(Entity::isPickable);
	private final FlyingBoatEntity.Kind kind;

	public FlyingBoatItem(FlyingBoatEntity.Kind kind, Properties properties) {
		super(properties);
		this.kind = kind;
	}

	public FlyingBoatEntity.Kind getKind() {
		return kind;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		HitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY);
		if (hit.getType() != HitResult.Type.BLOCK) {
			return InteractionResultHolder.pass(stack);
		}
		Vec3 view = player.getViewVector(1F);
		List<Entity> list = level.getEntities(player, player.getBoundingBox().expandTowards(view.scale(5)).inflate(1), ENTITY_PREDICATE);
		Vec3 eye = player.getEyePosition();
		for (Entity entity : list) {
			AABB box = entity.getBoundingBox().inflate(entity.getPickRadius());
			if (box.contains(eye)) {
				return InteractionResultHolder.pass(stack);
			}
		}
		Vec3 at = hit.getLocation();
		FlyingBoatEntity boat = new FlyingBoatEntity(level, at.x, at.y, at.z, kind);
		boat.setYRot(player.getYRot());
		if (!level.noCollision(boat, boat.getBoundingBox())) {
			return InteractionResultHolder.fail(stack);
		}
		if (!level.isClientSide()) {
			level.addFreshEntity(boat);
			level.gameEvent(player, GameEvent.ENTITY_PLACE, at);
			stack.consume(1, player);
		}
		player.awardStat(Stats.ITEM_USED.get(this));
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
	}
}
