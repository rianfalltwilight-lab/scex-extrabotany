package io.github.lounode.extrabotany.common.item.equipment.tool.hammer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import vazkii.botania.api.item.SequentialBreaker;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.annotations.SoftImplement;
import io.github.lounode.extrabotany.common.helper.ItemNBTHelper;
import vazkii.botania.common.item.equipment.tool.ToolCommons;

import io.github.lounode.extrabotany.common.sounds.ExtraBotanySounds;

import java.util.function.Predicate;

public class TerrasteelHammerItem extends ManasteelHammerItem implements SequentialBreaker {
	private static final String TAG_ENABLED = "Enabled";
	private static final int MANA_PER_DAMAGE = 100;
	private static final int ACTIVE_COST = 10;
	public static final int RANGE = 2;

	public TerrasteelHammerItem(Tier tier, int attackDamageModifier, float attackSpeedModifier, Properties properties) {
		super(tier, attackDamageModifier, attackSpeedModifier, properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);

		if (!player.isSecondaryUseActive()) {
			return InteractionResultHolder.pass(stack);
		}
		if (hand != InteractionHand.MAIN_HAND) {
			return InteractionResultHolder.pass(stack);
		}

		BlockHitResult blockhitresult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
		if (blockhitresult.getType() == HitResult.Type.BLOCK) {
			return InteractionResultHolder.pass(stack);
		}

		setEnabled(stack, !isEnabled(stack));

		if (!level.isClientSide) {
			level.playSound(null, player.getX(), player.getY(), player.getZ(), ExtraBotanySounds.HAMMER_USE, SoundSource.PLAYERS, 1F, 1F);
		}

		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
	}

	//TODO BlockEventWrapper
	@SoftImplement("IForgeItem")
	public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, Player player) {
		BlockHitResult raycast = ToolCommons.raytraceFromEntity(player, 10, false);
		if (!player.level().isClientSide && raycast.getType() == HitResult.Type.BLOCK) {
			Direction face = raycast.getDirection();
			breakOtherBlock(player, stack, pos, pos, face);
		}

		return false;
	}

	@Override
	public void breakOtherBlock(Player player, ItemStack stack, BlockPos pos, BlockPos originPos, Direction side) {
		if (!isEnabled(stack)) {
			return;
		}

		Level world = player.level();
		Predicate<BlockState> canMine = state -> {
			boolean rightToolForDrops = !state.requiresCorrectToolForDrops() || stack.isCorrectToolForDrops(state);
			boolean rightToolForSpeed = stack.getDestroySpeed(state) > 1
					|| state.is(BlockTags.MINEABLE_WITH_SHOVEL)
					|| state.is(BlockTags.MINEABLE_WITH_HOE);
			return rightToolForDrops && rightToolForSpeed;
		};

		BlockState targetState = world.getBlockState(pos);
		if (!canMine.test(targetState)) {
			return;
		}

		if (world.isEmptyBlock(pos)) {
			return;
		}

		boolean doX = side.getStepX() == 0;
		boolean doY = side.getStepY() == 0;
		boolean doZ = side.getStepZ() == 0;

		int range = getRange(player, stack, pos, originPos, side);

		Vec3i beginDiff = new Vec3i(doX ? -range : 0, doY ? -1 : 0, doZ ? -range : 0);
		Vec3i endDiff = new Vec3i(doX ? range : 0, doY ? range * 2 - 1 : 0, doZ ? range : 0);

		ToolCommons.removeBlocksInIteration(player, stack, world, pos, beginDiff, endDiff, canMine);
	}

	@Override
	public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
		super.inventoryTick(stack, world, entity, slot, selected);
		if (!(entity instanceof Player player)) {
			return;
		}
		if (!isEnabled(stack)) {
			return;
		}
		if (player.swinging) {
			return;
		}
		if (!ManaItemHandler.instance().requestManaExactForTool(stack, player, getActiveCost(), true)) {
			setEnabled(stack, false);
		}
	}

	public static boolean isEnabled(ItemStack stack) {
		return ItemNBTHelper.getBoolean(stack, TAG_ENABLED, false);
	}

	public void setEnabled(ItemStack stack, boolean enabled) {
		ItemNBTHelper.setBoolean(stack, TAG_ENABLED, enabled);
	}

	@Override
	public int getManaPerDamage() {
		return MANA_PER_DAMAGE;
	}

	public int getActiveCost() {
		return ACTIVE_COST;
	}

	public int getRange(Player player, ItemStack stack, BlockPos pos, BlockPos originPos, Direction side) {
		return RANGE;
	}
}
