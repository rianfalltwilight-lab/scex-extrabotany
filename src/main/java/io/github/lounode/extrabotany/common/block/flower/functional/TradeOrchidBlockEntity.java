package io.github.lounode.extrabotany.common.block.flower.functional;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import io.github.lounode.extrabotany.common.block.flower.ExtraFunctionalFlowerBlockEntity;
import vazkii.botania.api.block_entity.RadiusDescriptor;

import io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks;
import io.github.lounode.extrabotany.common.brew.ExtraBotanyMobEffects;
import io.github.lounode.extrabotany.xplat.ExtraBotanyConfig;

import java.util.List;

/**
 * 商友兰。它消耗魔力，给范围里的村民打折效果。有这个效果的村民在交易时降价。
 * Trade Orchid. It uses mana to give the Discount effect to the villagers in the range. A villager with this effect decreases its prices in a trade.
 *
 * <p>效果在花不再补充后短时间内自然到期，避免移除相邻花提供的折扣。红石信号让花停止工作。
 * <p>The short effect expires when no flower refreshes it, so neighbouring flowers do not remove each other's discounts. A redstone signal stops the flower.
 *
 * <p>来源：重制版（Lounode）。原版没有这朵花。
 * <p>Source: Reburn (Lounode). The original does not have this flower.
 */
public class TradeOrchidBlockEntity extends ExtraFunctionalFlowerBlockEntity {

	private static final int RANGE = 8;
	public static final int MAX_MANA = 10000;
	public static final int MANA_PER_USE = 1000;
	public static final int COOLDOWN = 2 * 20;
	public static final double DISCOUNT_RATE = 0.50D;

	public TradeOrchidBlockEntity(BlockPos pos, BlockState state) {
		super(ExtrabotanyFlowerBlocks.TRADE_ORCHID, pos, state);
	}

	@Override
	public void tickFlower() {
		super.tickFlower();
		if (getLevel().isClientSide) {
			return;
		}
		// 这朵花没有自己的数据。魔力变化时，植物魔法的基类会保存并同步它。
		// This flower has no data of its own. When the mana changes, the base class of Botania saves and synchronizes it.
		applyDiscounts();
	}

	private void applyDiscounts() {
		if (isPowered()) {
			return;
		}
		if (ticksExisted % Math.max(1, getCooldown()) != 0) {
			return;
		}
		if (getMana() < getManaPerUse()) {
			return;
		}

		List<Villager> villagers = getLevel()
				.getEntitiesOfClass(Villager.class, ((RadiusDescriptor.Rectangle) getRadius()).aabb()).stream()
				.filter(villager -> !villager.isRemoved())
				.filter(LivingEntity::isAlive)
				.toList();

		MobEffectInstance discount = getNewEffect();
		if (discount == null) {
			// The config asks for no discount (a price factor of 1.0): nothing to give, no mana to use.
			return;
		}
		for (var villager : villagers) {
			if (getMana() < getManaPerUse()) {
				break;
			}
			if (!villager.addEffect(new MobEffectInstance(discount))) {
				continue;
			}

			addMana(-getManaPerUse());
		}

	}

	public int getCooldown() {
		return ExtraBotanyConfig.common().tradeOrchidCooldown();
	}

	public int getManaPerUse() {
		return ExtraBotanyConfig.common().tradeOrchidManaCost();
	}

	@Override
	public int getMaxMana() {
		return ExtraBotanyConfig.common().tradeOrchidMaxMana();
	}

	@Override
	public int getColor() {
		return 0x54eb89;
	}

	/**
	 * 给村民的折扣效果。配置要求不打折（价格系数 1.0）时返回 {@code null}。以前这时效果等级是 -1，被截成 0 级，结果还是打 1% 的折。
	 * The discount effect for the villagers. {@code null} when the config asks for no discount (a price factor of 1.0). Before, the amplifier was -1 in that case, which was cut to 0, so it still gave 1% off.
	 */
	@Nullable
	public MobEffectInstance getNewEffect() {
		int level = calcRequireLevel(ExtraBotanyConfig.common().tradeOrchidDiscountPercentage());
		if (level <= 0) {
			return null;
		}
		return new MobEffectInstance(
				ExtraBotanyMobEffects.DISCOUNT,
				ExtraBotanyConfig.common().tradeOrchidCooldown() + 20,
				level - 1,
				true,
				false
		);
	}

	private int calcRequireLevel(double percentage) {
		percentage = Math.max(0, Math.min(1, percentage));
		return (int) Math.round((1 - percentage) * 100);
	}

	@Override
	public @Nullable RadiusDescriptor getRadius() {
		return RadiusDescriptor.Rectangle.square(getEffectivePos(), RANGE);
	}
}
