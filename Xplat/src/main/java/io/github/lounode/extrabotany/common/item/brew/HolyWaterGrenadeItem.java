package io.github.lounode.extrabotany.common.item.brew;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.brew.Brew;
import vazkii.botania.api.brew.BrewItem;
import vazkii.botania.common.brew.BotaniaBrews;
import io.github.lounode.extrabotany.common.helper.ItemNBTHelper;
import vazkii.botania.common.item.CustomCreativeTabContents;

import io.github.lounode.extrabotany.common.entity.HolyWaterGrenadeEntity;

import java.util.ArrayList;
import java.util.List;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class HolyWaterGrenadeItem extends Item implements BrewItem, CustomCreativeTabContents {

	private static final String TAG_BREW_KEY = "brewKey";

	public HolyWaterGrenadeItem(Properties properties) {
		super(properties);
	}

	@Override
	public void addToCreativeTab(Item me, CreativeModeTab.Output output) {
		for (Brew brew : BotaniaAPI.instance().getBrewRegistry()) {
			if (brew == BotaniaBrews.FALLBACK) {
				continue;
			}
			ItemStack stack = new ItemStack(this);
			setBrew(stack, brew);
			output.accept(stack);
		}
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
		ItemStack stack = player.getItemInHand(usedHand);
		if (!level.isClientSide()) {
			level.playSound((Player) null, player.getX(), player.getY(), player.getZ(), SoundEvents.SPLASH_POTION_THROW, SoundSource.PLAYERS, 0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));

			HolyWaterGrenadeEntity grenade = new HolyWaterGrenadeEntity(level, player);
			grenade.setItem(stack);
			grenade.shootFromRotation(player, player.getXRot(), player.getYRot(), -20.0F, 0.5F, 1.0F);

			level.addFreshEntity(grenade);
		}

		player.awardStat(Stats.ITEM_USED.get(this));
		if (!player.isCreative()) {
			stack.shrink(1);
		}

		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flags) {
		addPotionTooltip(getBrew(stack).getPotionEffects(stack), list, 1, context.tickRate());
	}

	@NotNull
	@Override
	public Component getName(@NotNull ItemStack stack) {
		return Component.translatable(getDescriptionId(), Component.translatable(getBrew(stack).getTranslationKey(stack)));
	}

	public static void addPotionTooltip(List<MobEffectInstance> list, List<Component> lores,
			float durationFactor, float tickRate) {
		List<Pair<Holder<Attribute>, AttributeModifier>> attributeModifiers = Lists.newArrayList();
		if (list.isEmpty()) {
			lores.add((Component.translatable("effect.none")).withStyle(ChatFormatting.GRAY));
		} else {
			for (MobEffectInstance effectinstance : list) {
				MutableComponent effectName = Component.translatable(effectinstance.getDescriptionId());
				var effect = effectinstance.getEffect();
				effect.value().createModifiers(effectinstance.getAmplifier(),
						(attribute, modifier) -> attributeModifiers.add(new Pair<>(attribute, modifier)));

				if (effectinstance.getAmplifier() > 0) {
					effectName = Component.translatable("potion.withAmplifier", effectName,
							Component.translatable("potion.potency." + effectinstance.getAmplifier()));
				}

				if (!effectinstance.endsWithin(20)) {
					effectName = Component.translatable("potion.withDuration", effectName,
							MobEffectUtil.formatDuration(effectinstance, durationFactor, tickRate));
				}

				lores.add(effectName.withStyle(effect.value().getCategory().getTooltipFormatting()));
			}
		}

		if (!attributeModifiers.isEmpty()) {
			lores.add(Component.empty());
			lores.add((Component.translatable("potion.whenDrank")).withStyle(ChatFormatting.DARK_PURPLE));

			List<Component> enemyModifiers = new ArrayList<>();
			for (Pair<Holder<Attribute>, AttributeModifier> pair : attributeModifiers) {
				if (pair.getSecond().amount() < 0.0D) {
					enemyModifiers.add(formatAttributeModifier(pair));
				}
			}
			if (!enemyModifiers.isEmpty()) {
				lores.add(Component.empty());
				lores.add((Component.translatable("tooltip.extrabotany.buff_for_enemy")).withStyle(ChatFormatting.DARK_RED));
				lores.addAll(enemyModifiers);
			}

			List<Component> selfModifiers = new ArrayList<>();
			for (Pair<Holder<Attribute>, AttributeModifier> pair : attributeModifiers) {
				if (pair.getSecond().amount() > 0.0D) {
					selfModifiers.add(formatAttributeModifier(pair));
				}
			}

			if (!selfModifiers.isEmpty()) {
				lores.add(Component.empty());
				lores.add((Component.translatable("tooltip.extrabotany.buff_for_self")).withStyle(ChatFormatting.GREEN));
				lores.addAll(selfModifiers);
			}
		}
	}

	private static Component formatAttributeModifier(Pair<Holder<Attribute>, AttributeModifier> pair) {
		AttributeModifier modifier = pair.getSecond();
		double amount = modifier.amount();
		double displayAmount = modifier.operation() == AttributeModifier.Operation.ADD_VALUE
				? amount : amount * 100.0D;
		String sign = amount > 0.0D ? "plus" : "take";
		ChatFormatting color = amount > 0.0D ? ChatFormatting.BLUE : ChatFormatting.RED;
		return Component.translatable("attribute.modifier." + sign + "." + modifier.operation().id(),
				ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(Math.abs(displayAmount)),
				Component.translatable(pair.getFirst().value().getDescriptionId())).withStyle(color);
	}

	@Override
	public Brew getBrew(ItemStack stack) {
		String key = ItemNBTHelper.getString(stack, TAG_BREW_KEY, "");
		ResourceLocation id = ResourceLocation.tryParse(key);
		Brew brew = id == null ? null : BotaniaAPI.instance().getBrewRegistry().get(id);
		return brew == null ? BotaniaBrews.FALLBACK : brew;
	}

	public static void setBrew(ItemStack stack, @Nullable Brew brew) {
		ResourceLocation id;
		if (brew != null) {
			id = BotaniaAPI.instance().getBrewRegistry().getKey(brew);
		} else {
			id = prefix("fallback");
		}
		setBrew(stack, id);
	}

	public static void setBrew(ItemStack stack, ResourceLocation brew) {
		ItemNBTHelper.setString(stack, TAG_BREW_KEY, brew.toString());
	}

	@NotNull
	public static String getSubtype(ItemStack stack) {
		return ItemNBTHelper.getString(stack, TAG_BREW_KEY, "none");
	}
}
