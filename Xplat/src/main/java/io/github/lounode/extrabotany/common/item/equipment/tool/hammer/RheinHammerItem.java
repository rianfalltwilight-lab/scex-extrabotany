package io.github.lounode.extrabotany.common.item.equipment.tool.hammer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.item.Relic;
import vazkii.botania.common.helper.PlayerHelper;
import vazkii.botania.common.item.relic.RelicImpl;
import vazkii.botania.xplat.XplatAbstractions;

import io.github.lounode.extrabotany.api.item.IAerialite;
import io.github.lounode.extrabotany.api.item.IOrichalcos;
import io.github.lounode.extrabotany.api.item.IPhotonium;
import io.github.lounode.extrabotany.api.item.IShadowium;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.item.material.HammerTiers;
import io.github.lounode.extrabotany.common.lib.LibAdvancementNames;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.List;
import java.util.Locale;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class RheinHammerItem extends TerrasteelHammerItem implements IShadowium, IPhotonium, IAerialite, IOrichalcos {

	private static final int MANA_PER_DAMAGE = 300;
	private static final int ACTIVE_COST = 30;
	public static final int RANGE = 5;
	public static final int BUFF_MANA = 200;

	private static final String ADVANCEMENT_NAME = "spongebob";

	public RheinHammerItem(Properties props) {
		this(HammerTiers.RHEIN, 4, -3.0F, props);
	}

	public RheinHammerItem(Tier tier, int attackDamageModifier, float attackSpeedModifier, Properties properties) {
		super(tier, attackDamageModifier, attackSpeedModifier, properties);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
		super.appendHoverText(stack, context, tooltipComponents, isAdvanced);
		RelicImpl.addDefaultTooltip(stack, tooltipComponents);
	}

	@Override
	public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
		super.inventoryTick(stack, world, entity, slot, selected);
		if (!world.isClientSide && entity instanceof Player player) {
			var relic = io.github.lounode.extrabotany.xplat.EXplatAbstractions.INSTANCE.findRelic(stack);
			if (relic != null) {
				relic.tickBinding(player);
			}
		}
		if (entity instanceof ServerPlayer serverPlayer && checkAdvancement(stack, serverPlayer)) {
			PlayerHelper.grantCriterion(serverPlayer, prefix("main/" + LibAdvancementNames.SPONGE_HAMMER), "code_triggered");
		}

		if (!isEnabled(stack)) {
			return;
		}
		shadowiumTick(stack, world, entity, slot, selected);
		photoniumTick(stack, world, entity, slot, selected);
		aerialiteTick(stack, world, entity, slot, selected);
		orichalcosTick(stack, world, entity, slot, selected);
	}

	private boolean checkAdvancement(ItemStack stack, ServerPlayer serverPlayer) {
		if (PlayerHelper.hasAdvancement(serverPlayer, prefix("main/" + LibAdvancementNames.SPONGE_HAMMER))) {
			return false;
		}

		if (!ADVANCEMENT_NAME.equals(stack.getHoverName().getString().toLowerCase(Locale.ROOT))) {
			return false;
		}

		return true;
	}

	@Override
	public boolean canBeHurtBy(ItemStack stack, DamageSource damageSource) {
		return IOrichalcos.canBeHurtBy(damageSource);
	}

	public static void onDig(PlayerEvent.BreakSpeed wrapper) {
		ItemStack _left = wrapper.getEntity().getItemInHand(InteractionHand.OFF_HAND);
		ItemStack _right = wrapper.getEntity().getItemInHand(InteractionHand.MAIN_HAND);
		ItemStack stack = ItemStack.EMPTY;

		if (_left.is(ExtraBotanyItems.rheinHammer)) {
			stack = _left;
		} else if (_right.is(ExtraBotanyItems.rheinHammer)) {
			stack = _right;
		}

		if (stack.isEmpty() || !stack.is(ExtraBotanyItems.rheinHammer)) {
			return;
		}

		if (stack.is(ExtraBotanyItems.rheinHammer) && !RheinHammerItem.isEnabled(stack)) {
			wrapper.setNewSpeed(0);
			wrapper.setCanceled(true);
		}
	}

	@Override
	public int getManaPerDamage() {
		return MANA_PER_DAMAGE;
	}

	@Override
	public int getActiveCost() {
		return ACTIVE_COST;
	}

	@Override
	public int getRange(Player player, ItemStack stack, BlockPos pos, BlockPos originPos, Direction side) {
		return RANGE;
	}

	@Override
	public int getShadowiumBuffMana() {
		return BUFF_MANA;
	}

	@Override
	public int getPhotoniumBuffMana() {
		return BUFF_MANA;
	}

	@Override
	public int getAerialiteBuffMana() {
		return BUFF_MANA;
	}

	@Override
	public int getOrichalcosBuffMana() {
		return BUFF_MANA;
	}

	@Override
	public boolean isNight(Level level) {
		return true;
	}

	@Override
	public boolean isInCave(Level level, BlockPos pos) {
		return true;
	}

	@Override
	public boolean isDay(Level level) {
		return true;
	}

	@Override
	public boolean isOutCave(Level level, BlockPos pos) {
		return true;
	}

	public static Relic makeRelic(ItemStack stack) {
		return new RelicImpl(stack, prefix("main/" + LibAdvancementNames.RHEIN_KRAFT));
	}
}
