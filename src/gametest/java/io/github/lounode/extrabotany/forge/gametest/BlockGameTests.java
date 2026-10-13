package io.github.lounode.extrabotany.forge.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import vazkii.botania.api.block.PetalApothecary;
import vazkii.botania.api.mana.ManaReceiver;
import vazkii.botania.api.mana.spark.ManaSparkAttachable;
import vazkii.botania.api.neoforge.BotaniaNeoForgeCapabilities;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.block.block_entity.mana.ManaPoolBlockEntity;
import vazkii.botania.common.block.block_entity.mana.ManaSpreaderBlockEntity;

import io.github.lounode.extrabotany.common.block.ExtraBotanyBlocks;
import io.github.lounode.extrabotany.common.block.block_entity.CocoonOfDesireBlockEntity;
import io.github.lounode.extrabotany.common.block.block_entity.LivingrockBarrelBlockEntity;
import io.github.lounode.extrabotany.common.block.block_entity.ManaBufferBlockEntity;
import io.github.lounode.extrabotany.common.block.block_entity.ManaGeneratorBlockEntity;
import io.github.lounode.extrabotany.common.block.block_entity.ManaLiquefactionBlockEntity;
import io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks;
import io.github.lounode.extrabotany.common.block.flower.functional.StardustLotusBlockEntity;
import io.github.lounode.extrabotany.common.fluid.ExtraBotanyFluids;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

import java.util.List;

/**
 * 方块的回归测试：魔力缓存器、FE转换器、液态魔力转化器、活石桶、不随想之茧、星尘莲和液态魔力。
 * Regression tests for the blocks: the Mana Buffer, the Flux Manafield, the Mana Liquefaction Device, the Livingrock Barrel, the Cocoon of Desire, the Stardust Lotus and Fluided Mana.
 */
@GameTestHolder("extrabotany")
@PrefixGameTestTemplate(false)
public final class BlockGameTests {
	private static final BlockPos CENTER = new BlockPos(2, 2, 2);

	private BlockGameTests() {}

	/**
	 * 放置一个方块，并返回它的方块实体。
	 * Puts down a block and returns its block entity.
	 */
	private static <T extends BlockEntity> T place(GameTestHelper helper, BlockPos pos, Block block, Class<T> type) {
		helper.setBlock(pos, block);
		return type.cast(helper.getLevel().getBlockEntity(helper.absolutePos(pos)));
	}

	private static ItemEntity dropItem(GameTestHelper helper, BlockPos pos, double yOffset, ItemStack stack) {
		Vec3 position = Vec3.atBottomCenterOf(helper.absolutePos(pos)).add(0, yOffset, 0);
		ItemEntity entity = new ItemEntity(helper.getLevel(), position.x, position.y, position.z, stack);
		entity.setDeltaMovement(Vec3.ZERO);
		helper.getLevel().addFreshEntity(entity);
		return entity;
	}

	private static BlockHitResult hit(GameTestHelper helper, BlockPos pos) {
		BlockPos absolute = helper.absolutePos(pos);
		return new BlockHitResult(Vec3.atCenterOf(absolute), Direction.UP, absolute, false);
	}

	// ---------------------------------------------------------------------
	// 魔力缓存器 / Mana Buffer
	// ---------------------------------------------------------------------

	/**
	 * 缓存器从下方吸取魔力并向上方输出，每 tick 1000 点。存档保留魔力，魔力不会超过容量。
	 * A buffer takes mana from below and sends it up, 1000 mana on each tick. The save keeps the mana, and the mana cannot be more than the capacity.
	 */
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void manaBufferMovesManaUpwards(GameTestHelper helper) {
		ManaBufferBlockEntity bottom = place(helper, CENTER.below(), ExtraBotanyBlocks.manaBuffer, ManaBufferBlockEntity.class);
		ManaBufferBlockEntity middle = place(helper, CENTER, ExtraBotanyBlocks.manaBuffer, ManaBufferBlockEntity.class);
		ManaBufferBlockEntity top = place(helper, CENTER.above(), ExtraBotanyBlocks.manaBuffer, ManaBufferBlockEntity.class);
		bottom.receiveMana(12345);

		middle.serverTick();
		helper.assertTrue(bottom.getCurrentMana() == 11345 && middle.getCurrentMana() == 0 && top.getCurrentMana() == 1000,
				"The buffer did not move 1000 mana from below to above");

		HolderLookup.Provider registries = helper.getLevel().registryAccess();
		CompoundTag saved = top.saveWithFullMetadata(registries);
		helper.assertTrue(saved.getInt("mana") == 1000, "The buffer does not save its mana in the key 'mana'");
		ManaBufferBlockEntity loaded = (ManaBufferBlockEntity) BlockEntity.loadStatic(top.getBlockPos(), top.getBlockState(), saved, registries);
		helper.assertTrue(loaded != null && loaded.getCurrentMana() == 1000, "The buffer lost its mana in the save");
		loaded.receiveMana(Integer.MAX_VALUE);
		helper.assertTrue(loaded.getCurrentMana() == 64_000_000 && loaded.isFull(), "The buffer holds more than its capacity");
		loaded.receiveMana(Integer.MIN_VALUE);
		helper.assertTrue(loaded.getCurrentMana() == 0, "The mana of the buffer became negative");
		helper.succeed();
	}

	/**
	 * 缓存器从旁边和下方的魔力池吸取魔力，并向上方的魔力池输出。
	 * A buffer takes mana from the mana pools on its sides and below it, and sends mana to the mana pool above it.
	 */
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void manaBufferExchangesManaWithPools(GameTestHelper helper) {
		ManaPoolBlockEntity below = place(helper, CENTER.below(), BotaniaBlocks.MANA_POOL, ManaPoolBlockEntity.class);
		ManaPoolBlockEntity side = place(helper, CENTER.east(), BotaniaBlocks.MANA_POOL, ManaPoolBlockEntity.class);
		ManaPoolBlockEntity above = place(helper, CENTER.above(), BotaniaBlocks.MANA_POOL, ManaPoolBlockEntity.class);
		ManaBufferBlockEntity buffer = place(helper, CENTER, ExtraBotanyBlocks.manaBuffer, ManaBufferBlockEntity.class);
		below.receiveMana(5000);
		side.receiveMana(300);

		buffer.serverTick();
		helper.assertTrue(side.getCurrentMana() == 0 && below.getCurrentMana() == 4000,
				"The buffer did not take the correct mana from the pools: side " + side.getCurrentMana() + ", below " + below.getCurrentMana());
		helper.assertTrue(above.getCurrentMana() == 1000 && buffer.getCurrentMana() == 300,
				"The buffer did not send 1000 mana to the pool above: above " + above.getCurrentMana() + ", buffer " + buffer.getCurrentMana());
		helper.succeed();
	}

