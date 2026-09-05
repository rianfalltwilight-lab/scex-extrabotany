package io.github.lounode.extrabotany.common.integration.kubejs.util;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;

import dev.latvian.mods.kubejs.plugin.builtin.wrapper.BlockWrapper;
import dev.latvian.mods.kubejs.recipe.RecipeScriptContext;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.rhino.type.TypeInfo;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import vazkii.botania.api.recipe.StateIngredient;
import vazkii.botania.common.crafting.StateIngredients;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * KubeJS component for Botania's typed world-state ingredients.
 */
public final class BlockStateComponent implements RecipeComponent<StateIngredient> {
	public static final RecipeComponentType<StateIngredient> TYPE =
			RecipeComponentType.unit(prefix("state_ingredient"), BlockStateComponent::new);

	private final RecipeComponentType<StateIngredient> type;

	private BlockStateComponent(RecipeComponentType<StateIngredient> type) {
		this.type = type;
	}

	@Override
	public RecipeComponentType<?> type() {
		return type;
	}

	@Override
	public Codec<StateIngredient> codec() {
		return StateIngredients.TYPED_CODEC;
	}

	@Override
	public TypeInfo typeInfo() {
		return TypeInfo.of(StateIngredient.class);
	}

	@Override
	public StateIngredient wrap(RecipeScriptContext context, Object from) {
		if (from instanceof StateIngredient ingredient) {
			return ingredient;
		}
		if (from instanceof Block block) {
			return StateIngredients.of(block);
		}
		if (from instanceof BlockState state) {
			return StateIngredients.of(state);
		}
		if (from instanceof CharSequence chars) {
			String value = chars.toString();
			if (value.startsWith("#")) {
				ResourceLocation id = ResourceLocation.tryParse(value.substring(1));
				if (id == null) {
					throw new IllegalArgumentException("Invalid block tag: " + value);
				}
				return StateIngredients.of(TagKey.create(Registries.BLOCK, id));
			}
			return StateIngredients.of(BlockWrapper.parseBlockState(context.registries(), value));
		}
		if (from instanceof JsonElement json) {
			return StateIngredients.TYPED_CODEC.parse(context.ops().json(), json).getOrThrow();
		}
		if (from instanceof java.util.Map<?, ?>) {
			return StateIngredients.TYPED_CODEC.parse(context.ops().java(), from).getOrThrow();
		}
		throw new IllegalArgumentException("Unsupported state ingredient: " + from);
	}
}
