package io.github.lounode.extrabotany.forge.fluid;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;

/** Maintained implementation of the archived SCEX mana fluid's registry and flow contract. */
public final class LegacyManaFluid {
    public static final FluidType TYPE = new FluidType(FluidType.Properties.create()
            .lightLevel(12).temperature(100).viscosity(1200));
    public static final FlowingFluid SOURCE = new BaseFlowingFluid.Source(properties());
    public static final FlowingFluid FLOWING = new BaseFlowingFluid.Flowing(properties());
    public static final LiquidBlock BLOCK = new ManaBlock();
    public static final Item BUCKET = new BucketItem(SOURCE,
            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1));

    private static BaseFlowingFluid.Properties properties() {
        return new BaseFlowingFluid.Properties(() -> TYPE, () -> SOURCE, () -> FLOWING)
                .bucket(() -> BUCKET).block(() -> BLOCK).slopeFindDistance(4)
                .levelDecreasePerBlock(1).explosionResistance(100).tickRate(5);
    }

    private static final class ManaBlock extends LiquidBlock {
        private ManaBlock() {
            super(SOURCE, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE)
                    .replaceable().noCollission().strength(100).lightLevel(state -> 12)
                    .noLootTable().liquid());
        }
    }

    private LegacyManaFluid() {}
}
