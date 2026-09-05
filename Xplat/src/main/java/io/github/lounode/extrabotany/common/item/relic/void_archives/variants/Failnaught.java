package io.github.lounode.extrabotany.common.item.relic.void_archives.variants;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;

import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.entity.ManaBurstEntity;
import io.github.lounode.extrabotany.common.helper.ItemNBTHelper;
import vazkii.botania.xplat.XplatAbstractions;

import io.github.lounode.extrabotany.api.item.VoidArchivesVariant;
import io.github.lounode.extrabotany.common.entity.MagicArrowEntity;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.sounds.ExtraBotanySounds;

public class Failnaught implements VoidArchivesVariant {

	public static Failnaught INSTANCE = new Failnaught();
	private static final String ID = "failnaught";

	public static final float[] TIER_PROCESS = { 0, 0, 0.35F, 0.7F, 0.9F };
	public static final int[] MANA_PER_USE_MAX = { 0, 350, 500, 650, 800 };

	private static final float DEFAULT_CHARGE_SPEED = 0.25F;
	private static final float QUICK_CHARGE_BONUS_PER_LEVEL = 0.1F;
	public static final float MINIMUM_SHOOT_PROCESS = 0.1F;

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack itemstack = player.getItemInHand(hand);
		int multiShoutLevel = getEnchantmentLevel(itemstack, level, Enchantments.MULTISHOT);
		float chargeProgress = getChargeProcess(itemstack, player);

		boolean flag = false;

		var relic = io.github.lounode.extrabotany.xplat.EXplatAbstractions.INSTANCE.findRelic(itemstack);
		if (relic != null &&
				relic.isRightPlayer(player) &&
				ManaItemHandler.instance().requestManaExactForTool(itemstack, player,
						getManaForUse(chargeProgress) * (multiShoutLevel > 1 ? 3 : 1), false)

		) {
			flag = true;
		}

