package io.github.lounode.extrabotany.common.item.relic.void_archives.variants;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.mixin.LivingEntityAccessor;
import vazkii.botania.xplat.XplatAbstractions;

import io.github.lounode.extrabotany.api.item.VoidArchivesVariant;

public class FruitOfGrisaia implements VoidArchivesVariant {

	public static FruitOfGrisaia INSTANCE = new FruitOfGrisaia();

	private static final String ID = "fruit_of_grisaia";

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		var relic = io.github.lounode.extrabotany.xplat.EXplatAbstractions.INSTANCE.findRelic(stack);
		if (player.canEat(true) && relic != null && relic.isRightPlayer(player)) {
			return ItemUtils.startUsingInstantly(level, player, hand);
		}
		return InteractionResultHolder.pass(stack);
	}

	@Override
	public void onUseTick(Level level, LivingEntity living, ItemStack stack, int remainingUseDuration) {
		if (!(living instanceof Player player)) {
			return;
		}
		if (ManaItemHandler.instance().requestManaExact(stack, player, 500, true)) {
			if (remainingUseDuration % 5 == 0) {
				player.gameEvent(GameEvent.EAT);
				player.getFoodData().eat(2, 2.4F);
			}

			if (remainingUseDuration == 5) {
				if (player.canEat(true)) {
					((LivingEntityAccessor) player).botania_setUseItemRemaining(20);
				}
			}
		}
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public int getUseDuration(ItemStack stack) {
		return 32;
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.EAT;
	}
}
