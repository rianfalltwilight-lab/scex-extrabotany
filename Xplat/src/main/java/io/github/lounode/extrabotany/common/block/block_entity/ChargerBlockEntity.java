package io.github.lounode.extrabotany.common.block.block_entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

public abstract class ChargerBlockEntity extends ExposedSimpleInventoryBlockEntity implements Charger {

	public int tickCount;

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

		if (self.tickCount % 10 == 0) {
			self.setChanged();
			level.sendBlockUpdated(pos, state, state, 3);
		}

		var pool = self.getPool();
		if (pool == null) {
			return;
		}

		BlockPos poolPos = pool.getBlockPos();

		ManaItem mana = io.github.lounode.extrabotany.xplat.EXplatAbstractions.INSTANCE.findManaItem(self.getItem());
		if (mana == null || (!mana.canReceiveManaFromPool(pool) && !mana.canDrainManaToPool(pool))) {
			return;
		}
		boolean didSomething = false;

		var bellows = self.getBellows(level, poolPos, pool);

		int bellowCount = bellows.size();
		int transferRate = 1000 * (bellowCount + 1);

		if (pool.isOutputtingPower() && mana.canReceiveManaFromPool(pool)) {

			int output = pool.getCurrentMana();
			output = Math.min(output, mana.getMaxMana() - mana.getMana());
			output = Math.min(output, transferRate);

			if (output > 0) {
				didSomething = true;
				mana.addMana(output);
				pool.receiveMana(-output);
			}

		} else if (mana.canDrainManaToPool(pool)) {

			int input = mana.getMana();
			input = Math.min(input, pool.getMaxMana() - pool.getCurrentMana());
			input = Math.min(input, transferRate);

			if (input > 0) {
				didSomething = true;
				mana.addMana(-input);
				pool.receiveMana(input);
			}

		}

		if (didSomething) {
			if (self.tickCount % 10 == 0 && BotaniaConfig.common().chargingAnimationEnabled()) {
				self.chargeParticles();
			}

			for (var bellow : bellows) {
				bellow.setActive(true);
			}
		}

		self.tickCount++;
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
