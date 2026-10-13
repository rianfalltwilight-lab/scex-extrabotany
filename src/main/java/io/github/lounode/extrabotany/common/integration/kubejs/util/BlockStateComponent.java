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
 * KubeJS 的配方组件：植物魔法的方块状态原料（{@code StateIngredient}）。
 * A recipe component for KubeJS: the block state ingredient of Botania ({@code StateIngredient}).
 *
 * <p>脚本里可以写方块 ID（{@code 'minecraft:obsidian'}）、带状态的方块（{@code 'minecraft:furnace[lit=true]'}）、以 {@code #} 开头的方块标签，或者和数据包 JSON 相同的对象。
 * <p>A script can give a block ID ({@code 'minecraft:obsidian'}), a block with a state ({@code 'minecraft:furnace[lit=true]'}), a block tag that starts with {@code #}, or an object that is the same as the data pack JSON.
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
