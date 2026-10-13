package io.github.lounode.extrabotany.common.item.equipment.bauble;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
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
import io.github.lounode.extrabotany.common.entity.gaia.Ego;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.xplat.EXplatAbstractions;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.List;

/**
 * 自然蕴息宝珠。它在祭坛上积累自然能量，能量越多等级越高。佩戴时：一级起每 tick 产生魔力，三级起缓慢恢复生命值，四级起消耗能量来清除负面效果。潜行并对着盖亚仪式场地的信标使用时，消耗 200000 点能量召唤本我。
 * Nature Orb. It collects nature energy on a pedestal, and more energy gives a higher tier. When it is worn: from tier 1 it makes mana on each tick, from tier 3 it heals slowly, and from tier 4 it uses energy to remove harmful effects. A sneak-use on the beacon of a Gaia ritual arena uses 200000 energy to summon the Ego.
 */
public class NatureOrbItem extends BaubleItem implements CustomCreativeTabContents {

	public static final long MAX_ENERGY = 500_000;
	public static final int SUMMON_EGO_COST = 200_000;
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

	/**
	 * 潜行右键信标时召唤本我。手里拿着宝珠时用手里的宝珠；这只手是空的时候，用佩戴着的宝珠。
	 * Summons the Ego at a sneak-use on a beacon. An orb in the hand is used; when this hand is empty, the worn orb is used.
	 *
	 * <p>每次右键方块都会调用这个方法，所以先做最便宜的判断（是否潜行），最后才查找佩戴的饰品。
	 * <p>This method is called for each use on a block, thus the cheapest check (sneak) comes first, and the search for the worn accessory comes last.
	 *
	 * <p>以前只有副手的事件才会查找佩戴的宝珠。两只手都空着的时候，主手的事件会先打开信标的界面，副手的事件不再发生，所以佩戴着的宝珠不能召唤。
	 * <p>Before, only the off-hand event searched for the worn orb. With two empty hands, the main-hand event opened the beacon screen first and the off-hand event did not occur, thus a worn orb could not summon.
	 */
	public static void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {
		Player player = event.getEntity();
		if (!player.isShiftKeyDown()) {
			return;
		}
		Level level = player.level();
		BlockPos pos = event.getPos();
		if (!(level.getBlockEntity(pos) instanceof BeaconBlockEntity)) {
			return;
		}
		ItemStack orbStack = event.getItemStack();
		if (orbStack.isEmpty()) {
			orbStack = EquipmentHandler.findOrEmpty(ExtraBotanyItems.natureOrb, player);
		}
		if (!(orbStack.getItem() instanceof NatureOrbItem orb)) {
			return;
		}
		var natureItem = EXplatAbstractions.INSTANCE.findNatureEnergyItem(orbStack);
		if (natureItem == null || natureItem.getEnergy() < orb.getSummonCost()) {
			return;
		}

		if (Ego.spawn(player, level, pos)) {
			if (!level.isClientSide()) {
				natureItem.addEnergy(-orb.getSummonCost());
			}
			// 两端都取消这次右键，这样信标的界面不会打开。
			// The use is cancelled on the two sides, so that the beacon screen does not open.
			event.setCanceled(true);
			event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide()));
			player.swing(event.getHand());
		}
	}

	@Override
	public void onWornTick(ItemStack stack, LivingEntity entity) {
		// 魔力、治疗和药水效果都由服务端决定。客户端运行这段代码只会让两边的数据暂时不一致。
		// The server decides the mana, the healing and the potion effects. If the client ran this code, the data of the two sides would only be different for a time.
		if (entity.level().isClientSide() || !(entity instanceof Player player)) {
			return;
		}
		var natureItem = EXplatAbstractions.INSTANCE.findNatureEnergyItem(stack);
		if (natureItem == null) {
			return;
		}

		int tier = getTier(stack);
		// 每一级产生 1 点魔力，最多 3 级。分开发送，因为"精确"发送在放不下全部数量时会整笔失败。
		// Each tier makes 1 mana, to a maximum of 3 tiers. They are sent separately, because an "exact" dispatch fails fully when the full quantity does not fit.
		for (int n = Math.min(tier, 3); n > 0; n--) {
			ManaItemHandler.instance().dispatchManaExact(stack, player, MANA_GENERATE_ADDITION_PER_TIER, true);
		}
		if (tier >= 3 && player.tickCount % HEAL_DELAY == 0) {
			player.heal(HEAL_AMOUNT);
		}
		if (tier >= 4) {
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

	public int getSummonCost() {
		return SUMMON_EGO_COST;
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
