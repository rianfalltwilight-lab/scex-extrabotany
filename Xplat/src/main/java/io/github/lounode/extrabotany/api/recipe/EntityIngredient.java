package io.github.lounode.extrabotany.api.recipe;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;

import java.util.List;
import java.util.function.Predicate;

public interface EntityIngredient extends Predicate<EntityType<?>> {
	@Override
	boolean test(EntityType<?> entityType);

	EntityType<?> pick(RandomSource random);

	JsonObject serialize();

	void write(FriendlyByteBuf buffer);

	List<EntityType<?>> getDisplayed();
}
