package io.github.lounode.extrabotany.common.item.equipment.bauble;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;

import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.handler.EquipmentHandler;
import io.github.lounode.extrabotany.common.helper.ItemNBTHelper;
import vazkii.botania.common.item.CustomCreativeTabContents;
import vazkii.botania.common.item.equipment.bauble.BaubleItem;

import io.github.lounode.extrabotany.api.item.NatureEnergyItem;
import io.github.lounode.extrabotany.common.entity.gaia.GaiaIII;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.xplat.EXplatAbstractions;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.List;

public class NatureOrbItem extends BaubleItem implements CustomCreativeTabContents {

	public static final long MAX_ENERGY = 500_000;
	public static final int SPAWN_GAIAIII_COST = 200_000;
	public static final int TIER_1_ENERGY = 100_000;
	public static final int TIER_2_ENERGY = 200_000;
	public static final int TIER_3_ENERGY = 300_000;
	public static final int TIER_4_ENERGY = 400_000;
	public static final int MANA_GENERATE_ADDITION_PER_TIER = 1;
	public static final int HEAL_DELAY = 60;
	public static final float HEAL_AMOUNT = 1.0F;
	public static final int REMOVE_HARMFUL_POTION_DELAY = 40;
	public static final int REMOVE_HARMFUL_POTION_COST = 200;

	private static final String TAG_ENERGY = "NatureEnergy";

	public NatureOrbItem(Properties properties) {
		super(properties);
	}

	@Override
	public void addToCreativeTab(Item me, CreativeModeTab.Output output) {
		output.accept(this);

		ItemStack fullPower = new ItemStack(this);
		setEnergy(fullPower, MAX_ENERGY);
		output.accept(fullPower);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		BlockPos pos = context.getClickedPos();
		Level level = context.getLevel();
		var tile = level.getBlockEntity(pos);
		if (tile instanceof BeaconBlockEntity) {
			return InteractionResult.CONSUME;
		}

		return super.useOn(context);
	}

	public static void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {
		Player player = event.getEntity();
		BlockPos pos = event.getPos();
		ItemStack handItem = event.getItemStack();
		ItemStack equipItem = EquipmentHandler.findOrEmpty(ExtraBotanyItems.natureOrb, player);
		InteractionHand hand = event.getHand();
		Level level = player.level();
		var tile = level.getBlockEntity(pos);

		if (!player.isShiftKeyDown()) {
			return;
		}

		ItemStack finalItem = handItem;
		if (handItem.isEmpty() && hand == InteractionHand.OFF_HAND) {
			finalItem = equipItem;
		}

		if (tile instanceof BeaconBlockEntity && finalItem.getItem() instanceof NatureOrbItem orb) {
			var natureItem = EXplatAbstractions.INSTANCE.findNatureEnergyItem(finalItem);
			if (natureItem == null || natureItem.getEnergy() < orb.getSpawnGaiaCost()) {
				return;
			}

			if (GaiaIII.spawn(player, finalItem, level, pos)) {
				if (!level.isClientSide()) {
					natureItem.addEnergy(-orb.getSpawnGaiaCost());
					event.setCanceled(true);
				}

				player.swing(hand);
			}
		}
	}

	@Override
	public void onWornTick(ItemStack stack, LivingEntity entity) {
		if (!(entity instanceof Player player)) {
			return;
		}
		var natureItem = EXplatAbstractions.INSTANCE.findNatureEnergyItem(stack);
		if (natureItem == null) {
			return;
		}

		if (getTier(stack) >= 1) {
			ManaItemHandler.instance().dispatchManaExact(stack, player, MANA_GENERATE_ADDITION_PER_TIER, true);
		}
		if (getTier(stack) >= 2) {
			ManaItemHandler.instance().dispatchManaExact(stack, player, MANA_GENERATE_ADDITION_PER_TIER, true);
		}
		if (getTier(stack) >= 3) {
			ManaItemHandler.instance().dispatchManaExact(stack, player, MANA_GENERATE_ADDITION_PER_TIER, true);
			if (player.tickCount % HEAL_DELAY == 0) {
				player.heal(HEAL_AMOUNT);
			}
		}
		if (getTier(stack) >= 4) {
			if (player.tickCount % REMOVE_HARMFUL_POTION_DELAY != 0) {
				return;
			}
			int removeCount = clearHarmfulPotion(player);
			if (removeCount > 0) {
				natureItem.addEnergy((long) -REMOVE_HARMFUL_POTION_COST * removeCount);
			}

		}
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flags) {
		super.appendHoverText(stack, context, tooltip, flags);
		var natureItem = EXplatAbstractions.INSTANCE.findNatureEnergyItem(stack);

		tooltip.add(Component.translatable("tooltip.extrabotany.nature_orb.tier1")
				.withStyle(getTier(stack) >= 1 ? ChatFormatting.AQUA : ChatFormatting.GRAY));
		tooltip.add(Component.translatable("tooltip.extrabotany.nature_orb.tier2")
				.withStyle(getTier(stack) >= 3 ? ChatFormatting.RED : ChatFormatting.GRAY));
		tooltip.add(Component.translatable("tooltip.extrabotany.nature_orb.tier3")
				.withStyle(getTier(stack) >= 4 ? ChatFormatting.GREEN : ChatFormatting.GRAY));
		tooltip.add(Component.empty());
		tooltip.add(Component.translatable("tooltip.extrabotany.nature_orb.energy", natureItem.getEnergy() + "/" + natureItem.getMaxEnergy())
				.withStyle(ChatFormatting.GRAY));
	}

