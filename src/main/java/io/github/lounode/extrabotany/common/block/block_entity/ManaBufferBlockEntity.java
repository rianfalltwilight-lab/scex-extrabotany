package io.github.lounode.extrabotany.common.block.block_entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.mana.ManaPool;
import vazkii.botania.api.mana.ManaReceiver;

/**
 * 魔力缓存器和量子魔力缓存器的方块实体。
 * The block entity of the Mana Buffer and of the Quantum Mana Buffer.
 *
 * <p>缓存器从四个水平方向和下方的魔力池、缓存器吸取魔力，并向上方的魔力池或缓存器输出魔力。
 * <p>A buffer takes mana from the mana pools and the buffers on its four horizontal sides and below it, and sends mana to the mana pool or the buffer above it.
 *
 * <p>魔力缓存器的容量是 6400 万（64 个魔力池），每 tick 和每个邻居传输 1000 点。量子魔力缓存器的容量是 10.24 亿，每 tick 传输 5000 点。
 * <p>The Mana Buffer holds 64 million mana (64 mana pools) and moves 1000 mana for each neighbour on each tick. The Quantum Mana Buffer holds 1024 million mana and moves 5000 mana on each tick.
 *
 * <p>和原版相同：魔力缓存器不主动和量子魔力缓存器交换魔力。量子魔力缓存器会主动和两种缓存器交换。
 * <p>The same as in the original: the Mana Buffer does not start a transfer with a Quantum Mana Buffer. The Quantum Mana Buffer starts transfers with the two types of buffer.
 *
 * <p>来源：ExtraMeteorP 1.12 {@code TileManaBuffer} 和 {@code TileQuantumManaBuffer}。魔力缓存器的速度来自 1.16 版（1000）。1.12 版的默认值是 400。
 * <p>Source: ExtraMeteorP 1.12 {@code TileManaBuffer} and {@code TileQuantumManaBuffer}. The speed of the Mana Buffer comes from the 1.16 version (1000). The default in the 1.12 version is 400.
 */
public class ManaBufferBlockEntity extends ManaMachineBlockEntity {
	/**
	 * 缓存器的等级。
	 * The tier of a buffer.
	 */
	public enum Tier {
		BUFFER(64_000_000, 1000),
		QUANTUM(1_024_000_000, 5000);

		private final int maxMana;
		private final int transferSpeed;

		Tier(int maxMana, int transferSpeed) {
			this.maxMana = maxMana;
			this.transferSpeed = transferSpeed;
		}
	}

	// 这个顺序和原版相同。它决定了吸取魔力的先后。
	// This sequence is the same as in the original. It gives the order in which the buffer takes mana.
	private static final Direction[] INPUT_SIDES = { Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.NORTH, Direction.DOWN };

	private final Tier tier;

	public ManaBufferBlockEntity(Tier tier, BlockPos pos, BlockState state) {
		super(tier == Tier.QUANTUM ? ExtraBotanyBlockEntities.QUANTUM_MANA_BUFFER : ExtraBotanyBlockEntities.MANA_BUFFER, pos, state);
		this.tier = tier;
	}

	public Tier getTier() {
		return tier;
	}

	@Override
	public int getMaxMana() {
		return tier.maxMana;
	}

	public int getTransferSpeed() {
		return tier.transferSpeed;
	}

	@Override
	protected void tickMachine(ServerLevel level) {
		int speed = tier.transferSpeed;
		for (Direction side : INPUT_SIDES) {
			if (isFull()) {
				break;
			}
			ManaReceiver source = findPartner(level, worldPosition.relative(side));
			if (source == null) {
				continue;
			}
			int amount = Math.min(speed, Math.min(source.getCurrentMana(), getAvailableSpaceForMana()));
			if (amount > 0) {
				source.receiveMana(-amount);
				receiveMana(amount);
			}
		}

		if (getCurrentMana() == 0) {
			return;
		}
		ManaReceiver target = findPartner(level, worldPosition.above());
		if (target == null) {
			return;
		}
		int space = target instanceof ManaPool pool
				? pool.getMaxMana() - pool.getCurrentMana()
				: ((ManaBufferBlockEntity) target).getAvailableSpaceForMana();
		int amount = Math.min(speed, Math.min(getCurrentMana(), space));
		if (amount > 0) {
			target.receiveMana(amount);
			receiveMana(-amount);
		}
	}

	/**
	 * 返回这个位置上可以交换魔力的魔力池或缓存器。没有时返回 {@code null}。
	 * Returns the mana pool or the buffer at this position that can exchange mana. Returns {@code null} when there is none.
	 */
	@Nullable
	private ManaReceiver findPartner(ServerLevel level, BlockPos pos) {
		BlockEntity blockEntity = level.getBlockEntity(pos);
		if (blockEntity instanceof ManaPool pool) {
			return pool;
		}
		if (blockEntity instanceof ManaBufferBlockEntity buffer && (tier == Tier.QUANTUM || buffer.tier == Tier.BUFFER)) {
			return buffer;
		}
		return null;
	}
}
