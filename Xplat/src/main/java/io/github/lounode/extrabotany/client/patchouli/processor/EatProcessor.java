package io.github.lounode.extrabotany.client.patchouli.processor;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

import vazkii.patchouli.api.IComponentProcessor;
import vazkii.patchouli.api.IVariable;
import vazkii.patchouli.api.IVariableProvider;

public class EatProcessor implements IComponentProcessor {
	private ItemLike output;

	@Override
	public void setup(Level level, IVariableProvider variables) {
		String itemId = variables.get("output", level.registryAccess()).asString();
		output = BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(itemId));
	}

	@Override
	public IVariable process(Level level, String key) {
		return switch (key) {
			case "operate" -> {
				Component q = Component.literal("(?)").withStyle(ChatFormatting.BOLD);
				yield IVariable.from(Component.translatable("extrabotany.patchouli.template.eat.operate").append(" ").append(q), level.registryAccess());
			}
			case "tip" -> IVariable.from(Component.translatable("extrabotany.patchouli.template.eat.tip"), level.registryAccess());
			case "heading" -> {
				if (output == null) {
					yield null;
				}
				yield IVariable.from(output.asItem().getDefaultInstance().getHoverName(), level.registryAccess());
			}

			default -> null;
		};
	}
}