	public static int getTier(ItemStack stack) {
		var natureItem = EXplatAbstractions.INSTANCE.findNatureEnergyItem(stack);
		if (natureItem == null) {
			return -1;
		}

		long energy = natureItem.getEnergy();
		if (energy > TIER_4_ENERGY) {
			return 4;
		} else if (energy > TIER_3_ENERGY) {
			return 3;
		} else if (energy > TIER_2_ENERGY) {
			return 2;
		} else if (energy > TIER_1_ENERGY) {
			return 1;
		}
		return 0;
	}

	public static int clearHarmfulPotion(LivingEntity entity) {
		List<MobEffectInstance> effects = entity.getActiveEffects().stream()
				.filter(effect -> effect.getEffect().value().getCategory() == MobEffectCategory.HARMFUL)
				.toList();

		effects.forEach(e -> entity.removeEffect(e.getEffect()));

		return effects.size();
	}

	public int getSpawnGaiaCost() {
		return SPAWN_GAIAIII_COST;
	}

	protected static void setEnergy(ItemStack stack, long energy) {
		if (energy > 0) {
			ItemNBTHelper.setLong(stack, TAG_ENERGY, energy);
		} else {
			ItemNBTHelper.removeEntry(stack, TAG_ENERGY);
		}
	}

	@Override
	public int getBarColor(ItemStack stack) {
		var natureItem = EXplatAbstractions.INSTANCE.findNatureEnergyItem(stack);
		return Mth.hsvToRgb((natureItem.getEnergy() / (float) natureItem.getMaxEnergy()) / 3.0F, 1.0F, 1.0F);
	}

	@Override
	public int getBarWidth(ItemStack stack) {
		var natureItem = EXplatAbstractions.INSTANCE.findNatureEnergyItem(stack);
		return Math.round(13 * (natureItem.getEnergy() / (float) natureItem.getMaxEnergy()));
	}

	@Override
	public boolean isBarVisible(ItemStack stack) {
		var natureItem = EXplatAbstractions.INSTANCE.findNatureEnergyItem(stack);
		return natureItem.getEnergy() < natureItem.getMaxEnergy();
	}

	public static class NatureEnergyImpl implements NatureEnergyItem {

		private final ItemStack stack;

		public NatureEnergyImpl(ItemStack stack) {
			this.stack = stack;
		}

		@Override
		public long getEnergy() {
			return ItemNBTHelper.getLong(stack, TAG_ENERGY, 0) * stack.getCount();
		}

		@Override
		public long getMaxEnergy() {
			return MAX_ENERGY * stack.getCount();
		}

		@Override
		public boolean addEnergy(long energy) {
			long current = getEnergy() / stack.getCount();
			long maxSingle = getMaxEnergy() / stack.getCount();

			long newEnergy;
			if (energy > 0 && current > maxSingle - energy) {
				newEnergy = maxSingle;
			} else if (energy < 0 && current < -energy) {
				newEnergy = 0;
			} else {
				newEnergy = current + energy;
			}

			newEnergy = Math.min(newEnergy, maxSingle);
			newEnergy = Math.max(newEnergy, 0);

			setEnergy(stack, newEnergy);
			return newEnergy != current;
		}
	}
}
