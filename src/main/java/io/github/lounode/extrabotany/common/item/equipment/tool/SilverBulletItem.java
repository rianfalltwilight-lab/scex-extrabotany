package io.github.lounode.extrabotany.common.item.equipment.tool;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import vazkii.botania.api.mana.BasicLensItem;
import vazkii.botania.api.mana.BurstProperties;
import vazkii.botania.common.advancements.ManaBlasterTrigger;
import vazkii.botania.common.component.BotaniaDataComponents;
import vazkii.botania.common.entity.ManaBurstEntity;
import vazkii.botania.common.handler.BotaniaSounds;
import vazkii.botania.common.handler.EquipmentHandler;
import vazkii.botania.common.helper.DataComponentHelper;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.item.ManaBlasterItem;

import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.item.equipment.armor.shooting_guardian.ShootingGuardianArmorItem;

/**
 * 银翼射手。它是魔力脉冲枪的强化版：脉冲的魔力是 2 倍，速度是 1.5 倍。
 * Silver Bullet (the gun of the Shooting Guardian). It is a stronger Mana Blaster: the burst has 2 times the mana and 1.5 times the speed.
 *
 * <p>玩家穿齐银翼护卫套装时，射击不消耗魔力。
 * <p>When the player has the full Shooting Guardian armor set, a shot takes no mana.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code ItemSilverBullet}。
 * <p>Source: ExtraMeteorP 1.16 {@code ItemSilverBullet}.
 *
 * <p>和原版的差异：原版只把"不消耗魔力"写进了说明书。它的代码读取一个从不写入的 NBT 标记，所以魔力照常扣除。这里按说明书实现。
 * <p>Difference from the original: the original only gives "no mana cost" in the lexicon. Its code reads an NBT flag that is never written, so the mana is still taken. This port does what the lexicon says.
 */
public class SilverBulletItem extends ManaBlasterItem {
	private static final int MAX_MANA = 240;
	private static final int COLOR = 0x87CEFA;
	private static final int TICKS_BEFORE_MANA_LOSS = 80;
	private static final float MANA_LOSS_PER_TICK = 3F;
	private static final float MOTION_MODIFIER = 7.5F;
	private static final int COOLDOWN_TICKS = 30;

	public SilverBulletItem(Properties props) {
		super(props);
	}

	@Override
	public BurstProperties getBurstProps(Player player, ItemStack stack, boolean request, InteractionHand hand) {
		BurstProperties props = new BurstProperties(MAX_MANA, TICKS_BEFORE_MANA_LOSS, MANA_LOSS_PER_TICK, 0F, MOTION_MODIFIER, COLOR);
		ItemStack lens = getLens(stack);
		if (!lens.isEmpty() && lens.getItem() instanceof BasicLensItem lensItem) {
			lensItem.apply(lens, props, player.level());
		}
		return props;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		// 潜行时切换弹夹里的透镜。这一步和魔力脉冲枪相同。
		// A sneak-use changes the lens in the clip. This step is the same as for the Mana Blaster.
		boolean cyclesClip = player.isSecondaryUseActive() && hasClip(stack);
		if (cyclesClip || !hasArmorSet(player)) {
			return super.use(level, player, hand);
		}
		return shootWithoutMana(level, player, stack, hand);
	}

	private static boolean hasArmorSet(Player player) {
		return ((ShootingGuardianArmorItem) ExtraBotanyItems.shootingGuardianHelm).hasArmorSet(player);
	}

	/**
	 * 发射一个不消耗魔力的脉冲。冷却存放在物品的冷却组件里，所以冷却条和魔力脉冲枪的相同。
	 * Shoots one burst that takes no mana. The cooldown is kept in the cooldown component of the item, so the cooldown bar is the same as for the Mana Blaster.
	 */
	private InteractionResultHolder<ItemStack> shootWithoutMana(Level level, Player player, ItemStack stack, InteractionHand hand) {
		if (stack.getOrDefault(BotaniaDataComponents.COOLDOWN, 0) > 0) {
			return InteractionResultHolder.pass(stack);
		}

		BurstProperties props = getBurstProps(player, stack, false, hand);
		ManaBurstEntity burst = new ManaBurstEntity(player);
		burst.setSourceLens(getLens(stack));
		burst.setColor(props.color);
		burst.setMana(props.maxMana);
		burst.setStartingMana(props.maxMana);
		burst.setMinManaLoss(props.ticksBeforeManaLoss);
		burst.setManaLossPerTick(props.manaLossPerTick);
		burst.setGravity(props.gravity);
		burst.setDeltaMovement(burst.getDeltaMovement().scale(props.motionModifier));

		if (!level.isClientSide()) {
			level.playSound(null, player.getX(), player.getY(), player.getZ(),
					BotaniaSounds.MANA_BLASTER, SoundSource.PLAYERS, 1F, 1F);
			level.addFreshEntity(burst);
			if (player instanceof ServerPlayer serverPlayer) {
				ManaBlasterTrigger.INSTANCE.trigger(serverPlayer, stack);
			}
			DataComponentHelper.setIntNonZero(stack, BotaniaDataComponents.COOLDOWN, cooldownFor(player));
		} else if (!EquipmentHandler.getAllWorn(player).hasAnyMatching(worn -> worn.is(BotaniaItems.TECTONIC_GIRDLE))) {
			// 后坐力只在客户端施加。构造腰带可以抵消后坐力。这条规则和魔力脉冲枪相同。
			// The recoil applies only on the client. The Tectonic Girdle cancels the recoil. This rule is the same as for the Mana Blaster.
			player.setDeltaMovement(player.getDeltaMovement().subtract(burst.getDeltaMovement().multiply(0.1, 0.3, 0.1)));
		}
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
	}

	/**
	 * 急迫效果缩短射击间隔，规则和魔力脉冲枪相同。
	 * Haste decreases the time between shots. The rule is the same as for the Mana Blaster.
	 */
	private static int cooldownFor(Player player) {
		MobEffectInstance haste = player.getEffect(MobEffects.DIG_SPEED);
		return haste == null ? COOLDOWN_TICKS : Math.max(2, COOLDOWN_TICKS - (haste.getAmplifier() + 1) * 8);
	}
}