	/**
	 * 和原版相同：只有量子魔力缓存器主动和另一种缓存器交换魔力。它每 tick 传输 5000 点。
	 * The same as in the original: only the Quantum Mana Buffer starts a transfer with the other type of buffer. It moves 5000 mana on each tick.
	 */
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void quantumBufferStartsTransfersWithBothTiers(GameTestHelper helper) {
		ManaBufferBlockEntity below = place(helper, CENTER.below(), ExtraBotanyBlocks.manaBuffer, ManaBufferBlockEntity.class);
		ManaBufferBlockEntity quantum = place(helper, CENTER, ExtraBotanyBlocks.quantumManaBuffer, ManaBufferBlockEntity.class);
		ManaBufferBlockEntity above = place(helper, CENTER.above(), ExtraBotanyBlocks.manaBuffer, ManaBufferBlockEntity.class);
		below.receiveMana(20000);
		quantum.receiveMana(7000);
		helper.assertTrue(quantum.getTier() == ManaBufferBlockEntity.Tier.QUANTUM && quantum.getMaxMana() == 1_024_000_000,
				"The Quantum Mana Buffer has an incorrect tier or capacity");

		above.serverTick();
		below.serverTick();
		helper.assertTrue(below.getCurrentMana() == 20000 && quantum.getCurrentMana() == 7000 && above.getCurrentMana() == 0,
				"A Mana Buffer started a transfer with a Quantum Mana Buffer");

		quantum.serverTick();
		helper.assertTrue(below.getCurrentMana() == 15000 && quantum.getCurrentMana() == 7000 && above.getCurrentMana() == 5000,
				"The Quantum Mana Buffer did not move 5000 mana in and 5000 mana out");
		helper.succeed();
	}

	/**
	 * 魔力缓存器有比较器输出，量子魔力缓存器没有。
	 * The Mana Buffer has a comparator output, and the Quantum Mana Buffer has none.
	 */
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void manaBufferComparatorOutput(GameTestHelper helper) {
		ManaBufferBlockEntity buffer = place(helper, CENTER, ExtraBotanyBlocks.manaBuffer, ManaBufferBlockEntity.class);
		BlockState state = buffer.getBlockState();
		BlockPos pos = buffer.getBlockPos();
		helper.assertTrue(state.hasAnalogOutputSignal() && state.getAnalogOutputSignal(helper.getLevel(), pos) == 0,
				"An empty Mana Buffer must give the comparator signal 0");
		buffer.receiveMana(1);
		helper.assertTrue(state.getAnalogOutputSignal(helper.getLevel(), pos) == 1, "A Mana Buffer with some mana must give a signal of 1 or more");
		buffer.receiveMana(buffer.getMaxMana());
		helper.assertTrue(state.getAnalogOutputSignal(helper.getLevel(), pos) == 15, "A full Mana Buffer must give the comparator signal 15");
		helper.assertTrue(!ExtraBotanyBlocks.quantumManaBuffer.defaultBlockState().hasAnalogOutputSignal(),
				"The Quantum Mana Buffer must not have a comparator output");
		helper.succeed();
	}

	// ---------------------------------------------------------------------
	// FE转换器 / Flux Manafield
	// ---------------------------------------------------------------------

	private static final BlockPos GENERATOR_POS = new BlockPos(1, 1, 1);

	private static ManaGeneratorBlockEntity generator(GameTestHelper helper, BlockPos pos) {
		return place(helper, pos, ExtraBotanyBlocks.manaGenerator, ManaGeneratorBlockEntity.class);
	}

	private static IEnergyStorage energy(GameTestHelper helper, ManaGeneratorBlockEntity generator, Direction side) {
		IEnergyStorage storage = helper.getLevel().getCapability(Capabilities.EnergyStorage.BLOCK, generator.getBlockPos(), side);
		helper.assertTrue(storage != null, "The energy capability of the Flux Manafield is missing");
		return storage;
	}

	/**
	 * FE转换器在每个面上提供同一个能量接口，并且可以接收魔力和连接火花。
	 * The Flux Manafield gives the same energy interface on each side, and it can receive mana and have a spark.
	 */
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void manaGeneratorCapabilities(GameTestHelper helper) {
		ManaGeneratorBlockEntity generator = generator(helper, GENERATOR_POS);
		IEnergyStorage storage = energy(helper, generator, null);
		helper.assertTrue(storage.getMaxEnergyStored() == 40_000, "The energy capacity is not the default of the original (40000 FE)");
		for (Direction side : Direction.values()) {
			helper.assertTrue(energy(helper, generator, side) == storage, "The energy interface is different on the side " + side);
		}
		helper.assertTrue(helper.getLevel().getCapability(BotaniaNeoForgeCapabilities.getBlockApiLookupById(ManaReceiver.LOOKUP),
				generator.getBlockPos(), null) == generator, "The Flux Manafield is not a mana receiver");
		helper.assertTrue(helper.getLevel().getCapability(BotaniaNeoForgeCapabilities.getBlockApiLookupById(ManaSparkAttachable.LOOKUP),
				generator.getBlockPos(), null) == generator, "A spark cannot attach to the Flux Manafield");
		helper.succeed();
	}

