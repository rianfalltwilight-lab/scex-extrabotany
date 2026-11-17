package io.github.lounode.extrabotany.forge.gametest;

import io.github.lounode.extrabotany.common.block.ExtraBotanyBlocks;
import io.github.lounode.extrabotany.common.block.block_entity.*;
import io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks;
import io.github.lounode.extrabotany.common.block.flower.functional.*;
import io.github.lounode.extrabotany.common.block.flower.generating.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.block.block_entity.mana.ManaPoolBlockEntity;

@GameTestHolder("extrabotany")
@PrefixGameTestTemplate(false)
public final class P2MachineChunkGameTests {

	@GameTest(template = "empty", timeoutTicks = 40)
	public static void manaMachinesDoNotLoadAbsentNeighbors(GameTestHelper h) {
		for (int variant = 0; variant < 4; variant++) {
			BlockPos pos = edge(h, 10300 + variant * 10);
			ManaMachineBlockEntity machine = switch (variant) {
				case 0 -> new ManaBufferBlockEntity(ManaBufferBlockEntity.Tier.BUFFER, pos, ExtraBotanyBlocks.manaBuffer.defaultBlockState());
				case 1 -> new ManaBufferBlockEntity(ManaBufferBlockEntity.Tier.QUANTUM, pos, ExtraBotanyBlocks.quantumManaBuffer.defaultBlockState());
				case 2 -> new ManaGeneratorBlockEntity(pos, ExtraBotanyBlocks.manaGenerator.defaultBlockState());
				default -> new ManaLiquefactionBlockEntity(pos, ExtraBotanyBlocks.manaLiquefaction.defaultBlockState());
			};
			CompoundTag tag = new CompoundTag();
			tag.putInt("mana", variant >= 2 ? 10000 : 0);
			machine.loadWithComponents(tag, h.getLevel().registryAccess());
			machine.setLevel(h.getLevel());
			absent(h, pos, "machine fixture");
			machine.serverTick();
			absent(h, pos, "mana machine " + variant);
			h.assertTrue(machine.getCurrentMana() == (variant >= 2 ? 10000 : 0), "Boundary machine lost mana to an absent neighbor");
		}
		h.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 40)
	public static void barrelPedestalAndBellowsScansDoNotLoadAbsentNeighbors(GameTestHelper h) {
		var level = h.getLevel();
		BlockPos pos = edge(h, 10340);
		while (Math.floorMod(level.getGameTime() + pos.asLong(), 10) != 0) pos = pos.below();
		var barrel = new LivingrockBarrelBlockEntity(pos, ExtraBotanyBlocks.livingrockBarrel.defaultBlockState());
		barrel.fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE);
		barrel.setLevel(level);
		barrel.serverTick();
		absent(h, pos, "barrel neighbor scan");
		h.assertTrue(barrel.getFluid().getAmount() == 1000, "Barrel drained into an absent apothecary");
		var pedestal = new PedestalBlockEntity(pos, ExtraBotanyBlocks.livingrockPedestal.defaultBlockState());
		pedestal.setLevel(level);
		pedestal.setTier(2);
		h.assertTrue(pedestal.getChargeAmount() == 9, "Pedestal charged from absent pools");
		absent(h, pos, "pedestal pool scan");
		pedestal.updateTier();
		absent(h, pos, "pedestal structure scan");
		h.assertTrue(pedestal.getTier() == 0, "An incomplete boundary structure retained a tier");
		var frame = new PowerFrameBlockEntity(pos, ExtraBotanyBlocks.powerFrame.defaultBlockState());
		frame.setLevel(level);
		h.assertTrue(frame.getBellows(level, pos, null).isEmpty(), "Absent bellows were linked");
		absent(h, pos, "charger direct bellows scan");

