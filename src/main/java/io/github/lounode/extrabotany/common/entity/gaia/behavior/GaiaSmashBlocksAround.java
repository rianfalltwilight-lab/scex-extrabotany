package io.github.lounode.extrabotany.common.entity.gaia.behavior;

import com.google.common.collect.ImmutableMap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import vazkii.botania.common.lib.BotaniaTags;

import io.github.lounode.extrabotany.common.entity.gaia.Gaia;

import java.util.Arrays;
import java.util.List;

/**
 * 每 tick 破坏 Boss 周围 3×4×3 范围里的方块。地板、水晶所在的四根柱子，以及带有 {@code botania:gaia_guardian_immune} 标签的方块不会被破坏。
 * On each tick, this breaks the blocks in the 3×4×3 area around the boss. The floor, the four columns of the pylons, and the blocks with the tag {@code botania:gaia_guardian_immune} are not broken.
 */
public class GaiaSmashBlocksAround<E extends Gaia> extends Behavior<E> {

	protected static final TagKey<Block> BLACKLIST = BotaniaTags.Blocks.GAIA_GUARDIAN_IMMUNE;
	protected static final List<ResourceLocation> CHEATY_BLOCKS = Arrays.asList(
			ResourceLocation.tryBuild("openblocks", "beartrap"),
			ResourceLocation.tryBuild("thaumictinkerer", "magnet")
	);

	public GaiaSmashBlocksAround() {
		super(ImmutableMap.of(

		));
	}

	@Override
	protected boolean canStillUse(ServerLevel level, E entity, long gameTime) {
		return true;
	}

	@Override
	protected void tick(ServerLevel level, E gaia, long gameTime) {
		smashBlocksAround(gaia, Mth.floor(gaia.getX()), Mth.floor(gaia.getY()), Mth.floor(gaia.getZ()), 1);
	}

	/**
	 * 破坏以这个位置为中心的方块。水平方向和向下是 {@code radius} 格，向上多一格。Boss 沿着传送的路线破坏方块时也使用这个方法。
	 * Breaks the blocks around this position. The range is {@code radius} blocks horizontally and down, and one block more up. A boss also uses this method to break the blocks along the path of a teleport.
	 */
	public static void smashBlocksAround(Gaia gaia, int centerX, int centerY, int centerZ, int radius) {
		for (int dx = -radius; dx <= radius; dx++) {
			for (int dy = -radius; dy <= radius + 1; dy++) {
				for (int dz = -radius; dz <= radius; dz++) {
					int x = centerX + dx;
					int y = centerY + dy;
					int z = centerZ + dz;

					BlockPos pos = new BlockPos(x, y, z);
					BlockState state = gaia.level().getBlockState(pos);
					if (state.isAir()) {
						continue;
					}
					Block block = state.getBlock();

					if (state.getDestroySpeed(gaia.level(), pos) == -1) {
						continue;
					}

					if (CHEATY_BLOCKS.contains(BuiltInRegistries.BLOCK.getKey(block))) {
						gaia.level().destroyBlock(pos, true);
						continue;
					}

					//don't break blacklisted blocks
					if (state.is(BLACKLIST)) {
						continue;
					}
					//don't break the floor
					if (y < gaia.getHome().pos().getY()) {
						continue;
					}
					//don't break blocks in pylon columns
					if (Math.abs(gaia.getHome().pos().getX() - x) == 4 && Math.abs(gaia.getHome().pos().getZ() - z) == 4) {
						continue;
					}

					gaia.level().destroyBlock(pos, true);
				}
			}
		}
	}

}
