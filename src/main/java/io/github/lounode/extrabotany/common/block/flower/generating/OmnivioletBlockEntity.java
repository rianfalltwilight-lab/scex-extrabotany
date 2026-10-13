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

/**
 * 全知瑾。它烧掉丢给它的书和成书来产生魔力。哪些物品能烧、能烧多久，由全知瑾配方决定。周围的书架让它每 tick 产生更多魔力，规则和附魔台相同。
 * Omniviolet. It burns the books and written books that are thrown to it to make mana. The Omniviolet recipes tell which items it can burn and for how long. Bookshelves around it give more mana on each tick, with the same rule as the enchanting table.
 *
 * <p>来源：重制版（Lounode）。它对应 ExtraMeteorP 1.12 的 {@code SubTileOmniViolet}。
 * <p>Source: Reburn (Lounode). It corresponds to {@code SubTileOmniViolet} of ExtraMeteorP 1.12.
 */
public class OmnivioletBlockEntity extends ExtraGeneratingFlowerBlockEntity {

	private static final String TAG_BURN_TIME = "burnTime";

	public static final int MAX_MANA = 1500;
	private static final int RANGE = 2;

	private static final int START_BURN_EVENT = 0;

	private static final int ENCHANT_POWER_INTERVAL = 20;

	private int burnTime = 0;
	private float enchantPower;
	private int enchantPowerAge = ENCHANT_POWER_INTERVAL;

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
		if (isIdleBackoff()) {
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
		// No fuel found: skip the item AABB scan + recipe walk for a few ticks.
		backoffIdle(IDLE_BACKOFF_TICKS);
	}

	public void burnTick() {
		if (getLevel().isClientSide()) {
			if (getLevel().random.nextInt(10) == 0) {
				emitParticle(ParticleTypes.FLAME, 0.4 + Math.random() * 0.2, 0.7, 0.4 + Math.random() * 0.2, 0.0D, 0.0D, 0.0D);
			}
		} else {
			// 附魔能量的查找要读一百个左右的方块，所以每 20 tick 才查找一次。
			// The search for the enchant power reads approximately one hundred blocks, thus it only runs one time for each 20 ticks.
			if (++enchantPowerAge >= ENCHANT_POWER_INTERVAL) {
				enchantPowerAge = 0;
				enchantPower = getEnchantPower();
			}
			float buff = 1 + enchantPower * 0.05F;

			addMana((int) (8 * Math.min(7F, buff)));
			syncThrottled();
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
		for (var holder : ExtraBotanyRecipeTypes.getRecipeHolders(getLevel(), ExtraBotanyRecipeTypes.OMNIVIOLET_RECIPE_TYPE)) {
			OmnivioletRecipe recipe = holder.value();
			if (recipe.getInput().test(stack)) {
				return recipe.getBurnTime();
			}
		}
		return 0;
	}
}
