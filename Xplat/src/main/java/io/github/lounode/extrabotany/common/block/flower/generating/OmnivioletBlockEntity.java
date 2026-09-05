package io.github.lounode.extrabotany.common.block.flower.generating;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import io.github.lounode.extrabotany.common.block.flower.ExtraGeneratingFlowerBlockEntity;
import vazkii.botania.api.block_entity.RadiusDescriptor;
import vazkii.botania.common.internal_caps.ItemLifetime;
import vazkii.botania.common.helper.EntityHelper;

import io.github.lounode.extrabotany.api.recipe.OmnivioletRecipe;
import io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks;
import io.github.lounode.extrabotany.common.crafting.ExtraBotanyRecipeTypes;
import io.github.lounode.extrabotany.xplat.EXplatAbstractions;
import io.github.lounode.extrabotany.xplat.ExtraBotanyConfig;

public class OmnivioletBlockEntity extends ExtraGeneratingFlowerBlockEntity {

	private static final String TAG_BURN_TIME = "burnTime";

	public static final int MAX_MANA = 1500;
	private static final int RANGE = 2;

	private static final int START_BURN_EVENT = 0;

	private int burnTime = 0;

	public OmnivioletBlockEntity(BlockPos pos, BlockState state) {
		super(ExtrabotanyFlowerBlocks.OMNIVIOLET, pos, state);
	}

	@Override
	public void tickFlower() {
		super.tickFlower();
		if (getBurnTime() > 0) {
			burnTick();
			setBurnTime(getBurnTime() - 1);
			return;
		}

		if (getLevel().isClientSide()) {
			return;
		}
		if (getMana() >= getMaxMana()) {
			return;
		}

		for (ItemEntity item : getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(
				Vec3.atLowerCornerOf(getEffectivePos().offset(-RANGE, -RANGE, -RANGE)),
				Vec3.atLowerCornerOf(getEffectivePos().offset(RANGE + 1, RANGE + 1, RANGE + 1))))) {
			if (ItemLifetime.canInteractWith(this, item)) {
				ItemStack stack = item.getItem();

				int addTime = getFeedBurnTime(stack);
				if (addTime <= 0 || stack.getCount() <= 0) {
					continue;
				}

				setBurnTime(addTime);

				EntityHelper.shrinkItem(item);
				getLevel().playSound(null, getEffectivePos(), SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1F, 1F);
				getLevel().blockEvent(getBlockPos(), getBlockState().getBlock(), START_BURN_EVENT, item.getId());
				getLevel().gameEvent(null, GameEvent.BLOCK_ACTIVATE, getBlockPos());
				sync();
				return;
			}
		}
	}

	public void burnTick() {
		if (getLevel().isClientSide()) {
			if (getLevel().random.nextInt(10) == 0) {
				emitParticle(ParticleTypes.FLAME, 0.4 + Math.random() * 0.2, 0.7, 0.4 + Math.random() * 0.2, 0.0D, 0.0D, 0.0D);
			}
		} else {
			float power = getEnchantPower();
			float buff = 1 + power * 0.05F;

			addMana((int) (8 * Math.min(7F, buff)));
			sync();
		}
	}

	@Override
	public boolean triggerEvent(int event, int param) {
		if (event == START_BURN_EVENT) {
			Entity e = getLevel().getEntity(param);
			if (e != null) {
				e.level().addParticle(ParticleTypes.LARGE_SMOKE, e.getX(), e.getY() + 0.1, e.getZ(), 0.0D, 0.0D, 0.0D);
				e.level().addParticle(ParticleTypes.FLAME, e.getX(), e.getY(), e.getZ(), 0.0D, 0.0D, 0.0D);
			}
			return true;
		} else {
			return super.triggerEvent(event, param);
		}
	}

	public float getEnchantPower() {
		return EXplatAbstractions.INSTANCE.getEnchantPowerBonus((ServerLevel) getLevel(), getEffectivePos());
	}

	@Override
	public int getMaxMana() {
		return ExtraBotanyConfig.common().omnivioletMaxMana();
	}

	@Override
	public int getColor() {
		return 0xEE82EE;
	}

	@Override
	public @Nullable RadiusDescriptor getRadius() {
		return RadiusDescriptor.Rectangle.square(getEffectivePos(), RANGE);
	}

	@Override
	public void writeToPacketNBT(CompoundTag cmp) {
		super.writeToPacketNBT(cmp);

		cmp.putInt(TAG_BURN_TIME, getBurnTime());
	}

	@Override
	public void readFromPacketNBT(CompoundTag cmp) {
		super.readFromPacketNBT(cmp);

		setBurnTime(cmp.getInt(TAG_BURN_TIME));
	}

	public int getBurnTime() {
		return burnTime;
	}

	public void setBurnTime(int burnTime) {
		this.burnTime = burnTime;
	}

	public int getFeedBurnTime(ItemStack stack) {
		for (Recipe<?> r : ExtraBotanyRecipeTypes.getRecipes(getLevel(), ExtraBotanyRecipeTypes.OMNIVIOLET_RECIPE_TYPE).values()) {
			if (!(r instanceof OmnivioletRecipe recipe)) {
				continue;
			}
			if (recipe.getInput().test(stack)) {
				return recipe.getBurnTime();
			}
		}
		return 0;
	}
}
