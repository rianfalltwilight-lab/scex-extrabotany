package io.github.lounode.extrabotany.common.block.block_entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.EventHooks;

import io.github.lounode.extrabotany.common.data.ExtraBotanyDataMaps;

/**
 * 不随想之茧的方块实体。茧里放着一个物品，{@value #HATCH_TIME} tick 之后孵化出对应的生物。
 * The block entity of the Cocoon of Desire. The cocoon holds one item, and after {@value #HATCH_TIME} ticks the related creature hatches.
 *
 * <p>玩家可以右键放入物品，也可以把物品丢在茧旁边。丢在旁边时，茧只吸收孵化表里有的物品。
 * <p>A player can use an item on the cocoon to put it in, or can drop the item adjacent to the cocoon. For dropped items, the cocoon only takes items that are in the hatch table.
 *
 * <p>孵化表是数据映射 {@link ExtraBotanyDataMaps#COCOON_HATCH}。能长大的生物孵化出来时是幼年。茧里的物品不在表里时，什么也不孵化，计时重新开始。
 * <p>The hatch table is the data map {@link ExtraBotanyDataMaps#COCOON_HATCH}. A creature that can grow hatches as a baby. When the item in the cocoon is not in the table, nothing hatches and the timer starts again.
 *
 * <p>数据存在 {@code Item} 和 {@code timePassed} 键里。旧存档使用相同的键名。
 * <p>The data is in the keys {@code Item} and {@code timePassed}. Old saves use the same keys.
 *
 * <p>来源：ExtraMeteorP 1.12 {@code TileCocoonDesire}。
 * <p>Source: ExtraMeteorP 1.12 {@code TileCocoonDesire}.
 *
 * <p>和原版的差异：
 * <p>Differences from the original:
 * <ul>
 * <li>孵化进度会保存。原版在区块重新加载后从零开始。
 * The hatch progress is saved. The original starts from zero after the chunk loads again.</li>
 * <li>取出或放入物品时，计时归零。原版不归零，所以下一个物品会提前孵化。
 * The timer goes to zero when an item is taken out or put in. The original does not reset it, thus the next item hatches too soon.</li>
 * <li>茧每次只吸收一个掉落物。原版会一次吃掉旁边每一个符合的掉落物，但是只保留最后一个。
 * The cocoon takes only one dropped item at a time. The original eats one item from each applicable stack adjacent to it at the same time, but only keeps the last one.</li>
 * <li>孵化表可以用数据包修改。原版把它写在代码里。
 * A data pack can change the hatch table. The original has the table in the code.</li>
 * </ul>
 *
 * <p>性能：茧每 {@value #PICKUP_INTERVAL} tick 查找一次掉落物，孵化期间每 {@value #SAVE_INTERVAL} tick 把区块标记为需要保存。只有放入和取出物品时才发同步数据包，客户端自己计时。
 * <p>Performance: the cocoon looks for dropped items one time for each {@value #PICKUP_INTERVAL} ticks, and during the incubation it marks the chunk for saving one time for each {@value #SAVE_INTERVAL} ticks. A synchronization packet is only sent when an item is put in or taken out, and the client counts the time itself.
 */
public class CocoonOfDesireBlockEntity extends BlockEntity {
	public static final int HATCH_TIME = 1200;

	private static final String TAG_ITEM = "Item";
	private static final String TAG_TIME_PASSED = "timePassed";
	private static final int PICKUP_INTERVAL = 10;
	private static final int SAVE_INTERVAL = 20;
	private static final int BABY_AGE = -24000;

	private ItemStack item = ItemStack.EMPTY;
	private int timePassed;

	public CocoonOfDesireBlockEntity(BlockPos pos, BlockState state) {
		super(ExtraBotanyBlockEntities.COCOON_OF_DESIRE, pos, state);
	}

	public ItemStack getItem() {
		return item;
	}

	/**
	 * 孵化已经进行的 tick 数。茧是空的时候是 0。
	 * The number of ticks that the incubation has run. It is 0 when the cocoon is empty.
	 */
	public int getTimePassed() {
		return timePassed;
	}

	/**
	 * 放入一个物品，并重新开始计时。传入空物品就是清空茧。
	 * Puts in one item and starts the timer again. An empty stack clears the cocoon.
	 */
	public void setItem(ItemStack stack) {
		item = stack.copyWithCount(1);
		restartTimer();
	}

	private void restartTimer() {
		timePassed = 0;
		setChanged();
		if (level != null) {
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
		}
	}

	public void playPickupSound(SoundSource source) {
		level.playSound(null, worldPosition, SoundEvents.ITEM_PICKUP, source, 0.5F,
				(level.random.nextFloat() - level.random.nextFloat()) * 0.2F + 1F);
	}

	public void clientTick() {
		if (!item.isEmpty() && timePassed < HATCH_TIME) {
			timePassed++;
		}
	}

	public void serverTick() {
		if (!(level instanceof ServerLevel serverLevel)) {
			return;
		}
		if (item.isEmpty()) {
			if (Math.floorMod(serverLevel.getGameTime() + worldPosition.asLong(), PICKUP_INTERVAL) == 0) {
				takeDroppedItem(serverLevel);
			}
			return;
		}
		timePassed++;
		if (timePassed >= HATCH_TIME) {
			hatch(serverLevel);
		} else if (timePassed % SAVE_INTERVAL == 0) {
			serverLevel.blockEntityChanged(worldPosition);
		}
	}

	private void takeDroppedItem(ServerLevel level) {
		// 和原版相同：范围是方块中心周围 1 格。
		// The same as in the original: the range is 1 block around the center of the block.
		AABB range = new AABB(worldPosition).inflate(0.5);
		for (ItemEntity dropped : level.getEntitiesOfClass(ItemEntity.class, range,
				entity -> entity.isAlive() && entity.getItem().getItemHolder().getData(ExtraBotanyDataMaps.COCOON_HATCH) != null)) {
			ItemStack remaining = dropped.getItem().copy();
			setItem(remaining.split(1));
			if (remaining.isEmpty()) {
				dropped.discard();
			} else {
				dropped.setItem(remaining);
			}
			playPickupSound(SoundSource.BLOCKS);
			return;
		}
	}

	private void hatch(ServerLevel level) {
		ExtraBotanyDataMaps.CocoonHatch hatch = item.getItemHolder().getData(ExtraBotanyDataMaps.COCOON_HATCH);
		Entity born = hatch == null ? null : hatch.entity().create(level);
		if (born == null) {
			restartTimer();
			return;
		}
		born.moveTo(worldPosition.getX() + 0.5, worldPosition.getY() + 1, worldPosition.getZ() + 0.5, level.random.nextFloat() * 360F, 0F);
		if (born instanceof AgeableMob ageable) {
			ageable.setAge(BABY_AGE);
		}
		if (born instanceof Mob mob) {
			EventHooks.finalizeMobSpawn(mob, level, level.getCurrentDifficultyAt(worldPosition), MobSpawnType.MOB_SUMMONED, null);
		}
		if (!level.addFreshEntity(born)) {
			restartTimer();
			return;
		}
		if (born instanceof Mob mob) {
			mob.spawnAnim();
		}
		setItem(ItemStack.EMPTY);
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		tag.putInt(TAG_TIME_PASSED, timePassed);
		if (!item.isEmpty()) {
			tag.put(TAG_ITEM, item.save(registries));
		}
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		item = ItemStack.parseOptional(registries, tag.getCompound(TAG_ITEM));
		timePassed = item.isEmpty() ? 0 : Math.clamp(tag.getInt(TAG_TIME_PASSED), 0, HATCH_TIME);
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
