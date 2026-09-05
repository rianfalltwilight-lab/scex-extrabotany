package io.github.lounode.extrabotany.common.block.flower.functional;

import net.minecraft.core.BlockPos;
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

public class MirrowtuniaBlockEntity extends ExtraFunctionalFlowerBlockEntity {

	public static final int RANGE = 13;
	public static final int MAX_MANA = 2000;

	public static final int EFFECT_COST = 100;
	public static final int COOLDOWN = 50;

	public MirrowtuniaBlockEntity(BlockPos pos, BlockState state) {
		super(ExtrabotanyFlowerBlocks.MIRROWTUNIA, pos, state);
	}

	@Override
	public void tickFlower() {
		super.tickFlower();
		if (getLevel().isClientSide()) {
			return;
		}
		if (ticksExisted % COOLDOWN != 0) {
			return;
		}
		if (getMana() < getEffectCost()) {
			return;
		}

		var players = getLevel().getEntitiesOfClass(Player.class, getAABB());

		for (var player : players) {
			if (getMana() >= getEffectCost()) {

				player.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.THIRROR, 6 * 20, 3));

				NatureOrbItem.clearHarmfulPotion(player);
				addMana(-getEffectCost());
				sync();
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
