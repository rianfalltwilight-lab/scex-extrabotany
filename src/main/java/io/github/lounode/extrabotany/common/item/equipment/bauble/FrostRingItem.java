package io.github.lounode.extrabotany.common.item.equipment.bauble;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

import vazkii.botania.common.item.equipment.bauble.BaubleItem;

/**
 * 霜冻指环。佩戴者脚下的水结成霜冰，脚下的岩浆变成黑曜石。佩戴者盯着看的生物会被减速。
 * Frost Ring. The water below the wearer becomes frosted ice, and the lava below the wearer becomes obsidian. A creature that the wearer looks at is slowed.
 *
 * <p>来源：重制版（Lounode）。它对应 ExtraMeteorP 1.12 的 {@code ItemFrostStar}。
 * <p>Source: Reburn (Lounode). It corresponds to {@code ItemFrostStar} of ExtraMeteorP 1.12.
 *
 * <p>性能：结冰只在佩戴者换了一格之后进行，另外每 {@value #IDLE_FREEZE_INTERVAL} tick 进行一次。减速每 {@value #SLOW_INTERVAL} tick 进行一次。以前这三件事每 tick 都做，每次要读一千多个方块。
 * <p>Performance: the freeze only runs after the wearer moves to a different block, and also one time for each {@value #IDLE_FREEZE_INTERVAL} ticks. The slow effect runs one time for each {@value #SLOW_INTERVAL} ticks. Before, these three tasks ran on each tick, and each run read more than one thousand blocks.
 */
public class FrostRingItem extends BaubleItem {
	private static final int RANGE = 6;
	private static final int IDLE_FREEZE_INTERVAL = 20;
	private static final int SLOW_INTERVAL = 10;

	public FrostRingItem(Properties props) {
		super(props);
	}

	@Override
	public void onWornTick(ItemStack stack, LivingEntity entity) {
		super.onWornTick(stack, entity);
		if (entity.level().isClientSide()) {
			return;
		}
		BlockPos pos = entity.blockPosition();
		boolean moved = !pos.equals(BlockPos.containing(entity.xo, entity.yo, entity.zo));
		if (moved || entity.tickCount % IDLE_FREEZE_INTERVAL == 0) {
			freezeWater(entity, entity.level(), pos, getRange());
			freezeLava(entity, entity.level(), pos, getRange());
		}
		if (entity.tickCount % SLOW_INTERVAL == 0) {
			slowDownMobsNearby(entity);
		}
	}

	public void slowDownMobsNearby(LivingEntity owner) {
		AABB area = new AABB(owner.position(), owner.position()).inflate(getRange());
		for (LivingEntity living : owner.level().getEntitiesOfClass(LivingEntity.class, area)) {
			if (living != owner && isLookingAt(owner, living)) {
				living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 4));
			}
		}
	}

	/**
	 * 判断一个生物是否正盯着另一个生物看。算法和末影人的"被注视"判定相同。
	 * Finds if a creature looks directly at a different creature. The algorithm is the same as the "stared at" check of the Enderman.
	 */
	private static boolean isLookingAt(LivingEntity from, LivingEntity target) {
		Vec3 view = from.getViewVector(1.0F).normalize();
		Vec3 toTarget = new Vec3(target.getX() - from.getX(), target.getEyeY() - from.getEyeY(), target.getZ() - from.getZ());
		double distance = toTarget.length();
		double dot = view.dot(toTarget.normalize());
		return dot > 1.0 - 0.025 / distance && from.hasLineOfSight(target);
	}

	/**
	 * 把生物脚下一圈的岩浆源变成黑曜石。只有站在地面上的生物才有这个效果。
	 * Changes the lava sources in a circle below the creature into obsidian. This only applies to a creature that stands on the ground.
	 *
	 * @param strength 效果的强度。半径是它加 2，最大 16。 / the strength of the effect. The radius is this value plus 2, with a maximum of 16.
	 */
	public static void freezeLava(LivingEntity living, Level level, BlockPos pos, int strength) {
		freeze(living, level, pos, strength, Blocks.LAVA.defaultBlockState(), Blocks.OBSIDIAN.defaultBlockState());
	}

	/**
	 * 把生物脚下一圈的水源变成霜冰，和原版的冰霜行者相同。流动的水不会结冰。只有站在地面上的生物才有这个效果。
	 * Changes the water sources in a circle below the creature into frosted ice, the same as vanilla Frost Walker. Flowing water does not freeze. This only applies to a creature that stands on the ground.
	 *
	 * @param strength 效果的强度。半径是它加 2，最大 16。 / the strength of the effect. The radius is this value plus 2, with a maximum of 16.
	 */
	public static void freezeWater(LivingEntity living, Level level, BlockPos pos, int strength) {
		freeze(living, level, pos, strength, Blocks.WATER.defaultBlockState(), Blocks.FROSTED_ICE.defaultBlockState());
	}

	private static void freeze(LivingEntity living, Level level, BlockPos pos, int strength, BlockState source, BlockState frozen) {
		if (!living.onGround()) {
			return;
		}
		int range = Math.min(16, 2 + strength);
		BlockPos.MutableBlockPos above = new BlockPos.MutableBlockPos();
		for (BlockPos target : BlockPos.betweenClosed(pos.offset(-range, -1, -range), pos.offset(range, -1, range))) {
			if (!target.closerToCenterThan(living.position(), range)) {
				continue;
			}
			above.set(target.getX(), target.getY() + 1, target.getZ());
			// 只有源方块才会被冻结：方块状态必须和默认状态完全相同。
			// Only a source block freezes: the block state must be fully equal to the default state.
			if (level.getBlockState(above).isAir()
					&& level.getBlockState(target) == source
					&& frozen.canSurvive(level, target)
					&& level.isUnobstructed(frozen, target, CollisionContext.empty())) {
				level.setBlockAndUpdate(target, frozen);
				level.scheduleTick(target, frozen.getBlock(), Mth.nextInt(living.getRandom(), 60, 120));
			}
		}
	}

	public int getRange() {
		return RANGE;
	}
}
