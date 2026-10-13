package io.github.lounode.extrabotany.common.fluid;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;

/**
 * 这个模组的流体。现在只有一种：液态魔力。
 * The fluids of this mod. At this time there is only one: Fluided Mana.
 *
 * <p>液态魔力转化器把魔力变成液态魔力，也把液态魔力变回魔力。1 mB 液态魔力等于 1000 点魔力。
 * <p>The Mana Liquefaction Device changes mana into Fluided Mana, and changes Fluided Mana back into mana. 1 mB of Fluided Mana is equal to 1000 mana.
 *
 * <p>注册 ID 是 {@code fluidedmana}、{@code flowing_fluidedmana} 和 {@code fluidedmana_bucket}。旧存档使用相同的 ID。
 * <p>The registry IDs are {@code fluidedmana}, {@code flowing_fluidedmana} and {@code fluidedmana_bucket}. Old saves use the same IDs.
 *
 * <p>来源：ExtraMeteorP 1.12 {@code ManaFluid}。粘度、温度和亮度和原版相同。
 * <p>Source: ExtraMeteorP 1.12 {@code ManaFluid}. The viscosity, the temperature and the light level are the same as in the original.
 */
public final class ExtraBotanyFluids {
	private static final int MANA_LIGHT_LEVEL = 12;

	public static final FluidType MANA_TYPE = new FluidType(FluidType.Properties.create()
			.lightLevel(MANA_LIGHT_LEVEL)
			.temperature(100)
			.viscosity(1200));
	public static final FlowingFluid MANA = new BaseFlowingFluid.Source(manaProperties());
	public static final FlowingFluid FLOWING_MANA = new BaseFlowingFluid.Flowing(manaProperties());
	public static final LiquidBlock MANA_BLOCK = new LiquidBlock(MANA, BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_PURPLE)
			.replaceable()
			.noCollission()
			.strength(100)
			.lightLevel(state -> MANA_LIGHT_LEVEL)
			.pushReaction(PushReaction.DESTROY)
			.noLootTable()
			.liquid());
	public static final Item MANA_BUCKET = new BucketItem(MANA, new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1));

	private static BaseFlowingFluid.Properties manaProperties() {
		return new BaseFlowingFluid.Properties(() -> MANA_TYPE, () -> MANA, () -> FLOWING_MANA)
				.bucket(() -> MANA_BUCKET)
				.block(() -> MANA_BLOCK)
				.slopeFindDistance(4)
				.levelDecreasePerBlock(1)
				.explosionResistance(100)
				.tickRate(5);
	}

	private ExtraBotanyFluids() {}
}