	/**
	 * 模拟的能量操作不改变任何东西。真实的操作有上下限，并且把区块标记为需要保存。存档使用 mana 和 energy 键。
	 * A simulated energy operation changes nothing. A real operation has limits and marks the chunk for saving. The save uses the keys 'mana' and 'energy'.
	 */
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void manaGeneratorEnergyLimitsAndSave(GameTestHelper helper) {
		ManaGeneratorBlockEntity generator = generator(helper, GENERATOR_POS);
		IEnergyStorage storage = energy(helper, generator, Direction.UP);
		var chunk = helper.getLevel().getChunkAt(generator.getBlockPos());
		chunk.setUnsaved(false);

		helper.assertTrue(storage.receiveEnergy(Integer.MAX_VALUE, true) == 40_000 && storage.getEnergyStored() == 0 && !chunk.isUnsaved(),
				"A simulated input changed the Flux Manafield");
		helper.assertTrue(storage.receiveEnergy(Integer.MAX_VALUE, false) == 40_000 && chunk.isUnsaved(),
				"A real input did not fill the Flux Manafield or did not mark the chunk");
		helper.assertTrue(!storage.canReceive() && storage.canExtract(), "A full Flux Manafield gives incorrect transfer flags");
		helper.assertTrue(storage.receiveEnergy(-1, false) == 0 && storage.extractEnergy(-1, false) == 0 && storage.getEnergyStored() == 40_000,
				"A negative request changed the energy");
		chunk.setUnsaved(false);
		helper.assertTrue(storage.extractEnergy(1250, true) == 1250 && storage.getEnergyStored() == 40_000 && !chunk.isUnsaved(),
				"A simulated output changed the Flux Manafield");
		helper.assertTrue(storage.extractEnergy(1250, false) == 1250 && chunk.isUnsaved(), "A real output did not mark the chunk");

		generator.receiveMana(1234);
		HolderLookup.Provider registries = helper.getLevel().registryAccess();
		CompoundTag saved = generator.saveWithoutMetadata(registries);
		helper.assertTrue(saved.getInt("energy") == 38_750 && saved.getInt("mana") == 1234, "The save does not use the keys 'energy' and 'mana'");
		ManaGeneratorBlockEntity restored = new ManaGeneratorBlockEntity(generator.getBlockPos(), generator.getBlockState());
		restored.loadWithComponents(saved, registries);
		helper.assertTrue(restored.getEnergy() == 38_750 && restored.getCurrentMana() == 1234, "The load lost energy or mana");
		helper.assertTrue(generator.getUpdatePacket().getTag().equals(generator.getUpdateTag(registries)),
				"The update packet and the chunk data are different");
		helper.assertTrue(storage.extractEnergy(Integer.MAX_VALUE, false) == 38_750 && storage.getEnergyStored() == 0
				&& !storage.canExtract() && storage.canReceive(), "The output was more than the energy in the machine");
		helper.succeed();
	}

	/**
	 * 每 tick 只转换一次：1000 FE 变成 99 点魔力。魔力装不下时不转换，所以 FE 不会被浪费。
	 * There is only one conversion on each tick: 1000 FE become 99 mana. There is no conversion when the mana does not fit, thus no FE is lost.
	 */
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void manaGeneratorConvertsOneLotForEachTick(GameTestHelper helper) {
		ManaGeneratorBlockEntity generator = generator(helper, GENERATOR_POS);
		IEnergyStorage storage = energy(helper, generator, null);
		storage.receiveEnergy(3000, false);

		generator.serverTick();
		helper.assertTrue(generator.getCurrentMana() == 99 && storage.getEnergyStored() == 2000, "The first conversion is not 1000 FE to 99 mana");
		generator.serverTick();
		helper.assertTrue(generator.getCurrentMana() == 198 && storage.getEnergyStored() == 1000, "The second conversion is not 1000 FE to 99 mana");

		generator.receiveMana(generator.getMaxMana() - generator.getCurrentMana() - 98);
		generator.serverTick();
		helper.assertTrue(storage.getEnergyStored() == 1000 && generator.getAvailableSpaceForMana() == 98,
				"The machine used FE when the result did not fit");
		generator.receiveMana(98);
		generator.serverTick();
		helper.assertTrue(generator.isFull() && storage.getEnergyStored() == 1000, "A machine with full mana used FE");
		generator.receiveMana(-99);
		generator.serverTick();
		helper.assertTrue(generator.isFull() && storage.getEnergyStored() == 0, "The last lot did not fit exactly");
		helper.succeed();
	}

	/**
	 * 机器每 tick 从每个邻居最多抽取 1000 FE，并且只抽取它装得下的量。
	 * On each tick the machine takes a maximum of 1000 FE from each neighbour, and only the quantity that it has space for.
	 */
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void manaGeneratorPullsEnergyFromNeighbours(GameTestHelper helper) {
		ManaGeneratorBlockEntity generator = generator(helper, GENERATOR_POS);
		for (Direction side : Direction.values()) {
			ManaGeneratorBlockEntity source = generator(helper, GENERATOR_POS.relative(side));
			source.getEnergyStorage().receiveEnergy(4000, false);
		}

		generator.serverTick();
		helper.assertTrue(generator.getEnergy() == 5000 && generator.getCurrentMana() == 99,
				"Six inputs must give 6000 FE and one conversion, but the machine has " + generator.getEnergy() + " FE");
		for (Direction side : Direction.values()) {
			ManaGeneratorBlockEntity source = (ManaGeneratorBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(GENERATOR_POS.relative(side)));
			helper.assertTrue(source.getEnergy() == 3000 && source.getCurrentMana() == 0, "The machine took more than 1000 FE from the neighbour on the side " + side);
		}

		// 快要装满时，只抽取剩下的空间。
		// When the machine is almost full, it only takes the remaining space.
		generator.receiveMana(generator.getMaxMana());
		generator.getEnergyStorage().receiveEnergy(40_000 - 50 - generator.getEnergy(), false);
		generator.serverTick();
		int total = 0;
		for (Direction side : Direction.values()) {
			total += ((ManaGeneratorBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(GENERATOR_POS.relative(side)))).getEnergy();
		}
		helper.assertTrue(generator.getEnergy() == 40_000 && total == 6 * 3000 - 50, "An almost full machine took energy that it had no space for");
		helper.succeed();
	}

