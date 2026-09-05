package io.github.lounode.extrabotany.client.patchouli.processor;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import vazkii.botania.client.patchouli.PatchouliUtils;
import vazkii.patchouli.api.IComponentProcessor;
import vazkii.patchouli.api.IVariable;
import vazkii.patchouli.api.IVariableProvider;

import io.github.lounode.extrabotany.api.recipe.PedestalRecipe;
import io.github.lounode.extrabotany.common.crafting.ExtraBotanyRecipeTypes;

import java.util.List;

public class PedestalSmashProcessor implements IComponentProcessor {
	private PedestalRecipe recipe;

	@Override
	public void setup(Level level, IVariableProvider variables) {
		ResourceLocation id = ResourceLocation.tryParse(variables.get("recipe", level.registryAccess()).asString());
		this.recipe = PatchouliUtils.getRecipe(level, ExtraBotanyRecipeTypes.PEDESTAL_SMASH_TYPE, id);
	}

	@Override
	public IVariable process(Level level, String key) {
		if (recipe == null) {
			return null;
		}
		return switch (key) {
			case "output" -> IVariable.from(recipe.getResultItem(level.registryAccess()), level.registryAccess());
			case "input" -> PatchouliUtils.interweaveIngredients(List.of(recipe.getInput()), level);
			case "smash_tools" -> PatchouliUtils.interweaveIngredients(List.of(recipe.getSmashTools()), level);
			case "heading" -> IVariable.from(recipe.getResultItem(level.registryAccess()).getHoverName(), level.registryAccess());
			case "operate" -> {
				Component q = Component.literal("(?)").withStyle(ChatFormatting.BOLD);
				yield IVariable.from(Component.translatable("extrabotany.patchouli.template.pedestal_smash.operate").append(" ").append(q), level.registryAccess());
			}
			case "tip" -> IVariable.from(Component.translatable("extrabotany.patchouli.template.pedestal_smash.tip"), level.registryAccess());

			default -> null;
		};
	}
}
