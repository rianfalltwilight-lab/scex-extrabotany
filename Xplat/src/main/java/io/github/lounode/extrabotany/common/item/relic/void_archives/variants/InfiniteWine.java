package io.github.lounode.extrabotany.common.item.relic.void_archives.variants;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.brew.Brew;
import vazkii.botania.api.brew.BrewItem;
import vazkii.botania.api.mana.ManaItemHandler;
import io.github.lounode.extrabotany.common.helper.ItemNBTHelper;
import vazkii.botania.xplat.XplatAbstractions;

import io.github.lounode.extrabotany.api.item.VoidArchivesVariant;
import io.github.lounode.extrabotany.common.lib.LibBrewNames;

import java.util.List;

import static io.github.lounode.extrabotany.common.item.brew.InfiniteWineItem.*;
import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class InfiniteWine implements VoidArchivesVariant, BrewItem {

	public static InfiniteWine INSTANCE = new InfiniteWine();
	private static final String ID = "infinite_wine";
	private static final String TAG_BREW_KEY = "brewKey";

	private static final ResourceLocation DEFAULT_BREW = prefix(LibBrewNames.ALL_IN_ONE);
	private static final int MANA_PER_USE = 20_000;

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		var relic = io.github.lounode.extrabotany.xplat.EXplatAbstractions.INSTANCE.findRelic(stack);
		if (relic == null || !relic.isRightPlayer(player)) {
			return InteractionResultHolder.pass(stack);
		}

		return ItemUtils.startUsingInstantly(level, player, hand);
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity living) {
		if (!level.isClientSide()) {
			if (living instanceof Player player) {
				ManaItemHandler.instance().requestManaExactForTool(stack, player, getManaPerUse(), true);
			}

			for (MobEffectInstance effect : getBrew(stack).getPotionEffects(stack)) {
				MobEffectInstance newEffect = new MobEffectInstance(effect.getEffect(), (int) ((float) effect.getDuration() * (1.0 + getDurationMultiplier())), effect.getAmplifier() + getAmplifierAddition(), true, true);
				if (effect.getEffect().value().isInstantenous()) {
					effect.getEffect().value().applyInstantenousEffect(living, living, living, newEffect.getAmplifier() + getAmplifierAddition(), 1F);
				} else {
					living.addEffect(newEffect);
				}
			}

			if (level.random.nextBoolean()) {
				level.playSound(null, living.getX(), living.getY(), living.getZ(), SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 1F, 1F);
			}
		}

		return stack;
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
		tooltipComponents.add(Component.empty());
		addPotionTooltip(getBrew(stack).getPotionEffects(stack), tooltipComponents,
				(float) (1.0D + getDurationMultiplier()), getAmplifierAddition(), context.tickRate());
	}

	@Override
	public void onActive(ItemStack stack) {
		if (ItemNBTHelper.getString(stack, TAG_BREW_KEY, "").isEmpty()) {
			ItemNBTHelper.setString(stack, TAG_BREW_KEY, getDefaultBrew().toString());
		}
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.DRINK;
	}

	@Override
	public int getUseDuration(ItemStack stack) {
		return 16;
	}

	@Override
	public String getId() {
		return ID;
	}

	public static int getManaPerUse() {
		return MANA_PER_USE;
	}

	public static ResourceLocation getDefaultBrew() {
		return DEFAULT_BREW;
	}

	@Override
	public Brew getBrew(ItemStack itemStack) {
		String key = ItemNBTHelper.getString(itemStack, TAG_BREW_KEY, "");
		return BotaniaAPI.instance().getBrewRegistry().get(ResourceLocation.tryParse(key));
	}

	public int getAmplifierAddition() {
		return AMPLIFIER_ADDITION;
	}

	public float getDurationMultiplier() {
		return DURATION_MULTIPLIER;
	}
}
