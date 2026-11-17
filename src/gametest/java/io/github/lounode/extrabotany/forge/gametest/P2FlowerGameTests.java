package io.github.lounode.extrabotany.forge.gametest;

import io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks;
import io.github.lounode.extrabotany.common.block.flower.functional.TradeOrchidBlockEntity;
import io.github.lounode.extrabotany.common.block.flower.generating.TwinstarBlockEntity;
import io.github.lounode.extrabotany.common.brew.ExtraBotanyMobEffects;
import io.github.lounode.extrabotany.xplat.EXplatAbstractions;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.botania.api.block_entity.RadiusDescriptor;

@GameTestHolder("extrabotany")
@PrefixGameTestTemplate(false)
public final class P2FlowerGameTests {

	@GameTest(template = "empty", timeoutTicks = 40)
	public static void twinstarUsesActualColdEndAndClampsBothLimits(GameTestHelper h) {
		var level = h.getLevel();
		BlockPos pos = new BlockPos(10200 * 16 + 8, level.getMaxBuildHeight() - 10, 10200 * 16 + 8);
		level.setBlock(pos.below(), Blocks.DIRT.defaultBlockState(), 18);
		level.setBlock(pos, ExtrabotanyFlowerBlocks.twinstar.defaultBlockState(), 18);
		var flower = (TwinstarBlockEntity) level.getBlockEntity(pos);
		try {
			h.assertTrue(flower.getThermoelectric() == 0, "No fluid produced a temperature difference");
			level.setBlock(pos.west(), Blocks.WATER.defaultBlockState(), 18);
			h.assertTrue(flower.getThermoelectric() == 0 && flower.getGenerateMana() == 0,
					"A single fluid used the configured minimum instead of its actual temperature");
			level.setBlock(pos.east(), Blocks.WATER.defaultBlockState(), 18);
			h.assertTrue(flower.getThermoelectric() == 0, "Equal-temperature fluids produced mana");
			level.setBlock(pos.east(), Blocks.LAVA.defaultBlockState(), 18);
			int cold = EXplatAbstractions.INSTANCE.getFluidTemperature(Fluids.WATER);
			int hot = EXplatAbstractions.INSTANCE.getFluidTemperature(Fluids.LAVA);
			h.assertTrue(hot - cold > 200, "Fixture does not contain distinct hot and cold fluids");
			int difference = Math.max(0, Math.min(flower.getTemperatureMax(), hot) - Math.max(flower.getTemperatureMin(), cold));
			h.assertTrue(flower.getThermoelectric() == difference && flower.getGenerateMana() == difference / 100,
					"Twinstar does not subtract the observed cold endpoint");
			for (int tick = 0; tick < TwinstarBlockEntity.COOLDOWN; tick++) flower.tickFlower();
			h.assertTrue(flower.getMana() == difference / 100, "Actual generating tick did not use the corrected temperature difference");
			var clamped = new TemperatureBounds(pos, cold + 100, hot - 100);
			clamped.setLevel(level);
			h.assertTrue(clamped.getThermoelectric() == hot - cold - 200, "Configured endpoints did not clamp both observed temperatures");
			var reversed = new TemperatureBounds(pos, hot, cold);
			reversed.setLevel(level);
			h.assertTrue(reversed.getThermoelectric() == 0, "Reversed temperature limits produced negative mana");
		} finally {
			for (var block : new BlockPos[] {pos, pos.below(), pos.west(), pos.east()}) level.setBlock(block, Blocks.AIR.defaultBlockState(), 18);
		}
		h.succeed();
	}

	@GameTest(template = "projectile_arena", timeoutTicks = 40)
	public static void neighboringTradeOrchidsDoNotEraseDiscountsAndEffectsStillExpire(GameTestHelper h) {
		var level = h.getLevel();
		// Entity queries require an accessible entity section, not merely a FULL block chunk.
		BlockPos firstPos = h.absolutePos(new BlockPos(24, 5, 32));
		BlockPos secondPos = firstPos.east(10);
		Villager villager = EntityType.VILLAGER.create(level);
		for (var pos : new BlockPos[] {firstPos, secondPos}) {
			level.setBlock(pos.below(), Blocks.DIRT.defaultBlockState(), 18);
			level.setBlock(pos, ExtrabotanyFlowerBlocks.tradeOrchid.defaultBlockState(), 18);
		}
		var first = (TradeOrchidBlockEntity) level.getBlockEntity(firstPos);
		var second = (TradeOrchidBlockEntity) level.getBlockEntity(secondPos);
		villager.setNoAi(true);
		villager.setNoGravity(true);
		villager.setPos(firstPos.getX() + 0.5, firstPos.getY() - 0.25, firstPos.getZ() + 0.5);
		h.assertTrue(level.addFreshEntity(villager), "Could not add the trading fixture");
		try {
			h.assertTrue(level.getEntitiesOfClass(Villager.class, ((RadiusDescriptor.Rectangle) first.getRadius()).aabb()).contains(villager),
					"Trading fixture is not visible to the flower's actual entity query");
			first.addMana(first.getMaxMana());
			second.addMana(second.getMaxMana());
			for (int tick = 0; tick < first.getCooldown(); tick++) first.tickFlower();
			h.assertTrue(villager.hasEffect(ExtraBotanyMobEffects.DISCOUNT), "First flower did not apply the actual discount");
			for (int tick = 0; tick < second.getCooldown(); tick++) second.tickFlower();
			h.assertTrue(villager.hasEffect(ExtraBotanyMobEffects.DISCOUNT), "Neighbor's outer cleanup erased the first flower's discount");
			villager.setPos(firstPos.getX() + 5.5, firstPos.getY() - 0.25, firstPos.getZ() + 0.5);
			for (int tick = 0; tick < first.getCooldown(); tick++) first.tickFlower();
			level.removeBlock(secondPos, false);
			h.assertTrue(villager.hasEffect(ExtraBotanyMobEffects.DISCOUNT), "Removing the second flower erased an overlapping flower's discount");
			int remaining = villager.getEffect(ExtraBotanyMobEffects.DISCOUNT).getDuration();
			// Tick only the villager: no flower refresh and no global game-time mutation.
			for (int tick = 0; tick <= remaining; tick++) villager.tick();
			h.assertTrue(!villager.hasEffect(ExtraBotanyMobEffects.DISCOUNT), "Unmaintained discounts did not naturally expire");
		} finally {
			villager.discard();
			for (var pos : new BlockPos[] {firstPos, secondPos, firstPos.below(), secondPos.below()}) level.setBlock(pos, Blocks.AIR.defaultBlockState(), 18);
		}
		h.succeed();
	}

	private static final class TemperatureBounds extends TwinstarBlockEntity {
		private final int min, max;
		private TemperatureBounds(BlockPos pos, int min, int max) {
			super(pos, ExtrabotanyFlowerBlocks.twinstar.defaultBlockState());
			this.min = min;
			this.max = max;
		}
		@Override public int getTemperatureMin() { return min; }
		@Override public int getTemperatureMax() { return max; }
	}
}
