package io.github.lounode.extrabotany.common.block.flower;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import vazkii.botania.api.block_entity.SpecialFlowerBlockEntity;

import io.github.lounode.extrabotany.api.block.Pedestal;
import io.github.lounode.extrabotany.api.item.NatureEnergyItem;
import io.github.lounode.extrabotany.xplat.EXplatAbstractions;

/**
 * 1.12 的自然宝珠催化：花同一层、水平 7 格内的基座上放着能量不少于 {@code rate} 的自然宝珠时，花被"催化"，并且每 20 tick 从宝珠里扣 {@code rate} 能量。只有 1.12 独有的石中姬和镜姬用它。
 * The Nature Orb catalysis of 1.12: when a pedestal on the layer of the flower, 7 blocks or less away horizontally, holds a Nature Orb with at least {@code rate} energy, the flower is "catalysed", and each 20 ticks the orb loses {@code rate} energy. Only the Stonesia and the Mirrowtunia, which only 1.12 has, use it.
 *
 * <p>来源：ExtraMeteorP 1.12 {@code SubTileGeneratingNature} 和 {@code SubTileFunctionalNature}。1.12 每 tick 都查找；这里每 20 tick 查找一次（扣能量的间隔也是 20 tick），中间沿用上次的结果。
 * <p>Source: {@code SubTileGeneratingNature} and {@code SubTileFunctionalNature} of ExtraMeteorP 1.12. 1.12 searched on each tick; here the search runs one time for each 20 ticks (which is also the interval of the energy cost), and the ticks between keep the last result.
 */
public final class NatureCatalysis {
	public static final int RANGE = 7;
	public static final int INTERVAL = 20;

	private NatureCatalysis() {}

	/**
	 * 每 {@value #INTERVAL} tick 查找一次宝珠并扣能量，返回新的催化状态；其余的 tick 返回 {@code previous}。
	 * Searches for an orb and takes the energy one time for each {@value #INTERVAL} ticks, and returns the new catalysis state; on the other ticks it returns {@code previous}.
	 */
	public static boolean update(SpecialFlowerBlockEntity flower, long ticksExisted, int rate, boolean previous) {
		if (ticksExisted % INTERVAL != 0) {
			return previous;
		}
		Level level = flower.getLevel();
		BlockPos center = flower.getEffectivePos();
		for (int x = -RANGE; x < RANGE; x++) {
			for (int z = -RANGE; z < RANGE; z++) {
				BlockPos pos = center.offset(x, 0, z);
				if (!level.isLoaded(pos) || !(level.getBlockEntity(pos) instanceof Pedestal pedestal)) {
					continue;
				}
				ItemStack stack = pedestal.getItem();
				NatureEnergyItem orb = stack.isEmpty() ? null : EXplatAbstractions.INSTANCE.findNatureEnergyItem(stack);
				if (orb != null && orb.getEnergy() >= rate) {
					orb.addEnergy(-rate);
					((BlockEntity) pedestal).setChanged();
					return true;
				}
			}
		}
		return false;
	}
}
