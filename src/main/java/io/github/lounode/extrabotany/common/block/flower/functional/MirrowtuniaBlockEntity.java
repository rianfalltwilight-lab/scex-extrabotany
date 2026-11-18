package io.github.lounode.extrabotany.common.block.flower.functional;

import net.minecraft.core.BlockPos;
import io.github.lounode.extrabotany.common.block.flower.NatureCatalysis;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import org.jetbrains.annotations.Nullable;

import io.github.lounode.extrabotany.common.block.flower.ExtraFunctionalFlowerBlockEntity;
import vazkii.botania.api.block_entity.RadiusDescriptor;

import io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks;
import io.github.lounode.extrabotany.common.brew.ExtraBotanyMobEffects;
import io.github.lounode.extrabotany.common.item.equipment.bauble.NatureOrbItem;
import io.github.lounode.extrabotany.xplat.ExtraBotanyConfig;

/**
 * 镜姬。每 {@value #COOLDOWN} tick，它给范围里的每个玩家镜棘效果，并移除玩家身上的负面效果。每个玩家消耗一份魔力。红石信号让它停止工作。
 * Mirrowtunia. One time for each {@value #COOLDOWN} ticks, it gives the Thirror effect to each player in the range and removes the harmful effects of the player. Each player uses one unit of mana. A redstone signal stops it.
 *
 * <p>来源：ExtraMeteorP 1.12 的 {@code SubTileMirrortunia}：每 50 tick 给范围里每个玩家 6 秒的反射 IV，每人 90 魔力。镜棘效果就是 1.12 的反射效果（反弹 1 / (6 − 等级) 的伤害）。有自然宝珠催化（见 {@link NatureCatalysis}）时是反射 V。
 * <p>Source: {@code SubTileMirrortunia} of ExtraMeteorP 1.12: each 50 ticks it gives each player in the range Reflect IV for 6 seconds, 90 mana for each player. The Thirror effect is the Reflect effect of 1.12 (it returns 1 / (6 − level) of the damage). With the Nature Orb catalysis (see {@link NatureCatalysis}) it is Reflect V.
 */
public class MirrowtuniaBlockEntity extends ExtraFunctionalFlowerBlockEntity {

	public static final int RANGE = 13;
	public static final int MAX_MANA = 2000;

	public static final int EFFECT_COST = 90;
	public static final int COOLDOWN = 50;
	/** 1.12 的催化：反射 V 代替 IV，每 20 tick 扣宝珠 20 能量。 / The catalysis of 1.12: Reflect V in place of IV, 20 orb energy each 20 ticks. */
	public static final int CATALYSIS_RATE = 20;
	private boolean catalysed;

	public MirrowtuniaBlockEntity(BlockPos pos, BlockState state) {
		super(ExtrabotanyFlowerBlocks.MIRROWTUNIA, pos, state);
	}

	@Override
	public void tickFlower() {
		super.tickFlower();
		if (getLevel().isClientSide() || isPowered()) {
			return;
		}
		catalysed = NatureCatalysis.update(this, ticksExisted, CATALYSIS_RATE, catalysed);
		if (ticksExisted % COOLDOWN != 0) {
			return;
		}
		if (getMana() < getEffectCost()) {
			return;
		}

		var players = getLevel().getEntitiesOfClass(Player.class, getAABB());

		for (var player : players) {
			if (getMana() >= getEffectCost()) {

				player.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.THIRROR, 6 * 20, catalysed ? 4 : 3));

				NatureOrbItem.clearHarmfulPotion(player);
				addMana(-getEffectCost());
				syncThrottled();
			}
		}
	}

	public int getEffectCost() {
		return ExtraBotanyConfig.common().mirrowtuniaEffectCost();
	}

	public AABB getAABB() {
		return new AABB(getEffectivePos()).inflate(RANGE);
	}

	@Override
	public int getMaxMana() {
		return ExtraBotanyConfig.common().mirrowtuniaMaxMana();
	}

	@Override
	public int getColor() {
		return 0X4169E1;
	}

	@Override
	public @Nullable RadiusDescriptor getRadius() {
		return RadiusDescriptor.Rectangle.square(getEffectivePos(), RANGE);
	}
}
