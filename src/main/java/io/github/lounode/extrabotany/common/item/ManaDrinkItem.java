package io.github.lounode.extrabotany.common.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

/**
 * 魔力饮品。喝下后得到 60 秒的抗性提升、速度和跳跃提升（效果写在食物属性里），并返还一个魔力玻璃瓶。
 * Mana Drink. The drink gives Resistance, Speed and Jump Boost for 60 seconds (the effects are in the food properties), and the player gets a Managlass Bottle back.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code ItemManaDrink} 和 {@code ModItems.MANADRINK_PROP}。1.16 的类没有调用父类，食物效果实际上不生效；这里按食物属性的本意给效果。1.12 的回血和 10000 魔力已经去掉。
 * <p>Source: {@code ItemManaDrink} and {@code ModItems.MANADRINK_PROP} of ExtraMeteorP 1.16. The class of 1.16 did not call its parent, so the food effects did not work in fact; here the effects are given as the food properties mean them. The healing and the 10000 mana of 1.12 are removed.
 */
public class ManaDrinkItem extends Item {
	public ManaDrinkItem(Properties props) {
		super(props);
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
		ItemStack result = super.finishUsingItem(stack, level, entity);
		if (entity instanceof Player player && player.hasInfiniteMaterials()) {
			return result;
		}
		ItemStack bottle = new ItemStack(ExtraBotanyItems.manaGlassBottle);
		if (result.isEmpty()) {
			return bottle;
		}
		if (entity instanceof Player player && !player.getInventory().add(bottle)) {
			player.drop(bottle, false);
		}
		return result;
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.DRINK;
	}
}
