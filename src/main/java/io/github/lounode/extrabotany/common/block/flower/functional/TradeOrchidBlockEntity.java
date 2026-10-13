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
import java.util.HashSet;

/**
 * 商友兰。它消耗魔力，给范围里的村民打折效果。有这个效果的村民在交易时降价。
 * Trade Orchid. It uses mana to give the Discount effect to the villagers in the range. A villager with this effect decreases its prices in a trade.
 *
 * <p>离开范围的村民立刻失去这个效果。花被移除时，范围里的村民也失去这个效果。红石信号让花停止工作。
 * <p>A villager that goes out of the range loses the effect immediately. When the flower is removed, the villagers in the range also lose the effect. A redstone signal stops the flower.
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
		var nearbyVillagers = new HashSet<>(villagers);
		List<Villager> farVillagers = getLevel()
				.getEntitiesOfClass(Villager.class, ((RadiusDescriptor.Rectangle) getRadius()).aabb().inflate(5)).stream()
				.filter(villager -> !villager.isRemoved())
				.filter(LivingEntity::isAlive)
				.filter(villager -> !nearbyVillagers.contains(villager))
				.toList();

		for (var villager : villagers) {
			if (getMana() < getManaPerUse()) {
				break;
			}
			if (!villager.addEffect(getNewEffect())) {
				continue;
			}

			addMana(-getManaPerUse());
		}

		for (var villager : farVillagers) {
			villager.removeEffect(ExtraBotanyMobEffects.DISCOUNT);
		}
	}

	@Override
	public void setRemoved() {
		if (getLevel() != null && !getLevel().isClientSide()) {
			List<Villager> villagers = getLevel().getEntitiesOfClass(Villager.class, ((RadiusDescriptor.Rectangle) getRadius()).aabb());
			for (var villager : villagers) {
				villager.removeEffect(ExtraBotanyMobEffects.DISCOUNT);
			}
		}
		super.setRemoved();
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

	public MobEffectInstance getNewEffect() {
		return new MobEffectInstance(
				ExtraBotanyMobEffects.DISCOUNT,
				ExtraBotanyConfig.common().tradeOrchidCooldown() + 20,
				calcRequireLevel(ExtraBotanyConfig.common().tradeOrchidDiscountPercentage()) - 1,
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
