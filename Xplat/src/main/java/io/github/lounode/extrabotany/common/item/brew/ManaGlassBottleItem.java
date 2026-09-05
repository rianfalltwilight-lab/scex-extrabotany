package io.github.lounode.extrabotany.common.item.brew;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

import vazkii.botania.api.mana.ManaReceiver;
import vazkii.botania.common.item.CustomCreativeTabContents;

import io.github.lounode.extrabotany.common.lib.ExtraBotanyTags;

public class ManaGlassBottleItem extends Item implements CustomCreativeTabContents {

	public static int LADLE_OUT_MANA = 25_000;

	public ManaGlassBottleItem(Properties properties) {
		super(properties);
	}

	@Override
	public void addToCreativeTab(Item item, CreativeModeTab.Output output) {
		ItemStack empty = new ItemStack(this);
		output.accept(empty);
		output.accept(ManaCocktailItem.getDefaultCocktail());
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		BlockPos pos = context.getClickedPos();
		Player player = context.getPlayer();
		Level level = context.getLevel();
		ItemStack stack = context.getItemInHand();

		BlockState state = level.getBlockState(pos);
		BlockEntity tile = level.getBlockEntity(pos);

		if (!state.is(ExtraBotanyTags.Blocks.MANA_POOLS)) {
			return InteractionResult.PASS;
		}

		if (!(tile instanceof ManaReceiver receiver)) {
			return InteractionResult.PASS;
		}

		if (receiver.getCurrentMana() < getLadleOutMana()) {
			return InteractionResult.PASS;
		}

		level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.BOTTLE_FILL, SoundSource.NEUTRAL, 1.0F, 1.0F);
		level.gameEvent(player, GameEvent.FLUID_PICKUP, pos);

		ItemStack defaultCocktail = ManaCocktailItem.getDefaultCocktail();
		var emptyResult = turnBottleIntoItem(stack, player, defaultCocktail);

		if (stack.isEmpty()) {
			if (!player.getInventory().add(emptyResult)) {
				player.drop(emptyResult, false);
			}
		}

		receiver.receiveMana(-LADLE_OUT_MANA);
		return InteractionResult.sidedSuccess(level.isClientSide());
	}

	protected ItemStack turnBottleIntoItem(ItemStack bottleStack, Player player, ItemStack filledBottleStack) {
		player.awardStat(Stats.ITEM_USED.get(this));
		return ItemUtils.createFilledResult(bottleStack, player, filledBottleStack);
	}

	public int getLadleOutMana() {
		return LADLE_OUT_MANA;
	}
}
