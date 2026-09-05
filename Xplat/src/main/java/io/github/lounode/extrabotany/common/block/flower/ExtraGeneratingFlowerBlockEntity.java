package io.github.lounode.extrabotany.common.block.flower;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import vazkii.botania.api.block_entity.GeneratingFlowerBlockEntity;

/**
 * Bridges ExtraBotany's 1.20 flower lifecycle onto Botania 1.21's
 * registry-aware persistence and throttled synchronization.
 */
public abstract class ExtraGeneratingFlowerBlockEntity extends GeneratingFlowerBlockEntity {
	protected long ticksExisted;

	protected ExtraGeneratingFlowerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public void tickFlower() {
		super.tickFlower();
		ticksExisted++;
	}

	protected final void sync() {
		setChanged();
		markForImmediateSync();
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		readFromPacketNBT(tag);
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		writeToPacketNBT(tag);
	}

	@Override
	protected void applyImplicitComponents(DataComponentInput componentInput) {
		super.applyImplicitComponents(componentInput);
		CustomData customData = componentInput.get(DataComponents.CUSTOM_DATA);
		if (customData != null) {
			readFromPacketNBT(customData.copyTag());
		}
	}

	public void readFromPacketNBT(CompoundTag tag) {}

	public void writeToPacketNBT(CompoundTag tag) {}
}