	/**
	 * 机器攒够一份（200 点）才向魔力发射器输出。输出在转换之前进行。
	 * The machine waits for one full lot (200 mana) before it sends mana to a Mana Spreader. The output occurs before the conversion.
	 */
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void manaGeneratorFeedsSpreaderInFullLots(GameTestHelper helper) {
		ManaGeneratorBlockEntity generator = generator(helper, GENERATOR_POS);
		ManaSpreaderBlockEntity spreader = place(helper, GENERATOR_POS.east(), BotaniaBlocks.MANA_SPREADER, ManaSpreaderBlockEntity.class);
		generator.receiveMana(199);
		generator.getEnergyStorage().receiveEnergy(1000, false);

		generator.serverTick();
		helper.assertTrue(spreader.getCurrentMana() == 0 && generator.getCurrentMana() == 298,
				"The spreader got a part of a lot, or got mana that was made in the same tick");
		generator.serverTick();
		helper.assertTrue(spreader.getCurrentMana() == 200 && generator.getCurrentMana() == 98, "The lot for the spreader is not 200 mana");

		spreader.receiveMana(spreader.getMaxMana() - spreader.getCurrentMana() - 50);
		generator.receiveMana(102);
		generator.serverTick();
		helper.assertTrue(spreader.getCurrentMana() == spreader.getMaxMana() && generator.getCurrentMana() == 150,
				"An almost full spreader must only get the mana that it has space for");
		helper.succeed();
	}

	// ---------------------------------------------------------------------
	// 液态魔力转化器 / Mana Liquefaction Device
	// ---------------------------------------------------------------------

	private static FluidStack fluidedMana(int amount) {
		return new FluidStack(ExtraBotanyFluids.MANA, amount);
	}

	/**
	 * 1 mB 液态魔力变成 1000 点魔力。有红石信号时反过来。来回转换不产生也不丢失资源。
	 * 1 mB of Fluided Mana becomes 1000 mana. With a redstone signal the direction is opposite. A conversion in the two directions makes no resource and loses no resource.
	 */
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void manaLiquefactionConservesResources(GameTestHelper helper) {
		ManaLiquefactionBlockEntity machine = place(helper, CENTER, ExtraBotanyBlocks.manaLiquefaction, ManaLiquefactionBlockEntity.class);
		LivingrockBarrelBlockEntity tank = place(helper, CENTER.east(), ExtraBotanyBlocks.livingrockBarrel, LivingrockBarrelBlockEntity.class);
		tank.fill(fluidedMana(100), FluidAction.EXECUTE);

		machine.serverTick();
		helper.assertTrue(machine.getCurrentMana() == 1000 && machine.getEnergy() == 0 && tank.getFluid().getAmount() == 99,
				"1 mB of Fluided Mana did not become 1000 mana");

		helper.setBlock(CENTER.below(), Blocks.REDSTONE_BLOCK);
		machine.serverTick();
		machine.serverTick();
		helper.assertTrue(machine.getCurrentMana() == 0 && machine.getEnergy() == 0 && tank.getFluid().getAmount() == 100,
				"1000 mana did not become 1 mB of Fluided Mana");
		helper.succeed();
	}

	/**
	 * 魔力已满时机器不再抽取液体。有红石信号时，机器每 tick 最多把 2000 点魔力变成 2 mB 液体。
	 * The machine does not drain fluid when its mana is full. With a redstone signal, the machine changes a maximum of 2000 mana into 2 mB of fluid on each tick.
	 */
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void manaLiquefactionRatesAndLimits(GameTestHelper helper) {
		ManaLiquefactionBlockEntity machine = place(helper, CENTER, ExtraBotanyBlocks.manaLiquefaction, ManaLiquefactionBlockEntity.class);
		LivingrockBarrelBlockEntity tank = place(helper, CENTER.east(), ExtraBotanyBlocks.livingrockBarrel, LivingrockBarrelBlockEntity.class);
		tank.fill(fluidedMana(LivingrockBarrelBlockEntity.CAPACITY), FluidAction.EXECUTE);
		machine.receiveMana(machine.getMaxMana());

		machine.serverTick();
		helper.assertTrue(tank.getFluid().getAmount() == LivingrockBarrelBlockEntity.CAPACITY && machine.getEnergy() == 0,
				"A machine with full mana drained fluid");

		tank.drain(LivingrockBarrelBlockEntity.CAPACITY, FluidAction.EXECUTE);
		machine.receiveMana(10_000 - machine.getMaxMana());
		helper.setBlock(CENTER.below(), Blocks.REDSTONE_BLOCK);
		machine.serverTick();
		helper.assertTrue(machine.getCurrentMana() == 8000 && machine.getEnergy() == 2 && tank.getFluid().isEmpty(),
				"The first powered tick must change 2000 mana into 2 energy");
		machine.serverTick();
		helper.assertTrue(machine.getCurrentMana() == 6000 && machine.getEnergy() == 2 && tank.getFluid().getAmount() == 2,
				"The second powered tick must put 2 mB into the tank");
		helper.assertTrue(tank.getFluid().is(ExtraBotanyFluids.MANA), "The machine made an incorrect fluid");
		helper.succeed();
	}

	/**
	 * 没有红石信号时，机器向旁边的魔力发射器输出魔力。
	 * Without a redstone signal, the machine sends mana to the adjacent Mana Spreader.
	 */
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void manaLiquefactionFeedsSpreader(GameTestHelper helper) {
		ManaLiquefactionBlockEntity machine = place(helper, CENTER, ExtraBotanyBlocks.manaLiquefaction, ManaLiquefactionBlockEntity.class);
		ManaSpreaderBlockEntity spreader = place(helper, CENTER.west(), BotaniaBlocks.MANA_SPREADER, ManaSpreaderBlockEntity.class);
		machine.receiveMana(500);

		machine.serverTick();
		helper.assertTrue(spreader.getCurrentMana() == 200 && machine.getCurrentMana() == 300, "The machine did not send one lot of 200 mana to the spreader");
		helper.succeed();
	}

	// ---------------------------------------------------------------------
	// 活石桶和液态魔力 / Livingrock Barrel and Fluided Mana
	// ---------------------------------------------------------------------