		if (!player.getAbilities().instabuild && !flag) {
			return InteractionResultHolder.fail(itemstack);
		} else {
			player.startUsingItem(hand);
			return InteractionResultHolder.consume(itemstack);
		}
	}

	@Override
	public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeCharged) {
		if (level.isClientSide()) {
			return;
		}
		if (livingEntity instanceof Player player) {
			float chargeProgress = getChargeProcess(stack, player);

			int quickChargeLevel = getEnchantmentLevel(stack, level, Enchantments.QUICK_CHARGE);
			if (quickChargeLevel > 0) {
				ManaItemHandler.instance().requestManaExactForTool(stack, player, 100 * quickChargeLevel, true);
			}

			if (chargeProgress < MINIMUM_SHOOT_PROCESS) {
				return;
			}

			int multiShoutLevel = getEnchantmentLevel(stack, level, Enchantments.MULTISHOT);
			var relic = io.github.lounode.extrabotany.xplat.EXplatAbstractions.INSTANCE.findRelic(stack);
			if (relic != null &&
					relic.isRightPlayer(player) &&
					(player.getAbilities().instabuild
							||
							ManaItemHandler.instance().requestManaExactForTool(stack, player,
									getManaForUse(chargeProgress) * (multiShoutLevel > 1 ? 3 : 1), true))) {
				int manaInBurst = getManaForUse(chargeProgress) * (multiShoutLevel > 1 ? 3 : 1);

				float spreadAngle = 10.0f;

				for (int i = 0; i < (multiShoutLevel > 0 ? 3 : 1); i++) {
					ManaBurstEntity burst = getBurst(player, stack, manaInBurst, getTier(chargeProgress));

					if (multiShoutLevel > 0 && i > 0) {
						float angle = (i == 1 ? -spreadAngle : spreadAngle) * Mth.DEG_TO_RAD;
						burst.setDeltaMovement(burst.getDeltaMovement().yRot(angle));
					}

					player.level().addFreshEntity(burst);
				}

				player.level().playSound(null, player.getX(), player.getY(), player.getZ(), ExtraBotanySounds.FAILNAUGHT_SHOOT, SoundSource.PLAYERS, 1F, 1F);
			}
		}
	}

	public ManaBurstEntity getBurst(Player player, ItemStack stack, int mana, int tier) {
		MagicArrowEntity burst = new MagicArrowEntity(player);

		float motionModifier = 7F;

		burst.setColor(0x20FF20);
		burst.setMana(mana);
		burst.setStartingMana(mana);
		burst.setManaLossPerTick(MANA_PER_USE_MAX[tier] / 87.5f);
		burst.setGravity(0F);
		burst.setDeltaMovement(burst.getDeltaMovement().scale(motionModifier));

		ItemStack lens = new ItemStack(ExtraBotanyItems.failnaught);
		lens.set(DataComponents.ENCHANTMENTS,
				stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY));

		burst.setSourceLens(lens);

		float chargeProcess = getChargeProcess(stack, player);
		float tierProcess = getProcessInTier(chargeProcess);

		float baseDamage = 10;
		int powerLevel = getEnchantmentLevel(stack, player.level(), Enchantments.POWER);
		if (powerLevel > 0 &&
				ManaItemHandler.instance().requestManaExactForTool(stack, player, 50 * powerLevel, true)) {
			baseDamage = baseDamage + 0.5f + 0.5f * powerLevel;
		}
		float damage = baseDamage * (tier * tierProcess);

		burst.setDamage(damage);
		return burst;
	}

	public float getChargeProcess(ItemStack stack, LivingEntity entity) {
		return Mth.clamp((getUseDuration(stack) - entity.getUseItemRemainingTicks()) * chargeVelocityMultiplier(stack, entity) / 20.0F, 0.0F, 1.0F);
	}

	public int getTier(float chargeProcess) {
		chargeProcess = Mth.clamp(chargeProcess, 0.0f, 1.0f);

		if (chargeProcess < TIER_PROCESS[2]) {
			return 1; // 0.0 - 0.35
		} else if (chargeProcess < TIER_PROCESS[3]) {
			return 2; // 0.35 - 0.7
		} else if (chargeProcess < TIER_PROCESS[4]) {
			return 3; // 0.7 - 0.9
		} else {
			return 4; // 0.9 - 1.0
		}
	}

	public float getProcessInTier(float chargeProcess) {
		return switch (getTier(chargeProcess)) {
			case 1 -> chargeProcess / TIER_PROCESS[2];
			case 2 -> (chargeProcess - TIER_PROCESS[2]) / (TIER_PROCESS[3] - TIER_PROCESS[2]);
			case 3 -> (chargeProcess - TIER_PROCESS[3]) / (TIER_PROCESS[4] - TIER_PROCESS[3]);
			case 4 -> (chargeProcess - TIER_PROCESS[4]) / (1.0f - TIER_PROCESS[4]);
			default -> 0;
		};
	}

	public int getManaForUse(float chargeProcess) {
		chargeProcess = Mth.clamp(chargeProcess, 0.0f, 1.0f);
		int tier = getTier(chargeProcess);
		float tierProcess = getProcessInTier(chargeProcess);

		return switch (tier) {
			case 1 -> (int) (MANA_PER_USE_MAX[1] * tierProcess);
			case 2 -> (int) (MANA_PER_USE_MAX[1] + (MANA_PER_USE_MAX[2] - MANA_PER_USE_MAX[1]) * tierProcess);
			case 3 -> (int) (MANA_PER_USE_MAX[2] + (MANA_PER_USE_MAX[3] - MANA_PER_USE_MAX[2]) * tierProcess);
			case 4 -> (int) (MANA_PER_USE_MAX[3] + (MANA_PER_USE_MAX[4] - MANA_PER_USE_MAX[3]) * tierProcess);
			default -> 0;
		};
	}

	public float chargeVelocityMultiplier(ItemStack itemStack, LivingEntity livingEntity) {
		if (livingEntity instanceof Player player) {
			int quickChargeLevel = getEnchantmentLevel(itemStack, livingEntity.level(), Enchantments.QUICK_CHARGE);
			if (ManaItemHandler.instance().requestManaExactForTool(itemStack, player, 100 * quickChargeLevel, false)) {
				return DEFAULT_CHARGE_SPEED + QUICK_CHARGE_BONUS_PER_LEVEL * quickChargeLevel;
			}
		}

		return DEFAULT_CHARGE_SPEED;
	}

	@Override
	public int getUseDuration(ItemStack stack) {
		return 72000;
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.BOW;
	}

	private static int getEnchantmentLevel(ItemStack stack, Level level, ResourceKey<Enchantment> enchantment) {
		Holder<Enchantment> holder = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
				.getOrThrow(enchantment);
		return stack.getEnchantmentLevel(holder);
	}
}
