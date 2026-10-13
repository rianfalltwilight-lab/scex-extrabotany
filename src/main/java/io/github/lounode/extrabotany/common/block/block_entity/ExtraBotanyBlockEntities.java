package io.github.lounode.extrabotany.common.block.block_entity;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import io.github.lounode.extrabotany.common.lib.LibBlockNames;
import io.github.lounode.extrabotany.xplat.EXplatAbstractions;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import static io.github.lounode.extrabotany.common.block.ExtraBotanyBlocks.*;
import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class ExtraBotanyBlockEntities {
	private static final Map<ResourceLocation, BlockEntityType<?>> ALL = new HashMap<>();
	public static final BlockEntityType<CocoonOfDesireBlockEntity> COCOON_OF_DESIRE =
			type(prefix("cocoondesire"), CocoonOfDesireBlockEntity::new, cocoonOfDesire);
	public static final BlockEntityType<LivingrockBarrelBlockEntity> LIVINGROCK_BARREL =
			type(prefix("elfjar"), LivingrockBarrelBlockEntity::new, livingrockBarrel);
	public static final BlockEntityType<ManaBufferBlockEntity> MANA_BUFFER = type(prefix("manabuffer"),
			(pos, state) -> new ManaBufferBlockEntity(ManaBufferBlockEntity.Tier.BUFFER, pos, state), manaBuffer);
	public static final BlockEntityType<ManaBufferBlockEntity> QUANTUM_MANA_BUFFER = type(prefix("quantummanabuffer"),
			(pos, state) -> new ManaBufferBlockEntity(ManaBufferBlockEntity.Tier.QUANTUM, pos, state), quantumManaBuffer);
	public static final BlockEntityType<ManaGeneratorBlockEntity> MANA_GENERATOR =
			type(prefix("managenerator"), ManaGeneratorBlockEntity::new, manaGenerator);
	public static final BlockEntityType<ManaLiquefactionBlockEntity> MANA_LIQUEFACTION =
			type(prefix("manaliquefaction"), ManaLiquefactionBlockEntity::new, manaLiquefaction);

	public static final BlockEntityType<PedestalBlockEntity> PEDESTAL = type(prefix(LibBlockNames.PEDESTAL), PedestalBlockEntity::new,
			ALL_PEDESTALS
	);
	public static final BlockEntityType<ManaChargerBlockEntity> MANA_CHARGER =
			type(prefix(LibBlockNames.MANA_CHARGER), ManaChargerBlockEntity::new, manaCharger);
	public static final BlockEntityType<PowerFrameBlockEntity> POWER_FRAME =
			type(prefix(LibBlockNames.POWER_FRAME), PowerFrameBlockEntity::new, powerFrame);

	private static <T extends BlockEntity> BlockEntityType<T> type(ResourceLocation id, BiFunction<BlockPos, BlockState, T> func, Block... blocks) {
		var ret = EXplatAbstractions.INSTANCE.createBlockEntityType(func, blocks);
		var old = ALL.put(id, ret);
		if (old != null) {
			throw new IllegalArgumentException("Duplicate id " + id);
		}
		return ret;
	}

	public static void registerTiles(BiConsumer<BlockEntityType<?>, ResourceLocation> r) {
		for (var e : ALL.entrySet()) {
			r.accept(e.getValue(), e.getKey());
		}
	}
}
