package io.github.lounode.extrabotany.common.block.flower.generating;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import io.github.lounode.extrabotany.common.block.flower.ExtraGeneratingFlowerBlockEntity;
import vazkii.botania.api.block_entity.RadiusDescriptor;

import io.github.lounode.extrabotany.api.recipe.StonesiaRecipe;
import io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks;
import io.github.lounode.extrabotany.common.crafting.ExtraBotanyRecipeTypes;
import io.github.lounode.extrabotany.xplat.ExtraBotanyConfig;

/**
 * 石中姬。它吃掉周围的石头和矿石来产生魔力。哪些方块能吃、能产生多少魔力，由石中姬配方决定。
 * Stonesia. It eats the stone and the ores around it to make mana. The Stonesia recipes tell which blocks it can eat and how much mana they give.
 *
 * <p>来源：重制版（Lounode）。它对应 ExtraMeteorP 1.12 的 {@code SubTileStonesia}。
 * <p>Source: Reburn (Lounode). It corresponds to {@code SubTileStonesia} of ExtraMeteorP 1.12.
 */
public class StonesiaBlockEntity extends ExtraGeneratingFlowerBlockEntity {

	private static final String TAG_COOLDOWN = "cooldown";

	private static final int RANGE = 1;

	public static final int MAX_MANA = 3200;
	public static final int COOLDOWN = 40;

	private int cooldown;

	public StonesiaBlockEntity(BlockPos pos, BlockState state) {
		super(ExtrabotanyFlowerBlocks.STONESIA, pos, state);
	}

	@Override
	public void tickFlower() {
		super.tickFlower();
		if (getLevel().isClientSide()) {
			return;
		}
		if (getCooldown() > 0) {
			setCooldown(getCooldown() - 1);
			return;
		}
		if (getMana() >= getMaxMana()) {
			return;
		}

		BlockPos eatPos = getRadomBlockPos();

		int mana = getManaFormBlock(getLevel().getBlockState(eatPos));
		if (mana > 0) {
			setCooldown(getAfterWorkCooldown());
			getLevel().destroyBlock(eatPos, false);
			addMana(mana);
			syncThrottled();
			getLevel().playSound(null, getEffectivePos(), SoundEvents.GENERIC_DRINK, SoundSource.BLOCKS, 0.01F, 0.5F + (float) Math.random() * 0.5F);
		}
	}

	/**
	 * 返回这个方块能产生的魔力。没有石中姬配方接受这个方块时返回 0。排在前面的配方优先。
	 * Returns the mana that this block gives. Returns 0 when no Stonesia recipe accepts the block. The recipe that comes first has priority.
	 *
	 * <p>这里不缓存结果。配方管理器每次都返回一个新的列表，所以不能用列表来判断配方有没有变化；配方只有几十个，逐个判断很快。
	 * <p>The result is not cached here. The recipe manager returns a new list each time, thus the list cannot tell if the recipes changed; there are only some tens of recipes, and it is fast to examine them one by one.
	 */
	public int getManaFormBlock(BlockState state) {
		for (var holder : ExtraBotanyRecipeTypes.getRecipeHolders(getLevel(), ExtraBotanyRecipeTypes.STONESIA_RECIPE_TYPE)) {
			StonesiaRecipe recipe = holder.value();
			if (recipe.getInput().test(state)) {
				return recipe.getManaOutput();
			}
		}
		return 0;
	}

	/**
	 * 在花的周围随机选一个位置：水平方向 1 格之内，和花同一层或者高一层，不包括花自己的位置。
	 * Selects a position around the flower at random: 1 block or less horizontally, on the layer of the flower or one layer above, but not the position of the flower itself.
	 */
	public BlockPos getRadomBlockPos() {
		BlockPos center = getEffectivePos();
		RandomSource random = getLevel().getRandom();
		BlockPos result;
		do {
			int x = center.getX() - RANGE + random.nextInt(2 * RANGE + 1);
			int y = center.getY() + random.nextInt(RANGE + 1);
			int z = center.getZ() - RANGE + random.nextInt(2 * RANGE + 1);
			result = new BlockPos(x, y, z);
		} while (result.equals(center));
		return result;
	}

	public int getAfterWorkCooldown() {
		return ExtraBotanyConfig.common().stonesiaCooldown();
	}

	@Override
	public int getMaxMana() {
		return ExtraBotanyConfig.common().stonesiaMaxMana();
	}

	public int getCooldown() {
		return cooldown;
	}

	public void setCooldown(int cooldown) {
		this.cooldown = cooldown;
	}

	@Override
	public int getColor() {
		return 0x778899;
	}

	@Override
	public @Nullable RadiusDescriptor getRadius() {
		return RadiusDescriptor.Rectangle.square(getEffectivePos(), RANGE);
	}

	@Override
	public void readFromPacketNBT(CompoundTag cmp) {
		super.readFromPacketNBT(cmp);

		setCooldown(cmp.getInt(TAG_COOLDOWN));
	}

	@Override
	public void writeToPacketNBT(CompoundTag cmp) {
		super.writeToPacketNBT(cmp);

		cmp.putInt(TAG_COOLDOWN, getCooldown());
	}
}
