package io.github.lounode.extrabotany.common.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import java.util.UUID;

/** UUID ownership shared by legacy area effects; the original Owner tag stays readable. */
public abstract class LegacyOwnedEntity extends Entity {
    private UUID ownerId;
    private Entity owner;
    protected LegacyOwnedEntity(EntityType<?> type, Level level) { super(type, level); }
    public void setOwner(Entity entity) { owner = entity; ownerId = entity == null ? null : entity.getUUID(); }
    public Entity getOwner() {
        if (owner != null && !owner.isRemoved()) return owner;
        if (ownerId != null && level() instanceof ServerLevel server) owner = server.getEntity(ownerId);
        return owner;
    }
    protected boolean hasOwner() { return ownerId != null; }
    @Override protected void addAdditionalSaveData(CompoundTag tag) { if (ownerId != null) tag.putUUID("Owner", ownerId); }
    @Override protected void readAdditionalSaveData(CompoundTag tag) { ownerId = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null; owner = null; }
}
