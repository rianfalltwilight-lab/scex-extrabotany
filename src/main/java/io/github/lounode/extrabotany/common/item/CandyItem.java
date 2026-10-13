package io.github.lounode.extrabotany.common.item;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 糖果。吃下后恢复 4 点生命值，并得到 10 秒的二级药水效果（每种糖果一种效果）。圣诞节期间它显示另一个名字。
 * Candy. When it is eaten, it heals 4 health and gives a level II potion effect for 10 seconds (one effect for each candy type). In the Christmas period it shows a different name.
 */
public class CandyItem extends Item {
	private final Holder<MobEffect> effect;

	public CandyItem(Properties properties, Holder<MobEffect> effect) {
		super(properties.food(new FoodProperties.Builder().nutrition(2).saturationModifier(0.15F).alwaysEdible().build()));
		this.effect = effect;
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
		ItemStack result = super.finishUsingItem(stack, level, entity);
		if (!level.isClientSide() && entity instanceof Player player) {
			if (player.getHealth() < player.getMaxHealth()) {
				player.heal(4.0F);
			}
			player.addEffect(new MobEffectInstance(effect, 200, 1));
		}
		return result;
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity entity) {
		return 14;
	}

	@Override
	public Component getName(ItemStack stack) {
		return SeasonalItemHelper.isChristmas()
				? Component.translatable(getDescriptionId(stack) + ".christmas") : super.getName(stack);
	}
}
