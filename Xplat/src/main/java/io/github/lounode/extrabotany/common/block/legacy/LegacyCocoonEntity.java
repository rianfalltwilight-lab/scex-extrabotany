package io.github.lounode.extrabotany.common.block.legacy;

import io.github.lounode.extrabotany.common.block.block_entity.ExtraBotanyBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import java.util.Map;
import static java.util.Map.entry;

public final class LegacyCocoonEntity extends BlockEntity {
    private static final Map<Item, EntityType<? extends LivingEntity>> HATCHES = Map.ofEntries(
            entry(Items.CHORUS_FRUIT, EntityType.SHULKER), entry(Items.COAL_BLOCK, EntityType.WITHER_SKELETON),
            entry(Items.WHITE_WOOL, EntityType.SHEEP), entry(Items.LEATHER, EntityType.COW),
            entry(Items.BONE, EntityType.SKELETON), entry(Items.ROTTEN_FLESH, EntityType.ZOMBIE),
            entry(Items.WHEAT_SEEDS, EntityType.CHICKEN), entry(Items.FEATHER, EntityType.CHICKEN),
            entry(Items.BEETROOT_SEEDS, EntityType.CHICKEN), entry(Items.MELON_SEEDS, EntityType.CHICKEN),
            entry(Items.PUMPKIN_SEEDS, EntityType.CHICKEN), entry(Items.WHEAT, EntityType.PIG),
            entry(Items.ENDER_PEARL, EntityType.ENDERMAN), entry(Items.GUNPOWDER, EntityType.CREEPER),
            entry(Items.GOLD_INGOT, EntityType.ZOMBIFIED_PIGLIN), entry(Items.BLAZE_ROD, EntityType.BLAZE),
            entry(Items.GHAST_TEAR, EntityType.GHAST), entry(Items.EMERALD_BLOCK, EntityType.VILLAGER));
    private ItemStack item = ItemStack.EMPTY;
    private int timePassed, rotation;
    public LegacyCocoonEntity(BlockPos pos, BlockState state) { super(ExtraBotanyBlockEntities.COCOON_OF_DESIRE, pos, state); }
    public ItemStack getItem() { return item; }
    public void setItem(ItemStack stack) { item = stack.copyWithCount(1); timePassed = 0; changed(); }
    public void clearItem() { item = ItemStack.EMPTY; timePassed = 0; changed(); }
    public int getRotation() { return rotation; }
    @SuppressWarnings("deprecation") // Preserve the original summoned-mob initialization contract.
    public void serverTick() {
        if (!(level instanceof ServerLevel server)) return;
        if (item.isEmpty()) {
            for (var dropped : level.getEntitiesOfClass(ItemEntity.class, new AABB(worldPosition).inflate(1),
                    entity -> entity.isAlive() && HATCHES.containsKey(entity.getItem().getItem()))) {
                setItem(dropped.getItem());
                dropped.getItem().shrink(1);
                if (dropped.getItem().isEmpty()) dropped.discard();
                pickupSound(SoundSource.BLOCKS);
                break;
            }
            return;
        }
        timePassed++;
        rotation = (rotation + 1) % 360;
        // Mark progress dirty so a chunk save before hatching does not reset the timer.
        setChanged();
        if (timePassed < 1200) return;
        var type = HATCHES.get(item.getItem());
        var born = type == null ? null : type.create(server);
        if (born == null) { timePassed = 0; changed(); return; }
        born.moveTo(worldPosition.getX() + .5, worldPosition.getY() + 1, worldPosition.getZ() + .5,
                level.random.nextFloat() * 360, 0);
        if (born instanceof Animal animal) animal.setAge(-24000);
        if (born instanceof Mob mob) mob.finalizeSpawn(server, server.getCurrentDifficultyAt(worldPosition), MobSpawnType.MOB_SUMMONED, null);
        if (server.addFreshEntity(born)) { server.levelEvent(2004, worldPosition, 0); clearItem(); }
        else { timePassed = 0; changed(); }
    }
    public void pickupSound(SoundSource category) {
        level.playSound(null, worldPosition, SoundEvents.ITEM_PICKUP, category, .5F,
                (level.random.nextFloat() - level.random.nextFloat()) * .2F + 1);
    }
    private void changed() {
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("timePassed", timePassed); tag.putInt("Rot", rotation);
        if (!item.isEmpty()) tag.put("Item", item.save(registries));
    }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        timePassed = Math.clamp(tag.getInt("timePassed"), 0, 1200);
        rotation = Math.clamp(tag.getInt("Rot"), 0, 359);
        item = ItemStack.parseOptional(registries, tag.getCompound("Item"));
    }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveWithoutMetadata(registries); }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
}
