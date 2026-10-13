package io.github.lounode.extrabotany.common.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import vazkii.botania.api.mana.ManaItemHandler;

import java.util.List;

/**
 * 魔力饮品。喝下后回复 5 点生命值，获得 60 秒的四种增益，并向玩家的魔力物品充入 10000 点魔力。
 * Mana Drink. The drink gives back 5 health, gives four beneficial effects for 60 seconds, and puts 10000 mana into the mana items of the player.
 *
 * <p>喝完后返还一个魔力玻璃瓶。
 * <p>The player gets a Managlass Bottle back after the drink.
 *
 * <p>来源：ExtraMeteorP 1.12 {@code ItemManaDrink}。
 * <p>Source: ExtraMeteorP 1.12 {@code ItemManaDrink}.
 */
public class ManaDrinkItem extends Item {
	private static final float HEAL_AMOUNT = 5F;
	private static final int MANA_GIVEN = 10000;
	private static final int EFFECT_DURATION_TICKS = 1200;
	private static final int DRINK_DURATION_TICKS = 32;
	private static final List<Holder<MobEffect>> EFFECTS = List.of(
			MobEffects.DAMAGE_RESISTANCE, MobEffects.DAMAGE_BOOST, MobEffects.MOVEMENT_SPEED, MobEffects.JUMP);

	public ManaDrinkItem(Properties props) {
		super(props);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		return ItemUtils.startUsingInstantly(level, player, hand);
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
		if (entity instanceof ServerPlayer serverPlayer) {
			CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, stack);
			serverPlayer.awardStat(Stats.ITEM_USED.get(this));
		}
		if (level.isClientSide() || !(entity instanceof Player player)) {
			return stack;
		}

		player.getFoodData().eat(1, 0.1F);
		player.heal(HEAL_AMOUNT);
		for (Holder<MobEffect> effect : EFFECTS) {
			player.addEffect(new MobEffectInstance(effect, EFFECT_DURATION_TICKS, 0));
		}
		ManaItemHandler.instance().dispatchManaExact(stack, player, MANA_GIVEN, true);

		if (player.hasInfiniteMaterials()) {
			return stack;
		}
		stack.shrink(1);
		ItemStack bottle = new ItemStack(ExtraBotanyItems.manaGlassBottle);
		if (stack.isEmpty()) {
			return bottle;
		}
		if (!player.getInventory().add(bottle)) {
			player.drop(bottle, false);
		}
		return stack;
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity entity) {
		return DRINK_DURATION_TICKS;
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.DRINK;
	}
}