	/**
	 * 液态魔力、它的桶和它的方块用旧存档的 ID 注册，并且互相关联。
	 * Fluided Mana, its bucket and its block are registered with the IDs of the old saves, and they refer to each other.
	 */
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void fluidedManaRegistryLinks(GameTestHelper helper) {
		helper.assertTrue(BuiltInRegistries.FLUID.getKey(ExtraBotanyFluids.MANA).toString().equals("extrabotany:fluidedmana")
				&& BuiltInRegistries.FLUID.getKey(ExtraBotanyFluids.FLOWING_MANA).toString().equals("extrabotany:flowing_fluidedmana"),
				"The fluid IDs changed");
		helper.assertTrue(BuiltInRegistries.ITEM.getKey(ExtraBotanyFluids.MANA_BUCKET).toString().equals("extrabotany:fluidedmana_bucket")
				&& BuiltInRegistries.BLOCK.getKey(ExtraBotanyFluids.MANA_BLOCK).toString().equals("extrabotany:fluidedmana"),
				"The bucket ID or the block ID changed");
		helper.assertTrue(ExtraBotanyFluids.MANA.getBucket() == ExtraBotanyFluids.MANA_BUCKET
				&& ExtraBotanyFluids.MANA.getFluidType() == ExtraBotanyFluids.MANA_TYPE
				&& ExtraBotanyFluids.FLOWING_MANA.getSource() == ExtraBotanyFluids.MANA
				&& ExtraBotanyFluids.MANA.defaultFluidState().createLegacyBlock().is(ExtraBotanyFluids.MANA_BLOCK),
				"The fluid, the bucket and the block do not refer to each other");
		helper.assertTrue(ExtraBotanyFluids.MANA_TYPE.getLightLevel() == 12, "The light level of Fluided Mana is not 12");
		helper.succeed();
	}

	/**
	 * 桶读取这个移植版旧存档的格式。它不混合两种液体，模拟操作不改变内容，新的存档保留液体。
	 * The barrel reads the format of the old saves of this port. It does not mix two fluids, a simulated operation does not change the contents, and the new save keeps the fluid.
	 */
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void livingrockBarrelTransactionsAndSave(GameTestHelper helper) {
		HolderLookup.Provider registries = helper.getLevel().registryAccess();
		BlockPos pos = helper.absolutePos(CENTER);
		BlockState state = ExtraBotanyBlocks.livingrockBarrel.defaultBlockState();
		LivingrockBarrelBlockEntity barrel = new LivingrockBarrelBlockEntity(pos, state);
		CompoundTag legacy = new CompoundTag();
		legacy.putString("fluid", "extrabotany:fluidedmana");
		legacy.putInt("amount", 15333);
		barrel.loadWithComponents(legacy, registries);
		helper.assertTrue(barrel.getFluid().is(ExtraBotanyFluids.MANA) && barrel.getFluid().getAmount() == 15333, "The barrel did not read the old save format");

		helper.assertTrue(barrel.fill(fluidedMana(1000), FluidAction.SIMULATE) == 667 && barrel.getFluid().getAmount() == 15333,
				"A simulated fill changed the barrel");
		helper.assertTrue(barrel.fill(new FluidStack(Fluids.WATER, 1000), FluidAction.EXECUTE) == 0, "The barrel mixed two fluids");
		helper.assertTrue(barrel.drain(new FluidStack(Fluids.WATER, 1000), FluidAction.EXECUTE).isEmpty(), "The barrel gave a fluid that it does not hold");
		helper.assertTrue(barrel.drain(333, FluidAction.SIMULATE).getAmount() == 333 && barrel.getFluid().getAmount() == 15333,
				"A simulated drain changed the barrel");
		helper.assertTrue(barrel.drain(333, FluidAction.EXECUTE).getAmount() == 333, "The barrel did not give the fluid");
		helper.assertTrue(barrel.fill(fluidedMana(5000), FluidAction.EXECUTE) == 1000 && barrel.getFluid().getAmount() == LivingrockBarrelBlockEntity.CAPACITY,
				"The barrel holds more than 16 buckets");

		LivingrockBarrelBlockEntity reloaded = (LivingrockBarrelBlockEntity) BlockEntity.loadStatic(pos, state, barrel.saveWithFullMetadata(registries), registries);
		helper.assertTrue(reloaded != null && reloaded.getFluid().is(ExtraBotanyFluids.MANA)
				&& reloaded.getFluid().getAmount() == LivingrockBarrelBlockEntity.CAPACITY, "The save lost the fluid");
		helper.assertTrue(barrel.drain(Integer.MAX_VALUE, FluidAction.EXECUTE).getAmount() == LivingrockBarrelBlockEntity.CAPACITY && barrel.getFluid().isEmpty()
				&& barrel.fill(new FluidStack(Fluids.WATER, 1), FluidAction.EXECUTE) == 1, "An empty barrel must accept a new fluid");
		helper.succeed();
	}

	/**
	 * 装着水的桶给旁边的空花药台加水，每个花药台用掉一桶水。
	 * A barrel with water fills the adjacent empty Petal Apothecary, and each apothecary uses one bucket of water.
	 */
	@GameTest(template = "empty", timeoutTicks = 60)
	public static void livingrockBarrelFillsApothecary(GameTestHelper helper) {
		LivingrockBarrelBlockEntity barrel = place(helper, CENTER, ExtraBotanyBlocks.livingrockBarrel, LivingrockBarrelBlockEntity.class);
		helper.setBlock(CENTER.east(), BotaniaBlocks.PETAL_APOTHECARY);
		PetalApothecary apothecary = (PetalApothecary) helper.getLevel().getBlockEntity(helper.absolutePos(CENTER.east()));
		barrel.fill(new FluidStack(Fluids.WATER, 2500), FluidAction.EXECUTE);

		helper.succeedWhen(() -> helper.assertTrue(apothecary.getFluid() == PetalApothecary.State.WATER && barrel.getFluid().getAmount() == 1500,
				"The barrel did not fill the apothecary with exactly one bucket of water"));
	}

