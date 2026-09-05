package io.github.lounode.extrabotany.common.crafting;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;

import io.github.lounode.extrabotany.api.recipe.EntityIngredient;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class EntityTypeIngredient implements EntityIngredient {

	protected final ImmutableSet<EntityType<?>> entityTypes;

	public EntityTypeIngredient(Collection<EntityType<?>> entityTypes) {
		this.entityTypes = ImmutableSet.copyOf(entityTypes);
	}

	@Override
	public boolean test(EntityType<?> entityType) {
		return entityTypes.contains(entityType);
	}

	@Override
	public EntityType<?> pick(RandomSource random) {
		return entityTypes.asList().get(random.nextInt(entityTypes.size()));
	}

	@Override
	public JsonObject serialize() {
		JsonObject object = new JsonObject();

		object.addProperty("type", "entities");
		JsonArray array = new JsonArray();

		for (var type : entityTypes) {
			array.add(BuiltInRegistries.ENTITY_TYPE.getKey(type).toString());
		}
		object.add("entities", array);

		return object;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		List<EntityType<?>> entities = getEntities();

		buffer.writeVarInt(0);
		buffer.writeVarInt(entities.size());
		for (var type : entities) {
			buffer.writeVarInt(BuiltInRegistries.ENTITY_TYPE.getId(type));
		}
	}

	@Override
	public List<EntityType<?>> getDisplayed() {
		return entityTypes.stream().toList();
	}

	public List<EntityType<?>> getEntities() {
		return entityTypes.asList();
	}

	@Override
	public String toString() {
		return "EntityTypeIngredient{" + entityTypes + "}";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		return entityTypes.equals(((EntityTypeIngredient) o).entityTypes);
	}

	@Override
	public int hashCode() {
		return Objects.hash(entityTypes);
	}
}
