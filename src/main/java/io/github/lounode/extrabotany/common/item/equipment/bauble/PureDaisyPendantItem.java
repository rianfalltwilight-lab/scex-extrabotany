package io.github.lounode.extrabotany.common.item.equipment.bauble;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.api.recipe.PureDaisyRecipe;
import vazkii.botania.common.crafting.BotaniaRecipeTypes;
import vazkii.botania.common.item.equipment.bauble.BaubleItem;

import io.github.lounode.extrabotany.common.crafting.ExtraBotanyRecipeTypes;
import io.github.lounode.extrabotany.common.helper.ItemNBTHelper;
import io.github.lounode.extrabotany.common.util.PlayerUtil;

/**
 * 白雏菊项链。拿在手里对一个方块使用时，按白雏菊的配方立刻转化这个方块，消耗 {@value #TRANSFORM_MANA} 魔力。每用 {@value #USES_BEFORE_COOLDOWN} 次进入 {@value #COOLDOWN} tick 的冷却；冷却只在佩戴时减少。佩戴时，白天每 6 秒回复 1 点生命。
 * Pure Daisy Pendant. When it is used on a block from the hand, the block is converted immediately by the Pure Daisy recipes, for {@value #TRANSFORM_MANA} mana. Each {@value #USES_BEFORE_COOLDOWN} uses start a cooldown of {@value #COOLDOWN} ticks; the cooldown only goes down while it is worn. When worn, it heals 1 each 6 seconds in the day.
 *
 * <p>来源：ExtraMeteorP 1.12 的 {@code ItemPureDaisyPendant}（这件只有 1.12 有）。1.12 先转化再扣魔力，魔力不够也能转化；这里先扣魔力。重燃的"佩戴时空手右键"已经去掉。
 * <p>Source: {@code ItemPureDaisyPendant} of ExtraMeteorP 1.12 (only 1.12 has it). 1.12 converted first and took the mana afterwards, so it converted even without mana; here the mana comes first. The "wear it and use with an empty hand" of Reburn is removed.
 */
public class PureDaisyPendantItem extends BaubleItem {
	public static final String TAG_USE = "usecount";
	public static final String TAG_CD = "cooldown";
	public static final int TRANSFORM_MANA = 50;
	public static final int USES_BEFORE_COOLDOWN = 64;
	public static final int COOLDOWN = 3000;
	public static final int HEAL_INTERVAL = 120;

	public PureDaisyPendantItem(Properties props) {
		super(props);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		BlockState state = level.getBlockState(pos);
		if (state.isAir()) {
			return InteractionResult.PASS;
		}
		if (level.isClientSide() || !(context.getPlayer() instanceof Player player)) {
			return InteractionResult.SUCCESS;
		}
		ItemStack stack = context.getItemInHand();
		BlockState output = getDaisyRecipe(level, state);
		if (output == null || output.is(state.getBlock()) || getCooldown(stack) > 0) {
			return InteractionResult.SUCCESS;
		}
		if (!ManaItemHandler.instance().requestManaExactForTool(stack, player, TRANSFORM_MANA, true)) {
			return InteractionResult.SUCCESS;
		}
		if (PlayerUtil.tryBreakBlock(player, ItemStack.EMPTY, level, pos)) {
			level.setBlockAndUpdate(pos, output.getBlock().withPropertiesOf(state));
			level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(output));
			level.gameEvent(null, GameEvent.BLOCK_CHANGE, pos);
			int count = getUseCount(stack) + 1;
			ItemNBTHelper.setInt(stack, TAG_USE, count);
			if (count % USES_BEFORE_COOLDOWN == 0) {
				ItemNBTHelper.setInt(stack, TAG_CD, COOLDOWN);
			}
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public void onWornTick(ItemStack stack, LivingEntity entity) {
		super.onWornTick(stack, entity);
		Level level = entity.level();
		if (level.isClientSide()) {
			return;
		}
		int cooldown = getCooldown(stack);
		if (cooldown > 0) {
			if (cooldown == 1) {
				ItemNBTHelper.removeEntry(stack, TAG_CD);
			} else {
				ItemNBTHelper.setInt(stack, TAG_CD, cooldown - 1);
			}
		}
		if (level.isDay() && level.getGameTime() % HEAL_INTERVAL == 0 && entity instanceof Player) {
			entity.heal(1F);
		}
	}

	public static int getCooldown(ItemStack stack) {
		return ItemNBTHelper.getInt(stack, TAG_CD, 0);
	}

	public static int getUseCount(ItemStack stack) {
		return ItemNBTHelper.getInt(stack, TAG_USE, 0);
	}

	@Nullable
	private static BlockState getDaisyRecipe(Level level, BlockState input) {
		for (var holder : ExtraBotanyRecipeTypes.getRecipeHolders(level, BotaniaRecipeTypes.PURE_DAISY_TYPE)) {
			PureDaisyRecipe recipe = holder.value();
			if (recipe.getInput().test(input)) {
				return recipe.getOutput().pick(level.getRandom());
			}
		}
		return null;
	}
}
