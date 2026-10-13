package io.github.lounode.extrabotany.forge.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import vazkii.botania.common.block.BotaniaBlocks;

import io.github.lounode.extrabotany.common.block.ExtraBotanyBlocks;
import io.github.lounode.extrabotany.common.block.block_entity.PedestalBlockEntity;
import io.github.lounode.extrabotany.common.block.flower.functional.ManalinkBlockEntity;

/**
 * 多方块结构的回归测试：祭坛的两级结构和魔链星的结构。
 * Regression tests for the multiblock structures: the two tiers of the pedestal structure, and the structure of the Manalink.
 */
@GameTestHolder("extrabotany")
@PrefixGameTestTemplate(false)
public final class StructureGameTests {
	private static final BlockPos CENTER = new BlockPos(32, 5, 32);

	private StructureGameTests() {}

	private static PedestalBlockEntity pedestal(GameTestHelper helper, BlockPos pos) {
		helper.setBlock(pos, ExtraBotanyBlocks.livingrockPedestal);
		return (PedestalBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(pos));
	}

	/**
	 * 一级结构：祭坛的四个斜角方向、距离两格的位置各有一个自然水晶。祭坛必须在中心。
	 * The tier 1 structure: one Natura Pylon at each of the four diagonal positions, two blocks from the pedestal. The pedestal must be at the center.
	 */
	@GameTest(template = "projectile_arena", timeoutTicks = 20)
	public static void pedestalTierOneNeedsThePedestalAtTheCenter(GameTestHelper helper) {
		PedestalBlockEntity center = pedestal(helper, CENTER);
		PedestalBlockEntity beside = pedestal(helper, CENTER.offset(-1, 0, -1));
		center.updateTier();
		helper.assertTrue(center.getTier() == 0, "A pedestal without a structure has a tier");

		for (int x : new int[] { -2, 2 }) {
			for (int z : new int[] { -2, 2 }) {
				helper.setBlock(CENTER.offset(x, 0, z), BotaniaBlocks.NATURA_PYLON);
			}
		}
		center.updateTier();
		beside.updateTier();
		helper.assertTrue(center.getTier() == 1, "The pedestal at the center of the tier 1 structure is not tier 1");
		helper.assertTrue(beside.getTier() == 0, "A pedestal that is not at the center of the structure got a tier");

		helper.setBlock(CENTER.offset(2, 0, -2), Blocks.AIR);
		center.updateTier();
		helper.assertTrue(center.getTier() == 0, "The tier stayed after a pylon was removed");
		helper.succeed();
	}

	/**
	 * 二级结构：下面一层是闪光石围成的方框，祭坛这一层有四个自然水晶和四个魔力池，上面一层有四个自然水晶。
	 * The tier 2 structure: a frame of Shimmerrock in the layer below, four Natura Pylons and four mana pools in the layer of the pedestal, and four Natura Pylons in the layer above.
	 */
	@GameTest(template = "projectile_arena", timeoutTicks = 20)
	public static void pedestalTierTwoStructure(GameTestHelper helper) {
		PedestalBlockEntity center = pedestal(helper, CENTER);
		for (int x = -2; x <= 2; x++) {
			for (int z = -2; z <= 2; z++) {
				if (Math.max(Math.abs(x), Math.abs(z)) == 2) {
					helper.setBlock(CENTER.offset(x, -1, z), BotaniaBlocks.SHIMMERROCK);
				}
			}
		}
		for (int x : new int[] { -1, 1 }) {
			for (int z : new int[] { -1, 1 }) {
				helper.setBlock(CENTER.offset(2 * x, 0, 2 * z), BotaniaBlocks.NATURA_PYLON);
				helper.setBlock(CENTER.offset(3 * x, 0, 3 * z), BotaniaBlocks.MANA_POOL);
				helper.setBlock(CENTER.offset(3 * x, 1, 3 * z), BotaniaBlocks.NATURA_PYLON);
			}
		}
		center.updateTier();
		helper.assertTrue(center.getTier() == 2, "The pedestal at the center of the tier 2 structure is not tier 2");

		helper.setBlock(CENTER.offset(3, 1, 3), Blocks.AIR);
		center.updateTier();
		helper.assertTrue(center.getTier() == 1, "Without one upper pylon the structure must be tier 1");
		helper.succeed();
	}

	/**
	 * 魔链星的结构在花的下面：中心是泥土，四周是黑曜石，四角是青金石块，外圈是石英块。
	 * The structure of the Manalink is below the flower: dirt at the center, obsidian at the four sides, lapis blocks at the four corners, and quartz blocks in the outer ring.
	 */
	@GameTest(template = "projectile_arena", timeoutTicks = 20)
	public static void manalinkStructure(GameTestHelper helper) {
		BlockPos base = CENTER.below();
		for (int x = -2; x <= 2; x++) {
			for (int z = -2; z <= 2; z++) {
				int ring = Math.max(Math.abs(x), Math.abs(z));
				if (ring == 2) {
					helper.setBlock(base.offset(x, 0, z), Blocks.QUARTZ_BLOCK);
				} else if (ring == 0) {
					helper.setBlock(base.offset(x, 0, z), Blocks.DIRT);
				} else if (x != 0 && z != 0) {
					helper.setBlock(base.offset(x, 0, z), Blocks.LAPIS_BLOCK);
				} else {
					helper.setBlock(base.offset(x, 0, z), Blocks.OBSIDIAN);
				}
			}
		}
		BlockPos origin = helper.absolutePos(base).offset(2, 0, 2);
		helper.assertTrue(ManalinkBlockEntity.PATTERN_BUILTIN.matches(helper.getLevel(), origin, Direction.DOWN, Direction.SOUTH) != null,
				"The complete Manalink structure was not accepted");

		helper.setBlock(base.offset(1, 0, 1), Blocks.OBSIDIAN);
		helper.assertTrue(ManalinkBlockEntity.PATTERN_BUILTIN.matches(helper.getLevel(), origin, Direction.DOWN, Direction.SOUTH) == null,
				"A Manalink structure with an incorrect block was accepted");
		helper.succeed();
	}
}
