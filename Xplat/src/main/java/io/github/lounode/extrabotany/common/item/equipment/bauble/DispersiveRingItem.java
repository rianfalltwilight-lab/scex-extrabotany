package io.github.lounode.extrabotany.common.item.equipment.bauble;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

import vazkii.botania.api.block_entity.FunctionalFlowerBlockEntity;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.item.equipment.bauble.BaubleItem;

public class DispersiveRingItem extends BaubleItem {

	private static final int RANGE = 7;
	private static final int DISPERSIVE_COOLDOWN = 20;

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

		BlockPos.betweenClosedStream(new AABB(player.blockPosition()).inflate(RANGE)).forEach(pos -> {
			BlockEntity tile = player.level().getBlockEntity(pos);
			if (tile instanceof FunctionalFlowerBlockEntity flower) {
				int mana = flower.getMaxMana() - flower.getMana();
				if (ManaItemHandler.instance().requestManaExact(stack, player, mana, true)) {
					flower.addMana(mana);
				}
			}
		});
	}

	public int getDispersiveCooldown() {
		return DISPERSIVE_COOLDOWN;
	}

	public int getRange() {
		return RANGE;
	}
}
