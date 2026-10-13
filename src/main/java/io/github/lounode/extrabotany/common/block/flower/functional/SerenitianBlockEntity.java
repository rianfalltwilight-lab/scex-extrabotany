package io.github.lounode.extrabotany.common.block.flower.functional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.block_entity.GeneratingFlowerBlockEntity;
import vazkii.botania.api.block_entity.RadiusDescriptor;
import vazkii.botania.api.block_entity.SpecialFlowerBlockEntity;
import vazkii.botania.common.block.block_entity.flower.generating.HydroangeasBlockEntity;

import io.github.lounode.extrabotany.api.block.PassiveFlower;
import io.github.lounode.extrabotany.common.block.flower.ExtraFunctionalFlowerBlockEntity;
import io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks;
import io.github.lounode.extrabotany.common.lib.LibMisc;
import io.github.lounode.extrabotany.mixin.accessor.HydroangeasBlockEntityAccessor;
import io.github.lounode.extrabotany.xplat.ExtraBotanyConfig;

/**
 * 永寂龙胆。它让周围的被动产能花不会枯萎。它不使用魔力。红石信号能让它停止工作。
 * Serenitian. It stops the decay of the passive generating flowers around it. It uses no mana. A redstone signal stops it.
 *
 * <p>来源：重制版（Lounode），它对应 ExtraMeteorP 1.16 的 {@code SubTileSerenitian}。
 * <p>Source: Reburn (Lounode). It corresponds to {@code SubTileSerenitian} of ExtraMeteorP 1.16.
 *
 * <p>性能：以前这朵花每秒把范围里每一朵花的全部数据写成 NBT 再读回去，只为了把一个计数器清零。现在它直接清零本模组的花和植物魔法的水绣球。别的模组的花仍然用 NBT 的方法。
 * <p>Performance: before, on each second this flower wrote the full data of each flower in the range to NBT and read it back, only to set one counter to zero. Now it sets the counter directly for the flowers of this mod and for the Hydroangeas of Botania. The flowers of other mods continue to use the NBT method.
 */
public class SerenitianBlockEntity extends ExtraFunctionalFlowerBlockEntity {
	private static final int COOLDOWN = 20;

	public static final int RANGE = 3;

	public SerenitianBlockEntity(BlockPos pos, BlockState state) {
		super(ExtrabotanyFlowerBlocks.SERENITIAN, pos, state);
	}

	@Override
	public void tickFlower() {
		super.tickFlower();
		if (getLevel().isClientSide() || isPowered()) {
			return;
		}
		if (ticksExisted % COOLDOWN != 0) {
			return;
		}

		int range = getRange();
		BlockPos base = getEffectivePos();
		for (BlockPos pos : BlockPos.betweenClosed(base.offset(-range, 0, -range), base.offset(range, 0, range))) {
			if (getLevel().getBlockEntity(pos) instanceof SpecialFlowerBlockEntity flower) {
				resetDecay(flower);
			}
		}
	}

	private void resetDecay(SpecialFlowerBlockEntity flower) {
		if (flower instanceof PassiveFlower passive) {
			if (passive.getPassiveDecayTicks() != 0) {
				passive.setPassiveDecayTicks(0);
				flower.setChanged();
			}
			return;
		}
		if (flower instanceof HydroangeasBlockEntity) {
			((HydroangeasBlockEntityAccessor) flower).extrabotany_setPassiveDecayTicks(0);
			flower.setChanged();
			return;
		}
		if (!(flower instanceof GeneratingFlowerBlockEntity) || isKnownMod(flower.getType())) {
			return;
		}
		// 别的模组的被动花：只能通过它的存档数据来清零计数器。
		// A passive flower of a different mod: the counter can only be set to zero through its save data.
		HolderLookup.Provider registries = getLevel().registryAccess();
		CompoundTag tag = flower.saveCustomOnly(registries);
		if (tag.getInt(PassiveFlower.TAG_PASSIVE_DECAY_TICKS) != 0) {
			tag.putInt(PassiveFlower.TAG_PASSIVE_DECAY_TICKS, 0);
			flower.loadCustomOnly(tag, registries);
			flower.setChanged();
		}
	}

	/**
	 * 这种花是否属于植物魔法或者本模组。这两个模组的被动花在前面已经处理了。
	 * Tells if this flower type is from Botania or from this mod. The passive flowers of these two mods are already processed before this check.
	 */
	private static boolean isKnownMod(BlockEntityType<?> type) {
		ResourceLocation id = BlockEntityType.getKey(type);
		return id == null || BotaniaAPI.MODID.equals(id.getNamespace()) || LibMisc.MOD_ID.equals(id.getNamespace());
	}

	@Override
	public int getMaxMana() {
		return 0;
	}

	@Override
	public int getColor() {
		return 0x8A68BF;
	}

	@Override
	public @Nullable RadiusDescriptor getRadius() {
		return RadiusDescriptor.Rectangle.square(getEffectivePos(), getRange());
	}

	public int getRange() {
		return ExtraBotanyConfig.common().serenitianRange();
	}
}
