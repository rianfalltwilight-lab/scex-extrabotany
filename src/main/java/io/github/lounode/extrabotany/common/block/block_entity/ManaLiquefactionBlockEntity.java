package io.github.lounode.extrabotany.common.block.block_entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

import io.github.lounode.extrabotany.common.fluid.ExtraBotanyFluids;
import io.github.lounode.extrabotany.xplat.ExtraBotanyConfig;

/**
 * 液态魔力转化器的方块实体。它在液态魔力和魔力之间转换。
 * The block entity of the Mana Liquefaction Device. It converts between Fluided Mana and mana.
 *
 * <p>没有红石信号时：机器从旁边的液体容器抽取液态魔力，把它变成魔力，并向旁边的魔力发射器输出魔力。
 * <p>Without a redstone signal: the machine drains Fluided Mana from the adjacent tanks, changes it into mana, and sends the mana to the adjacent Mana Spreaders.
 *
 * <p>有红石信号时：机器把自己的魔力变成液态魔力，并注入旁边的液体容器。
 * <p>With a redstone signal: the machine changes its mana into Fluided Mana and puts it into the adjacent tanks.
 *
 * <p>机器内部有一个"能量"储量，它是液体和魔力之间的中间量。默认配置下，1 mB 液态魔力是 1 点能量，1 点能量是 1000 点魔力。
 * <p>The machine has an internal store of "energy", which is the intermediate quantity between the fluid and the mana. With the default configuration, 1 mB of Fluided Mana is 1 energy, and 1 energy is 1000 mana.
 *
 * <p>数据存在 {@code mana} 和 {@code energy} 键里。旧存档使用相同的键名。
 * <p>The data is in the keys {@code mana} and {@code energy}. Old saves use the same keys.
 *
 * <p>来源：ExtraMeteorP 1.12 {@code TileManaLiquefaction}。
 * <p>Source: ExtraMeteorP 1.12 {@code TileManaLiquefaction}.
 *
 * <p>和原版的差异：
 * <p>Differences from the original:
 * <ul>
 * <li>每次转换按最小的整数份进行，所以没有资源凭空出现或消失。原版里能量可以变成负数，容器装不下的液体会消失。每 tick 的最大速度和原版相同。
 * Each conversion is done in the smallest whole lots, thus no resource comes from nothing and no resource is lost. In the original the energy can become negative, and the fluid that does not fit into a tank is lost. The maximum speed for each tick is the same as in the original.</li>
 * <li>能量储量的上限是 {@value #MAX_ENERGY}。原版定义了这个上限，但是没有使用它。
 * The limit of the energy store is {@value #MAX_ENERGY}. The original defines this limit but does not use it.</li>
 * </ul>
 *
 * <p>性能：邻居的液体容器通过 {@link BlockCapabilityCache} 缓存。
 * <p>Performance: {@link BlockCapabilityCache} keeps the tanks of the neighbours.
 */
public class ManaLiquefactionBlockEntity extends ManaMachineBlockEntity {
	public static final int MAX_ENERGY = 16000;

	private static final String TAG_ENERGY = "energy";

	private int energy;
	@SuppressWarnings("unchecked")
	private final BlockCapabilityCache<IFluidHandler, Direction>[] neighbours = new BlockCapabilityCache[ALL_SIDES.length];

	public ManaLiquefactionBlockEntity(BlockPos pos, BlockState state) {
		super(ExtraBotanyBlockEntities.MANA_LIQUEFACTION, pos, state);
	}

	@Override
	public int getMaxMana() {
		return ExtraBotanyConfig.common().manaLiquefactionMaxMana();
	}

	public int getEnergy() {
		return energy;
	}

	@Override
	public Component getHudDetail() {
		return Component.translatable("message.extrabotany.mana_liquefaction.energy", energy, MAX_ENERGY);
	}

	private void addEnergy(int amount) {
		if (amount != 0) {
			energy += amount;
			markChanged();
		}
	}

