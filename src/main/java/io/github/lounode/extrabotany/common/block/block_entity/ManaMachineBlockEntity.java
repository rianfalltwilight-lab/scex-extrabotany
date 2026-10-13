package io.github.lounode.extrabotany.common.block.block_entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.block.Wandable;
import vazkii.botania.api.mana.ManaCollector;
import vazkii.botania.api.mana.ManaReceiver;
import vazkii.botania.api.mana.spark.ManaSparkAttachable;

/**
 * 储存魔力的机器的公共部分：魔力缓存器、FE转换器和液态魔力转化器。
 * The common part of the machines that hold mana: the Mana Buffer, the Flux Manafield and the Mana Liquefaction Device.
 *
 * <p>这些机器可以接收魔力脉冲，也可以连接火花。森林法杖右键机器时，机器立刻把数据同步到客户端。
 * <p>These machines can receive mana bursts, and a spark can attach to them. When the Wand of the Forest is used on a machine, the machine immediately synchronizes its data to the clients.
 *
 * <p>魔力存在 {@code mana} 键里。旧存档使用相同的键名。
 * <p>The mana is in the key {@code mana}. Old saves use the same key.
 *
 * <p>性能：数据改变时，机器最多每 {@value #SYNC_INTERVAL} tick 发一次同步数据包。
 * <p>Performance: when the data changes, a machine sends at most one synchronization packet for each {@value #SYNC_INTERVAL} ticks.
 */
public abstract class ManaMachineBlockEntity extends BlockEntity implements ManaReceiver, ManaSparkAttachable, Wandable {
	protected static final Direction[] ALL_SIDES = Direction.values();

	private static final String TAG_MANA = "mana";
	private static final int SYNC_INTERVAL = 10;

	private int mana;
	private boolean syncPending;

	protected ManaMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public abstract int getMaxMana();

	/**
	 * 机器每 tick 的工作。只在服务端运行。
	 * The work of the machine on each tick. This only runs on the server.
	 */
	protected abstract void tickMachine(ServerLevel level);

	public final void serverTick() {
		if (!(level instanceof ServerLevel serverLevel)) {
			return;
		}
		tickMachine(serverLevel);
		if (syncPending && serverLevel.getGameTime() % SYNC_INTERVAL == 0) {
			syncNow();
		}
	}

	/**
	 * 记录数据已经改变。区块会被保存，客户端会在稍后收到新数据。
	 * Records that the data changed. The chunk will be saved, and the clients will get the new data later.
	 */
	protected final void markChanged() {
		syncPending = true;
		if (level == null) {
			return;
		}
		level.blockEntityChanged(worldPosition);
		// 只有带比较器输出的方块才需要通知旁边的比较器。
		// Only a block with a comparator output must tell the adjacent comparators.
		BlockState state = getBlockState();
		if (state.hasAnalogOutputSignal()) {
			level.updateNeighbourForOutputSignal(worldPosition, state.getBlock());
		}
	}

	private void syncNow() {
		syncPending = false;
		if (level != null) {
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
		}
	}

	/**
	 * 向这个位置的魔力发射器输出魔力。魔力不足一份时不输出。这和原版相同。
	 * Sends mana to the Mana Spreader at this position. When the mana is less than one lot, the machine sends nothing. This is the same as in the original.
	 *
	 * @param lot 一份的魔力量 / the quantity of mana in one lot
	 */
	protected final void pushManaToSpreader(BlockPos pos, int lot) {
		if (mana < lot || !(level.getBlockEntity(pos) instanceof ManaCollector spreader)) {
			return;
		}
		int amount = Math.min(lot, spreader.getMaxMana() - spreader.getCurrentMana());
		if (amount > 0) {
			spreader.receiveMana(amount);
			receiveMana(-amount);
		}
	}

	/**
	 * 法杖界面上魔力条下面的一行说明。没有说明时返回 {@code null}。
	 * The line of detail below the mana bar on the wand display. Returns {@code null} when there is no detail.
	 */
	@Nullable
	public Component getHudDetail() {
		return null;
	}

	@Override
	public Level getManaReceiverLevel() {
		return level;
	}

	@Override
	public BlockPos getManaReceiverPos() {
		return worldPosition;
	}

	@Override
	public int getCurrentMana() {
		return mana;
	}

	@Override
	public boolean isFull() {
		return mana >= getMaxMana();
	}

	@Override
	public void receiveMana(int amount) {
		int newMana = (int) Math.clamp((long) mana + amount, 0L, getMaxMana());
		if (newMana != mana) {
			mana = newMana;
			markChanged();
		}
	}

	@Override
	public boolean canReceiveManaFromBursts() {
		return true;
	}

	@Override
	public boolean canAttachSpark(ItemStack stack) {
		return true;
	}

	@Override
	public int getAvailableSpaceForMana() {
		return Math.max(0, getMaxMana() - mana);
	}

	@Override
	public boolean areIncomingTransfersDone() {
		return false;
	}

	@Override
	public boolean onUsedByWand(@Nullable Player player, ItemStack stack, Direction side) {
		if (level != null && !level.isClientSide()) {
			syncNow();
		}
		return true;
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		tag.putInt(TAG_MANA, mana);
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		mana = Math.clamp(tag.getInt(TAG_MANA), 0, getMaxMana());
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
