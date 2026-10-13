package io.github.lounode.extrabotany.common.block.mana;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.common.block.BotaniaBlock;

import io.github.lounode.extrabotany.common.block.block_entity.ManaMachineBlockEntity;

import java.util.function.Supplier;

/**
 * 储存魔力的机器的方块：FE转换器和液态魔力转化器。魔力缓存器使用子类 {@link ManaBufferBlock}。
 * The block of the machines that hold mana: the Flux Manafield and the Mana Liquefaction Device. The Mana Buffer uses the subclass {@link ManaBufferBlock}.
 *
 * <p>方块本身没有逻辑。全部逻辑在方块实体里，并且只在服务端运行。
 * <p>The block itself has no logic. All logic is in the block entity and only runs on the server.
 *
 * <p>来源：ExtraMeteorP 1.12 {@code BlockManaGenerator} 和 {@code BlockManaLiquefaction}。
 * <p>Source: ExtraMeteorP 1.12 {@code BlockManaGenerator} and {@code BlockManaLiquefaction}.
 */
public class ManaMachineBlock<T extends ManaMachineBlockEntity> extends BotaniaBlock implements EntityBlock {
	private final Supplier<BlockEntityType<T>> type;

	public ManaMachineBlock(Properties properties, Supplier<BlockEntityType<T>> type) {
		super(properties);
		this.type = type;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return type.get().create(pos, state);
	}

	@Nullable
	@Override
	public <E extends BlockEntity> BlockEntityTicker<E> getTicker(Level level, BlockState state, BlockEntityType<E> givenType) {
		return level.isClientSide() ? null : createTickerHelper(givenType, type.get(),
				(tickLevel, pos, tickState, machine) -> machine.serverTick());
	}
}
