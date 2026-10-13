package io.github.lounode.extrabotany.common.item.relic.void_archives;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;


import vazkii.botania.api.item.Relic;
import vazkii.botania.api.mana.ManaItemHandler;
import io.github.lounode.extrabotany.common.helper.ItemNBTHelper;
import vazkii.botania.common.item.CustomCreativeTabContents;
import vazkii.botania.common.item.relic.RelicImpl;

import io.github.lounode.extrabotany.api.ExtraBotanyAPI;
import io.github.lounode.extrabotany.api.item.VoidArchivesVariant;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.item.relic.void_archives.variants.*;
import io.github.lounode.extrabotany.common.lib.LibAdvancementNames;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 虚空万藏。遗物。它可以变成别的遗物的形态（见 {@code VoidArchivesVariant}），潜行并使用来切换。保持默认形态以外的形态时，它每 tick 消耗 {@value #KEEP_VARIANT_REQUIRE} 点魔力，魔力不足时变回默认形态。物品的所有行为都转交给当前的形态。
 * Void Archives. A relic. It can take the form of other relics (see {@code VoidArchivesVariant}), and a sneak-use changes the form. While it has a form that is not the default form, it uses {@value #KEEP_VARIANT_REQUIRE} mana on each tick, and it goes back to the default form when there is not sufficient mana. All the behavior of the item is sent to the current form.
 */
public class VoidArchivesItem extends Item implements CustomCreativeTabContents {

	private static final String TAG_VARIANT = "variant";
	public static final int KEEP_VARIANT_REQUIRE = 500;

	public VoidArchivesItem(Properties properties) {
		super(properties);
		ExtraBotanyAPI.instance().registerVoidArchivesVariant(VoidArchivesVariant.DEFAULT);
		ExtraBotanyAPI.instance().registerVoidArchivesVariant(FruitOfGrisaia.INSTANCE);
		ExtraBotanyAPI.instance().registerVoidArchivesVariant(InfiniteWine.INSTANCE);
		ExtraBotanyAPI.instance().registerVoidArchivesVariant(Excalibur.INSTANCE);
		ExtraBotanyAPI.instance().registerVoidArchivesVariant(Failnaught.INSTANCE);
		ExtraBotanyAPI.instance().registerVoidArchivesVariant(Camera.INSTANCE);
	}

	@Override
	public void addToCreativeTab(Item item, CreativeModeTab.Output output) {
		output.accept(getDefaultItemStack());
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
		ItemStack stack = player.getItemInHand(usedHand);

		if (player.isShiftKeyDown()) {
			switchVariant(stack);
			return super.use(level, player, usedHand);
		}

		return getVariant(stack).use(level, player, usedHand);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
		super.appendHoverText(stack, context, tooltipComponents, isAdvanced);
		RelicImpl.addDefaultTooltip(stack, tooltipComponents);
		getVariant(stack).appendHoverText(stack, context, tooltipComponents, isAdvanced);
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
		if (!level.isClientSide && entity instanceof Player player) {
			var relic = io.github.lounode.extrabotany.xplat.EXplatAbstractions.INSTANCE.findRelic(stack);
			if (relic != null) {
				relic.tickBinding(player);
			}
			if (getVariant(stack) != VoidArchivesVariant.DEFAULT &&
					!ManaItemHandler.instance().requestManaExactForTool(stack, player, getKeepVariantRequire(), true)) {

				setVariant(stack, VoidArchivesVariant.DEFAULT);
			}
		}
		super.inventoryTick(stack, level, entity, slotId, isSelected);
		getVariant(stack).inventoryTick(stack, level, entity, slotId, isSelected);
	}

	public static void switchVariant(ItemStack stack) {
		if (!(stack.getItem() instanceof VoidArchivesItem)) {
			return;
		}

		Map<String, VoidArchivesVariant> variants = ExtraBotanyAPI.instance().getVoidArchivesVariants();
		VoidArchivesVariant current = getVariant(stack);

		int index = new ArrayList<>(variants.keySet()).indexOf(current.getId());
		int nextIndex = (index + 1) % variants.size();

		String nextKey = new ArrayList<>(variants.keySet()).get(nextIndex);
		VoidArchivesVariant nextVariant = variants.get(nextKey);

		setVariant(stack, nextVariant);
	}

	public static void setVariant(ItemStack stack, VoidArchivesVariant variant) {
		if (!(stack.getItem() instanceof VoidArchivesItem)) {
			return;
		}
		VoidArchivesVariant current = getVariant(stack);

		current.onInactive(stack);
		ItemNBTHelper.setString(stack, TAG_VARIANT, variant.getId());
		variant.onActive(stack);
	}

	public static VoidArchivesVariant getVariant(ItemStack stack) {
		String variantString = ItemNBTHelper.getString(stack, TAG_VARIANT, VoidArchivesVariant.DEFAULT.getId());
		return ExtraBotanyAPI.instance().getVoidArchivesVariants()
				.getOrDefault(variantString, VoidArchivesVariant.DEFAULT);
	}

	public static String getTagVariant(ItemStack stack) {
		return ItemNBTHelper.getString(stack, TAG_VARIANT, VoidArchivesVariant.DEFAULT.getId());
	}

	public static int getVariantIndex(ItemStack stack) {
		if (!(stack.getItem() instanceof VoidArchivesItem)) {
			return -1;
		}

		Map<String, VoidArchivesVariant> variants = ExtraBotanyAPI.instance().getVoidArchivesVariants();
		VoidArchivesVariant current = getVariant(stack);

		return new ArrayList<>(variants.keySet()).indexOf(current.getId());
	}

	public static ItemStack getDefaultItemStack() {
		var defaultStack = new ItemStack(ExtraBotanyItems.voidArchives);
		ItemNBTHelper.setString(defaultStack, TAG_VARIANT, VoidArchivesVariant.DEFAULT.getId());
		return defaultStack;
	}

	public int getKeepVariantRequire() {
		return KEEP_VARIANT_REQUIRE;
	}

	@Override
	public ItemStack getDefaultInstance() {
		return getDefaultItemStack();
	}

	public static Relic makeRelic(ItemStack stack) {
		return new RelicImpl(stack, prefix("main/" + LibAdvancementNames.THE_ORIGINAL_DIVINE_KEY));
	}

	@Override
	public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
		super.onUseTick(level, livingEntity, stack, remainingUseDuration);
		getVariant(stack).onUseTick(level, livingEntity, stack, remainingUseDuration);
	}

	@Override
	public void onDestroyed(ItemEntity itemEntity, DamageSource damageSource) {
		getVariant(itemEntity.getItem()).onDestroyed(itemEntity);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		return getVariant(context.getItemInHand()).useOn(context);
	}

	@Override
	public float getDestroySpeed(ItemStack stack, BlockState state) {
		return getVariant(stack).getDestroySpeed(stack, state);
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
		return getVariant(stack).finishUsingItem(stack, level, livingEntity);
	}

	@Override
	public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access) {
		return getVariant(stack).overrideOtherStackedOnMe(stack, other, slot, action, player, access);
	}

	@Override
	public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction action, Player player) {
		return getVariant(stack).overrideStackedOnOther(stack, slot, action, player);
	}

	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		return getVariant(stack).hurtEnemy(stack, target, attacker);
	}

	@Override
	public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miningEntity) {
		return getVariant(stack).mineBlock(stack, level, state, pos, miningEntity);
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity entity) {
		return getVariant(stack).getUseDuration(stack);
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return getVariant(stack).getUseAnimation(stack);
	}

	@Override
	public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeCharged) {
		getVariant(stack).releaseUsing(stack, level, livingEntity, timeCharged);
	}

	@Override
	public boolean useOnRelease(ItemStack stack) {
		return getVariant(stack).useOnRelease(stack);
	}
}
