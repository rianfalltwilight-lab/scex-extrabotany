package io.github.lounode.extrabotany.common.block.legacy;

import io.github.lounode.extrabotany.common.block.block_entity.ExtraBotanyBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import vazkii.botania.api.block.PetalApothecary;

public final class LegacyBarrelEntity extends BlockEntity implements IFluidHandler {
    private Fluid fluid = Fluids.EMPTY;
    private int amount;
    public LegacyBarrelEntity(BlockPos pos, BlockState state) { super(ExtraBotanyBlockEntities.LIVINGROCK_BARREL, pos, state); }
    public void serverTick() {
        if (fluid != Fluids.WATER || amount < 1000) return;
        for (var side : Direction.values()) {
            if (level.getBlockEntity(worldPosition.relative(side)) instanceof PetalApothecary apothecary
                    && apothecary.getFluid() == PetalApothecary.State.EMPTY) {
                apothecary.setFluid(PetalApothecary.State.WATER);
                drain(1000, FluidAction.EXECUTE);
                return;
            }
        }
    }
    @Override public int getTanks() { return 1; }
    @Override public FluidStack getFluidInTank(int tank) { return tank == 0 && amount > 0 ? new FluidStack(fluid, amount) : FluidStack.EMPTY; }
    @Override public int getTankCapacity(int tank) { return tank == 0 ? 16000 : 0; }
    @Override public boolean isFluidValid(int tank, FluidStack stack) { return tank == 0 && !stack.isEmpty() && (amount == 0 || fluid == stack.getFluid()); }
    @Override public int fill(FluidStack stack, FluidAction action) {
        if (!isFluidValid(0, stack)) return 0;
        int accepted = Math.min(stack.getAmount(), 16000 - amount);
        if (accepted > 0 && action.execute()) { fluid = stack.getFluid(); amount += accepted; changed(); }
        return accepted;
    }
    @Override public FluidStack drain(FluidStack stack, FluidAction action) {
        return !stack.isEmpty() && stack.getFluid() == fluid ? drain(stack.getAmount(), action) : FluidStack.EMPTY;
    }
    @Override public FluidStack drain(int requested, FluidAction action) {
        int removed = Math.min(Math.max(0, requested), amount);
        if (removed == 0) return FluidStack.EMPTY;
        var result = new FluidStack(fluid, removed);
        if (action.execute()) { amount -= removed; if (amount == 0) fluid = Fluids.EMPTY; changed(); }
        return result;
    }
    private void changed() {
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (amount > 0) { tag.putString("fluid", BuiltInRegistries.FLUID.getKey(fluid).toString()); tag.putInt("amount", amount); }
    }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        var id = ResourceLocation.tryParse(tag.getString("fluid"));
        fluid = id == null ? Fluids.EMPTY : BuiltInRegistries.FLUID.getOptional(id).orElse(Fluids.EMPTY);
        amount = Math.max(0, Math.min(16000, tag.getInt("amount")));
        if (amount == 0 || fluid == Fluids.EMPTY) { amount = 0; fluid = Fluids.EMPTY; }
    }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveWithoutMetadata(registries); }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
}
