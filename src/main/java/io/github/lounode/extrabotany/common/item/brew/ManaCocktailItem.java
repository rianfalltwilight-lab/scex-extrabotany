package io.github.lounode.extrabotany.common.item.brew;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.brew.Brew;
import vazkii.botania.common.brew.BotaniaBrews;
import vazkii.botania.common.component.BotaniaDataComponents;
import vazkii.botania.common.item.brew.BaseBrewItem;

import io.github.lounode.extrabotany.common.brew.ExtraBotanyBrews;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

import java.util.function.Supplier;

/**
 * 魔力鸡尾酒。用酿造瓶和魔力饮品合成，装有酿造瓶里的酿造，可以喝 8 次。效果持续时间是普通酿造的 1.3 倍。
 * Mana Cocktail. It is crafted from a brew flask and a Mana Drink, holds the brew of the flask, and has 8 swigs. Its effects last 1.3 times as long as those of a usual brew.
 *
 * <p>"默认的鸡尾酒"（魔力鸡尾酒酿造、只能喝一次）是重制版的旧物品，只为旧存档保留。
 * <p>The "default cocktail" (the Mana Cocktail brew, one swig) is an old item of Reburn, kept only for old saves.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code ItemCocktail}（8 次、饮用 20 tick、持续时间 × 1.3、等级不变）。
 * <p>Source: ExtraMeteorP 1.16 {@code ItemCocktail} (8 swigs, 20 ticks to drink, duration × 1.3, the same level).
 */
public class ManaCocktailItem extends BaseBrewItem {
	public static final float DURATION_FACTOR = 1.3F;

	public ManaCocktailItem(Properties properties, int swigs, int drinkSpeed, Supplier<Item> baseItem) {
		super(drinkSpeed, baseItem, properties.component(BotaniaDataComponents.MAX_USES, swigs));
	}

	@Override
	public void addToCreativeTab(Item me, CreativeModeTab.Output output) {
		for (Brew brew : BotaniaAPI.instance().getBrewRegistry()) {
			if (brew == BotaniaBrews.FALLBACK) {
				continue;
			}
			if (brew == ExtraBotanyBrews.manaCocktail) {
				continue;
			}

			ItemStack stack = new ItemStack(this);
			setBrew(stack, brew);
			output.accept(stack);
		}
	}

	@Override
	public @NotNull Component getName(@NotNull ItemStack stack) {
		Brew brew = getBrew(stack);
		if (brew == ExtraBotanyBrews.manaCocktail && getSwigsLeft(stack) == 1) {
			return Component.translatable("item.extrabotany.mana_cocktail.default");
		}
		return super.getName(stack);
	}

	/**
	 * 和植物魔法的 {@code BaseBrewItem#finishUsingItem} 相同，只是效果持续时间乘以 {@link #DURATION_FACTOR}。
	 * The same as {@code BaseBrewItem#finishUsingItem} of Botania, except that the effect duration is multiplied by {@link #DURATION_FACTOR}.
	 */
	@Override
	public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, Level world, LivingEntity living) {
		if (world.isClientSide) {
			return stack;
		}
		for (MobEffectInstance effect : getBrew(stack).getPotionEffects(stack)) {
			MobEffectInstance newEffect = new MobEffectInstance(effect.getEffect(),
					(int) (effect.getDuration() * DURATION_FACTOR), effect.getAmplifier(), true, true);
			if (effect.getEffect().value().isInstantenous()) {
				effect.getEffect().value().applyInstantenousEffect(living, living, living, newEffect.getAmplifier(), 1F);
			} else {
				living.addEffect(newEffect);
			}
		}
		if (world.random.nextBoolean()) {
			world.playSound(null, living.getX(), living.getY(), living.getZ(), SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 1F, 1F);
		}
		int swigs = getSwigsLeft(stack);
		if (living instanceof Player player && !player.hasInfiniteMaterials()) {
			if (swigs <= 1) {
				ItemStack base = getBaseStack();
				return player.getInventory().add(base) ? ItemStack.EMPTY : base;
			}
			setSwigsLeft(stack, swigs - 1);
		}
		return stack;
	}

	public static ItemStack getDefaultCocktail() {
		ItemStack manaCocktail = new ItemStack(ExtraBotanyItems.manaCocktail);
		ManaCocktailItem.setBrew(manaCocktail, ExtraBotanyBrews.manaCocktail);
		manaCocktail.set(BotaniaDataComponents.REMAINING_USES, 1);

		return manaCocktail;
	}
}
