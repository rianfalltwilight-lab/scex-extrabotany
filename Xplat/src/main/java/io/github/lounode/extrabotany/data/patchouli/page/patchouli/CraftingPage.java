package io.github.lounode.extrabotany.data.patchouli.page.patchouli;

import com.demonwav.mcdev.annotations.Translatable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;

import io.github.lounode.extrabotany.data.patchouli.page.AbstractPage;

import static io.github.lounode.extrabotany.common.lib.RegistryHelper.getRegistryName;

public class CraftingPage extends AbstractPage<CraftingPage> {

	public CraftingPage(String recipe) {
		object.addProperty("recipe", recipe);
	}

	public CraftingPage(ItemLike itemLike) {
		this(getRegistryName(itemLike.asItem()).toString());
	}

	public CraftingPage withRecipe2(String recipe) {
		object.addProperty("recipe2", recipe);
		return this;
	}

	public CraftingPage withRecipe2(ItemLike recipe) {
		object.addProperty("recipe2", getRegistryName(recipe.asItem()).toString());
		return this;
	}

	public CraftingPage withTitle(@Translatable String title) {
		object.addProperty("title", title);
		return this;
	}

	public CraftingPage withText(@Translatable String text) {
		object.addProperty("text", text);
		return this;
	}

	@Override
	public ResourceLocation getType() {
		return ResourceLocation.tryParse("patchouli:crafting");
	}
}
