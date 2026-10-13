package io.github.lounode.extrabotany.common.block.block_entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import vazkii.botania.api.block.PetalApothecary;

/**
 * 活石桶的方块实体。它储存 16 桶同一种液体。桶里有水时，它给旁边的空花药台加水。
 * The block entity of the Livingrock Barrel. It holds 16 buckets of one fluid. When the barrel holds water, it fills the adjacent empty Petal Apothecaries.
 *
 * <p>液体存在 {@code Fluid} 键里。这个移植版的旧存档使用 {@code fluid} 和 {@code amount} 键，读取时仍然支持。
 * <p>The fluid is in the key {@code Fluid}. Old saves of this port use the keys {@code fluid} and {@code amount}, and the load still accepts them.
 *
 * <p>来源：ExtraMeteorP 1.12 {@code TileLivingrockBarrel}。
 * <p>Source: ExtraMeteorP 1.12 {@code TileLivingrockBarrel}.
 *
 * <p>性能：桶每 {@value #WORK_INTERVAL} tick 工作一次。原版每 tick 查找六个方向的花药台，并且每次液体变化都发一个数据包。
 * <p>Performance: the barrel does its work one time for each {@value #WORK_INTERVAL} ticks. The original looks for apothecaries on six sides on each tick, and sends one packet for each change of the fluid.
 */
public class LivingrockBarrelBlockEntity extends BlockEntity implements IFluidHandler {
	public static final int CAPACITY = 16000;

	private static final String TAG_FLUID = "Fluid";
	private static final String TAG_LEGACY_FLUID = "fluid";
	private static final String TAG_LEGACY_AMOUNT = "amount";
	private static final int WORK_INTERVAL = 10;
	private static final int BUCKET = 1000;
	private static final Direction[] ALL_SIDES = Direction.values();

	private FluidStack fluid = FluidStack.EMPTY;
	private boolean syncPending;

	public LivingrockBarrelBlockEntity(BlockPos pos, BlockState state) {
		super(ExtraBotanyBlockEntities.LIVINGROCK_BARREL, pos, state);
	}

	public FluidStack getFluid() {
		return fluid;
	}

	public void serverTick() {
		// 不同位置的桶在不同的 tick 工作。
		// Barrels at different positions do their work on different ticks.
		if (level == null || Math.floorMod(level.getGameTime() + worldPosition.asLong(), WORK_INTERVAL) != 0) {
			return;
		}
		if (fluid.is(Fluids.WATER) && fluid.getAmount() >= BUCKET) {
			fillApothecaries();
		}
		if (syncPending) {
			syncPending = false;
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
		}
	}

	private void fillApothecaries() {
		for (Direction side : ALL_SIDES) {
			if (fluid.getAmount() < BUCKET) {
				return;
			}
			if (level.getBlockEntity(worldPosition.relative(side)) instanceof PetalApothecary apothecary
					&& apothecary.getFluid() == PetalApothecary.State.EMPTY) {
				apothecary.setFluid(PetalApothecary.State.WATER);
				drain(BUCKET, FluidAction.EXECUTE);
			}
		}
	}

	private void markChanged() {
		setChanged();
		syncPending = true;
	}

	@Override
	public int getTanks() {
		return 1;
	}

	@Override
	public FluidStack getFluidInTank(int tank) {
		return tank == 0 ? fluid : FluidStack.EMPTY;
	}

	@Override
	public int getTankCapacity(int tank) {
		return tank == 0 ? CAPACITY : 0;
	}

	@Override
	public boolean isFluidValid(int tank, FluidStack stack) {
		return tank == 0;
	}

	@Override
	public int fill(FluidStack resource, FluidAction action) {
		if (resource.isEmpty() || !fluid.isEmpty() && !FluidStack.isSameFluidSameComponents(fluid, resource)) {
			return 0;
		}
		int accepted = Math.min(resource.getAmount(), CAPACITY - fluid.getAmount());
		if (accepted > 0 && action.execute()) {
			fluid = resource.copyWithAmount(fluid.getAmount() + accepted);
			markChanged();
		}
		return accepted;
	}

	@Override
	public FluidStack drain(FluidStack resource, FluidAction action) {
		if (resource.isEmpty() || !FluidStack.isSameFluidSameComponents(fluid, resource)) {
			return FluidStack.EMPTY;
		}
		return drain(resource.getAmount(), action);
	}

	@Override
	public FluidStack drain(int maxDrain, FluidAction action) {
		int removed = Math.min(Math.max(0, maxDrain), fluid.getAmount());
		if (removed == 0) {
			return FluidStack.EMPTY;
		}
		FluidStack result = fluid.copyWithAmount(removed);
		if (action.execute()) {
			fluid = fluid.copyWithAmount(fluid.getAmount() - removed);
			markChanged();
		}
		return result;
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		if (!fluid.isEmpty()) {
			tag.put(TAG_FLUID, fluid.save(registries));
		}
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		if (tag.contains(TAG_LEGACY_FLUID)) {
			fluid = loadLegacyFluid(tag);
		} else {
			fluid = FluidStack.parseOptional(registries, tag.getCompound(TAG_FLUID));
		}
		if (fluid.getAmount() > CAPACITY) {
			fluid.setAmount(CAPACITY);
		}
	}

	/**
	 * 读取这个移植版旧存档的格式：一个液体 ID 和一个数量。
	 * Reads the format of the old saves of this port: one fluid ID and one quantity.
	 */
	private static FluidStack loadLegacyFluid(CompoundTag tag) {
		ResourceLocation id = ResourceLocation.tryParse(tag.getString(TAG_LEGACY_FLUID));
		Fluid legacyFluid = id == null ? Fluids.EMPTY : BuiltInRegistries.FLUID.get(id);
		int amount = tag.getInt(TAG_LEGACY_AMOUNT);
		return legacyFluid == Fluids.EMPTY || amount <= 0 ? FluidStack.EMPTY : new FluidStack(legacyFluid, amount);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		return saveWithoutMetadata(registries);
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}
}