		BlockPos bellows = pos.east(15);
		level.setBlock(bellows, BotaniaBlocks.MANATIDE_BELLOWS.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST), 18);
		try {
			absent(h, pos, "bellows fixture");
			h.assertTrue(frame.getBellows(level, bellows.west(), null).isEmpty(), "Bellows linked through an absent target chunk");
			absent(h, pos, "bellows second neighbor lookup");
		} finally {
			level.setBlock(bellows, Blocks.AIR.defaultBlockState(), 18);
		}
		h.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 40)
	public static void allBoundaryFlowerScansSkipTrulyUnloadedChunks(GameTestHelper h) {
		var level = h.getLevel();
		for (boolean floating : new boolean[] {false, true}) {
			BlockPos pos = edge(h, floating ? 10360 : 10350);
			var bell = at(h, new BellflowerBlockEntity(pos, (floating ? ExtrabotanyFlowerBlocks.bellflowerFloating : ExtrabotanyFlowerBlocks.bellflower).defaultBlockState()));
			bell.setFloating(floating);
			bell.countNegativeBlocks();
			bell.checkBeBlew(pos.west());
			absent(h, pos, "bellflower");
			var annoying = at(h, new AnnoyingFlowerBlockEntity(pos, (floating ? ExtrabotanyFlowerBlocks.annoyingflowerFloating : ExtrabotanyFlowerBlocks.annoyingflower).defaultBlockState()));
			annoying.setFloating(floating);
			h.assertTrue(!annoying.hasWater(), "Absent water pedestal counted as present");
			absent(h, pos, "annoying flower");
			var enchanter = at(h, new EnchanterBlockEntity(pos, (floating ? ExtrabotanyFlowerBlocks.enchanterFloating : ExtrabotanyFlowerBlocks.enchanter).defaultBlockState()));
			enchanter.setFloating(floating);
			h.assertTrue(enchanter.getRandomPos() == null, "Absent grass counted as a conversion target");
			absent(h, pos, "enchanter");
			var twinstar = at(h, new TwinstarBlockEntity(pos, (floating ? ExtrabotanyFlowerBlocks.twinstarFloating : ExtrabotanyFlowerBlocks.twinstar).defaultBlockState()));
			twinstar.setFloating(floating);
			h.assertTrue(twinstar.getThermoelectric() == 0, "Absent fluids supplied heat");
			absent(h, pos, "twinstar");
			var lotus = at(h, new StardustLotusBlockEntity(pos, (floating ? ExtrabotanyFlowerBlocks.stardustLotusFloating : ExtrabotanyFlowerBlocks.stardustLotus).defaultBlockState()));
			lotus.setFloating(floating);
			h.assertTrue(!lotus.isStructureValid(), "Absent lotus structure was accepted");
			absent(h, pos, "stardust lotus");
			var serenitian = at(h, new SerenitianBlockEntity(pos, (floating ? ExtrabotanyFlowerBlocks.serenitianFloating : ExtrabotanyFlowerBlocks.serenitian).defaultBlockState()));
			serenitian.setFloating(floating);
			for (int tick = 0; tick < 20; tick++) serenitian.tickFlower();
			absent(h, pos, "serenitian");
			var stonesia = at(h, new StonesiaBlockEntity(pos, (floating ? ExtrabotanyFlowerBlocks.stonesiaFloating : ExtrabotanyFlowerBlocks.stonesia).defaultBlockState()) {
				@Override public BlockPos getRadomBlockPos() { return pos.west(); }
			});
			stonesia.setFloating(floating);
			stonesia.tickFlower();
			absent(h, pos, "stonesia selected candidate");
			h.assertTrue(stonesia.getMana() == 0 && stonesia.getCooldown() == 0, "Unloaded stone candidate generated mana");
		}
		h.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 40)
	public static void loadedNeighborsStillSupplyManaWaterAndDecayReset(GameTestHelper h) {
		var level = h.getLevel();
		BlockPos pos = edge(h, 10370).offset(8, 0, 8);
		BlockPos poolPos = pos.east(), waterPos = pos.south(), flowerPos = pos.west();
		level.setBlock(pos, ExtraBotanyBlocks.manaBuffer.defaultBlockState(), 18);
		level.setBlock(poolPos, BotaniaBlocks.MANA_POOL.defaultBlockState(), 18);
		level.setBlock(waterPos, ExtraBotanyBlocks.livingrockPedestal.defaultBlockState(), 18);
		level.setBlock(flowerPos, ExtrabotanyFlowerBlocks.twinstarFloating.defaultBlockState(), 18);
		try {
			var pool = (ManaPoolBlockEntity) level.getBlockEntity(poolPos);
			pool.receiveMana(2000);
			var buffer = (ManaBufferBlockEntity) level.getBlockEntity(pos);
			buffer.serverTick();
			h.assertTrue(buffer.getCurrentMana() == 1000 && pool.getCurrentMana() == 1000, "Loaded neighbor mana transfer stopped");
			((PedestalBlockEntity) level.getBlockEntity(waterPos)).setItem(new ItemStack(Items.WATER_BUCKET));
			var annoying = at(h, new AnnoyingFlowerBlockEntity(pos, ExtrabotanyFlowerBlocks.annoyingflower.defaultBlockState()));
			h.assertTrue(annoying.hasWater() && annoying.hasWater(), "Loaded pedestal lookup or cached lookup stopped");
			var twinstar = (TwinstarBlockEntity) level.getBlockEntity(flowerPos);
			twinstar.setPassiveDecayTicks(100);
			var serenitian = at(h, new SerenitianBlockEntity(pos, ExtrabotanyFlowerBlocks.serenitian.defaultBlockState()));
			for (int tick = 0; tick < 20; tick++) serenitian.tickFlower();
			h.assertTrue(twinstar.getPassiveDecayTicks() == 0, "Loaded flower was no longer protected from decay");
		} finally {
			for (var block : new BlockPos[] {pos, poolPos, waterPos, flowerPos}) level.setBlock(block, Blocks.AIR.defaultBlockState(), 18);
		}
		h.succeed();
	}

	private static BlockPos edge(GameTestHelper h, int chunk) {
		h.getLevel().getChunk(chunk, chunk);
		BlockPos pos = new BlockPos(chunk * 16, h.getLevel().getMaxBuildHeight() - 10, chunk * 16);
		absent(h, pos, "initial fixture");
		return pos;
	}

	private static void absent(GameTestHelper h, BlockPos pos, String operation) {
		int x = pos.getX() >> 4, z = pos.getZ() >> 4;
		for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
			if (dx == 0 && dz == 0) continue;
			h.assertTrue(!h.getLevel().getChunkSource().hasChunk(x + dx, z + dz), operation + " loaded a neighboring chunk " + dx + "," + dz);
		}
	}

	private static <T extends BlockEntity> T at(GameTestHelper h, T tile) {
		tile.setLevel(h.getLevel());
		return tile;
	}
}
