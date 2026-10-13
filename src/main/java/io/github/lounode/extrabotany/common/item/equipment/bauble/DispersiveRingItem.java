package io.github.lounode.extrabotany.common.item.equipment.bauble;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

import vazkii.botania.api.block_entity.FunctionalFlowerBlockEntity;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.item.equipment.bauble.BaubleItem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 弥散指环。每秒它用佩戴者身上的魔力，把周围 7 格内的功能花补满。
 * Dispersive Ring. On each second it uses the mana of the wearer to fill the functional flowers in 7 blocks.
 */
public class DispersiveRingItem extends BaubleItem {

	private static final int RANGE = 7;
	private static final int DISPERSIVE_COOLDOWN = 20;
	private static final Comparator<BlockEntity> LEGACY_ORDER = Comparator
			.<BlockEntity>comparingInt(be -> be.getBlockPos().getX())
			.thenComparingInt(be -> be.getBlockPos().getY())
			.thenComparingInt(be -> be.getBlockPos().getZ());

	public DispersiveRingItem(Properties props) {
		super(props);
	}

	@Override
	public void onWornTick(ItemStack stack, LivingEntity entity) {
		super.onWornTick(stack, entity);
		if (entity.level().isClientSide() || !(entity instanceof Player player)) {
			return;
		}
		if (player.tickCount % getDispersiveCooldown() != 0) {
			return;
		}

		BlockPos origin = player.blockPosition();
		int range = getRange();
		// Legacy half-open bounds [-range, range) on every axis, filled in X/Y/Z priority
		// order when mana is scarce. Instead of probing 14^3 = 2,744 positions with
		// hasChunkAt + getBlockEntity, walk only the block entities of the (at most 4)
		// loaded chunks that overlap the box, then restore the legacy ordering.
		int minX = origin.getX() - range, maxX = origin.getX() + range - 1;
		int minY = origin.getY() - range, maxY = origin.getY() + range - 1;
		int minZ = origin.getZ() - range, maxZ = origin.getZ() + range - 1;
		Level level = player.level();
		List<FunctionalFlowerBlockEntity> flowers = new ArrayList<>();
		for (int cx = SectionPos.blockToSectionCoord(minX); cx <= SectionPos.blockToSectionCoord(maxX); cx++) {
			for (int cz = SectionPos.blockToSectionCoord(minZ); cz <= SectionPos.blockToSectionCoord(maxZ); cz++) {
				if (!(level.getChunkSource().getChunkNow(cx, cz) instanceof LevelChunk chunk)) {
					continue;
				}
				for (BlockEntity tile : chunk.getBlockEntities().values()) {
					BlockPos p = tile.getBlockPos();
					if (tile instanceof FunctionalFlowerBlockEntity flower && !tile.isRemoved()
							&& p.getX() >= minX && p.getX() <= maxX
							&& p.getY() >= minY && p.getY() <= maxY
							&& p.getZ() >= minZ && p.getZ() <= maxZ) {
						flowers.add(flower);
					}
				}
			}
		}
		if (flowers.isEmpty()) {
			return;
		}
		flowers.sort(LEGACY_ORDER);
		for (FunctionalFlowerBlockEntity flower : flowers) {
			int mana = flower.getMaxMana() - flower.getMana();
			if (mana > 0 && ManaItemHandler.instance().requestManaExact(stack, player, mana, true)) {
				flower.addMana(mana);
			}
		}
	}

	public int getDispersiveCooldown() {
		return DISPERSIVE_COOLDOWN;
	}

	public int getRange() {
		return RANGE;
	}
}
