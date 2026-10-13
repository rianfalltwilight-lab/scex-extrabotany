package io.github.lounode.extrabotany.common.item.equipment.tool;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.block.block_entity.mana.ManaPoolBlockEntity;

import io.github.lounode.extrabotany.common.brew.ExtraBotanyMobEffects;

/**
 * 魔术手指。潜行并对着魔力池使用时，把玩家身上最多 10000 点魔力注入魔力池。用它攻击生物时，给目标 3 秒的治疗反转效果。
 * Magic Finger. A sneak-use on a mana pool moves a maximum of 10000 mana from the items of the player into the pool. An attack with it gives the target the Heal Reverse effect for 3 seconds.
 */
public class MagicFingerItem extends Item {
	public static final int DENY_HEAL_TIME = 3;
	public static final int SUPPORT_MANA_MAX = 10_000;

	public MagicFingerItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Player player = context.getPlayer();
		Level world = context.getLevel();
		if (player == null || !player.isShiftKeyDown()) {
			return InteractionResult.PASS;
		}

		BlockPos pos = context.getClickedPos();
		BlockEntity tile = world.getBlockEntity(pos);

		if (tile instanceof ManaPoolBlockEntity pool) {
			if (pool.isFull()) {
				return InteractionResult.FAIL;
			}

			int mana = Math.min(getSupportManaMax(), pool.getMaxMana() - pool.getCurrentMana());
			if (ManaItemHandler.instance().requestManaExact(player.getItemInHand(context.getHand()), player, mana, true)) {
				pool.receiveMana(mana);
				return InteractionResult.sidedSuccess(world.isClientSide());
			}
		}
		return InteractionResult.PASS;
	}

	public int getSupportManaMax() {
		return SUPPORT_MANA_MAX;
	}

	public int getDenyHealTime() {
		return DENY_HEAL_TIME * 20;
	}

	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		if (!attacker.level().isClientSide()) {
			target.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.HEAL_REVERSE, getDenyHealTime()), target);
		}
		return true;
	}
}
