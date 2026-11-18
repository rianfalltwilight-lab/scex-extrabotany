package io.github.lounode.extrabotany.common.block.flower.generating;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import io.github.lounode.extrabotany.common.block.flower.ExtraGeneratingFlowerBlockEntity;
import io.github.lounode.extrabotany.common.block.flower.FlowerBlockActions;
import io.github.lounode.extrabotany.common.block.flower.NatureCatalysis;
import vazkii.botania.api.block_entity.RadiusDescriptor;

import io.github.lounode.extrabotany.api.recipe.StonesiaRecipe;
import io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks;
import io.github.lounode.extrabotany.common.crafting.ExtraBotanyRecipeTypes;
import io.github.lounode.extrabotany.xplat.ExtraBotanyConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 石中姬。它吃掉身边同一层的石头和矿石，然后在一段时间里慢慢产生魔力。哪些方块能吃、一共产生多少魔力，由石中姬配方决定。红石信号让它停止工作。
 * Stonesia. It eats the stone and the ores beside it on its own layer, and then makes the mana slowly over some time. The Stonesia recipes tell which blocks it can eat and how much mana they give in total. A redstone signal stops it.
 *
 * <p>来源：ExtraMeteorP 1.12 的 {@code SubTileStonesia}。吃一块后每 tick 产生 {@value #MANA_PER_TICK} 魔力，直到这一块的魔力产完才吃下一块；两次吃之间至少 {@value #COOLDOWN} tick；上限 {@value #MAX_MANA}。有 1.12 的自然宝珠催化（见 {@link NatureCatalysis}）时每 tick 多 2。
 * <p>Source: {@code SubTileStonesia} of ExtraMeteorP 1.12. After it eats a block, it makes {@value #MANA_PER_TICK} mana on each tick until the mana of that block is all made, and only then eats the next block; at least {@value #COOLDOWN} ticks pass between two meals; the maximum is {@value #MAX_MANA}. With the Nature Orb catalysis of 1.12 (see {@link NatureCatalysis}) it makes 2 more each tick.
 */
public class StonesiaBlockEntity extends ExtraGeneratingFlowerBlockEntity {

	private static final String TAG_COOLDOWN = "cooldown";
	private static final String TAG_BURN_MANA = "burnMana";

	private static final int RANGE = 1;
	private static final BlockPos[] OFFSETS = {
			new BlockPos(0, 0, 1), new BlockPos(0, 0, -1), new BlockPos(1, 0, 0), new BlockPos(-1, 0, 0),
			new BlockPos(-1, 0, 1), new BlockPos(-1, 0, -1), new BlockPos(1, 0, 1), new BlockPos(1, 0, -1)
	};

	public static final int MAX_MANA = 800;
	public static final int COOLDOWN = 40;
	public static final int MANA_PER_TICK = 5;
	/** 1.12 的催化：每 tick 多 2，每 20 tick 扣宝珠 25 能量。 / The catalysis of 1.12: 2 more on each tick, 25 orb energy each 20 ticks. */
	public static final int CATALYSIS_BONUS = 2;
	public static final int CATALYSIS_RATE = 25;
	private boolean catalysed;

	private int cooldown;
	/** 吃下的方块还没有产出的魔力。 / The mana of the eaten block that is not made yet. */
	private int burnMana;
	@Nullable private UUID ownerUUID;

	public StonesiaBlockEntity(BlockPos pos, BlockState state) {
		super(ExtrabotanyFlowerBlocks.STONESIA, pos, state);
	}

	@Override
	public void tickFlower() {
		super.tickFlower();
		if (getLevel().isClientSide() || isPowered()) {
			return;
		}

		if (getCooldown() > 0) {
			setCooldown(getCooldown() - 1);
		}
		catalysed = NatureCatalysis.update(this, ticksExisted, CATALYSIS_RATE, catalysed);

		if (burnMana > 0) {
			int mana = Math.min(MANA_PER_TICK, burnMana);
			burnMana -= mana;
			addMana(mana + (catalysed ? CATALYSIS_BONUS : 0));
			syncThrottled();
			return;
		}

		if (getMana() >= getMaxMana()) {
			return;
		}

		List<BlockPos> offsets = new ArrayList<>(Arrays.asList(OFFSETS));
		Util.shuffle(offsets, getLevel().getRandom());
		for (BlockPos offset : offsets) {
			BlockPos eatPos = getEffectivePos().offset(offset);
			if (!getLevel().isLoaded(eatPos)) {
				continue;
			}
			int mana = getManaFormBlock(getLevel().getBlockState(eatPos));
			if (mana <= 0) {
				continue;
			}
			// 1.12 找到第一块能吃的方块就停下；冷却没结束时就等着。
			// 1.12 stops at the first block that it can eat; while the cooldown runs, it waits.
			if (getCooldown() == 0 && FlowerBlockActions.breakBlock((ServerLevel) getLevel(), eatPos, ownerUUID, false)) {
				burnMana = mana;
				setCooldown(getAfterWorkCooldown());
				syncThrottled();
				getLevel().playSound(null, getEffectivePos(), SoundEvents.GENERIC_DRINK, SoundSource.BLOCKS, 0.01F, 0.5F + (float) Math.random() * 0.5F);
			}
			break;
		}
	}

	public boolean isCatalysed() {
		return catalysed;
	}

	public int getBurnMana() {
		return burnMana;
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

	public int getAfterWorkCooldown() {
		return ExtraBotanyConfig.common().stonesiaCooldown();
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
		super.setPlacedBy(level, pos, state, placer, stack);
		ownerUUID = placer instanceof Player player ? player.getUUID() : null;
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
		burnMana = cmp.getInt(TAG_BURN_MANA);
		ownerUUID = cmp.hasUUID("ownerUUID") ? cmp.getUUID("ownerUUID") : null;
	}

	@Override
	public void writeToPacketNBT(CompoundTag cmp) {
		super.writeToPacketNBT(cmp);

		cmp.putInt(TAG_COOLDOWN, getCooldown());
		cmp.putInt(TAG_BURN_MANA, burnMana);
		if (ownerUUID != null) {
			cmp.putUUID("ownerUUID", ownerUUID);
		}
	}
}
