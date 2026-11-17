package io.github.lounode.extrabotany.forge.gametest;

import io.github.lounode.extrabotany.common.block.ExtraBotanyBlocks;
import io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks;
import io.github.lounode.extrabotany.common.block.flower.functional.EnchanterBlockEntity;
import io.github.lounode.extrabotany.common.block.flower.functional.WoodieniaBlockEntity;
import io.github.lounode.extrabotany.common.block.flower.generating.StonesiaBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@GameTestHolder("extrabotany")
@PrefixGameTestTemplate(false)
public final class P2FlowerProtectionGameTests {

	private static final UUID UNOWNED = UUID.fromString("8aef4b0b-f184-4f8f-bc06-f18107aa9ee8");

	@GameTest(template = "projectile_arena", timeoutTicks = 40)
	public static void stonesiaHonorsOwnedAndOwnerlessBreakCancellation(GameTestHelper h) {
		var level = h.getLevel();
		for (boolean owned : new boolean[] {true, false}) {
			BlockPos pos = h.absolutePos(new BlockPos(owned ? 24 : 40, 5, 32));
			Player owner = h.makeMockPlayer(GameType.SURVIVAL);
			UUID expected = owned ? owner.getUUID() : UNOWNED;
			level.setBlock(pos.below(), Blocks.DIRT.defaultBlockState(), 18);
			level.setBlock(pos, ExtrabotanyFlowerBlocks.stonesia.defaultBlockState(), 18);
			var original = (StonesiaBlockEntity) level.getBlockEntity(pos);
			original.setPlacedBy(level, pos, original.getBlockState(), owned ? owner : null, ItemStack.EMPTY);
			var flower = reload(h, original, StonesiaBlockEntity.class);
			for (var candidate : BlockPos.betweenClosed(pos.offset(-1, 0, -1), pos.offset(1, 1, 1))) {
				if (!candidate.equals(pos)) level.setBlock(candidate, Blocks.STONE.defaultBlockState(), 18);
			}
			AtomicBoolean cancel = new AtomicBoolean(true);
			AtomicInteger events = new AtomicInteger();
			Consumer<BlockEvent.BreakEvent> listener = event -> {
				if (event.getLevel() == level && event.getPos().distManhattan(pos) <= 3) {
					h.assertTrue(event.getPlayer().getUUID().equals(expected), "Stonesia lost its persisted protection identity");
					events.incrementAndGet();
					event.setCanceled(cancel.get());
				}
			};
			NeoForge.EVENT_BUS.addListener(listener);
			try {
				flower.tickFlower();
				h.assertTrue(events.get() == 1 && countStone(h, pos) == 17 && flower.getMana() == 0 && flower.getCooldown() == 0,
						"Cancelled stone break changed blocks, generated mana, or started a cooldown");
				h.assertTrue(level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(3)).isEmpty(), "Cancelled stone break dropped items");
				cancel.set(false);
				flower.tickFlower();
				h.assertTrue(events.get() == 2 && countStone(h, pos) == 16
						&& flower.getMana() == flower.getManaFormBlock(Blocks.STONE.defaultBlockState())
						&& flower.getCooldown() == flower.getAfterWorkCooldown(), "Allowed stone break did not commit exactly once");
				h.assertTrue(level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(3)).isEmpty(), "Stonesia began dropping consumed stones");
			} finally {
				NeoForge.EVENT_BUS.unregister(listener);
				for (var candidate : BlockPos.betweenClosed(pos.offset(-1, 0, -1), pos.offset(1, 1, 1))) level.setBlock(candidate, Blocks.AIR.defaultBlockState(), 18);
				level.setBlock(pos.below(), Blocks.AIR.defaultBlockState(), 18);
			}
		}
		h.succeed();
	}

	@GameTest(template = "projectile_arena", timeoutTicks = 40)
	public static void woodieniaHonorsOwnedAndOwnerlessBreakCancellation(GameTestHelper h) {
		var level = h.getLevel();
		for (boolean owned : new boolean[] {true, false}) {
			// Keep real drops inside GameTest's forced, entity-accessible chunks.
			BlockPos pos = h.absolutePos(new BlockPos(owned ? 24 : 40, 5, 32));
			BlockPos log = pos.east(2);
			Player owner = h.makeMockPlayer(GameType.SURVIVAL);
			UUID expected = owned ? owner.getUUID() : UNOWNED;
			level.setBlock(pos.below(), Blocks.DIRT.defaultBlockState(), 18);
			level.setBlock(pos, ExtrabotanyFlowerBlocks.woodienia.defaultBlockState(), 18);
			level.setBlock(log, Blocks.OAK_LOG.defaultBlockState(), 18);
			var original = (WoodieniaBlockEntity) level.getBlockEntity(pos);
			original.setPlacedBy(level, pos, original.getBlockState(), owned ? owner : null, ItemStack.EMPTY);
			original.addMana(original.getManaPerUse() * 2);
			var flower = reload(h, original, WoodieniaBlockEntity.class);
			AtomicBoolean cancel = new AtomicBoolean(true);
			AtomicInteger events = new AtomicInteger();
			Consumer<BlockEvent.BreakEvent> listener = event -> {
				if (event.getLevel() == level && event.getPos().equals(log)) {
					h.assertTrue(event.getPlayer().getUUID().equals(expected), "Woodienia lost its persisted protection identity");
					events.incrementAndGet();
					event.setCanceled(cancel.get());
				}
			};
			List<ItemEntity> joinedDrops = new ArrayList<>();
			Consumer<EntityJoinLevelEvent> dropListener = event -> {
				if (event.getLevel() == level && event.getEntity() instanceof ItemEntity item
						&& new AABB(log).inflate(2).contains(item.position())) {
					joinedDrops.add(item);
				}
			};
			NeoForge.EVENT_BUS.addListener(listener);
			NeoForge.EVENT_BUS.addListener(dropListener);
			try {
				int mana = flower.getMana();
				flower.tickFlower();
				h.assertTrue(events.get() == 1 && level.getBlockState(log).is(Blocks.OAK_LOG)
						&& flower.getMana() == mana && flower.getCooldown() == 0, "Cancelled log break spent mana or removed the log");
				h.assertTrue(joinedDrops.isEmpty() && level.getEntitiesOfClass(ItemEntity.class, new AABB(log).inflate(2)).isEmpty(), "Cancelled log break dropped an item");
				cancel.set(false);
				flower.tickFlower();
				h.assertTrue(events.get() == 2 && level.getBlockState(log).isAir()
						&& flower.getMana() == mana - flower.getManaPerUse() && flower.getCooldown() == flower.getAfterEatCooldown(),
						"Allowed log break did not spend mana exactly once");
				int logs = level.getEntitiesOfClass(ItemEntity.class, new AABB(log).inflate(2)).stream()
						.filter(item -> item.getItem().is(Blocks.OAK_LOG.asItem())).mapToInt(item -> item.getItem().getCount()).sum();
				h.assertTrue(joinedDrops.size() == 1 && !joinedDrops.getFirst().isRemoved()
						&& level.getEntity(joinedDrops.getFirst().getUUID()) == joinedDrops.getFirst(),
						"Allowed log break did not join exactly one visible drop entity");
				h.assertTrue(logs == 1, "Allowed log break lost or duplicated its drop: " + logs);
			} finally {
				NeoForge.EVENT_BUS.unregister(listener);
				NeoForge.EVENT_BUS.unregister(dropListener);
				joinedDrops.forEach(ItemEntity::discard);
				level.getEntitiesOfClass(ItemEntity.class, new AABB(log).inflate(2)).forEach(ItemEntity::discard);
				for (var block : new BlockPos[] {pos, pos.below(), log}) level.setBlock(block, Blocks.AIR.defaultBlockState(), 18);
			}
		}
		h.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 40)
	public static void enchanterHonorsOwnedAndOwnerlessPlaceCancellation(GameTestHelper h) {
		var level = h.getLevel();
		for (boolean owned : new boolean[] {true, false}) {
			BlockPos pos = new BlockPos((owned ? 10280 : 10290) * 16 + 8, level.getMaxBuildHeight() - 10, 10280 * 16 + 8);
			BlockPos grass = pos.east(2);
			Player owner = h.makeMockPlayer(GameType.SURVIVAL);
			UUID expected = owned ? owner.getUUID() : UNOWNED;
			level.setBlock(pos.below(), Blocks.DIRT.defaultBlockState(), 18);
			level.setBlock(pos, ExtrabotanyFlowerBlocks.enchanter.defaultBlockState(), 18);
			level.setBlock(grass, Blocks.GRASS_BLOCK.defaultBlockState(), 18);
			var original = (EnchanterBlockEntity) level.getBlockEntity(pos);
			original.setPlacedBy(level, pos, original.getBlockState(), owned ? owner : null, ItemStack.EMPTY);
			original.setConsumedMana(original.getTransformCost());
			var flower = reload(h, original, EnchanterBlockEntity.class);
			AtomicBoolean cancel = new AtomicBoolean(true);
			AtomicInteger events = new AtomicInteger();
			Consumer<BlockEvent.EntityPlaceEvent> listener = event -> {
				if (event.getLevel() == level && event.getPos().equals(grass)) {
					h.assertTrue(event.getEntity() instanceof Player player && player.getUUID().equals(expected), "Enchanter lost its persisted protection identity");
					h.assertTrue(level.getBlockState(grass).is(Blocks.GRASS_BLOCK) && event.getBlockSnapshot().getState().is(Blocks.GRASS_BLOCK)
						&& event.getState().is(ExtraBotanyBlocks.enchantedSoil) && event.getPlacedBlock().is(ExtraBotanyBlocks.enchantedSoil),
						"Place event did not expose the proposed state before changing the old block");
					events.incrementAndGet();
					event.setCanceled(cancel.get());
				}
			};
			NeoForge.EVENT_BUS.addListener(listener);
			try {
				for (int tick = 0; tick < 20; tick++) flower.tickFlower();
				h.assertTrue(events.get() == 1 && level.getBlockState(grass).is(Blocks.GRASS_BLOCK)
						&& flower.getConsumedMana() == flower.getTransformCost(), "Cancelled soil conversion spent the saved charge or changed the block");
				cancel.set(false);
				for (int tick = 0; tick < 20; tick++) flower.tickFlower();
				h.assertTrue(events.get() == 2 && level.getBlockState(grass).is(ExtraBotanyBlocks.enchantedSoil)
						&& flower.getConsumedMana() == 0, "Allowed soil conversion did not commit exactly once");
			} finally {
				NeoForge.EVENT_BUS.unregister(listener);
				for (var block : new BlockPos[] {pos, pos.below(), grass}) level.setBlock(block, Blocks.AIR.defaultBlockState(), 18);
			}
		}
		h.succeed();
	}

	private static int countStone(GameTestHelper h, BlockPos center) {
		int count = 0;
		for (var pos : BlockPos.betweenClosed(center.offset(-1, 0, -1), center.offset(1, 1, 1))) {
			if (h.getLevel().getBlockState(pos).is(Blocks.STONE)) count++;
		}
		return count;
	}

	private static <T extends BlockEntity> T reload(GameTestHelper h, T original, Class<T> type) {
		var replacement = original.getType().create(original.getBlockPos(), original.getBlockState());
		replacement.loadWithComponents(original.saveWithoutMetadata(h.getLevel().registryAccess()), h.getLevel().registryAccess());
		h.getLevel().setBlockEntity(replacement);
		return type.cast(replacement);
	}
}
