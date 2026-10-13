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
 * 这个模组的产能花的基类。
 * The base class of the generating flowers of this mod.
 *
 * <p>这些花的代码来自 1.20 版。那时植物魔法用 {@code writeToPacketNBT} 和 {@code readFromPacketNBT} 同时处理存档和同步。植物魔法 1.21 把这两件事分开了。这个基类把旧的写法接到新的接口上。
 * <p>The code of these flowers comes from the 1.20 version. At that time Botania used {@code writeToPacketNBT} and {@code readFromPacketNBT} for the save and for the synchronization together. Botania 1.21 made these two tasks separate. This base class connects the old pattern to the new interface.
 *
 * <p>子类在 {@link #writeToPacketNBT} 和 {@link #readFromPacketNBT} 里读写自己的数据。这个基类把这些数据用于存档、用于同步到客户端，也用于读取物品上的数据。
 * <p>A subclass reads and writes its data in {@link #writeToPacketNBT} and {@link #readFromPacketNBT}. This base class uses that data for the save, for the synchronization to the clients, and to read the data of the item.
 */
public abstract class ExtraGeneratingFlowerBlockEntity extends GeneratingFlowerBlockEntity {
	/** 一次没有结果的查找之后，默认休眠的 tick 数。 / The default number of ticks to sleep after a search that found nothing. */
	protected static final int IDLE_BACKOFF_TICKS = 5;

	protected long ticksExisted;
	private long idleUntil;

	protected ExtraGeneratingFlowerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public void tickFlower() {
		super.tickFlower();
		ticksExisted++;
	}

	/**
	 * 立刻把数据同步到客户端，并标记区块需要保存。只用于客户端必须马上看到的变化。只有魔力变化时不要调用它。
	 * Immediately synchronizes the data to the clients and marks the chunk for saving. Only use it for changes that the client must see at once. Do not call it when only the mana changes.
	 */
	protected final void sync() {
		setChanged();
		markForImmediateSync();
	}

	/**
	 * 稍后同步数据。植物魔法按固定的间隔合并这些同步。
	 * Synchronizes the data later. Botania collects these synchronizations at a fixed interval.
	 */
	protected final void syncThrottled() {
		markForPotentialSync();
	}

	/**
	 * 花是否正在休眠。花在一次没有结果的查找之后休眠几 tick，这样它不会每 tick 都查找。
	 * Tells if the flower sleeps at this time. The flower sleeps for some ticks after a search that found nothing, thus it does not search on each tick.
	 */
	protected final boolean isIdleBackoff() {
		return level != null && level.getGameTime() < idleUntil;
	}

	protected final void backoffIdle(int ticks) {
		if (level != null) {
			idleUntil = level.getGameTime() + ticks;
		}
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
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		// 客户端用 loadAdditional 读取这个标签，所以它必须带有子类的数据。
		// The client reads this tag with loadAdditional, thus the tag must have the data of the subclass.
		CompoundTag tag = super.getUpdateTag(registries);
		writeToPacketNBT(tag);
		return tag;
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
