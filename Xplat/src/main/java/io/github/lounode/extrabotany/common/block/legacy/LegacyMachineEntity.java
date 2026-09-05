package io.github.lounode.extrabotany.common.block.legacy;

import io.github.lounode.extrabotany.xplat.ExtraBotanyConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import vazkii.botania.api.block.Wandable;
import vazkii.botania.api.mana.ManaReceiver;
import vazkii.botania.api.mana.ManaPool;
import vazkii.botania.api.mana.spark.ManaSparkAttachable;
import vazkii.botania.common.block.block_entity.mana.ManaPoolBlockEntity;
import vazkii.botania.common.block.block_entity.mana.ManaSpreaderBlockEntity;

public final class LegacyMachineEntity extends BlockEntity implements ManaReceiver,ManaSparkAttachable,Wandable {
    public final LegacyMachineBlock.Kind kind;
    private int mana,energy,ticks;
    private boolean pending;
    public LegacyMachineEntity(LegacyMachineBlock.Kind kind,BlockPos pos,BlockState state) { super(LegacyMachineBlock.TYPES.get(kind),pos,state);this.kind=kind; }
    private boolean buffer() { return kind==LegacyMachineBlock.Kind.BUFFER||kind==LegacyMachineBlock.Kind.QUANTUM; }
    private int setting(String name,int fallback) { var config=ExtraBotanyConfig.common(); return config==null?fallback:config.legacyMachineSetting(name,fallback); }
    public int getMaxMana() { return switch(kind) { case BUFFER->64000000;case QUANTUM->1024000000;case GENERATOR->1000000;case LIQUEFACTION->setting("manaLiquefaction.maxMana",1000000); }; }
    public int getEnergyStored() { return energy; }
    private int maxEnergy() { return kind==LegacyMachineBlock.Kind.GENERATOR?setting("manaGenerator.maxEnergy",40000):16000; }
    public int transferSpeed() { return buffer()?(kind==LegacyMachineBlock.Kind.QUANTUM?5000:1000):setting("manaGenerator.transferSpeed",200); }
    public void serverTick() {
        if(level==null||level.isClientSide())return;
        var config=ExtraBotanyConfig.common();
        if(!buffer()&&config!=null&&!config.legacyMachineEnabled(kind==LegacyMachineBlock.Kind.GENERATOR?"manaGenerator":"manaLiquefaction"))return;
        int beforeMana=mana,beforeEnergy=energy;
        if(buffer()) {
            for(var side:new Direction[]{Direction.NORTH,Direction.SOUTH,Direction.WEST,Direction.EAST,Direction.DOWN}) {
                var source=endpoint(worldPosition.relative(side));
                if(source!=null&&source!=this) { int amount=Math.min(transferSpeed(),Math.min(source.getCurrentMana(),getAvailableSpaceForMana()));if(amount>0){source.receiveMana(-amount);receiveMana(amount);} }
                if(isFull())break;
            }
            var target=endpoint(worldPosition.above());
            int space=target instanceof ManaPool pool?pool.getMaxMana()-pool.getCurrentMana():target instanceof LegacyMachineEntity machine?machine.getAvailableSpaceForMana():0;
            int amount=Math.min(transferSpeed(),Math.min(mana,Math.max(0,space)));
            if(target!=null&&amount>0){target.receiveMana(amount);receiveMana(-amount);}
        } else if(kind==LegacyMachineBlock.Kind.GENERATOR) {
            for(var side:Direction.values()) {
                var handler=level.getCapability(Capabilities.EnergyStorage.BLOCK,worldPosition.relative(side),side.getOpposite());
                if(handler!=null&&energy<maxEnergy())energy+=Math.max(0,Math.min(maxEnergy()-energy,handler.extractEnergy(Math.min(1000,maxEnergy()-energy),false)));
                pushMana(worldPosition.relative(side));
            }
            int generated=setting("manaGenerator.manaPer1000FE",99);
            int conversions=Math.min(energy/1000,(getMaxMana()-mana)/generated);
            if(conversions>0){energy-=conversions*1000;receiveMana(conversions*generated);}
        } else {
            boolean powered=level.hasNeighborSignal(worldPosition);
            for(var side:Direction.values()){fluid(side,powered);if(!powered)pushMana(worldPosition.relative(side));}
            int energyRate=setting("manaLiquefaction."+(powered?"energyGain":"energyLoss"),2);
            int manaRate=setting("manaLiquefaction."+(powered?"manaGive":"manaReceive"),2000);
            int divisor=gcd(energyRate,manaRate),energyUnit=energyRate/divisor,manaUnit=manaRate/divisor;
            int units=Math.min(divisor,Math.min((powered?maxEnergy()-energy:energy)/energyUnit,(powered?mana:getMaxMana()-mana)/manaUnit));
            energy+=units*energyUnit*(powered?1:-1);receiveMana(units*manaUnit*(powered?-1:1));
        }
        if(mana!=beforeMana||energy!=beforeEnergy){setChanged();pending=true;}
        if(pending&&ticks%10==0){level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);pending=false;}ticks++;
    }
    private ManaReceiver endpoint(BlockPos position) {
        var entity=level.getBlockEntity(position);
        return entity instanceof ManaPoolBlockEntity pool?pool:entity instanceof LegacyMachineEntity machine&&machine.buffer()?machine:null;
    }
    private void pushMana(BlockPos position) {
        if(level.getBlockEntity(position) instanceof ManaSpreaderBlockEntity spreader) {
            int amount=Math.min(mana,Math.min(transferSpeed(),Math.max(0,spreader.getMaxMana()-spreader.getCurrentMana())));
            if(amount>0){spreader.receiveMana(amount);receiveMana(-amount);}
        }
    }
    private void fluid(Direction side,boolean output) {
        var handler=level.getCapability(Capabilities.FluidHandler.BLOCK,worldPosition.relative(side),side.getOpposite());if(handler==null)return;
        int energyRate=setting("manaLiquefaction."+(output?"storagePump":"storageDrain"),output?25:1);
        int fluidRate=setting("manaLiquefaction."+(output?"storagePumpContainer":"storageDrainContainer"),output?25:1);
        int divisor=gcd(energyRate,fluidRate),energyUnit=energyRate/divisor,fluidUnit=fluidRate/divisor;
        int units=Math.min(divisor,(output?energy:maxEnergy()-energy)/energyUnit);if(units<=0)return;
        var fluid=BuiltInRegistries.FLUID.get(ResourceLocation.parse("extrabotany:fluidedmana"));
        var request=new FluidStack(fluid,units*fluidUnit);
        int available=output?handler.fill(request,IFluidHandler.FluidAction.SIMULATE):handler.drain(request,IFluidHandler.FluidAction.SIMULATE).getAmount();
        units=Math.min(units,available/fluidUnit);if(units<=0)return;request.setAmount(units*fluidUnit);
        int committed=output?handler.fill(request,IFluidHandler.FluidAction.EXECUTE):handler.drain(request,IFluidHandler.FluidAction.EXECUTE).getAmount();
        energy+=Math.min(units,committed/fluidUnit)*energyUnit*(output?-1:1);
    }
    private static int gcd(int a,int b){while(b!=0){int c=a%b;a=b;b=c;}return a;}
    @Override public Level getManaReceiverLevel(){return level;}
    @Override public BlockPos getManaReceiverPos(){return worldPosition;}
    @Override public int getCurrentMana(){return mana;}
    @Override public boolean isFull(){return mana>=getMaxMana();}
    @Override public void receiveMana(int amount){int value=(int)Math.max(0,Math.min((long)getMaxMana(),(long)mana+amount));if(value!=mana){mana=value;setChanged();pending=true;}}
    @Override public boolean canReceiveManaFromBursts(){return true;}
    @Override public boolean canAttachSpark(ItemStack stack){return true;}
    @Override public int getAvailableSpaceForMana(){return Math.max(0,getMaxMana()-mana);}
    @Override public boolean areIncomingTransfersDone(){return false;}
    @Override public boolean onUsedByWand(Player player,ItemStack stack,Direction direction){if(level!=null&&!level.isClientSide()){level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);pending=false;}return true;}
    @Override protected void saveAdditional(CompoundTag tag,HolderLookup.Provider registries){super.saveAdditional(tag,registries);tag.putInt("mana",mana);if(!buffer())tag.putInt("energy",energy);}
    @Override protected void loadAdditional(CompoundTag tag,HolderLookup.Provider registries){super.loadAdditional(tag,registries);mana=Math.max(0,Math.min(getMaxMana(),tag.getInt("mana")));energy=Math.max(0,Math.min(maxEnergy(),tag.getInt("energy")));}
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries){return saveWithoutMetadata(registries);}
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
}
