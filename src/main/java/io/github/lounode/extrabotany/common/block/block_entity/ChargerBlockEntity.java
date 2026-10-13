package io.github.lounode.extrabotany.common.block.block_entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.mana.ManaItem;
import vazkii.botania.common.block.block_entity.ExposedSimpleInventoryBlockEntity;
import vazkii.botania.common.block.block_entity.mana.BellowsBlockEntity;
import vazkii.botania.common.block.block_entity.mana.ManaPoolBlockEntity;
import vazkii.botania.xplat.BotaniaConfig;

import io.github.lounode.extrabotany.api.block.Charger;
import io.github.lounode.extrabotany.xplat.EXplatAbstractions;

import java.util.ArrayList;
import java.util.List;

/**
 * 魔力充能方块的基类。它保存一个魔力物品，并在这个物品和相邻的魔力池之间转移魔力，每 tick 最多 1000 点。
 * The base class of the mana charging blocks. It holds one mana item and moves mana between this item and the adjacent mana pool, a maximum of 1000 mana on each tick.
 *
 * <p>魔力池处于输出模式时，魔力从魔力池流向物品。否则魔力从物品流回魔力池。
 * <p>When the mana pool is in the output mode, the mana goes from the pool to the item. If not, the mana goes from the item back to the pool.
 *
 * <p>来源：重制版（Lounode）。它对应 ExtraMeteorP 1.12 的 {@code TileChargePad}。
 * <p>Source: Reburn (Lounode). It corresponds to {@code TileChargePad} of ExtraMeteorP 1.12.
 */
public abstract class ChargerBlockEntity extends ExposedSimpleInventoryBlockEntity implements Charger {

	public int tickCount;
	private boolean syncPending;

	public ChargerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state, true);
	}

	@Override
	protected SimpleContainer createItemHandler() {
		return new SimpleContainer(1) {
			@Override
			public int getMaxStackSize() {
				return 1;
			}
		};
	}

	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (isEmpty()) {
			ItemStack stack = player.getItemInHand(hand);
			if (!isValidItem(stack)) {
				return InteractionResult.PASS;
			}
			ItemStack put = stack.split(1);
			setItem(put);
			return InteractionResult.SUCCESS;
		} else {
			ItemStack get = getItem().copy();
			setItem(ItemStack.EMPTY);
			if (player.addItem(get)) {
				playSound();
			} else {
				Containers.dropItemStack(world, player.getX(), player.getY(), player.getZ(), get);
			}
			return InteractionResult.SUCCESS;
		}
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, ChargerBlockEntity self) {
		int tick = self.tickCount++;
		boolean transferred = self.transferMana(level, tick);
		if (transferred) {
			// ManaItem mutates the stack in place, bypassing the container listener.
			// Save every change without notifying comparators on every transfer tick.
			level.blockEntityChanged(pos);
			self.syncPending = true;
		}
		// Flush after work as well as on the normal cadence, including early-exit paths.
		if (self.syncPending && (tick % 10 == 0 || !transferred)) {
			self.syncPending = false;
			self.syncItem();
		}
	}

	private boolean transferMana(Level level, int tick) {
		if (isEmpty()) {
			return false;
		}
		var pool = getPool();
		if (pool == null) {
			return false;
		}

		BlockPos poolPos = pool.getBlockPos();

		ManaItem mana = EXplatAbstractions.INSTANCE.findManaItem(getItem());
		if (mana == null || (!mana.canReceiveManaFromPool(pool) && !mana.canDrainManaToPool(pool))) {
			return false;
		}
		boolean charging = pool.isOutputtingPower() && mana.canReceiveManaFromPool(pool);
		int amount;
		if (charging) {
			amount = Math.min(pool.getCurrentMana(), mana.getMaxMana() - mana.getMana());
		} else if (mana.canDrainManaToPool(pool)) {
			amount = Math.min(mana.getMana(), pool.getMaxMana() - pool.getCurrentMana());
		} else {
			return false;
		}
		if (amount <= 0) {
			return false;
		}

		// Only inspect adjacent bellows when mana can actually move.
		var bellows = getBellows(level, poolPos, pool);
		amount = Math.min(amount, 1000 * (bellows.size() + 1));
		mana.addMana(charging ? amount : -amount);
		pool.receiveMana(charging ? -amount : amount);

		if (tick % 10 == 0 && BotaniaConfig.common().chargingAnimationEnabled()) {
			chargeParticles();
		}
		for (var bellow : bellows) {
			bellow.setActive(true);
		}
		return true;
	}

	@Override
	public void setChanged() {
		super.setChanged();
		syncPending = true;
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		// The inherited inventory loader calls setChanged; loading is not a new mutation.
		syncPending = false;
	}

	protected void syncItem() {
		super.setChanged();
		level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
	}

	public static void clientTick(Level level, BlockPos pos, BlockState state, ChargerBlockEntity self) {
		/*
		if (level.getGameTime() % 10 == 0 && BotaniaConfig.common().chargingAnimationEnabled()) {
		
		}
		
		*/
		self.tickCount++;
	}

	public void chargeParticles() {
		var pool = getPool();
		if (pool == null) {
			return;
		}

		BlockPos poolPos = pool.getBlockPos();

		level.blockEvent(poolPos, level.getBlockState(poolPos).getBlock(), pool.isOutputtingPower() ? 1 : 2,
				encodeRelativeItemPosition(poolPos, worldPosition.getX() + 0.5F, worldPosition.getY(), worldPosition.getZ() + 0.5F));
	}

	@Override
	public void setItem(ItemStack stack) {
		setItem(0, stack);
	}

	@Override
	public ItemStack getItem() {
		return getItem(0);
	}

	@Override
	public float getChargeProcess() {
		var mana = io.github.lounode.extrabotany.xplat.EXplatAbstractions.INSTANCE.findManaItem(getItem());
		if (mana != null) {
			return (float) ((double) mana.getMana() / (double) mana.getMaxMana());
		}
		return -1;
	}

	@Override
	public boolean canPlaceItem(int index, ItemStack stack) {
		return isValidItem(stack);
	}

	private void playSound() {
		level.playSound(null, worldPosition, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, .2f,
				((level.random.nextFloat() - level.random.nextFloat()) * .7f + 1) * 2);
	}

	@Nullable
	public abstract ManaPoolBlockEntity getPool();

	public List<BellowsBlockEntity> getBellows(Level level, BlockPos worldPosition, ManaPoolBlockEntity self) {
		List<BellowsBlockEntity> bellows = new ArrayList<>();

		for (Direction dir : Direction.Plane.HORIZONTAL) {
			BlockEntity tile = level.getBlockEntity(worldPosition.relative(dir));
			if (tile instanceof BellowsBlockEntity bellow) {
				if (bellow.getLinkedTile() == self) {
					bellows.add(bellow);
				}
			}
		}

		return bellows;
	}

	private static int encodeRelativeItemPosition(BlockPos worldPosition, double x, double y, double z) {
		double relX = Mth.clamp(x - (double) worldPosition.getX(), 0.0F, 1.0F);
		double relY = Mth.clamp((double) 0.125F + (double) 0.875F * (y - (double) worldPosition.getY()), 0.125F, 0.9);
		double relZ = Mth.clamp(z - (double) worldPosition.getZ(), 0.0F, 1.0F);
		int compressedX = (int) Math.round((double) 7.0F * relX);
		int compressedY = 4 - Mth.ceillog2(14 - (int) ((double) 14.0F * relY));
		int compressedZ = (int) Math.round((double) 7.0F * relZ);
		return compressedX | compressedY << 3 | compressedZ << 5;
	}
}
