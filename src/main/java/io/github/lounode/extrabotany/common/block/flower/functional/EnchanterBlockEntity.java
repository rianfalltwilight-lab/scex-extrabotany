package io.github.lounode.extrabotany.common.block.flower.functional;


import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import org.jetbrains.annotations.Nullable;

import io.github.lounode.extrabotany.common.block.flower.ExtraFunctionalFlowerBlockEntity;
import io.github.lounode.extrabotany.common.block.flower.FlowerBlockActions;
import vazkii.botania.api.block_entity.RadiusDescriptor;
import vazkii.botania.common.helper.EntityHelper;
import vazkii.botania.common.internal_caps.ItemLifetime;
import vazkii.botania.common.item.BotaniaItems;

import io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks;
import io.github.lounode.extrabotany.common.block.ExtraBotanyBlocks;
import io.github.lounode.extrabotany.xplat.ExtraBotanyConfig;

import java.util.UUID;

/**
 * 蕴魔瑾。丢给它一朵黑莲花后，它积累大量魔力，然后把下面一层的一个草方块变成蕴魔土。红石信号让它停止工作。
 * Enchanter. After a Black Lotus is thrown to it, it collects a large quantity of mana, and then changes one grass block on the layer below into Enchanted Soil. A redstone signal stops it.
 *
 * <p>来源：ExtraMeteorP 1.12 的 {@code SubTileEnchantedOrchid}：每块蕴魔土要一朵黑莲花和 200000 魔力，每 tick 消耗 400；草方块在花下面一层、水平 4 格内；花的上限 10000。
 * <p>Source: {@code SubTileEnchantedOrchid} of ExtraMeteorP 1.12: each Enchanted Soil needs one Black Lotus and 200000 mana, 400 is used on each tick; the grass block is on the layer below the flower, 4 blocks or less away horizontally; the flower holds at most 10000.
 */
public class EnchanterBlockEntity extends ExtraFunctionalFlowerBlockEntity {

	private static final String TAG_CONSUMED = "consumedMana";
	private static final String TAG_HAS_LOTUS = "hasLotus";

	public static final int RANGE = 4;
	private static final int LOTUS_RANGE = 1;

	public static final int CONSUME_SPEED = 400;
	public static final int TRANSFORM_COST = 200000;
	public static final int MAX_MANA = 10000;

	private int consumedMana;
	private boolean hasLotus;
	@Nullable private UUID ownerUUID;

	public EnchanterBlockEntity(BlockEntityType<? extends EnchanterBlockEntity> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public EnchanterBlockEntity(BlockPos pos, BlockState state) {
		super(ExtrabotanyFlowerBlocks.ENCHANTER, pos, state);
	}

	@Override
	public void tickFlower() {
		super.tickFlower();
		if (getLevel().isClientSide() || isPowered()) {
			return;
		}

		if (!hasLotus && !isIdleBackoff()) {
			if (tryEatLotus()) {
				hasLotus = true;
				sync();
			} else {
				backoffIdle(IDLE_BACKOFF_TICKS);
			}
		}
		if (!hasLotus) {
			return;
		}

		if (consumedMana < getTransformCost()) {
			int manaCanConsume = Math.min(getTransformCost() - consumedMana, Math.min(getConsumeSpeed(), getMana()));
			if (manaCanConsume > 0) {
				addMana(-manaCanConsume);
				setConsumedMana(getConsumedMana() + manaCanConsume);
				syncThrottled();
			}
			return;
		}

		// 领地插件可能拒绝放置；每秒试一次，不要每 tick 都发放置事件。
		// A claim plugin can refuse the placement; try once per second, not a place event on each tick.
		if (ticksExisted % 20 != 0) {
			return;
		}
		BlockPos transformPos = findGrass();
		if (transformPos == null) {
			return;
		}
		if (!FlowerBlockActions.placeBlock((ServerLevel) getLevel(), transformPos,
				ExtraBotanyBlocks.enchantedSoil.defaultBlockState(), ownerUUID)) {
			return;
		}
		setConsumedMana(0);
		hasLotus = false;
		sync();
	}

	private boolean tryEatLotus() {
		BlockPos pos = getEffectivePos();
		for (ItemEntity item : getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(LOTUS_RANGE))) {
			if (ItemLifetime.canInteractWith(this, item) && item.getItem().is(BotaniaItems.BLACK_LOTUS)) {
				EntityHelper.shrinkItem(item);
				return true;
			}
		}
		return false;
	}

	/**
	 * 花下面一层、水平 {@value #RANGE} 格内的第一个草方块。
	 * The first grass block on the layer below the flower, {@value #RANGE} blocks or less away horizontally.
	 */
	@Nullable
	public BlockPos findGrass() {
		BlockPos center = getEffectivePos().below();
		for (BlockPos pos : BlockPos.betweenClosed(center.offset(-RANGE, 0, -RANGE), center.offset(RANGE, 0, RANGE))) {
			if (getLevel().isLoaded(pos) && getLevel().getBlockState(pos).is(Blocks.GRASS_BLOCK)) {
				return pos.immutable();
			}
		}
		return null;
	}

	public boolean hasLotus() {
		return hasLotus;
	}

	public void setHasLotus(boolean hasLotus) {
		this.hasLotus = hasLotus;
	}

	public int getTransformCost() {
		return ExtraBotanyConfig.common().enchanterTransformCost();
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
		super.setPlacedBy(level, pos, state, placer, stack);
		ownerUUID = placer instanceof Player player ? player.getUUID() : null;
	}

	public int getConsumeSpeed() {
		return ExtraBotanyConfig.common().enchanterConsumeSpeed();
	}

	@Override
	public int getMaxMana() {
		return MAX_MANA;
	}

	@Override
	public int getColor() {
		return 0x4B0082;
	}

	@Override
	public @Nullable RadiusDescriptor getRadius() {
		return RadiusDescriptor.Rectangle.square(getEffectivePos(), RANGE);
	}

	public int getConsumedMana() {
		return consumedMana;
	}

	public void setConsumedMana(int consumedMana) {
		this.consumedMana = consumedMana;
	}

	@Override
	public void writeToPacketNBT(CompoundTag cmp) {
		super.writeToPacketNBT(cmp);
		cmp.putInt(TAG_CONSUMED, getConsumedMana());
		cmp.putBoolean(TAG_HAS_LOTUS, hasLotus);
		if (ownerUUID != null) {
			cmp.putUUID("ownerUUID", ownerUUID);
		}
	}

	@Override
	public void readFromPacketNBT(CompoundTag cmp) {
		super.readFromPacketNBT(cmp);
		setConsumedMana(cmp.getInt(TAG_CONSUMED));
		hasLotus = cmp.getBoolean(TAG_HAS_LOTUS);
		ownerUUID = cmp.hasUUID("ownerUUID") ? cmp.getUUID("ownerUUID") : null;
	}
}
