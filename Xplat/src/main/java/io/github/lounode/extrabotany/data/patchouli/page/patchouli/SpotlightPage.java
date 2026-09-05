package io.github.lounode.extrabotany.data.patchouli.page.patchouli;

import com.demonwav.mcdev.annotations.Translatable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;

import io.github.lounode.extrabotany.data.patchouli.page.AbstractPage;

import static io.github.lounode.extrabotany.common.lib.RegistryHelper.getRegistryName;

public class SpotlightPage extends AbstractPage<SpotlightPage> {

	public SpotlightPage(String itemString) {
		object.addProperty("item", itemString);
	}

	public SpotlightPage(ItemLike itemLike) {
		this(getRegistryName(itemLike.asItem()).toString());
	}

	public SpotlightPage withTitle(String title) {
		object.addProperty("title", title);
		return this;
	}

	public SpotlightPage linkRecipe(boolean link) {
		object.addProperty("link_recipe", link);
		return this;
	}

	public SpotlightPage withText(@Translatable String text) {
		object.addProperty("text", text);
		return this;
	}

	@Override
	public ResourceLocation getType() {
		return ResourceLocation.tryParse("patchouli:spotlight");
	}
}
