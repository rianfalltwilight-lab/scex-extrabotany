package io.github.lounode.extrabotany.common.block.flower.functional;


import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import io.github.lounode.extrabotany.common.block.flower.ExtraFunctionalFlowerBlockEntity;
import vazkii.botania.api.block_entity.RadiusDescriptor;

import io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks;
import io.github.lounode.extrabotany.common.block.ExtraBotanyBlocks;
import io.github.lounode.extrabotany.xplat.ExtraBotanyConfig;

import java.util.*;

/**
 * 蕴魔瑾。它积累大量魔力，然后把范围里的一个草方块变成蕴魔土。红石信号让它停止工作。
 * Enchanter. It collects a large quantity of mana, and then changes one grass block in the range into Enchanted Soil. A redstone signal stops it.
 *
 * <p>来源：重制版（Lounode）。它对应 ExtraMeteorP 1.12 的 {@code SubTileEnchantedOrchid}。
 * <p>Source: Reburn (Lounode). It corresponds to {@code SubTileEnchantedOrchid} of ExtraMeteorP 1.12.
 */
public class EnchanterBlockEntity extends ExtraFunctionalFlowerBlockEntity {

	private static final String TAG_CONSUMED = "consumedMana";

	public static final int RANGE = 3;

	public static final int CONSUME_SPEED = 400;
	public static final int TRANSFORM_COST = 250000;

	private int consumedMana;

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

		if (consumedMana < getTransformCost()) {
			int manaCanConsume = Math.min(getTransformCost() - consumedMana, Math.min(getConsumeSpeed(), getMana()));
			if (manaCanConsume > 0) {
				addMana(-manaCanConsume);
				setConsumedMana(getConsumedMana() + manaCanConsume);
				syncThrottled();
			}
			return;
		}

		if (ticksExisted % 20 != 0) {
			return;
		}

		BlockPos transformPos = getRandomPos();
		if (transformPos == null) {
			return;
		}

		if (!getLevel().setBlockAndUpdate(transformPos, ExtraBotanyBlocks.enchantedSoil.defaultBlockState())) {
			return;
		}

		setConsumedMana(0);
		sync();
	}

	@Nullable
	public BlockPos getRandomPos() {
		Map<BlockPos, Float> coordsMap = new HashMap<>();
		Random rng = new Random();
		BlockPos pos = getEffectivePos();

		for (BlockPos pos_ : BlockPos.betweenClosed(pos.offset(-RANGE, -RANGE, -RANGE), pos.offset(RANGE, RANGE, RANGE))) {
			BlockState state = getLevel().getBlockState(pos_);
			BlockState above = getLevel().getBlockState(pos_.above());
			if (!state.is(Blocks.GRASS_BLOCK) || !above.isAir()) {
				continue;
			}
			coordsMap.put(pos_.immutable(), 2.0f * rng.nextFloat());
		}

		if (coordsMap.isEmpty()) {
			return null;
		}

		float maxDistance = 0F;
		for (float distance : coordsMap.values()) {
			maxDistance = Math.max(maxDistance, distance);
		}

		float finalMaxDistance = maxDistance;
		coordsMap.values().removeIf(dist -> dist < finalMaxDistance - 1f);
		List<BlockPos> coords = new ArrayList<>(coordsMap.keySet());

		BlockPos breakCoords = coords.get(level.getRandom().nextInt(coords.size()));
		return breakCoords;
	}

	public int getTransformCost() {
		return ExtraBotanyConfig.common().enchanterTransformCost();
	}

	public int getConsumeSpeed() {
		return ExtraBotanyConfig.common().enchanterConsumeSpeed();
	}

	@Override
	public int getMaxMana() {
		return getConsumeSpeed() * 4;
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
	}

	@Override
	public void readFromPacketNBT(CompoundTag cmp) {
		super.readFromPacketNBT(cmp);
		setConsumedMana(cmp.getInt(TAG_CONSUMED));
	}
}
