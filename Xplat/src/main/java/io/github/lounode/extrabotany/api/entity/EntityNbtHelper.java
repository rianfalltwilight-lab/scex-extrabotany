package io.github.lounode.extrabotany.api.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

public class EntityNbtHelper {

	public static CompoundTag getNBT(Entity entity) {
		CompoundTag tag = new CompoundTag();
		entity.save(tag);

		return tag;
	}
}
