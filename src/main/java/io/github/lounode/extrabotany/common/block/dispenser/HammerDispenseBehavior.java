package io.github.lounode.extrabotany.common.block.dispenser;

import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;

import io.github.lounode.extrabotany.common.block.block_entity.PedestalBlockEntity;

/**
 * 发射器朝着自然祭坛时，把锤子敲在祭坛上：每次发射敲 {@value #STRIKES} 下，每下用掉锤子 1 点耐久。前面不是祭坛时照常发射。
 * When a dispenser faces a Nature Pedestal, it strikes the pedestal with the hammer: {@value #STRIKES} strikes for each dispense, each using 1 durability of the hammer. When the block in front is not a pedestal, it dispenses as usual.
 *
 * <p>来源：ExtraMeteorP 1.12 {@code BehaviourHammer}（锤子 +5 耐久损耗，祭坛 +5 次敲击）。
 * <p>Source: {@code BehaviourHammer} of ExtraMeteorP 1.12 (+5 wear on the hammer, +5 strikes on the pedestal).
 */
public class HammerDispenseBehavior extends DefaultDispenseItemBehavior {
	public static final int STRIKES = 5;

	@Override
	protected ItemStack execute(BlockSource source, ItemStack stack) {
		BlockPos pos = source.pos().relative(source.state().getValue(DispenserBlock.FACING));
		if (!(source.level().getBlockEntity(pos) instanceof PedestalBlockEntity pedestal)) {
			return super.execute(source, stack);
		}
		for (int i = 0; i < STRIKES && !stack.isEmpty(); i++) {
			if (!pedestal.tryAutoSmash(stack)) {
				break;
			}
		}
		source.level().playSound(null, pos, SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS, 0.5F,
				source.level().getRandom().nextFloat() * 0.2F + 0.9F);
		return stack;
	}
}
