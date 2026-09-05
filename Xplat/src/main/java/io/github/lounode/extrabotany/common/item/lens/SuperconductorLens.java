package io.github.lounode.extrabotany.common.item.lens;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.ItemStack;
import vazkii.botania.api.mana.BurstProperties;
import vazkii.botania.common.item.lens.Lens;

/** scex.1 burst capacity, speed and loss multipliers. */
public final class SuperconductorLens extends Lens {
    @Override public void apply(ItemStack stack, BurstProperties burst) {
        burst.maxMana *= 8;
        burst.motionModifier *= 1.5F;
        burst.manaLossPerTick *= 16;
        burst.ticksBeforeManaLoss = (int) (burst.ticksBeforeManaLoss * 0.8F);
        burst.color = ChatFormatting.BLUE.getColor();
    }
}
