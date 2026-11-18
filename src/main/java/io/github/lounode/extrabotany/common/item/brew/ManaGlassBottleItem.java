package io.github.lounode.extrabotany.common.item.brew;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

import vazkii.botania.api.mana.ManaReceiver;

import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.lib.ExtraBotanyTags;

/**
 * 魔力玻璃瓶。对着魔力池使用时，它从魔力池里取走 25000 点魔力，变成一瓶魔力饮品。
 * Managlass Bottle. When it is used on a mana pool, it takes 25000 mana from the pool and becomes one Mana Drink.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code ItemEmptyBottle}（1.12 也是对魔力池使用得到魔力饮品）。魔力鸡尾酒由酿造瓶和魔力饮品合成。
 * <p>Source: ExtraMeteorP 1.16 {@code ItemEmptyBottle} (in 1.12 too, a use on a mana pool gives a Mana Drink). A Mana Cocktail is crafted from a brew flask and a Mana Drink.
 */
public class ManaGlassBottleItem extends Item {

	public static int LADLE_OUT_MANA = 25_000;

	public ManaGlassBottleItem(Properties properties) {
		super(properties);
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

		var emptyResult = turnBottleIntoItem(stack, player, new ItemStack(ExtraBotanyItems.manaDrink));

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
