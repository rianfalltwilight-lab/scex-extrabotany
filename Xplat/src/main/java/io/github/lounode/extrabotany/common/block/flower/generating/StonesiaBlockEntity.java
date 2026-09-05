package io.github.lounode.extrabotany.common.block.flower.generating;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import io.github.lounode.extrabotany.common.block.flower.ExtraGeneratingFlowerBlockEntity;
import vazkii.botania.api.block_entity.RadiusDescriptor;

import io.github.lounode.extrabotany.api.recipe.StonesiaRecipe;
import io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks;
import io.github.lounode.extrabotany.common.crafting.ExtraBotanyRecipeTypes;
import io.github.lounode.extrabotany.xplat.ExtraBotanyConfig;

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
			sync();
			getLevel().playSound(null, getEffectivePos(), SoundEvents.GENERIC_DRINK, SoundSource.BLOCKS, 0.01F, 0.5F + (float) Math.random() * 0.5F);
		}
	}

	public int getManaFormBlock(BlockState state) {
		for (Recipe<?> r : ExtraBotanyRecipeTypes.getRecipes(getLevel(), ExtraBotanyRecipeTypes.STONESIA_RECIPE_TYPE).values()) {
			if (!(r instanceof StonesiaRecipe recipe)) {
				continue;
			}
			if (recipe.getInput().test(state)) {
				return recipe.getManaOutput();
			}
		}
		return 0;
	}

	public BlockPos getRadomBlockPos() {
		BlockPos result = null;
		while (result == null || result.equals(getEffectivePos())) {
			result = BlockPos.randomBetweenClosed(getLevel().getRandom(), 1,
					getEffectivePos().getX() - RANGE, getEffectivePos().getY(), getEffectivePos().getZ() - RANGE,
					getEffectivePos().getX() + RANGE, getEffectivePos().getY() + RANGE, getEffectivePos().getZ() + RANGE
			).iterator().next();
		}
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