	@Override
	protected void tickMachine(ServerLevel level) {
		ExtraBotanyConfig.ConfigAccess config = ExtraBotanyConfig.common();
		if (!config.manaLiquefactionEnabled()) {
			return;
		}
		boolean powered = level.hasNeighborSignal(worldPosition);
		int transferSpeed = config.manaGeneratorTransferSpeed();
		for (Direction side : ALL_SIDES) {
			IFluidHandler tank = getNeighbour(level, side).getCapability();
			if (tank != null) {
				if (powered) {
					fillTank(tank, Ratio.of(config.manaLiquefactionStoragePump(), config.manaLiquefactionStoragePumpContainer()));
				} else if (!isFull()) {
					drainTank(tank, Ratio.of(config.manaLiquefactionStorageDrain(), config.manaLiquefactionStorageDrainContainer()));
				}
			}
			if (!powered) {
				pushManaToSpreader(worldPosition.relative(side), transferSpeed);
			}
		}

		if (powered) {
			Ratio ratio = Ratio.of(config.manaLiquefactionEnergyGain(), config.manaLiquefactionManaGive());
			int lots = Math.min(ratio.maxLots, Math.min((MAX_ENERGY - energy) / ratio.energy, getCurrentMana() / ratio.other));
			if (lots > 0) {
				receiveMana(-lots * ratio.other);
				addEnergy(lots * ratio.energy);
			}
		} else {
			Ratio ratio = Ratio.of(config.manaLiquefactionEnergyLoss(), config.manaLiquefactionManaReceive());
			int lots = Math.min(ratio.maxLots, Math.min(energy / ratio.energy, getAvailableSpaceForMana() / ratio.other));
			if (lots > 0) {
				addEnergy(-lots * ratio.energy);
				receiveMana(lots * ratio.other);
			}
		}
	}

	/**
	 * 从容器抽取液态魔力，并把它变成能量。
	 * Drains Fluided Mana from the tank and changes it into energy.
	 */
	private void drainTank(IFluidHandler tank, Ratio ratio) {
		int lots = Math.min(ratio.maxLots, (MAX_ENERGY - energy) / ratio.energy);
		if (lots <= 0) {
			return;
		}
		int available = tank.drain(new FluidStack(ExtraBotanyFluids.MANA, lots * ratio.other), FluidAction.SIMULATE).getAmount();
		lots = Math.min(lots, available / ratio.other);
		if (lots <= 0) {
			return;
		}
		int drained = tank.drain(new FluidStack(ExtraBotanyFluids.MANA, lots * ratio.other), FluidAction.EXECUTE).getAmount();
		addEnergy(drained / ratio.other * ratio.energy);
	}

	/**
	 * 把能量变成液态魔力，并注入容器。
	 * Changes energy into Fluided Mana and puts it into the tank.
	 */
	private void fillTank(IFluidHandler tank, Ratio ratio) {
		int lots = Math.min(ratio.maxLots, energy / ratio.energy);
		if (lots <= 0) {
			return;
		}
		int accepted = tank.fill(new FluidStack(ExtraBotanyFluids.MANA, lots * ratio.other), FluidAction.SIMULATE);
		lots = Math.min(lots, accepted / ratio.other);
		if (lots <= 0) {
			return;
		}
		int filled = tank.fill(new FluidStack(ExtraBotanyFluids.MANA, lots * ratio.other), FluidAction.EXECUTE);
		addEnergy(-(filled / ratio.other * ratio.energy));
	}

	private BlockCapabilityCache<IFluidHandler, Direction> getNeighbour(ServerLevel level, Direction side) {
		BlockCapabilityCache<IFluidHandler, Direction> cache = neighbours[side.ordinal()];
		if (cache == null) {
			cache = BlockCapabilityCache.create(Capabilities.FluidHandler.BLOCK, level, worldPosition.relative(side), side.getOpposite(),
					() -> !isRemoved(), () -> {});
			neighbours[side.ordinal()] = cache;
		}
		return cache;
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		tag.putInt(TAG_ENERGY, energy);
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		energy = Math.clamp(tag.getInt(TAG_ENERGY), 0, MAX_ENERGY);
	}

	/**
	 * 一个转换比例，约分到最小的整数份。
	 * A conversion ratio, reduced to the smallest whole lots.
	 *
	 * <p>例子：配置是"2 点能量换 2000 点魔力"。一份就是 1 点能量换 1000 点魔力，每 tick 最多 2 份。
	 * <p>Example: the configuration is "2 energy for 2000 mana". Then one lot is 1 energy for 1000 mana, with a maximum of 2 lots on each tick.
	 *
	 * @param energy  一份里的能量 / the energy in one lot
	 * @param other   一份里的另一种资源（魔力或者液体 mB） / the other resource in one lot (mana, or mB of fluid)
	 * @param maxLots 每 tick 最多的份数 / the maximum number of lots on each tick
	 */
	private record Ratio(int energy, int other, int maxLots) {
		static Ratio of(int energyRate, int otherRate) {
			int divisor = greatestCommonDivisor(energyRate, otherRate);
			return new Ratio(energyRate / divisor, otherRate / divisor, divisor);
		}

		private static int greatestCommonDivisor(int a, int b) {
			while (b != 0) {
				int remainder = a % b;
				a = b;
				b = remainder;
			}
			return a;
		}
	}
}
