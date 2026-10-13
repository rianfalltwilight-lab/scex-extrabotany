package io.github.lounode.extrabotany.common.item.equipment.bauble;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.handler.EquipmentHandler;
import vazkii.botania.common.item.equipment.bauble.BaubleItem;

import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.network.clientbound.ItemActivationPacket;
import io.github.lounode.extrabotany.xplat.EXplatAbstractions;

/**
 * 薯片。佩戴者受到致命伤害时，它消耗 3000 点魔力并阻止死亡。
 * Potato Chips. When the wearer takes fatal damage, the bauble uses 3000 mana and prevents the death.
 *
 * <p>触发后有冷却时间：普通情况 30 秒，被 Boss 击杀时 10 分钟。
 * <p>A cooldown follows: 30 seconds in the usual case, and 10 minutes when a boss kills the wearer.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code ItemPotatoChips}。
 * <p>Source: ExtraMeteorP 1.16 {@code ItemPotatoChips}.
 */
public class PotatoChipsItem extends BaubleItem {
	public static final int MANA_PER_RESCUE = 3000;
	private static final int COOLDOWN_TICKS = 600;
	private static final int BOSS_COOLDOWN_TICKS = 12000;
	private static final float HEALTH_AFTER_RESCUE = 5.0F;

	public PotatoChipsItem(Properties props) {
		super(props);
	}

	/**
	 * 处理玩家死亡事件。事件必须以最低优先级注册，这样其他保命道具先生效。
	 * Handles the death event of a player. The listener must have the lowest priority, so that other life-saving items apply first.
	 */
	public static void onPlayerDeath(LivingDeathEvent event) {
		if (event.isCanceled() || !(event.getEntity() instanceof Player player) || player.level().isClientSide()) {
			return;
		}
		ItemStack chips = EquipmentHandler.findOrEmpty(ExtraBotanyItems.potatoChips, player);
		if (chips.isEmpty() || player.getCooldowns().isOnCooldown(chips.getItem())) {
			return;
		}
		if (!ManaItemHandler.instance().requestManaExactForTool(chips, player, MANA_PER_RESCUE, true)) {
			return;
		}

		event.setCanceled(true);
		player.setHealth(HEALTH_AFTER_RESCUE);
		player.removeAllEffects();
		player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 900, 1));
		player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1));
		player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 800, 0));

		Entity killer = event.getSource().getEntity();
		boolean killedByBoss = killer != null && killer.getType().is(Tags.EntityTypes.BOSSES);
		player.getCooldowns().addCooldown(chips.getItem(), killedByBoss ? BOSS_COOLDOWN_TICKS : COOLDOWN_TICKS);

		player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.TOTEM_USE, player.getSoundSource(), 1.0F, 1.0F);
		if (player instanceof ServerPlayer serverPlayer) {
			EXplatAbstractions.INSTANCE.sendToPlayer(serverPlayer, new ItemActivationPacket(new ItemStack(chips.getItem())));
		}
	}

	@Override
	public boolean canEquip(ItemStack stack, LivingEntity entity) {
		return EquipmentHandler.findOrEmpty(this, entity).isEmpty();
	}
}