	/**
	 * 玩家用水桶右键活石桶，把水倒进去。活石桶不接受这种液体时，水桶不会把水倒在旁边。
	 * A player uses a water bucket on the barrel to put the water in. When the barrel does not accept the fluid, the bucket does not put the water adjacent to the barrel.
	 */
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void livingrockBarrelBucketInteraction(GameTestHelper helper) {
		LivingrockBarrelBlockEntity barrel = place(helper, CENTER, ExtraBotanyBlocks.livingrockBarrel, LivingrockBarrelBlockEntity.class);
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		player.setPos(Vec3.atBottomCenterOf(helper.absolutePos(CENTER.above())));
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WATER_BUCKET));

		ItemInteractionResult result = barrel.getBlockState().useItemOn(player.getMainHandItem(), helper.getLevel(), player, InteractionHand.MAIN_HAND, hit(helper, CENTER));
		helper.assertTrue(result.consumesAction() && barrel.getFluid().is(Fluids.WATER) && barrel.getFluid().getAmount() == 1000
				&& player.getMainHandItem().is(Items.BUCKET), "The water bucket did not go into the barrel");

		barrel.drain(1000, FluidAction.EXECUTE);
		barrel.fill(fluidedMana(500), FluidAction.EXECUTE);
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WATER_BUCKET));
		result = barrel.getBlockState().useItemOn(player.getMainHandItem(), helper.getLevel(), player, InteractionHand.MAIN_HAND, hit(helper, CENTER));
		helper.assertTrue(result == ItemInteractionResult.CONSUME && player.getMainHandItem().is(Items.WATER_BUCKET) && barrel.getFluid().getAmount() == 500,
				"A bucket that the barrel cannot accept must do nothing");
		helper.assertTrue(barrel.getBlockState().useItemOn(new ItemStack(Items.STICK), helper.getLevel(), player, InteractionHand.MAIN_HAND, hit(helper, CENTER))
				== ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION, "An item that is not a fluid container must not be stopped");
		helper.succeed();
	}

	// ---------------------------------------------------------------------
	// 不随想之茧 / Cocoon of Desire
	// ---------------------------------------------------------------------

	/**
	 * 把茧设为"还差 1 tick 孵化"的状态。
	 * Sets the cocoon to the state "1 tick before the hatch".
	 */
	private static void almostHatch(GameTestHelper helper, CocoonOfDesireBlockEntity cocoon, Item item) {
		HolderLookup.Provider registries = helper.getLevel().registryAccess();
		CompoundTag tag = new CompoundTag();
		tag.putInt("timePassed", CocoonOfDesireBlockEntity.HATCH_TIME - 1);
		tag.put("Item", new ItemStack(item).save(registries));
		cocoon.loadWithComponents(tag, registries);
	}

	private static AABB around(GameTestHelper helper, BlockPos pos) {
		return new AABB(helper.absolutePos(pos)).inflate(2);
	}

	/**
	 * 茧的进度会保存。时间到了之后，茧孵化出孵化表里的生物，生物是幼年。
	 * The progress of the cocoon is saved. When the time is complete, the cocoon hatches the creature from the hatch table, and the creature is a baby.
	 */
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void cocoonHatchesBabiesFromTheHatchTable(GameTestHelper helper) {
		HolderLookup.Provider registries = helper.getLevel().registryAccess();
		CocoonOfDesireBlockEntity cocoon = place(helper, CENTER.below(), ExtraBotanyBlocks.cocoonOfDesire, CocoonOfDesireBlockEntity.class);
		almostHatch(helper, cocoon, Items.LEATHER);
		cocoon.loadWithComponents(cocoon.saveWithFullMetadata(registries), registries);
		helper.assertTrue(cocoon.getTimePassed() == CocoonOfDesireBlockEntity.HATCH_TIME - 1 && cocoon.getItem().is(Items.LEATHER),
				"The save lost the item or the progress of the cocoon");

		cocoon.serverTick();
		List<Cow> cows = helper.getLevel().getEntitiesOfClass(Cow.class, around(helper, CENTER));
		helper.assertTrue(cocoon.getItem().isEmpty() && cocoon.getTimePassed() == 0 && cows.size() == 1 && cows.getFirst().isBaby(),
				"Leather must hatch into exactly one baby cow, and the cocoon must become empty");

		// 原版的表里是任意颜色的羊毛。
		// The table of the original has wool of any color.
		almostHatch(helper, cocoon, Items.RED_WOOL);
		cocoon.serverTick();
		helper.assertTrue(helper.getLevel().getEntitiesOfClass(Sheep.class, around(helper, CENTER)).size() == 1, "Red wool must hatch into a sheep");

		almostHatch(helper, cocoon, Items.EMERALD_BLOCK);
		cocoon.serverTick();
		List<Villager> villagers = helper.getLevel().getEntitiesOfClass(Villager.class, around(helper, CENTER));
		helper.assertTrue(villagers.size() == 1 && villagers.getFirst().isBaby(), "A block of emerald must hatch into a baby villager");
		helper.succeed();
	}

	/**
	 * 茧里的物品不在孵化表里时，什么也不孵化。物品留在茧里，计时重新开始。
	 * When the item in the cocoon is not in the hatch table, nothing hatches. The item stays in the cocoon, and the timer starts again.
	 */
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void cocoonKeepsItemsThatCannotHatch(GameTestHelper helper) {
		CocoonOfDesireBlockEntity cocoon = place(helper, CENTER.below(), ExtraBotanyBlocks.cocoonOfDesire, CocoonOfDesireBlockEntity.class);
		almostHatch(helper, cocoon, Items.STICK);

		cocoon.serverTick();
		helper.assertTrue(cocoon.getItem().is(Items.STICK) && cocoon.getTimePassed() == 0, "An item that cannot hatch must stay, and the timer must start again");
		helper.assertTrue(helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.Mob.class, around(helper, CENTER)).isEmpty(),
				"An item that is not in the hatch table hatched into a creature");
		helper.succeed();
	}

	/**
	 * 茧只吸收孵化表里有的掉落物，并且一次只吸收一个。
	 * The cocoon only takes dropped items that are in the hatch table, and it takes only one at a time.
	 */
	@GameTest(template = "empty", timeoutTicks = 60)
	public static void cocoonTakesOneHatchableDroppedItem(GameTestHelper helper) {
		CocoonOfDesireBlockEntity cocoon = place(helper, CENTER.below(), ExtraBotanyBlocks.cocoonOfDesire, CocoonOfDesireBlockEntity.class);
		ItemEntity stone = dropItem(helper, CENTER.below(), 1.0, new ItemStack(Items.COBBLESTONE));
		ItemEntity wool = dropItem(helper, CENTER.below(), 1.0, new ItemStack(Items.RED_WOOL, 3));

		helper.succeedWhen(() -> helper.assertTrue(cocoon.getItem().is(Items.RED_WOOL) && cocoon.getItem().getCount() == 1
				&& wool.isAlive() && wool.getItem().getCount() == 2 && stone.isAlive() && stone.getItem().getCount() == 1,
				"The cocoon must take exactly one wool and must not take the cobblestone"));
	}

	/**
	 * 玩家右键放入一个物品，再右键取回它。
	 * A player uses an item on the cocoon to put one in, and uses the cocoon again to take it back.
	 */
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void cocoonPlayerInteraction(GameTestHelper helper) {
		CocoonOfDesireBlockEntity cocoon = place(helper, CENTER.below(), ExtraBotanyBlocks.cocoonOfDesire, CocoonOfDesireBlockEntity.class);
		BlockState state = cocoon.getBlockState();
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		player.setPos(Vec3.atBottomCenterOf(helper.absolutePos(CENTER)));
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK, 5));

		ItemInteractionResult put = state.useItemOn(player.getMainHandItem(), helper.getLevel(), player, InteractionHand.MAIN_HAND, hit(helper, CENTER.below()));
		helper.assertTrue(put.consumesAction() && cocoon.getItem().is(Items.STICK) && cocoon.getItem().getCount() == 1
				&& player.getMainHandItem().getCount() == 4, "The player did not put exactly one item into the cocoon");
		helper.assertTrue(state.useItemOn(player.getMainHandItem(), helper.getLevel(), player, InteractionHand.MAIN_HAND, hit(helper, CENTER.below()))
				== ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION && cocoon.getItem().getCount() == 1, "A cocoon that holds an item accepted a second item");

		InteractionResult take = state.useWithoutItem(helper.getLevel(), player, hit(helper, CENTER.below()));
		helper.assertTrue(take.consumesAction() && cocoon.getItem().isEmpty() && player.getInventory().countItem(Items.STICK) == 5,
				"The player did not get the item back");
		helper.assertTrue(state.useWithoutItem(helper.getLevel(), player, hit(helper, CENTER.below())) == InteractionResult.PASS,
				"An empty cocoon must not react to an empty hand");
		helper.succeed();
	}

	// ---------------------------------------------------------------------
	// 星尘莲 / Stardust Lotus
	// ---------------------------------------------------------------------

	private static final BlockPos LOTUS_POS = new BlockPos(2, 1, 2);
	private static final int[][] CORNERS = { { 0, 0 }, { 0, 4 }, { 4, 0 }, { 4, 4 } };

	/**
	 * 搭好星尘莲的多方块结构，并放上一朵浮空的星尘莲。
	 * Builds the multiblock structure of the Stardust Lotus and puts a floating Stardust Lotus in it.
	 */
	private static StardustLotusBlockEntity buildLotus(GameTestHelper helper) {
		for (int x = 0; x <= 4; x++) {
			for (int z = 0; z <= 4; z++) {
				if (x == 0 || x == 4 || z == 0 || z == 4) {
					helper.setBlock(new BlockPos(x, 0, z), Blocks.QUARTZ_BLOCK);
				}
			}
		}
		for (int[] corner : CORNERS) {
			helper.setBlock(new BlockPos(corner[0], 1, corner[1]), Blocks.LAPIS_BLOCK);
			helper.setBlock(new BlockPos(corner[0], 2, corner[1]), Blocks.LAPIS_BLOCK);
			helper.setBlock(new BlockPos(corner[0], 3, corner[1]), Blocks.SEA_LANTERN);
		}
		return place(helper, LOTUS_POS, ExtrabotanyFlowerBlocks.stardustLotusFloating, StardustLotusBlockEntity.class);
	}

	/**
	 * 星尘莲的数据会保存，也会发给客户端。客户端需要这些数据来显示法杖界面和粒子。
	 * The data of the Stardust Lotus is saved and is also sent to the clients. The client needs this data for the wand display and the particles.
	 */
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void stardustLotusKeepsItsDataAndSendsItToClients(GameTestHelper helper) {
		HolderLookup.Provider registries = helper.getLevel().registryAccess();
		StardustLotusBlockEntity lotus = place(helper, LOTUS_POS, ExtrabotanyFlowerBlocks.stardustLotusFloating, StardustLotusBlockEntity.class);
		CompoundTag legacy = lotus.saveWithFullMetadata(registries);
		legacy.putBoolean("hasTarget", true);
		legacy.putInt("targetX", 37);
		legacy.putInt("targetY", 83);
		legacy.putInt("targetZ", -49);
		legacy.putBoolean("hasPaper", true);
		legacy.putInt("consumedMana", 18763);
		lotus.loadWithComponents(legacy, registries);

		StardustLotusBlockEntity restored = (StardustLotusBlockEntity) BlockEntity.loadStatic(lotus.getBlockPos(), lotus.getBlockState(),
				lotus.saveWithFullMetadata(registries), registries);
		helper.assertTrue(restored != null && restored.hasPaper() && restored.getConsumedMana() == 18763
				&& new BlockPos(37, 83, -49).equals(restored.getTarget()), "The save lost the paper, the mana or the target of the lotus");

		CompoundTag update = lotus.getUpdateTag(registries);
		helper.assertTrue(update.getBoolean("hasPaper") && update.getInt("consumedMana") == 18763 && update.getBoolean("hasTarget")
				&& update.getInt("targetY") == 83, "The update tag for the clients does not have the data of the lotus");
		helper.succeed();
	}

	/**
	 * 一张纸和足够的魔力打开传送门。传送门把旁边的生物传送到目标上方，并且一直保持打开。
	 * One paper and sufficient mana open the portal. The portal teleports the adjacent creatures to the position above the target, and it stays open.
	 */
	@GameTest(template = "empty", timeoutTicks = 200)
	public static void stardustLotusOpensPermanentPortal(GameTestHelper helper) {
		StardustLotusBlockEntity lotus = buildLotus(helper);
		BlockPos target = helper.absolutePos(new BlockPos(2, 3, 2));
		lotus.setTarget(target);
		lotus.addMana(50_000);
		int cost = lotus.getTeleportCost();
		helper.assertTrue(cost == 20_000 + 2 * 150, "The cost for a distance of 2 blocks must be 20300, but it is " + cost);

		// 纸放在花的旁边。放在花的方块里面时，游戏会把它推走。
		// The paper is adjacent to the flower. When it is in the block of the flower, the game pushes it away.
		ItemEntity paper = dropItem(helper, LOTUS_POS.north(), 0.5, new ItemStack(Items.PAPER, 2));
		paper.setNoGravity(true);
		Husk first = helper.spawnWithNoFreeWill(EntityType.HUSK, LOTUS_POS.west());
		first.setNoGravity(true);
		Pig[] second = new Pig[1];

		helper.startSequence()
				.thenWaitUntil(() -> helper.assertTrue(first.blockPosition().equals(target.above()), "The portal did not teleport the first creature"))
				.thenExecute(() -> {
					helper.assertTrue(lotus.hasPaper() && lotus.getConsumedMana() == cost && lotus.getMana() == 50_000 - cost,
							"The lotus must use exactly the cost: used " + lotus.getConsumedMana());
					helper.assertTrue(paper.isAlive() && paper.getItem().getCount() == 1, "The lotus must use exactly one paper");
					helper.assertTrue(first.isOnPortalCooldown(), "A creature that was teleported must have a cooldown");
					second[0] = helper.spawnWithNoFreeWill(EntityType.PIG, LOTUS_POS.east());
					second[0].setNoGravity(true);
				})
				.thenWaitUntil(() -> helper.assertTrue(second[0].blockPosition().equals(target.above()), "The open portal did not teleport the second creature"))
				.thenExecute(() -> helper.assertTrue(lotus.getConsumedMana() == cost && lotus.getMana() == 50_000 - cost && paper.getItem().getCount() == 1,
						"An open portal must not use more mana or paper"))
				.thenSucceed();
	}

	/**
	 * 多方块结构不完整时，星尘莲不工作。结构补好之后它开始工作。
	 * The Stardust Lotus does not work when the multiblock structure is not complete. It starts to work after the structure is repaired.
	 */
	@GameTest(template = "empty", timeoutTicks = 200)
	public static void stardustLotusNeedsTheStructure(GameTestHelper helper) {
		StardustLotusBlockEntity lotus = buildLotus(helper);
		helper.setBlock(new BlockPos(0, 0, 2), Blocks.AIR);
		lotus.setTarget(helper.absolutePos(new BlockPos(2, 3, 2)));
		lotus.addMana(50_000);
		// 纸放在花的旁边。放在花的方块里面时，游戏会把它推走。
		// The paper is adjacent to the flower. When it is in the block of the flower, the game pushes it away.
		ItemEntity paper = dropItem(helper, LOTUS_POS.north(), 0.5, new ItemStack(Items.PAPER));
		paper.setNoGravity(true);
		helper.assertTrue(!lotus.isStructureValid(), "A structure with a missing quartz block must not be valid");

		helper.startSequence()
				.thenIdle(40)
				.thenExecute(() -> {
					helper.assertTrue(!lotus.hasPaper() && lotus.getConsumedMana() == 0 && paper.isAlive(), "The lotus worked with a structure that is not complete");
					helper.setBlock(new BlockPos(0, 0, 2), Blocks.QUARTZ_BLOCK);
					helper.assertTrue(lotus.isStructureValid(), "The repaired structure must be valid");
				})
				.thenWaitUntil(() -> helper.assertTrue(lotus.hasPaper() && lotus.getConsumedMana() > 0, "The lotus did not start after the structure was repaired"))
				.thenSucceed();
	}

	/**
	 * 绑定器把记录的坐标设为星尘莲的目标。别的维度的坐标和没有绑定的绑定器不起作用。
	 * The binder sets its recorded position as the target of the Stardust Lotus. A position in a different dimension and a binder that is not bound have no effect.
	 */
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void binderSetsTheLotusTarget(GameTestHelper helper) {
		StardustLotusBlockEntity lotus = place(helper, LOTUS_POS, ExtrabotanyFlowerBlocks.stardustLotusFloating, StardustLotusBlockEntity.class);
		helper.setBlock(new BlockPos(0, 0, 0), Blocks.STONE);
		BlockPos recorded = helper.absolutePos(new BlockPos(0, 0, 0));
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);

		ItemStack unbound = new ItemStack(ExtraBotanyItems.binder);
		player.setItemInHand(InteractionHand.MAIN_HAND, unbound);
		unbound.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit(helper, LOTUS_POS)));
		helper.assertTrue(lotus.getTarget() == null, "A binder that is not bound set a target");

		ItemStack binder = new ItemStack(ExtraBotanyItems.binder);
		player.setItemInHand(InteractionHand.MAIN_HAND, binder);
		player.setShiftKeyDown(true);
		binder.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit(helper, new BlockPos(0, 0, 0))));
		player.setShiftKeyDown(false);
		InteractionResult result = binder.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit(helper, LOTUS_POS)));
		helper.assertTrue(result.consumesAction() && recorded.equals(lotus.getTarget()), "The binder did not set its recorded position as the target");

		ItemStack otherDimension = binder.copy();
		CustomData.update(DataComponents.CUSTOM_DATA, otherDimension, tag -> {
			tag.putString("dim", "minecraft:the_nether");
			tag.putInt("posx", 999);
		});
		player.setItemInHand(InteractionHand.MAIN_HAND, otherDimension);
		otherDimension.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit(helper, LOTUS_POS)));
		helper.assertTrue(recorded.equals(lotus.getTarget()), "A position in a different dimension changed the target");
		helper.succeed();
	}
}
