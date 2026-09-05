package io.github.lounode.extrabotany.common.block.flower.functional;

import io.github.lounode.extrabotany.common.block.flower.ExtraFunctionalFlowerBlockEntity;
import io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import vazkii.botania.api.block_entity.RadiusDescriptor;

/** The archived lotus charges a single paper ticket and teleports a batch in this dimension. */
public final class StardustLotusBlockEntity extends ExtraFunctionalFlowerBlockEntity {
    private BlockPos target;
    private int consumedMana;
    private boolean hasPaper;

    public StardustLotusBlockEntity(BlockPos pos, BlockState state) {
        super(ExtrabotanyFlowerBlocks.STARDUST_LOTUS, pos, state);
    }

    @Override public void tickFlower() {
        super.tickFlower();
        if (getLevel().isClientSide() || isPowered() || target == null || !isMultiblockValid()) return;
        int cost = getTeleportCost();
        if (cost <= 0) return;
        var bounds = new AABB(getEffectivePos()).inflate(1);
        if (!hasPaper) {
            for (var item : getLevel().getEntitiesOfClass(ItemEntity.class, bounds,
                    entity -> entity.isAlive() && entity.getItem().is(Items.PAPER))) {
                item.getItem().shrink(1);
                hasPaper = true;
                sync();
                break;
            }
        }
        if (hasPaper && consumedMana < cost) {
            int amount = Math.min(Math.min(getConsumeSpeed(), getMana()), cost - consumedMana);
            if (amount > 0) { addMana(-amount); consumedMana += amount; sync(); }
            return;
        }
        if (consumedMana >= cost) {
            var passengers = getLevel().getEntitiesOfClass(LivingEntity.class, bounds, LivingEntity::isAlive);
            if (!passengers.isEmpty()) {
                for (var entity : passengers) entity.teleportTo(target.getX() + .5, target.getY() + 1, target.getZ() + .5);
                consumedMana = 0;
                hasPaper = false;
                sync();
            }
        }
    }

    public boolean isMultiblockValid() {
        for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++) {
            if ((Math.abs(x) == 2 || Math.abs(z) == 2)
                    && !getLevel().getBlockState(worldPosition.offset(x, -1, z)).is(Blocks.QUARTZ_BLOCK)) return false;
        }
        for (int x : new int[] {-2, 2}) for (int z : new int[] {-2, 2}) {
            for (int y = 0; y < 2; y++)
                if (!getLevel().getBlockState(worldPosition.offset(x, y, z)).is(Blocks.LAPIS_BLOCK)) return false;
            if (!getLevel().getBlockState(worldPosition.offset(x, 2, z)).is(Blocks.SEA_LANTERN)) return false;
        }
        return true;
    }

    public void setTarget(BlockPos position) { target = position.immutable(); consumedMana = 0; hasPaper = false; sync(); }
    public BlockPos getTarget() { return target; }
    public int getConsumedMana() { return consumedMana; }
    public boolean hasPaper() { return hasPaper; }
    private int setting(String key, int fallback) {
        return io.github.lounode.extrabotany.xplat.ExtraBotanyConfig.common().legacyMachineSetting("stardustLotus." + key, fallback);
    }
    public int getTeleportCost() {
        return target == null ? 0 : (int) ((setting("baseCost", 20000)
                + Math.sqrt(target.distSqr(worldPosition)) * setting("costPerBlock", 150)) * (isValidBinding() ? .8F : 1F));
    }
    public int getConsumeSpeed() { return setting("consumeSpeed", 800); }
    @Override public int getMaxMana() { return setting("maxMana", 100000); }
    @Override public int getColor() { return 0x800080; }
    @Override public RadiusDescriptor getRadius() { return RadiusDescriptor.Rectangle.square(getEffectivePos(), 1); }
    @Override public void writeToPacketNBT(CompoundTag tag) {
        tag.putInt("consumedMana", consumedMana);
        tag.putBoolean("hasPaper", hasPaper);
        tag.putBoolean("hasTarget", target != null);
        if (target != null) { tag.putInt("targetX", target.getX()); tag.putInt("targetY", target.getY()); tag.putInt("targetZ", target.getZ()); }
    }
    @Override public void readFromPacketNBT(CompoundTag tag) {
        consumedMana = tag.getInt("consumedMana");
        hasPaper = tag.getBoolean("hasPaper");
        target = tag.getBoolean("hasTarget") ? new BlockPos(tag.getInt("targetX"), tag.getInt("targetY"), tag.getInt("targetZ")) : null;
    }
}
