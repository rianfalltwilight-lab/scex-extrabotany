package io.github.lounode.extrabotany.common.item.brew;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.api.item.Relic;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.item.brew.BaseBrewItem;
import vazkii.botania.common.component.BotaniaDataComponents;
import vazkii.botania.common.item.relic.RelicImpl;
import vazkii.botania.xplat.XplatAbstractions;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class InfiniteWineItem extends BaseBrewItem {

	public static final int UPDATE_MODULO = 4000;
	private static final int MANA_PER_REGEN = 12_000;
	public static final int AMPLIFIER_ADDITION = 1;
	public static final float DURATION_MULTIPLIER = 0.5F;

	public InfiniteWineItem(Properties builder, int swigs, int drinkSpeed, Supplier<Item> baseItem) {
		super(drinkSpeed, baseItem, builder.component(BotaniaDataComponents.MAX_USES, swigs));
	}

	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(Level world, Player player, @NotNull InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		var relic = io.github.lounode.extrabotany.xplat.EXplatAbstractions.INSTANCE.findRelic(stack);
		if (relic == null || !relic.isRightPlayer(player) || getSwigsLeft(stack) < 1) {
			return InteractionResultHolder.pass(stack);
		}

		return super.use(world, player, hand);
	}

	@Override
	public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, Level world, LivingEntity living) {
		if (!world.isClientSide) {
			for (MobEffectInstance effect : getBrew(stack).getPotionEffects(stack)) {
				MobEffectInstance newEffect = new MobEffectInstance(effect.getEffect(), (int) ((float) effect.getDuration() * (1.0 + getDurationMultiplier())), effect.getAmplifier() + getAmplifierAddition(), true, true);
				if (effect.getEffect().value().isInstantenous()) {
					effect.getEffect().value().applyInstantenousEffect(living, living, living, newEffect.getAmplifier() + getAmplifierAddition(), 1F);
				} else {
					living.addEffect(newEffect);
				}
			}

			if (world.random.nextBoolean()) {
				world.playSound(null, living.getX(), living.getY(), living.getZ(), SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 1F, 1F);
			}

			int swigs = getSwigsLeft(stack);
			if (living instanceof Player player && !player.hasInfiniteMaterials()) {
				setSwigsLeft(stack, swigs - 1);
			}
		}

		return stack;
	}

	public static Relic makeRelic(ItemStack stack) {
		return new RelicImpl(stack, null);
	}

	@Override
	public void appendHoverText(@NotNull ItemStack stack, TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flags) {
		RelicImpl.addDefaultTooltip(stack, tooltip);
		addPotionTooltip(getBrew(stack).getPotionEffects(stack), tooltip,
				(float) (1.0D + getDurationMultiplier()), getAmplifierAddition(), context.tickRate());
	}

	@Override
	public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
		if (!world.isClientSide && entity instanceof Player player) {
			var relic = io.github.lounode.extrabotany.xplat.EXplatAbstractions.INSTANCE.findRelic(stack);
			if (relic != null) {
				relic.tickBinding(player);
			}

			if (world.getGameTime() % getUpdateModulo() == 0 &&
					getSwigsLeft(stack) < getSwigs(stack) &&
					ManaItemHandler.instance().requestManaExactForTool(stack, player, getManaPerRegen(), true)) {
				setSwigsLeft(stack, getSwigsLeft(stack) + 1);
			}
		}
	}

	public int getUpdateModulo() {
		return UPDATE_MODULO;
	}

	public int getManaPerRegen() {
		return MANA_PER_REGEN;
	}

	public int getAmplifierAddition() {
		return AMPLIFIER_ADDITION;
	}

	public float getDurationMultiplier() {
		return DURATION_MULTIPLIER;
	}

	public static void addPotionTooltip(List<MobEffectInstance> effects, List<Component> lores,
			float durationFactor, int amplifierAddition, float tickRate) {
		List<MobEffectInstance> adjustedEffects = effects.stream()
				.map(effect -> new MobEffectInstance(effect.getEffect(), effect.getDuration(),
						effect.getAmplifier() + amplifierAddition, effect.isAmbient(), effect.isVisible(), effect.showIcon()))
				.toList();
		PotionContents.addPotionTooltip(adjustedEffects, lores::add, durationFactor, tickRate);
	}
}
