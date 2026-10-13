package io.github.lounode.extrabotany.common.item.lens;

import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import vazkii.botania.api.internal.ManaBurst;
import vazkii.botania.api.mana.BurstProperties;
import vazkii.botania.common.item.lens.Lens;

import io.github.lounode.extrabotany.common.handler.DamageHandler;
import io.github.lounode.extrabotany.common.handler.DamageHandler.DamageKind;

import java.util.Objects;

/**
 * 魔力透镜：超导。魔力脉冲的容量变为 8 倍、速度变为 1.5 倍，但衰减也快得多。
 * Mana Lens: SUPERCONDUCTOR. The mana burst has 8 times the capacity and 1.5 times the speed, but it also loses mana much faster.
 *
 * <p>脉冲对路径上的生物造成穿透魔法伤害：玩家 25 点，其他生物 8 点。
 * <p>The burst applies piercing magic damage to living entities on its path: 25 to a player and 8 to other entities.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code LensSuperconductor}。重制版和之前的移植都没有实现伤害。
 * <p>Source: ExtraMeteorP 1.16 {@code LensSuperconductor}. Reburn and the earlier port did not implement the damage.
 */
public class SuperconductorLens extends Lens {
	private static final float PLAYER_DAMAGE = 25F;
	private static final float MOB_DAMAGE = 8F;

	@Override
	public void apply(ItemStack stack, BurstProperties props) {
		props.maxMana *= 8;
		props.motionModifier *= 1.5F;
		props.manaLossPerTick *= 16F;
		props.ticksBeforeManaLoss = (int) (props.ticksBeforeManaLoss * 0.8F);
		props.color = Objects.requireNonNull(ChatFormatting.BLUE.getColor());
	}

	@Override
	public void updateBurst(ManaBurst burst, ItemStack stack) {
		Projectile entity = burst.entity();
		// 假脉冲只用来计算路径，不能造成伤害。
		// A fake burst only calculates the path. It must not apply damage.
		if (entity.level().isClientSide() || burst.isFake()) {
			return;
		}
		AABB path = new AABB(
				entity.getX() - 0.5, entity.getY() - 0.5, entity.getZ() - 0.5,
				entity.xOld + 0.5, entity.yOld + 0.5, entity.zOld + 0.5).inflate(1);
		for (LivingEntity living : entity.level().getEntitiesOfClass(LivingEntity.class, path)) {
			float damage = living instanceof Player ? PLAYER_DAMAGE : MOB_DAMAGE;
			DamageHandler.INSTANCE.dmg(living, null, damage, DamageKind.MAGIC_PIERCING);
		}
	}
}
