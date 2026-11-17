package io.github.lounode.extrabotany.forge.gametest;

import io.github.lounode.extrabotany.common.entity.projectile.FlowerWeaponEntity;
import io.github.lounode.extrabotany.common.item.relic.RelicSwordItem;
import io.github.lounode.extrabotany.common.util.LoadedAreaRaycast;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("extrabotany")
@PrefixGameTestTemplate(false)
public final class P2RaycastGameTests {

	@GameTest(template = "empty", timeoutTicks = 40)
	public static void longSwordRaysStopBeforeTrulyUnloadedChunks(GameTestHelper h) {
		var level = h.getLevel();
		int cx = 10000, cz = 10000;
		level.getChunk(cx, cz); // Load only this fixture's origin; every horizontal neighbor must stay absent.
		Player user = h.makeMockPlayer(GameType.SURVIVAL);
		user.setPos(cx * 16 + 8, level.getMaxBuildHeight() - 10, cz * 16 + 8);
		for (float yaw : new float[] {0, 90, 180, -90}) {
			user.setYRot(yaw);
			user.setXRot(0);
			Vec3 eye = user.getEyePosition(1F);
			for (double range : new double[] {64, 80}) {
				Vec3 end = eye.add(user.getViewVector(1F).scale(range));
				h.assertTrue(!level.hasChunkAt(BlockPos.containing(end)), "Ray endpoint was already loaded");
				Vec3 aim = SwordProbe.aim(user, range, false);
				h.assertTrue(level.hasChunkAt(BlockPos.containing(aim)) && aim.distanceTo(eye) < 10,
						"Sword aim did not stop on the loaded side of the boundary");
				for (int step = 1; step <= 5; step++) {
					BlockPos probe = BlockPos.containing(eye.add(user.getViewVector(1F).scale(16 * step)));
					h.assertTrue(!level.hasChunkAt(probe), "Sword ray synchronously loaded a neighboring chunk");
				}
			}
		}
		h.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 40)
	public static void loadedRaysKeepVanillaBlockFluidAndMissResults(GameTestHelper h) {
		var level = h.getLevel();
		int cx = 10010, cz = 10010;
		level.getChunk(cx, cz);
		BlockPos water = new BlockPos(cx * 16 + 8, level.getMaxBuildHeight() - 10, cz * 16 + 8);
		BlockPos stone = water.east(2);
		level.setBlockAndUpdate(water, Blocks.WATER.defaultBlockState());
		level.setBlockAndUpdate(stone, Blocks.STONE.defaultBlockState());
		Player user = h.makeMockPlayer(GameType.SURVIVAL);
		Vec3 start = water.west(2).getCenter();
		Vec3 end = stone.east(2).getCenter();
		try {
			for (ClipContext.Block block : new ClipContext.Block[] {ClipContext.Block.COLLIDER, ClipContext.Block.OUTLINE}) {
				for (ClipContext.Fluid fluid : new ClipContext.Fluid[] {ClipContext.Fluid.NONE, ClipContext.Fluid.ANY}) {
					var context = new ClipContext(start, end, block, fluid, user);
					var expected = level.clip(context);
					var actual = LoadedAreaRaycast.clip(level, context);
					h.assertTrue(actual.getType() == expected.getType() && actual.getBlockPos().equals(expected.getBlockPos())
							&& actual.getDirection() == expected.getDirection() && actual.getLocation().distanceToSqr(expected.getLocation()) < 1E-12,
							"Loaded terrain/fluid ray no longer matches vanilla");
					h.assertTrue(actual.getBlockPos().equals(fluid == ClipContext.Fluid.ANY ? water : stone),
							"Fixture did not exercise the expected water/solid intersection");
				}
			}
			var miss = new ClipContext(start.add(0, 2, 0), end.add(0, 2, 0), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, user);
			h.assertTrue(LoadedAreaRaycast.clip(level, miss).getType() == HitResult.Type.MISS
					&& LoadedAreaRaycast.clip(level, miss).getLocation().equals(miss.getTo()), "Loaded miss was shortened");
		} finally {
			level.setBlockAndUpdate(water, Blocks.AIR.defaultBlockState());
			level.setBlockAndUpdate(stone, Blocks.AIR.defaultBlockState());
		}
		h.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 40)
	public static void flowerWeaponLaunchUsesTheLoadedBoundary(GameTestHelper h) {
		var level = h.getLevel();
		int cx = 10020, cz = 10020;
		level.getChunk(cx, cz);
		Player user = h.makeMockPlayer(GameType.SURVIVAL);
		user.setPos(cx * 16 + 8, level.getMaxBuildHeight() - 10, cz * 16 + 8);
		user.setYRot(0);
		user.setXRot(0);
		Vec3 eye = user.getEyePosition();
		var context = new ClipContext(eye, eye.add(0, 0, 64), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, user);
		h.assertTrue(!level.hasChunkAt(BlockPos.containing(context.getTo())), "Weapon endpoint was already loaded");
		var weapon = new FlowerWeaponEntity(level, user, 0, 0, 0);
		weapon.setPos(eye.add(3, 0, 0));
		Vec3 expected = LoadedAreaRaycast.clip(level, context).getLocation().subtract(weapon.position()).normalize().scale(2);
		weapon.tick();
		h.assertTrue(!weapon.isRemoved() && weapon.getLiveTicks() == 1 && weapon.getDeltaMovement().distanceToSqr(expected) < 1E-10,
				"Actual flower-weapon launch ignored the shortened ray");
		h.assertTrue(!level.hasChunkAt(new BlockPos(cx * 16 + 8, 0, (cz + 1) * 16 + 8)), "Weapon aiming loaded its neighboring chunk");
		weapon.discard();
		h.succeed();
	}

	private abstract static class SwordProbe extends RelicSwordItem {
		private SwordProbe() { super(Tiers.DIAMOND, new Properties()); }
		static Vec3 aim(LivingEntity user, double range, boolean fluids) { return aimPosition(user, null, range, fluids); }
	}
}
