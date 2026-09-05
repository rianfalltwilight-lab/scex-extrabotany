package io.github.lounode.extrabotany.common.block.legacy;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.block.BotaniaWaterloggedBlock;
import java.util.LinkedHashMap;
import java.util.Map;

public final class LegacyMachineBlock extends BotaniaWaterloggedBlock implements EntityBlock {
    public enum Kind { BUFFER("manabuffer"), QUANTUM("quantummanabuffer"), GENERATOR("managenerator"), LIQUEFACTION("manaliquefaction");
        public final String id; Kind(String id) { this.id=id; } }
    public static final Map<Kind,LegacyMachineBlock> BLOCKS=new LinkedHashMap<>();
    public static final Map<Kind,BlockEntityType<LegacyMachineEntity>> TYPES=new LinkedHashMap<>();
    static {
        for(var kind:Kind.values()) {
            var properties=BlockBehaviour.Properties.ofFullCopy(BotaniaBlocks.LIVINGROCK).requiresCorrectToolForDrops();
            switch(kind) {
                case BUFFER -> properties.strength(5.5F,10).mapColor(MapColor.TERRACOTTA_CYAN);
                case QUANTUM -> properties.strength(3,55).mapColor(MapColor.TERRACOTTA_PURPLE).noOcclusion();
                case GENERATOR -> properties.strength(2,10).mapColor(MapColor.LAPIS);
                case LIQUEFACTION -> properties.strength(2,10).mapColor(MapColor.COLOR_PURPLE);
            }
            var block=new LegacyMachineBlock(kind,properties); BLOCKS.put(kind,block);
            TYPES.put(kind,BlockEntityType.Builder.of((pos,state)->new LegacyMachineEntity(kind,pos,state),block).build(null));
        }
    }
    private final Kind kind;
    private LegacyMachineBlock(Kind kind,Properties properties) { super(properties);this.kind=kind; }
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state) { return new LegacyMachineEntity(kind,pos,state); }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type) {
        return level.isClientSide()?null:createTickerHelper(type,TYPES.get(kind),(world,pos,block,entity)->entity.serverTick());
    }
}
